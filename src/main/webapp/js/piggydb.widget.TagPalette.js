(function(module) {
	
	module.TagPalette = function(paletteDiv) {
		module.Widget.call(this, paletteDiv);
		
	  this.ref = piggydb.widget.getGlobalIdentifier(this);
	  
	  this.viewType = "tree";
	  this.sessionName = null;
	  this.autoHeight = true;
	  
	  this.onPaletteInit = null;
	  this.onPaletteUpdate = null;
	  this.onTagSelect = null;
	  
	  this.breadcrumbs = [];	// a breadcrumb => [tagId, toChildren(true/false)]
	  this.flatIndex = 0;
	};
	
	module.TagPalette.CLASS_OPENED = "pulled";
	
	module.TagPalette.DRAGGABLE_SETTINGS = { 
	  revert: true,
	  helper: 'clone',
	  appendTo: 'body',
	  opacity: 0.70,
	  zIndex: 120
	};
	
	module.TagPalette.prototype = jQuery.extend({
		
		init: function(toggleButton) {
			if (toggleButton != null) {
		    this.toggleButton = toggleButton;
		    var outer = this;
		    this.toggleButton.click(function() {
		      outer.onToggleButtonClick();
		      return false;
		    });
		  }
		  else {
		    this.open();
		  }
		},
		
	  open: function() {
	    this.breadcrumbs = [];  
	    this.element.empty().show();
	    this.switchView(this.viewType, true);
	  },
	  
	  switchView: function(name, init) {
	  	if (name == "flat")
	  		this.updatePaletteFlat({}, init);
	  	else if (name == "cloud")
	  		this.updatePaletteCloud(init);
	  	else
	  		this.updatePaletteTree({}, init);
	  },
	  
	  onViewSwitchClick: function(button, name) {
	  	if (!clickSelectSwitch(button)) return;
	  	this.switchView(name, false);
	  },
	  
	  onToggleButtonClick: function() {
	    if (this.toggleButton.hasClass(module.TagPalette.CLASS_OPENED)) {
	      this.close();
	    } 
	    else {
	      this.toggleButton.addClass(module.TagPalette.CLASS_OPENED);
	      this.open();
	    }
	  },
	  
	  close: function() {
	    this.toggleButton.removeClass(module.TagPalette.CLASS_OPENED);
	    this.element.hide();
	  },
	  
	  toRoot: function() {
	    this.breadcrumbs = [];
	    this.updatePaletteTree({}, false);
	  },
	  
	  back: function() {
	    if (this.breadcrumbs.length <= 1) {
	      this.toRoot();
	      return;
	    }
	    
	    this.breadcrumbs.pop();
	    var redo = this.breadcrumbs[this.breadcrumbs.length - 1];
	    var tagId = redo[0];
	    var toChildren = redo[1];
	    var params = toChildren ? {"parent": tagId} : {"child": tagId};
	    
	    this.updatePaletteTree(params, false);
	  },
	  
	  toParent: function(tagId) {
	  	this.breadcrumbs.push([tagId, false]);
	  	this.updatePaletteTree({"child": tagId}, false);
	  },
	  
	  toChild: function(tagId) {
	  	this.breadcrumbs.push([tagId, true]);
	  	this.updatePaletteTree({"parent": tagId}, false);
	  },
	  
	  updatePaletteTree: function(params, init) {
	  	this.setLoading(); 	
	  	this.setCommonParams(params);
	  	params.enableBack = this.breadcrumbs.length > 0;
	  	var outer = this;
	  	jQuery.post("html/tag-palette-tree.htm", params, function(html) {
	  		outer.updatePalette(html, init);
	  	});
	  },
	  
	  updatePaletteFlat: function(params, init) {
	  	this.flatIndex = 0;
	  	this.setLoading(); 	
	  	this.setCommonParams(params);
	  	var outer = this;
	  	jQuery.post("html/tag-palette-flat.htm", params, function(html) {
	  		outer.updatePalette(html, init);
	      outer.arrangeFlat();
	  	});
	  },
	  
	  updatePaletteCloud: function(init) {
	  	this.setLoading();
	  	var params = {};
	  	this.setCommonParams(params);
	  	var outer = this;
	  	jQuery.post("html/tag-palette-cloud.htm", params, function(html) {
	  		outer.updatePalette(html, init);
	  	});
	  },
	  
	  arrangeFlat: function() {
	  	liquidBlocks(this.element, 80, this.element.width() - 30);
	  },
	  
	  setCommonParams: function(params) {
	  	params.jsPaletteRef = this.ref;
	  	params.enableClose = this.toggleButton != null;
	  	if (this.sessionName != null) params.sessionName = this.sessionName;
	  },
	  
	  updatePalette: function(html, init) {
	  	this.element.html(html);
			if (this.autoHeight) 
				this.element.css("max-height", this.decideMaxHeight());
			if (init && this.onPaletteInit)
				this.onPaletteInit();
	    if (this.onPaletteUpdate) 
	    	this.onPaletteUpdate();
	  },
	  
	  decideMaxHeight: function() {
	  	var scrollTop = jQuery(document).scrollTop();
	  	var offset = cumulativeOffsetTop(this.element[0]) - scrollTop;
	  	return jQuery(window).height() - offset - 20;
	  },
	  
	  setLoading: function() {
	    // if (navigator.userAgent.indexOf("AppleWebKit") != -1) return;
	  	this.element.empty().putLoadingIcon("margin: 5px");
	  },
	  
	  showMore: function (button) {
	  	button = jQuery(button);
	  	button.hide();
	  	var loadIcon = button.closest("td").putLoadingIcon("margin: 2px;");
	  	
	  	var params = {pi: ++this.flatIndex};
	  	this.setCommonParams(params);
	  	var outer = this;
	  	jQuery.post("html/tag-palette-flat.htm", params, function(html) {
	  		var page = jQuery(html);
	  		outer.element.find("ul.liquid-blocks").append(page);
	  		loadIcon.remove();
	  		outer.arrangeFlat();
	  		if (!page.filter("li:first").hasClass("last-page")) button.show();
	  		if (outer.onPaletteUpdate) outer.onPaletteUpdate();
	  	});
	  }		
		
	}, module.Widget.prototype);
	
})(piggydb.widget);	
