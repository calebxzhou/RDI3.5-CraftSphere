package calebzhou.rdi.craftsphere.screen;

import calebzhou.rdi.craftsphere.ExampleGui;
import com.mojang.blaze3d.platform.Window;
import com.spinyowl.legui.DefaultInitializer;
import com.spinyowl.legui.animation.Animator;
import com.spinyowl.legui.animation.AnimatorProvider;
import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.style.Style;
import com.spinyowl.legui.style.color.ColorConstants;
import com.spinyowl.legui.system.context.Context;
import com.spinyowl.legui.system.layout.LayoutManager;
import com.spinyowl.legui.system.renderer.Renderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class TestScreen extends Screen {
    private boolean running=false;
    private static Context context;
    private ExampleGui gui;
    private Frame frame;
    private Window window;
    DefaultInitializer initializer;
    Renderer renderer;
    Animator animator;
    public TestScreen() {
        super(new TextComponent(""));
    }

    @Override
    protected void init() {
        Frame frame = new Frame();
        ExampleGui gui = new ExampleGui(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
        gui.setFocusable(false);
        gui.getStyle().setMinWidth(Minecraft.getInstance().getWindow().getWidth());
        gui.getStyle().setMinHeight(Minecraft.getInstance().getWindow().getHeight());
        gui.getStyle().getFlexStyle().setFlexGrow(1);
        gui.getStyle().setPosition(Style.PositionType.RELATIVE);
        gui.getStyle().getBackground().setColor(ColorConstants.transparent());
        GLFWKeyCallbackI exitOnEscCallback =
                (w1, key, code, action, mods) ->
                        running = !(key == GLFW_KEY_ESCAPE && action != GLFW_RELEASE);
        GLFWWindowCloseCallbackI glfwWindowCloseCallbackI = w -> running = false;

        // if we want to create some callbacks for system events you should create and put them to
        // keeper
        //
        // Wrong:
        // glfwSetKeyCallback(window, exitOnEscCallback);
        // glfwSetWindowCloseCallback(window, glfwWindowCloseCallbackI);
        //
        // Right:
        initializer.getCallbackKeeper().getChainKeyCallback().add(exitOnEscCallback);
        initializer.getCallbackKeeper().getChainWindowCloseCallback().add(glfwWindowCloseCallbackI);
        frame.getContainer().getStyle().setDisplay(Style.DisplayType.FLEX);
        frame.getContainer().add(gui);
        this.gui=gui;
        this.frame=frame;
        this.window = Minecraft.getInstance().getWindow();
        DefaultInitializer initializer = new DefaultInitializer(window.getWindow(), frame);
        Renderer renderer = initializer.getRenderer();
        Animator animator = AnimatorProvider.getAnimator();
        renderer.initialize();
        this.initializer=initializer;
        this.renderer=renderer;
        this.animator = animator;
        context = initializer.getContext();
        running=true;
        while(running){
            context.updateGlfwWindow();
            Vector2i windowSize = context.getFramebufferSize();

            glClearColor(1, 1, 1, 1);
            // Set viewport size
            glViewport(0, 0, windowSize.x, windowSize.y);
            // Clear screen
            glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

            // We need to relayout components.
            if (gui.getGenerateEventsByLayoutManager().isChecked()) {
                LayoutManager.getInstance().layout(frame, context);
            } else {
                LayoutManager.getInstance().layout(frame);
            }

            // render frame
            renderer.render(frame, context);

            // poll events to callbacks
            glfwPollEvents();
            glfwSwapBuffers(window.getWindow());

            animator.runAnimations();

            // Now we need to handle events. Firstly we need to handle system events.
            // And we need to know to which frame they should be passed.
            initializer.getSystemEventProcessor().processEvents(frame, context);

            // When system events are translated to GUI events we need to handle them.
            // This event processor calls listeners added to ui components
            initializer.getGuiEventProcessor().processEvents();
            update();

        }
    }
    private void update() {
        if (context != null) {
            com.spinyowl.legui.component.Component mouseTargetGui = context.getMouseTargetGui();
            gui.getMouseTargetLabel()
                    .getTextState()
                    .setText(
                            "-> " + (mouseTargetGui == null ? null : mouseTargetGui.getClass().getSimpleName()));

            Component focusedGui = context.getFocusedGui();
            gui.getFocusedGuiLabel()
                    .getTextState()
                    .setText("-> " + (focusedGui == null ? null : focusedGui.getClass().getSimpleName()));
        }
    }
}
