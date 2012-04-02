//
// utilities
//
(function($){
  $.fn.putLoadingIcon = function (style) {
    var html = '<span><img src="images/load.gif" border="0"';
    if (style != null) html = html + ' style="' + style + '"';
    html = html + '/><\/span>';
    return $(html).appendTo(this);
  }
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
  
  $.fn.fadingHighlight = function (highlightColor) {
    this.each(function() {
      var bgColor = getBackgroundColor($(this));
      $(this).css("background-color", highlightColor);
      
      var target = $(this);
      $(this).animate({ 
          backgroundColor: bgColor
        }, 
        {
          duration: 3000,
          complete: function(){ 
            // workaround for a bug not to animate occasionally
            target.css("background-color", bgColor);
          }
        });
    });
  }
})(jQuery);



//
// disabled flag
//
(function($){
  var CLASS = "disabled";
  
  $.fn.setDisabledFlag = function () {
    this.each(function() {
      if (!$(this).hasClass(CLASS)) $(this).addClass(CLASS);
    });
  }
  
  $.fn.deleteDisabledFlag = function () {
    this.removeClass(CLASS);
  }
  
  $.fn.hasDisabledFlag = function () {
    if (this.size() == 0) return false;
    return this.eq(0).hasClass(CLASS);
  }
})(jQuery);

