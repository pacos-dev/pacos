window.lastActiveDialogId = null;

function informBackendAboutWindowOnTopIfNeeded(overlay, dialog) {
    if(dialog && dialog.id){
        let currentId = dialog.id;
        if (window.lastActiveDialogId !== currentId) {
            dialog.$server.markWindowOnTop();
            window.lastActiveDialogId = currentId;
        }
    }
}

window.monitorWindowOnFront = function(dialog){
    const overlay = dialog.$.overlay;
    if (!overlay) return;

    if (overlay.__frontListenerInstalled) return;

    const handler = () => {
        if (dialog && dialog.id) {
            const currentId = dialog.id;
            if (window.lastActiveDialogId !== currentId) {
                dialog.$server.markWindowOnTop();
                window.lastActiveDialogId = currentId;
            }
        }
    };

    overlay.__frontListenerInstalled = true;
    overlay.__frontListenerHandler = handler;

    overlay.addEventListener('pointerdown', handler);
}
/**
 * Remove listeners from closed window
 * @param dialog
 */
window.cleanupWindowOnFront = function (dialog) {
    const overlay = dialog?.$.overlay;
    if (!overlay) return;

    if (overlay.__frontListenerInstalled && overlay.__frontListenerHandler) {
        overlay.removeEventListener('pointerdown', overlay.__frontListenerHandler);
    }

    delete overlay.__frontListenerInstalled;
    delete overlay.__frontListenerHandler;
};
/**
 * Called fron backend when icon on dock was clicked and window mus be moved to the top
 * @param dialog
 * @param callback
 */
window.bringToFront = function (dialog,callback) {
    const overlay = dialog.$.overlay;

    if (overlay && callback) {
        informBackendAboutWindowOnTopIfNeeded(overlay, dialog);
        const event = new MouseEvent('mousedown', {
            bubbles: true,
            cancelable: true,
            composed: true
        });
        setTimeout(() => {
            if (overlay) {
                overlay.dispatchEvent(event);
                overlay.bringToFront();
            }
        },50);
    }
}

window.pacos = window.pacos || {};

