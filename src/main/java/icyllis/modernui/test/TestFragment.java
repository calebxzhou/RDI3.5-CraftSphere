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

package icyllis.modernui.test;

import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.math.Rect;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.view.ViewGroup.LayoutParams;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static icyllis.modernui.view.View.dp;

public class TestFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@Nullable ViewGroup container, @Nullable DataSet savedInstanceState) {
        ScrollView base = new ScrollView();

        {
            LinearLayout content = new TestLinearLayout();
            content.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            var params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            base.addView(content, params);
        }

        base.setBackground(new Drawable() {
            //long lastTime = AnimationHandler.currentTimeMillis();

            @Override
            public void draw(@Nonnull Canvas canvas) {
                Paint paint = Paint.get();
                Rect b = getBounds();
                paint.setRGBA(8, 8, 8, 80);
                canvas.drawRoundRect(b.left, b.top, b.right, b.bottom, 8, paint);

                /*SpectrumGraph graph = TestMain.sGraph;
                long time = AnimationHandler.currentTimeMillis();
                long delta = time - lastTime;
                lastTime = time;
                if (graph != null) {
                    float playTime = TestMain.sTrack.getTime();
                    graph.update(delta);
                    graph.draw(canvas, getBounds().centerX(), getBounds().centerY());
                    invalidateSelf();
                }*/
            }
        });
        {
            var params = new FrameLayout.LayoutParams(dp(480), dp(360));
            params.gravity = Gravity.CENTER;
            base.setLayoutParams(params);
        }
        return base;
    }

    private static class TestView extends View {

        @Override
        protected void onDraw(@Nonnull Canvas canvas) {
            //canvas.drawRing(100, 20, 5, 8);
            // 3


            /*//RenderHelper.setupGuiFlatDiffuseLighting();
            //GL11.glColor4d(1, 1, 1, 1);
            //GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            RenderSystem.disableDepthTest();

            MainWindow mainWindow = Minecraft.getInstance().getMainWindow();
            RenderSystem.multMatrix(Matrix4f.perspective(90.0D,
                    (float) mainWindow.getFramebufferWidth() / mainWindow.getFramebufferHeight(),
                    1.0F, 100.0F));
            //RenderSystem.viewport(0, 0, mainWindow.getFramebufferWidth(), mainWindow.getFramebufferHeight());
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glTranslatef(-2.8f, -1.0f, -1.8f);
            GL11.glScalef(1 / 90f, -1 / 90f, 1 / 90f);
            //GL11.glTranslatef(0, 3, 1984);
            //GL11.glRotatef((canvas.getDrawingTime() / 10f) % 360 - 180, 0, 1, 0);
            GL11.glRotatef(12, 0, 1, 0);
            *//*if ((canvas.getDrawingTime() ^ 127) % 40 == 0) {
             *//**//*float[] pj = new float[16];
                GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, pj);
                ModernUI.LOGGER.info(Arrays.toString(pj));
                GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, pj);
                ModernUI.LOGGER.info(Arrays.toString(pj));*//**//*
                ModernUI.LOGGER.info(GL11.glGetBoolean(GL30.GL_RESCALE_NORMAL));
            }*//*
            ClientPlayerEntity player = Minecraft.getInstance().player;
            canvas.setColor(170, 220, 240, 128);
            if (player != null) {
                canvas.drawRoundedRect(0, 25, player.getHealth() * 140 / player.getMaxHealth(), 39, 4);
            }
            *//*canvas.setAlpha(255);
            canvas.drawRoundedFrame(1, 26, 141, 40, 4);*//*
             *//*canvas.setColor(53, 159, 210, 192);
            canvas.drawRoundedFrame(0, 25, 140, 39, 4);*//*
            if (player != null) {
                canvas.resetColor();
                canvas.setTextAlign(TextAlign.RIGHT);
                canvas.drawText(decimalFormat.format(player.getHealth()) + " / " + decimalFormat.format(player
                .getMaxHealth()), 137, 28);
            }
            RenderSystem.enableDepthTest();
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            //GL11.glEnable(GL11.GL_CULL_FACE);
            //RenderHelper.setupGui3DDiffuseLighting();

            //canvas.drawRoundedRect(0, 25, 48, 45, 6);*/
        }
    }
}
