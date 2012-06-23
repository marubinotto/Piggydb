(function(module) {
	
	var _ID = "file-form";
	
	var _open = function() {
		if (module.FormDialog.setFocusIfOpen(_ID)) return;
		
		jQuery("#" + _ID).remove();
		piggydb.util.blockPageDuringAjaxRequest();
		jQuery.get("partial/file-form.htm", function(html) {
			if (!module.FormDialog.checkOpenError(html)) {
				jQuery("body").append(html);
				var form = new _class(jQuery("#" + _ID));
				form.open();
			}
		});
	};
	
	var _class = function(element) {
		module.FormDialog.call(this, element);
	};
	
	_class.openToAdd = function() {
		_open();
	};
	
	_class.prototype = jQuery.extend({
		
		open: function() {
			var outer = this;
			
			var initialHeight = 80;
			this.element.dialog({
				width: 600,
				height: initialHeight
			});
		
			this.element.find("div.buttons").hide();
			
			this.element.find("input.file").change(function() {
				outer.element.dialog("option", "height", initialHeight + 15);
				outer.element.find("div.uploaded-file").empty().putLoadingIcon("margin: 5px 10px;");
				outer.element.find("form.upload-file").submit();
			});
		}
		
	}, module.FormDialog.prototype);
	
	module.FileForm = _class;
	
})(piggydb.widget);	
