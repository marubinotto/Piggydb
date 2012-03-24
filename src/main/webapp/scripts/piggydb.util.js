piggydb.namespace("piggydb.util", {
	
	escapeHtml: function(str) {
		if (!str) return str;
	  return str.replace(/&/g, "&amp;").replace(/"/g, "&quot;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
	},
	
	isBlank: function(str) {
	  if (str == null) return true;
	  if (jQuery.trim(str) == "") return true;
	  return false;
	},
	
	isNotBlank: function(str) {
	  return !isBlank(str);
	}
});
