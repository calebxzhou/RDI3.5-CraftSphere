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

package icyllis.modernui.text;

import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.font.LineBreaker;
import icyllis.modernui.math.Rect;
import icyllis.modernui.text.method.TextKeyListener;
import icyllis.modernui.text.style.ParagraphStyle;
import icyllis.modernui.text.style.ReplacementSpan;
import icyllis.modernui.text.style.TabStopSpan;
import icyllis.modernui.view.KeyEvent;
import it.unimi.dsi.fastutil.floats.FloatArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * A base class that manages text layout in visual elements on the screen,
 * which is designed for text pages at high-level.
 * <p>
 * For text that will be edited, use a {@link DynamicLayout},
 * which will be updated as the text changes.
 * For text that will not change, use a {@link StaticLayout}.
 *
 * @see StaticLayout
 * @see DynamicLayout
 * @since 3.0
 */
public abstract class Layout {

    public static final int DIR_LEFT_TO_RIGHT = 1;
    public static final int DIR_RIGHT_TO_LEFT = -1;

    public static final float TAB_INCREMENT = 20;

    private static final ParagraphStyle[] NO_PARA_SPANS = {};

    /// member variables \\\

    private CharSequence mText;
    private TextPaint mPaint;
    private int mWidth;
    private Alignment mAlignment;
    private boolean mSpannedText;
    private final TextDirectionHeuristic mTextDir;

    /**
     * Subclasses of Layout use this constructor to set the display text,
     * width, and other standard properties.
     *
     * @param text  the text to render
     * @param paint the default paint for the layout.  Styles can override
     *              various attributes of the paint.
     * @param width the wrapping width for the text.
     * @param align whether to left, right, or center the text.  Styles can
     *              override the alignment.
     */
    protected Layout(CharSequence text, TextPaint paint,
                     int width, Alignment align) {
        this(text, paint, width, align, TextDirectionHeuristics.FIRSTSTRONG_LTR);
    }

    /**
     * Subclasses of Layout use this constructor to set the display text,
     * width, and other standard properties.
     *
     * @param text    the text to render
     * @param paint   the default paint for the layout.  Styles can override
     *                various attributes of the paint.
     * @param width   the wrapping width for the text.
     * @param align   whether to left, right, or center the text.  Styles can
     *                override the alignment.
     * @param textDir the text direction algorithm
     */
    protected Layout(CharSequence text, TextPaint paint,
                     int width, Alignment align, TextDirectionHeuristic textDir) {
        if (width < 0) {
            throw new IllegalArgumentException("Layout: " + width + " < 0");
        }

        // We probably should re-evaluate bgColor.
        if (paint != null) {
            paint.bgColor = 0;
        }

        mText = text;
        mPaint = paint;
        mWidth = width;
        mAlignment = align;
        mSpannedText = text instanceof Spanned;
        mTextDir = textDir;
    }


    /**
     * Replace constructor properties of this Layout with new ones.  Be careful.
     */
    void replaceWith(CharSequence text, TextPaint paint,
                     int width, Alignment align) {
        if (width < 0) {
            throw new IllegalArgumentException("Layout: " + width + " < 0");
        }

        mText = text;
        mPaint = paint;
        mWidth = width;
        mAlignment = align;
        mSpannedText = text instanceof Spanned;
    }

    /**
     * Draw this Layout on the specified Canvas.
     * <p>
     * Note that this method just calls {@link #drawBackground(Canvas, int, int)}
     * and then {@link #drawText(Canvas, int, int)}. If you need to draw something between the two,
     * such as blinking cursor and selection highlight, you may manually call them separately.
     *
     * @param canvas the canvas to draw on
     * @see #drawBackground(Canvas, int, int)
     * @see #drawText(Canvas, int, int)
     */
    public void draw(@Nonnull Canvas canvas) {
        final long range = getLineRangeForDraw(canvas);
        if (range < 0) return;
        int firstLine = (int) (range >>> 32);
        int lastLine = (int) (range & 0xFFFFFFFFL);
        drawBackground(canvas, firstLine, lastLine);
        drawText(canvas, firstLine, lastLine);
    }

    /**
     * Draw the visible background drawables of this Layout on the specified canvas.
     * <p>
     * Significantly, visible area given by <code>firstLine</code> and
     * <code>lastLine</code> is computed by {@link #getLineRangeForDraw(Canvas)}.
     * You may never just call this method without that method.
     *
     * @param canvas    the canvas to draw on
     * @param firstLine first line index (inclusive)
     * @param lastLine  last line index (inclusive)
     * @see #drawText(Canvas, int, int)
     */
    //TODO background span
    public final void drawBackground(@Nonnull Canvas canvas, int firstLine, int lastLine) {
        if (!mSpannedText) return;
        assert firstLine >= 0 && lastLine >= firstLine;
        Spanned buffer = (Spanned) mText;
    }

    /**
     * Draw all visible text lines of this Layout on the specified canvas.
     * <p>
     * Significantly, visible area given by <code>firstLine</code> and
     * <code>lastLine</code> is computed by {@link #getLineRangeForDraw(Canvas)}.
     * You may never just call this method without that method.
     *
     * @param canvas    the canvas to draw on
     * @param firstLine first line index (inclusive)
     * @param lastLine  last line index (inclusive)
     * @see #drawBackground(Canvas, int, int)
     */
    public final void drawText(@Nonnull Canvas canvas, int firstLine, int lastLine) {
        assert firstLine >= 0 && lastLine >= firstLine && lastLine < getLineCount();

        int previousLineBottom = getLineTop(firstLine);
        int previousLineEnd = getLineStart(firstLine);
        ParagraphStyle[] spans = NO_PARA_SPANS;
        int spanEnd = 0;
        final TextPaint paint = TextPaint.obtain();
        paint.set(mPaint);
        CharSequence buf = mText;

        Alignment paraAlign = mAlignment;
        TabStops tabStops = null;
        boolean tabStopsIsInitialized = false;

        final TextLine tl = TextLine.obtain();

        // Draw the lines, one at a time.
        // The baseline is the top of the following line minus the current line's descent.
        for (int lineNum = firstLine; lineNum <= lastLine; lineNum++) {
            int start = previousLineEnd;
            previousLineEnd = getLineStart(lineNum + 1);
            int end = getLineVisibleEnd(lineNum, start, previousLineEnd);

            int ltop = previousLineBottom;
            int lbottom = getLineTop(lineNum + 1);
            previousLineBottom = lbottom;
            int lbaseline = lbottom - getLineDescent(lineNum);

            int dir = getParagraphDirection(lineNum);
            int left = 0;
            int right = mWidth;

            //TODO para style
            if (mSpannedText) {
                Spanned sp = (Spanned) buf;
                spans = getParagraphSpans(sp, start, spanEnd, ParagraphStyle.class);
            }

            boolean hasTab = getLineContainsTab(lineNum);
            // Can't tell if we have tabs for sure, currently
            if (hasTab && !tabStopsIsInitialized) {
                if (tabStops == null) {
                    tabStops = new TabStops(TAB_INCREMENT, spans);
                } else {
                    tabStops.reset(TAB_INCREMENT, spans);
                }
                tabStopsIsInitialized = true;
            }

            // Determine whether the line aligns to normal, opposite, or center.
            Alignment align = paraAlign;
            if (align == Alignment.ALIGN_LEFT) {
                align = (dir == DIR_LEFT_TO_RIGHT) ?
                        Alignment.ALIGN_NORMAL : Alignment.ALIGN_OPPOSITE;
            } else if (align == Alignment.ALIGN_RIGHT) {
                align = (dir == DIR_LEFT_TO_RIGHT) ?
                        Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL;
            }

            Directions directions = getLineDirections(lineNum);
            final int ellipsisStart = getEllipsisStart(lineNum);
            tl.set(paint, buf, start, end, dir, directions, hasTab, tabStops,
                    ellipsisStart, ellipsisStart + getEllipsisCount(lineNum));

            int x;
            final int indentWidth;
            if (align == Alignment.ALIGN_NORMAL) {
                if (dir == DIR_LEFT_TO_RIGHT) {
                    indentWidth = getIndentAdjust(lineNum, Alignment.ALIGN_LEFT);
                    x = left + indentWidth;
                } else {
                    indentWidth = -getIndentAdjust(lineNum, Alignment.ALIGN_RIGHT);
                    x = right - indentWidth;
                }
            } else {
                int max = (int) tl.metrics(null);
                if (align == Alignment.ALIGN_OPPOSITE) {
                    if (dir == DIR_LEFT_TO_RIGHT) {
                        indentWidth = -getIndentAdjust(lineNum, Alignment.ALIGN_RIGHT);
                        x = right - max - indentWidth;
                    } else {
                        indentWidth = getIndentAdjust(lineNum, Alignment.ALIGN_LEFT);
                        x = left - max + indentWidth;
                    }
                } else { // Alignment.ALIGN_CENTER
                    indentWidth = getIndentAdjust(lineNum, Alignment.ALIGN_CENTER);
                    max = max & ~1;
                    x = ((right + left - max) >> 1) + indentWidth;
                }
            }

            if (directions == Directions.ALL_LEFT_TO_RIGHT && !mSpannedText && !hasTab) {
                // XXX: assumes there's nothing additional to be done
                canvas.drawText(buf, start, end, x, lbaseline, paint);
            } else {
                tl.draw(canvas, x, ltop, lbaseline, lbottom);
            }
        }

        paint.recycle();
        tl.recycle();
    }

