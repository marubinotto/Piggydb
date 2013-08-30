(function(module) {
  
  var _COLLAPSED = "plus";
  var _EXPANDED = "minus";
  
  var _class = function(toggle) {
  	this.toggle = jQuery(toggle);
  	this.fragment = new module.Fragment(toggle);
  };
  
  _class.prototype = jQuery.extend({
  	
  	node: function() {
  		return this.toggle.closest("li");
  	},
  	
  	icon: function() {
  		return this.toggle.children("img");
  	},
  	
  	iconSrc: function() {
  		return this.icon().attr("src");
  	},
  	
  	isCollapsed: function() {
  		return this.iconSrc().indexOf(_COLLAPSED) != -1;
  	},
  	
  	isExpanded: function() {
  		return this.iconSrc().indexOf(_EXPANDED) != -1;
  	},
  	
  	expand: function() {
  		this.icon().attr("src", this.iconSrc().replace(_COLLAPSED, _EXPANDED));
  		this.fragment.buttonToOpenChildren().hide();
  	},
  	
  	collapse: function() {
  		this.icon().attr("src", this.iconSrc().replace(_EXPANDED, _COLLAPSED));
  		this.fragment.buttonToOpenChildren().show();
  	},
  	
  	disable: function() {
  		this.toggle.setDisabledFlag();
  	},
  	
  	isDisabled: function() {
  		return this.toggle.hasDisabledFlag();
  	},
  	
  	enable: function() {
  		this.toggle.deleteDisabledFlag();
  	},
  	
  	click: function() {
  		this.toggle.click();
  	}
  	
  }, module.Widget.prototype);
  
  module.FragmentNodeToggle = _class;
  
  
  module.FragmentTree = {
    
    onNodeToggleClick: function(toggle, id, contextParentId) {
    	var toggle = new module.FragmentNodeToggle(toggle);
      if (toggle.isDisabled()) return;
    
      var node = toggle.node();
       
      // Expand
      if (toggle.isCollapsed()) {
      	toggle.disable();
      	toggle.expand();
        var loadIcon = node.putLoadingIcon("margin:5px");
        var params = {"id" : id};
        if (contextParentId != null) params.contextParentId = contextParentId;
        jQuery.get("partial/fragment-child-nodes.htm", params, function(childrenHtml) {
        	node.append(childrenHtml);
          loadIcon.remove();
          toggle.enable();
        });
      }
      // Collapse
      else if (toggle.isExpanded()) {
      	toggle.collapse();
      	node.children("ul").remove();
      }
    },
    
    onReorderSwitchClick: function (button, parentId) {
      var button = jQuery(button);
      if (button.hasClass("selected")) {
        module.FragmentTree.disableSortable(button);
        button.removeClass("selected");
      }
      else {
        module.FragmentTree.enableSortable(button, parentId);
        button.addClass("selected");
      }
    },
    
    enableSortable: function(button, parentId) {
      var container = jQuery(button).closest("div.children");
      var sortableUl = container.find(".sortable-children");
      sortableUl.sortable({
        update: function(event, ui) {
          var children = jQuery(this);
          var childOrder = children.sortable('toArray');
          var processingIcon = jQuery(
            '<span><img src="images/load.gif" border="0"/><\/span>')
              .appendTo(container.find("span.processing-children"));
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
      sortableUl.find("table.fragment-root-node").toggleClass("fragment-node-movable", true);
      sortableUl.find(".fragment-root-node .root-header-cell .fragment-header")
        .prepend('<img class="sortable-icon" src="images/sortable.png" border="0" alt=""/>');
    },
    
    disableSortable: function(button) {
      var container = jQuery(button).closest("div.children");
      var sortableUl = container.find("ul.sortable-children");
      sortableUl.sortable("destroy");
      sortableUl.enableSelection();
      sortableUl.find("table.fragment-root-node").toggleClass("fragment-node-movable", false);
      sortableUl.find(".fragment-root-node .sortable-icon").remove();
    }    
  };
  
})(piggydb.widget);  
