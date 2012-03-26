//
// Piggydb Utilities
//

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


