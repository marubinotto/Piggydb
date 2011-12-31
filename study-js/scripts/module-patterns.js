var module1 = (function() {

	var counter = 0;

	return {
		add: function() {
			counter++;
		},

		getCount: function() {
			return counter;
		}
	};	
})();	

