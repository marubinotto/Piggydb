(function(module) {
	
	var _MIN_HORIZONTAL_WIDTH = 950;
	
	var _pageContent = jQuery("#page-content");
	var _container = jQuery("div.sl-container");
	var _pane = jQuery("div.sl-pane");
	
	var _object = {}
	
	_object.vertical = true;
	
	_object.init = function() {
		_object.updateLayout();
    jQuery(window).resize(function() {
    	_object.updateLayout();
		});
	};
	
	_object.updateLayout = function() {
		if (_container.width() < _MIN_HORIZONTAL_WIDTH) {
			_object.setVerticalLayout();
		}
		else {
			_object.setHorizontalLayout();
		}
	};
	
	_object.setVerticalLayout = function() {
		if (!_object.vertical) {
			_object.vertical = true;
			_pageContent.toggleClass("sl-horizontal", false);
			jQuery("body").css("overflow", "auto");
			_pane.css("width", "auto");
		}
	};
	
	_object.setHorizontalLayout = function() {
		if (_object.vertical) {
			// to make sure that the layout switching will be done with the scrollTop zero
			jQuery('html,body').animate({ scrollTop: 0 }, 0, 'linear', function() {
				_object.vertical = false
				_pageContent.toggleClass("sl-horizontal", true);
				jQuery("body").css("overflow", "hidden");
			});
		}
		_pane.css("width", (_container.width() / 2) - 25);
	};
	
	module.SmartLayout = _object;
	
})(piggydb.widget);
