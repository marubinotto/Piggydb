(function(module) {
	
	var _class = function(jQueryElement) {
		module.Widget.call(this, jQueryElement);
	};
	
	_class.isOpen = function(id) {
		var element = jQuery("#" + id);
		return element.size() > 0 ? element.dialog("isOpen") : false;
	};
	
	_class.setFocusIfOpen = function(id) {
		if (_class.isOpen(id)) {
			jQuery("#" + id).dialog("moveToTop");
			return true;
		}
		else {
			return false;
		}
	};
	
	_class.checkOpenError = function(html) {
		var error = jQuery(html).children("span.error");
		if (error.size() > 0) {
			module.putGlobalMessage(error.html());
			return true;
		}
		return false;
	};
	
	_class.prototype = jQuery.extend({
		
		close: function() {
			this.element.dialog("close");	
		},
		
		block: function() {
			this.element.block({ 
				message: '<img src="images/load-large.gif" border="0"/>',
				centerX: true,
		    centerY: true, 
				css: { 
					border: '0px solid #aaa',
					width: '30px',
					padding: '15px',
					fadeIn: 0,
					fadeOut: 0
				},
				overlayCSS:  { 
	      	opacity: 0.4 
				}
			});
		},
		
		unblock: function() {
			this.element.unblock();
		},
		
		serializeForm: function() {
			return this.element.find("form").serializeArray();
		},
		
		setFormError: function(message) {
			piggydb.widget.putErrorMessage(this.indicator, this.id, message, this.element);
		},
		
		setInputError: function(name, message) {
			var input = this.element.find("form :input[name='" + name + "']");
			piggydb.widget.setInputError(input, this.id + "-" + name, message, this.element);
		},
		
		clearErrors: function() {
			piggydb.widget.clearErrorMessage(this.indicator);
			this.element.find("form :input").each(function() {
				piggydb.widget.clearInputError(this);
			});
		},
		
		checkErrors: function(html) {
			var errors = jQuery(html).children("div.errors");
			if (errors.size() == 0) return false;
			
			var outer = this;
			errors.find("span.global-error").each(function() {
				outer.setFormError(jQuery(this).html());
			})
			errors.find("div.field-errors > span").each(function() {
				outer.setInputError(jQuery(this).attr("class"), jQuery(this).html());
			});
			return true;
		},
		
		prepareCommonInputs: function() {
			jQuery.updnWatermark.attachAll();
			
			this.element.find("input.fragment-as-tag").button({
	      icons: {
	      	primary: "ui-icon-piggydb-tag"
	      },
	      text: false
		  });
			
			this.element.find("input[name=tags]").autocomplete(piggydb.server.autoCompleteUrl, {
		    minChars: 1,
		    selectFirst: true,
		    multiple: true,
		    multipleSeparator: ', ',
		    scrollHeight: 300
		  });
			
			var outer = this;
			var palette = new piggydb.widget.TagPalette(this.element.find("div.tag-palette"));
			palette.onTagSelect = function(source, tagId, tagName, palette) {
        var input = outer.element.find("input[name='tags']");
        var tags = jQuery.trim(input.val());
        if (tags == null || tags == "")
          tags = tagName;
        else
          tags = tags + ", " + tagName;
        input.val(tags);
        input.focus();
      };
      palette.flatColumnWidth = 100;
      palette.autoFlatWidth = true;
      palette.init(this.element.find("button.pulldown"));
      this.tagPalette = palette;
		},
		
		processResponseOnSaved: function(response, fragment) {
			var outer = this;
			
			// success message
			jQuery(response).find("span.success").each(function() {
				piggydb.widget.putGlobalMessage(jQuery(this).html());
			});
			
			// updated
			if (fragment != null) {
				jQuery(response).children("div.fragment-properties").each(function() {
					fragment.update(jQuery(this));
				});
				piggydb.widget.Fragment.highlight(fragment.id(), null);
				if (jQuery.isFunction(outer.onSaved)) outer.onSaved(fragment.id());
			}
			// created 
			else {
				jQuery(response).find("span.new-id").each(function() {
					if (jQuery.isFunction(outer.onSaved)) outer.onSaved(jQuery(this).text());
				});
			}
		}		
	}, module.Widget.prototype);
	
	module.FragmentFormBase = _class;
	
})(piggydb.widget);	
