// Module Patterns
// http://javascriptweblog.wordpress.com/2010/12/07/namespacing-in-javascript/
// http://www.adequatelygood.com/2010/3/JavaScript-Module-Pattern-In-Depth

//
// Basic
//

// using this to reference sibling properties is a little risky 
// since there is nothing to stop your namespaced functions from being reassigned

var module1 = (function() {
	var _counter = 0;

	return {
		add: function() {
			_counter++;
		},

		getCount: function() {
			return _counter;
		}
	};	
})();	



//
// The namespace is injected via a local variable
//

var module2 = (function() {
	var module = {};
	var _counter = 0;

	module.add = function() {
		_counter++;
	};

	module.getCount = function() {
		return _counter;	
	};

	return module;	
})();	



//
// Dynamic Namespacing (module creator, namespace injection)
//

// 1) Supply a Namespace Argument

var module3 = {};
(function(context) {
	var _counter = 0;

	context.add = function() {
		_counter++;
	};

	context.getCount = function() {
		return _counter;	
	};
})(module3);	


// 2) Use this as a Namespace Proxy with apply

// the namespace is injected via the this keyword 
// (which is static within a given execution context) 
// it cannot be accidentally modified. 

var module4 = {};
(function() {
	var _counter = 0;

	this.add = function() {
		_counter++;
	};

	this.getCount = function() {
		return _counter;
	};
}).apply(module4);	


// 3) Use this as a Namespace Proxy with call

// passing additional arguments to the module creator 

var module5 = {};
var module5Creator = function(initial) {
	var _counter = initial || 0;

	this.add = function() {
		_counter++;
	};

	this.getCount = function() {
		return _counter;
	};
};
module5Creator.call(module5, 10);	



