jQuery(function() {
	jQuery.updnWatermark.attachAll();
	
	jQuery("a.img-link").live("click", function() {
		piggydb.widget.imageViewer.showImage(this.href);
	  return false;
	});
	
  jQuery.Autocompleter.defaults.formatItem = function(row) { 
    return escapeHtml(row[0]); 
  };
  jQuery("#searchForm_keywords").autocomplete(piggydb.server.autoCompleteUrl, {
    minChars: 1,
    selectFirst: true,
    multiple: false,
    scrollHeight: 300
  });
  jQuery("input.single-tag").autocomplete(piggydb.server.autoCompleteUrl, {
    minChars: 1,
    selectFirst: true,
    multiple: false,
    scrollHeight: 300
  });
});



piggydb.namespace("piggydb.widget", {
	
	instances: [],
	
	getGlobalIdentifier: function(instance) {
		this.instances.push(instance);
		var index = this.instances.length - 1;
		return "piggydb.widget.instances[" + index + "]";
	}
});



/**
 * Utilities
 */
(function(module) {
	
	var _messages = piggydb.server.messages;
	
	module.showConfirmDialog = function(title, message, actionLabel, actionHandler) {
	  var dialogContent = jQuery(jQuery("#tpl-dialog-confirm").html());
	  dialogContent.attr("title", title);
	  dialogContent.append(message);
	  
	  var buttons = {};
	  buttons[actionLabel] = actionHandler;
	  buttons[_messages["cancel"]] = function() {
	    jQuery(this).dialog("close");
	  };
	  
	  dialogContent.dialog({
	    resizable: false,
	    modal: true,
	    width: 400,
	    minHeight: 100,
	    buttons: buttons
	  });
		dialogContent.closest(".ui-dialog").find(".ui-dialog-buttonset button").get(0).focus();
		return dialogContent;
	};
	
	var _tipStyle = {
		"white-space": "nowrap",
		"width": "auto",
		"max-width": "none"
	};
	
	module.putGlobalMessage = function(message) {
		var id = "global-message";
		var closeButton = ' <a class="close" href="#"><img src="images/delete.gif" alt="' + 
			_messages["close"] + '" border="0"/></a>';
		var baseElement = jQuery("#title-banner td.icon img");
		
		baseElement.qtip('destroy');
		baseElement.qtip({
			id: id,
			content: message + closeButton,
			position: {
				my: 'top left',
				at: 'bottom left',
				container: jQuery("#title-banner")
			},
			hide: {
				event: false
			},
			style: {
				classes: 'ui-tooltip-plain ui-tooltip-shadow ui-tooltip-rounded'
			}
		}).qtip('show');
		
		var tip = jQuery('#ui-tooltip-' + id);
		tip.css(_tipStyle).css("left", 15);
		tip.find("a.close")
			.css("margin-left", 5)
			.click(function() {
				baseElement.qtip('destroy');
				return false;
			});
	},
	
	module.putErrorMessage = function(baseElement, id, message, container) {
		baseElement.qtip({
			id: id,
			content: message,
			position: {
				my: 'left center',
				at: 'right center',
				container: container
			},
			hide: {
				event: false
			},
			style: {
				classes: 'ui-tooltip-red ui-tooltip-shadow ui-tooltip-rounded',
				tip: {
					corner: false
				}
			}
		}).qtip('show');
		
		jQuery('#ui-tooltip-' + id).css(_tipStyle);
		
		baseElement.qtip('reposition');
	},
	
	module.clearErrorMessage = function(baseElement) {
		baseElement.qtip('destroy');
	},
	
	module.setInputError = function(input, id, message, container) {
		input = jQuery(input);
		
		input.addClass('error');
		
		input.qtip({
			id: id,
			content: message,
			position: {
				my: 'top left',
				at: 'bottom left',
				container: container
			},
			show: {
				event: 'focus'
			},
			hide: {
				event: false
			},
			style: {
				classes: 'ui-tooltip-red ui-tooltip-shadow ui-tooltip-rounded'
			}
		}).qtip('show');
		
		jQuery('#ui-tooltip-' + id)
			.css(_tipStyle)
			.click(function() {
				input.qtip('hide');
			});
	};
	
	module.clearInputError = function(input) {
		input = jQuery(input);
		input.removeClass('error');
		input.qtip('destroy');
	};
	
})(piggydb.widget);



/**
 * The base class for HTML widgets
 */
(function(module) {
	
	var _messages = piggydb.server.messages;
	
	var _class = function(jQueryElement) {
		this.element = jQueryElement;
	};
		
	_class.prototype = {
			
	  saveState: function(name, value) {
	  	piggydb.server.putSessionValue(name, value);
	  },
	  
	  getMessage: function(key) {
	  	return _messages[key];
	  }
	};

	module.Widget = _class;
	
})(piggydb.widget);



/**
 * Facebox based on facebox (http://famspam.com/facebox)
 */
