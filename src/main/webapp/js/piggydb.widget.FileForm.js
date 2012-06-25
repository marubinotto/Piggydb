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
	
	var _initialHeight = 75;
	
	var _class = function(element) {
		module.FormDialog.call(this, element);
	};
	
	_class.openToAdd = function() {
		_open();
	};
	
	_class.prototype = jQuery.extend({
		
		open: function() {
			var outer = this;
			
			this.element.dialog({
				width: 600,
				height: _initialHeight,
				close: function(event, ui) {
					piggydb.widget.Fragment.imageViewer.close();
				}
			});
		
			this.buttonsDiv().hide();
			
			this.element.find("input.file").change(function() {
				outer.setDialogHeight(_initialHeight + 15);
				outer.previewDiv().empty().putLoadingIcon("margin: 5px 10px;");
				outer.element.find("form.upload-file").submit();
			});
			this.element.find("div.onPreviewUpdate").click(function() {
				outer.onPreviewUpdate();
			});
		},
		
		setDialogHeight: function(height) {
			this.element.dialog("option", "height", height);
		},
		
		buttonsDiv: function() {
			return this.element.find("div.buttons");
		},
		
		previewDiv: function() {
			return this.element.find("div.preview");
		},
		
		onPreviewUpdate: function() {
			this.buttonsDiv().show();
			this.setDialogHeight(
				_initialHeight + 
				this.previewDiv().height() +
				this.buttonsDiv().height());
			piggydb.widget.Fragment.imageViewer.close();
		}
		
	}, module.FormDialog.prototype);
	
	module.FileForm = _class;
	
})(piggydb.widget);	
