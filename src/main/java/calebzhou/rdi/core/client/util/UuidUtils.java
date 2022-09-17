package calebzhou.rdi.core.client.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Created by calebzhou on 2022-09-16,17:14.
 */
public class UuidUtils {
	//把不带横线的uuid转换成带横线的
	public static String uuidAddDash(String uuidNoDash){
		return uuidNoDash.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5" );
	}
	public static String createUuidByName(String playerName){
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" +playerName).getBytes(StandardCharsets.UTF_8)).toString();
	}
}
