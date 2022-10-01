/*
 * Modern UI.
 * Copyright (C) 2019-2022 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.graphics.font;

/**
 * This class holds information for a glyph about its pre-rendered image in an
 * OpenGL texture. The glyph must be laid-out so that it has something to render
 * in a context.
 *
 * @see GlyphManager
 * @see GLFontAtlas
 * @since 2.0
 */
public class GLBakedGlyph {

    /**
     * The OpenGL texture ID that contains this glyph image.
     */
    public int texture;

    /**
     * The horizontal offset to baseline in pixels.
     */
    public int x;

    /**
     * The vertical offset to baseline in pixels.
     */
    public int y;

    /**
     * The width of this glyph image in pixels (w/o padding).
     */
    public int width;

    /**
     * The height of this glyph image in pixels (w/o padding).
     */
    public int height;

    /**
     * The horizontal texture coordinate of the upper-left corner.
     */
    public float u1;

    /**
     * The vertical texture coordinate of the upper-left corner.
     */
    public float v1;

    /**
     * The horizontal texture coordinate of the lower-right corner.
     */
    public float u2;

    /**
     * The vertical texture coordinate of the lower-right corner.
     */
    public float v2;

    public GLBakedGlyph() {
    }

    @Override
    public String toString() {
        return "Glyph{tex=" + texture +
                ",x=" + x +
                ",y=" + y +
                ",w=" + width +
                ",h=" + height +
                ",u1=" + u1 +
                ",v1=" + v1 +
                ",u2=" + u2 +
                ",v2=" + v2 +
                '}';
    }
}
