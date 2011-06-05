//
// Utilities
//

function escapeHtml(str) {
  if (!str) return str;
  return str.replace(/&/g, "&amp;").replace(/"/g, "&quot;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

function escapeJsString(str) {
  if (!str) return str;
  return str.replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

function isBlank(str) {
  if (str == null) return true;
  if (jQuery.trim(str) == "") return true;
  return false;
}

function isNotBlank(str) {
  return !isBlank(str);
}

var FormUtils = {
  addParamToForm: function(form, name, value) {
    jQuery('<input type="hidden" name="' + name + '" value="' + value + '"/>')
      .appendTo(form);
  }
};

// The tag name should be html escaped before
function tagIconClass(tagName) {
	var c = "tagIcon";
	if (tagName.charAt(0) == "#") c = c + " tagIcon-" + tagName.substring(1);
	return c;
}

function miniTagIconClass(tagName) {
	var c = "miniTagIcon";
	if (tagName.charAt(0) == "#") c = c + " miniTagIcon-" + tagName.substring(1);
	return c;
}



//
// Tree
//

function toggleTreeNode(node) {
  var node = jQuery(node).closest("li");
  var className = node.attr("class");
  node.removeAttr("class");
  if (className.match("^collapsed")) {
    node.addClass(className.replace("collapsed", "expanded"));
    node.children("ul").show();
  }
  else if (className.match("^expanded")) {
    node.addClass(className.replace("expanded", "collapsed"));
    node.children("ul").hide();
  }
}



//
// Others
//

function setScrollTopTo(id) {
  var targets = jQuery('#' + id);
  if (targets.size() == 0) return;
  
  var element = targets[0];
  var offset = 0;
  while (true) {
    offset += element.offsetTop;
    element = element.offsetParent;
    if (!element) break;
  }
  
  jQuery("html, body").scrollTop(offset);
}

jQuery.Autocompleter.defaults.formatItem = function(row) { 
  return escapeHtml(row[0]); 
};


