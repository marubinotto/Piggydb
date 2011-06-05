package marubinotto.piggydb.presentation.page.control.form;

import marubinotto.piggydb.model.Tag;
import marubinotto.util.Assert;
import net.sf.click.Page;
import net.sf.click.control.Form;
import net.sf.click.control.HiddenField;
import net.sf.click.control.ImageSubmit;
import net.sf.click.control.Submit;
import net.sf.click.control.TextField;

public class SingleTagForm extends Form {
	
	private Page page;
	
	public TextField tagField = new TextField("tag");
	private String listenerForAdd;
	
	public HiddenField tagToDeleteField = new HiddenField("tagToDelete", String.class);
	private String listenerForDelete;

	public SingleTagForm(Page page) {
		this.page = page;
	}
	
	public void setListenerForAdd(String listenerForAdd) {
		this.listenerForAdd = listenerForAdd;
	}

	public void setListenerForDelete(String listenerForDelete) {
		this.listenerForDelete = listenerForDelete;
	}

	public void initialize() {
		Assert.Property.requireNotNull(listenerForAdd, "listenerForAdd");
		
		this.tagField.setSize(20);
		this.tagField.setMinLength(Tag.MIN_LENGTH);
		this.tagField.setMaxLength(Tag.MAX_LENGTH);
		this.tagField.setAttribute("class", "single-tag watermarked");
		this.tagField.setTitle(getMessage("tag"));
		add(this.tagField);
		
		if (this.listenerForDelete != null) {
			add(this.tagToDeleteField);
			add(new ImageSubmit("deleteTag", "", this.page, this.listenerForDelete));
		}
		
		add(new Submit("addTag", getMessage("add"), this.page, this.listenerForAdd));		
	}
}
