(function () {
    let dragSrcEl = null;
    let placeholder = null;
    let isDraggingStarted = false;
    let yOffset = 0; // Zmieniamy x na yOffset

    function handleDragStart(e) {
        if (dragSrcEl != null) {
            handleDragEnd(e);
        }

        // Pobieramy konkretny element 'li'
        dragSrcEl = e.target.closest('li');
        if (!dragSrcEl) return;

        dragSrcEl.parentElement.classList.add('draggable');

        // KLUCZOWA ZMIANA: rect pobieramy z dragSrcEl (konkretnego elementu li)
        const rect = dragSrcEl.getBoundingClientRect();

        // Obliczamy gdzie dokładnie wewnątrz elementu kliknął użytkownik
        yOffset = e.clientY - rect.top + 30;

        dragSrcEl.style.zIndex = '1000';
        dragSrcEl.style.pointerEvents = "none";

        if (e.type === 'dragstart') {
            if (e.dataTransfer) {
                e.dataTransfer.setDragImage(new Image(), 0, 0);
            }
        }

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
        dragSrcEl.style.removeProperty('width');

        yOffset = null;
        dragSrcEl = null;
        document.removeEventListener('mousemove', mouseMoveHandler);
        document.removeEventListener('mouseup', handleDragEnd);
    }

    const mouseMoveHandler = function (e) {
        if (!dragSrcEl) return;
        // Set position for dragging element
        const draggingRect = dragSrcEl.getBoundingClientRect();

        if (!isDraggingStarted) {
            // Update the flag
            isDraggingStarted = true;

            // Let the placeholder take the height of dragging element
            // So the next element won't move up
            placeholder = document.createElement('li');
            placeholder.classList.add('placeholder');

            placeholder.style.height = `${draggingRect.height}px`;
            placeholder.style.width = `${draggingRect.width}px`;

            dragSrcEl.parentNode.insertBefore(placeholder, dragSrcEl.nextSibling);

            dragSrcEl.style.width = `${draggingRect.width}px`;
        }

        dragSrcEl.style.position = 'fixed';
        dragSrcEl.style.top = `${e.clientY - yOffset}px`;

        const prevEle = dragSrcEl.previousElementSibling;
        const nextEle = placeholder.nextElementSibling;

        if (prevEle && prevEle !== placeholder && isAbove(dragSrcEl, prevEle)) {
            swap(placeholder, dragSrcEl);
            swap(placeholder, prevEle);
            return;
        }

        if (nextEle && nextEle !== placeholder && isAbove(nextEle, dragSrcEl)) {
            swap(nextEle, placeholder);
            swap(nextEle, dragSrcEl);
        }

    };

    const isAbove = function (nodeA, nodeB) {
        const rectA = nodeA.getBoundingClientRect();
        const rectB = nodeB.getBoundingClientRect();

        return (rectA.top + rectA.height / 2) < (rectB.top + rectB.height / 2);
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

