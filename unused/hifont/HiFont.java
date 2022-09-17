package calebzhou.rdi.hifont;

import calebzhou.rdi.core.client.FileConst;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.joml.Math;

import java.util.HashMap;

/**
 * Created by calebzhou on 2022-09-12,22:11.
 */
public class HiFont {
	static FileHandle fontFileHandle;
	static FreeTypeFontGenerator fontGenerator;
	public static void init(){
		fontFileHandle =  Gdx.files.classpath(FileConst.FONT_PATH);
		fontGenerator = new FreeTypeFontGenerator(fontFileHandle);
	}
	public static boolean isFontGeneratorHasGlyph(int charCode){
		return fontGenerator.hasGlyph(charCode);
	}
	public static BitmapFont drawText(Batch batch, TextBeingDraw textBeingDraw){
		final float fontOversampling = 2f;
		FreeTypeFontGenerator.FreeTypeFontParameter parameter;
		parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.characters = textBeingDraw.text;
		if(textBeingDraw.useMipmap){
			parameter.genMipMaps=true;
			parameter.minFilter = Texture.TextureFilter.MipMapLinearLinear;
		}else{
			parameter.genMipMaps=false;
			parameter.minFilter = Texture.TextureFilter.Linear;
		}
		parameter.size = Math.round(textBeingDraw.sizePx * fontOversampling);
		parameter.magFilter = Texture.TextureFilter.Linear;
		parameter.color = textBeingDraw.color;
		BitmapFont font = fontGenerator.generateFont(parameter);
		font.setUseIntegerPositions(false);
		font.getRegion().getTexture().setFilter(parameter.minFilter, Texture.TextureFilter.Linear);
		font.getData().setScale((float) (1.0/fontOversampling));
		font.draw(batch, textBeingDraw.text,textBeingDraw.x,textBeingDraw.y);
		return font;
	}
}
