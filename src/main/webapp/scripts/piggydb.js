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

function onDeleteTagClick(tagName, form) {
  form.tagToDelete.value = tagName;
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

function liquidBlocks(selectorPrefix, blockWidth, containerWidth) {
  var blocksSelector = selectorPrefix + "ul.liquid-blocks";

  // Get the width of row
  if (containerWidth == null) {
    // Reset the container size to a 100% once view port has been adjusted
    jQuery(blocksSelector).css({ 'width' : "100%" });
    containerWidth = jQuery(blocksSelector).width();
  }

  // Find how many blocks can fit per row
  // then round it down to a whole number
  var colNum = Math.floor(containerWidth / blockWidth);
  if (colNum == 0) colNum = 1;

  // Get the width of the row and divide it by the number of blocks it can fit
  // then round it down to a whole number.
  // This value will be the exact width of the re-adjusted block
  var colFixed = Math.floor(containerWidth / colNum);

  // Set exact width of row in pixels instead of using %
  // Prevents cross-browser bugs that appear in certain view port resolutions.
  jQuery(blocksSelector).css({ 'width' : containerWidth });

  // Set exact width of the re-adjusted block
  jQuery(blocksSelector + " li.liquid-block").css({ 'width' : colFixed });
}

function clickSelectSwitch(button) {
	button = jQuery(button);
  if (button.hasClass("selected")) return false;
  button.siblings("button.selected").removeClass("selected");
  button.addClass("selected");
  return true;
}


