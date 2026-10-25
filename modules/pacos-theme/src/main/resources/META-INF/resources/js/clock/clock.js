(function () {
    function currentTime(diff) {

        let date = new Date(new Date().getTime() + diff);

        let hh = date.getHours();
        let mm = date.getMinutes();

        hh = (hh < 10) ? "0" + hh : hh;
        mm = (mm < 10) ? "0" + mm : mm;

        let curTime = hh + ":" + mm;
        if (document.getElementById("clock").innerText !== curTime) {
            document.getElementById("clock").innerText = curTime;
        }
        setTimeout(function () {
            currentTime(diff)
        }, 1000);
    }

    window.systemClock = function systemClock() {
        let userTime = new Date().getTime();
        let serverTime = document.getElementById("clock").getAttribute("time");
        let diff = serverTime - userTime;
        // console.log("Diff time between server and client " + diff)
        currentTime(diff);
    };
})();

