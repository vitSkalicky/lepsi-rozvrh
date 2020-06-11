function fallbackCopyTextToClipboard(text) {
    var textArea = document.createElement("textarea");
    textArea.value = text;
    
    // Avoid scrolling to bottom
    textArea.style.top = "0";
    textArea.style.left = "0";
    textArea.style.position = "fixed";

    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
        var successful = document.execCommand('copy');
        var msg = successful ? 'successful' : 'unsuccessful';
        console.log('Fallback: Copying text command was ' + msg);
    } catch (err) {
        console.error('Fallback: Oops, unable to copy', err);
    }

    document.body.removeChild(textArea);
}
function copy(text) {
    if (!navigator.clipboard) {
        fallbackCopyTextToClipboard(text);
        return;
    }
    navigator.clipboard.writeText(text).then(function() {
        console.log('Async: Copying to clipboard was successful!');
    }, function(err) {
        console.error('Async: Could not copy text: ', err);
    });

}

document.addEventListener("DOMContentLoaded", function() {
    var buttons = document.getElementsByClassName("copy-button");
    var i;
    for (i = 0; i < buttons.length; i++) {
        const item = buttons[i];
        item.onclick = function(){
            copy(item.closest("section").getElementsByClassName("theme-string")[0].innerHTML.trim());
            item.classList.add("green-button");
            //item.style.backgroundColor = "green";
            item.innerHTML = "Zkopírováno"
            var temp = setInterval( function(){
                item.classList.remove("green-button");
                item.innerHTML = "Kopírovat"
                clearInterval(temp);
            }, 10000 );
        }
    }
    var import_bs = document.getElementsByClassName("import-button");
    var i;
    for (i = 0; i < import_bs.length; i++) {
        var item = import_bs[i];
        //item.setAttribute("href", "https://vitskalicky.github.io/lepsi-rozvrh/motiv-info?data=" + item.closest("section").getElementsByClassName("theme-string")[0].innerHTML.trim().split('+').join('-').split('/').join('_'))
        item.setAttribute("href", "https://vitskalicky.github.io/lepsi-rozvrh/motiv-info/?data=" + item.closest("section").getElementsByClassName("theme-string")[0].innerHTML.trim().split('+').join('-').split('/').join('_'))
    }

    /*var dialogdiv = document.createElement("div");
	dialogdiv.setAttribute('class',"dialog");
	dialogdiv.innerHTML = '<a id="qr-close"></a><div class="qr-img" id="qr-img" title="qr code" ></div>';
	document.body.appendChild(dialogdiv);*/
});


/*buttons.forEach(element => {
    element.onclick = copy("hoj", element);
});*/



// just a dummy function
function kopirovat(){}

