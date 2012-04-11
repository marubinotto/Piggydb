//
// Initialization
//
jQuery(function() {
  FragmentForm.init();
  
  // auto-complete
  jQuery("input[name=tags]").autocomplete(piggydb.server.autoCompleteUrl, {
    minChars: 1,
    selectFirst: true,
    multiple: true,
    multipleSeparator: ', ',
    scrollHeight: 300
  });
  jQuery("input.single-tag").autocomplete(piggydb.server.autoCompleteUrl, {
    minChars: 1,
    selectFirst: true,
    multiple: false,
    scrollHeight: 300
  });
});


//
// Fragment Form
//
var FragmentForm = {
	init: function() {
		jQuery('.content-type-switch input').click(FragmentForm.onContentTypeSwitch);
		piggydb.widget.FragmentForm.addToolBar(jQuery("textarea.fragment-content"));
		piggydb.widget.FragmentForm.linkToWikiHelp(jQuery(".markItUp .markItUpButton9 a"));
	  jQuery("input[name=preview]").click(function () {
	    this.form.contentFieldHeight.value = jQuery(this.form.content).height();
	  });
	  jQuery(".fragment-form-panel input[name=register]").click(function () {
	    var panel = jQuery(this).closest(".fragment-form-panel");
	    panel.find(".fragment-form-toggle").putLoadingIcon("margin-left: 5px; vertical-align: middle;");
	    panel.find(".toggle-icon").attr("src", "images/twistie-up.gif");
	    panel.find(".fragment-form-div").hide();
	  });
	  
	  jQuery("input.fragment-as-tag").button({
      icons: {
      	primary: "ui-icon-piggydb-tag"
      },
      text: false
	  });
	},
		
  onToggleClick: function(panelName) {
		var formDiv = document.getElementById(panelName + "-div");
		var toggleIcon = document.getElementById(panelName + "-toggle-icon");
		if (formDiv.style.display == "none") {
		  toggleIcon.src = "images/twistie-down.gif";
		  formDiv.style.display = "block";
		} 
		else {
		  toggleIcon.src = "images/twistie-up.gif";
		  formDiv.style.display = "none";
		}
  },
  
  onContentTypeSwitch: function() {
    var formId = jQuery(this.form).attr("id");
    var newValue = this.value;
    var oldValue = this.form.contentType.value;
    if (newValue == oldValue) {
      return;
    }
    jQuery('#' + formId + ' .for-' + oldValue).hide();
    jQuery('#' + formId + ' .for-' + newValue).show();
    this.form.contentType.value = newValue;
  }
};

