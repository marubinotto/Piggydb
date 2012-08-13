package marubinotto.piggydb.ui.page.control;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.User;
import marubinotto.util.Assert;

public class UserMenu {
	
	private static UserMenu instance = new UserMenu();

	public static UserMenu getInstance() {
		return instance;
	}
	
	private UserMenu() {
	}
	
	public static interface Item {
		
		public String getHref();
		
		public String getLabel();
		
		public boolean isAvailableTo(User user);
	}
	
	private List<Item> items = new ArrayList<Item>();
	
	public void addItem(Item item) {
		Assert.Arg.notNull(item, "item");
		this.items.add(item);
	}
	
	public List<Item> getItems() {
		return this.items;
	}
}