    /**
     * Computes the range of visible lines that will be drawn on the specified canvas.
     * It will be used for {@link #drawText(Canvas, int, int)}. The higher 32 bits represent
     * the first line number, while the lower 32 bits represent the last line number.
     * Note that if the range is empty, then the method returns <code>~0L</code>.
     *
     * @param canvas the canvas used to draw this Layout
     * @return the range of lines that need to be drawn, possibly empty.
     */
    public final long getLineRangeForDraw(@Nonnull Canvas canvas) {
        final int lineCount = getLineCount();
        if (lineCount <= 0) {
            return ~0L;
        }
        final int bottom = getLineTop(lineCount);
        if (canvas.quickReject(0, 0, mWidth, bottom)) {
            return ~0L;
        }
        int lineNum = 0, lineTop = 0, lineBottom;
        int firstLine = -1, lastLine = -1;
        do {
            lineBottom = getLineTop(lineNum + 1);
            if (firstLine == -1) {
                if (!canvas.quickReject(0, lineTop, mWidth, lineBottom)) {
                    firstLine = lineNum;
                }
            } else if (canvas.quickReject(0, lineTop, mWidth, lineBottom)) {
                lastLine = lineNum - 1;
                break;
            }
            lineTop = lineBottom;
        } while (++lineNum < lineCount);

        if (firstLine == -1) {
            return ~0L;
        }
        if (lastLine == -1) {
            assert lineNum == lineCount;
            lastLine = lineCount - 1;
        }
        assert lastLine >= firstLine;
        return (long) firstLine << 32 | lastLine;
    }

    /**
     * Get the line number corresponding to the specified vertical position.
     * If you ask for a position above 0, you get 0; if you ask for a position
     * below the bottom of the text, you get the last line.
     */
    // FIXME: It may be faster to do a linear search for layouts without many lines.
    public int getLineForVertical(int vertical) {
        int high = getLineCount(), low = -1, guess;

        while (high - low > 1) {
            guess = (high + low) >> 1;

            if (getLineTop(guess) > vertical)
                high = guess;
            else
                low = guess;
        }

        return Math.max(low, 0);
    }

    /**
     * Get the line number on which the specified text offset appears.
     * If you ask for a position before 0, you get 0; if you ask for a position
     * beyond the end of the text, you get the last line.
     */
    public int getLineForOffset(int offset) {
        int high = getLineCount(), low = -1, guess;

        while (high - low > 1) {
            guess = (high + low) / 2;

            if (getLineStart(guess) > offset)
                high = guess;
            else
                low = guess;
        }

        return Math.max(low, 0);
    }

    /**
     * Return the text that is displayed by this Layout.
     */
    public final CharSequence getText() {
        return mText;
    }

    /**
     * Return the base Paint properties for this layout.
     * Do NOT change the paint, which may result in funny
     * drawing for this layout.
     */
    public final TextPaint getPaint() {
        return mPaint;
    }

    /**
     * Return the width of this layout.
     */
    public final int getWidth() {
        return mWidth;
    }

    /**
     * Return the width to which this Layout is ellipsizing, or
     * {@link #getWidth} if it is not doing anything special.
     */
    public int getEllipsizedWidth() {
        return mWidth;
    }

    /**
     * Increase the width of this layout to the specified width.
     * Be careful to use this only when you know it is appropriate&mdash;
     * it does not cause the text to reflow to use the full new width.
     */
    public final void increaseWidthTo(int wid) {
        if (wid < mWidth) {
            throw new RuntimeException("attempted to reduce Layout width");
        }

        mWidth = wid;
    }

    /**
     * Return the total height of this layout.
     */
    public int getHeight() {
        return getLineTop(getLineCount());
    }

    /**
     * Return the total height of this layout.
     *
     * @param cap if true and max lines is set, returns the height of the layout at the max lines.
     */
    public int getHeight(boolean cap) {
        return getHeight();
    }

    /**
     * Return the base alignment of this layout.
     */
    public final Alignment getAlignment() {
        return mAlignment;
    }

    /**
     * Return the heuristic used to determine paragraph text direction.
     */
    public final TextDirectionHeuristic getTextDirectionHeuristic() {
        return mTextDir;
    }

    /**
     * Return the number of lines of text in this layout.
     */
    public abstract int getLineCount();

    /**
     * Return the baseline for the specified line (0&hellip;getLineCount() - 1)
     * If bounds is not null, return the top, left, right, bottom extents
     * of the specified line in it.
     *
     * @param line   which line to examine (0..getLineCount() - 1)
     * @param bounds Optional. If not null, it returns the extent of the line
     * @return the Y-coordinate of the baseline
     */
    public int getLineBounds(int line, @Nullable Rect bounds) {
        if (bounds != null) {
            bounds.left = 0;     // ???
            bounds.top = getLineTop(line);
            bounds.right = mWidth;   // ???
            bounds.bottom = getLineTop(line + 1);
        }
        return getLineBaseline(line);
    }

    /**
     * Return the vertical position of the top of the specified line
     * (0&hellip;getLineCount()).
     * If the specified line is equal to the line count, returns the
     * bottom of the last line.
     */
    public abstract int getLineTop(int line);

    /**
     * Return the descent of the specified line(0&hellip;getLineCount() - 1).
     */
    public abstract int getLineDescent(int line);

    /**
     * Return the text offset of the beginning of the specified line (
     * 0&hellip;getLineCount()). If the specified line is equal to the line
     * count, returns the length of the text.
     */
    public abstract int getLineStart(int line);

    /**
     * Returns the primary directionality of the paragraph containing the
     * specified line, either 1 for left-to-right lines, or -1 for right-to-left
     * lines (see {@link #DIR_LEFT_TO_RIGHT}, {@link #DIR_RIGHT_TO_LEFT}).
     */
    public abstract int getParagraphDirection(int line);

    /**
     * Returns whether the specified line contains one or more
     * characters that need to be handled specially, like tabs.
     */
    public abstract boolean getLineContainsTab(int line);

    /**
     * Returns the directional run information for the specified line.
     * The array alternates counts of characters in left-to-right
     * and right-to-left segments of the line.
     *
     * <p>NOTE: this is inadequate to support bidirectional text, and will change.
     */
    public abstract Directions getLineDirections(int line);

