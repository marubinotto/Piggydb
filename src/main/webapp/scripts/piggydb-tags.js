/*
 * -event handler slot, which starts with "slotOn", should NOT depend on "this" context.
 * -event handler normally should NOT depend on "this" unless it is attached to a slot directory.
 */


/*
 * TagUtils
 */
var TagUtils = {
  htmlTagIcon: function(tagName) {
    return '<span class="' + tagIconClass(tagName) + '">&nbsp;<\/span>';
  },
  
  htmlTagIconSelected: function(tagName) {
    return '<span class="' + tagIconClass(tagName) + ' tagIcon-selected">&nbsp;<\/span>';
  },
  
  selectable: true,
  
  htmlTag: function(tag, isSelected, isDisabled) {
    var html = [];
    
    // icon
    if (isSelected) 
      html.push(TagUtils.htmlTagIconSelected(tag.name));
    else 
      html.push(TagUtils.htmlTagIcon(tag.name));
    
    // checkbox
    if (this.selectable) {
      html.push('<input type="checkbox"');
      if (isSelected) html.push(' checked="checked"');
      if (isDisabled) html.push(' disabled="disabled"');
      html.push(' class="tagCheckbox" name="selectedTags" value="' + tag.id + '"/>');
    }
    else {
      html.push(" ");
    }
    
    // name
    html.push('<a class="tag' + (isSelected ? ' tag-selected' : '') + '"');
    html.push(' href="tag.htm?id=' + tag.id + '">');
    html.push(tag.name + '<\/a>');

    return html.join("");
  }
};


/*
 * TagView (base class)
 */
function TagView(widget, tagData, nodeState, onTagCheckboxChange) {
  this.widget = widget;
  this.tagData = tagData;
  this.nodeState = nodeState;
  this.onTagCheckboxChange = onTagCheckboxChange;
}
TagView.prototype = {
  setSelection: function(tagId, checked) {
    var checkboxes = this.widget.find('input.tagCheckbox[value=' + tagId + ']');
    var tagIcon = checkboxes.siblings(".tagIcon").removeClass('tagIcon-selected');
    var tagLink = checkboxes.siblings("a.tag").removeClass('tag-selected');
    if (checked) {
      checkboxes.attr("checked", "checked");
      tagIcon.addClass("tagIcon-selected");
      tagLink.addClass("tag-selected");
    }
    else {
      checkboxes.removeAttr("checked");
    } 
  },
  
  disableCheckboxes: function(tagId2name) {
    for (var tagId in tagId2name) {
      this.widget.find('input.tagCheckbox[value=' + tagId + ']').attr("disabled", "disabled");
    }
  },
  
  clearAllSelection: function() {
    this.widget.find('input.tagCheckbox').removeAttr("checked")
      .siblings(".tagIcon").removeClass('tagIcon-selected').end()
      .siblings("a.tag").removeClass('tag-selected');
  },
  
  restoreSelections: function(tagId2name) {
    this.clearAllSelection();
    this.widget.find('input.tagCheckbox').removeAttr("disabled");
    for (var tagId in tagId2name) {
      this.setSelection(tagId, true);
    }
  },
  
  loading: function() {
    this.widget.html('<img src="images/load.gif" border="0" style="margin:5px"/>');
  }
};


/*
 * TagCloud
 */
function TagCloud(widget, tagData, nodeState, onTagCheckboxChange) {
  TagView.call(this, widget, tagData, nodeState, onTagCheckboxChange);
}
TagCloud.prototype = jQuery.extend({
  render: function() {
    this.widget.attr("class", "content-box tag-cloud");
    this.loading();
    var outer = this;
    this.tagData.tagsByPopularity(function(tags) {
      outer.widget.empty();
      
      if (tags.length == 0) return; 
      
      var containerDiv = jQuery('<div class="container">');
      jQuery.each(tags, function(index, tag) {
        outer.addTag(
          tag, 
          outer.nodeState.isSelected(tag.id),
          outer.nodeState.isDisabled(tag.id),
          containerDiv);
      });
      outer.widget.append(containerDiv);
    });
  },
  
  addTag: function(tag, isSelected, isDisabled, containerDiv) {
    var html = ['<span class="tag" style="font-size: ' + tag.popularity + 'px;">'];
    html.push(TagUtils.htmlTag(tag, isSelected, isDisabled));
    html.push("</span> <span> </span>");
    
    var outer = this;
    var dom = jQuery(html.join(""));
    dom.find('.tagCheckbox').click(function() {
      outer.setSelection(tag.id, this.checked);
      outer.onTagCheckboxChange(this, tag);
    });  
    
    containerDiv.append(dom);
  }
}, TagView.prototype);


