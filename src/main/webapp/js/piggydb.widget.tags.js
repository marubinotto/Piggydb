piggydb.namespace("piggydb.widget.tags", {

});

(function(module) {
	
	var _htmlTagIcon = function(tagName) {
    return '<span class="' + domain.tagIconClass(tagName) + '">&nbsp;<\/span>';
  };
  
  var _htmlTagNameLink = function(tag) {
  	return '<a class="tag" href="tag.htm?id=' + tag.id + '">' + tag.name + '<\/a>';
  };
  
  var _htmlTag = function(tag) {
  	return _htmlTagIcon(tag.name) + " " + _htmlTagNameLink(tag);
  };
  
  var _TagTreeNode = function(tag, isLast) {
    this.tag = tag;
    this.isLast = isLast;
  };
  _TagTreeNode.prototype = {
  		
	  nodeType: function() {
	    if (this.tag.hasChildren)
		    return this.isLast ? "collapsedLastNode" : "collapsed";
	    else
	      return this.isLast ? "leafLastNode" : "leaf";
	  }	,
	  
	  createDom: function() {
	    // HTML
	    var html = ['<li class="' + this.nodeType() + '">'];
	    if (this.tag.hasChildren) {
	      html.push('<a href="#" class="toggle">');
	      html.push('<img src="style/tree/transparent.gif" border="0"/><\/a>&nbsp;');
	    }
	    html.push(_htmlTag(this.tag));
	    html.push('<\/li>');
	    
	    // create DOM
	    var dom = jQuery(html.join(""));
	    var outer = this;
	    dom.find('.toggle').click(function() {
	      outer.onToggleClicked(this);
	      return false;
	    });
	    return dom;
	  },
	  
	  onToggleClicked: function(toggle) {
	    var li = jQuery(toggle.parentNode);
	    var nodeType = li.attr("class");
	    li.removeAttr("class");
	    if (nodeType.match("^collapsed")) {
	      li.addClass(nodeType.replace("collapsed", "expanded"));
	      this.slotOnChildrenRequired(this.tag.id, li);        
	    }
	    else if (nodeType.match("^expanded")) {
	      li.addClass(nodeType.replace("expanded", "collapsed"));
	      li.children("ul").remove();      
	    }
	  },
	  
	  slotOnChildrenRequired: function(id, li) {}
	};

  
	/**
	 * TagView (base class)
	 */
  module.TagView = function(widget, tagData) {
    this.widget = widget;
    this.tagData = tagData;
  };
  module.TagView.prototype = jQuery.extend({
  		
	  loading: function() {
	    this.widget.html('<img src="images/load.gif" border="0" style="margin:5px"/>');
	  }
  }, piggydb.widget.Widget.prototype);
  
  
  /**
   * TagCloud
   */
  module.TagCloud = function(widget, tagData) {
  	module.TagView.call(this, widget, tagData);
  };
  module.TagCloud.prototype = jQuery.extend({
  	
    render: function() {
      this.widget.attr("class", "content-box tag-cloud");
      this.loading();
      var outer = this;
      this.tagData.tagsByPopularity(function(tags) {
        outer.widget.empty();

        if (tags.length == 0) return;
        
        var containerDiv = jQuery('<div class="container">');
        jQuery.each(tags, function(index, tag) {
          outer.addTag(tag, containerDiv);
        });
        outer.widget.append(containerDiv);
      });
    },
    
    addTag: function(tag, containerDiv) {
      var html = ['<span class="tag" style="font-size: ' + tag.fontSize + 'px;">'];
      html.push(_htmlTagNameLink(tag));
      html.push("</span> <span> </span>");
      containerDiv.append(html.join(""));
    }
  }, module.TagView.prototype);

  
  /**
   * TagTree
   */
  module.TagTree = function(widget, tagData) {
  	module.TagView.call(this, widget, tagData);
  };
  module.TagTree.prototype = jQuery.extend({
  	
    render: function() {
      this.widget.attr("class", "content-box collapsable-tree");
      this.loading();
      var outer = this;
      this.tagData.rootTags(function (tags) {
        outer.rootUl = jQuery("<ul>").appendTo(outer.widget.empty());
        outer.addNodes(outer.rootUl, tags);
      });
    },
    
    addNodes: function(ul, tags) {
      var outer = this;
      jQuery.each(tags, function(index, tag) {
        var node = new _TagTreeNode(tag, index >= (tags.length - 1));
        node.slotOnChildrenRequired = function(id, li) { 
          outer.loadChildren(id, li); 
        };
        ul.append(node.createDom());
      });
    },
    
    loadChildren: function(id, li) {
      var loadIcon = jQuery(
        '<span><br/><img src="images/load.gif" border="0" style="margin:5px"/><\/span>')
          .appendTo(li);
      
      var childrenUl = li.children("ul");
      if (childrenUl.length == 0) childrenUl = jQuery("<ul>").appendTo(li);
      
      var outer = this;
      this.tagData.children(id, function(tags) {
        outer.addNodes(childrenUl, tags);
        loadIcon.remove();
      });
    }
  }, module.TagView.prototype);
  

  /**
   * TagFlat
   */
  module.TagFlat = function(widget, tagData) {
  	module.TagView.call(this, widget, tagData);
    this.pageIndex = 0;
  };
  module.TagFlat.prototype = jQuery.extend({
  	
    render: function() {
      this.widget.attr("class", "content-box tag-flat");
      this.loading();
      var outer = this;
      this.tagData.allTags(this.pageIndex, function(response) {
        outer.widget.empty();
        
        var pageIndex = response[0].pageIndex;
        var pageCount = response[0].pageCount;
        if (pageCount > 1) {
          outer.widget.append(outer.htmlPageLink(pageIndex, pageCount));
          outer.widget.append("<br/>");
        }
        
        var containerDiv;
        var currentChar = -1;
        var others = false;
        jQuery.each(response[1], function(index, tag) {
          // Divide by the first char if it's ascii
          var firstChar = tag.name.charAt(0).toLowerCase();
          if ((firstChar != currentChar) && !others) {
            currentChar = firstChar;
            containerDiv = jQuery('<div class="container">').appendTo(outer.widget);
            if (tag.name.charCodeAt(0) > 127) others = true;
          }
          outer.addTag(tag, containerDiv);
        });
        
        if (pageCount > 1) {
          outer.widget.append(outer.htmlPageLink(pageIndex, pageCount));
        }
      });
    },
    
    addTag: function(tag, containerDiv) {
      var html = ['<span class="tag">'];
      html.push(_htmlTag(tag));
      html.push("</span> <span> </span>");
      containerDiv.append(jQuery(html.join("")));
    },
    
    htmlPageLink: function(pageIndex, pageCount) {
      var html = ['<table class="page-link" width="100%" border="0" cellpadding="0" cellspacing="0">'];
      html.push('<tr>');
      html.push('<td class="previous" width="30%" align="left" valign="middle">');
      if (pageIndex > 0)
        html.push('<a href="#">&lt; ' + this.getMessage("previous") + '</a>');
      html.push('</td>');
      html.push('<td class="page-number" align="center" valign="middle">');
      html.push((pageIndex + 1) + " / " + pageCount);
      html.push('</td>');
      html.push('<td class="next" width="30%" align="right" valign="middle">');
      if (pageIndex < (pageCount - 1))
        html.push('<a href="#">' + this.getMessage("next") + ' &gt;</a>');
      html.push('</td>');
      html.push('</tr>');
      html.push('</table>');
      
      var outer = this;
      var dom = jQuery(html.join(""));
      dom.find('.previous a').click(function() {
        outer.pageIndex--;
        outer.render();
      });  
      dom.find('.next a').click(function() {
        outer.pageIndex++;
        outer.render();
      });  
      
      return dom;
    }
  }, module.TagView.prototype);

})(piggydb.widget.tags);
