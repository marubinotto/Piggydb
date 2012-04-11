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



//
// Fragment Tree
//
var FragmentTree = {
  COLLAPSED: "plus",
  EXPANDED: "minus",
  
  onNodeToggleClick: function(toggle, id, contextParentId) {
    if (jQuery(toggle).hasDisabledFlag()) return;
  
    var li = jQuery(toggle).closest("li");
    var icon = jQuery(toggle).children("img");
    var iconSrc = icon.attr("src");
    
    // Expand
    if (iconSrc.indexOf(this.COLLAPSED) != -1) {
      jQuery(toggle).setDisabledFlag();
      icon.attr("src", iconSrc.replace(this.COLLAPSED, this.EXPANDED));
      var loadIcon = jQuery(li).putLoadingIcon("margin:5px");
      var params = {"id" : id};
      if (contextParentId != null) params.contextParentId = contextParentId;
      jQuery.get("html/fragment-child-nodes.htm", params, function(childrenHtml) {
        li.append(childrenHtml);
        loadIcon.remove();
        jQuery(toggle).deleteDisabledFlag();
      });
    }
    // Collapse
    else if (iconSrc.indexOf(this.EXPANDED) != -1) {
      icon.attr("src", iconSrc.replace(this.EXPANDED, this.COLLAPSED));
      li.children("ul").remove();
    }
  },
  
  enableSortable: function(parentId) {
  	var sortableUl = jQuery(".sortable-children");
  	sortableUl.sortable({
      update: function(event, ui) {
        var children = jQuery(this);
        var childOrder = children.sortable('toArray');
        var processingIcon = jQuery(
          '<span><img src="images/load.gif" border="0"/><\/span>')
            .appendTo(jQuery("#processing-children"));
        jQuery.ajax({
          type: "POST",
          url: "command/update-child-relation-priorities.htm",
          data: "id=" + parentId + "&" + jQuery(this).sortable('serialize'),
          success: function(response) {
            if (response == "error") children.sortable('cancel');
            processingIcon.remove();
          }
        });
      }
    });
  	sortableUl.disableSelection();
  	sortableUl.find("table.fragment-root-node").css("cursor", "move");
  	sortableUl.find(".fragment-root-node .root-header-cell .fragment-header")
      .prepend('<img class="sortable-icon" src="images/sortable.png" border="0" alt=""/>');
  },
  
  disableSortable: function() {
  	var sortableUl = jQuery(".sortable-children");
  	sortableUl.sortable("destroy");
  	sortableUl.enableSelection();
  	sortableUl.find("table.fragment-root-node").css("cursor", "auto");
  	sortableUl.find(".fragment-root-node .sortable-icon").remove();
  }
};



//
// Fragments View 
//
function FragmentsView(id, fragmentsUrl, scale, orderBy, ascending) {
	this.id = id;
	this.rootDiv = jQuery("#" + id);
	this.fragmentsUrl = fragmentsUrl;
	this.scale = scale;
	this.orderBy = orderBy;
	this.ascending = ascending;
	this.highlight = null;
	this.highlighted = false;
	this.contentDiv = this.rootDiv.find("div.view-content");
	this.viewControl = this.rootDiv.find(".view-control");
	this.pageIndex = 0;
}
FragmentsView.MIN_SCALE = 0;
FragmentsView.MAX_SCALE = 1000;
FragmentsView.prototype = {
  init: function () {
    var outer = this;
    this.rootDiv.find(".view-slider").slider({ 
      max: FragmentsView.MAX_SCALE,
      min: FragmentsView.MIN_SCALE,
      value: outer.scale,
      change: function(event, ui) {
        outer.scale = ui.value;
        outer.loadFirstSet();
      }
    });
    this.rootDiv.find(".select-orderBy").change(function () {
      outer.orderBy = jQuery(this).val();
      outer.loadFirstSet();
    });
    this.rootDiv.find(".select-ascending-or-not button").click(function () {
    	var button = jQuery(this);
      if (!clickSelectSwitch(button)) return;     
      outer.ascending = (button.attr("name") == "ascending");
      outer.loadFirstSet();
    });
    
    this.loadFirstSet();
  },

  createParameters: function () {
    return {
      "viewId": this.id, 
      "scale": this.scale, 
      "orderBy": this.orderBy,
      "ascending": this.ascending
    };
  },
  
  loadFirstSet: function () {
  	this.pageIndex = 0;
    this.contentDiv.empty();
    var loadIcon = this.contentDiv.putLoadingIcon("margin: 5px;");
    var outer = this;
    jQuery.get(this.fragmentsUrl, this.createParameters(), function(html) {
      if (jQuery.trim(html) != "") {
      	outer.contentDiv.append(html);
      	outer.viewControl.show();
        prettyPrint();
        if (outer.highlight != null && !outer.highlighted) {
        	piggydb.widget.Fragment.highlight(outer.highlight, outer.contentDiv);
          // highlighing should be done only once 
        	// for example, when a user changes the scale in the same page,
          // highlighing should not be enabled.
          outer.highlighted = true;
        }
      }
      loadIcon.remove();
    });
  },
  
	showMore: function (button) {
    jQuery(button).remove();
    var fragmentsContainer = this.rootDiv.find(".fragments-container");
    
    var loadIcon = jQuery('<div style="text-align: center; margin-bottom: 5px;">')
      .appendTo(fragmentsContainer);
    loadIcon.putLoadingIcon(null);
    
    var params = this.createParameters();
    params.pi = ++this.pageIndex;
    jQuery.get(this.fragmentsUrl, params, function(html) {
      fragmentsContainer.append(html);
      loadIcon.remove();
      prettyPrint();
    });
  }
};


