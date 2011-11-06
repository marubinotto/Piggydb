package marubinotto.piggydb.model;

import static java.lang.Math.max;
import static java.lang.Math.min;
import marubinotto.piggydb.model.entity.RawClassifiable;
import marubinotto.piggydb.model.exception.AuthorizationException;
import marubinotto.util.Assert;

public class FragmentRelation extends RawClassifiable {

	public Fragment from;
	public Fragment to;
	
	public Integer priority = 0;
	public Boolean twoWay;
	
	public FragmentRelation() {		
	}
	
	public FragmentRelation(User user) {
		super(user);
	}
	
	public FragmentRelation(Fragment from, Fragment to) {
		this.from = from;
		this.to = to;
	}
	
	public boolean isSamePairAs(FragmentRelation other) {
		Assert.Arg.notNull(other, "other");
		Assert.Arg.notNull(other.from, "other.from");
		Assert.Arg.notNull(other.from.getId(), "other.from.getId()");
		Assert.Arg.notNull(other.to, "other.to");
		Assert.Arg.notNull(other.to.getId(), "other.to.getId()");
		Assert.Property.requireNotNull(from, "from");
		Assert.Property.requireNotNull(from.getId(), "from.getId()");
		Assert.Property.requireNotNull(to, "to");
		Assert.Property.requireNotNull(to.getId(), "to.getId()");
		
		return min(from.getId(), to.getId()) == min(other.from.getId(), other.to.getId())
			&& max(from.getId(), to.getId()) == max(other.from.getId(), other.to.getId());
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