/*
 * TagTree
 */
function TagTree(widget, tagData, nodeState, onTagCheckboxChange) {
  TagView.call(this, widget, tagData, nodeState, onTagCheckboxChange);
}
TagTree.prototype = jQuery.extend({
  render: function() {
    this.widget.attr("class", "content-box collapsable-tree");
    this.loading();
    var outer = this;
    this.tagData.rootTags(function (tags) {
      outer.rootUl = jQuery("<ul>").appendTo(outer.widget.empty());
      outer.addNodes(outer.rootUl, tags);
    });
  },
  
  addNodes: function(ul, tags) {
    var outer = this;
    jQuery.each(tags, function(index, tag) {
      var node = new TagTreeNode(tag, index >= (tags.length - 1));
      node.slotOnChildrenRequired = function(id, li) { 
        outer.loadChildren(id, li); 
      };
      node.slotOnCheckboxClicked = function(checkbox, tag) {
        outer.setSelection(tag.id, checkbox.checked);
        outer.onTagCheckboxChange(checkbox, tag);
      };
      ul.append(node.createDom(
          outer.nodeState.isSelected(tag.id), 
          outer.nodeState.isDisabled(tag.id)));
    });
  },
  
  loadChildren: function(id, li) {
    var loadIcon = jQuery(
      '<span><br/><img src="images/load.gif" border="0" style="margin:5px"/><\/span>')
        .appendTo(li);
    
    var childrenUl = li.children("ul");
    if (childrenUl.length == 0) childrenUl = jQuery("<ul>").appendTo(li);
    
    var outer = this;
    this.tagData.children(id, function(tags) {
      outer.addNodes(childrenUl, tags);
      loadIcon.remove();
    });
  }
}, TagView.prototype);


/*
 * TagTreeNode
 */
function TagTreeNode(tag, isLast) {
  this.tag = tag;
  this.isLast = isLast;
}
TagTreeNode.prototype = {
  nodeType: function() {
    if (this.tag.hasChildren)
	    return this.isLast ? "collapsedLastNode" : "collapsed";
    else
      return this.isLast ? "leafLastNode" : "leaf";
  }	,
  
  createDom: function(isSelected, isDisabled) {
    // HTML
    var html = ['<li class="' + this.nodeType() + '">'];
    if (this.tag.hasChildren) {
      html.push('<a href="#" class="toggle">');
      html.push('<img src="style/tree/transparent.gif" border="0"/><\/a>&nbsp;');
    }
    html.push(TagUtils.htmlTag(this.tag, isSelected, isDisabled));
    html.push('<\/li>');
    
    // create DOM
    var dom = jQuery(html.join(""));
    var outer = this;
    dom.find('.toggle').click(function() {
      outer.onToggleClicked(this);
      return false;
    });
    dom.find('.tagCheckbox').click(function() {
      outer.slotOnCheckboxClicked(this, outer.tag);
    });  
    return dom;
  },
  
  onToggleClicked: function(toggle) {
    var li = jQuery(toggle.parentNode);
    var nodeType = li.attr("class");
    li.removeAttr("class");
    if (nodeType.match("^collapsed")) {
      li.addClass(nodeType.replace("collapsed", "expanded"));
      this.slotOnChildrenRequired(this.tag.id, li);        
    }
    else if (nodeType.match("^expanded")) {
      li.addClass(nodeType.replace("expanded", "collapsed"));
      li.children("ul").remove();      
    }
  },
  
  slotOnChildrenRequired: function(id, li) {},
  
  slotOnCheckboxClicked: function(checkbox, tag) {}
};


/*
 * TagFlat
 */
