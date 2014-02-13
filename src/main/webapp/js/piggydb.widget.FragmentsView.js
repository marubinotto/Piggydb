(function(module) {

	var _MIN_SCALE = 0;
	var _MAX_SCALE = 1000;
	
	var _instances = {};

	var _class = function(id, fragmentsUrl, scale, orderBy, ascending) {
		_instances[id] = this;
		
		this.id = id;
		this.rootDiv = jQuery("#" + id);
		this.fragmentsUrl = fragmentsUrl;
		this.scale = scale;
		this.orderBy = orderBy;
		this.ascending = ascending;
		this.highlight = null;
		this.highlighted = false;
		this.headerDiv = this.rootDiv.find("div.view-header");
		this.contentDiv = this.rootDiv.find("div.view-content");
		this.pageIndex = 0;
		this.queryable = false;
		this.query = null;
	};
	
	_class.refreshViews = function(highlightId) {
		jQuery.each(_instances, function(id) {
			this.refresh(highlightId);
		});
	};
	
	_class.prototype = jQuery.extend({
		
	  init: function () {
	    var outer = this;
	    this.rootDiv.find(".view-slider").slider({ 
	      max: _MAX_SCALE,
	      min: _MIN_SCALE,
	      value: outer.scale,
	      change: function(event, ui) {
	        outer.scale = ui.value;
	        outer.loadFirstSet();
	      }
	    });
	    this.rootDiv.find(".select-orderBy").change(function () {
	      outer.orderBy = jQuery(this).val();
	      outer.loadFirstSet();
	    });
	    this.rootDiv.find(".select-ascending-or-not button").click(function () {
	    	var button = jQuery(this);
	      if (!clickSelectSwitch(button)) return;     
	      outer.ascending = (button.attr("name") == "ascending");
	      outer.loadFirstSet();
	    });
	    
	    if (this.queryable) {
	    	this.headerDiv.find("span.label span.toggle").show();
		    this.headerDiv.find("span.label")
		    	.css("cursor", "pointer")
		    	.click(function() {
		    		var criteriaDiv = outer.headerDiv.find("div.fragments-criteria");
		    		criteriaDiv.toggle();
		    		
		    		jQuery(this).css("border-bottom-style", criteriaDiv.is(":visible") ? "none" : "solid");
		    		
		    		var icon = jQuery(this).find("span.toggle img");
		    		icon.attr("src", criteriaDiv.is(":visible") ? 
		    			icon.attr("src").replace("down", "up") : 
		    			icon.attr("src").replace("up", "down"))
		    	});
		    this.headerDiv.find("input.keywords")
			    .keyup(function() {
			    	outer.query = jQuery(this).val();
			    	outer.loadFirstSet({lazyDisplay: true});
			    });
		    
		    var tagitConfig = {
          allowSpaces: true,
          autocomplete: {
            delay: 0, 
            minLength: 1,
            autoFocus: true,
            source: "command/complete-tag-name2.htm"
          }
        };
		    this.headerDiv.find("input.tags-include").tagit(tagitConfig);
		    this.headerDiv.find("input.tags-exclude").tagit(tagitConfig);
	    }
	    
	    this.loadFirstSet();
	  },

	  createParameters: function () {
	  	var params = {
	      viewId: this.id, 
	      scale: this.scale, 
	      orderBy: this.orderBy,
	      ascending: this.ascending
	    };
	  	if (this.query) params.query = this.query;
	  	return params;
	  },
	  
	  loadFirstSet: function (options) {
	  	if (!options) options = {};
	  	this.pageIndex = 0;
	  	
	  	var params = this.createParameters();
	  	if (options.shuffle) params.shuffle = true;
	  	
	  	var loadIcon = null;
	  	if (!options.lazyDisplay) {
	  		this.contentDiv.empty();
	    	loadIcon = this.contentDiv.putLoadingIcon("margin: 5px;");
	  	}
	    
	    var outer = this;
	    jQuery.get(this.fragmentsUrl, params, function(html) {
	      if (jQuery.trim(html) != "") {
	      	outer.contentDiv.html(html);
	        prettyPrint();
	        if (outer.highlight != null && !outer.highlighted) {
	        	piggydb.widget.Fragment.highlight(outer.highlight, outer.contentDiv);
	        	piggydb.widget.ContentToggle.clickContentToggle(outer.highlight);
	        	
	          // highlighing should be done only once 
	        	// for example, when a user changes the scale in the same page,
	          // highlighing should not be enabled.
	          outer.highlighted = true;
	        }
	      }
	      if (!options.lazyDisplay) loadIcon.remove();
	    });
	  },
	  
	  refresh: function (highlight) {
	  	if (highlight != null) {
	  		this.highlight = highlight;
	  		this.highlighted = false;
	  	}
	  	this.loadFirstSet();
	  },
	  
	  shuffle: function () {
	  	this.loadFirstSet({shuffle: true});
	  },
	  
		showMore: function (button) {
	    jQuery(button).remove();
	    var fragmentsContainer = this.rootDiv.find(".fragments-container");
	    
	    var loadIcon = jQuery('<div style="text-align: center; margin-bottom: 5px;">')
	      .appendTo(fragmentsContainer);
	    loadIcon.putLoadingIcon(null);
	    
	    var params = this.createParameters();
	    params.pi = ++this.pageIndex;
	    jQuery.get(this.fragmentsUrl, params, function(html) {
	      fragmentsContainer.append(html);
	      loadIcon.remove();
	      prettyPrint();
	    });
	  }		
	}, module.Widget.prototype);
	
	module.FragmentsView = _class;
	
})(piggydb.widget);	
