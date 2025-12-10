window.lastActiveDialogId = null;

function informBackendAboutWindowOnTopIfNeeded(overlay, dialog) {
    let currentId = overlay.__dataHost.id;
    if (window.lastActiveDialogId !== currentId) {
        dialog.$server.markWindowOnTop();
        window.lastActiveDialogId = overlay.__dataHost.id;
    }
}

window.monitorWindowOnFront = function(dialog){
    const overlay = dialog.$.overlay;
    overlay.addEventListener('mousedown', () => {
        informBackendAboutWindowOnTopIfNeeded(overlay, dialog);
    });
}

window.bringToFront = function (dialogId,callback) {
    const overlays = document.querySelectorAll('vaadin-dialog-overlay');
    const overlay = Array.from(overlays).find(o => o.__dataHost.id === dialogId);

    if (overlay && callback) {
        informBackendAboutWindowOnTopIfNeeded(overlay, overlay.__dataHost);
        //inform vaadin to recalculate z-index
        const event = new MouseEvent('mousedown', {
            bubbles: true,
            cancelable: true,
            composed: true
        });
        overlay.dispatchEvent(event);

    }
}

