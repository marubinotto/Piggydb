//
// Initialization
//
jQuery(function() {
  FragmentForm.init();
  Fragment.init();
  QuickEdit.init();
  
  // auto-complete
  jQuery("input[name=tags]").autocomplete(constants["autocomplete-url"], {
    minChars: 1,
    selectFirst: true,
    multiple: true,
    multipleSeparator: ', ',
    scrollHeight: 300
  });
  jQuery("input.single-tag").autocomplete(constants["autocomplete-url"], {
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
	  jQuery(".markItUp .markItUpButton9 a").attr("href", constants["wiki-help-href"])
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
  
  wikiHelp: new Facebox("facebox-wiki-help"),
  
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
// Fragment 
//
function Fragment(node) {
	this.node = jQuery(node);
	this.root = this.node.closest("table.fragment");
}
Fragment.init = function() {
	jQuery("table.fragment").live('mouseenter', function() {
    jQuery(this).find(".fragment-tools").eq(0).show();
  });
  jQuery("table.fragment").live('mouseleave', function() {
    jQuery(this).find(".fragment-tools").eq(0).hide();
  });
  jQuery("a.img-link").live("click", Fragment.onImageClick);
  makeFragmentsDroppable("table.fragment", null);
  makeSelectedFragmentsDroppable();
  makeRelationsDraggable("");
};
Fragment.findInTheSameFragmentNode = function(node, selector) {
	return jQuery(node).closest("table.fragment-node").find(selector);
};
Fragment.highlight = function(id, baseNode) {
  var selector = ".fragment-header-" + id;
  var color = "#ff9900";
  if (baseNode == null)
  	jQuery(selector).fadingHighlight(color);
  else
  	jQuery(baseNode).find(selector).fadingHighlight(color);
};
Fragment.onShowHiddenTags = function(button) {
  jQuery(button).siblings(".hidden-tags").show();
  jQuery(button).hide();
};
Fragment.imageViewer = new Facebox("facebox-image-viewer");
Fragment.onImageClick = function() {
	Fragment.imageViewer.showImage(this.href);
  return false;
};
Fragment.prototype = {
	id: function() {
		return this.root.find("span.fragment-id:first").text();
	},
	
	header: function() {
		return this.root.find("div.fragment-header:first");
	},
	
	headerRow: function() {
		return this.header().closest("tr");
	},
	
	bodyRow: function() {
		return this.headerRow().siblings("tr.fragment-body");
	},
	
	setBodyRow: function(rowHtml) {
		this.bodyRow().remove();
		this.headerRow().after(rowHtml);
	},
	
	textContentDiv: function() {
		return this.bodyRow().find("div.fragment-content-text");
	},
	
	isMultirow: function() {
		return this.root.hasClass("multirow");
	},
	
	isMain: function() {
		return this.root.hasClass("fragment-main");
	},
	
	contentToggle: function() {
		var toggle = this.header().find(".fragment-content-toggle a.tool-button");
		return toggle.size() == 0 ? null : new ContentToggle(toggle);
	}
};



//
// Quick Edit
//
var QuickEdit = {
	init: function() {
	  jQuery("div.fragment-content-text").live('dblclick', function() {
		  var contentDiv = jQuery(this);
		  QuickEdit.openEditor(new Fragment(contentDiv).id(), contentDiv);
		});
	},
	
	onEditButtonClick: function(button) {
		var fragment = new Fragment(button);
		
		// fragment page
		if (fragment.isMain()) {
			jQuery("#fragmentFormPanel a.toggle-link").click();
			return true;
		}
		
		// content opened
		var contentDiv = fragment.textContentDiv();	
		if (contentDiv.size() == 1) {
			QuickEdit.openEditor(fragment.id(), contentDiv);
			return true;
		}
		
		// content hidden or empty on a multirow fragment table
		if (fragment.isMultirow()) {
			var contentToggle = fragment.contentToggle();
			if (contentToggle != null) contentToggle.setOpened();
			
			var emptyBodyRow = jQuery("#tpl-fragment-body-row-with-empty-text tbody").html().trim();
			fragment.setBodyRow(emptyBodyRow);
			QuickEdit.openEditor(fragment.id(), fragment.textContentDiv());
			return true;
		}	
		return false;
	},
	
	openEditor: function(id, contentDiv) {
		var contentDivHeight = contentDiv.height();
	  var editorDiv = contentDiv.siblings("div.fragment-content-editor");
	  contentDiv.empty().putLoadingIcon(); 
	  jQuery.get("html/fragment-editor.htm", {"id" : id}, function(html) {
	  	contentDiv.empty();
	  	editorDiv.html(html);
	  	
	  	jQuery.updnWatermark.attachAll();
		
	  	var editor = editorDiv.find("textarea.fragment-content");
	  	editor.markItUp(FragmentForm.markItUpSettings);
	  	editorDiv.find(".markItUp .markItUpButton9 a")
		  	.attr("href", constants["wiki-help-href"]).click(FragmentForm.onWikiHelpClick);
		
	  	var height = Math.max(contentDivHeight, editor.height());
	  	editor.height(Math.min(height, 500));
	  });
	},

	onCancel: function(button) {
		var fragment = new Fragment(button);
		var editorDiv = jQuery(button).closest("div.fragment-content-editor");	
		var contentDiv = editorDiv.siblings("div.fragment-content-text");
		
		editorDiv.empty();
		contentDiv.empty().putLoadingIcon();
		jQuery.get("html/fragment-body-row.htm", {"id": fragment.id()}, function(html) {
			if (isNotBlank(html)) {
				var content = jQuery(html).find("div.fragment-content").html();
				contentDiv.html(content);
				prettyPrint();
			}
			else {
				QuickEdit.emptyContent(contentDiv);
			}
		});
	},
	
	emptyContent: function(contentDiv) {
		Fragment.findInTheSameFragmentNode(contentDiv, "span.fragment-content-toggle:first").remove();
  	contentDiv.closest("tr.fragment-body").remove();
	},

	onUpdate: function(button) {
		var fragment = new Fragment(button);
		var editorDiv = jQuery(button).closest("div.fragment-content-editor");
		var content = editorDiv.find("textarea").val();
		var contentDiv = editorDiv.siblings("div.fragment-content-text");
		
		editorDiv.empty();
		contentDiv.empty().putLoadingIcon();
		var params = {"id": fragment.id(), "content": content};
		jQuery.post("html/update-fragment-content.htm", params, function(html) {
		  if (isNotBlank(html)) {
		  	contentDiv.html(html);
		  	prettyPrint();
		  }
		  else {
		  	QuickEdit.emptyContent(contentDiv);
		  } 
		});
	}
};



//
// Fragment Operations
//
var fragmentOps = {
  deleteRelation: function (id, relationHtml, relationContainerHtml) {
    if (!window.confirm(messages["confirm-delete-relation"])) 
      return false;
    
    ajaxCommand("delete-relation", {"id": id});
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
// Content Toggle
//
function ContentToggle(toggleButton) {
	this.toggleButton = jQuery(toggleButton);
	this.toggleSpan = this.toggleButton.closest("span.fragment-content-toggle");
	this.fragment = new Fragment(toggleButton);
}
ContentToggle.CLOSED = "down";
ContentToggle.OPENED = "up";
ContentToggle.onContentToggleClick = function(toggle, id) {
	var toggle = new ContentToggle(toggle);
	
  if (toggle.isDisabled()) return;
  
  if (toggle.isClosed()) {
  	toggle.setDisabled(true);
    var loadIcon = toggle.loading();
    toggle.setOpened();
    
    jQuery.get("html/fragment-body-row.htm", {"id" : id}, function(html) {
    	toggle.fragment.setBodyRow(html);
      loadIcon.remove();
      toggle.setDisabled(false);
      prettyPrint();
    });
  }
  else if (toggle.isOpened()) {
  	toggle.fragment.bodyRow().remove();
    toggle.setClosed();
  }
};
ContentToggle.onAllContentToggleClick = function(toggle) {
	var toggle = new ContentToggle(toggle);
  if (toggle.isClosed()) {
    jQuery(".fragment-content-toggle img[src*='" + ContentToggle.CLOSED + "']").closest("a").click();
    toggle.setOpened();
  }
  else if (toggle.isOpened()) {
    jQuery(".fragment-content-toggle img[src*='" + ContentToggle.OPENED + "']").closest("a").click();
    toggle.setClosed();
  }
};
ContentToggle.prototype = {
	isDisabled: function() {
		return this.toggleButton.hasDisabledFlag();
	},
	
	setDisabled: function(disabled) {
		if (disabled)
			this.toggleButton.setDisabledFlag();
		else
			this.toggleButton.deleteDisabledFlag();
	},
	
	loading: function() {
		return this.toggleSpan.putLoadingIcon("margin: -2px; margin-left: 5px;");
	},
	
	buttonImg: function() {
		return this.toggleButton.children("img");
	},
	
	buttonImgSrc: function() {
		return this.buttonImg().attr("src");
	},
	
	isClosed: function() {
    return this.buttonImgSrc().indexOf(ContentToggle.CLOSED) != -1;
  },
  
  isOpened: function() {
    return this.buttonImgSrc().indexOf(ContentToggle.OPENED) != -1;
  },
  
  setOpened: function() {
    var img = this.buttonImg();
    img.attr("src", img.attr("src").replace(ContentToggle.CLOSED, ContentToggle.OPENED));
  },
  
  setClosed: function() {
    var img = this.buttonImg();
    img.attr("src", img.attr("src").replace(ContentToggle.OPENED, ContentToggle.CLOSED));
  }
};



//
// Fragment Tree
//
var FragmentTree = {
  COLLAPSED: "plus",
  EXPANDED: "minus",
  
  onNodeToggleClick: function(toggle, id) {
    if (jQuery(toggle).hasDisabledFlag()) return;
  
    var li = jQuery(toggle).closest("li");
    var icon = jQuery(toggle).children("img");
    var iconSrc = icon.attr("src");
    
    // Expand
    if (iconSrc.indexOf(this.COLLAPSED) != -1) {
      jQuery(toggle).setDisabledFlag();
      icon.attr("src", iconSrc.replace(this.COLLAPSED, this.EXPANDED));
      var loadIcon = jQuery(li).putLoadingIcon("margin:5px");
      jQuery.get("html/fragment-child-nodes.htm", {"id" : id}, function(childrenHtml) {
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
    jQuery(".sortable-children").sortable({
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
    jQuery(".sortable-children").disableSelection();
    jQuery("table.fragment-root-node").css("cursor", "move");
    jQuery(".fragment-root-node .root-header-cell .fragment-header")
      .prepend('<img class="sortable-icon" src="images/sortable.png" border="0" alt=""/>');
  },
  
  disableSortable: function() {
    jQuery(".sortable-children").sortable("destroy");
    jQuery(".sortable-children").enableSelection();
    jQuery("table.fragment-root-node").css("cursor", "auto");
    jQuery(".fragment-root-node .sortable-icon").remove();
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
        	Fragment.highlight(outer.highlight, outer.contentDiv);
          // highlighing should be done only once 
        	// in other words, when a user changes the scale in the same page,
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


