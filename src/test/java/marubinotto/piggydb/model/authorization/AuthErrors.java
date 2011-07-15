package marubinotto.piggydb.model.authorization;

import marubinotto.piggydb.model.Entity;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.exception.AuthorizationException;

public class AuthErrors {
	
	public static AuthorizationException toChangeEntity(Entity entity) {
		return new AuthorizationException("no-auth-to-change-entity", entity.toString());
	}
	
	public static AuthorizationException forTag(Tag tag) {
		return forTag(tag.getName());
	}
	
	public static AuthorizationException forTag(String tagName) {
		return new AuthorizationException("no-auth-for-tag", tagName);
	}
	
	public static AuthorizationException toExtendTrash() {
		return new AuthorizationException("no-auth-to-extend-trash");
	}

	public static AuthorizationException toCreateFragment() {
		return new AuthorizationException("no-auth-to-create-fragment");
	}

	public static AuthorizationException toChangeFragment(Fragment fragment) {
		return new AuthorizationException("no-auth-to-change-fragment", fragment.getId().toString());
	}
	
	public static AuthorizationException toDeleteFragment(Fragment fragment) {
		return new AuthorizationException("no-auth-to-delete-fragment", fragment.getId().toString());
	}
	
	public static AuthorizationException toCreateFragmentRelation() {
		return new AuthorizationException("no-auth-to-create-fragment-relation");
	}	
	
	public static AuthorizationException toDeleteFragmentRelation() {
		return new AuthorizationException("no-auth-to-delete-fragment-relation");
	}

	public static AuthorizationException toChangeFilter(Filter filter) {
		return new AuthorizationException("no-auth-to-change-filter", filter.getName());
	}
}
