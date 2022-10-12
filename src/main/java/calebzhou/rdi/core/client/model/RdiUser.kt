package calebzhou.rdi.core.client.model;

/**
 * Created by calebzhou on 2022-09-18,22:40.
 */
public class RdiUser {
	public static RdiUser getCurrentUser() {
		return currentUser;
	}

	public static void setCurrentUser(RdiUser currentUser) {
		RdiUser.currentUser = currentUser;
	}

	private static RdiUser currentUser;
	private  String uuid;
	private  String name;
	private  String pwd;
	private  String type;

	public RdiUser setUuid(String uuid) {
		this.uuid = uuid;
		return this;
	}

	public RdiUser setName(String name) {
		this.name = name;
		return this;
	}

	public RdiUser setPwd(String pwd) {
		this.pwd = pwd;
		return this;
	}

	public RdiUser setType(String type) {
		this.type = type;
		return this;
	}

	public String getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public String getPwd() {
		return pwd;
	}

	public String getType() {
		return type;
	}

	public RdiUser(String uuid, String name, String pwd, String type) {
		this.uuid = uuid;
		this.name = name;
		this.pwd = pwd;
		this.type = type;
	}
}
