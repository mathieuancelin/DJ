var DJ = (typeof module !== "undefined" && module.exports) || {};

(function (exports) {
    exports.name = "main.js";
    exports.version = "1.0.0";
    exports.log = log;
    exports.debug = debug;
    exports.trace = trace;
    exports.startsWith = startsWith;
    exports.param = param;

    function log(message) {
        console.log("[INFO] %s", message)
    }

    function debug(message) {
        console.log("[DEBUG] %s", message)
    }

    function trace(message) {
        console.log("[TRACE] %s", message)
    }

    function startsWith(value, starts) {
        return (value.match("^"+starts)==starts)
    }

    function param(name){
        var strReturn = "";
        var strHref = window.location.href;
        if ( strHref.indexOf("?") > -1 ){
            var strQueryString = strHref.substr(strHref.indexOf("?")).toLowerCase();
            var aQueryString = strQueryString.split("&");
            for ( var iParam = 0; iParam < aQueryString.length; iParam++ ){
                if ( 
                aQueryString[iParam].indexOf(name.toLowerCase() + "=") > -1 ){
                    var aParam = aQueryString[iParam].split("=");
                    strReturn = aParam[1];
                    break;
                }
            }
        }
        return unescape(strReturn);
    }

    String.prototype.startsWith = function(str) {
        return startsWith(this, str)
    }

})(DJ);