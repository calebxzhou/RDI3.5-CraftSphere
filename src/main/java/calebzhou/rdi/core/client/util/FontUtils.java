package calebzhou.rdi.core.client.util;

import java.util.stream.IntStream;

/**
 * Created by calebzhou on 2022-09-11,22:34.
 */
public class FontUtils {
	public static void main(String[] args) {

		StringBuilder allchars = new StringBuilder(65536);
		//英文
		IntStream.range(0,65536)
				//.parallel()
				.filter(Character::isDefined)
				.forEach(i->allchars.append((char)i));
		System.out.println(allchars.toString());
	}
}
