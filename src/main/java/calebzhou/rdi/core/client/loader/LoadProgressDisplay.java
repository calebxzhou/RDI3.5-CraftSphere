package calebzhou.rdi.core.client.loader;

import calebzhou.rdi.core.client.misc.MusicPlayer;
import calebzhou.rdi.core.client.util.DialogUtils;
import net.minecraft.Util;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
public class LoadProgressDisplay extends Thread{
	public static void main(String[] args) {
		new LoadProgressDisplay().start();;
	}
	private static long window;
	//载入进度条 -1开始 1结束
	public static float loadProgress=-1;
	public static long loadStartTime;
	public static long loadEndTime;
	public LoadProgressDisplay(){
	}
	private void init(){
		//if(Util.getPlatform() != Util.OS.WINDOWS) return;
		GLFWErrorCallback.createPrint(System.err).set();
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(800, 40, "RDI客户端 启动进度条", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
					window,
					(vidmode.width() - pWidth.get(0)) / 2,
					(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}
    @Override
    public void run() {
		/*System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		//glfwTerminate();
		glfwSetErrorCallback(null).free();*/
    }
	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		// Set the clear color
		glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer


			glColor3f(RandomUtils.nextFloat(0,1), RandomUtils.nextFloat(0,1), RandomUtils.nextFloat(0,1));
			glRectf(-1f,1f, loadProgress, -1f);
			loadProgress+=0.001f;

			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}

    public  static void onFinish(){
        if(Util.getPlatform() != Util.OS.WINDOWS)
			return;
		//glfwSetWindowShouldClose(window,true);
            loadEndTime = System.currentTimeMillis();
			float usedTime = (loadEndTime-loadStartTime)/1000.0f;
            String displayTime = String.format("%.2f",usedTime);
            //最快载入20秒
            int standardLoadTime = 40;
            double beyondPlayerRatio = 1.0 / (usedTime / standardLoadTime);
            if(beyondPlayerRatio>=1.0)
                beyondPlayerRatio=0.999;
            String beyondPerc = String.format("%.2f",beyondPlayerRatio*100);
            DialogUtils.showPopup(TrayIcon.MessageType.INFO,"您本次载入游戏用时"+displayTime+"秒","超越了"+beyondPerc+"%的玩家！");
            MusicPlayer.playStartupMusic();
    }
}
