package marubinotto.piggydb.ui.page.common;

import marubinotto.util.Assert;
import marubinotto.util.CodedException;
import marubinotto.util.MessageSource;
import net.sf.click.control.Field;
import net.sf.click.control.Form;

public class Utils {
	
	public static final int ALMOST_UNLIMITED_PAGE_SIZE = 1000000;

	public static void handleFieldError(Exception exception, Field field, MessageSource messageSource) 
	throws Exception {
		Assert.Arg.notNull(exception, "exception");
		Assert.Arg.notNull(field, "field");
		Assert.Arg.notNull(messageSource, "messageSource");

		field.setError(CodedException.getCodedMessageOrThrow(exception, messageSource));
	}

	public static void handleFormError(Exception exception, Form form, MessageSource messageSource) 
	throws Exception {
		Assert.Arg.notNull(exception, "exception");
		Assert.Arg.notNull(form, "form");
		Assert.Arg.notNull(messageSource, "messageSource");

		form.setError(CodedException.getCodedMessageOrThrow(exception, messageSource));
	}
}