    /**
     * Returns the (negative) number of extra pixels of ascent padding in the
     * top line of the Layout.
     */
    public abstract int getTopPadding();

    /**
     * Returns the number of extra pixels of descent padding in the
     * bottom line of the Layout.
     */
    public abstract int getBottomPadding();

    /**
     * Returns the left indent for a line.
     */
    public int getIndentAdjust(int line, Alignment alignment) {
        return 0;
    }

    /**
     * Return the offset of the first character to be ellipsized away,
     * relative to the start of the line.  (So 0 if the beginning of the
     * line is ellipsized, not getLineStart().)
     */
    public abstract int getEllipsisStart(int line);

    /**
     * Returns the number of characters to be ellipsized away, or 0 if
     * no ellipsis is to take place.
     */
    public abstract int getEllipsisCount(int line);

    /**
     * Gets the unsigned horizontal extent of the specified line, including
     * leading margin indent, but excluding trailing whitespace.
     */
    public float getLineMax(int line) {
        float margin = getParagraphLeadingMargin(line);
        float signedExtent = getLineExtent(line, false);
        return margin + (signedExtent >= 0 ? signedExtent : -signedExtent);
    }

    /**
     * Gets the unsigned horizontal extent of the specified line, including
     * leading margin indent and trailing whitespace.
     */
    public float getLineWidth(int line) {
        float margin = getParagraphLeadingMargin(line);
        float signedExtent = getLineExtent(line, true);
        return margin + (signedExtent >= 0 ? signedExtent : -signedExtent);
    }

