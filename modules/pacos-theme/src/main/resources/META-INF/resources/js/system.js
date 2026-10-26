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

window.highlightSearchTerm = function(containerElement, term) {
    if (!containerElement) {
        return;
    }
    containerElement.querySelectorAll('mark.search-highlight').forEach(el => {
        const parent = el.parentNode;
        parent.replaceChild(document.createTextNode(el.textContent), el);
        parent.normalize();
    });

    if (!term || term.trim() === "") return;

    const regex = new RegExp(`(${term})`, 'gi');

    const walk = document.createTreeWalker(containerElement, NodeFilter.SHOW_TEXT, null, false);
    const nodes = [];
    while(walk.nextNode()) nodes.push(walk.currentNode);

    nodes.forEach(node => {
        if (node.parentNode.nodeName === 'SCRIPT' || node.parentNode.nodeName === 'STYLE') return;

        const matches = node.data.match(regex);
        if (matches) {
            console.log("found");
            const span = document.createElement('span');
            span.innerHTML = node.data.replace(regex, '<mark class="search-highlight">$1</mark>');
            node.parentNode.replaceChild(span, node);
        }
    });
};