(function(module) {
	
	var _messages = piggydb.server.messages;
	
	var _class = function(id) {
		module.Widget.call(this, jQuery('\
<div id="' + id + '" class="facebox"> \
  <div class="popup"> \
    <table> \
      <tbody> \
        <tr> \
          <td class="tl"/><td class="b"/><td class="tr"/> \
        </tr> \
        <tr> \
          <td class="b"/> \
          <td class="body"> \
           <div class="header"> \
             <a href="#" class="close"> \
             <img src="images/large-delete.gif" class="close_image" alt="' + _messages["close"] + '"/></a> \
           </div> \
           <div class="content"></div> \
          </td> \
          <td class="b"/> \
        </tr> \
        <tr> \
          <td class="bl"/><td class="b"/><td class="br"/> \
        </tr> \
      </tbody> \
    </table> \
  </div> \
</div>'));
		
	  this.id = id;
	  this.body = this.element.find('.body');
	  this.content = this.element.find('.content');
	};
	
	_class.prototype = jQuery.extend({
		
	  show: function(url) {
	    this.init();  
	    this.loading();
	    
	    var outer = this;
	    jQuery.get(url, function(data) { 
	    	outer.reveal(data);
		  });
	  },
	  
	  showHtml: function(html) {
	  	this.init();
	  	this.loading();
	    this.reveal(html);
	    this.content.css({
	      'max-height': jQuery(window).height() - 120
	    });
	  },
	  
	  showImage: function(url) {
	    this.init();  
	    this.loading();
	   
	    var maxWidth = jQuery(window).width() - 80;
	    var maxHeight = jQuery(window).height() - 120;
	    var image = new Image();
	    var outer = this;
	    image.onload = function() {
	    	outer.reveal('<div class="image"><img src="' + image.src + '" /></div>');
	      var width = image.width < maxWidth ? image.width : maxWidth;
	      var height = image.height < maxHeight ? image.height : maxHeight;
	      if (width < maxWidth && height == maxHeight) width = Math.min(width + 15, maxWidth);
	      if (height < maxHeight && width == maxWidth) height = Math.min(height + 15, maxHeight);
	      outer.content.css({
		      'width':  width,
		      'height': height
		    });   
	    };
	    image.src = url;
	  },
	  
	  init: function() {
	    jQuery('#' + this.id).remove();
	    this.content.empty();
	    this.element.find(".close").click(this.onCloseClick);
	    this.element.appendTo(jQuery('body'));
	  },
	  
	  loading: function () {
		  if (this.element.find('.loading').length == 1) return true;
	
		  this.body.children().hide().end().
		  	append('<div class="loading"><img src="images/load.gif"/></div>'); 
		  this.element.show();
		},
		
		reveal: function (data) {
	    this.content.append(data);
	    this.element.find('.loading').remove();
	    this.body.children().fadeIn('normal');
		},
		
		close: function () {
			this.element.fadeOut();
		},
	
	  onCloseClick: function () {
	    var element = jQuery(this).closest(".facebox");
	    element.fadeOut(function() {
	    	element.find('.loading').remove();
		  });
		  return false;
		}		
	}, module.Widget.prototype);
	
	module.Facebox = _class;
	
	module.imageViewer = new module.Facebox("facebox-image-viewer");

})(piggydb.widget);



/**
 * ShowHideToggle
 */
(function(module) {
	
	var _class = function(id, target) {
		module.Widget.call(this, jQuery('#' + id));
		
	  this.id = id;
	  this.target = target;
	  this.onShow = null; 
	  this.icon = this.element.children("img");
	   
	  var outer = this;
	  this.element.click(function() {
	    outer.onToggleClick();
	    return false;
	  });
	};
	
	_class.prototype = jQuery.extend({
		
	  SHOW: "down",
	  HIDE: "up",
	  
	  onToggleClick: function() {
	    var iconSrc = this.icon.attr("src");
	    var stateKey = "state." + this.id;
	    if (iconSrc.indexOf(this.SHOW) != -1) {
	      this.icon.attr("src", iconSrc.replace(this.SHOW, this.HIDE));
	      this.target.slideDown("fast");
	      this.saveState(stateKey, "shown");
	      if (this.onShow) this.onShow();
	    }
	    else if (iconSrc.indexOf(this.HIDE) != -1) {
	      this.target.slideUp("fast");
	      this.icon.attr("src", iconSrc.replace(this.HIDE, this.SHOW));
	      this.saveState(stateKey, "hidden");
	    }
	  }
	}, module.Widget.prototype);

	module.ShowHideToggle = _class;
	
})(piggydb.widget);



/**
 * SidebarEntry
 */
(function(module) {
	
	var _class = function(id, toggleId) {
		module.Widget.call(this, jQuery('#' + id));
		
	  this.id = id;
	  this.content = this.element.find(".sidebar-content");
	  this.toggle = new module.ShowHideToggle(toggleId, this.content);
	  
	  var cls = module.SidebarEntry;
	  if (!cls.instances) cls.instances = [];
	  cls.instances[id] = this;
	}
	
	_class.prototype = jQuery.extend({
		
		isContentHidden: function() {
	    return this.content.css("display") == "none";
	  }
	}, module.Widget.prototype);
	
	module.SidebarEntry = _class;

})(piggydb.widget);

