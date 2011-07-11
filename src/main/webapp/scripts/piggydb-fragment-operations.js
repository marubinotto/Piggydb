//
// Settings for markItUp
//
var markItUpSettings = {
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
};


//
// Initialization
//
jQuery(function() {
  // fragment
  jQuery("table.fragment").live('mouseenter', function() {
    jQuery(this).find(".fragment-tools").eq(0).show();
  });
  jQuery("table.fragment").live('mouseleave', function() {
    jQuery(this).find(".fragment-tools").eq(0).hide();
  });
  jQuery("a.img-link").live("click", onImageClick);
  Fragment.setUpQuickEdit();
  makeFragmentsDroppable("table.fragment", null);
  makeSelectedFragmentsDroppable();
  makeRelationsDraggable("");
  
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
  
  // fragment form
  jQuery('.content-type-switch input').click(onContentTypeSwitch);
  jQuery("textarea.fragment-content").markItUp(markItUpSettings);
  jQuery(".markItUp .markItUpButton9 a").attr("href", constants["wiki-help-href"]).click(onWikiHelpClick);
  jQuery("input[name=preview]").click(function () {
    this.form.contentFieldHeight.value = jQuery(this.form.content).height();
  });
  jQuery(".fragment-form-panel input[name=register]").click(function () {
    var panel = jQuery(this).closest(".fragment-form-panel");
    panel.find(".fragment-form-toggle").putLoadingIcon("margin-left: 5px; vertical-align: middle;");
    panel.find(".toggle-icon").attr("src", "images/twistie-up.gif");
    panel.find(".fragment-form-div").hide();
  });
});


//
// Fragment form
//

function onFragmentFormToggleClick(panelName) {
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
}

function onContentTypeSwitch() { 
  var formId = jQuery(this.form).attr("id");
  var newValue = this.value;
  var oldValue = this.form.contentType.value;
  if (newValue == oldValue) {
    return;
  }
  jQuery('#' + formId + ' .for-' + oldValue).hide();
  jQuery('#' + formId + ' .for-' + newValue).show();
  this.form.contentType.value = newValue;
}

function onWikiHelpClick() {
  wikiHelp.show(this.href);
  return false;
}



//
// Liquid blocks
//
function liquidBlocks(selectorPrefix, blockWidth, containerWidth) {
  var blocksSelector = selectorPrefix + "ul.liquid-blocks";

  // Get the width of row
  if (containerWidth == null) {
    // Reset the container size to a 100% once view port has been adjusted
    jQuery(blocksSelector).css({ 'width' : "100%" });
    containerWidth = jQuery(blocksSelector).width();
  }

  // Find how many blocks can fit per row
  // then round it down to a whole number
  var colNum = Math.floor(containerWidth / blockWidth);
  if (colNum == 0) colNum = 1;

  // Get the width of the row and divide it by the number of blocks it can fit
  // then round it down to a whole number.
  // This value will be the exact width of the re-adjusted block
  var colFixed = Math.floor(containerWidth / colNum);

  // Set exact width of row in pixels instead of using %
  // Prevents cross-browser bugs that appear in certain view port resolutions.
  jQuery(blocksSelector).css({ 'width' : containerWidth });

  // Set exact width of the re-adjusted block
  jQuery(blocksSelector + " li.liquid-block").css({ 'width' : colFixed });
}



//
// Fragment Operations
//

function onShowHiddenTags(button) {
  jQuery(button).siblings(".hidden-tags").show();
  jQuery(button).hide();
}

function onImageClick() {
  imageViewer.showImage(this.href);
  return false;
}

function onDeleteTagClick(tagName, form) {
  form.tagToDelete.value = tagName;
}

function onFragmentChecked(checkbox, fragmentId, fragmentTitle) {
  if (checkbox.checked)
    selectedFragments.add(fragmentId, fragmentTitle);
  else 
    selectedFragments.remove(fragmentId);
}

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
// Fragment highlighting
//

