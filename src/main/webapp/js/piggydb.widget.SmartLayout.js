jQuery(function() {
	piggydb.widget.SmartLayout.init();
});

(function(module) {
	
	var _container = jQuery("#page-fragments");
	var _mainPane = jQuery("#page-fragments-main");
	var _mainPaneResizable = jQuery("#page-fragments-main-resizable");
	var _subPane = jQuery("#page-fragments-sub");
	
	var _object = {}
	
	_object.vertical = true;
	
	_object.init = function() {
		_object.updateLayout();
    jQuery(window).resize(function() {
    	_object.updateLayout();
		});
	};
	
	_object.updateLayout = function() {
		if (_container.width() < 900) {
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
			"padding-right": 0
		});
		_subPane.css("padding-left", 0);
	};
	
	_object.setHorizontalLayout = function() {
		if (_object.vertical) _object.vertical = false; else return;
		
		_container.css({
			"display": "table",
			"table-layout": "fixed"
		});
		_container.children("div").css("display", "table-cell");
		_mainPane.css("border-right", "2px dotted #ccc");
		_mainPaneResizable
			.css("padding-right", 8)
			.resizable({
				handles: "e",
				resize: function(event, ui) {
					var width = ui.element.width();
					_mainPane.width(width + 8);
				}
    	});
		_subPane.css("padding-left", 8);
			
		_mainPane.width(450);
	};
	
	module.SmartLayout = _object;
	
})(piggydb.widget);
