package marubinotto.piggydb.ui.page.common;

import marubinotto.util.Assert;

import org.apache.commons.lang.text.StrBuilder;

public class PageImports {

	private HtmlFragments html;
	
	private static String DEFAULT_CSS_IMPORTS;
	public StrBuilder css = new StrBuilder();
	
	private static String DEFAULT_HEAD_JS_IMPORTS;
	public StrBuilder headJs = new StrBuilder();
	public StrBuilder bottomJs = new StrBuilder();
	
	public PageImports(HtmlFragments html) {
		Assert.Arg.notNull(html, "html");
		
		this.html = html;
		setDefaultCss();
		setDefaultHeadJs();
	}
	
	public void importCss(String filePath, boolean versioning, String media) {
		addCssImportTo(this.css, filePath, versioning, media);
	}
	
	public void importBottomJs(String filePath, boolean versioning) {
		addJsImportTo(this.bottomJs, filePath, versioning);
	}
	
	private void addCssImportTo(StrBuilder imports, String filePath, boolean versioning, String media) {
		imports.appendln(this.html.cssImport(filePath, versioning, media));
	}
	
	private void addJsImportTo(StrBuilder imports, String filePath, boolean versioning) {
		imports.appendln(this.html.jsImport(filePath, versioning));
	}
	
	private void setDefaultCss() {
		if (DEFAULT_CSS_IMPORTS == null) {
			StrBuilder imports = new StrBuilder();
			addCssImportTo(imports, "js/vendor/prettify/prettify.css", true, null);
			addCssImportTo(imports, "js/vendor/updnWatermark/watermark.css", true, null);
			addCssImportTo(imports, "js/vendor/jquery-ui-1.8.18/themes/base/jquery.ui.all.css", false, "screen");
			addCssImportTo(imports, "js/vendor/autocomplete/jquery.autocomplete-1.1-1.css", false, "screen");
			addCssImportTo(imports, "style/curve/curve.css", true, "screen");
			addCssImportTo(imports, "style/tree/tree.css", true, null);
			addCssImportTo(imports, "style/facebox/facebox.css", true, null);
			addCssImportTo(imports, "style/piggydb-base.css", true, "screen");
			addCssImportTo(imports, "style/piggydb-shared.css", true, "screen");
			addCssImportTo(imports, "style/piggydb-wiki-help.css", true, null);
			addCssImportTo(imports, "style/piggydb-print.css", true, "print");
			
			DEFAULT_CSS_IMPORTS = imports.toString();
		}
		this.css.append(DEFAULT_CSS_IMPORTS);
	}
	
	private void setDefaultHeadJs() {
		if (DEFAULT_HEAD_JS_IMPORTS == null) {
			StrBuilder imports = new StrBuilder();
			addJsImportTo(imports, "js/vendor/jquery-1.7.2.min.js", false);
			addJsImportTo(imports, "js/vendor/jquery-ui-1.8.18/jquery-ui-1.8.18.custom.min.js", false);
			addJsImportTo(imports, "js/vendor/purePacked.js", false);
			addJsImportTo(imports, "js/vendor/prettify/prettify.js", true);
			addJsImportTo(imports, "js/vendor/updnWatermark/jquery.updnWatermark.js", true);
			addJsImportTo(imports, "js/vendor/autocomplete/jquery.bgiframe.min.js", false);
			addJsImportTo(imports, "js/vendor/autocomplete/jquery.ajaxQueue.js", false);
			addJsImportTo(imports, "js/vendor/autocomplete/jquery.autocomplete-1.1-modified.js", true);
			addJsImportTo(imports, "js/piggydb.js", true);
			addJsImportTo(imports, "js/piggydb.util.js", true);
			addJsImportTo(imports, "scripts/piggydb-jquery.js", true);
			addJsImportTo(imports, "scripts/piggydb-widgets.js", true);
			DEFAULT_HEAD_JS_IMPORTS = imports.toString();
		}
		this.headJs.append(DEFAULT_HEAD_JS_IMPORTS);
	}
}
