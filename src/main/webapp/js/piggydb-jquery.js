//
// utilities
//
(function($){
  $.fn.putLoadingIcon = function (style) {
    var html = '<span><img src="images/load.gif" border="0"';
    if (style != null) html = html + ' style="' + style + '"';
    html = html + '/><\/span>';
    return $(html).appendTo(this);
  };
  
  $.fn.insertAtCaret = function(text, textRange) {
  	return this.each(function() {
  		var textarea = this;
	    textarea.focus();
	    if (document.selection) {
	      var range = textRange != null ? textRange : document.selection.createRange();
	      range.text = text;
	      range.select();
	    } 
	    else {
	      var startPos = textarea.selectionStart;
	      var newCaretPos = startPos + text.length;
	      textarea.value = textarea.value.substr(0, startPos) + text + textarea.value.substr(startPos);
	      textarea.setSelectionRange(newCaretPos, newCaretPos);
	    }
	  });
	};
})(jQuery);



//
// fadingHighlight
//  - Depends on jQuery UI (effects.core)
//
(function($){
  function isTransparent(color) {
    if (color == "transparent") return true;
    if (color.match(/^rgba(.+)0\)$/i)) return true;
    return false;
  }
  
  function getBackgroundColor(element) {
    var color = element.css("background-color");
    if (!isTransparent(color)) return color;
    
    jQuery.each(element.parents(), function(index, parent) {
      color = jQuery(parent).css("background-color");
      if (!isTransparent(color)) return false;
    });
    
    return color;
  }
  
  $.fn.fadingHighlight = function (highlightColor, duration) {
    if (!highlightColor) highlightColor = "#ffcc66";  // default color
    if (!duration) duration = 3000;
    this.each(function() {
      var target = $(this);

      // support for tables
      var tagName = this.tagName.toLowerCase();
      if (tagName === "table" || tagName === "tr") {
        target = $(this).find("td");
      }

      var originalColor = getBackgroundColor(target);
      target.css("background-color", highlightColor);

      target.animate({
          backgroundColor: originalColor
        },
        {
          duration: duration,
          complete: function() {
            target.css("background-color", "");
          }
        });
    });
  };
})(jQuery);



//
// disabled flag
//
(function($){
  var CLASS = "disabled";
  
  $.fn.setDisabledFlag = function() {
    this.each(function() {
      if (!$(this).hasClass(CLASS)) $(this).addClass(CLASS);
    });
  };
  
  $.fn.deleteDisabledFlag = function() {
    this.removeClass(CLASS);
  };
  
  $.fn.hasDisabledFlag = function() {
    if (this.size() == 0) return false;
    return this.eq(0).hasClass(CLASS);
  };
})(jQuery);

