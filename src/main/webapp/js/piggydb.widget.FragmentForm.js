(function(module) {
	
	var _messages = piggydb.server.messages;
		
	var _markItUpSettings = {
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
	
	var _onWikiHelpClick = function() {
  	_wikiHelp.show(this.href);
    return false;
  };
	
	var _prepare = function(element) {
		jQuery.updnWatermark.attachAll();
		element.find("input.fragment-as-tag").button({
      icons: {
      	primary: "ui-icon-piggydb-tag"
      },
      text: false
	  });
		element.find("textarea.fragment-content").markItUp(_markItUpSettings);
		element.find(".markItUp .markItUpButton9 a").attr("href", constants["wiki-help-href"])
	  	.click(_onWikiHelpClick);
	};
	
	var _open = function(element) {
		_prepare(element);
		element.dialog({
	    modal: false,
	    width: 600,
	    height: 400
	  });
		element.find("textarea.fragment-content").get(0).focus();
	};
	
	module.FragmentForm = {
		openToCreate: function() {
			jQuery("#dialog-fragment-form").remove();
			jQuery.get("html/fragment-editor.htm", function(html) {
				jQuery("body").append(html);
				_open(jQuery("#dialog-fragment-form"));
			});
		}
	};
	
})(piggydb.widget);	
