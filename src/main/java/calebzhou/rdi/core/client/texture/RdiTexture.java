package calebzhou.rdi.core.client.texture;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_CLAMP_TO_BORDER;
import static org.lwjgl.stb.STBImage.*;

/**
 * Created by calebzhou on 2022-09-23,19:19.
 */
public class RdiTexture {

	/**
	 * Stores the handle of the texture.
	 */
	private final int id;

	/**
	 * Width of the texture.
	 */
	private int width;
	/**
	 * Height of the texture.
	 */
	private int height;

	/** Creates a texture. */
	public RdiTexture() {
		id = glGenTextures();
	}

	/**
	 * Binds the texture.
	 */
	public void bind() {
		glBindTexture(GL_TEXTURE_2D, id);
	}

	/**
	 * Sets a parameter of the texture.
	 *
	 * @param name  Name of the parameter
	 * @param value Value to set
	 */
	public void setParameter(int name, int value) {
		glTexParameteri(GL_TEXTURE_2D, name, value);
	}

	/**
	 * Uploads image data with specified width and height.
	 *
	 * @param width  Width of the image
	 * @param height Height of the image
	 * @param data   Pixel data of the image
	 */
	public void uploadData(int width, int height, ByteBuffer data) {
		uploadData(GL_RGBA8, width, height, GL_RGBA, data);
	}

	/**
	 * Uploads image data with specified internal format, width, height and
	 * image format.
	 *
	 * @param internalFormat Internal format of the image data
	 * @param width          Width of the image
	 * @param height         Height of the image
	 * @param format         Format of the image data
	 * @param data           Pixel data of the image
	 */
	public void uploadData(int internalFormat, int width, int height, int format, ByteBuffer data) {
		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, data);
	}

	/**
	 * Delete the texture.
	 */
	public void delete() {
		glDeleteTextures(id);
	}

	/**
	 * Gets the texture width.
	 *
	 * @return Texture width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the texture width.
	 *
	 * @param width The width to set
	 */
	public void setWidth(int width) {
		if (width > 0) {
			this.width = width;
		}
	}

	/**
	 * Gets the texture height.
	 *
	 * @return Texture height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets the texture height.
	 *
	 * @param height The height to set
	 */
	public void setHeight(int height) {
		if (height > 0) {
			this.height = height;
		}
	}

	/**
	 * Creates a texture with specified width, height and data.
	 *
	 * @param width  Width of the texture
	 * @param height Height of the texture
	 * @param data   Picture Data in RGBA format
	 *
	 * @return Texture from the specified data
	 */
	public static RdiTexture createTexture(int width, int height, ByteBuffer data) {
		RdiTexture texture = new RdiTexture();
		texture.setWidth(width);
		texture.setHeight(height);

		texture.bind();

		texture.setParameter(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		texture.setParameter(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
		texture.setParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		texture.setParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		texture.uploadData(GL_RGBA8, width, height, GL_RGBA, data);

		return texture;
	}

	/**
	 * Load texture from file.
	 *
	 * @param path File path of the texture
	 *
	 * @return Texture from specified file
	 */
	public static RdiTexture loadTexture(String path) {
		ByteBuffer image;
		int width, height;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			/* Prepare image buffers */
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer comp = stack.mallocInt(1);

			/* Load image */
			stbi_set_flip_vertically_on_load(true);
			image = stbi_load(path, w, h, comp, 4);
			if (image == null) {
				throw new RuntimeException("Failed to load a texture file!"
						+ System.lineSeparator() + stbi_failure_reason());
			}

			/* Get width and height of image */
			width = w.get();
			height = h.get();
		}

		return createTexture(width, height, image);
	}
}
