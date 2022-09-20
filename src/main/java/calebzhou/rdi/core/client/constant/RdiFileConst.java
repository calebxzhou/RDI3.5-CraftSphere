package calebzhou.rdi.core.client.constant;

import calebzhou.rdi.core.client.RdiSharedConstants;
import calebzhou.rdi.core.client.model.RdiUser;

import java.io.File;

/**
 * Created by calebzhou on 2022-09-20,21:23.
 */
public class RdiFileConst {
	public static File getUserPasswordFile(String uuid){
		return new File(RdiSharedConstants.RDI_USERS_FOLDER, uuid + "_password.txt");
	}
}
