package calebzhou.rdi.hifont;

import com.badlogic.gdx.graphics.Color;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Created by calebzhou on 2022-09-12;22:50.
 */
public class TextBeingDraw {
	public String text; public boolean useMipmap; public float x; public float y; public float sizePx;
	public Color color
			;

	public TextBeingDraw(String text, boolean useMipmap, float x, float y, float sizePx, Color color) {
		this.text = text;
		this.useMipmap = useMipmap;
		this.x = x;
		this.y = y;
		this.sizePx = sizePx;
		this.color = color;
	}

	public static TextBeingDraw create(String text, boolean useMipmap,Color color, float x, float y, float sizePx){
		TextBeingDraw textBeingDraw = new TextBeingDraw(text, useMipmap, x, y, sizePx,color);
		return textBeingDraw;
	}
}