    /**
     * Returns the signed horizontal extent of the specified line, excluding
     * leading margin.  If full is false, excludes trailing whitespace.
     *
     * @param line the index of the line
     * @param full whether to include trailing whitespace
     * @return the extent of the line
     */
    private float getLineExtent(int line, boolean full) {
        final int start = getLineStart(line);
        final int end = full ? getLineEnd(line) : getLineVisibleEnd(line);

        final boolean hasTabs = getLineContainsTab(line);
        TabStops tabStops = null;
        if (hasTabs && mText instanceof Spanned) {
            // Just checking this line should be good enough, tabs should be
            // consistent across all lines in a paragraph.
            TabStopSpan[] tabs = getParagraphSpans((Spanned) mText, start, end, TabStopSpan.class);
            if (tabs != null && tabs.length > 0) {
                tabStops = new TabStops(TAB_INCREMENT, tabs); // XXX should reuse
            }
        }
        final Directions directions = getLineDirections(line);
        // Returned directions can actually be null
        if (directions == null) {
            return 0f;
        }
        final int dir = getParagraphDirection(line);

        final TextLine tl = TextLine.obtain();
        final TextPaint paint = TextPaint.obtain();
        paint.set(mPaint);
        tl.set(paint, mText, start, end, dir, directions, hasTabs, tabStops,
                getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
        final float width = tl.metrics(null);
        tl.recycle();
        paint.recycle();
        return width;
    }


    /**
     * Returns the signed horizontal extent of the specified line, excluding
     * leading margin.  If full is false, excludes trailing whitespace.
     *
     * @param line     the index of the line
     * @param tabStops the tab stops, can be null if we know they're not used.
     * @param full     whether to include trailing whitespace
     * @return the extent of the text on this line
     */
    private float getLineExtent(int line, TabStops tabStops, boolean full) {
        final int start = getLineStart(line);
        final int end = full ? getLineEnd(line) : getLineVisibleEnd(line);
        final boolean hasTabs = getLineContainsTab(line);
        final Directions directions = getLineDirections(line);
        final int dir = getParagraphDirection(line);

        final TextLine tl = TextLine.obtain();
        final TextPaint paint = TextPaint.obtain();
        paint.set(mPaint);
        tl.set(paint, mText, start, end, dir, directions, hasTabs, tabStops,
                getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
        final float width = tl.metrics(null);
        tl.recycle();
        paint.recycle();
        return width;
    }

    /**
     * Return the text offset after the last character on the specified line.
     */
    public final int getLineEnd(int line) {
        return getLineStart(line + 1);
    }

    /**
     * Return the text offset after the last visible character (so whitespace
     * is not counted) on the specified line.
     */
    public int getLineVisibleEnd(int line) {
        return getLineVisibleEnd(line, getLineStart(line), getLineStart(line + 1));
    }

    private int getLineVisibleEnd(int line, int start, int end) {
        CharSequence text = mText;
        char ch;
        if (line == getLineCount() - 1) {
            return end;
        }

        for (; end > start; end--) {
            ch = text.charAt(end - 1);

            if (ch == '\n') {
                return end - 1;
            }

            if (!LineBreaker.isLineEndSpace(ch)) {
                break;
            }

        }

        return end;
    }

    /**
     * Return the vertical position of the bottom of the specified line.
     */
    public final int getLineBottom(int line) {
        return getLineTop(line + 1);
    }

    /**
     * Return the vertical position of the baseline of the specified line.
     */
    public final int getLineBaseline(int line) {
        // getLineTop(line+1) == getLineBottom(line)
        return getLineTop(line + 1) - getLineDescent(line);
    }

    /**
     * Get the ascent of the text on the specified line.
     * The return value is negative to match the Paint.ascent() convention.
     */
    public final int getLineAscent(int line) {
        // getLineTop(line+1) - getLineDescent(line) == getLineBaseLine(line)
        return getLineTop(line) - (getLineTop(line + 1) - getLineDescent(line));
    }

    /**
     * Get the alignment of the specified paragraph, taking into account
     * markup attached to it.
     */
    public final Alignment getParagraphAlignment(int line) {
        Alignment align = mAlignment;

        return align;
    }

    /**
     * Get the left edge of the specified paragraph, inset by left margins.
     */
    public final int getParagraphLeft(int line) {
        int left = 0;
        int dir = getParagraphDirection(line);
        if (dir == DIR_RIGHT_TO_LEFT || !mSpannedText) {
            return left; // leading margin has no impact, or no styles
        }
        return getParagraphLeadingMargin(line);
    }

    /**
     * Get the right edge of the specified paragraph, inset by right margins.
     */
    public final int getParagraphRight(int line) {
        int right = mWidth;
        int dir = getParagraphDirection(line);
        if (dir == DIR_LEFT_TO_RIGHT || !mSpannedText) {
            return right; // leading margin has no impact, or no styles
        }
        return right - getParagraphLeadingMargin(line);
    }

    /**
     * Checks if the trailing BiDi level should be used for an offset
     * <p>
     * This method is useful when the offset is at the BiDi level transition point and determine
     * which run need to be used. For example, let's think about following input: (L* denotes
     * Left-to-Right characters, R* denotes Right-to-Left characters.)
     * Input (Logical Order): L1 L2 L3 R1 R2 R3 L4 L5 L6
     * Input (Display Order): L1 L2 L3 R3 R2 R1 L4 L5 L6
     * <p>
     * Then, think about selecting the range (3, 6). The offset=3 and offset=6 are ambiguous here
     * since they are at the BiDi transition point.  In Android, the offset is considered to be
     * associated with the trailing run if the BiDi level of the trailing run is higher than of the
     * previous run.  In this case, the BiDi level of the input text is as follows:
     * <p>
     * Input (Logical Order): L1 L2 L3 R1 R2 R3 L4 L5 L6
     * BiDi Run: [ Run 0 ][ Run 1 ][ Run 2 ]
     * BiDi Level:  0  0  0  1  1  1  0  0  0
     * <p>
     * Thus, offset = 3 is part of Run 1 and this method returns true for offset = 3, since the BiDi
     * level of Run 1 is higher than the level of Run 0.  Similarly, the offset = 6 is a part of Run
     * 1 and this method returns false for the offset = 6 since the BiDi level of Run 1 is higher
     * than the level of Run 2.
     *
     * @return true if offset is at the BiDi level transition point and trailing BiDi level is
     * higher than previous BiDi level. See above for the detail.
     * @hide
     */
    public boolean primaryIsTrailingPrevious(int offset) {
        int line = getLineForOffset(offset);
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int[] runs = getLineDirections(line).mDirections;

        int levelAt = -1;
        for (int i = 0; i < runs.length; i += 2) {
            int start = lineStart + runs[i];
            int limit = start + (runs[i + 1] & Directions.RUN_LENGTH_MASK);
            if (limit > lineEnd) {
                limit = lineEnd;
            }
            if (offset >= start && offset < limit) {
                if (offset > start) {
                    // Previous character is at same level, so don't use trailing.
                    return false;
                }
                levelAt = (runs[i + 1] >>> Directions.RUN_LEVEL_SHIFT) & Directions.RUN_LEVEL_MASK;
                break;
            }
        }
        if (levelAt == -1) {
            // Offset was limit of line.
            levelAt = getParagraphDirection(line) == 1 ? 0 : 1;
        }

        // At level boundary, check previous level.
        int levelBefore = -1;
        if (offset == lineStart) {
            levelBefore = getParagraphDirection(line) == 1 ? 0 : 1;
        } else {
            offset -= 1;
            for (int i = 0; i < runs.length; i += 2) {
                int start = lineStart + runs[i];
                int limit = start + (runs[i + 1] & Directions.RUN_LENGTH_MASK);
                if (limit > lineEnd) {
                    limit = lineEnd;
                }
                if (offset >= start && offset < limit) {
                    levelBefore = (runs[i + 1] >>> Directions.RUN_LEVEL_SHIFT) & Directions.RUN_LEVEL_MASK;
                    break;
                }
            }
        }

        return levelBefore < levelAt;
    }

    /**
     * Computes in linear time the results of calling
     * #primaryIsTrailingPrevious for all offsets on a line.
     *
     * @param line The line giving the offsets we compute the information for
     * @return The array of results, indexed from 0, where 0 corresponds to the line start offset
     * @hide
     */
    public boolean[] primaryIsTrailingPreviousAllLineOffsets(int line) {
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int[] runs = getLineDirections(line).mDirections;

        boolean[] trailing = new boolean[lineEnd - lineStart + 1];

        byte[] level = new byte[lineEnd - lineStart + 1];
        for (int i = 0; i < runs.length; i += 2) {
            int start = lineStart + runs[i];
            int limit = start + (runs[i + 1] & Directions.RUN_LENGTH_MASK);
            if (limit > lineEnd) {
                limit = lineEnd;
            }
            if (limit == start) {
                continue;
            }
            level[limit - lineStart - 1] =
                    (byte) ((runs[i + 1] >>> Directions.RUN_LEVEL_SHIFT) & Directions.RUN_LEVEL_MASK);
        }

        for (int i = 0; i < runs.length; i += 2) {
            int start = lineStart + runs[i];
            byte currentLevel = (byte) ((runs[i + 1] >>> Directions.RUN_LEVEL_SHIFT) & Directions.RUN_LEVEL_MASK);
            trailing[start - lineStart] = currentLevel > (start == lineStart
                    ? (getParagraphDirection(line) == 1 ? 0 : 1)
                    : level[start - lineStart - 1]);
        }

        return trailing;
    }

    /**
     * Get the primary horizontal position for the specified text offset.
     * This is the location where a new character would be inserted in
     * the paragraph's primary direction.
     */
    public float getPrimaryHorizontal(int offset) {
        return getPrimaryHorizontal(offset, false /* not clamped */);
    }

    /**
     * Get the primary horizontal position for the specified text offset, but
     * optionally clamp it so that it doesn't exceed the width of the layout.
     *
     * @hide
     */
    public float getPrimaryHorizontal(int offset, boolean clamped) {
        boolean trailing = primaryIsTrailingPrevious(offset);
        return getHorizontal(offset, trailing, clamped);
    }

    /**
     * Get the secondary horizontal position for the specified text offset.
     * This is the location where a new character would be inserted in
     * the direction other than the paragraph's primary direction.
     */
    public float getSecondaryHorizontal(int offset) {
        return getSecondaryHorizontal(offset, false /* not clamped */);
    }

    /**
     * Get the secondary horizontal position for the specified text offset, but
     * optionally clamp it so that it doesn't exceed the width of the layout.
     *
     * @hide
     */
    public float getSecondaryHorizontal(int offset, boolean clamped) {
        boolean trailing = primaryIsTrailingPrevious(offset);
        return getHorizontal(offset, !trailing, clamped);
    }

    private float getHorizontal(int offset, boolean primary) {
        return primary ? getPrimaryHorizontal(offset) : getSecondaryHorizontal(offset);
    }

    private float getHorizontal(int offset, boolean trailing, boolean clamped) {
        int line = getLineForOffset(offset);

        return getHorizontal(offset, trailing, line, clamped);
    }

    private float getHorizontal(int offset, boolean trailing, int line, boolean clamped) {
        int start = getLineStart(line);
        int end = getLineEnd(line);
        int dir = getParagraphDirection(line);
        boolean hasTab = getLineContainsTab(line);
        Directions directions = getLineDirections(line);

        TabStops tabStops = null;
        if (hasTab && mText instanceof Spanned) {
            // Just checking this line should be good enough, tabs should be
            // consistent across all lines in a paragraph.
            TabStopSpan[] tabs = getParagraphSpans((Spanned) mText, start, end, TabStopSpan.class);
            if (tabs != null && tabs.length > 0) {
                tabStops = new TabStops(TAB_INCREMENT, tabs); // XXX should reuse
            }
        }

        TextLine tl = TextLine.obtain();
        tl.set(mPaint, mText, start, end, dir, directions, hasTab, tabStops,
                getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
        float wid = tl.measure(offset - start, trailing, null);
        tl.recycle();

        if (clamped && wid > mWidth) {
            wid = mWidth;
        }
        int left = getParagraphLeft(line);
        int right = getParagraphRight(line);

        return getLineStartPos(line, left, right) + wid;
    }

    /**
     * Computes in linear time the results of calling #getHorizontal for all offsets on a line.
     *
     * @param line    The line giving the offsets we compute information for
     * @param clamped Whether to clamp the results to the width of the layout
     * @param primary Whether the results should be the primary or the secondary horizontal
     * @return The array of results, indexed from 0, where 0 corresponds to the line start offset
     */
    private float[] getLineHorizontals(int line, boolean clamped, boolean primary) {
        int start = getLineStart(line);
        int end = getLineEnd(line);
        int dir = getParagraphDirection(line);
        boolean hasTab = getLineContainsTab(line);
        Directions directions = getLineDirections(line);

        TabStops tabStops = null;
        if (hasTab && mText instanceof Spanned) {
            // Just checking this line should be good enough, tabs should be
            // consistent across all lines in a paragraph.
            TabStopSpan[] tabs = getParagraphSpans((Spanned) mText, start, end, TabStopSpan.class);
            if (tabs != null && tabs.length > 0) {
                tabStops = new TabStops(TAB_INCREMENT, tabs); // XXX should reuse
            }
        }

        TextLine tl = TextLine.obtain();
        tl.set(mPaint, mText, start, end, dir, directions, hasTab, tabStops,
                getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
        boolean[] trailings = primaryIsTrailingPreviousAllLineOffsets(line);
        if (!primary) {
            for (int offset = 0; offset < trailings.length; ++offset) {
                trailings[offset] = !trailings[offset];
            }
        }
        float[] wid = tl.measureAllOffsets(trailings, null);
        tl.recycle();

        if (clamped) {
            for (int offset = 0; offset < wid.length; ++offset) {
                if (wid[offset] > mWidth) {
                    wid[offset] = mWidth;
                }
            }
        }
        int left = getParagraphLeft(line);
        int right = getParagraphRight(line);

        int lineStartPos = getLineStartPos(line, left, right);
        float[] horizontal = new float[end - start + 1];
        for (int offset = 0; offset < horizontal.length; ++offset) {
            horizontal[offset] = lineStartPos + wid[offset];
        }
        return horizontal;
    }

    /**
     * Get the character offset on the specified line whose position is
     * closest to the specified horizontal position.
     */
    public int getOffsetForHorizontal(int line, float horiz) {
        return getOffsetForHorizontal(line, horiz, true);
    }

    /**
     * Get the character offset on the specified line whose position is
     * closest to the specified horizontal position.
     *
     * @param line    the line used to find the closest offset
     * @param horiz   the horizontal position used to find the closest offset
     * @param primary whether to use the primary position or secondary position to find the offset
     * @hide
     */
    public int getOffsetForHorizontal(int line, float horiz, boolean primary) {
        // TODO: use Paint.getOffsetForAdvance to avoid binary search
        final int lineEndOffset = getLineEnd(line);
        final int lineStartOffset = getLineStart(line);

        Directions dirs = getLineDirections(line);

        TextLine tl = TextLine.obtain();
        // XXX: we don't care about tabs as we just use TextLine#getOffsetToLeftRightOf here.
        tl.set(mPaint, mText, lineStartOffset, lineEndOffset, getParagraphDirection(line), dirs,
                false, null,
                getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
        final HorizontalMeasurementProvider horizontal =
                new HorizontalMeasurementProvider(line, primary);

        final int max;
        if (line == getLineCount() - 1) {
            max = lineEndOffset;
        } else {
            max = tl.getOffsetToLeftRightOf(lineEndOffset - lineStartOffset,
                    !isRtlCharAt(lineEndOffset - 1)) + lineStartOffset;
        }
        int best = lineStartOffset;
        float bestdist = Math.abs(horizontal.get(lineStartOffset) - horiz);

        for (int i = 0; i < dirs.mDirections.length; i += 2) {
            int here = lineStartOffset + dirs.mDirections[i];
            int there = here + (dirs.mDirections[i + 1] & Directions.RUN_LENGTH_MASK);
            boolean isRtl = (dirs.mDirections[i + 1] & Directions.RUN_RTL_FLAG) != 0;
            int swap = isRtl ? -1 : 1;

            if (there > max)
                there = max;
            int high = there - 1 + 1, low = here + 1 - 1, guess;

            while (high - low > 1) {
                guess = (high + low) / 2;
                int adguess = getOffsetAtStartOf(guess);

                if (horizontal.get(adguess) * swap >= horiz * swap) {
                    high = guess;
                } else {
                    low = guess;
                }
            }

            if (low < here + 1)
                low = here + 1;

            if (low < there) {
                int aft = tl.getOffsetToLeftRightOf(low - lineStartOffset, isRtl) + lineStartOffset;
                low = tl.getOffsetToLeftRightOf(aft - lineStartOffset, !isRtl) + lineStartOffset;
                if (low >= here && low < there) {
                    float dist = Math.abs(horizontal.get(low) - horiz);
                    if (aft < there) {
                        float other = Math.abs(horizontal.get(aft) - horiz);

                        if (other < dist) {
                            dist = other;
                            low = aft;
                        }
                    }

                    if (dist < bestdist) {
                        bestdist = dist;
                        best = low;
                    }
                }
            }

            float dist = Math.abs(horizontal.get(here) - horiz);

            if (dist < bestdist) {
                bestdist = dist;
                best = here;
            }
        }

        float dist = Math.abs(horizontal.get(max) - horiz);

        if (dist <= bestdist) {
            best = max;
        }

        tl.recycle();
        return best;
    }

    /**
     * Responds to #getHorizontal queries, by selecting the better strategy between:
     * - calling #getHorizontal explicitly for each query
     * - precomputing all #getHorizontal measurements, and responding to any query in constant time
     * The first strategy is used for LTR-only text, while the second is used for all other cases.
     * The class is currently only used in #getOffsetForHorizontal, so reuse with care in other
     * contexts.
     */
    private class HorizontalMeasurementProvider {

        private final int mLine;
        private final boolean mPrimary;

        private float[] mHorizontals;
        private int mLineStartOffset;

        HorizontalMeasurementProvider(final int line, final boolean primary) {
            mLine = line;
            mPrimary = primary;
            init();
        }

        private void init() {
            final Directions dirs = getLineDirections(mLine);
            if (dirs == Directions.ALL_LEFT_TO_RIGHT) {
                return;
            }

            mHorizontals = getLineHorizontals(mLine, false, mPrimary);
            mLineStartOffset = getLineStart(mLine);
        }

        float get(final int offset) {
            final int index = offset - mLineStartOffset;
            if (mHorizontals == null || index < 0 || index >= mHorizontals.length) {
                return getHorizontal(offset, mPrimary);
            } else {
                return mHorizontals[index];
            }
        }
    }

    /**
     * Return the start position of the line, given the left and right bounds
     * of the margins.
     *
     * @param line  the line index
     * @param left  the left bounds (0, or leading margin if ltr para)
     * @param right the right bounds (width, minus leading margin if rtl para)
     * @return the start position of the line (to right of line if rtl para)
     */
    private int getLineStartPos(int line, int left, int right) {
        // Adjust the point at which to start rendering depending on the
        // alignment of the paragraph.
        Alignment align = getParagraphAlignment(line);
        int dir = getParagraphDirection(line);

        if (align == Alignment.ALIGN_LEFT) {
            align = (dir == DIR_LEFT_TO_RIGHT) ? Alignment.ALIGN_NORMAL : Alignment.ALIGN_OPPOSITE;
        } else if (align == Alignment.ALIGN_RIGHT) {
            align = (dir == DIR_LEFT_TO_RIGHT) ? Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL;
        }

        int x;
        if (align == Alignment.ALIGN_NORMAL) {
            if (dir == DIR_LEFT_TO_RIGHT) {
                x = left + getIndentAdjust(line, Alignment.ALIGN_LEFT);
            } else {
                x = right + getIndentAdjust(line, Alignment.ALIGN_RIGHT);
            }
        } else {
            TabStops tabStops = null;
            if (mSpannedText && getLineContainsTab(line)) {
                Spanned spanned = (Spanned) mText;
                int start = getLineStart(line);
                int spanEnd = spanned.nextSpanTransition(start, spanned.length(),
                        TabStopSpan.class);
                TabStopSpan[] tabSpans = getParagraphSpans(spanned, start, spanEnd,
                        TabStopSpan.class);
                if (tabSpans != null && tabSpans.length > 0) {
                    tabStops = new TabStops(TAB_INCREMENT, tabSpans);
                }
            }
            int max = (int) getLineExtent(line, tabStops, false);
            if (align == Alignment.ALIGN_OPPOSITE) {
                if (dir == DIR_LEFT_TO_RIGHT) {
                    x = right - max + getIndentAdjust(line, Alignment.ALIGN_RIGHT);
                } else {
                    // max is negative here
                    x = left - max + getIndentAdjust(line, Alignment.ALIGN_LEFT);
                }
            } else { // Alignment.ALIGN_CENTER
                max = max & ~1;
                x = (left + right - max) >> 1 + getIndentAdjust(line, Alignment.ALIGN_CENTER);
            }
        }
        return x;
    }


    /**
     * Returns true if the character at offset and the preceding character
     * are at different run levels (and thus there's a split caret).
     *
     * @param offset the offset
     * @return true if at a level boundary
     * @hide
     */
    public boolean isLevelBoundary(int offset) {
        int line = getLineForOffset(offset);
        Directions dirs = getLineDirections(line);
        if (dirs == Directions.ALL_LEFT_TO_RIGHT || dirs == Directions.ALL_RIGHT_TO_LEFT) {
            return false;
        }

        int[] runs = dirs.mDirections;
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        if (offset == lineStart || offset == lineEnd) {
            int paraLevel = getParagraphDirection(line) == 1 ? 0 : 1;
            int runIndex = offset == lineStart ? 0 : runs.length - 2;
            return ((runs[runIndex + 1] >>> Directions.RUN_LEVEL_SHIFT) & Directions.RUN_LEVEL_MASK) != paraLevel;
        }

        offset -= lineStart;
        for (int i = 0; i < runs.length; i += 2) {
            if (offset == runs[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the character at offset is right to left (RTL).
     *
     * @param offset the offset
     * @return true if the character is RTL, false if it is LTR
     */
    public boolean isRtlCharAt(int offset) {
        int line = getLineForOffset(offset);
        Directions dirs = getLineDirections(line);
        if (dirs == Directions.ALL_LEFT_TO_RIGHT) {
            return false;
        }
        if (dirs == Directions.ALL_RIGHT_TO_LEFT) {
            return true;
        }
        int[] runs = dirs.mDirections;
        int lineStart = getLineStart(line);
        for (int i = 0; i < runs.length; i += 2) {
            int start = lineStart + runs[i];
            int limit = start + (runs[i + 1] & Directions.RUN_LENGTH_MASK);
            if (offset >= start && offset < limit) {
                int level = (runs[i + 1] >>> Directions.RUN_LEVEL_SHIFT) & Directions.RUN_LEVEL_MASK;
                return ((level & 1) != 0);
            }
        }
        // Should happen only if the offset is "out of bounds"
        return false;
    }

    private int getOffsetAtStartOf(int offset) {
        // XXX this probably should skip local reorderings and
        // zero-width characters, look at callers
        if (offset == 0)
            return 0;

        CharSequence text = mText;
        char c = text.charAt(offset);

        if (c >= '\uDC00' && c <= '\uDFFF') {
            char c1 = text.charAt(offset - 1);

            if (c1 >= '\uD800' && c1 <= '\uDBFF')
                offset -= 1;
        }

        if (mSpannedText) {
            ReplacementSpan[] spans = ((Spanned) text).getSpans(offset, offset,
                    ReplacementSpan.class);

            if (spans != null) {
                for (ReplacementSpan span : spans) {
                    int start = ((Spanned) text).getSpanStart(span);
                    int end = ((Spanned) text).getSpanEnd(span);

                    if (start < offset && end > offset)
                        offset = start;
                }
            }
        }

        return offset;
    }

    public int getOffsetToLeftOf(int offset) {
        return getOffsetToLeftRightOf(offset, true);
    }

    public int getOffsetToRightOf(int offset) {
        return getOffsetToLeftRightOf(offset, false);
    }

    private int getOffsetToLeftRightOf(int caret, boolean toLeft) {
        int line = getLineForOffset(caret);
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int lineDir = getParagraphDirection(line);

        boolean lineChanged = false;
        boolean advance = toLeft == (lineDir == DIR_RIGHT_TO_LEFT);
        // if walking off line, look at the line we're headed to
        if (advance) {
            if (caret == lineEnd) {
                if (line < getLineCount() - 1) {
                    lineChanged = true;
                    ++line;
                } else {
                    return caret; // at very end, don't move
                }
            }
        } else {
            if (caret == lineStart) {
                if (line > 0) {
                    lineChanged = true;
                    --line;
                } else {
                    return caret; // at very start, don't move
                }
            }
        }

        if (lineChanged) {
            lineStart = getLineStart(line);
            lineEnd = getLineEnd(line);
            int newDir = getParagraphDirection(line);
            if (newDir != lineDir) {
                // unusual case.  we want to walk onto the line, but it runs
                // in a different direction than this one, so we fake movement
                // in the opposite direction.
                toLeft = !toLeft;
                lineDir = newDir;
            }
        }

        Directions directions = getLineDirections(line);

        TextLine tl = TextLine.obtain();
        // XXX: we don't care about tabs
        tl.set(mPaint, mText, lineStart, lineEnd, lineDir, directions, false, null,
                getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
        caret = lineStart + tl.getOffsetToLeftRightOf(caret - lineStart, toLeft);
        tl.recycle();
        return caret;
    }

    /**
     * Returns the effective leading margin (unsigned) for this line,
     * taking into account LeadingMarginSpan and LeadingMarginSpan2.
     *
     * @param line the line index
     * @return the leading margin of this line
     */
    private int getParagraphLeadingMargin(int line) {
        //TODO
        return 0;
    }

    /**
     * Determine whether we should clamp cursor position. Currently it's
     * only robust for left-aligned displays.
     */
    private boolean shouldClampCursor(int line) {
        // Only clamp cursor position in left-aligned displays.
        return switch (getParagraphAlignment(line)) {
            case ALIGN_LEFT -> true;
            case ALIGN_NORMAL -> getParagraphDirection(line) > 0;
            default -> false;
        };

    }

    /**
     * Get the leftmost position that should be exposed for horizontal
     * scrolling on the specified line.
     */
    public float getLineLeft(int line) {
        final int dir = getParagraphDirection(line);
        Alignment align = getParagraphAlignment(line);
        // Before Q, StaticLayout.Builder.setAlignment didn't check whether the input alignment
        // is null. And when it is null, the old behavior is the same as ALIGN_CENTER.
        // To keep consistency, we convert a null alignment to ALIGN_CENTER.
        if (align == null) {
            align = Alignment.ALIGN_CENTER;
        }

        // First convert combinations of alignment and direction settings to
        // three basic cases: ALIGN_LEFT, ALIGN_RIGHT and ALIGN_CENTER.
        // For unexpected cases, it will fallback to ALIGN_LEFT.
        final Alignment resultAlign;
        switch (align) {
            case ALIGN_NORMAL:
                resultAlign =
                        dir == DIR_RIGHT_TO_LEFT ? Alignment.ALIGN_RIGHT : Alignment.ALIGN_LEFT;
                break;
            case ALIGN_OPPOSITE:
                resultAlign =
                        dir == DIR_RIGHT_TO_LEFT ? Alignment.ALIGN_LEFT : Alignment.ALIGN_RIGHT;
                break;
            case ALIGN_CENTER:
                resultAlign = Alignment.ALIGN_CENTER;
                break;
            case ALIGN_RIGHT:
                resultAlign = Alignment.ALIGN_RIGHT;
                break;
            default: /* align == Alignment.ALIGN_LEFT */
                resultAlign = Alignment.ALIGN_LEFT;
        }

        // Here we must use getLineMax() to do the computation, because it maybe overridden by
        // derived class. And also note that line max equals the width of the text in that line
        // plus the leading margin.
        switch (resultAlign) {
            case ALIGN_CENTER:
                final int left = getParagraphLeft(line);
                final float max = getLineMax(line);
                // This computation only works when mWidth equals leadingMargin plus
                // the width of text in this line. If this condition doesn't meet anymore,
                // please change here too.
                return (float) Math.floor(left + (mWidth - max) / 2);
            case ALIGN_RIGHT:
                return mWidth - getLineMax(line);
            default: /* resultAlign == Alignment.ALIGN_LEFT */
                return 0;
        }
    }

    /**
     * Get the rightmost position that should be exposed for horizontal
     * scrolling on the specified line.
     */
    public float getLineRight(int line) {
        final int dir = getParagraphDirection(line);
        Alignment align = getParagraphAlignment(line);
        // Before Q, StaticLayout.Builder.setAlignment didn't check whether the input alignment
        // is null. And when it is null, the old behavior is the same as ALIGN_CENTER.
        // To keep consistency, we convert a null alignment to ALIGN_CENTER.
        if (align == null) {
            align = Alignment.ALIGN_CENTER;
        }

        final Alignment resultAlign;
        switch (align) {
            case ALIGN_NORMAL:
                resultAlign =
                        dir == DIR_RIGHT_TO_LEFT ? Alignment.ALIGN_RIGHT : Alignment.ALIGN_LEFT;
                break;
            case ALIGN_OPPOSITE:
                resultAlign =
                        dir == DIR_RIGHT_TO_LEFT ? Alignment.ALIGN_LEFT : Alignment.ALIGN_RIGHT;
                break;
            case ALIGN_CENTER:
                resultAlign = Alignment.ALIGN_CENTER;
                break;
            case ALIGN_RIGHT:
                resultAlign = Alignment.ALIGN_RIGHT;
                break;
            default: /* align == Alignment.ALIGN_LEFT */
                resultAlign = Alignment.ALIGN_LEFT;
        }

        switch (resultAlign) {
            case ALIGN_CENTER:
                final int right = getParagraphRight(line);
                final float max = getLineMax(line);
                // This computation only works when mWidth equals leadingMargin plus width of the
                // text in this line. If this condition doesn't meet anymore, please change here.
                return (float) Math.ceil(right - (mWidth - max) / 2);
            case ALIGN_RIGHT:
                return mWidth;
            default: /* resultAlign == Alignment.ALIGN_LEFT */
                return getLineMax(line);
        }
    }

    /**
     * Fills in the specified Path with a representation of a cursor
     * at the specified offset.  This will often be a vertical line
     * but can be multiple discontinuous lines in text with multiple
     * directionalities.
     *
     * @param point  the cursor offset in chars
     * @param dest   the destination lines
     * @param buffer the editing buffer
     */
    public void getCursorPath(int point, @Nonnull FloatArrayList dest, @Nonnull CharSequence buffer) {
        dest.clear();
        if (point < 0) {
            return;
        }

        int line = getLineForOffset(point);
        int top = getLineTop(line);
        int bottom = getLineBottom(line);

        boolean clamped = shouldClampCursor(line);
        float h1 = getPrimaryHorizontal(point, clamped) - 0.5f;

        int caps = TextKeyListener.getMetaState(buffer, KeyEvent.META_SHIFT_ON);
        int fn = TextKeyListener.getMetaState(buffer, KeyEvent.META_ALT_ON);
        int dist = (bottom - top) >> 3;
        top += dist;
        bottom -= dist;

        if (caps != 0 || fn != 0) {
            dist = (bottom - top) >> 2;

            if (fn != 0)
                top += dist;
            if (caps != 0)
                bottom -= dist;
        } else {
            dist = 0;
        }

        if (h1 < 0.5f)
            h1 = 0.5f;

        dest.add(h1);
        dest.add(top);
        dest.add(h1);
        dest.add(bottom);

        if (caps == 1) {
            dest.add(h1);
            dest.add(bottom);
            dest.add(h1 - dist * 0.7f);
            dest.add(bottom + dist);

            dest.add(h1 - dist * 0.7f);
            dest.add(bottom + dist - 0.5f);
            dest.add(h1 + dist * 0.7f);
            dest.add(bottom + dist - 0.5f);

            dest.add(h1 + dist * 0.7f);
            dest.add(bottom + dist);
            dest.add(h1);
            dest.add(bottom);
        }

        if (fn == 1) {
            dest.add(h1);
            dest.add(top);
            dest.add(h1 - dist * 0.7f);
            dest.add(top - dist);

            dest.add(h1 - dist * 0.7f);
            dest.add(top - dist + 0.5f);
            dest.add(h1 + dist * 0.7f);
            dest.add(top - dist + 0.5f);

            dest.add(h1 + dist * 0.7f);
            dest.add(top - dist);
            dest.add(h1);
            dest.add(top);
        }
    }

    private void addSelection(int line, int start, int end,
                              int top, int bottom, FloatArrayList out) {
        int linestart = getLineStart(line);
        int lineend = getLineEnd(line);
        Directions dirs = getLineDirections(line);

        if (lineend > linestart && mText.charAt(lineend - 1) == '\n') {
            lineend--;
        }

        for (int i = 0; i < dirs.mDirections.length; i += 2) {
            int here = linestart + dirs.mDirections[i];
            int there = here + (dirs.mDirections[i + 1] & Directions.RUN_LENGTH_MASK);

            if (there > lineend) {
                there = lineend;
            }

            if (start <= there && end >= here) {
                int st = Math.max(start, here);
                int en = Math.min(end, there);

                if (st != en) {
                    float h1 = getHorizontal(st, false, line, false /* not clamped */);
                    float h2 = getHorizontal(en, true, line, false /* not clamped */);

                    float left = Math.min(h1, h2);
                    float right = Math.max(h1, h2);

                    out.add(left);
                    out.add(top);
                    out.add(right);
                    out.add(bottom);
                }
            }
        }
    }

    /**
     * Calculates the rectangles which should be highlighted to indicate a selection between start
     * and end and feeds them into the given array.
     *
     * @param start the starting index of the selection
     * @param end   the ending index of the selection
     * @param dest  the destination rectangles
     */
    public void getSelectionPath(int start, int end, @Nonnull FloatArrayList dest) {
        dest.clear();
        if (start == end) {
            return;
        }

        if (end < start) {
            int temp = end;
            end = start;
            start = temp;
        }

        final int startline = getLineForOffset(start);
        final int endline = getLineForOffset(end);

        int top = getLineTop(startline);
        int bottom = getLineBottom(endline);

        if (startline == endline) {
            addSelection(startline, start, end, top, bottom, dest);
        } else {
            final float width = mWidth;

            addSelection(startline, start, getLineEnd(startline),
                    top, getLineBottom(startline), dest);

            if (getParagraphDirection(startline) == DIR_RIGHT_TO_LEFT) {
                dest.add(0);
                dest.add(top);
                dest.add(getLineLeft(startline));
                dest.add(getLineBottom(startline));
            } else {
                dest.add(getLineRight(startline));
                dest.add(top);
                dest.add(width);
                dest.add(getLineBottom(startline));
            }

            for (int i = startline + 1; i < endline; i++) {
                top = getLineTop(i);
                bottom = getLineBottom(i);
                dest.add(0);
                dest.add(top);
                dest.add(width);
                dest.add(bottom);
            }

            top = getLineTop(endline);
            bottom = getLineBottom(endline);

            addSelection(endline, getLineStart(endline), end, top, bottom, dest);

            if (getParagraphDirection(endline) == DIR_RIGHT_TO_LEFT) {
                dest.add(getLineRight(endline));
                dest.add(top);
                dest.add(width);
                dest.add(bottom);
            } else {
                dest.add(0);
                dest.add(top);
                dest.add(getLineLeft(endline));
                dest.add(bottom);
            }
        }
    }

    /**
     * Return how wide a layout must be in order to display the specified text with one line per
     * paragraph.
     *
     * <p>As of O, Uses
     * {@link TextDirectionHeuristics#FIRSTSTRONG_LTR} as the default text direction heuristics. In
     * the earlier versions uses {@link TextDirectionHeuristics#LTR} as the default.</p>
     */
    public static float getDesiredWidth(CharSequence source,
                                        TextPaint paint) {
        return getDesiredWidth(source, 0, source.length(), paint);
    }

    /**
     * Return how wide a layout must be in order to display the specified text slice with one
     * line per paragraph.
     *
     * <p>As of O, Uses
     * {@link TextDirectionHeuristics#FIRSTSTRONG_LTR} as the default text direction heuristics. In
     * the earlier versions uses {@link TextDirectionHeuristics#LTR} as the default.</p>
     */
    public static float getDesiredWidth(CharSequence source, int start, int end, TextPaint paint) {
        return getDesiredWidth(source, start, end, paint, TextDirectionHeuristics.FIRSTSTRONG_LTR);
    }

    /**
     * Return how wide a layout must be in order to display the
     * specified text slice with one line per paragraph.
     *
     * @hide
     */
    public static float getDesiredWidth(CharSequence source, int start, int end, TextPaint paint,
                                        TextDirectionHeuristic textDir) {
        return getDesiredWidthWithLimit(source, start, end, paint, textDir, Float.MAX_VALUE);
    }

    /**
     * Return how wide a layout must be in order to display the
     * specified text slice with one line per paragraph.
     * <p>
     * If the measured width exceeds given limit, returns limit value instead.
     *
     * @hide
     */
    public static float getDesiredWidthWithLimit(CharSequence source, int start, int end,
                                                 TextPaint paint, TextDirectionHeuristic textDir, float upperLimit) {
        float need = 0;

        int next;
        for (int i = start; i <= end; i = next) {
            next = TextUtils.indexOf(source, '\n', i, end);

            if (next < 0)
                next = end;

            // note, omits trailing paragraph char
            float w = measurePara(paint, source, i, next, textDir);
            if (w > upperLimit) {
                return upperLimit;
            }

            if (w > need)
                need = w;

            next++;
        }

        return need;
    }

    private static float measurePara(TextPaint paint, CharSequence text, int start, int end,
                                     TextDirectionHeuristic textDir) {
        MeasuredParagraph mt = null;
        TextLine tl = TextLine.obtain();
        try {
            mt = MeasuredParagraph.buildForBidi(text, start, end, textDir, null);
            final char[] chars = mt.getChars();
            final int len = chars.length;
            final Directions directions = mt.getDirections(0, len);
            final int dir = mt.getParagraphDir();
            boolean hasTabs = false;
            TabStops tabStops = null;
            // leading margins should be taken into account when measuring a paragraph
            int margin = 0;
            /*if (text instanceof Spanned) {
                Spanned spanned = (Spanned) text;
                LeadingMarginSpan[] spans = getParagraphSpans(spanned, start, end,
                        LeadingMarginSpan.class);
                for (LeadingMarginSpan lms : spans) {
                    margin += lms.getLeadingMargin(true);
                }
            }*/
            for (char c : chars) {
                if (c == '\t') {
                    hasTabs = true;
                    if (text instanceof Spanned spanned) {
                        int spanEnd = spanned.nextSpanTransition(start, end,
                                TabStopSpan.class);
                        TabStopSpan[] spans = getParagraphSpans(spanned, start, spanEnd,
                                TabStopSpan.class);
                        if (spans != null && spans.length > 0) {
                            tabStops = new TabStops(TAB_INCREMENT, spans);
                        }
                    }
                    break;
                }
            }
            tl.set(paint, text, start, end, dir, directions, hasTabs, tabStops,
                    0 /* ellipsisStart */, 0 /* ellipsisEnd */);
            return margin + Math.abs(tl.metrics(null));
        } finally {
            tl.recycle();
            if (mt != null) {
                mt.recycle();
            }
        }
    }

    /**
     * Returns the same as <code>text.getSpans()</code>, except where
     * <code>start</code> and <code>end</code> are the same and are not
     * at the very beginning of the text, in which case an empty array
     * is returned instead.
     * <p>
     * This is needed because of the special case that <code>getSpans()</code>
     * on an empty range returns the spans adjacent to that range, which is
     * primarily for the sake of <code>TextWatchers</code> so they will get
     * notifications when text goes from empty to non-empty.  But it also
     * has the unfortunate side effect that if the text ends with an empty
     * paragraph, that paragraph accidentally picks up the styles of the
     * preceding paragraph (even though those styles will not be picked up
     * by new text that is inserted into the empty paragraph).
     * <p>
     * The reason it just checks whether <code>start</code> and <code>end</code>
     * is the same is that the only time a line can contain 0 characters
     * is if it is the final paragraph of the Layout; otherwise any line will
     * contain at least one printing or newline character.  The reason for the
     * additional check if <code>start</code> is greater than 0 is that
     * if the empty paragraph is the entire content of the buffer, paragraph
     * styles that are already applied to the buffer will apply to text that
     * is inserted into it.
     */
    @Nullable
    static <T> T[] getParagraphSpans(@Nonnull Spanned text, int start, int end, Class<T> type) {
        if (start == end && start > 0) {
            return null;
        }

        if (text instanceof SpannableStringBuilder) {
            return ((SpannableStringBuilder) text).getSpans(start, end, type, false, null);
        } else {
            return text.getSpans(start, end, type, null);
        }
    }

    private void ellipsize(int start, int end, int line,
                           char[] dest, int destoff, TextUtils.TruncateAt method) {
        final int ellipsisCount = getEllipsisCount(line);
        if (ellipsisCount == 0) {
            return;
        }
        final int ellipsisStart = getEllipsisStart(line);
        final int lineStart = getLineStart(line);

        final String ellipsisString = TextUtils.getEllipsisString(method);
        final int ellipsisStringLen = ellipsisString.length();
        // Use the ellipsis string only if there are that at least as many characters to replace.
        final boolean useEllipsisString = ellipsisCount >= ellipsisStringLen;
        final int min = Math.max(0, start - ellipsisStart - lineStart);
        final int max = Math.min(ellipsisCount, end - ellipsisStart - lineStart);

        for (int i = min; i < max; i++) {
            final char c;
            if (useEllipsisString && i < ellipsisStringLen) {
                c = ellipsisString.charAt(i);
            } else {
                c = TextUtils.ELLIPSIS_FILLER;
            }

            final int a = i + ellipsisStart + lineStart;
            dest[destoff + a - start] = c;
        }
    }

    public enum Alignment {
        ALIGN_NORMAL,
        ALIGN_OPPOSITE,
        ALIGN_CENTER,
        // internal use
        ALIGN_LEFT,
        // internal use
        ALIGN_RIGHT
    }

    static class Ellipsizer implements CharSequence, GetChars {

        CharSequence mText;
        Layout mLayout;
        int mWidth;
        TextUtils.TruncateAt mMethod;

        public Ellipsizer(CharSequence s) {
            mText = s;
        }

        @Override
        public char charAt(int off) {
            char[] buf = TextUtils.obtain(1);
            getChars(off, off + 1, buf, 0);
            char ret = buf[0];

            TextUtils.recycle(buf);
            return ret;
        }

        @Override
        public void getChars(int start, int end, char[] dest, int destoff) {
            int line1 = mLayout.getLineForOffset(start);
            int line2 = mLayout.getLineForOffset(end);

            TextUtils.getChars(mText, start, end, dest, destoff);

            for (int i = line1; i <= line2; i++) {
                mLayout.ellipsize(start, end, i, dest, destoff, mMethod);
            }
        }

        @Override
        public int length() {
            return mText.length();
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            char[] s = new char[end - start];
            getChars(start, end, s, 0);
            return new String(s);
        }

        @Override
        public String toString() {
            char[] s = new char[length()];
            getChars(0, length(), s, 0);
            return new String(s);
        }
    }

    static class SpannedEllipsizer extends Ellipsizer implements Spanned {

        private final Spanned mSpanned;

        public SpannedEllipsizer(CharSequence display) {
            super(display);
            mSpanned = (Spanned) display;
        }

        @Nullable
        @Override
        public <T> T[] getSpans(int start, int end, Class<? extends T> type, @Nullable List<T> out) {
            return mSpanned.getSpans(start, end, type, out);
        }

        @Override
        public int getSpanStart(@Nonnull Object tag) {
            return mSpanned.getSpanStart(tag);
        }

        @Override
        public int getSpanEnd(@Nonnull Object tag) {
            return mSpanned.getSpanEnd(tag);
        }

        @Override
        public int getSpanFlags(@Nonnull Object tag) {
            return mSpanned.getSpanFlags(tag);
        }

        @Override
        public int nextSpanTransition(int start, int limit, Class<?> type) {
            return mSpanned.nextSpanTransition(start, limit, type);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            char[] s = new char[end - start];
            getChars(start, end, s, 0);

            SpannableString ss = new SpannableString(new String(s));
            TextUtils.copySpansFrom(mSpanned, start, end, Object.class, ss, 0);
            return ss;
        }
    }
}
