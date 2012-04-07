var FragmentFormDialog = {
	openToCreate: function() {
		jQuery("#dialog-fragment-form").remove();
		jQuery.get("html/fragment-editor.htm", function(html) {
			jQuery("body").append(html);
			var dialog = jQuery("#dialog-fragment-form");
			FragmentFormDialog.init(dialog);
			dialog.dialog({
		    modal: false,
		    width: 600,
		    height: 400
		  });
			dialog.find("textarea.fragment-content").get(0).focus();
		});
	},
	
	init: function(dialog) {
		jQuery.updnWatermark.attachAll();
		dialog.find("input.fragment-as-tag").button({
      icons: {
      	primary: "ui-icon-piggydb-tag"
      },
      text: false
	  });
		dialog.find("textarea.fragment-content").markItUp(FragmentForm.markItUpSettings);
		dialog.find(".markItUp .markItUpButton9 a").attr("href", constants["wiki-help-href"])
	  	.click(FragmentFormDialog.onWikiHelpClick);
	},
  
  // wikiHelp: new piggydb.util.Facebox("facebox-wiki-help"),
  
  onWikiHelpClick: function() {
  	FragmentFormDialog.wikiHelp.show(this.href);
    return false;
  }
};

