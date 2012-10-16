(function(module) {
	
	var _MIN_HORIZONTAL_WIDTH = 950;
	var _DEFAULT_MAIN_PANE_WIDTH = 500;
	var _KEY_MAIN_PANE_WIDTH = "state.main-pane-width";
	
	var _container = jQuery("#page-fragments");
	var _mainPane = jQuery("#page-fragments-main");
	var _mainPaneResizable = jQuery("#page-fragments-main-resizable");
	var _subPane = jQuery("#page-fragments-sub");
	
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
		if (_object.vertical) return; else _object.vertical = true;
		
		_container.css("display", "block");
		_container.children("div").css("display", "block");
		_mainPane.css({
			"border-right-style": "none",
			"width": "auto"
		});
		_mainPaneResizable.resizable("destroy");
		_mainPaneResizable.css({
			"width": "auto",
			"height": "auto",
			"padding-right": 0
		});
		_subPane.css("padding-left", 0);
	};
	
	_object.setHorizontalLayout = function() {
		if (_object.vertical) _object.vertical = false; else return;
		
		var paddingToSplitter = 8;
		
		_container.css({
			"display": "table",
			"table-layout": "fixed",
			"height": "100%"
		});
		_container.children("div").css("display", "table-cell");
		_mainPane.css({
			"height": "100%",
			"border-right": "2px dotted #ccc"
		});
		_mainPaneResizable
			.css({
				"height": "100%",
				"padding-right": paddingToSplitter
			})
			.resizable({
				handles: "e",
				containment: _container,
				minWidth: 150,
				resize: function(event, ui) {
					var width = ui.element.width();
					_mainPane.width(width + paddingToSplitter);
				},
				stop: function(event, ui) {
					piggydb.server.putSessionValue(_KEY_MAIN_PANE_WIDTH, _mainPane.width());
				}
    	});
		_subPane.css("padding-left", paddingToSplitter);
			
		_mainPane.width(_object.getMainPaneWidth());
	};
	
	module.SmartLayout = _object;
	
})(piggydb.widget);
