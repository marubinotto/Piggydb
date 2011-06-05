package marubinotto.piggydb.model;

import marubinotto.piggydb.model.entity.RawClassifiable;
import marubinotto.util.Assert;

public class FragmentRelation extends RawClassifiable {

	public Fragment from;
	public Fragment to;
	
	public Integer priority = 0;
	
	public FragmentRelation() {		
	}
	
	public FragmentRelation(User user) {
		super(user);
	}
	
	public FragmentRelation(Fragment from, Fragment to) {
		this.from = from;
		this.to = to;
	}
	
	@Override
	public String toString() {
		return from + " -> " + to;
	}
	
	//
	// Authorization
	//
	
	public static boolean canCreate(User user) {
		Assert.Arg.notNull(user, "user");
		
		try { ensureCanCreate(user); return true; } 
		catch (AuthorizationException e) { return false; }
	}

	public static void ensureCanCreate(User user) {
		if (user.isViewer()) {
			throw new AuthorizationException("no-auth-to-create-fragment-relation");
		}
	}
	
	@Override
	public void ensureCanDelete(User user) throws AuthorizationException {
		super.ensureCanDelete(user);
		if (user.isViewer()) {
			throw new AuthorizationException("no-auth-to-delete-fragment-relation");
		}
	}
}
