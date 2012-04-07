piggydb.namespace("piggydb.server", {

  contextPath: "$context/",
	
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
	}
});



piggydb.server.messages = {
#foreach ($key in $messages.keySet())
  "$key": "$utils.escapeJs($messages.get($key))", 
#end
	dummy: null
};
