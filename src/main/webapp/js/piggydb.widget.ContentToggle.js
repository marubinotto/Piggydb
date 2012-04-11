(function(module) {
	
	var _class = function(toggleButton) {
		this.toggleButton = jQuery(toggleButton);
		this.toggleSpan = this.toggleButton.closest("span.fragment-content-toggle");
		this.fragment = new piggydb.widget.Fragment(toggleButton);
	};
	
	_class.CLOSED = "down";
	_class.OPENED = "up";
	
	_class.onContentToggleClick = function(toggle, id) {
		var toggle = new _class(toggle);
		
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
	
	_class.onAllContentToggleClick = function(toggle, root) {
		var toggle = new _class(toggle);
	  if (toggle.isClosed()) {
	  	root.find(".fragment-content-toggle img[src*='" + _class.CLOSED + "']").closest("a").click();
	    toggle.setOpened();
	  }
	  else if (toggle.isOpened()) {
	  	root.find(".fragment-content-toggle img[src*='" + _class.OPENED + "']").closest("a").click();
	    toggle.setClosed();
	  }
	};
	
	_class.prototype = jQuery.extend({
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
	    return this.buttonImgSrc().indexOf(_class.CLOSED) != -1;
	  },
	  
	  isOpened: function() {
	    return this.buttonImgSrc().indexOf(_class.OPENED) != -1;
	  },
	  
	  setOpened: function() {
	    var img = this.buttonImg();
	    img.attr("src", img.attr("src").replace(_class.CLOSED, _class.OPENED));
	  },
	  
	  setClosed: function() {
	    var img = this.buttonImg();
	    img.attr("src", img.attr("src").replace(_class.OPENED, _class.CLOSED));
	  }
	}, module.Widget.prototype);
	
	module.ContentToggle = _class;
	
})(piggydb.widget);	
