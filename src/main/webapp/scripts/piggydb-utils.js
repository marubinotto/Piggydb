//
// Piggydb Utilities
//

function clickSelectSwitch(button) {
	button = jQuery(button);
  if (button.hasClass("selected")) return false;
  button.siblings("button.selected").removeClass("selected");
  button.addClass("selected");
  return true;
}


