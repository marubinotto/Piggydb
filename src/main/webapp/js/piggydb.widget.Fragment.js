jQuery(function() {
  piggydb.widget.Fragment.init();
});


(function(module) {
  
  var _messages = piggydb.server.messages;
  
  var _class = function(node) {
    this.node = jQuery(node);
    this.root = this.node.closest("table.fragment");
  };
  
  _class.ID_HOME = "0";
  
  _class.init = function() {
    prettyPrint();
    jQuery("table.fragment").live('mouseenter', function() {
      jQuery(this).find(".fragment-tools").eq(0).show();
    });
    jQuery("table.fragment").live('mouseleave', function() {
      jQuery(this).find(".fragment-tools").eq(0).hide();
    });
    _class.makeFragmentsDroppable("table.fragment", null);
    _class.makeRelationsDraggable("");
  };
  
  _class.initForPartial = function(partialSelector) {
    _class.makeFragmentsDroppable(partialSelector + " table.fragment", null);
    _class.makeRelationsDraggable(partialSelector + " ");
  };
  
  _class.findInTheSameFragmentNode = function(node, selector) {
    return jQuery(node).closest("table.fragment-node").find(selector);
  };
  
  _class.highlight = function(id, baseNode) {
    var selector = ".fragment-header-" + id;
    var color = "#ff9900";
    if (baseNode == null)
      jQuery(selector).fadingHighlight(color);
    else
      jQuery(baseNode).find(selector).fadingHighlight(color);
  };
  
  _class.onShowHiddenTags = function(button) {
    jQuery(button).siblings(".hidden-tags").show();
    jQuery(button).hide();
  };
  
  _class.syncTitles = function(id, title, headline) {
    var selector = ".fragment-header-" + id + " span.title";
    jQuery(selector).html(headline);
    jQuery("table.fragment-full > tbody > tr > th.header-cell " + selector).html(title);
  };
  
  _class.syncCaptions = function(id, caption) {
    jQuery(".fragment-header-" + id + " span.fragment-caption").html(caption.html());
  };
  
  _class.getHeaders = function(fragmentId) {
    var headerClass = ".fragment-header";
    if (fragmentId != null) headerClass = headerClass + "-" + fragmentId;
    return jQuery(headerClass);
  };
  
  _class.makeFragmentsDroppable = function(selector, hoverClass) {
    jQuery(selector).droppable({
      
      accept: function(draggable) {
        if (!draggable.hasClass("droppable-to-fragment")) return false;
        
        // check if the relation can be created
        if (draggable.hasClass("relation-draggable")) {
          var from = draggable.find(".fragment-id").text();
          var to = jQuery(this).find(".fragment-id:first").text();
          if (from == to) return false;
        }
        
        return true;
      },
      
      hoverClass: hoverClass != null ? hoverClass : 'fragment-drophover',
          
      greedy: true, 
      
      tolerance: 'intersect',
      
      drop: function(event, ui) {
        var targetId = jQuery(this).find(".fragment-id:first").text();
        
        // add a tag
        if (ui.draggable.hasClass("tag-palette-draggable")) {
          var tagId = ui.draggable.find(".tag .id").text();
          if (isNotBlank(tagId)) {
            var tags = jQuery("span.tags-placeholder-" + targetId);
            tags.empty().putLoadingIcon("margin: -2px; margin-left: 5px;");
            jQuery.get("partial/add-tag.htm", {"fragmentId": targetId, "tagId": tagId}, 
              function(html) {
                tags.empty().append(jQuery(html).children("span.tags"));
                _class.highlight(targetId, null);
              });
          }
        }
        
        // create a relationship
        if (ui.draggable.hasClass("relation-draggable")) {
          var fromId = ui.draggable.find(".fragment-id").text();
          var fromTitle = ui.draggable.find(".fragment-title").text();
          var toTitle = jQuery(this).find(".fragment-tools .fragment-title:first").text();
          var dialog = piggydb.widget.showConfirmDialog(
            _messages["create-relation"], 
            _createRelationConfirmHtml(fromId, fromTitle, targetId, toTitle), 
            _messages["create"], 
            function () {
              var forward = jQuery(this).find("input.forward")[0].checked;
              var backward = jQuery(this).find("input.backward")[0].checked;
            
              var fm = document.forms['createRelationForm'];
              fm.fromId.value = fromId;
              fm.toId.value = targetId;
              if (forward) fm.forward.value = "on";
              if (backward) fm.backward.value = "on";
              fm.submit();
            }
          );
        }
      }
    });
  };
  
  var _createRelationConfirmHtml = function(fromId, fromTitle, toId, toTitle) {
    var html = jQuery(jQuery("#tpl-confirm-create-relation").html());
    html.find(".from-id").html(_toIcon(fromId));
    html.find(".from-title").text(fromTitle);
    html.find(".to-id").html(_toIcon(toId));
    html.find(".to-title").text(toTitle);
    return html;
  };
  
  var _toIcon = function(fragmentId) {
    if (fragmentId == _class.ID_HOME)
      return '<img class="home-icon" src="style/images/mini-tag-home.png" border="0" alt=""/>';
    else
      return "#" + fragmentId;
  }
  
  _class.makeRelationsDraggable = function(selectorPrefix) {
    jQuery(selectorPrefix + ".fragment-tools .relation-draggable").draggable({ 
      revert: true,
      helper: 'clone',
      appendTo: 'body',
      opacity: 0.70,
      zIndex: 120,
      cursorAt: { bottom: 2, right: 0 }
    });  
  };
  
  _class.reloadRootChildNodes = function(parentId, newId) {
    var selectorChildren = "table.fragment-" + parentId + " div.children";
    var div = jQuery(selectorChildren).empty().putLoadingIcon("margin: 5px;");
    jQuery.get("partial/fragment-root-child-nodes.htm", {id: parentId}, function(childrenHtml) {
      div.html(childrenHtml);
      _class.initForPartial(selectorChildren);
      _class.highlight(newId, null);
      piggydb.widget.ContentToggle.clickContentToggle(newId);
    });
  };
  
  _class.prototype = jQuery.extend({
    
    id: function() {
      return this.root.find("span.fragment-id:first").text();
    },
    
    header: function() {
      return this.root.find("div.fragment-header:first");
    },
    
    caption: function() {
      return this.header().find("span.fragment-caption");
    },
    
    mainTitleSpan: function() {
      return this.header().find("span.title");
    },
    
    shortTitleSpan: function() {
      return this.header().find(".fragment-tools span.fragment-title");
    },
    
    headerRow: function() {
      return this.header().closest("tr");
    },
    
    bodyRow: function() {
      return this.headerRow().siblings("tr.fragment-body");
    },
    
    hasBodyRow: function() {
      return this.bodyRow().size() > 0;
    },
    
    setBodyRow: function(rowHtml) {
      this.bodyRow().remove();
      this.headerRow().after(rowHtml);
    },
    
    textContentDiv: function() {
      return this.bodyRow().find("div.fragment-content-text");
    },
    
    emptyTextContent: function() {
      var contentToggle = this.contentToggle();
      if (contentToggle != null) contentToggle.setClosed();
      this.contentToggleContainer().hide();
      this.bodyRow().remove();
    },
    
    isFull: function() {
      return this.root.hasClass("fragment-full");
    },
    
    isMultirow: function() {
      return this.root.hasClass("multirow");
    },
    
    isMain: function() {
      return this.root.hasClass("fragment-main");
    },
    
    isCellCompact: function() {
      return this.root.hasClass("fragment-cell-compact");
    },
    
    isEditable: function() {
      return this.header().find("a.edit-fragment").size() > 0;
    },
    
    contentToggleContainer: function() {
      return this.header().find("span.fragment-content-toggle");
    },
    
    contentToggle: function() {
      var toggle = this.header().find(".fragment-content-toggle a.tool-button");
      return toggle.size() == 0 ? null : new piggydb.widget.ContentToggle(toggle);
    },
    
    openContentIfClosed: function() {
      var toggle = this.contentToggle();
      if (toggle != null) toggle.open();
    },
    
    highlight: function() {
      _class.highlight(this.id(), this.root);
    },
    
    fullEditor: function() {
      var editor = this.root.siblings(".fragment-form-panel");
      return editor.size() > 0 ? editor : null;
    },
    
    quickEditor: function() {
      return this.bodyRow().find("div.fragment-editor-quick");
    },
    
    closeQuickEditor: function() {
      this.quickEditor().empty();
    },
    
    tagsPlaceholder: function() {
      return jQuery("span.tags-placeholder-" + this.id());
    },
    
    update: function(propertiesHtml) {
      var properties = jQuery(propertiesHtml);
      
      // caption
      _class.syncCaptions(
        this.id(), 
        properties.find("div.prop-caption > div.default > span.fragment-caption"));
      if (this.isCellCompact()) {
        this.caption().html(properties.find(
          "div.prop-caption > div.cell-compact > span.fragment-caption").html());
      }
      
      // title
      _class.syncTitles(
        this.id(), 
        properties.find("div.prop-title > span.title").html(),
        properties.find("div.prop-title > span.headline").html());
      
      this.shortTitleSpan().html(
        properties.find("div.prop-title > span.title-short").html());
      
      // body row
      var bodyRow = properties.children("div.prop-body-row");
      if (isNotBlank(bodyRow.find("div.fragment-content").html())) {
        if (this.hasBodyRow() || this.isFull()) {
          this.setBodyRow(bodyRow.find("tbody.body-row-container").html());
          prettyPrint();
        }
        else {
          this.contentToggleContainer().show();
          this.openContentIfClosed();
        }
      }
      else {
        this.emptyTextContent();
      }      
      
      // update info
      this.header().find("span.update-info").html(
        properties.find("div.prop-update-info > span.update-info").html());
      
      // tags
      var tags = properties.find("div.prop-tags span.tags");
      var placeholder = this.tagsPlaceholder().empty();
      if (tags.size() > 0) {
        placeholder.append(tags);
      }  
    }
  }, module.Widget.prototype);
  
  module.Fragment = _class;
  
})(piggydb.widget);  
