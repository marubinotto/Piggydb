(function(module) {
	
	var _ID = "file-form";
	
	var _open = function(fragment, modal) {
		jQuery("#" + _ID).remove();
		
		var args = fragment != null ? {id: fragment.id()} : {};
		piggydb.util.blockPageDuringAjaxRequest();
		jQuery.get("partial/file-form.htm", args, function(html) {
			if (!module.FormDialog.checkOpenError(html)) {
				jQuery("body").append(html);
				var form = new _class(jQuery("#" + _ID));
				form.fragment = fragment;
				form.modal = modal;
				form.open();
			}
		});
	};
	
	var _initialHeight = 75;
	
	var _class = function(element) {
		module.FormDialog.call(this, element);
		
		window.fileForm = this;
		
		this.id = _ID;
		this.modal = false;
		this.indicator = this.element.find("span.indicator");
		this.fragment = null;		// target fragment widget to be updated
	};
	
	_class.openToAdd = function() {
		_open(null, false);
	};
	
	_class.openToUpdate = function(button) {
		_open(new piggydb.widget.Fragment(button), false);
	};
	
	_class.openToEmbed = function(textarea) {
		_open(null, true);
	};
	
	_class.prototype = jQuery.extend({
		
		open: function() {
			var outer = this;
			
			this.element.dialog({
				width: 600,
				height: _initialHeight,
				modal: outer.modal,
				close: function(event, ui) {
					piggydb.widget.imageViewer.close();
					window.fileForm = null;
				}
			});
		
			this.buttonsDiv().hide();
			
			this.element.find("input.file").change(function() {
				outer.setDialogHeight(_initialHeight + 15);
				outer.previewDiv().empty().putLoadingIcon("margin: 5px 10px;");
				outer.element.find("form.upload-file").submit();
			});
			this.element.find("button.register").click(function() {
				outer.clearErrors();
				outer.block();
				var values = outer.element.find("form.save-file").serializeArray();
				jQuery.post("partial/save-file.htm", values, function(html) {
					if (outer.checkErrors(html)) {
						piggydb.widget.imageViewer.close();
						outer.unblock();
					}
					else {
						piggydb.widget.Fragment.onAjaxSaved(html, outer.fragment);
						outer.close();
					}
				});
			});
			this.element.find("div.preview img").load(function() {
				outer.setDialogHeight(
					_initialHeight + 
					outer.previewDiv().height() +
					5);
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
				this.buttonsDiv().height() +
				5);
			piggydb.widget.imageViewer.close();
		}
		
	}, module.FormDialog.prototype);
	
	module.FileForm = _class;
	
})(piggydb.widget);	
