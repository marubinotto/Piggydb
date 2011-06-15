package marubinotto.piggydb.ui.page.control.form;

import org.apache.commons.lang.UnhandledException;

import net.sf.click.control.Field;
import net.sf.click.control.Form;

public class PublicFieldForm extends Form {

	public PublicFieldForm() {
		super();
	}
	
	public PublicFieldForm(Object listener, String method) {
		super();
		setListener(listener, method);
	}

	@Override
	public void onInit() {
		registerPublicControls();
		super.onInit();
	}

	private void registerPublicControls() {
		for (java.lang.reflect.Field publicField : getClass().getFields()) {
			Object fieldValue = null;
			try {
				fieldValue = publicField.get(this);
			}
			catch (Exception e) {
				throw new UnhandledException(e);
			}
			
			if (fieldValue == null || !(fieldValue instanceof Field)) {
				continue;
			}
			
			Field formField = (Field)fieldValue;
            if (formField.getName() == null) {
            	formField.setName(publicField.getName());
            }
            
            add(formField);
		}
	}
}
