// 
// A modified version of Namespace.js - https://github.com/smith/namespacedotjs
//

var piggydb = {};

piggydb.namespace = (function() {

	var _separator = '.';

	var _namespace = function(identifier) {
		var module = arguments[1] || false;
		var ns = window;
		
		if (identifier !== '') {
			var parts = identifier.split(_separator);
			for (var i = 0; i < parts.length; i++) {
				if (!ns[parts[i]]) {
					ns[parts[i]] = {};
				}
				ns = ns[parts[i]];
			}
		}
		
		if (module) {
			for (var propertyName in module) {
				if (module.hasOwnProperty(propertyName)) {
					ns[propertyName] = module[propertyName];
				}
			}
		}
		
		return ns;
	};

	return _namespace;
})();


//
// Examples
//

piggydb.namespace("net.piggydb.util", {
	log: function(message) {
		jQuery("#console").append(message + "<br/>");
	}
});

