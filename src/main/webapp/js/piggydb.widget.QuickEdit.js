jQuery(function() {
	piggydb.widget.QuickEdit.init();
});


(function(module) {
	
	var _editorSelector = "div.fragment-editor-quick";
	
	var _openEditor = function(id, contentDiv) {
		var contentDivHeight = contentDiv.height();
	  var editorDiv = contentDiv.siblings(_editorSelector);
	  contentDiv.empty().putLoadingIcon(); 
	  jQuery.get("html/fragment-editor-quick.htm", {"id" : id}, function(html) {
	  	contentDiv.empty();
	  	editorDiv.html(html);
	  	
	  	jQuery.updnWatermark.attachAll();
		
	  	var editor = editorDiv.find("textarea.fragment-content");
	  	piggydb.widget.FragmentForm.addToolBar(editor, true);
	  	piggydb.widget.FragmentForm.linkToWikiHelp(editorDiv.find(".markItUp .markItUpButton9 a"));
		
	  	var height = Math.max(contentDivHeight, editor.height());
	  	editor.height(Math.min(height, 500));
	  });
	};
	
	var _emptyContent = function(contentDiv) {
		piggydb.widget.Fragment.findInTheSameFragmentNode(
			contentDiv, "span.fragment-content-toggle:first").remove();
  	contentDiv.closest("tr.fragment-body").remove();
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
			jQuery.get("html/fragment-body-row.htm", {"id": fragment.id()}, function(html) {
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
			jQuery.post("html/update-fragment.htm", params, function(html) {
				html = jQuery(html);
				
				// error
				var error = html.find("div.res-error");
				if (error.size() > 0) {
					loadingIcon.remove();
					editorDiv.find("div.error").remove();
					editorDiv.prepend(error.html());
					editorDiv.show();
					return;
				}
				
				// prepare to show the new content
				editorDiv.empty().show();
				
				// new title
				piggydb.widget.Fragment.syncTitles(
					fragmentId, 
					html.find("div.res-title span.title").html(),
					html.find("div.res-title span.headline").html());
		  	
		  	fragment.shortTitleSpan().html(
		  		html.find("div.res-title span.title-short").html());
		  	
		  	// new content
		  	var newContent = html.find("div.res-content").html();
		  	if (isNotBlank(newContent)) {
		  		contentDiv.html(newContent);
			  	prettyPrint();
			  }
			  else {
			  	_emptyContent(contentDiv);
			  }
		  	
		  	// update info
		  	fragment.header().find("span.update-info").html(
		  		html.find("div.res-update-info span.update-info").html());
		  	
		  	piggydb.widget.Fragment.highlight(fragmentId, null);
			});
		}
	};
	
})(piggydb.widget);
