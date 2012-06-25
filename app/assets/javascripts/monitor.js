function create(elt) {
    return window.document.createElement(elt);
}

function SpeedOMeter(config) {
    this.maxVal = config.maxVal;
    this.unit = config.unit ? config.unit + " " : "";
    this.name = config.name;
    this.container = config.container;
    this.elt = create("div");
    this.elt.className = "monitor";

    var title = create("span");
    title.innerHTML = this.name;
    title.className = 'title';
    this.elt.appendChild(title);

    this.screenCurrent = create("span");
    this.screenCurrent.className = 'screen current';
    this.elt.appendChild(this.screenCurrent);

    this.screenMax = create("span");
    this.screenMax.className = 'screen max';
    this.screenMax.innerHTML = this.maxVal + this.unit;
    this.elt.appendChild(this.screenMax);

    this.needle = create("div");
    this.needle.className = "needle";
    this.elt.appendChild(this.needle);

    this.light = create("div");
    this.light.className = "green light";
    this.elt.appendChild(this.light);

    var wheel = create("div");
    wheel.className = "wheel";
    this.elt.appendChild(wheel);

    this.container.appendChild(this.elt);
}

SpeedOMeter.prototype.red = function () {
    this.light.className = "red light";
};

SpeedOMeter.prototype.green = function () {
    this.light.className = "red green";
};

SpeedOMeter.prototype.update = function (val) {
    Zanimo.transition(
        this.needle,
        "transform",
        "rotate(" + (val > this.maxVal ? 175 : val * 170 / this.maxVal) + "deg)",
        500,
        "ease-in"
    );
    this.screenCurrent.innerHTML = val + this.unit;
}

var threads
var memory
var cpu
var lastCall
var monitorPushSource

function initMonitors( element ) {
    threads = new SpeedOMeter({
        name : "THREADS",
        maxVal : 300,
        container : element
    })

    memory = new SpeedOMeter({
        name : "MEMORY",
        maxVal : 1024,
        unit : "MB",
        container : element
    })

    cpu = new SpeedOMeter({
        name : "CPU",
        maxVal : 100,
        unit : "%",
        container : element
    })
}

function runMonitors() {
    setTimeout(openMonitoringConnection, 500)
    setInterval(function() {
        if ((new Date()).getTime() - lastCall > 5000) {
            threads.red()
            memory.red()
            cpu.red()
        }
    }, 300)
}

function stopMonitors() {
    monitorPushSource.close()
}

function updateMonitoringUI( data) {
    if (data.thread != undefined) {
        threads.update(data.thread)
    }
    if (data.mem != undefined) {
        memory.update(data.mem)
    }
    if (data.cpu != undefined) {
        cpu.update(data.cpu)
    }
}

function openMonitoringConnection() {
    monitorPushSource = new EventSource( '/monitoring' )
    monitorPushSource.onmessage = function ( event ) {
        var data = JSON.parse( event.data )
        lastCall = (new Date()).getTime()
        updateMonitoringUI( data )
    }
}