function TagFlat(widget, tagData, nodeState, onTagCheckboxChange, messages) {
  TagView.call(this, widget, tagData, nodeState, onTagCheckboxChange);
  this.pageIndex = 0;
  this.messages = messages;
}
TagFlat.prototype = jQuery.extend({
  render: function() {
    this.widget.attr("class", "content-box tag-flat");
    this.loading();
    var outer = this;
    this.tagData.allTags(this.pageIndex, function(response) {
      outer.widget.empty();
      
      var pageIndex = response[0].pageIndex;
      var pageCount = response[0].pageCount;
      if (pageCount > 1) {
        outer.widget.append(outer.htmlPageLink(pageIndex, pageCount));
        outer.widget.append("<br/>");
      }
      
      var containerDiv;
      var currentChar = -1;
      var others = false;
      jQuery.each(response[1], function(index, tag) {
        // Divide by the first char if it's ascii
        var firstChar = tag.name.charAt(0).toLowerCase();
        if ((firstChar != currentChar) && !others) {
          currentChar = firstChar;
          containerDiv = jQuery('<div class="container">').appendTo(outer.widget);
          if (tag.name.charCodeAt(0) > 127) others = true;
        }
        outer.addTag(
          tag, 
          outer.nodeState.isSelected(tag.id),
          outer.nodeState.isDisabled(tag.id),
          containerDiv);
      });
      
      if (pageCount > 1) {
        outer.widget.append(outer.htmlPageLink(pageIndex, pageCount));
      }
    });
  },
  
  addTag: function(tag, isSelected, isDisabled, containerDiv) {
    var html = ['<span class="tag">'];
    html.push(TagUtils.htmlTag(tag, isSelected, isDisabled));
    html.push("</span> <span> </span>");
    
    var outer = this;
    var dom = jQuery(html.join(""));
    dom.find('.tagCheckbox').click(function() {
      outer.setSelection(tag.id, this.checked);
      outer.onTagCheckboxChange(this, tag);
    });  
    
    containerDiv.append(dom);
  },
  
  htmlPageLink: function(pageIndex, pageCount) {
    var html = ['<table class="page-link" width="100%" border="0" cellpadding="0" cellspacing="0">'];
    html.push('<tr>');
    html.push('<td class="previous" width="30%" align="left" valign="middle">');
    if (pageIndex > 0)
      html.push('<a href="#">&lt; ' + this.messages["previous"] + '</a>');
    html.push('</td>');
    html.push('<td class="page-number" align="center" valign="middle">');
    html.push((pageIndex + 1) + " / " + pageCount);
    html.push('</td>');
    html.push('<td class="next" width="30%" align="right" valign="middle">');
    if (pageIndex < (pageCount - 1))
      html.push('<a href="#">' + this.messages["next"] + ' &gt;</a>');
    html.push('</td>');
    html.push('</tr>');
    html.push('</table>');
    
    var outer = this;
    var dom = jQuery(html.join(""));
    dom.find('.previous a').click(function() {
      outer.pageIndex--;
      outer.render();
    });  
    dom.find('.next a').click(function() {
      outer.pageIndex++;
      outer.render();
    });  
    
    return dom;
  }
}, TagView.prototype);


/*
 * TagList
 */
function TagList(ul) {
  this.ul = ul;
  this.id2name = {};
}
TagList.prototype = {
  get: function(id) {
    return this.id2name[id];
  },
  
  addOrRemove: function(add, id, name) {
    if (add) 
      this.id2name[id] = name; 
    else 
      delete this.id2name[id];
    this.updateView();
  },
  
  clear: function() {
    this.id2name = {};
    this.updateView();
  },
    
  updateView: function() {
    var count = 0;
    this.ul.empty();
    for (var id in this.id2name) {
      var html = ['<li>'];
      html.push(TagUtils.htmlTagIconSelected(this.id2name[id]) + ' ');
      if (isNaN(id)) {
        html.push(this.id2name[id]);
        html.push(' <span style="color: silver;">(new)<\/span>');
      }
      else {
        html.push('<a class="tag" href="tag.htm?id=' + id  + '">');
        html.push(this.id2name[id]);
        html.push('<\/a>');
      }
      html.push(' <a class="delete-tag" href="#">');
      html.push('<img src="images/delete.gif" /><\/a><\/li>');
      var li = jQuery(html.join(""));
      
      // Set an event handler for the delete button
      var tagList = this;
      var createHandler = function(id) {
        return function() {
          tagList.onDeleteButtonClick(id);
          return false;
        }
      };
      li.find('a.delete-tag').click(createHandler(id));
      
      this.ul.append(li);
      count++;
    }
    this.slotOnUpdate(count);
  },
  
  slotOnUpdate: function(count) {},
  
  onDeleteButtonClick: function(id) {
    this.addOrRemove(false, id, null);
    this.slotOnTagDeletion(id);
  },
  
  slotOnTagDeletion: function(tagId) {}
};


/*
 * TagSelection
 */
