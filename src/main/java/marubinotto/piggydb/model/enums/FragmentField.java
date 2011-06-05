package marubinotto.piggydb.model.enums;

import java.io.Serializable;
import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.util.Assert;

import org.apache.commons.lang.enums.ValuedEnum;

public class FragmentField extends ValuedEnum {
	
	public static final FragmentField UPDATE_DATETIME = 
		new FragmentField("update_datetime", 1, false, new FieldValueGetter() {
			public Object get(Fragment fragment) {
				return fragment.getUpdateDatetime();
			}
		});
	
	public static final FragmentField CREATION_DATETIME = 
		new FragmentField("creation_datetime", 2, false, new FieldValueGetter() {
			public Object get(Fragment fragment) {
				return fragment.getCreationDatetime();
			}
		});
	
	public static final FragmentField FRAGMENT_ID = 
		new FragmentField("fragment_id", 3, false, new FieldValueGetter() {
			public Object get(Fragment fragment) {
				return fragment.getId();
			}
		});
	
	public static final FragmentField TITLE = 
		new FragmentField("title", 4, true, new FieldValueGetter() {
			public Object get(Fragment fragment) {
				return fragment.getTitle();
			}
		});
	
	public static final FragmentField CREATOR = 
		new FragmentField("creator", 5, true, new FieldValueGetter() {
			public Object get(Fragment fragment) {
				return fragment.getCreator();
			}
		});
	
	public static final FragmentField UPDATER = 
		new FragmentField("updater", 6, true, new FieldValueGetter() {
			public Object get(Fragment fragment) {
				return fragment.getUpdater();
			}
		});
	
	private boolean isString;
	private FieldValueGetter fieldValueGetter;

	private FragmentField(String name, int id, boolean isString, FieldValueGetter fieldValueGetter) {
        super(name, id);
        this.isString = isString;
        this.fieldValueGetter = fieldValueGetter;
    }
	
	public static FragmentField getEnum(int id) {
        return (FragmentField)getEnum(FragmentField.class, id);
    }
	
	@SuppressWarnings("unchecked")
	public static List<FragmentField> getEnumList() {
		return getEnumList(FragmentField.class);
	}
	
	public boolean isString() {
		return this.isString;
	}

	public interface FieldValueGetter extends Serializable {
		public Object get(Fragment fragment);
	}
	
	@SuppressWarnings("unchecked")
	public Comparable getFieldValue(Fragment fragment) {
		Assert.Property.requireNotNull(fragment, "fragment");
		return (Comparable)this.fieldValueGetter.get(fragment);
	}
}
