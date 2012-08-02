(function(module) {
	
	var _messages = piggydb.server.messages;
	
	var _markItUpSettings = {
		nameSpace: 'markItUp-root',
    previewAutoRefresh: false,
    previewParserPath:  '', // path to your Wiki parser
    onShiftEnter:   {keepDefault: false, replaceWith: '~\n'},
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
      {name: _messages["editor-embed-file"], key: 'F'},
      {separator: '---------------' },
      {name: _messages["editor-quote"], key: 'Q', openWith: '>', placeHolder: ''},
      {separator: '---------------' },
      {name: _messages["help"]}
    ]
  };
	
	var _wikiHelp = new piggydb.widget.Facebox("facebox-wiki-help");
	
	var _previewBox = new piggydb.widget.Facebox("facebox-fragment-preview");
	
	var _open = function(args, fragment, onCreated) {
		var editorId = fragment != null ? 
			"fragment-editor-" + fragment.id() : 
			"fragment-editor-new";
			
		if (module.FragmentFormBase.setFocusIfOpen(editorId)) return;

		jQuery("#" + editorId).remove();
		
		if (fragment != null) args.id = fragment.id();
		
		piggydb.util.blockPageDuringAjaxRequest();
		jQuery.get("partial/fragment-editor.htm", args, function(html) {
			if (!module.FragmentFormBase.checkOpenError(html)) {
				jQuery("body").append(html);
				var form = new _class(jQuery("#" + editorId), editorId);
				form.fragment = fragment;
				form.onCreated = onCreated;
				form.open();
			}
		});
	};
	
	var _class = function(element, id) {
		module.FragmentFormBase.call(this, element);
		this.id = id;
		this.textarea = this.element.find("textarea.fragment-content");
		this.indicator = this.element.find("span.indicator");
		this.fragment = null;		// target fragment widget to be updated
		this.onCreated = null;
		this.prepare();
	};
	
	_class.addToolBar = function(textarea, resizeHandle) {
		_markItUpSettings.resizeHandle = resizeHandle;
		textarea.markItUp(_markItUpSettings);
		
		var markItUpRoot = textarea.closest("div.markItUp-root");
		
		// Tool button: embed a file
		if (document.selection) {
			var saveTextRange = function() {
				jQuery.data(textarea, "range", document.selection.createRange());
			};
			textarea
				.mousedown(saveTextRange)
				.mouseup(saveTextRange)
				.keydown(saveTextRange)
				.keyup(saveTextRange)
				.select(saveTextRange);
		}
		markItUpRoot.find(".markItUp li.markItUpButton8").mouseup(function() {
			piggydb.widget.FileForm.openToEmbed(
				function(newId) {
					var embeddedCode = "fragment:" + newId + ":embed";
					textarea.insertAtCaret(embeddedCode, jQuery.data(textarea, "range"));
				});
		});
		
		// Tool button: wiki help
		markItUpRoot.find(".markItUp .markItUpButton10 a")
			.attr("href", piggydb.server.wikiHelpUrl)
			.click(function() {
				_wikiHelp.show(this.href);
				return false;
			});
	};
	
	_class.openToCreate = function() {
		_open({}, null, function(newId) {
			piggydb.widget.FragmentsView.refreshViews(newId);
		});
	};
	
	_class.openToUpdate = function(button) {
		_open({}, new piggydb.widget.Fragment(button), null);
	};
	
	_class.openToAddChild = function(parentId) {
		_open({parentId: parentId}, null, function(newId) {
			piggydb.widget.Fragment.reloadRootChildNodes(parentId, newId);
		});
	};
	
	_class.openToCreateWithTag = function(tagId) {
		_open({tagId: tagId}, null, function(newId) {
			piggydb.widget.FragmentsView.refreshViews(newId);
		});
	};
	
	_class.prototype = jQuery.extend({
		
		prepare: function() {
			this.prepareCommonInputs();
			
			var outer = this;
			this.tagPalette.decideMaxHeight = function() {
      	return outer.textarea.height() + 50;
      };
			
			_class.addToolBar(this.textarea, false);
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
						outer.processResponseOnSaved(html, outer.fragment);
						outer.close();
					}
				});
			});
			
			this.adjustEditorHeight();
			// this.textarea.get(0).focus();	
		},
				
		adjustEditorHeight: function() {
			var baseHeight = this.element.find("form").height() 
				- this.element.find("div.title").height()
				- this.element.find("div.tags").height()
				- this.element.find("div.buttons").height();
			this.textarea.height(baseHeight - 55);
		}
		
	}, module.FragmentFormBase.prototype);
	
	module.FragmentForm = _class;

})(piggydb.widget);	
