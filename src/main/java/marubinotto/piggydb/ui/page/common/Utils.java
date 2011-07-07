package marubinotto.piggydb.ui.page.common;

import marubinotto.util.Assert;
import marubinotto.util.CodedException;
import marubinotto.util.web.WebMessageSource;
import marubinotto.util.web.WebUtils;
import net.sf.click.Page;
import net.sf.click.control.Field;
import net.sf.click.control.Form;

import org.springframework.core.ErrorCoded;

public class Utils {
	
	public static final int ALMOST_UNLIMITED_PAGE_SIZE = 1000000;

	public static String getMessage(CodedException codedException, Page messages) {
		Assert.Arg.notNull(codedException, "codedException");
		Assert.Arg.notNull(messages, "messages");

		if (codedException.getFields() == null) {
			return messages.getMessage(codedException.getErrorCode());
		}
		else {
			String[] fields = codedException.getFields();
			if (!(messages instanceof WebMessageSource)) {
				for (int i = 0; i < fields.length; i++)
					fields[i] = WebUtils.escapeHtml(fields[i]);
			}
			return messages.getMessage(codedException.getErrorCode(), fields);
		}
	}

	public static String getCodedMessageOrThrow(Exception exception, Page messages)
		throws Exception {
		Assert.Arg.notNull(exception, "exception");
		Assert.Arg.notNull(messages, "messages");

		if (!(exception instanceof ErrorCoded)) {
			// Delegate a system error to the Click framework's error handler
			throw exception;
		}

		ErrorCoded coded = (ErrorCoded) exception;
		if (!messages.getMessages().containsKey(coded.getErrorCode())) {
			return exception.getMessage();
		}
		if (exception instanceof CodedException) {
			return getMessage((CodedException) exception, messages);
		}
		else {
			return messages.getMessage(coded.getErrorCode());
		}
	}

	public static void handleFieldError(Exception exception, Field field,
		Page messages) throws Exception {
		Assert.Arg.notNull(exception, "exception");
		Assert.Arg.notNull(field, "field");
		Assert.Arg.notNull(messages, "messages");

		field.setError(getCodedMessageOrThrow(exception, messages));
	}

	public static void handleFormError(Exception exception, Form form,
		Page messages) throws Exception {
		Assert.Arg.notNull(exception, "exception");
		Assert.Arg.notNull(form, "form");
		Assert.Arg.notNull(messages, "messages");

		form.setError(getCodedMessageOrThrow(exception, messages));
	}
}
