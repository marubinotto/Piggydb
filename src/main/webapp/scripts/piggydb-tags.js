/*
 * TagUtils
 */
var TagUtils = {
  htmlTagIcon: function(tagName) {
    return '<span class="' + domain.tagIconClass(tagName) + '">&nbsp;<\/span>';
  },
  
  htmlTag: function(tag) {
    var html = [];
    html.push(TagUtils.htmlTagIcon(tag.name));
    html.push(' <a class="tag" href="tag.htm?id=' + tag.id + '">');
    html.push(tag.name + '<\/a>');
    return html.join("");
  }
};


/*
 * TagView (base class)
 */
function TagView(widget, tagData) {
  this.widget = widget;
  this.tagData = tagData;
}
TagView.prototype = {
  loading: function() {
    this.widget.html('<img src="images/load.gif" border="0" style="margin:5px"/>');
  }
};


/*
 * TagCloud
 */
function TagCloud(widget, tagData) {
  TagView.call(this, widget, tagData);
}
TagCloud.prototype = jQuery.extend({
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
    html.push(TagUtils.htmlTag(tag));
    html.push("</span> <span> </span>");
    containerDiv.append(html.join(""));
  }
}, TagView.prototype);


/*
 * TagTree
 */
function TagTree(widget, tagData) {
  TagView.call(this, widget, tagData);
}
TagTree.prototype = jQuery.extend({
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
      var node = new TagTreeNode(tag, index >= (tags.length - 1));
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
}, TagView.prototype);


/*
 * TagTreeNode
 */
function TagTreeNode(tag, isLast) {
  this.tag = tag;
  this.isLast = isLast;
}
TagTreeNode.prototype = {
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
    html.push(TagUtils.htmlTag(this.tag));
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


/*
 * TagFlat
 */
function TagFlat(widget, tagData, messages) {
  TagView.call(this, widget, tagData);
  this.pageIndex = 0;
  this.messages = messages;
}
TagFlat.prototype = jQuery.extend({
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
    html.push(TagUtils.htmlTag(tag));
    html.push("</span> <span> </span>");
    containerDiv.append(jQuery(html.join("")));
  },
  
  htmlPageLink: function(pageIndex, pageCount) {
    var html = ['<table class="page-link" width="100%" border="0" cellpadding="0" cellspacing="0">'];
    html.push('<tr>');
    html.push('<td class="previous" width="30%" align="left" valign="middle">');
    if (pageIndex > 0)
      html.push('<a href="#">&lt; ' + this.messages["previous"] + '</a>');
    html.push('</td>');
    html.push('<td class="page-number" align="center" valign="middle">');
    html.push((pageIndex + 1) + " / " + pageCount);
    html.push('</td>');
    html.push('<td class="next" width="30%" align="right" valign="middle">');
    if (pageIndex < (pageCount - 1))
      html.push('<a href="#">' + this.messages["next"] + ' &gt;</a>');
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
}, TagView.prototype);

