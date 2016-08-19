// Replacement of the function on help-button click event
Behaviour.specify("A.help-button", 'hudson-behavior', 1, function(e) {
    e.onclick = function() {
        var tr = findFollowingTR(this, "help-area");            
        var thistr = $(this).up().up();

        // Check if the custom help area is open
        if (!$(tr).hasClassName("custom-help")) {
            // Created element tr for custom help
            var customtr = document.createElement("tr");
            customtr.innerHTML = '  <td></td><td colspan="2"><div class="help"><div class="custom-help-button">'+
            '<div class="custom-help-button-save" onclick="save_custom_help(this)" >Save</div><div class="custom-help-button-edit" onclick="edit_custom_help(this)" >Edit</div>'+
            '</div><div class="custom-help-text">Loading1...</div></div></td><td></td>';
            $(customtr).addClassName("help-area");
            $(customtr).addClassName("custom-help");
            $(thistr).insert({after:customtr});
            // Modification of the inner structure of the created element
            if(isAdminHelp) {
                $(customtr).down().next().down().down().down().next().style.display = "block";
            }
            // Location for the default help
            var div = $(tr).down().next().down();
            // Location for the custom help
            var div2 = $(customtr).down().next().down().down().next()
            var div3 = $(customtr).down().next().down();
            div.style.display = "block";
            div3.style.display = "block";

            // make it visible
            new Ajax.Request(this.getAttribute("helpURL"), {
                method : 'get',
                onSuccess : function(x) {
                    var from = x.getResponseHeader("X-Plugin-From");
                    div.innerHTML = x.responseText+(from?"<div class='from-plugin'>"+from+"</div>":"");
                    layoutUpdateCallback.call();
                },
                onFailure : function(x) {
                    div.innerHTML = "<b>ERROR</b>: Failed to load help file: " + x.statusText;
                    layoutUpdateCallback.call();
                }
            });
            
            // Creation of unique id
            var customHelpClassName = this.getAttribute("helpURL").replace(rootURL+"/", "").replace("/", ".");
            while (customHelpClassName.indexOf("/") + 1) {
                customHelpClassName = customHelpClassName.replace("/", ".")
            }

            var customurl = rootURL + "/helpmanager/helpInfo?class="+customHelpClassName;
                $(div2).setAttribute("customHelpUrl", rootURL+"/helpmanager/updateHelpInfo");
                $(div2).setAttribute("className", customHelpClassName);

            new Ajax.Request(customurl, {
                method : 'get',
                onSuccess : function(x) {
                    var from = x.getResponseHeader("X-Plugin-From");
                    div2.innerHTML = x.responseText+(from?"<div class='from-plugin'>"+from+"</div>":"");
                    layoutUpdateCallback.call();
                },
                onFailure : function(x) {
                    div2.innerHTML = "<b>ERROR</b>: Failed to load help file: " + x.statusText;
                    layoutUpdateCallback.call();
                }
            });

        } else {
            var div = $(findFollowingTR(tr, "help-area")).down().next().down();
            div.style.display = "none";
            $(tr).remove();
            layoutUpdateCallback.call();
        }

        return false;
    };

    e.tabIndex = 9999; 
    e = null;
        
});

function save_custom_help(myel) {
    $(myel).style.display = "none";
    $(myel).next().style.display = "block";
    var div = $(myel).up().next();
    div.innerHTML = div.down().value;

    var xmlhttp;
    if (window.XMLHttpRequest) {
        xmlhttp = new XMLHttpRequest();
    }
    else {
        xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
    }

    xmlhttp.open("POST",$(div).getAttribute("customHelpUrl"),true);
    xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
    xmlhttp.send("class="+$(div).getAttribute("className")+"&textArea="+div.innerHTML);

    return false;
};

function edit_custom_help(myel) {
    $(myel).style.display = "none";
        $(myel).previous().style.display = "block";
        var div = $(myel).up().next();
        div.innerHTML = "<textarea name='textArea'>"+div.innerHTML+"</textarea>";
        div.down().focus();
        return false;
};