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
	
	var _class = function(element) {
		module.Widget.call(this, element);
		this.textarea = this.element.find("textarea.fragment-content");
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
		jQuery("#fragment-editor-new").remove();
		jQuery.get("html/fragment-editor.htm", function(html) {
			jQuery("body").append(html);
			var form = new _class(jQuery("#fragment-editor-new"));
			form.open();
		});
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
				}
		  });
			
			this.element.find("button.cancel").click(function() {
				outer.element.dialog("close");
			});
			
			this.adjustEditorHeight();
			this.textarea.get(0).focus();
		},
		
		adjustEditorHeight: function() {
			var baseHeight = this.element.find("form").height() 
				- this.element.find("div.title").height()
				- this.element.find("div.tags").height()
				- this.element.find("div.buttons").height();
			this.textarea.height(baseHeight - 55);
		}
		
	}, module.Widget.prototype);
	
	module.FragmentForm = _class;

})(piggydb.widget);	
