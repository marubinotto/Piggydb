(function(module) {
	
	var _COLLAPSED = "plus";
	var _EXPANDED = "minus";
	
	module.FragmentTree = {
	  
	  onNodeToggleClick: function(toggle, id, contextParentId) {
	    if (jQuery(toggle).hasDisabledFlag()) return;
	  
	    var li = jQuery(toggle).closest("li");
	    var icon = jQuery(toggle).children("img");
	    var iconSrc = icon.attr("src");
	    
	    // Expand
	    if (iconSrc.indexOf(_COLLAPSED) != -1) {
	      jQuery(toggle).setDisabledFlag();
	      icon.attr("src", iconSrc.replace(_COLLAPSED, _EXPANDED));
	      var loadIcon = jQuery(li).putLoadingIcon("margin:5px");
	      var params = {"id" : id};
	      if (contextParentId != null) params.contextParentId = contextParentId;
	      jQuery.get("partial/fragment-child-nodes.htm", params, function(childrenHtml) {
	        li.append(childrenHtml);
	        loadIcon.remove();
	        jQuery(toggle).deleteDisabledFlag();
	      });
	    }
	    // Collapse
	    else if (iconSrc.indexOf(_EXPANDED) != -1) {
	      icon.attr("src", iconSrc.replace(_EXPANDED, _COLLAPSED));
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
	
})(piggydb.widget);	
