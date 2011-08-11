package marubinotto.piggydb.model.enums;

public class Role extends org.apache.commons.lang.enums.Enum {

	// For the permission of default menu items (implemented by Click Framework)
	public static final Role DEFAULT = new Role("default", "Default");

	public static final Role OWNER = new Role("owner", "Owner");
	public static final Role INTERNAL_USER = new Role("internal-user", "Internal User");
	public static final Role VIEWER = new Role("viewer", "Viewer");

	private String diplayName;

	private Role(String name, String diplayName) {
		super(name);
		this.diplayName = diplayName;
	}

	public static Role getEnum(String name) {
		return (Role)getEnum(Role.class, name);
	}

	public String getDiplayName() {
		return this.diplayName;
	}
}
