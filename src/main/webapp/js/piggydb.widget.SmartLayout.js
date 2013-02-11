(function(module) {
	
	var _MIN_HORIZONTAL_WIDTH = 950;
	var _DEFAULT_MAIN_PANE_WIDTH = 500;
	
	var _container = jQuery("div.sl-container");
	var _pane = jQuery("div.sl-pane");
	
	var _object = {}
	
	_object.vertical = true;
	
	_object.mainPaneWidth = null;
	
	_object.getMainPaneWidth = function() {
		if (_object.mainPaneWidth) {
			return _object.mainPaneWidth;
		}
		else {
			return _DEFAULT_MAIN_PANE_WIDTH;
		}
	};
	
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
			_container.toggleClass("sl-container-horizontal", false);
		}
	};
	
	_object.setHorizontalLayout = function() {
		if (_object.vertical) {
			_object.vertical = false
			_container.toggleClass("sl-container-horizontal", true);
		}
	};
	
	module.SmartLayout = _object;
	
})(piggydb.widget);
