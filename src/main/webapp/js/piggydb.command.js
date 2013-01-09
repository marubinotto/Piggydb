piggydb.namespace("piggydb.command", (function() {
	
	var _messages = piggydb.server.messages;
	
	var _commands = {};
	
	_commands.deleteRelation = function(id, relationHtml, relationContainerHtml) {
    if (!window.confirm(_messages["confirm-delete-relation"])) 
      return false;
    
    piggydb.server.ajaxCommand("delete-relation", {"id": id});
    relationHtml.fadeOut("slow", function() {
      if (relationContainerHtml != null && relationHtml.siblings().size() == 0)
        relationContainerHtml.remove();
      else
        relationHtml.remove();
    });
  };
  
  _commands.removeTag = function(fragmentId, tagName) {
    if (!window.confirm(_messages["confirm-remove-tag"] + ' : "' + tagName + '"')) 
      return false;
      
    var fm = document.forms['removeTagForm'];
    fm.fragmentId.value = fragmentId;
    fm.tagName.value = tagName;
    fm.submit();
  };
  
  _commands.addTag = function(fragmentId, tagName) {
    var fm = document.forms['addTagForm'];
    fm.fragmentId.value = fragmentId;
    fm.tagName.value = tagName;
    fm.submit();
  };
  
  _commands.removeBookmark = function(fragmentId) {
    if (!window.confirm(_messages["confirm-remove-bookmark"])) 
      return false;
      
    var fm = document.forms['removeBookmarkForm'];
    fm.fragmentId.value = fragmentId;
    fm.submit();
  };
  
  _commands.putAtHome = function(fragmentId) {
  	var fm = document.forms['createRelationForm'];
    fm.fromId.value = piggydb.widget.Fragment.ID_HOME;
    fm.toId.value = fragmentId;
    fm.forward.value = "on";
    fm.submit();
  };
	
	return _commands;
})());
