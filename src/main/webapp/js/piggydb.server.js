piggydb.namespace("piggydb.server", {

  contextPath: "$context/",
	
  ajaxCommand: function(command, parameters) {
    jQuery.get("$context/command/" + command + ".htm", parameters);
  },
	
  putSessionValue: function(name, value) {
    jQuery.post(
      "$context/command/put-session-value.htm", 
      {name: name, value: value});
  }
});



piggydb.server.messages = {
#foreach ($key in $messages.keySet())
  "$key": "$utils.escapeJs($messages.get($key))", 
#end
	dummy: null
};
