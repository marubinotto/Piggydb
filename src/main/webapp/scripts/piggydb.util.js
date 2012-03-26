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
	},
	
	addHiddenValue: function(form, name, value) {
    jQuery('<input type="hidden" name="' + name + '" value="' + value + '"/>').appendTo(form);
	}
});


piggydb.namespace("piggydb.util.domain", {
	
	tagIconClass: function(tagName) {
		var c = "tagIcon";
		if (tagName.charAt(0) == "#") c = c + " tagIcon-" + tagName.substring(1);
		return c;
	},
	
	miniTagIconClass: function(tagName) {
		var c = "miniTagIcon";
		if (tagName.charAt(0) == "#") c = c + " miniTagIcon-" + tagName.substring(1);
		return c;
	}
});