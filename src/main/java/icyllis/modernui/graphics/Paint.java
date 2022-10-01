/*
 * Modern UI.
 * Copyright (C) 2019-2021 BloCamLimb. All rights reserved.
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

package icyllis.modernui.graphics;

import icyllis.modernui.annotation.ColorInt;
import icyllis.modernui.util.Pool;
import icyllis.modernui.util.Pools;
import org.intellij.lang.annotations.MagicConstant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A Paint collects all options outside the Canvas clip and Canvas matrix,
 * such as style and color information, applied when drawing geometries and images.
 * <p>
 * A Paint also collects effects and filters that describe single-pass and multiple-pass
 * algorithms that alter the drawing geometry, color, and transparency. For instance,
 * Paint does not directly implement dashing or blur, but contains the objects that do so.
 * <p>
 * Multisampling anti-aliasing (MSAA) is always enabled.
 */
public class Paint {

    private static final ThreadLocal<Paint> TLS = ThreadLocal.withInitial(Paint::new);
    private static final Pool<Paint> BAG = Pools.concurrent(4);

    /**
     * The Style specifies if the primitive being drawn is filled, stroked, or
     * both (in the same color). The default is FILL.
     */
    @MagicConstant(intValues = {FILL, STROKE, STROKE_AND_FILL, FILL_AND_STROKE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Style {
    }

    /**
     * Geometry drawn with this style will be filled, ignoring all
     * stroke-related settings in the paint.
     */
    public static final int FILL = 0x00;

    /**
     * Geometry drawn with this style will be stroked, respecting
     * the stroke-related fields on the paint.
     */
    public static final int STROKE = 0x01;

    /**
     * Geometry (path) drawn with this style will be both filled and stroked
     * at the same time, respecting the stroke-related fields on the paint.
     * This share all paint attributes; for instance, they are drawn
     * with the same color. Use this to avoid hitting the same pixels twice
     * with a stroke draw and a fill draw.
     */
    public static final int STROKE_AND_FILL = 0x02;
    public static final int FILL_AND_STROKE = 0x02; // alias

    private static final int STYLE_MASK = 0x03;

    /**
     * The Cap specifies the treatment for the beginning and ending of
     * stroked lines and paths. The default is ROUND.
     */
    @MagicConstant(intValues = {CAP_BUTT, CAP_ROUND, CAP_SQUARE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Cap {
    }

    /**
     * The stroke ends with the path, and does not project beyond it.
     */
    public static final int CAP_BUTT = 0x00;

    /**
     * The stroke projects out as a semicircle, with the center at the
     * end of the path.
     */
    public static final int CAP_ROUND = 0x04;

    /**
     * The stroke projects out as a square, with the center at the end
     * of the path.
     */
    public static final int CAP_SQUARE = 0x08;

    private static final int CAP_MASK = 0x0C;

    /**
     * The Join specifies the treatment where lines and curve segments
     * join on a stroked path. The default is ROUND.
     * <p>
     * Join affects the four corners of a stroked rectangle, and the connected segments in a
     * stroked path.
     * <p>
     * Choose miter join to draw sharp corners. Choose round join to draw a circle with a
     * radius equal to the stroke width on top of the corner. Choose bevel join to minimally
     * connect the thick strokes.
     * <p>
     * The fill path constructed to describe the stroked path respects the join setting but may
     * not contain the actual join. For instance, a fill path constructed with round joins does
     * not necessarily include circles at each connected segment.
     */
    @MagicConstant(intValues = {JOIN_MITER, JOIN_ROUND, JOIN_BEVEL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Join {
    }

    /**
     * The outer edges of a join meet at a sharp angle
     */
    public static final int JOIN_MITER = 0x00;

    /**
     * The outer edges of a join meet in a circular arc.
     */
    public static final int JOIN_ROUND = 0x10;

    /**
     * The outer edges of a join meet with a straight line
     */
    public static final int JOIN_BEVEL = 0x20;

    private static final int JOIN_MASK = 0x30;

    private static final int GRADIENT_MASK = 0x40;

    private static final int BLEND_MODE_SHIFT = 8;
    private static final int BLEND_MODE_MASK = 0xFF << BLEND_MODE_SHIFT;

    private static final int DEFAULT_FLAGS = FILL | CAP_ROUND | JOIN_ROUND |
            BlendMode.toValue(BlendMode.SRC_OVER) << BLEND_MODE_SHIFT;

    // may be replaced by float values
    private int mColor;
    private int mFlags;
    private float mStrokeWidth;
    private float mSmoothRadius;

    // may be removed in the future
    private int[] mColors;

    /**
     * Creates a new Paint with defaults.
     *
     * @see #take()
     */
    public Paint() {
        reset();
    }

    /**
     * Create a new paint, initialized with the attributes in the specified
     * paint parameter.
     *
     * @param paint Existing paint used to initialize the attributes of the
     *              new paint.
     */
    public Paint(@Nonnull Paint paint) {
        mColor = paint.mColor;
        mFlags = paint.mFlags;
        mStrokeWidth = paint.mStrokeWidth;
        mSmoothRadius = paint.mSmoothRadius;
        if (paint.mColors != null) {
            setColors(paint.mColors);
        }
    }

    /**
     * <b>WARNING: This method may be abused.</b> Use {@link #take()} instead.
     * <p>
     * Returns a thread-local paint, the paint will be reset before return.
     * This method is designed for temporary operations that avoid creating
     * new objects. For a {@link Canvas} draw call that is not after this method
     * or {@link #reset()}, the state of the paint is undefined.
     * <p>
     * For example:
     * <pre>{@code
     * @Override
     * protected void onDraw(Canvas canvas) {
     *     var paint = Paint.get();
     *     paint.setColor(mColorA);
     *     canvas.drawRect(mRectA, paint);
     *
     *     doSomeActions(); // call any other methods except with Canvas
     *
     *     paint.reset(); // not sure if they will use the paint, so reset it
     *     paint.setColor(mColorB);
     *     canvas.drawRect(mRectB, paint);
     * }
     * }</pre>
     * The API implementation requires that any method in {@link Canvas} must NOT
     * modify the state of the Paint instance obtained by this method.
     *
     * @return a thread-local paint
     * @see #take()
     */
    @Nonnull
    public static Paint get() {
        Paint paint = TLS.get();
        paint.reset();
        return paint;
    }

    /**
     * Returns a paint from a shared pool, the paint will be reset before return.
     * This method is designed for temporary operations that avoid creating
     * new objects. A {@link #drop()} is expected (not strictly necessary) after use.
     * <p>
     * For example:
     * <pre>{@code
     * @Override
     * protected void onDraw(Canvas canvas) {
     *     var paint = Paint.take();
     *     paint.setColor(mColorA);
     *     canvas.drawRect(mRectA, paint);
     *
     *     doSomeActions(); // calling any other methods will not affect this
     *
     *     paint.setColor(mColorB);
     *     canvas.drawRect(mRectB, paint);
     *     paint.drop(); // recycle it before method return
     * }
     * }</pre>
     *
     * @return a pooled paint object
     * @see #drop()
     */
    @Nonnull
    public static Paint take() {
        Paint paint = BAG.acquire();
        if (paint == null)
            return new Paint();
        paint.reset();
        return paint;
    }

    /**
     * Set the paint to defaults.
     */
    public void reset() {
        mColor = ~0;
        mFlags = DEFAULT_FLAGS;
        mStrokeWidth = 2;
        mSmoothRadius = 2;
    }

    /**
     * Set the paint's color. Note that the color is an int containing alpha
     * as well as r,g,b. This 32bit value is not premultiplied, meaning that
     * its alpha can be any value, regardless of the values of r,g,b.
     * See the Color class for more details.
     *
     * @param color The new color (including alpha) to set in the paint.
     */
    public void setColor(@ColorInt int color) {
        mColor = color;
        mFlags &= ~GRADIENT_MASK;
    }

    /**
     * Return the paint's color in sRGB. Note that the color is a 32bit value
     * containing alpha as well as r,g,b. This 32bit value is not premultiplied,
     * meaning that its alpha can be any value, regardless of the values of
     * r,g,b. See the Color class for more details.
     *
     * @return the paint's color (and alpha).
     */
    @ColorInt
    public int getColor() {
        return mColor;
    }

    /**
     * Helper to setColor(), that only assigns the color's alpha value,
     * leaving its r,g,b values unchanged. Results are undefined if the alpha
     * value is outside the range [0..255]
     *
     * @param a set the alpha component [0..255] of the paint's color.
     */
    public void setAlpha(int a) {
        mColor = (mColor << 8 >>> 8) | (a << 24);
        mFlags &= ~GRADIENT_MASK;
    }

    /**
     * Helper to getColor() that just returns the color's alpha value. This is
     * the same as calling getColor() >>> 24. It always returns a value between
     * 0 (completely transparent) and 255 (completely opaque).
     *
     * @return the alpha component of the paint's color.
     */
    public int getAlpha() {
        return mColor >>> 24;
    }

    /**
     * Helper to setColor(), that only assigns the color's r,g,b values,
     * leaving its alpha value unchanged.
     *
     * @param r The new red component (0..255) of the paint's color.
     * @param g The new green component (0..255) of the paint's color.
     * @param b The new blue component (0..255) of the paint's color.
     */
    public void setRGB(int r, int g, int b) {
        mColor = (mColor >>> 24 << 24) | (r << 16) | (g << 8) | b;
        mFlags &= ~GRADIENT_MASK;
    }

    /**
     * Helper to setColor(), that takes r,g,b,a and constructs the color int
     *
     * @param r The new red component (0..255) of the paint's color.
     * @param g The new green component (0..255) of the paint's color.
     * @param b The new blue component (0..255) of the paint's color.
     * @param a The new alpha component (0..255) of the paint's color.
     */
    public void setRGBA(int r, int g, int b, int a) {
        setColor(Color.argb(a, r, g, b));
    }

    /**
     * Helper to setColor(), that takes a,r,g,b and constructs the color int
     *
     * @param a The new alpha component (0..255) of the paint's color.
     * @param r The new red component (0..255) of the paint's color.
     * @param g The new green component (0..255) of the paint's color.
     * @param b The new blue component (0..255) of the paint's color.
     */
    public void setARGB(int a, int r, int g, int b) {
        setColor(Color.argb(a, r, g, b));
    }

    /**
     * Set the colors in ARGB and enable gradient mode. When enabled, a primitive should
     * use the colors sequentially, and {@link #setColor(int)} is ignored.
     * You can use this to make gradient effect or edge fading effect in one pass,
     * without post-processing shaders.
     * <p>
     * A Paint object has a backing array storing these values, then a copy of the parameter
     * array will be used. The colors are used in the order of top left, top right, bottom right
     * and bottom left.
     * <p>
     * If the length of the given array is less than 4, then rest color values will use the
     * last color in the given array. If greater than 4, then rest values are ignored.
     * <p>
     * By default, this mode is disabled. Calling other methods like {@link #setColor(int)}
     * or {@link #setAlpha(int)} will disable the mode.
     *
     * @param colors an array of sequential colors
     * @see #setColors(int, int, int, int)
     * @see #isGradient()
     */
    public void setColors(@ColorInt int[] colors) {
        int len = colors.length;
        if (len == 0) {
            return;
        }
        if (mColors == null) {
            mColors = new int[4];
        }
        if (len < 4) {
            System.arraycopy(colors, 0, mColors, 0, len);
            for (int i = len; i < 4; i++) {
                mColors[i] = colors[len - 1];
            }
        } else {
            System.arraycopy(colors, 0, mColors, 0, 4);
        }
        mFlags |= GRADIENT_MASK;
    }

    /**
     * Set the colors in ARGB and enable gradient mode. When enabled, a primitive should
     * use the colors sequentially, and {@link #setColor(int)} is ignored.
     * You can use this to make gradient effect or edge fading effect in one pass,
     * without post-processing shaders.
     * <p>
     * The colors are used in the order of top left, top right, bottom right and bottom left.
     *
     * @param tl the top-left color
     * @param tr the top-right color
     * @param br the bottom-right color
     * @param bl the bottom-left color
     * @see #setColors(int[])
     * @see #isGradient()
     */
    public void setColors(@ColorInt int tl, @ColorInt int tr, @ColorInt int br, @ColorInt int bl) {
        if (mColors == null) {
            mColors = new int[4];
        }
        mColors[0] = tl;
        mColors[1] = tr;
        mColors[2] = br;
        mColors[3] = bl;
        mFlags |= GRADIENT_MASK;
    }

    /**
     * Returns the backing array of the gradient colors. Each call will return
     * the same array object. Do not modify the elements of the array.
     *
     * @return the backing array of the gradient colors, may be null
     * @see #setColors(int[])
     */
    public int[] getColors() {
        return mColors;
    }

    /**
     * Returns whether gradient colors are used.
     *
     * @return whether gradient mode is enabled
     * @see #setColors(int[])
     */
    public boolean isGradient() {
        return (mFlags & GRADIENT_MASK) != 0;
    }

    /**
     * Return the paint's style, used for controlling how primitives' geometries
     * are interpreted, except where noted.
     *
     * @return the paint's style setting (Fill, Stroke)
     */
    public int getStyle() {
        return mFlags & STYLE_MASK;
    }

    /**
     * Set the paint's style, used for controlling how primitives' geometries
     * are interpreted, except where noted.
     *
     * @param style the new style to set in the paint
     */
    public void setStyle(@Style int style) {
        mFlags = (mFlags & ~STYLE_MASK) | (style & STYLE_MASK);
    }

    /**
     * Return the paint's Cap, controlling how the start and end of stroked
     * lines and paths are treated, except where noted.
     *
     * @return the line cap style for the paint, used whenever the paint's
     * style is Stroke or StrokeAndFill.
     */
    public int getStrokeCap() {
        return mFlags & CAP_MASK;
    }

    /**
     * Set the paint's Cap, controlling how the start and end of stroked
     * lines and paths are treated, except where noted.
     *
     * @param cap set the paint's line cap style, used whenever the paint's
     *            style is Stroke or StrokeAndFill.
     */
    public void setStrokeCap(@Cap int cap) {
        mFlags = (mFlags & ~CAP_MASK) | (cap & CAP_MASK);
    }

    /**
     * Return the paint's stroke join type.
     *
     * @return the paint's Join.
     */
    public int getStrokeJoin() {
        return mFlags & JOIN_MASK;
    }

    /**
     * Set the paint's Join.
     *
     * @param join set the paint's Join, used whenever the paint's style is
     *             Stroke or StrokeAndFill.
     */
    public void setStrokeJoin(@Join int join) {
        mFlags = (mFlags & ~JOIN_MASK) | (join & JOIN_MASK);
    }

    /**
     * Return the width for stroking. The default value is 2.0 px.
     * <p>
     * When a round cap is installed, the half of the stroke width will be used as
     * the stroke radius by analytic geometry.
     * <p>
     * A value of 0 will treat as hairlines (primitive).
     *
     * @return the paint's stroke width, used whenever the paint's style is Stroke or StrokeAndFill.
     */
    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    /**
     * Set the width for stroking. The default value is 2.0 px.
     * <p>
     * When a round cap is installed, the half of the stroke width will be used as
     * the stroke radius by analytic geometry.
     * <p>
     * Negative values are silently ignored. A value of 0 will treat as hairlines (primitive).
     *
     * @param width set the paint's stroke width, used whenever the paint's style is Stroke or StrokeAndFill.
     */
    public void setStrokeWidth(float width) {
        if (width < 0) {
            return;
        }
        mStrokeWidth = width;
    }

    /**
     * Return the current smooth (feather) radius. The default value is 2.0 px.
     * <p>
     * Feather effect is used to smooth or blur the edge of primitives by SDF.
     * It can be used as the softness (inverse of hardness) of the brush.
     * It can be used for provide anti-aliasing more than one pixel.
     *
     * @return the paint's feather radius, always non-negative
     * @see #setSmoothRadius(float)
     */
    public float getSmoothRadius() {
        return mSmoothRadius;
    }

    /**
     * Set the smooth (feather) radius in pixels for this paint. The default value is 2.0 px.
     * <p>
     * Feather effect is used to smooth or blur the edge of primitives by SDF.
     * It can be used as the softness (inverse of hardness) of the brush.
     * It can be used for provide anti-aliasing more than one pixel.
     * <p>
     * Negative values are treated as 0.
     *
     * @param radius the paint's feather radius, always non-negative
     */
    public void setSmoothRadius(float radius) {
        mSmoothRadius = Math.max(0, radius);
    }

    /**
     * Set or clear the blend mode. A blend mode defines how source pixels
     * (generated by a drawing command) are composited with the destination pixels
     * (content of the render target).
     * <p>
     * Pass null to clear any previous blend mode.
     *
     * @param blendMode the blend mode to be installed in the paint, may be null
     * @see BlendMode
     */
    public void setBlendMode(@Nullable BlendMode blendMode) {
        int value = BlendMode.toValue(blendMode == null ? BlendMode.SRC_OVER : blendMode) << BLEND_MODE_SHIFT;
        mFlags = (mFlags & ~BLEND_MODE_MASK) | value;
    }

    /**
     * Get the paint's blend mode. By default, returns {@link BlendMode#SRC_OVER}.
     *
     * @return the paint's blend mode used to combine source color with destination color
     */
    @Nonnull
    public BlendMode getBlendMode() {
        return BlendMode.fromValue((mFlags & BLEND_MODE_MASK) >> BLEND_MODE_SHIFT);
    }

    /**
     * Release this paint object to a shared pool. You can't touch this paint anymore after recycling.
     *
     * @see #take()
     */
    public void drop() {
        BAG.release(this);
    }

    /**
     * Returns true if the paint prevents all drawing; otherwise, the paint may or
     * may not allow drawing.
     * <p>
     * Returns true if, for example, blend mode combined with alpha computes a
     * new alpha of zero.
     *
     * @return true if the paint prevents all drawing
     */
    public boolean nothingToDraw() {
        switch (getBlendMode()) {
            case SRC_OVER, SRC_ATOP, DST_OUT, DST_OVER, PLUS -> {
                return getAlpha() == 0;
            }
            case DST -> {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Paint{");
        if (isGradient()) {
            s.append("mColors=[#");
            s.append(Integer.toHexString(mColors[0]));
            s.append(", #");
            s.append(Integer.toHexString(mColors[1]));
            s.append(", #");
            s.append(Integer.toHexString(mColors[2]));
            s.append(", #");
            s.append(Integer.toHexString(mColors[3]));
            s.append(']');
        } else {
            s.append("mColor=#");
            s.append(Integer.toHexString(mColor));
        }
        int style = getStyle();
        s.append(", mStyle=");
        if (style == FILL) {
            s.append("FILL");
        } else if (style == STROKE) {
            s.append("STROKE");
        } else {
            s.append("FILL|STROKE");
        }
        s.append(", mBlendMode=");
        s.append(getBlendMode());
        s.append(", mStrokeWidth=");
        s.append(mStrokeWidth);
        s.append(", mSmoothRadius=");
        s.append(mSmoothRadius);
        s.append('}');
        return s.toString();
    }

    public static int getAlphaDirect(@Nullable Paint paint) {
        return paint != null ? paint.getAlpha() : 0xFF;
    }

    @Nonnull
    public static BlendMode getBlendModeDirect(@Nullable Paint paint) {
        return paint != null ? paint.getBlendMode() : BlendMode.SRC_OVER;
    }

    public static boolean isOpaquePaint(@Nullable Paint paint) {
        if (paint == null) {
            return true;
        }
        if (paint.getAlpha() != 0xFF) {
            return false;
        }
        BlendMode mode = paint.getBlendMode();
        return mode == BlendMode.SRC_OVER || mode == BlendMode.SRC;
    }
}
