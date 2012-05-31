(function(module) {

	var _MIN_SCALE = 0;
	var _MAX_SCALE = 1000;

	var _class = function(id, fragmentsUrl, scale, orderBy, ascending) {
		this.id = id;
		this.rootDiv = jQuery("#" + id);
		this.fragmentsUrl = fragmentsUrl;
		this.scale = scale;
		this.orderBy = orderBy;
		this.ascending = ascending;
		this.highlight = null;
		this.highlighted = false;
		this.contentDiv = this.rootDiv.find("div.view-content");
		this.viewControl = this.rootDiv.find(".view-control");
		this.pageIndex = 0;
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
	    
	    this.loadFirstSet();
	  },

	  createParameters: function () {
	    return {
	      "viewId": this.id, 
	      "scale": this.scale, 
	      "orderBy": this.orderBy,
	      "ascending": this.ascending
	    };
	  },
	  
	  loadFirstSet: function () {
	  	this.pageIndex = 0;
	    this.contentDiv.empty();
	    var loadIcon = this.contentDiv.putLoadingIcon("margin: 5px;");
	    var outer = this;
	    jQuery.get(this.fragmentsUrl, this.createParameters(), function(html) {
	      if (jQuery.trim(html) != "") {
	      	outer.contentDiv.append(html);
	      	outer.viewControl.show();
	        prettyPrint();
	        if (outer.highlight != null && !outer.highlighted) {
	        	piggydb.widget.Fragment.highlight(outer.highlight, outer.contentDiv);
	          // highlighing should be done only once 
	        	// for example, when a user changes the scale in the same page,
	          // highlighing should not be enabled.
	          outer.highlighted = true;
	        }
	      }
	      loadIcon.remove();
	    });
	  },
	  
	  refresh: function (highlight) {
	  	if (highlight != null) {
	  		this.highlight = highlight;
	  		this.highlighted = false;
	  	}
	  	this.loadFirstSet();
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
