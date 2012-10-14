package marubinotto.piggydb.ui.page.common;

import marubinotto.util.Assert;

import org.apache.commons.lang.text.StrBuilder;

public class PageImports {

	private HtmlFragments html;
	
	private static String DEFAULT_CSS_IMPORTS;
	public static StrBuilder additionalCssImports = new StrBuilder(); 
	private StrBuilder css = new StrBuilder();
	
	private static String DEFAULT_HEAD_JS_IMPORTS;
	public StrBuilder headJs = new StrBuilder();
	public StrBuilder bottomJs = new StrBuilder();
	
	public PageImports(HtmlFragments html) {
		Assert.Arg.notNull(html, "html");
		
		this.html = html;
		setDefaultCss();
		setDefaultHeadJs();
		setDefaultBottomJs();
	}
	
	public String getCss() {
		return this.css.toString() + additionalCssImports;
	}
	
	private void setDefaultCss() {
		if (DEFAULT_CSS_IMPORTS == null) {
			StrBuilder imports = new StrBuilder();
			addCssImportTo(imports, "js/vendor/prettify/prettify.css", true, null);
			addCssImportTo(imports, "js/vendor/updnWatermark/watermark.css", true, null);
			addCssImportTo(imports, "js/vendor/jquery-ui-1.8.18/themes/base/jquery.ui.all.css", false, "screen");
			addCssImportTo(imports, "js/vendor/autocomplete/jquery.autocomplete-1.1-1.css", false, "screen");
			addCssImportTo(imports, "js/vendor/markitup-1.1.12/skins/simple/style.css", false, "screen");
			addCssImportTo(imports, "js/vendor/markitup-1.1.12/sets/wiki/style.css", false, "screen");
			addCssImportTo(imports, "js/vendor/qtip2/jquery.qtip.css", true, null);
			addCssImportTo(imports, "style/curve/curve.css", true, "screen");
			addCssImportTo(imports, "style/tree/tree.css", true, null);
			addCssImportTo(imports, "style/facebox/facebox.css", true, null);
			addCssImportTo(imports, "style/piggydb-base.css", true, "screen");
			addCssImportTo(imports, "style/piggydb-shared.css", true, "screen");
			addCssImportTo(imports, "style/piggydb-wiki-help.css", true, null);
			addCssImportTo(imports, "style/piggydb-print.css", true, "print");
			addCssImportTo(imports, "style/piggydb-fragments.css", true, null);	
			DEFAULT_CSS_IMPORTS = imports.toString();
		}
		this.css.append(DEFAULT_CSS_IMPORTS);
	}
	
	public static final String JQUERY_PATH = "js/vendor/jquery-1.7.2.min.js";
	
	private void setDefaultHeadJs() {
		if (DEFAULT_HEAD_JS_IMPORTS == null) {
			StrBuilder imports = new StrBuilder();
			addJsImportTo(imports, JQUERY_PATH, false);
			addJsImportTo(imports, "js/vendor/jquery-ui-1.8.18/jquery-ui-1.8.18.custom.min.js", false);
			addJsImportTo(imports, "js/vendor/jquery.blockUI.js", false);
			addJsImportTo(imports, "js/vendor/purePacked.js", false);
			addJsImportTo(imports, "js/vendor/prettify/prettify.js", true);
			addJsImportTo(imports, "js/vendor/updnWatermark/jquery.updnWatermark.js", true);
			addJsImportTo(imports, "js/vendor/autocomplete/jquery.bgiframe.min.js", true);
			addJsImportTo(imports, "js/vendor/autocomplete/jquery.ajaxQueue.js", false);
			addJsImportTo(imports, "js/vendor/autocomplete/jquery.autocomplete-1.1-modified.js", true);
			addJsImportTo(imports, "js/vendor/markitup-1.1.12/jquery.markitup.js", false);
			addJsImportTo(imports, "js/vendor/qtip2/jquery.qtip.min.js", true);
			addJsImportTo(imports, "js/piggydb.js", true);
			addJsImportTo(imports, "js/piggydb.util.js", true);
			addJsImportTo(imports, "js/piggydb.server.js", true);
			addJsImportTo(imports, "js/piggydb-jquery.js", true);	
			DEFAULT_HEAD_JS_IMPORTS = imports.toString();
		}
		this.headJs.append(DEFAULT_HEAD_JS_IMPORTS);
	}
	
	private void setDefaultBottomJs() {
		importBottomJs("js/piggydb.command.js", true);
		importBottomJs("js/piggydb.widget.js", true);
		importBottomJs("js/piggydb.widget.SmartLayout.js", true);
		importBottomJs("js/piggydb.widget.SelectedFragments.js", true);
		importBottomJs("js/piggydb.widget.TagPalette.js", true);
		importBottomJs("js/piggydb.widget.Fragment.js", true);
		importBottomJs("js/piggydb.widget.FragmentFormBase.js", true);
		importBottomJs("js/piggydb.widget.FragmentForm.js", true);
		importBottomJs("js/piggydb.widget.FileForm.js", true);
	}
	
	private void addCssImportTo(StrBuilder imports, String filePath, boolean versioning, String media) {
		imports.appendln(this.html.cssImport(filePath, versioning, media));
	}
	
	private void addJsImportTo(StrBuilder imports, String filePath, boolean versioning) {
		imports.appendln(this.html.jsImport(filePath, versioning));
	}
	
	public void importCss(String filePath, boolean versioning, String media) {
		addCssImportTo(this.css, filePath, versioning, media);
	}
	
	public void importBottomJs(String filePath, boolean versioning) {
		addJsImportTo(this.bottomJs, filePath, versioning);
	}
}
