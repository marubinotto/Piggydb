/*
* jQuery Watermark Plugin (v1.0.0)
*   http://updatepanel.net/2009/04/17/jquery-watermark-plugin/
*
* Copyright (c) 2009 Ting Zwei Kuei
*
* Dual licensed under the MIT and GPL licenses.
*   http://www.opensource.org/licenses/mit-license.php
*   http://www.opensource.org/licenses/gpl-3.0.html
*/
(function($) {
    $.fn.updnWatermark = function(options) {
        options = $.extend({}, $.fn.updnWatermark.defaults, options);
        return this.each(function() {
            var $input = $(this);
            // Checks to see if watermark already applied.
            var $watermark = $input.data("updnWatermark");
            // Only create watermark if title attribute exists
            if (!$watermark && this.title) {
                // Inserts a span and set as positioning context
                var $watermark = $("<span/>")
        					.addClass(options.cssClass)
                    .insertBefore(this)
                    .hide()
        					.bind("show", function() {
                      $(this).children().fadeIn("fast");
                  })
                  .bind("hide", function() {
                      $(this).children().hide();
                  });
      				// Positions watermark label relative to positioning context
      				$("<label/>").appendTo($watermark)
                  .text(this.title)
                  .attr("for", this.id);
      				// Associate input element with watermark plugin.
              $input.data("updnWatermark", $watermark);
            }
            // Hook up blur/focus handlers to show/hide watermark.
            if ($watermark) {
                $input
                    .focus(function(ev) {
                        $watermark.trigger("hide");
                    })
                    .blur(function(ev) {
                        if (!$(this).val()) {
                            $watermark.trigger("show");
                        }
                    });
                // Sets initial watermark state.
                if (!$input.val()) {
                    $watermark.show();
                }
            }
        });
    };
    $.fn.updnWatermark.defaults = {
        cssClass: "updnWatermark"
    };
    $.updnWatermark = {
        attachAll: function(options) {
			    $("input:text[title!=''],input:password[title!='']").updnWatermark(options);
        }
    };
})(jQuery);
