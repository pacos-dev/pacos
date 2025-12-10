(function () {
    let dragSrcEl = null;
    let placeholder = null;
    let isDraggingStarted = false;
    let x = 0;

    function handleDragStart(e) {
        if (dragSrcEl != null) {
            handleDragEnd(e);
        }
        dragSrcEl = e.target.parentElement.parentElement;
        dragSrcEl.parentElement.classList.add('draggable');
        const rect = dragSrcEl.parentElement.getBoundingClientRect();
        x = rect.x + 23;

        dragSrcEl.style.zIndex = '200';
        dragSrcEl.style.pointerEvents = "none";

        document.addEventListener('mouseup', handleDragEnd, false);
        document.addEventListener('mousemove', mouseMoveHandler);
    }

    function handleDragEnd(e) {
        dragSrcEl.parentElement.classList.remove('draggable');
        if (placeholder) {
            placeholder.parentNode.removeChild(placeholder);
        }
        // Reset the flag
        isDraggingStarted = false;
        dragSrcEl.style.removeProperty('top');
        dragSrcEl.style.removeProperty('left');
        dragSrcEl.style.removeProperty('position');
        dragSrcEl.style.removeProperty('z-index');
        dragSrcEl.style.removeProperty('pointer-events');
        x = null;
        dragSrcEl = null;
        document.removeEventListener('mousemove', mouseMoveHandler);
        document.removeEventListener('mouseup', handleDragEnd);
    }

    const mouseMoveHandler = function (e) {
        // Set position for dragging element
        const draggingRect = dragSrcEl.getBoundingClientRect();

        if (!isDraggingStarted) {
            // Update the flag
            isDraggingStarted = true;

            // Let the placeholder take the height of dragging element
            // So the next element won't move up
            placeholder = document.createElement('li');
            placeholder.classList.add('placeholder');
            dragSrcEl.parentNode.insertBefore(
                placeholder,
                dragSrcEl.nextSibling
            );

            // Set the placeholder's height
            placeholder.style.height = `${draggingRect.height}px`;
        }

        dragSrcEl.style.position = 'absolute';
        dragSrcEl.style.left = `${e.clientX - x}px`;
        const prevEle = dragSrcEl.previousElementSibling;
        const nextEle = placeholder.nextElementSibling;
        if (prevEle && isAbove(dragSrcEl, prevEle)) {
            swap(placeholder, dragSrcEl);
            swap(placeholder, prevEle);
            return;
        }
        if (nextEle && isAbove(nextEle, dragSrcEl)) {
            swap(nextEle, placeholder);
            swap(nextEle, dragSrcEl);
        }

    };

    const isAbove = function (nodeA, nodeB) {
        // Get the bounding rectangle of nodes
        const rectA = nodeA.getBoundingClientRect();
        const rectB = nodeB.getBoundingClientRect();
        return rectA.left + (rectA.height / 2) < rectB.left + (rectB.height / 2);
    };

    const swap = function (nodeA, nodeB) {
        const parentA = nodeA.parentNode;
        const siblingA = nodeA.nextSibling === nodeB ? nodeA : nodeA.nextSibling;

        // Move `nodeA` to before the `nodeB`
        nodeB.parentNode.insertBefore(nodeA, nodeB);

        // Move `nodeB` to before the sibling of `nodeA`
        parentA.insertBefore(nodeB, siblingA);
    };

    window.dockDraggable = function dockDraggable() {
        let items = document.querySelectorAll('.osx-dock li');
        items.forEach(function (item) {
            item.addEventListener('dragstart', handleDragStart, false);
        });
    };

})();

