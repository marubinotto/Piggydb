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
			this.element.dialog({
				width: 600,
				height: 100
			});
		}
		
	}, module.FormDialog.prototype);
	
	module.FileForm = _class;
	
})(piggydb.widget);	
