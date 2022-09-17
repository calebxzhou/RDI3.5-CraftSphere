package calebzhou.rdi.core.client.util;

import calebzhou.rdi.core.client.loader.LoadProgressDisplay;
import org.apache.commons.lang3.StringUtils;


/**
 * Created by calebzhou on 2022-09-16,17:22.
 */
public class RdiAccountUtils {
	public static void login(String name,String uuid,String pwd){
		/*if(StringUtils.isEmpty(uuid) || uuid.startsWith("00000000")){
			uuid = UuidUtils.createUuidByName(name);
		}else{
			//mojang登录的uuid不带横线，要通过正则表达式转换成带横线的
			uuid = UuidUtils.uuidAddDash(uuid);
		}
		RdiHttpClient.sendRequestAsync(new RdiHttpClient.RequestBuilder()
				.type(RdiHttpClient.RequestType.GET)
				.url(RDI_URL+"v37/account/login")
				.param("id",uuid)
				.param("pname",name)
				.param("pwd",pwd)
				.build(),
				(call, response) -> {
					String token = response.body().string();
				},(call, e) -> {

				}
		);*/
	}
}
