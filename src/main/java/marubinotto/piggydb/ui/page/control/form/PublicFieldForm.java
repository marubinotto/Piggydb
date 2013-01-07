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
    try {
      registerPublicControls();
    }
    catch (Exception e) {
      throw new UnhandledException(e);
    }
    super.onInit();
  }

  private void registerPublicControls() 
  throws IllegalArgumentException, IllegalAccessException {
    for (java.lang.reflect.Field publicField : getClass().getFields()) {
      Object fieldValue = publicField.get(this);
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
