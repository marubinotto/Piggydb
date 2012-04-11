jQuery(function() {
	piggydb.widget.Fragment.init();
});


(function(module) {
	
	var _class = function(node) {
		this.node = jQuery(node);
		this.root = this.node.closest("table.fragment");
	};
	
	_class.init = function() {
		jQuery("table.fragment").live('mouseenter', function() {
	    jQuery(this).find(".fragment-tools").eq(0).show();
	  });
	  jQuery("table.fragment").live('mouseleave', function() {
	    jQuery(this).find(".fragment-tools").eq(0).hide();
	  });
	  jQuery("a.img-link").live("click", _class.onImageClick);
	  makeFragmentsDroppable("table.fragment", null);
	  makeSelectedFragmentsDroppable();
	  makeRelationsDraggable("");
	};
	
	_class.findInTheSameFragmentNode = function(node, selector) {
		return jQuery(node).closest("table.fragment-node").find(selector);
	};
	
	_class.highlight = function(id, baseNode) {
	  var selector = ".fragment-header-" + id;
	  var color = "#ff9900";
	  if (baseNode == null)
	  	jQuery(selector).fadingHighlight(color);
	  else
	  	jQuery(baseNode).find(selector).fadingHighlight(color);
	};
	
	_class.onShowHiddenTags = function(button) {
	  jQuery(button).siblings(".hidden-tags").show();
	  jQuery(button).hide();
	};
	
	_class.imageViewer = new piggydb.widget.Facebox("facebox-image-viewer");
	
	_class.onImageClick = function() {
		_class.imageViewer.showImage(this.href);
	  return false;
	};
	
	_class.syncTitles = function(id, title, headline) {
		var selector = ".fragment-header-" + id + " span.title";
		jQuery(selector).html(headline);
		jQuery("table.fragment-full > tbody > tr > th.header-cell " + selector).html(title);
	};
	
	_class.getHeaders = function(fragmentId) {
	  var headerClass = ".fragment-header";
	  if (fragmentId != null) headerClass = headerClass + "-" + fragmentId;
	  return jQuery(headerClass);
	};
	
	_class.prototype = jQuery.extend({
		
		id: function() {
			return this.root.find("span.fragment-id:first").text();
		},
		
		header: function() {
			return this.root.find("div.fragment-header:first");
		},
		
		mainTitleSpan: function() {
			return this.header().find("span.title");
		},
		
		shortTitleSpan: function() {
			return this.header().find(".fragment-tools span.fragment-title");
		},
		
		headerRow: function() {
			return this.header().closest("tr");
		},
		
		bodyRow: function() {
			return this.headerRow().siblings("tr.fragment-body");
		},
		
		setBodyRow: function(rowHtml) {
			this.bodyRow().remove();
			this.headerRow().after(rowHtml);
		},
		
		textContentDiv: function() {
			return this.bodyRow().find("div.fragment-content-text");
		},
		
		isFull: function() {
			return this.root.hasClass("fragment-full");
		},
		
		isMultirow: function() {
			return this.root.hasClass("multirow");
		},
		
		isMain: function() {
			return this.root.hasClass("fragment-main");
		},
		
		isEditable: function() {
			return this.header().find("a.edit-fragment").size() > 0;
		},
		
		contentToggle: function() {
			var toggle = this.header().find(".fragment-content-toggle a.tool-button");
			return toggle.size() == 0 ? null : new piggydb.widget.ContentToggle(toggle);
		},
		
		highlight: function() {
			_class.highlight(this.id(), this.root);
		},
		
		fullEditor: function() {
			var editor = this.root.siblings(".fragment-form-panel");
			return editor.size() > 0 ? editor : null;
		}		
	}, module.Widget.prototype);
	
	module.Fragment = _class;
	
})(piggydb.widget);	
