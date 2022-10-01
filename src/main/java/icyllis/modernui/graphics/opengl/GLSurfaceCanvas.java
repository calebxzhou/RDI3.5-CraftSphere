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

package icyllis.modernui.graphics.opengl;

import icyllis.modernui.ModernUI;
import icyllis.modernui.annotation.RenderThread;
import icyllis.modernui.core.Core;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.font.*;
import icyllis.modernui.math.*;
import icyllis.modernui.text.TextPaint;
import icyllis.modernui.util.Pool;
import icyllis.modernui.util.Pools;
import icyllis.modernui.view.Gravity;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.lang.annotation.Native;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.*;

import static icyllis.modernui.graphics.opengl.GLCore.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Modern OpenGL implementation to Canvas, handling multithreaded rendering.
 * This requires OpenGL 4.5 core profile.
 * <p>
 * Modern UI Canvas is designed for high-performance real-time rendering of
 * vector graphics with infinite precision. Thus, you can't draw other things
 * except those defined in Canvas easily. All geometry instances will be
 * redrawn/re-rendered every frame.
 * <p>
 * Modern UI doesn't make use of tessellation for non-primitives. Instead, we use
 * analytic geometry algorithm in Fragment Shaders to get ideal solution with
 * infinite precision. However, for stoke shaders, there will be a lot of discarded
 * fragments that can not be avoided on the CPU side. And they're recomputed each
 * frame. Especially the quadratic Bézier curve, the algorithm is very complex.
 * And this can't draw cubic Bézier curves, the only way is through tessellation.
 * <p>
 * This class is used to build OpenGL buffers from one thread, using multiple
 * vertex arrays, uniform buffers and vertex buffers. All drawing methods are
 * recording commands and must be called from one thread. Later call
 * {@link #draw(GLFramebuffer)} for calling OpenGL functions on the render thread.
 * The color buffer drawn to must be at index 0, and stencil buffer must be 8-bit.
 * <p>
 * For multiple off-screen rendering targets, Modern UI allocates up to four
 * color buffers as attachments to the target framebuffer. This handles global
 * alpha transformation.
 * <p>
 * For clipping, Modern UI uses stencil test as well as pre-clipping on CPU side.
 * <p>
 * GLSL shader sources are defined in assets.
 *
 * @author BloCamLimb
 */
@NotThreadSafe
public final class GLSurfaceCanvas extends GLCanvas {

    private static volatile GLSurfaceCanvas sInstance;

    // we have only one instance called on UI thread only
    private static final Pool<DrawText> sDrawTextPool = Pools.simple(60);

    /**
     * Uniform block binding points (sequential)
     */
    public static final int MATRIX_BLOCK_BINDING = 0;

    /**
     * Vertex buffer binding points
     */
    public static final int GENERIC_BINDING = 0;

    /**
     * Vertex attributes
     */
    public static final VertexAttrib POS;
    public static final VertexAttrib COLOR;
    public static final VertexAttrib UV;

    /**
     * Vertex formats
     */
    public static final VertexFormat POS_COLOR;
    public static final VertexFormat POS_COLOR_TEX;
    public static final VertexFormat POS_TEX;

    /**
     * Shader programs
     */
    public static final GLProgram COLOR_FILL = new GLProgram();
    public static final GLProgram COLOR_TEX = new GLProgram();
    public static final GLProgram ROUND_RECT_FILL = new GLProgram();
    public static final GLProgram ROUND_RECT_TEX = new GLProgram();
    public static final GLProgram ROUND_RECT_STROKE = new GLProgram();
    public static final GLProgram CIRCLE_FILL = new GLProgram();
    public static final GLProgram CIRCLE_STROKE = new GLProgram();
    public static final GLProgram ARC_FILL = new GLProgram();
    public static final GLProgram ARC_STROKE = new GLProgram();
    public static final GLProgram BEZIER_CURVE = new GLProgram();
    public static final GLProgram ALPHA_TEX = new GLProgram();
    public static final GLProgram COLOR_TEX_MS = new GLProgram();

    /**
     * Recording draw operations (sequential)
     */
    public static final byte DRAW_TRIANGLE = 0;
    public static final byte DRAW_RECT = 1;
    public static final byte DRAW_IMAGE = 2;
    public static final byte DRAW_ROUND_RECT_FILL = 3;
    public static final byte DRAW_ROUND_IMAGE = 4;
    public static final byte DRAW_ROUND_RECT_STROKE = 5;
    public static final byte DRAW_CIRCLE_FILL = 6;
    public static final byte DRAW_CIRCLE_STROKE = 7;
    public static final byte DRAW_ARC_FILL = 8;
    public static final byte DRAW_ARC_STROKE = 9;
    public static final byte DRAW_BEZIER = 10;
    public static final byte DRAW_TEXT = 11;
    public static final byte DRAW_IMAGE_MS = 12;
    public static final byte DRAW_CLIP_PUSH = 13;
    public static final byte DRAW_CLIP_POP = 14;
    public static final byte DRAW_MATRIX = 15;
    public static final byte DRAW_SMOOTH = 16;
    public static final byte DRAW_LAYER_PUSH = 17;
    public static final byte DRAW_LAYER_POP = 18;
    public static final byte DRAW_CUSTOM = 19;

    /**
     * Uniform block sizes (maximum), use std140 layout
     */
    public static final int MATRIX_UNIFORM_SIZE = 144;
    public static final int SMOOTH_UNIFORM_SIZE = 4;
    public static final int ARC_UNIFORM_SIZE = 24;
    public static final int BEZIER_UNIFORM_SIZE = 28;
    public static final int CIRCLE_UNIFORM_SIZE = 16;
    public static final int ROUND_RECT_UNIFORM_SIZE = 24;

    @Native
    public static final int POS_COLOR_TEX_VERTEX_SIZE = 20;

    static {
        POS = new VertexAttrib(GENERIC_BINDING, VertexAttrib.Src.FLOAT, VertexAttrib.Dst.VEC2, false);
        COLOR = new VertexAttrib(GENERIC_BINDING, VertexAttrib.Src.UBYTE, VertexAttrib.Dst.VEC4, true);
        UV = new VertexAttrib(GENERIC_BINDING, VertexAttrib.Src.FLOAT, VertexAttrib.Dst.VEC2, false);
        //MODEL_VIEW = new VertexAttrib(INSTANCED_BINDING, VertexAttrib.Src.FLOAT, VertexAttrib.Dst.MAT4, false);
        POS_COLOR = new VertexFormat(POS, COLOR);
        POS_COLOR_TEX = new VertexFormat(POS, COLOR, UV);
        POS_TEX = new VertexFormat(POS, UV);

        ShaderManager.getInstance().addListener(GLSurfaceCanvas::onLoadShaders);
    }

    // recorded operations
    private final ByteArrayList mDrawOps = new ByteArrayList();

    // vertex buffer objects
    private final GLBuffer mPosColorVBO = new GLBuffer();
    private ByteBuffer mPosColorMemory = memAlloc(4096);
    private boolean mPosColorResized = true;

    private final GLBuffer mPosColorTexVBO = new GLBuffer();
    private ByteBuffer mPosColorTexMemory = memAlloc(4096);
    private boolean mPosColorTexResized = true;

    // dynamic update on render thread
    private final GLBuffer mPosTexVBO = new GLBuffer();
    private ByteBuffer mPosTexMemory = memAlloc(4096);
    private boolean mPosTexResized = true;

    /*private int mModelViewVBO = INVALID_ID;
    private ByteBuffer mModelViewData = memAlloc(1024);
    private boolean mRecreateModelView = true;*/

    // the client buffer used for updating the uniform blocks
    private ByteBuffer mUniformMemory = memAlloc(4096);

    // immutable uniform buffer objects
    private final GLBuffer mMatrixUBO = new GLBuffer();
    private final GLBuffer mSmoothUBO = new GLBuffer();
    private final GLBuffer mArcUBO = new GLBuffer();
    private final GLBuffer mBezierUBO = new GLBuffer();
    private final GLBuffer mCircleUBO = new GLBuffer();
    private final GLBuffer mRoundRectUBO = new GLBuffer();

    // mag filter = linear
    private final int mFontSampler;

    private final long mUniformBuffers = nmemAlloc(24);

    private final ByteBuffer mLayerImageMemory = memAlloc(POS_COLOR_TEX_VERTEX_SIZE * 4);

    // used in rendering, local states
    private int mCurrTexture;
    private int mCurrSampler;
    private int mCurrProgram;
    private int mCurrVertexFormat;

    // absolute value presents the reference value, and sign represents whether to
    // update the stencil buffer (positive = update, or just change stencil func)
    private final IntList mClipRefs = new IntArrayList();
    private final IntList mLayerAlphas = new IntArrayList();
    private final IntStack mLayerStack = new IntArrayList(3);

    // using textures of draw states, in the order of calling
    private final Queue<GLTexture> mTextures = new ArrayDeque<>();
    private final List<DrawText> mDrawTexts = new ArrayList<>();
    private final Queue<Runnable> mCustoms = new ArrayDeque<>();

    private final Matrix4 mProjection = new Matrix4();
    private final FloatBuffer mProjectionUpload = memAllocFloat(16);

    @RenderThread
    public GLSurfaceCanvas() {
        /*mProjectionUBO = glCreateBuffers();
        glNamedBufferStorage(mProjectionUBO, PROJECTION_UNIFORM_SIZE, GL_DYNAMIC_STORAGE_BIT);

        mGlyphUBO = glCreateBuffers();
        glNamedBufferStorage(mGlyphUBO, GLYPH_UNIFORM_SIZE, GL_DYNAMIC_STORAGE_BIT);

        mRoundRectUBO = glCreateBuffers();
        glNamedBufferStorage(mRoundRectUBO, ROUND_RECT_UNIFORM_SIZE, GL_DYNAMIC_STORAGE_BIT);

        mCircleUBO = glCreateBuffers();
        glNamedBufferStorage(mCircleUBO, CIRCLE_UNIFORM_SIZE, GL_DYNAMIC_STORAGE_BIT);

        mArcUBO = glCreateBuffers();
        glNamedBufferStorage(mArcUBO, ARC_UNIFORM_SIZE, GL_DYNAMIC_STORAGE_BIT);*/

        mMatrixUBO.allocate(MATRIX_UNIFORM_SIZE, NULL, GL_DYNAMIC_STORAGE_BIT);
        mSmoothUBO.allocate(SMOOTH_UNIFORM_SIZE, NULL, GL_DYNAMIC_STORAGE_BIT);
        mArcUBO.allocate(ARC_UNIFORM_SIZE, NULL, GL_DYNAMIC_STORAGE_BIT);
        mBezierUBO.allocate(BEZIER_UNIFORM_SIZE, NULL, GL_DYNAMIC_STORAGE_BIT);
        mCircleUBO.allocate(CIRCLE_UNIFORM_SIZE, NULL, GL_DYNAMIC_STORAGE_BIT);
        mRoundRectUBO.allocate(ROUND_RECT_UNIFORM_SIZE, NULL, GL_DYNAMIC_STORAGE_BIT);

        memPutInt(mUniformBuffers, mMatrixUBO.get());
        memPutInt(mUniformBuffers + 4, mSmoothUBO.get());
        memPutInt(mUniformBuffers + 8, mArcUBO.get());
        memPutInt(mUniformBuffers + 12, mBezierUBO.get());
        memPutInt(mUniformBuffers + 16, mCircleUBO.get());
        memPutInt(mUniformBuffers + 20, mRoundRectUBO.get());

        mFontSampler = glCreateSamplers();
        glSamplerParameteri(mFontSampler, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        // FontAtlas MAG_FILTER is NEAREST, but it's not smooth in UI animations
        glSamplerParameteri(mFontSampler, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(mFontSampler, GL_TEXTURE_MIN_LOD, 0);
        glSamplerParameteri(mFontSampler, GL_TEXTURE_MAX_LOD, GLFontAtlas.MIPMAP_LEVEL);

        POS_COLOR.setVertexBuffer(GENERIC_BINDING, mPosColorVBO, 0);
        POS_COLOR_TEX.setVertexBuffer(GENERIC_BINDING, mPosColorTexVBO, 0);
        POS_TEX.setVertexBuffer(GENERIC_BINDING, mPosTexVBO, 0);

        ModernUI.LOGGER.debug(MARKER,
                "Vertex buffers: PosColor {}, PosColorTex {}, PosTex(Glyph) {}",
                mPosColorVBO.get(), mPosColorTexVBO.get(), mPosTexVBO.get());

        mSaves.push(new Save());

        ModernUI.LOGGER.info(MARKER, "Created OpenGL surface canvas");
    }

    @RenderThread
    public static GLSurfaceCanvas initialize() {
        Core.checkRenderThread();
        if (sInstance == null) {
            sInstance = new GLSurfaceCanvas();
            /*POS_COLOR.setBindingDivisor(INSTANCED_BINDING, 1);
            POS_COLOR_TEX.setBindingDivisor(INSTANCED_BINDING, 1);*/
        }
        return sInstance;
    }

    /**
     * Exposed for internal use, be aware of the thread-safety and client-controlled GL states.
     *
     * @return the global instance
     */
    public static GLSurfaceCanvas getInstance() {
        return sInstance;
    }

    private static void onLoadShaders(@Nonnull ShaderManager manager) {
        int posColor = manager.getShard(ModernUI.ID, "pos_color.vert");
        int posColorTex = manager.getShard(ModernUI.ID, "pos_color_tex.vert");
        int posTex = manager.getShard(ModernUI.ID, "pos_tex.vert");

        int colorFill = manager.getShard(ModernUI.ID, "color_fill.frag");
        int colorTex = manager.getShard(ModernUI.ID, "color_tex.frag");
        int roundRectFill = manager.getShard(ModernUI.ID, "round_rect_fill.frag");
        int roundRectTex = manager.getShard(ModernUI.ID, "round_rect_tex.frag");
        int roundRectStroke = manager.getShard(ModernUI.ID, "round_rect_stroke.frag");
        int circleFill = manager.getShard(ModernUI.ID, "circle_fill.frag");
        int circleStroke = manager.getShard(ModernUI.ID, "circle_stroke.frag");
        int arcFill = manager.getShard(ModernUI.ID, "arc_fill.frag");
        int arcStroke = manager.getShard(ModernUI.ID, "arc_stroke.frag");
        int quadBezier = manager.getShard(ModernUI.ID, "quadratic_bezier.frag");
        int alphaTex = manager.getShard(ModernUI.ID, "alpha_tex.frag");
        int colorTexMs = manager.getShard(ModernUI.ID, "color_tex_4x.frag");

        manager.create(COLOR_FILL, posColor, colorFill);
        manager.create(COLOR_TEX, posColorTex, colorTex);
        manager.create(ROUND_RECT_FILL, posColor, roundRectFill);
        manager.create(ROUND_RECT_TEX, posColorTex, roundRectTex);
        manager.create(ROUND_RECT_STROKE, posColor, roundRectStroke);
        manager.create(CIRCLE_FILL, posColor, circleFill);
        manager.create(CIRCLE_STROKE, posColor, circleStroke);
        manager.create(ARC_FILL, posColor, arcFill);
        manager.create(ARC_STROKE, posColor, arcStroke);
        manager.create(BEZIER_CURVE, posColor, quadBezier);
        manager.create(ALPHA_TEX, posTex, alphaTex);
        manager.create(COLOR_TEX_MS, posColorTex, colorTexMs);

        ModernUI.LOGGER.info(MARKER, "Loaded OpenGL canvas shaders");
    }

    @Override
    public void reset(int width, int height) {
        super.reset(width, height);
        mDrawOps.clear();
        mClipRefs.clear();
        mLayerAlphas.clear();
        mDrawTexts.clear();
        mPosColorMemory.clear();
        mPosColorTexMemory.clear();
        mUniformMemory.clear();
    }

    @RenderThread
    public void setProjection(@Nonnull Matrix4 projection) {
        projection.put(mProjectionUpload.clear());
    }

    @Nonnull
    @RenderThread
    public FloatBuffer getProjection() {
        return mProjectionUpload.rewind();
    }

    @RenderThread
    public boolean bindVertexArray(int array) {
        if (mCurrVertexFormat != array) {
            glBindVertexArray(array);
            mCurrVertexFormat = array;
            return true;
        }
        return false;
    }

    @RenderThread
    public void useProgram(int program) {
        if (mCurrProgram != program) {
            glUseProgram(program);
            mCurrProgram = program;
        }
    }

    @RenderThread
    public void bindSampler(int sampler) {
        if (mCurrSampler != sampler) {
            glBindSampler(0, sampler);
            mCurrSampler = sampler;
        }
    }

    @RenderThread
    public void bindTexture(int texture) {
        if (mCurrTexture != texture) {
            glBindTextureUnit(0, texture);
            mCurrTexture = texture;
        }
    }

    @RenderThread
    public boolean draw(@Nullable GLFramebuffer framebuffer) {
        Core.checkRenderThread();
        Core.flushRenderCalls();
        if (framebuffer != null) {
            // there's a bug on NVIDIA driver with DSA, allocate them always
            framebuffer.makeBuffers(mWidth, mHeight, true);

            framebuffer.clearColorBuffer();
            framebuffer.clearDepthStencilBuffer();
        }
        if (mDrawOps.isEmpty()) {
            return false;
        }
        if (getSaveCount() != 1) {
            throw new IllegalStateException("Unbalanced save-restore pair: " + getSaveCount());
        }
        if (framebuffer != null) {
            framebuffer.bindDraw();
        }

        // upload projection matrix
        mMatrixUBO.upload(0, 64, memAddress(mProjectionUpload.flip()));

        uploadBuffers();

        // uniform bindings are globally shared, we must re-bind before we use them
        nglBindBuffersBase(GL_UNIFORM_BUFFER, MATRIX_BLOCK_BINDING, 6, mUniformBuffers);

        glStencilFuncSeparate(GL_FRONT, GL_EQUAL, 0, 0xff);
        glStencilMaskSeparate(GL_FRONT, 0xff);

        mCurrVertexFormat = 0;
        mCurrProgram = 0;
        mCurrSampler = 0;
        mCurrTexture = 0;

        long uniformDataPtr = memAddress(mUniformMemory.flip());

        // generic array index
        int posColorIndex = 0;
        // preserve two triangles
        int posColorTexIndex = 4;
        int clipIndex = 0;
        int textIndex = 0;
        // layer alphas
        int alphaIndex = 0;
        // draw buffers
        int colorBuffer = GL_COLOR_ATTACHMENT0;

        for (int op : mDrawOps) {
            switch (op) {
                case DRAW_TRIANGLE -> {
                    bindVertexArray(POS_COLOR.getVertexArray());
                    useProgram(COLOR_FILL.get());
                    glDrawArrays(GL_TRIANGLES, posColorIndex, 3);
                    posColorIndex += 3;
                }
                case DRAW_RECT -> {
                    bindVertexArray(POS_COLOR.getVertexArray());
                    useProgram(COLOR_FILL.get());
                    glDrawArrays(GL_TRIANGLE_STRIP, posColorIndex, 4);
                    posColorIndex += 4;
                }
                case DRAW_ROUND_RECT_FILL -> {
                    bindVertexArray(POS_COLOR.getVertexArray());
                    useProgram(ROUND_RECT_FILL.get());
                    mRoundRectUBO.upload(0, 20, uniformDataPtr);
                    uniformDataPtr += 20;
                    glDrawArrays(GL_TRIANGLE_STRIP, posColorIndex, 4);
                    posColorIndex += 4;
                }
                case DRAW_ROUND_RECT_STROKE -> {
                    bindVertexArray(POS_COLOR.getVertexArray());
                    useProgram(ROUND_RECT_STROKE.get());
                    mRoundRectUBO.upload(0, 24, uniformDataPtr);
                    uniformDataPtr += 24;
                    glDrawArrays(GL_TRIANGLE_STRIP, posColorIndex, 4);
                    posColorIndex += 4;
                }
                case DRAW_ROUND_IMAGE -> {
                    bindVertexArray(POS_COLOR_TEX.getVertexArray());
                    useProgram(ROUND_RECT_TEX.get());
                    bindSampler(0);
                    bindTexture(mTextures.remove().get());
                    mRoundRectUBO.upload(0, 20, uniformDataPtr);
                    uniformDataPtr += 20;
                    glDrawArrays(GL_TRIANGLE_STRIP, posColorTexIndex, 4);
                    posColorTexIndex += 4;
                }
                case DRAW_IMAGE -> {
                    bindVertexArray(POS_COLOR_TEX.getVertexArray());
                    useProgram(COLOR_TEX.get());
                    bindSampler(0);
                    bindTexture(mTextures.remove().get());
                    glDrawArrays(GL_TRIANGLE_STRIP, posColorTexIndex, 4);
                    posColorTexIndex += 4;
                }
                case DRAW_IMAGE_MS -> {
                    bindVertexArray(POS_COLOR_TEX.getVertexArray());
                    useProgram(COLOR_TEX_MS.get());
                    bindSampler(0);
                    bindTexture(mTextures.remove().get());
                    glDrawArrays(GL_TRIANGLE_STRIP, posColorTexIndex, 4);
                    posColorTexIndex += 4;
                }
                case DRAW_CIRCLE_FILL -> {
                    bindVertexArray(POS_COLOR.getVertexArray());
                    useProgram(CIRCLE_FILL.get());
                    mCircleUBO.upload(0, 12, uniformDataPtr);
                    uniformDataPtr += 12;
                    glDrawArrays(GL_TRIANGLE_STRIP, posColorIndex, 4);
                    posColorIndex += 4;
                }
                case DRAW_CIRCLE_STROKE -> {
                    bindVertexArray(POS_COLOR.getVertexArray());
                    useProgram(CIRCLE_STROKE.get());
                    mCircleUBO.upload(0, 16, uniformDataPtr);
                    uniformDataPtr += 16;
                    glDrawArrays(GL_TRIANGLE_STRIP, posColorIndex, 4);
                    posColorIndex += 4;
                }
                case DRAW_ARC_FILL -> {
                    bindVertexArray(POS_COLOR.getVertexArray());
                    useProgram(ARC_FILL.get());
                    mArcUBO.upload(0, 20, uniformDataPtr);
                    uniformDataPtr += 20;
                    glDrawArrays(GL_TRIANGLE_STRIP, posColorIndex, 4);
                    posColorIndex += 4;
                }
                case DRAW_ARC_STROKE -> {
                    bindVertexArray(POS_COLOR.getVertexArray());
                    useProgram(ARC_STROKE.get());
                    mArcUBO.upload(0, 24, uniformDataPtr);
                    uniformDataPtr += 24;
                    glDrawArrays(GL_TRIANGLE_STRIP, posColorIndex, 4);
                    posColorIndex += 4;
                }
                case DRAW_BEZIER -> {
                    bindVertexArray(POS_COLOR.getVertexArray());
                    useProgram(BEZIER_CURVE.get());
                    mBezierUBO.upload(0, 28, uniformDataPtr);
                    uniformDataPtr += 28;
                    glDrawArrays(GL_TRIANGLE_STRIP, posColorIndex, 4);
                    posColorIndex += 4;
                }
                case DRAW_CLIP_PUSH -> {
                    int clipRef = mClipRefs.getInt(clipIndex);

                    if (clipRef >= 0) {
                        glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_KEEP, GL_INCR);
                        glColorMaski(0, false, false, false, false);

                        bindVertexArray(POS_COLOR.getVertexArray());
                        useProgram(COLOR_FILL.get());
                        glDrawArrays(GL_TRIANGLE_STRIP, posColorIndex, 4);
                        posColorIndex += 4;

                        glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_KEEP, GL_KEEP);
                        glColorMaski(0, true, true, true, true);
                    }

                    glStencilFuncSeparate(GL_FRONT, GL_EQUAL, Math.abs(clipRef), 0xff);
                    clipIndex++;
                }
                case DRAW_CLIP_POP -> {
                    int clipRef = mClipRefs.getInt(clipIndex);

                    if (clipRef >= 0) {
                        glStencilFuncSeparate(GL_FRONT, GL_LESS, clipRef, 0xff);
                        glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_KEEP, GL_REPLACE);
                        glColorMaski(0, false, false, false, false);

                        bindVertexArray(POS_COLOR.getVertexArray());
                        useProgram(COLOR_FILL.get());
                        glDrawArrays(GL_TRIANGLE_STRIP, posColorIndex, 4);
                        posColorIndex += 4;

                        glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_KEEP, GL_KEEP);
                        glColorMaski(0, true, true, true, true);
                    }

                    glStencilFuncSeparate(GL_FRONT, GL_EQUAL, Math.abs(clipRef), 0xff);
                    clipIndex++;
                }
                case DRAW_TEXT -> {
                    bindVertexArray(POS_TEX.getVertexArray());
                    useProgram(ALPHA_TEX.get());
                    bindSampler(mFontSampler);
                    mMatrixUBO.upload(128, 16, uniformDataPtr);
                    uniformDataPtr += 16;

                    final GLBakedGlyph[] glyphs = mDrawTexts.get(textIndex++).build(this);

                    if (mPosTexResized) {
                        mPosTexVBO.allocateM(mPosTexMemory.capacity(), NULL, GL_DYNAMIC_DRAW);
                        mPosTexResized = false;
                    }
                    mPosTexVBO.upload(0, mPosTexMemory.flip());
                    mPosTexMemory.clear();

                    for (int i = 0, e = glyphs.length; i < e; i++) {
                        bindTexture(glyphs[i].texture);
                        glDrawArrays(GL_TRIANGLE_STRIP, i << 2, 4);
                    }
                }
                case DRAW_MATRIX -> {
                    mMatrixUBO.upload(64, 64, uniformDataPtr);
                    uniformDataPtr += 64;
                }
                case DRAW_SMOOTH -> {
                    mSmoothUBO.upload(0, 4, uniformDataPtr);
                    uniformDataPtr += 4;
                }
                case DRAW_LAYER_PUSH -> {
                    assert framebuffer != null;
                    mLayerStack.push(mLayerAlphas.getInt(alphaIndex));
                    framebuffer.setDrawBuffer(++colorBuffer);
                    framebuffer.clearColorBuffer();
                    alphaIndex++;
                }
                case DRAW_LAYER_POP -> {
                    assert framebuffer != null;
                    int alpha = mLayerStack.popInt();
                    putRectColorUV(mLayerImageMemory, 0, 0, mWidth, mHeight,
                            alpha << 24 | alpha << 16 | alpha << 8 | alpha,
                            0, 1, 1, 0);
                    mPosColorTexVBO.upload(0, mLayerImageMemory.flip());
                    mLayerImageMemory.clear();

                    bindVertexArray(POS_COLOR_TEX.getVertexArray());
                    useProgram(COLOR_TEX_MS.get());
                    bindSampler(0);
                    bindTexture(framebuffer.getAttachedTexture(colorBuffer).get());
                    framebuffer.setDrawBuffer(--colorBuffer);
                    glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
                    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
                    glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
                }
                case DRAW_CUSTOM -> mCustoms.remove().run();
                default -> throw new IllegalStateException("Unexpected draw op " + op);
            }
        }
        assert mLayerStack.isEmpty();
        assert mTextures.isEmpty();
        assert mCustoms.isEmpty();

        bindSampler(0);

        mDrawOps.clear();
        mClipRefs.clear();
        mLayerAlphas.clear();
        mDrawTexts.clear();
        mUniformMemory.clear();
        return true;
    }

    @RenderThread
    private void uploadBuffers() {
        if (mPosColorResized) {
            mPosColorVBO.allocateM(mPosColorMemory.capacity(), NULL, GL_DYNAMIC_DRAW);
            mPosColorResized = false;
        }
        mPosColorVBO.upload(0, mPosColorMemory.flip());
        mPosColorMemory.clear();

        if (mPosColorTexResized) {
            mPosColorTexVBO.allocateM(mPosColorTexMemory.capacity() + POS_COLOR_TEX_VERTEX_SIZE * 4, NULL,
                    GL_DYNAMIC_DRAW);
            mPosColorTexResized = false;
        }
        // preserve memory for layer rendering
        mPosColorTexVBO.upload(POS_COLOR_TEX_VERTEX_SIZE * 4, mPosColorTexMemory.flip());
        mPosColorTexMemory.clear();

        /*checkModelViewVBO();
        mModelViewData.flip();
        glNamedBufferSubData(mModelViewVBO, 0, mModelViewData);
        mModelViewData.clear();*/
    }

    /*@RenderThread
    private void checkModelViewVBO() {
        if (!mRecreateModelView)
            return;
        mModelViewVBO = glCreateBuffers();
        glNamedBufferStorage(mModelViewVBO, mModelViewData.capacity(), GL_DYNAMIC_STORAGE_BIT);
        // configure
        POS_COLOR.setVertexBuffer(INSTANCED_BINDING, mModelViewVBO, 0);
        POS_COLOR_TEX.setVertexBuffer(INSTANCED_BINDING, mModelViewVBO, 0);
        mRecreateModelView = false;
    }*/

    private ByteBuffer checkPosColorMemory() {
        if (mPosColorMemory.remaining() < 48) {
            int newCap = grow(mPosColorMemory.capacity());
            mPosColorMemory = memRealloc(mPosColorMemory, newCap);
            mPosColorResized = true;
            ModernUI.LOGGER.debug(MARKER, "Grow pos color buffer to {} bytes", newCap);
        }
        return mPosColorMemory;
    }

    private ByteBuffer checkPosColorTexMemory() {
        if (mPosColorTexMemory.remaining() < POS_COLOR_TEX_VERTEX_SIZE * 4) {
            int newCap = grow(mPosColorTexMemory.capacity());
            mPosColorTexMemory = memRealloc(mPosColorTexMemory, newCap);
            mPosColorTexResized = true;
            ModernUI.LOGGER.debug(MARKER, "Grow pos color tex buffer to {} bytes", newCap);
        }
        return mPosColorTexMemory;
    }

    /*private ByteBuffer getModelViewBuffer() {
        if (mModelViewData.remaining() < 64) {
            mModelViewData = memRealloc(mModelViewData, mModelViewData.capacity() << 1);
            mRecreateModelView = true;
            ModernUI.LOGGER.debug(MARKER, "Resize model view buffer to {} bytes", mModelViewData.capacity());
        }
        return mModelViewData;
    }*/

    @RenderThread
    private ByteBuffer checkPosTexMemory() {
        if (mPosTexMemory.remaining() < 64) {
            int newCap = grow(mPosTexMemory.capacity());
            mPosTexMemory = memRealloc(mPosTexMemory, newCap);
            mPosTexResized = true;
            ModernUI.LOGGER.debug(MARKER, "Grow pos tex buffer to {} bytes", newCap);
        }
        return mPosTexMemory;
    }

    private ByteBuffer checkUniformMemory() {
        if (mUniformMemory.remaining() < 64) {
            int newCap = grow(mUniformMemory.capacity());
            mUniformMemory = memRealloc(mUniformMemory, newCap);
            ModernUI.LOGGER.debug(MARKER, "Grow general uniform buffer to {} bytes", newCap);
        }
        return mUniformMemory;
    }

    public int getNativeMemoryUsage() {
        return mPosColorMemory.capacity() + mPosColorTexMemory.capacity() + mPosTexMemory.capacity() + mUniformMemory.capacity();
    }

    private static int grow(int cap) {
        return cap + (cap >> 1);
    }

    /**
     * "Draw" matrix. Update the model view matrix on render thread.
     */
    private void drawMatrix() {
        drawMatrix(getMatrix());
    }

    /**
     * "Draw" matrix. Update the model view matrix on render thread.
     *
     * @param matrix specified matrix
     */
    private void drawMatrix(@Nonnull Matrix4 matrix) {
        if (!matrix.approxEqual(mLastMatrix)) {
            mLastMatrix.set(matrix);
            matrix.put(checkUniformMemory());
            mDrawOps.add(DRAW_MATRIX);
        }
    }


    /**
     * Saves the current matrix and clip onto a private stack.
     * <p>
     * Subsequent calls to translate,scale,rotate,skew,concat or clipRect,
     * clipPath will all operate as usual, but when the balancing call to
     * restore() is made, those calls will be forgotten, and the settings that
     * existed before the save() will be reinstated.
     *
     * @return The value to pass to restoreToCount() to balance this save()
     */
    @Override
    public int save() {
        int saveCount = getSaveCount();

        Save s = sSavePool.acquire();
        if (s == null) {
            s = getSave().copy();
        } else {
            s.set(getSave());
        }
        mSaves.push(s);

        return saveCount;
    }

    @Override
    public int saveLayer(float left, float top, float right, float bottom, int alpha) {
        int saveCount = getSaveCount();

        Save s = sSavePool.acquire();
        if (s == null) {
            s = getSave().copy();
        } else {
            s.set(getSave());
        }
        mSaves.push(s);

        if (alpha <= 0) {
            // will be quick rejected
            s.mClip.setEmpty();
        } else if (alpha < 255 && s.mColorBuf < 3) {
            s.mColorBuf++;
            mLayerAlphas.add(alpha);
            mDrawOps.add(DRAW_LAYER_PUSH);
        }

        return saveCount;
    }

    /**
     * This call balances a previous call to save(), and is used to remove all
     * modifications to the matrix/clip state since the last save call. It is
     * an error to call restore() more or less times than save() was called in
     * the final state.
     * <p>
     * If current clip doesn't change, it won't generate overhead for modifying
     * the stencil buffer.
     */
    @Override
    public void restore() {
        if (mSaves.size() < 1) {
            throw new IllegalStateException("Underflow in restore");
        }
        Save save = mSaves.poll();
        if (save.mClipRef != getSave().mClipRef) {
            restoreClipBatch(save.mClip);
        }
        if (save.mColorBuf != getSave().mColorBuf) {
            restoreLayer();
        }
        sSavePool.release(save);
    }

    /**
     * Efficient way to pop any calls to save() that happened after the save
     * count reached saveCount. It is an error for saveCount to be less than 1.
     * <p>
     * Example:
     * <pre>
     * int count = canvas.save();
     * ... // more calls potentially to save()
     * canvas.restoreToCount(count);
     * // now the canvas is back in the same state it
     * // was before the initial call to save().
     * </pre>
     *
     * @param saveCount The save level to restore to.
     */
    @Override
    public void restoreToCount(int saveCount) {
        if (saveCount < 1) {
            throw new IllegalArgumentException("Underflow in restoreToCount");
        }
        Deque<Save> stack = mSaves;

        Save lastPop = stack.pop();
        final int topRef = lastPop.mClipRef;
        final int topBuf = lastPop.mColorBuf;
        sSavePool.release(lastPop);

        while (stack.size() > saveCount) {
            lastPop = stack.pop();
            sSavePool.release(lastPop);
        }

        if (topRef != getSave().mClipRef) {
            restoreClipBatch(lastPop.mClip);
        }

        int bufDiff = topBuf - getSave().mColorBuf;
        while (bufDiff-- > 0) {
            restoreLayer();
        }
    }

    /**
     * Batch operation.
     *
     * @param b bounds
     * @see #clipRect(float, float, float, float)
     */
    private void restoreClipBatch(@Nonnull Rect b) {
        /*int pointer = mDrawOps.size() - 1;
        int op;
        boolean skip = true;
        while ((op = mDrawOps.getInt(pointer)) != DRAW_CLIP_PUSH) {
            if (op < DRAW_CLIP_PUSH) {
                skip = false;
                break;
            }
            pointer--;
        }
        if (skip) {
            mClipRefs.removeInt(mClipRefs.size() - 1);
            for (int i = mDrawOps.size() - 1; i >= pointer; i--) {
                mDrawOps.removeInt(i);
            }
            return;
        }*/
        if (b.isEmpty()) {
            // taking the opposite number means that the clip rect is not to drawn
            mClipRefs.add(-getSave().mClipRef);
        } else {
            drawMatrix(RESET_MATRIX);
            // must have a color
            putRectColor(b.left, b.top, b.right, b.bottom, ~0);
            mClipRefs.add(getSave().mClipRef);
        }
        mDrawOps.add(DRAW_CLIP_POP);
    }

    private void restoreLayer() {
        drawMatrix(RESET_MATRIX);
        mDrawOps.add(DRAW_LAYER_POP);
        drawMatrix();
    }


    @Override
    public boolean clipRect(float left, float top, float right, float bottom) {
        final Save save = getSave();
        // empty rect, ignore it
        if (right <= left || bottom <= top) {
            return !save.mClip.isEmpty();
        }
        // already empty, return false
        if (save.mClip.isEmpty()) {
            return false;
        }
        RectF temp = mTmpRectF;
        temp.set(left, top, right, bottom);
        getMatrix().mapRect(temp);

        Rect test = mTmpRect;
        temp.roundOut(test);

        // not empty and not changed, return true
        if (test.contains(save.mClip)) {
            return true;
        }

        boolean intersects = save.mClip.intersect(test);
        save.mClipRef++;
        if (intersects) {
            drawMatrix();
            // updating stencil must have a color
            putRectColor(left, top, right, bottom, ~0);
            mClipRefs.add(save.mClipRef);
        } else {
            // empty
            mClipRefs.add(-save.mClipRef);
            save.mClip.setEmpty();
        }
        mDrawOps.add(DRAW_CLIP_PUSH);
        return intersects;
    }

    @Override
    public boolean quickReject(float left, float top, float right, float bottom) {
        // empty rect, always reject
        if (right <= left || bottom <= top) {
            return true;
        }
        final Rect clip = getSave().mClip;
        // already empty, reject
        if (clip.isEmpty()) {
            return true;
        }

        RectF temp = mTmpRectF;
        temp.set(left, top, right, bottom);
        getMatrix().mapRect(temp);

        Rect test = mTmpRect;
        temp.roundOut(test);
        return !Rect.intersects(clip, test);
    }

    private void putRectColor(float left, float top, float right, float bottom, @Nonnull Paint paint) {
        if (paint.isGradient()) {
            final ByteBuffer buffer = checkPosColorMemory();
            final int[] colors = paint.getColors();

            // CCW
            int color = colors[3];
            byte r = (byte) ((color >> 16) & 0xff);
            byte g = (byte) ((color >> 8) & 0xff);
            byte b = (byte) (color & 0xff);
            byte a = (byte) (color >>> 24);
            buffer.putFloat(left)
                    .putFloat(bottom)
                    .put(r).put(g).put(b).put(a);

            color = colors[2];
            r = (byte) ((color >> 16) & 0xff);
            g = (byte) ((color >> 8) & 0xff);
            b = (byte) (color & 0xff);
            a = (byte) (color >>> 24);
            buffer.putFloat(right)
                    .putFloat(bottom)
                    .put(r).put(g).put(b).put(a);

            color = colors[0];
            r = (byte) ((color >> 16) & 0xff);
            g = (byte) ((color >> 8) & 0xff);
            b = (byte) (color & 0xff);
            a = (byte) (color >>> 24);
            buffer.putFloat(left)
                    .putFloat(top)
                    .put(r).put(g).put(b).put(a);

            color = colors[1];
            r = (byte) ((color >> 16) & 0xff);
            g = (byte) ((color >> 8) & 0xff);
            b = (byte) (color & 0xff);
            a = (byte) (color >>> 24);
            buffer.putFloat(right)
                    .putFloat(top)
                    .put(r).put(g).put(b).put(a);
        } else {
            putRectColor(left, top, right, bottom, paint.getColor());
        }
    }

    private void putRectColor(float left, float top, float right, float bottom, int color) {
        ByteBuffer buffer = checkPosColorMemory();
        byte r = (byte) ((color >> 16) & 0xff);
        byte g = (byte) ((color >> 8) & 0xff);
        byte b = (byte) (color & 0xff);
        byte a = (byte) (color >>> 24);
        // CCW
        buffer.putFloat(left)
                .putFloat(bottom)
                .put(r).put(g).put(b).put(a);
        buffer.putFloat(right)
                .putFloat(bottom)
                .put(r).put(g).put(b).put(a);
        buffer.putFloat(left)
                .putFloat(top)
                .put(r).put(g).put(b).put(a);
        buffer.putFloat(right)
                .putFloat(top)
                .put(r).put(g).put(b).put(a);
    }

    private void putRectColorUV(float left, float top, float right, float bottom, @Nullable Paint paint,
                                float u1, float v1, float u2, float v2) {
        if (paint == null) {
            putRectColorUV(left, top, right, bottom, ~0, u1, v1, u2, v2);
        } else if (paint.isGradient()) {
            final ByteBuffer buffer = checkPosColorTexMemory();
            final int[] colors = paint.getColors();

            // CCW
            int color = colors[3];
            byte r = (byte) ((color >> 16) & 0xff);
            byte g = (byte) ((color >> 8) & 0xff);
            byte b = (byte) (color & 0xff);
            byte a = (byte) (color >>> 24);
            buffer.putFloat(left)
                    .putFloat(bottom)
                    .put(r).put(g).put(b).put(a)
                    .putFloat(u1).putFloat(v2);

            color = colors[2];
            r = (byte) ((color >> 16) & 0xff);
            g = (byte) ((color >> 8) & 0xff);
            b = (byte) (color & 0xff);
            a = (byte) (color >>> 24);
            buffer.putFloat(right)
                    .putFloat(bottom)
                    .put(r).put(g).put(b).put(a)
                    .putFloat(u2).putFloat(v2);

            color = colors[0];
            r = (byte) ((color >> 16) & 0xff);
            g = (byte) ((color >> 8) & 0xff);
            b = (byte) (color & 0xff);
            a = (byte) (color >>> 24);
            buffer.putFloat(left)
                    .putFloat(top)
                    .put(r).put(g).put(b).put(a)
                    .putFloat(u1).putFloat(v1);

            color = colors[1];
            r = (byte) ((color >> 16) & 0xff);
            g = (byte) ((color >> 8) & 0xff);
            b = (byte) (color & 0xff);
            a = (byte) (color >>> 24);
            buffer.putFloat(right)
                    .putFloat(top)
                    .put(r).put(g).put(b).put(a)
                    .putFloat(u2).putFloat(v1);
        } else {
            putRectColorUV(left, top, right, bottom, paint.getColor(), u1, v1, u2, v2);
        }
    }

    private void putRectColorUV(float left, float top, float right, float bottom, int color,
                                float u1, float v1, float u2, float v2) {
        ByteBuffer buffer = checkPosColorTexMemory();
        putRectColorUV(buffer, left, top, right, bottom, color, u1, v1, u2, v2);
    }

    private void putRectColorUV(@Nonnull ByteBuffer buffer, float left, float top, float right, float bottom,
                                int color, float u1, float v1, float u2, float v2) {
        byte r = (byte) ((color >> 16) & 0xff);
        byte g = (byte) ((color >> 8) & 0xff);
        byte b = (byte) (color & 0xff);
        byte a = (byte) (color >>> 24);
        buffer.putFloat(left)
                .putFloat(bottom)
                .put(r).put(g).put(b).put(a)
                .putFloat(u1).putFloat(v2);
        buffer.putFloat(right)
                .putFloat(bottom)
                .put(r).put(g).put(b).put(a)
                .putFloat(u2).putFloat(v2);
        buffer.putFloat(left)
                .putFloat(top)
                .put(r).put(g).put(b).put(a)
                .putFloat(u1).putFloat(v1);
        buffer.putFloat(right)
                .putFloat(top)
                .put(r).put(g).put(b).put(a)
                .putFloat(u2).putFloat(v1);
    }

    @RenderThread
    private void putGlyph(@Nonnull GLBakedGlyph glyph, float left, float top) {
        ByteBuffer buffer = checkPosTexMemory();
        left += glyph.x;
        top += glyph.y;
        float right = left + glyph.width;
        float bottom = top + glyph.height;
        buffer.putFloat(left)
                .putFloat(bottom)
                .putFloat(glyph.u1).putFloat(glyph.v2);
        buffer.putFloat(right)
                .putFloat(bottom)
                .putFloat(glyph.u2).putFloat(glyph.v2);
        buffer.putFloat(left)
                .putFloat(top)
                .putFloat(glyph.u1).putFloat(glyph.v1);
        buffer.putFloat(right)
                .putFloat(top)
                .putFloat(glyph.u2).putFloat(glyph.v1);
    }

    /**
     * Record an operation to update smooth radius later for geometries that use smooth radius.
     *
     * @param smooth current smooth
     */
    private void drawSmooth(float smooth) {
        if (smooth != mLastSmooth) {
            mLastSmooth = smooth;
            checkUniformMemory()
                    .putFloat(smooth);
            mDrawOps.add(DRAW_SMOOTH);
        }
    }

    @Override
    public void drawArc(float cx, float cy, float radius, float startAngle,
                        float sweepAngle, @Nonnull Paint paint) {
        if (FMath.zero(sweepAngle) || radius < 0.0001f) {
            return;
        }
        if (sweepAngle >= 360) {
            drawCircle(cx, cy, radius, paint);
            return;
        }
        sweepAngle %= 360;
        final float middleAngle = (startAngle % 360) + sweepAngle * 0.5f;
        if (paint.getStyle() == Paint.FILL) {
            drawArcFill(cx, cy, radius, middleAngle, sweepAngle, paint);
        } else {
            drawArcStroke(cx, cy, radius, middleAngle, sweepAngle, paint);
        }
    }

    private void drawArcFill(float cx, float cy, float radius, float middleAngle,
                             float sweepAngle, @Nonnull Paint paint) {
        if (quickReject(cx - radius, cy - radius, cx + radius, cy + radius)) {
            return;
        }
        drawMatrix();
        drawSmooth(Math.min(radius, paint.getSmoothRadius()));
        putRectColor(cx - radius, cy - radius, cx + radius, cy + radius, paint);
        checkUniformMemory()
                .putFloat(cx)
                .putFloat(cy)
                .putFloat(middleAngle)
                .putFloat(sweepAngle)
                .putFloat(radius);
        mDrawOps.add(DRAW_ARC_FILL);
    }

    private void drawArcStroke(float cx, float cy, float radius, float middleAngle,
                               float sweepAngle, @Nonnull Paint paint) {
        float strokeRadius = Math.min(radius, paint.getStrokeWidth() * 0.5f);
        if (strokeRadius < 0.0001f) {
            return;
        }
        float maxRadius = radius + strokeRadius;
        if (quickReject(cx - maxRadius, cy - maxRadius, cx + maxRadius, cy + maxRadius)) {
            return;
        }
        drawMatrix();
        drawSmooth(Math.min(strokeRadius, paint.getSmoothRadius()));
        putRectColor(cx - maxRadius, cy - maxRadius, cx + maxRadius, cy + maxRadius, paint);
        checkUniformMemory()
                .putFloat(cx)
                .putFloat(cy)
                .putFloat(middleAngle)
                .putFloat(sweepAngle)
                .putFloat(radius)
                .putFloat(strokeRadius);
        mDrawOps.add(DRAW_ARC_STROKE);
    }

    @Override
    public void drawBezier(float x0, float y0, float x1, float y1, float x2, float y2, @Nonnull Paint paint) {
        float strokeRadius = paint.getStrokeWidth() * 0.5f;
        if (strokeRadius < 0.0001f) {
            return;
        }
        float left = Math.min(Math.min(x0, x1), x2) - strokeRadius;
        float top = Math.min(Math.min(y0, y1), y2) - strokeRadius;
        float right = Math.max(Math.max(x0, x1), x2) + strokeRadius;
        float bottom = Math.max(Math.max(y0, y1), y2) + strokeRadius;
        if (quickReject(left, top, right, bottom)) {
            return;
        }
        drawMatrix();
        drawSmooth(Math.min(strokeRadius, paint.getSmoothRadius()));
        putRectColor(left, top, right, bottom, paint);
        checkUniformMemory()
                .putFloat(x0)
                .putFloat(y0)
                .putFloat(x1)
                .putFloat(y1)
                .putFloat(x2)
                .putFloat(y2)
                .putFloat(strokeRadius);
        mDrawOps.add(DRAW_BEZIER);
    }

    @Override
    public void drawCircle(float cx, float cy, float radius, @Nonnull Paint paint) {
        if (radius < 0.0001f) {
            return;
        }
        if (paint.getStyle() == Paint.FILL) {
            drawCircleFill(cx, cy, radius, paint);
        } else {
            drawCircleStroke(cx, cy, radius, paint);
        }
    }

    private void drawCircleFill(float cx, float cy, float radius, @Nonnull Paint paint) {
        if (quickReject(cx - radius, cy - radius, cx + radius, cy + radius)) {
            return;
        }
        drawMatrix();
        drawSmooth(Math.min(radius, paint.getSmoothRadius()));
        putRectColor(cx - radius, cy - radius, cx + radius, cy + radius, paint);
        checkUniformMemory()
                .putFloat(cx)
                .putFloat(cy)
                .putFloat(radius);
        mDrawOps.add(DRAW_CIRCLE_FILL);
    }

    private void drawCircleStroke(float cx, float cy, float radius, @Nonnull Paint paint) {
        float strokeRadius = Math.min(radius, paint.getStrokeWidth() * 0.5f);
        if (strokeRadius < 0.0001f) {
            return;
        }
        float maxRadius = radius + strokeRadius;
        if (quickReject(cx - maxRadius, cy - maxRadius, cx + maxRadius, cy + maxRadius)) {
            return;
        }
        drawMatrix();
        drawSmooth(Math.min(strokeRadius, paint.getSmoothRadius()));
        putRectColor(cx - maxRadius, cy - maxRadius, cx + maxRadius, cy + maxRadius, paint);
        checkUniformMemory()
                .putFloat(cx)
                .putFloat(cy)
                .putFloat(radius - strokeRadius) // inner radius
                .putFloat(maxRadius);
        mDrawOps.add(DRAW_CIRCLE_STROKE);
    }

    @Override
    public void drawTriangle(float x0, float y0, float x1, float y1, float x2, float y2, @Nonnull Paint paint) {
        float left = Math.min(Math.min(x0, x1), x2);
        float top = Math.min(Math.min(y0, y1), y2);
        float right = Math.max(Math.max(x0, x1), x2);
        float bottom = Math.max(Math.max(y0, y1), y2);
        if (quickReject(left, top, right, bottom)) {
            return;
        }
        drawMatrix();
        int color = paint.getColor();
        ByteBuffer buffer = checkPosColorMemory();
        byte r = (byte) ((color >> 16) & 0xff);
        byte g = (byte) ((color >> 8) & 0xff);
        byte b = (byte) (color & 0xff);
        byte a = (byte) (color >>> 24);
        // CCW
        buffer.putFloat(x0)
                .putFloat(y0)
                .put(r).put(g).put(b).put(a);
        buffer.putFloat(x1)
                .putFloat(y1)
                .put(r).put(g).put(b).put(a);
        buffer.putFloat(x2)
                .putFloat(y2)
                .put(r).put(g).put(b).put(a);
        mDrawOps.add(DRAW_TRIANGLE);
    }

    @Override
    public void drawRect(float left, float top, float right, float bottom, @Nonnull Paint paint) {
        if (quickReject(left, top, right, bottom)) {
            return;
        }
        drawMatrix();
        putRectColor(left, top, right, bottom, paint);
        mDrawOps.add(DRAW_RECT);
    }

    @Override
    public void drawImage(@Nonnull Image image, float left, float top, @Nullable Paint paint) {
        GLTexture texture = image.getTexture();
        float right = left + texture.getWidth();
        float bottom = top + texture.getHeight();
        if (quickReject(left, top, right, bottom)) {
            return;
        }
        drawMatrix();
        putRectColorUV(left, top, right, bottom, paint, 0, 0, 1, 1);
        mTextures.add(texture);
        mDrawOps.add(DRAW_IMAGE);
    }

    // this is only used for offscreen
    public void drawLayer(@Nonnull GLTexture texture, float w, float h, int color, boolean flipY) {
        int target = texture.getTarget();
        if (target == GL_TEXTURE_2D || target == GL_TEXTURE_2D_MULTISAMPLE) {
            drawMatrix();
            putRectColorUV(0, 0, w, h, color,
                    0, flipY ? h / texture.getHeight() : 0,
                    w / texture.getWidth(), flipY ? 0 : h / texture.getHeight());
            mTextures.add(texture);
            mDrawOps.add(target == GL_TEXTURE_2D ? DRAW_IMAGE : DRAW_IMAGE_MS);
        } else {
            ModernUI.LOGGER.warn(MARKER, "Cannot draw texture target {}", target);
        }
    }

    @Override
    public void drawImage(@Nonnull Image image, float srcLeft, float srcTop, float srcRight, float srcBottom,
                          float dstLeft, float dstTop, float dstRight, float dstBottom, @Nullable Paint paint) {
        if (quickReject(dstLeft, dstTop, dstRight, dstBottom)) {
            return;
        }
        GLTexture texture = image.getTexture();
        srcLeft = Math.max(0, srcLeft);
        srcTop = Math.max(0, srcTop);
        int w = texture.getWidth();
        int h = texture.getHeight();
        srcRight = Math.min(srcRight, w);
        srcBottom = Math.min(srcBottom, h);
        if (srcRight <= srcLeft || srcBottom <= srcTop) {
            return;
        }
        drawMatrix();
        putRectColorUV(dstLeft, dstTop, dstRight, dstBottom, paint,
                srcLeft / w, srcTop / h, srcRight / w, srcBottom / h);
        mTextures.add(texture);
        mDrawOps.add(DRAW_IMAGE);
    }

    public void drawTexture(@Nonnull GLTexture texture, float srcLeft, float srcTop, float srcRight, float srcBottom,
                            float dstLeft, float dstTop, float dstRight, float dstBottom) {
        if (quickReject(dstLeft, dstTop, dstRight, dstBottom)) {
            return;
        }
        srcLeft = Math.max(0, srcLeft);
        srcTop = Math.max(0, srcTop);
        int w = texture.getWidth();
        int h = texture.getHeight();
        srcRight = Math.min(srcRight, w);
        srcBottom = Math.min(srcBottom, h);
        if (srcRight <= srcLeft || srcBottom <= srcTop) {
            return;
        }
        drawMatrix();
        putRectColorUV(dstLeft, dstTop, dstRight, dstBottom, null,
                srcLeft / w, srcTop / h, srcRight / w, srcBottom / h);
        mTextures.add(texture);
        mDrawOps.add(DRAW_IMAGE);
    }

    @Override
    public void drawRoundLine(float startX, float startY, float stopX, float stopY, @Nonnull Paint paint) {
        float t = paint.getStrokeWidth() * 0.5f;
        if (t < 0.0001f) {
            return;
        }
        if (FMath.eq(startX, stopX)) {
            if (FMath.eq(startY, stopY)) {
                drawCircleFill(startX, startY, t, paint);
            } else {
                // vertical
                float top = Math.min(startY, stopY);
                float bottom = Math.max(startY, stopY);
                drawRoundRectFill(startX - t, top - t, startX + t, bottom + t, t, 0, paint);
            }
        } else if (FMath.eq(startY, stopY)) {
            // horizontal
            float left = Math.min(startX, stopX);
            float right = Math.max(startX, stopX);
            drawRoundRectFill(left - t, startY - t, right + t, startY + t, t, 0, paint);
        } else {
            float cx = (stopX + startX) * 0.5f;
            float cy = (stopY + startY) * 0.5f;
            float ang = FMath.atan2(stopY - startY, stopX - startX);
            save();
            Matrix4 mat = getMatrix();
            // rotate the round rect
            mat.preTranslate(cx, cy, 0);
            mat.preRotateZ(ang);
            mat.preTranslate(-cx, -cy, 0);
            // rotate positions to horizontal
            float sin = FMath.sin(-ang);
            float cos = FMath.cos(-ang);
            float left = (startX - cx) * cos - (startY - cy) * sin + cx;
            float right = (stopX - cx) * cos - (stopY - cy) * sin + cx;
            drawRoundRectFill(left - t, cy - t, right + t, cy + t, t, 0, paint);
            restore();
        }
    }

    @Override
    public void drawRoundRect(float left, float top, float right, float bottom, float radius,
                              int sides, @Nonnull Paint paint) {
        radius = Math.min(radius, Math.min(right - left, bottom - top) * 0.5f);
        if (radius < 0) {
            radius = 0;
        }
        if (paint.getStyle() == Paint.FILL) {
            drawRoundRectFill(left, top, right, bottom, radius, sides, paint);
        } else {
            drawRoundRectStroke(left, top, right, bottom, radius, sides, paint);
        }
    }

    private void drawRoundRectFill(float left, float top, float right, float bottom,
                                   float radius, int sides, @Nonnull Paint paint) {
        if (quickReject(left, top, right, bottom)) {
            return;
        }
        drawMatrix();
        drawSmooth(Math.min(radius, paint.getSmoothRadius()));
        putRectColor(left, top, right, bottom, paint);
        ByteBuffer buffer = checkUniformMemory();
        if ((sides & Gravity.RIGHT) == Gravity.RIGHT) {
            buffer.putFloat(left);
        } else {
            buffer.putFloat(left + radius);
        }
        if ((sides & Gravity.BOTTOM) == Gravity.BOTTOM) {
            buffer.putFloat(top);
        } else {
            buffer.putFloat(top + radius);
        }
        if ((sides & Gravity.LEFT) == Gravity.LEFT) {
            buffer.putFloat(right);
        } else {
            buffer.putFloat(right - radius);
        }
        if ((sides & Gravity.TOP) == Gravity.TOP) {
            buffer.putFloat(bottom);
        } else {
            buffer.putFloat(bottom - radius);
        }
        buffer.putFloat(radius);
        mDrawOps.add(DRAW_ROUND_RECT_FILL);
    }

    private void drawRoundRectStroke(float left, float top, float right, float bottom,
                                     float radius, int sides, @Nonnull Paint paint) {
        float strokeRadius = Math.min(radius, paint.getStrokeWidth() * 0.5f);
        if (strokeRadius < 0.0001f) {
            return;
        }
        if (quickReject(left - strokeRadius, top - strokeRadius, right + strokeRadius, bottom + strokeRadius)) {
            return;
        }
        drawMatrix();
        drawSmooth(Math.min(strokeRadius, paint.getSmoothRadius()));
        putRectColor(left - strokeRadius, top - strokeRadius, right + strokeRadius, bottom + strokeRadius, paint);
        ByteBuffer buffer = checkUniformMemory();
        if ((sides & Gravity.RIGHT) == Gravity.RIGHT) {
            buffer.putFloat(left);
        } else {
            buffer.putFloat(left + radius);
        }
        if ((sides & Gravity.BOTTOM) == Gravity.BOTTOM) {
            buffer.putFloat(top);
        } else {
            buffer.putFloat(top + radius);
        }
        if ((sides & Gravity.LEFT) == Gravity.LEFT) {
            buffer.putFloat(right);
        } else {
            buffer.putFloat(right - radius);
        }
        if ((sides & Gravity.TOP) == Gravity.TOP) {
            buffer.putFloat(bottom);
        } else {
            buffer.putFloat(bottom - radius);
        }
        buffer.putFloat(radius)
                .putFloat(strokeRadius);
        mDrawOps.add(DRAW_ROUND_RECT_STROKE);
    }

    @Override
    public void drawRoundImage(@Nonnull Image image, float left, float top, float radius, @Nonnull Paint paint) {
        if (radius < 0) {
            radius = 0;
        }
        GLTexture texture = image.getTexture();
        float right = left + texture.getWidth();
        float bottom = top + texture.getHeight();
        if (quickReject(left, top, right, bottom)) {
            return;
        }
        drawMatrix();
        drawSmooth(Math.min(radius, paint.getSmoothRadius()));
        putRectColorUV(left, top, right, bottom, paint,
                0, 0, 1, 1);
        checkUniformMemory()
                .putFloat(left + radius)
                .putFloat(top + radius)
                .putFloat(right - radius)
                .putFloat(bottom - radius)
                .putFloat(radius);
        mTextures.add(texture);
        mDrawOps.add(DRAW_ROUND_IMAGE);
    }

    @Override
    public void drawText(@Nonnull CharSequence text, int start, int end, float x, float y,
                         int align, @Nonnull TextPaint paint) {
        if ((start | end | end - start | text.length() - end) < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (start == end) {
            return;
        }
        final int color = paint.getColor();
        if (end - start <= LayoutCache.MAX_PIECE_LENGTH) {
            LayoutPiece piece = LayoutCache.getOrCreate(text, start, end, false, paint, false, true);
            switch (align & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.CENTER_HORIZONTAL -> x -= piece.getAdvance() / 2f;
                case Gravity.RIGHT -> x -= piece.getAdvance();
            }
            drawTextRun(piece, x, y, color);
        } else {
            final float originalX = x;
            final int pst = mDrawTexts.size();
            int s = start, e = s;
            do {
                e = Math.min(e + LayoutCache.MAX_PIECE_LENGTH, end);
                LayoutPiece piece = LayoutCache.getOrCreate(text, s, e, false, paint, false, true);
                drawTextRun(piece, x, y, color);
                x += piece.getAdvance();
                s = e;
            } while (s < end);
            switch (align & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.CENTER_HORIZONTAL -> {
                    float offset = (originalX - x) / 2f;
                    for (int i = pst; i < mDrawTexts.size(); i++) {
                        mDrawTexts.get(i).offsetX(offset);
                    }
                }
                case Gravity.RIGHT -> {
                    float offset = originalX - x;
                    for (int i = pst; i < mDrawTexts.size(); i++) {
                        mDrawTexts.get(i).offsetX(offset);
                    }
                }
            }
        }
    }

    @Override
    public void drawTextRun(@Nonnull LayoutPiece piece, float x, float y, @Nonnull TextPaint paint) {
        drawTextRun(piece, x, y, paint.getColor());
    }

    private void drawTextRun(@Nonnull LayoutPiece piece, float x, float y, int color) {
        if (piece.getAdvance() == 0 || (piece.getGlyphs() != null && piece.getGlyphs().length == 0)
                || quickReject(x, y - piece.getAscent(),
                x + piece.getAdvance(), y + piece.getDescent())) {
            return;
        }
        DrawText t = sDrawTextPool.acquire();
        if (t == null) {
            t = new DrawText();
        }
        mDrawTexts.add(t.set(piece, x, y));
        drawMatrix();
        checkUniformMemory()
                .putFloat(((color >> 16) & 0xff) / 255f)
                .putFloat(((color >> 8) & 0xff) / 255f)
                .putFloat((color & 0xff) / 255f)
                .putFloat((color >>> 24) / 255f);
        mDrawOps.add(DRAW_TEXT);
    }

    private static class DrawText {

        private LayoutPiece piece;
        private float x;
        private float y;

        private DrawText() {
        }

        @Nonnull
        private DrawText set(@Nonnull LayoutPiece piece, float x, float y) {
            this.piece = piece;
            this.x = x;
            this.y = y;
            return this;
        }

        private void offsetX(float dx) {
            x += dx;
        }

        @Nonnull
        private GLBakedGlyph[] build(@Nonnull GLSurfaceCanvas canvas) {
            final GLBakedGlyph[] glyphs = piece.getGlyphs();
            final float[] positions = piece.getPositions();
            piece = null;
            for (int i = 0, e = glyphs.length; i < e; i++) {
                canvas.putGlyph(glyphs[i], x + positions[i * 2], y + positions[i * 2 + 1]);
            }
            sDrawTextPool.release(this);
            return glyphs;
        }
    }

    /**
     * Draw something custom. Do not break any state of current OpenGL context or GLCanvas in the future.
     *
     * @param custom the custom draw
     */
    public void drawCustom(@Nonnull Runnable custom) {
        mCustoms.add(custom);
        mDrawOps.add(DRAW_CUSTOM);
    }
}
