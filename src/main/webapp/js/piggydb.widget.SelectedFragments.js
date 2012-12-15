jQuery(function() {
	piggydb.widget.SelectedFragments.instance = new piggydb.widget.SelectedFragments(
    "facebox-selected-fragments", 
    function (id) { piggydb.server.ajaxCommand("fragment-selection", {command: "add", id: id}) },
    function (id) { piggydb.server.ajaxCommand("fragment-selection", {command: "remove", id: id}) },
    function () { piggydb.server.ajaxCommand("fragment-selection", {command: "clear"}) });
	piggydb.widget.SelectedFragments.instance.makeDroppable();
});


(function(module) {
	
	var _messages = piggydb.server.messages;
	
	var _class = function(
	   id, 
	   callback_add,
	   callback_remove,
	   callback_clear) {
		
		module.Widget.call(this, jQuery('#' + id));
	   
	  this.content = this.element.find(".content");
	  this.ul = this.content.children("ul");
	  if (this.ul.size() == 0) this.ul = jQuery('<ul>').appendTo(this.content);
	  this.callback_add = callback_add;
	  this.callback_remove = callback_remove;
	  this.callback_clear = callback_clear;
	  this.update();
	};
	
	_class.makeDroppable = function(element) {
		element.droppable({
	  	
	    accept: ".droppable-to-fragment",
	    
	    hoverClass: "selected-fragments-drophover",
	    
	    tolerance: 'intersect',
	    
	    drop: function(event, ui) {
	      // add a tag
	      if (ui.draggable.hasClass("tag-palette-draggable")) {
	        var tagId = ui.draggable.find("span.tag span.id").text();
	        var tagName = ui.draggable.find("span.tag a.tag").text();
	        var message = _messages["confirm-add-tags-to-selected"] +
	          '<div class="detail">' + 
	          '<span class="' + domain.miniTagIconClass(tagName) + '">&nbsp;<\/span> ' + escapeHtml(tagName) + 
	          '<img class="arrow" src="images/arrow-right.gif" alt="&rarr;"/>' + 
	          _messages["selected-fragments"] + 
	          "<\/div>";
	        piggydb.widget.showConfirmDialog(_messages["add-tag"], message, _messages["add"], function () {
	          var fm = document.forms['addTagsToSelectedForm'];
	          fm.tagId.value = tagId;
	          fm.submit();
	        });
	      }
	      
	      // add a relation
	      if (ui.draggable.hasClass("relation-draggable")) {
	        var fromId = ui.draggable.find(".fragment-id").text();
	        var fromTitle = ui.draggable.find(".fragment-title").text();
	        var message = _messages["confirm-create-relations-to-selected"] +
	          '<div class="detail">' + 
	          "<strong>#" + fromId + "<\/strong> " + escapeHtml(fromTitle) + 
	          '<img class="arrow" src="images/arrow-right.gif" alt="&rarr;"/>' + 
	          _messages["selected-fragments"] + 
	          "<\/div>";
	        piggydb.widget.showConfirmDialog(_messages["create-relation"], message, _messages["create"], function () {
	          var fm = document.forms['createRelationsToSelectedForm'];
	          fm.fromId.value = fromId;
	          fm.submit();
	        });
	      }
	    }
	  });
	};
	
	_class.prototype = jQuery.extend({
		
	  CLASS_FRAGMENT_SELECTED: "selected-fragment",
	  
	  size: function() {
	    return this.element.find(".content li").size();
	  },
	  
	  add: function(id, title) {
	    this.callback_add(id);
	  
	    // Fragment headers
	    var headers = piggydb.widget.Fragment.getHeaders(id);
	    var allCheckboxes = headers.find("input.fragment-checkbox");
	    headers.addClass(this.CLASS_FRAGMENT_SELECTED);
	    allCheckboxes.attr("checked", "checked");
	  
	    // Selection widget
	    var li = jQuery('<li id="selected-fragment-' + id + '">').prependTo(this.ul);
	    li.html(jQuery("#tpl-selected-fragment-entry").html());
		  var directive = { 
	      'a.fragment-link' : function(arg) {
		      return '#' + arg.context.id;
		    },
	      'a.fragment-link[href]' : function(arg) {
	        return piggydb.server.getFragmentUrl(arg.context.id);
	      },
	      'a.remove[onclick]' : function(arg) {
	        return "piggydb.widget.SelectedFragments.instance.remove('" + arg.context.id + "'); return false;";
	      }
	    };
	    li.autoRender({"id": id, "title": title}, directive);
	    this.update();
	  },
	  
	  remove: function(id) {
	    this.callback_remove(id);
	  
	    // Fragment headers
	    var headers = piggydb.widget.Fragment.getHeaders(id);
	    var allCheckboxes = headers.find("input.fragment-checkbox");
	    headers.removeClass(this.CLASS_FRAGMENT_SELECTED);
	    allCheckboxes.removeAttr("checked");
	  
	    // Selection widget
	    jQuery('#selected-fragment-' + id).remove();
	    this.update();
	  },
	  
	  clear: function() {
	    if (!window.confirm(this.getMessage("confirm-clear-all-selections"))) return false;
	        
	    this.callback_clear();
	        
	    // Fragment headers
	    var headers = piggydb.widget.Fragment.getHeaders(null);
	    var allCheckboxes = headers.find("input.fragment-checkbox");
	    headers.removeClass(this.CLASS_FRAGMENT_SELECTED);
	    allCheckboxes.removeAttr("checked");
	    
	    // Selection widget
	    this.ul.empty();
	    this.update();
	  },
	  
	  update: function() {
	    if (this.size() > 0) {
	    	this.element.show();
	    }
	    else {
	    	this.element.fadeOut();
	    }
	  },
	  
	  onFragmentChecked: function(checkbox, fragmentId) {
	  	var fragment = new piggydb.widget.Fragment(checkbox);
	  	var title = fragment.mainTitleSpan().html();
	  	
	    if (checkbox.checked)
	      this.add(fragmentId, title);
	    else 
	      this.remove(fragmentId);
	  },
	  
	  makeDroppable: function() {
	  	_class.makeDroppable(this.element);
	  }
	}, module.Widget.prototype);
	
	module.SelectedFragments = _class;
	
})(piggydb.widget);	
