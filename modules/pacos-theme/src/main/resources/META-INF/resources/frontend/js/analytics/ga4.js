(() => {
    const measurementTag = document.querySelector('meta[name="pacos-ga4-measurement-id"]');
    const measurementId = measurementTag?.content?.trim();

    if (!measurementId || window.__pacosGaInitialized) {
        return;
    }

    window.__pacosGaInitialized = true;
    window.dataLayer = window.dataLayer || [];
    window.gtag = window.gtag || function () {
        window.dataLayer.push(arguments);
    };

    window.gtag('js', new Date());
    window.gtag('config', measurementId);
    window.gtag('event', 'demo_open', {app_name: 'PacOS'});

    const script = document.createElement('script');
    script.async = true;
    script.src = 'https://www.googletagmanager.com/gtag/js?id=' + encodeURIComponent(measurementId);
    document.head.appendChild(script);
})();
