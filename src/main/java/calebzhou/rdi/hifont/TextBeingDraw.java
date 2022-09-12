package calebzhou.rdi.hifont;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Created by calebzhou on 2022-09-12;22:50.
 */
public class TextBeingDraw {
	public String text; public boolean useMipmap; public float x; public float y; public float sizePx;
	private TextBeingDraw(String text, boolean useMipmap, float x, float y, float sizePx) {
		this.text = text;
		this.useMipmap = useMipmap;
		this.x = x;
		this.y = y;
		this.sizePx = sizePx;
	}
	public static TextBeingDraw create(String text, boolean useMipmap, float x, float y, float sizePx){
		TextBeingDraw textBeingDraw = new TextBeingDraw(text, useMipmap, x, y, sizePx);
		return textBeingDraw;
	}
}
