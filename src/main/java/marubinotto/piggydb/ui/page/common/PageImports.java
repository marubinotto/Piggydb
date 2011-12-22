package marubinotto.piggydb.ui.page.common;

import marubinotto.util.Assert;

import org.apache.commons.lang.text.StrBuilder;

public class PageImports {

	private HtmlFragments html;
	
	private static String DEFAULT_CSS_IMPORTS;
	public StrBuilder css = new StrBuilder();
	
	public PageImports(HtmlFragments html) {
		Assert.Arg.notNull(html, "html");
		this.html = html;
		setDefaultCss();
	}
	
	public void importCss(String filePath, boolean versioning, String media) {
		addCssImportTo(this.css, filePath, versioning, media);
	}
	
	private void addCssImportTo(StrBuilder imports, String filePath, boolean versioning, String media) {
		imports.appendln(this.html.cssImport(filePath, versioning, media));
	}
	
	private void setDefaultCss() {
		if (DEFAULT_CSS_IMPORTS == null) {
			StrBuilder imports = new StrBuilder();
			addCssImportTo(imports, "style/prettify.css", true, null);
			addCssImportTo(imports, "style/watermark.css", true, null);
			addCssImportTo(imports, "style/curve/curve.css", true, "screen");
			addCssImportTo(imports, "style/tree/tree.css", true, null);
			addCssImportTo(imports, "style/facebox/facebox.css", true, null);
			addCssImportTo(imports, "style/piggydb-base.css", true, "screen");
			addCssImportTo(imports, "style/piggydb-shared.css", true, "screen");
			addCssImportTo(imports, "style/piggydb-wiki-help.css", true, null);
			addCssImportTo(imports, "style/piggydb-print.css", true, "print");
			addCssImportTo(imports, "jquery-ui-1.8.14/themes/base/jquery.ui.all.css", false, "screen");
			addCssImportTo(imports, "autocomplete/jquery.autocomplete-1.1-1.css", false, "screen");
			DEFAULT_CSS_IMPORTS = imports.toString();
		}
		this.css.append(DEFAULT_CSS_IMPORTS);
	}
}
