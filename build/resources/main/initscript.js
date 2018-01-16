var script = document.createElement("script");
script.src = "https://ajax.googleapis.com/ajax/libs/jquery/2.2.0/jquery.min.js", document.getElementsByTagName("head")[0].appendChild(script);

function updateRow(params) {
    var row = $('td[data-value="' + params[0] + '"]');
    if (row.length > 0) {
        //Found
        var tds = $(row[0]).parent().find("td");
        for (i = 2; i < tds.length; i++) {
            var h = $(tds[i]),
                j = h.text(),
                k = !1;
            h.css("background-color", "#595959");
            if (j.indexOf("/") > 0) {
                var l = Number(j.substr(0, j.indexOf("/"))),
                    m = Number(j.substr(j.indexOf("/") + 1)),
                    n = Number(params[i].substr(0, j.indexOf("/"))),
                    o = Number(params[i].substr(j.indexOf("/") + 1));
                (l < n /*TODO*/ || m < o /*TODO*/ ) && (e = !0, k = !0)
            } else Number(j.replace("%", "")) < Number(params[i].replace("%", "")) /*TODO*/ && (k = !0);
            k && (h.css("background-color", "#ff0000"), h.css("font-weight", "bold"), h.css("font-size", "150%"), h.css("color", "#000099"))
        }

    } else {
        //New item
    }
}

//updateRow('app/adviser/index/', ['','','100%','6/6','100%','2/2','100%','2/2','100%','7/7']);

function defer(method) {
    if (window.jQuery) {
        method();
    } else {
        setTimeout(function() { defer(method) }, 50);
    }
}