window.pacos.textHighlighter = (() => {
    const states = new WeakMap();
    const styleId = 'pacos-text-highlighter-styles';
    const styleContent = [
        'mark.search-highlight { background: var(--pacos-search-highlight-background, #ffeb3b); color: var(--pacos-search-highlight-color, #000); padding: 0 1px; border-radius: 2px; }',
        'mark.search-highlight-active { background: var(--pacos-search-highlight-active-background, #ff9800); color: var(--pacos-search-highlight-active-color, #000); outline: 1px solid var(--pacos-search-highlight-active-outline, #e65100); }'
    ].join('');

    function getRoots(root) {
        const roots = [root];
        const visited = new Set(roots);

        for (let index = 0; index < roots.length; index++) {
            const currentRoot = roots[index];
            if (currentRoot.nodeType === Node.ELEMENT_NODE && currentRoot.shadowRoot && !visited.has(currentRoot.shadowRoot)) {
                visited.add(currentRoot.shadowRoot);
                roots.push(currentRoot.shadowRoot);
            }

            const walker = document.createTreeWalker(currentRoot, NodeFilter.SHOW_ELEMENT);
            while (walker.nextNode()) {
                const shadowRoot = walker.currentNode.shadowRoot;
                if (shadowRoot && !visited.has(shadowRoot)) {
                    visited.add(shadowRoot);
                    roots.push(shadowRoot);
                }
            }
        }

        return roots;
    }

    function disconnectObservers(state) {
        state.observers.forEach(observer => observer.disconnect());
        state.observers = [];
    }

    function clearMarks(roots) {
        roots.forEach(root => {
            if (root.nodeType === Node.ELEMENT_NODE && root.matches('mark.search-highlight')) {
                const parent = root.parentNode;
                if (parent) {
                    parent.replaceChild(document.createTextNode(root.textContent), root);
                    parent.normalize();
                }
            }

            root.querySelectorAll('mark.search-highlight').forEach(mark => {
                const parent = mark.parentNode;
                if (parent) {
                    parent.replaceChild(document.createTextNode(mark.textContent), mark);
                    parent.normalize();
                }
            });
        });
    }

    function ensureShadowStyles(root) {
        if (root.nodeType !== Node.DOCUMENT_FRAGMENT_NODE || !root.host || root.getElementById(styleId)) {
            return;
        }

        const style = document.createElement('style');
        style.id = styleId;
        style.textContent = styleContent;
        root.appendChild(style);
    }

    function shouldHighlight(node) {
        let parent = node.parentElement;
        while (parent) {
            const tagName = parent.tagName;
            if (tagName === 'SCRIPT' || tagName === 'STYLE' || tagName === 'NOSCRIPT'
                || tagName === 'TEXTAREA' || tagName === 'INPUT' || tagName === 'SELECT'
                || tagName === 'OPTION' || parent.isContentEditable
                || (parent.matches && parent.matches('mark.search-highlight'))) {
                return false;
            }
            parent = parent.parentElement;
        }
        return true;
    }

    function highlightTextNode(node, query, activeMatchIndex, matchCounter) {
        const originalText = node.data;
        const searchableText = originalText.toLowerCase();
        const searchableQuery = query.toLowerCase();
        let offset = 0;
        let cursor = 0;
        let matchIndex = searchableText.indexOf(searchableQuery, offset);

        if (matchIndex === -1) {
            return matchCounter;
        }

        const fragment = document.createDocumentFragment();
        while (matchIndex !== -1) {
            if (matchIndex > cursor) {
                fragment.appendChild(document.createTextNode(originalText.slice(cursor, matchIndex)));
            }

            const endIndex = matchIndex + query.length;
            const mark = document.createElement('mark');
            mark.className = 'search-highlight';
            if (matchCounter === activeMatchIndex) {
                mark.classList.add('search-highlight-active');
                mark.setAttribute('aria-current', 'true');
            }
            mark.textContent = originalText.slice(matchIndex, endIndex);
            fragment.appendChild(mark);

            matchCounter++;
            cursor = endIndex;
            offset = endIndex;
            matchIndex = searchableText.indexOf(searchableQuery, offset);
        }

        if (cursor < originalText.length) {
            fragment.appendChild(document.createTextNode(originalText.slice(cursor)));
        }

        node.parentNode.replaceChild(fragment, node);
        return matchCounter;
    }

    function render(state) {
        disconnectObservers(state);
        const roots = getRoots(state.root);
        clearMarks(roots);

        if (!state.query) {
            return;
        }

        roots.forEach(ensureShadowStyles);
        let matchCounter = 0;

        roots.forEach(root => {
            const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
            const textNodes = [];
            while (walker.nextNode()) {
                if (shouldHighlight(walker.currentNode)) {
                    textNodes.push(walker.currentNode);
                }
            }

            textNodes.forEach(node => {
                matchCounter = highlightTextNode(node, state.query, state.activeMatchIndex, matchCounter);
            });
        });

        if (typeof MutationObserver !== 'undefined') {
            roots.forEach(root => {
                const observer = new MutationObserver(() => scheduleRender(state));
                observer.observe(root, {childList: true, characterData: true, subtree: true});
                state.observers.push(observer);
            });
        }
    }

    function scheduleRender(state) {
        if (state.renderScheduled) {
            return;
        }

        state.renderScheduled = true;
        queueMicrotask(() => {
            state.renderScheduled = false;
            if (states.get(state.root) === state) {
                render(state);
            }
        });
    }

    function highlight(root, query, activeMatchIndex) {
        if (!root) {
            return;
        }
        if (typeof query !== 'string' || query.trim() === '') {
            clear(root);
            return;
        }

        let state = states.get(root);
        if (!state) {
            state = {root, query, activeMatchIndex: Number.isInteger(activeMatchIndex) ? activeMatchIndex : -1,
                observers: [], renderScheduled: false};
            states.set(root, state);
        } else {
            state.query = query;
            state.activeMatchIndex = Number.isInteger(activeMatchIndex) ? activeMatchIndex : -1;
        }

        render(state);
    }

    function clear(root) {
        if (!root) {
            return;
        }

        const state = states.get(root);
        if (state) {
            disconnectObservers(state);
            states.delete(root);
        }
        clearMarks(getRoots(root));
    }

    return {highlight, clear};
})();