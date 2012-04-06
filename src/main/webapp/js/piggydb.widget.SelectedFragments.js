(function(module) {
	
	module.SelectedFragments = function(
	   id, 
	   fragmentUrlPrefix, 
	   clearConfirmMessage,
	   callback_add,
	   callback_remove,
	   callback_clear) {
	   
	  this.widget = jQuery('#' + id);
	  this.content = this.widget.find(".content");
	  this.ul = this.content.children("ul");
	  if (this.ul.size() == 0) this.ul = jQuery('<ul>').appendTo(this.content);
	  this.fragmentUrlPrefix = fragmentUrlPrefix;
	  this.clearConfirmMessage = clearConfirmMessage;
	  this.callback_add = callback_add;
	  this.callback_remove = callback_remove;
	  this.callback_clear = callback_clear;
	  this.update();
	};
	
	module.SelectedFragments.prototype = jQuery.extend({
		
	  CLASS_FRAGMENT_SELECTED: "selected-fragment",
	  
	  size: function() {
	    return this.widget.find(".content li").size();
	  },
	  
	  add: function(id, title) {
	    this.callback_add(id);
	  
	    // Fragment headers
	    var headers = Fragment.getHeaders(id);
	    var allCheckboxes = headers.find("input.fragment-checkbox");
	    headers.addClass(this.CLASS_FRAGMENT_SELECTED);
	    allCheckboxes.attr("checked", "checked");
	  
	    // Selection widget
	    var urlPrefix = this.fragmentUrlPrefix;
	    var li = jQuery('<li id="selected-fragment-' + id + '">').prependTo(this.ul);
	    li.html(jQuery("#tpl-selected-fragment-entry").html());
		  var directive = { 
	      'a.fragment-link' : function(arg) {
		      return '#' + arg.context.id;
		    },
	      'a.fragment-link[href]' : function(arg) {
	        return urlPrefix + arg.context.id;
	      },
	      'a.remove[onclick]' : function(arg) {
	        return "selectedFragments.remove('" + arg.context.id + "'); return false;";
	      }
	    };
	    li.autoRender({"id": id, "title": title}, directive);
	    this.update();
	  },
	  
	  remove: function(id) {
	    this.callback_remove(id);
	  
	    // Fragment headers
	    var headers = Fragment.getHeaders(id);
	    var allCheckboxes = headers.find("input.fragment-checkbox");
	    headers.removeClass(this.CLASS_FRAGMENT_SELECTED);
	    allCheckboxes.removeAttr("checked");
	  
	    // Selection widget
	    jQuery('#selected-fragment-' + id).remove();
	    this.update();
	  },
	  
	  clear: function() {
	    if (!window.confirm(this.clearConfirmMessage)) return false;
	        
	    this.callback_clear();
	        
	    // Fragment headers
	    var headers = Fragment.getHeaders(null);
	    var allCheckboxes = headers.find("input.fragment-checkbox");
	    headers.removeClass(this.CLASS_FRAGMENT_SELECTED);
	    allCheckboxes.removeAttr("checked");
	    
	    // Selection widget
	    this.ul.empty();
	    this.update();
	  },
	  
	  update: function() {
	    if (this.size() > 0) {
	      this.widget.show();
	    }
	    else {
	      this.widget.fadeOut();
	    }
	  },
	  
	  onFragmentChecked: function(checkbox, fragmentId) {
	  	var fragment = new Fragment(checkbox);
	  	var title = fragment.mainTitleSpan().html();
	  	
	    if (checkbox.checked)
	      this.add(fragmentId, title);
	    else 
	      this.remove(fragmentId);
	  }			
	}, module.Widget.prototype);
	
})(piggydb.widget);	
