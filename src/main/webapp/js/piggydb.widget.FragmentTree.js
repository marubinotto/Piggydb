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
