piggydb.namespace("piggydb.server", {

  contextPath: "$context/",
	
  wikiHelpUrl: "$wikiHelpUrl",
	
  autoCompleteUrl: "$context/command/complete-tag-name.htm",
	
  ajaxCommand: function(command, parameters) {
    jQuery.get("$context/command/" + command + ".htm", parameters);
  },
	
  getJSON: function(command, parameters, callback) {
    jQuery.getJSON("$context/command/" + command + ".htm", parameters, callback);
  },
	
  putSessionValue: function(name, value) {
    jQuery.post(
      "$context/command/put-session-value.htm", 
      {name: name, value: value});
  },
	
	getFragmentUrl: function(id) {
		return "$context/fragment.htm?id=" + id;
	},
	
	getTagUrl: function(id) {
		return "$context/tag.htm?id=" + id;
	}
});



piggydb.server.messages = {
#foreach ($key in $messages.keySet())
  "$key": "$utils.escapeJs($messages.get($key))", 
#end
	dummy: null
};
