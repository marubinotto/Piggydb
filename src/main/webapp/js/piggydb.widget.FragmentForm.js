(function(module) {
	
	var _messages = piggydb.server.messages;
	
	var _markItUpSettings = {
		nameSpace: 'markItUp-root',
    previewAutoRefresh: false,
    previewParserPath:  '', // path to your Wiki parser
    onShiftEnter:   {keepDefault:false, replaceWith:'\n\n'},
    markupSet: [
      {name: _messages["editor-bold"], key: 'B', openWith: "'''", closeWith: "'''"}, 
      {name: _messages["editor-italic"], key: 'I', openWith: "''", closeWith: "''"}, 
      {name: _messages["editor-strike"], key: 'S', openWith: '__', closeWith: '__'}, 
      {separator: '---------------' },
      {name: _messages["editor-bulleted-list"], openWith: '-'}, 
      {name: _messages["editor-numeric-list"], openWith: '+'}, 
      {separator: '---------------' },
      {name: _messages["editor-link"], key: "L", openWith: "[[![URL:!:http://]!] ", 
        closeWith: ']', placeHolder: _messages["editor-link-label"] },
      {name: _messages["editor-embed-another-fragment"], key: 'E',
        openWith: "fragment:[![" + _messages["editor-fragment-id"] + "]!]:embed "},
      {separator: '---------------' },
      {name: _messages["editor-quote"], openWith: '>', placeHolder: ''},
      {separator: '---------------' },
      {name: _messages["help"]}
    ]
  };
	
	var _wikiHelp = new piggydb.widget.Facebox("facebox-wiki-help");
	
	var _previewBox = new piggydb.widget.Facebox("facebox-fragment-preview");
	
	var _open = function(fragment) {
		var editorId = fragment != null ? 
			"fragment-editor-" + fragment.id() : 
			"fragment-editor-new";
			
		if (_isOpen(editorId)) {
			jQuery("#" + editorId).dialog("moveToTop");
			return;
		}

		jQuery("#" + editorId).remove();
		
		var args = fragment != null ? {id: fragment.id()} : {};
		piggydb.util.blockPageDuringAjaxRequest();
		jQuery.get("partial/fragment-editor.htm", args, function(html) {
			if (!_checkOpenError(html)) {
				jQuery("body").append(html);
				var form = new _class(jQuery("#" + editorId), editorId);
				form.fragment = fragment;
				form.open();
			}
		});
	};
	
	var _isOpen = function(editorId) {
		var editor = jQuery("#" + editorId);
		return editor.size() > 0 ? editor.dialog("isOpen") : false;
	};
	
	var _checkOpenError = function(html) {
		var error = jQuery(html).children("span.error");
		if (error.size() > 0) {
			piggydb.widget.putGlobalMessage(error.html());
			return true;
		}
		return false;
	};
	
	var _class = function(element, id) {
		module.FormDialog.call(this, element);
		this.id = id;
		this.textarea = this.element.find("textarea.fragment-content");
		this.indicator = this.element.find("span.indicator");
		this.fragment = null;		// target fragment widget to be updated
		this.prepare();
	};
	
	_class.addToolBar = function(textarea, resizeHandle) {
		_markItUpSettings.resizeHandle = resizeHandle;
		textarea.markItUp(_markItUpSettings);
	};
	
	_class.linkToWikiHelp = function(a) {
		a.attr("href", piggydb.server.wikiHelpUrl).click(function() {
	  	_wikiHelp.show(this.href);
	    return false;
	  });
	};
	
	_class.openToCreate = function() {
		_open(null);
	};
	
	_class.openToUpdate = function(button) {
		_open(new piggydb.widget.Fragment(button));
	};
	
	_class.prototype = jQuery.extend({
		
		prepare: function() {
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
      palette.decideMaxHeight = function() {
      	return outer.textarea.height() + 50;
      };
      palette.flatColumnWidth = 100;
      palette.autoFlatWidth = true;
      palette.init(this.element.find("button.pulldown"));
			
			_class.addToolBar(this.textarea, false);
			_class.linkToWikiHelp(this.element.find(".markItUp .markItUpButton9 a"));
		},
		
		open: function() {
			var outer = this;
			
			this.element.dialog({
		    modal: false,
		    width: 600,
		    height: 450,
		    resize: function() {
		    	outer.adjustEditorHeight();
				},
				close: function(event, ui) {
					_previewBox.close();
				}
		  });
			
			this.element.find("button.preview").click(function() {
				outer.clearErrors();
				outer.block();
				jQuery.post("partial/preview-fragment.htm", outer.serializeForm(), function(html) {
					if (outer.checkErrors(html))
						_previewBox.close();
					else {
						_previewBox.showHtml(html);
						prettyPrint();
					}
					outer.unblock();
				});
			});
			this.element.find("button.register").click(function() {
				outer.clearErrors();
				outer.block();
				jQuery.post("partial/save-fragment.htm", outer.serializeForm(), function(html) {
					if (outer.checkErrors(html)) {
						_previewBox.close();
						outer.unblock();
					}
					else {
						jQuery(html).find("span.success").each(function() {
							piggydb.widget.putGlobalMessage(jQuery(this).html());
						});
						// created 
						jQuery(html).find("span.new-id").each(function() {
							if (typeof fragmentsView_fragmentsByDate != "undefined") {
								fragmentsView_fragmentsByDate.refresh(jQuery(this).text());
							}
						});
						// updated
						if (outer.fragment != null) {
							jQuery(html).children("div.fragment-properties").each(function() {
								outer.fragment.update(jQuery(this));
							});
							piggydb.widget.Fragment.highlight(outer.fragment.id(), null);
						}
						outer.close();
					}
				});
			});
			
			this.adjustEditorHeight();
			this.textarea.get(0).focus();	
		},
		
		close: function() {
			this.element.dialog("close");	
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
		
		adjustEditorHeight: function() {
			var baseHeight = this.element.find("form").height() 
				- this.element.find("div.title").height()
				- this.element.find("div.tags").height()
				- this.element.find("div.buttons").height();
			this.textarea.height(baseHeight - 55);
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
		}
		
	}, module.FormDialog.prototype);
	
	module.FragmentForm = _class;

})(piggydb.widget);	
