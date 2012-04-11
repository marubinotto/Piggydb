//
// Initialization
//
jQuery(function() {
  FragmentForm.init();
  
  // auto-complete
  jQuery("input[name=tags]").autocomplete(piggydb.server.autoCompleteUrl, {
    minChars: 1,
    selectFirst: true,
    multiple: true,
    multipleSeparator: ', ',
    scrollHeight: 300
  });
  jQuery("input.single-tag").autocomplete(piggydb.server.autoCompleteUrl, {
    minChars: 1,
    selectFirst: true,
    multiple: false,
    scrollHeight: 300
  });
});



//
// Fragment Form
//
var FragmentForm = {
	init: function() {
		jQuery('.content-type-switch input').click(FragmentForm.onContentTypeSwitch);
	  jQuery("textarea.fragment-content").markItUp(FragmentForm.markItUpSettings);
	  jQuery(".markItUp .markItUpButton9 a").attr("href", piggydb.server.wikiHelpUrl)
	  	.click(FragmentForm.onWikiHelpClick);
	  jQuery("input[name=preview]").click(function () {
	    this.form.contentFieldHeight.value = jQuery(this.form.content).height();
	  });
	  jQuery(".fragment-form-panel input[name=register]").click(function () {
	    var panel = jQuery(this).closest(".fragment-form-panel");
	    panel.find(".fragment-form-toggle").putLoadingIcon("margin-left: 5px; vertical-align: middle;");
	    panel.find(".toggle-icon").attr("src", "images/twistie-up.gif");
	    panel.find(".fragment-form-div").hide();
	  });
	  
	  jQuery("input.fragment-as-tag").button({
      icons: {
      	primary: "ui-icon-piggydb-tag"
      },
      text: false
	  });
	},
		
  onToggleClick: function(panelName) {
		var formDiv = document.getElementById(panelName + "-div");
		var toggleIcon = document.getElementById(panelName + "-toggle-icon");
		if (formDiv.style.display == "none") {
		  toggleIcon.src = "images/twistie-down.gif";
		  formDiv.style.display = "block";
		} 
		else {
		  toggleIcon.src = "images/twistie-up.gif";
		  formDiv.style.display = "none";
		}
  },
  
  onContentTypeSwitch: function() {
    var formId = jQuery(this.form).attr("id");
    var newValue = this.value;
    var oldValue = this.form.contentType.value;
    if (newValue == oldValue) {
      return;
    }
    jQuery('#' + formId + ' .for-' + oldValue).hide();
    jQuery('#' + formId + ' .for-' + newValue).show();
    this.form.contentType.value = newValue;
  },
  
  wikiHelp: new piggydb.widget.Facebox("facebox-wiki-help"),
  
  onWikiHelpClick: function() {
  	FragmentForm.wikiHelp.show(this.href);
    return false;
  },
  
  markItUpSettings: {
    previewAutoRefresh: false,
    previewParserPath:  '', // path to your Wiki parser
    onShiftEnter:   {keepDefault:false, replaceWith:'\n\n'},
    markupSet: [
      {name: messages["editor-bold"], key: 'B', openWith: "'''", closeWith: "'''"}, 
      {name: messages["editor-italic"], key: 'I', openWith: "''", closeWith: "''"}, 
      {name: messages["editor-strike"], key: 'S', openWith: '__', closeWith: '__'}, 
      {separator: '---------------' },
      {name: messages["editor-bulleted-list"], openWith: '-'}, 
      {name: messages["editor-numeric-list"], openWith: '+'}, 
      {separator: '---------------' },
      {name: messages["editor-link"], key: "L", openWith: "[[![URL:!:http://]!] ", 
        closeWith: ']', placeHolder: messages["editor-link-label"] },
      {name: messages["editor-embed-another-fragment"], key: 'E',
        openWith: "fragment:[![" + messages["editor-fragment-id"] + "]!]:embed "},
      {separator: '---------------' },
      {name: messages["editor-quote"], openWith: '>', placeHolder: ''},
      {separator: '---------------' },
      {name: messages["help"]}
    ]
  }
};



//
// Fragment Operations
//
var fragmentOps = {
  deleteRelation: function (id, relationHtml, relationContainerHtml) {
    if (!window.confirm(messages["confirm-delete-relation"])) 
      return false;
    
    piggydb.server.ajaxCommand("delete-relation", {"id": id});
    relationHtml.fadeOut("slow", function() {
      if (relationContainerHtml != null && relationHtml.siblings().size() == 0)
        relationContainerHtml.remove();
      else
        relationHtml.remove();
    });
  },
  
  removeTag: function (fragmentId, tagName) {
    if (!window.confirm(messages["confirm-remove-tag"] + ' : "' + tagName + '"')) 
      return false;
      
    var fm = document.forms['removeTagForm'];
    fm.fragmentId.value = fragmentId;
    fm.tagName.value = tagName;
    fm.submit();
  },
  
  addTag: function (fragmentId, tagName) {
    var fm = document.forms['addTagForm'];
    fm.fragmentId.value = fragmentId;
    fm.tagName.value = tagName;
    fm.submit();
  },
  
  removeBookmark: function(fragmentId) {
    if (!window.confirm(messages["confirm-remove-bookmark"])) 
      return false;
      
    var fm = document.forms['removeBookmarkForm'];
    fm.fragmentId.value = fragmentId;
    fm.submit();
  }
};
