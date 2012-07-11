jQuery(function() {
	piggydb.widget.QuickEdit.init();
});


(function(module) {
	
	var _editorSelector = "div.fragment-editor-quick";
	
	var _openEditor = function(id, contentDiv) {
		var contentDivHeight = contentDiv.height();
	  var editorDiv = contentDiv.siblings(_editorSelector);
	  contentDiv.empty().putLoadingIcon(); 
	  jQuery.get("partial/fragment-editor-quick.htm", {"id" : id}, function(html) {
	  	contentDiv.empty();
	  	editorDiv.html(html);
	  	
	  	jQuery.updnWatermark.attachAll();
		
	  	var editor = editorDiv.find("textarea.fragment-content");
	  	piggydb.widget.FragmentForm.addToolBar(editor, true);
		
	  	var height = Math.max(contentDivHeight, editor.height());
	  	editor.height(Math.min(height, 500));
	  });
	};
	
	module.QuickEdit = {
			
		init: function() {
		  jQuery("div.fragment-content-text").live('dblclick', function() {
			  var contentDiv = jQuery(this);
			  var fragment = new piggydb.widget.Fragment(contentDiv);
			  if (fragment.isEditable()) {
			  	_openEditor(fragment.id(), contentDiv);
			  }
			});
		},
		
		onEditButtonClick: function(button) {
			var fragment = new piggydb.widget.Fragment(button);
			
			// if there's a full-fledged editor, open it
			var fullEditor = fragment.fullEditor();
			if (fullEditor != null) {
				fullEditor.find("a.toggle-link").click();
				return true;
			}
			
			// content opened
			var contentDiv = fragment.textContentDiv();	
			if (contentDiv.size() == 1) {
				_openEditor(fragment.id(), contentDiv);
				return true;
			}
			
			// content hidden or empty on a multirow fragment table
			if (fragment.isMultirow()) {
				var contentToggle = fragment.contentToggle();
				if (contentToggle != null) contentToggle.setOpened();
				
				var emptyBodyRow = jQuery.trim(
					jQuery("#tpl-fragment-body-row-with-empty-text tbody").html());
				fragment.setBodyRow(emptyBodyRow);
				_openEditor(fragment.id(), fragment.textContentDiv());
				return true;
			}	
			return false;
		},

		onCancel: function(button) {
			var fragment = new piggydb.widget.Fragment(button);
			var editorDiv = jQuery(button).closest(_editorSelector);	
			var contentDiv = editorDiv.siblings("div.fragment-content-text");
			
			editorDiv.empty();
			contentDiv.empty().putLoadingIcon();
			jQuery.get("partial/fragment-body-row.htm", {"id": fragment.id()}, function(html) {
				if (isNotBlank(html)) {
					var content = jQuery(html).find("div.fragment-content").html();
					contentDiv.html(content);
					prettyPrint();
				}
				else {
					_emptyContent(contentDiv);
				}
			});
		},

		onUpdate: function(button) {
			var fragment = new piggydb.widget.Fragment(button);
			
			var fragmentId = fragment.id();
			var editorDiv = jQuery(button).closest(_editorSelector);
			var contentDiv = editorDiv.siblings("div.fragment-content-text");
			
			var params = {
				id: fragmentId,
				title: editorDiv.find("input.fragment-title").val(),
				content: editorDiv.find("textarea.fragment-content").val()};
			if (editorDiv.find("input.fragment-minorEdit").get(0).checked) {
				params.minorEdit = "on";
			}
			
			editorDiv.hide();
			var loadingIcon = contentDiv.empty().putLoadingIcon();
			jQuery.post("partial/quick-update-fragment.htm", params, function(html) {
				// error
				var error = jQuery(html).children("span.error");
				if (error.size() > 0) {
					loadingIcon.remove();
					editorDiv.find("div.error").remove();
					editorDiv.prepend(jQuery('<div class="error">').append(error.text()));	
					editorDiv.show();
					return;
				}
				
				// prepare to show the new content
				editorDiv.empty().show();
				
				// update the fragment properties
				jQuery(html).children("div.fragment-properties").each(function() {
					fragment.update(jQuery(this));
				});
		  	
		  	piggydb.widget.Fragment.highlight(fragmentId, null);
			});
		}
	};
	
})(piggydb.widget);