function TagSelection(widget, deleteAllConfirmMessage) {
  this.widget = widget;
  this.deleteAllConfirmMessage = deleteAllConfirmMessage;
  this.selectedTags = new TagList(this.widget.find('div.selected-tags ul.tags'));
  this.classifyingTags = new TagList(this.widget.find('div.classified-by ul.tags'));
  
  // event handlers
  var outer = this;
  this.widget.find("#create-filter").click(function() { 
    outer.onCreateFilterClick();
  });
  this.widget.find("#set-classification-mode").click(function() { 
    outer.onSetClassificationModeClick();
  });
  this.widget.find("#delete-all").click(function() { 
    outer.onDeleteAllClick();
  });
  this.widget.find("#back-to-selection").click(function() { 
    outer.onBackToSelectionClick();
  });
  this.widget.find("#add-classifying-tag").click(function() { 
    outer.onAddClassifyingTagClick();
  });
  this.widget.find("#classify-all").click(function() { 
    outer.onClassifyAllClick();
  });
  this.selectedTags.slotOnUpdate = function(count) {
    if (count > 0)
      outer.widget.show();
    else
      outer.widget.fadeOut();
  };
}
TagSelection.prototype = {
  setHandlerOnTagDeletion: function(onTagDeletion) {
    this.selectedTags.slotOnTagDeletion = onTagDeletion;
    this.classifyingTags.slotOnTagDeletion = onTagDeletion;
  },
  
  isClassificationMode: function() {
    return this.widget.find('#classified-by-pane').css("display") != "none";
  },
  
  isSelected: function(tagId) {
    return this.selectedTags.get(tagId) || 
      (this.isClassificationMode() && this.classifyingTags.get(tagId));
  },
  
  isDisabled: function(tagId) {
    return this.selectedTags.get(tagId) && this.isClassificationMode();
  },
  
  setSelection: function(selected, id, name) {
    if (this.isClassificationMode()) {
      this.classifyingTags.addOrRemove(selected, id, name);
    }
    else {
      this.selectedTags.addOrRemove(selected, id, name);
    }
  },
  
  onCreateFilterClick: function() {
    var form = jQuery('form[name=filter-form]');
    form.empty();
    
    FormUtils.addParamToForm(form, "new", "true");
    for (var tagId in this.selectedTags.id2name) {
      FormUtils.addParamToForm(form, "tagIds", tagId);
    }
    form.submit();
  },
  
  onSetClassificationModeClick: function() {
    this.classifyingTags.clear();
    
    this.widget.find('#selected-tags-pane .section-title').css({ color:"silver" });
    this.widget.find('div.selected-tags .delete-tag').hide();
    this.widget.find('#buttons-for-selected-tags').hide(); 
    this.widget.find('#classified-by-pane').show();
    this.widget.find('#buttons-for-classification').show();
   
    this.slotOnClassificationModeSet(this.selectedTags.id2name);
  },
  
  onDeleteAllClick: function() {
    if (!window.confirm(this.deleteAllConfirmMessage)) return;
    
    var form = jQuery('form[name=tags-form]').empty();
    FormUtils.addParamToForm(form, "command", "deleteAll");
    for (var tagId in this.selectedTags.id2name) {
      FormUtils.addParamToForm(form, "selectedTagIds", tagId);
    }
    form.submit();
  },
  
  slotOnClassificationModeSet: function(selectedId2name) {},
  
  onBackToSelectionClick: function() {
    this.widget.find('#classified-by-pane').hide();
    this.widget.find('#buttons-for-classification').hide();
    this.widget.find('div.selected-tags .delete-tag').show();
    this.widget.find('#buttons-for-selected-tags').show();
    this.widget.find('#selected-tags-pane .section-title').css({ color:"black" });
    
    this.slotOnBackToSelection(this.selectedTags.id2name);
  },
  
  slotOnBackToSelection: function(selectedId2name) {},
  
  onAddClassifyingTagClick: function() {
    // Input
    var tagNameInput = this.widget.find('#classifying-tag-name');
    var tagName = jQuery.trim(tagNameInput.val());
    tagNameInput.val("");
    if (tagName == null || tagName == "") return;
    
    // Check the name
    var outer = this;
    jQuery.post(
      "command/get-tag-id.htm", 
      { name: tagName }, 
      function(tagId) {
        // a new tag (no id)
        if (jQuery.trim(tagId) == "") {
          tagId = "new" + new Date().getTime();
        }
        // an existing tag
        else {
          if (outer.selectedTags.get(tagId)) return;
          outer.slotOnTagSelection(tagId);
        }
        outer.setSelection(true, tagId, escapeHtml(tagName));
      });
  },
  
  slotOnTagSelection: function(tagId) {},
  
  onClassifyAllClick: function() {
    var form = jQuery('form[name=tags-form]').empty();

    FormUtils.addParamToForm(form, "command", "classifyAll");
    
    for (var tagId in this.selectedTags.id2name) {
      FormUtils.addParamToForm(form, "selectedTagIds", tagId);
    }

    var count = 0;
    for (var tagId in this.classifyingTags.id2name) {
      count++;
      if (isNaN(tagId)) {
        FormUtils.addParamToForm(form, 
          "classifyingTagNames", this.classifyingTags.id2name[tagId]);
      }
      else {
        FormUtils.addParamToForm(form, "classifyingTagIds", tagId);
      }
    }
    if (count > 0) form.submit();
  }
};