function highlightFragment(id, baseSelector) {
  var selector = ".fragment-header-" + id;
  if (baseSelector != null) selector = baseSelector + " " + selector;
  jQuery(selector).fadingHighlight("#ff9900");
}



//
// Fragment 
//

var Fragment = {
  getId: function(node) {
    return jQuery(node).closest("table.fragment").find("span.fragment-id:first").text();
  },
  
  setUpQuickEdit: function() {
    jQuery("div.fragment-content-text").live('dblclick', function() {
	  var contentDiv = jQuery(this);
	  var contentDivHeight = contentDiv.height();
	  var id = Fragment.getId(contentDiv);
	  contentDiv.empty().putLoadingIcon();
	  var editorDiv = contentDiv.siblings("div.fragment-content-editor");
	  jQuery.get("html/fragment-content-editor.htm", {"id" : id}, function(html) {
        contentDiv.empty();
		editorDiv.html(html);
		
		var editor = editorDiv.find("textarea.fragment-content");
		editor.markItUp(markItUpSettings);
		editorDiv.find(".markItUp .markItUpButton9 a")
		  .attr("href", constants["wiki-help-href"]).click(onWikiHelpClick);
		
		var height = Math.max(contentDivHeight, editor.height());
		editor.height(Math.min(height, 500));
      });
	});
  },
  
  onQuickEditCancel: function(button) {
	var editorDiv = jQuery(button).closest("div.fragment-content-editor");
	editorDiv.empty();
	
	var id = Fragment.getId(editorDiv);
	var contentDiv = editorDiv.siblings("div.fragment-content-text");
	contentDiv.empty().putLoadingIcon();
	jQuery.get("html/fragment-node-content.htm", {"id" : id}, function(html) {
	  contentDiv.html(jQuery(html).find("div.fragment-content").html());
      prettyPrint();
    });
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
  
  CLOSED: "down",
  OPENED: "up",
  
  onContentToggleClick: function(toggle, id) {
    if (jQuery(toggle).hasDisabledFlag()) return;
    
    var table = jQuery(toggle).closest("table.fragment-node");
    var span = jQuery(toggle).closest("span.fragment-content-toggle");
    
    // Open
    if (this.isClosed(toggle)) {
      jQuery(toggle).setDisabledFlag();
      var loadIcon = jQuery(span).putLoadingIcon("margin: -2px; margin-left: 5px;");
      this.setOpened(toggle);
      
      jQuery.get("html/fragment-node-content.htm", {"id" : id}, function(html) {
        table.append(jQuery(html));
        loadIcon.remove();
        jQuery(toggle).deleteDisabledFlag();
        prettyPrint();
      });
    }
    // Close
    else if (this.isOpened(toggle)) {
      table.find("tr.fragment-body").remove();
      this.setClosed(toggle);
    }
  },
  
  isClosed: function(toggle) {
    return jQuery(toggle).children("img").attr("src").indexOf(this.CLOSED) != -1;
  },
  
  isOpened: function(toggle) {
    return jQuery(toggle).children("img").attr("src").indexOf(this.OPENED) != -1;
  },
  
  setOpened: function(toggle) {
    var icon = jQuery(toggle).children("img");
    icon.attr("src", icon.attr("src").replace(this.CLOSED, this.OPENED));
  },
  
  setClosed: function(toggle) {
    var icon = jQuery(toggle).children("img");
    icon.attr("src", icon.attr("src").replace(this.OPENED, this.CLOSED));
  },
  
  onAllContentToggleClick: function(toggle) {
    if (this.isClosed(toggle)) {
      jQuery(".fragment-content-toggle img[src*='" + this.CLOSED + "']").closest("a").click();
      this.setOpened(toggle);
    }
    else if (this.isOpened(toggle)) {
      jQuery(".fragment-content-toggle img[src*='" + this.OPENED + "']").closest("a").click();
      this.setClosed(toggle);
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

