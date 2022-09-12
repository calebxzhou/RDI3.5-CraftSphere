package calebzhou.rdi.core.client.loader;

import calebzhou.rdi.core.client.FileConst;
import calebzhou.rdi.core.client.util.LazyBitmapFont;
import calebzhou.rdi.hifont.HiFont;
import calebzhou.rdi.hifont.TextBeingDraw;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import org.joml.Math;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.stream.IntStream;

public class LoadProgressDisplay extends ApplicationAdapter implements InputProcessor {
	public static void main(String[] args) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setWindowedMode(1280,720);
		config.setTitle("RDI客户端启动中");
		config.setResizable(false);
		config.setWindowIcon(Files.FileType.Classpath, FileConst.RDI_ICON_PATH);

		LoadProgressDisplay progressDisplay = new LoadProgressDisplay();
		Lwjgl3Application lwjgl3Application = new Lwjgl3Application(progressDisplay, config);
	}
    public static  LoadProgressDisplay INSTANCE;// = new LoadProgressDisplay();
    private long loadStartTime;
    private long loadEndTime;
   /* private JTextArea loadProgressInfo  ;
    private JProgressBar loadProgressBar  ;
    private JFrame loadProgressFrame ;*/
	// The window handle
	private long window;
    private LoadProgressDisplay(){

        //if(Util.getPlatform() != Util.OS.WINDOWS) return;
        /*loadProgressInfo = new JTextArea("RDI客户端正在启动....\n");
        loadProgressBar = new JProgressBar();
        loadProgressFrame= new JFrame("RDI客户端启动中");*/
    }

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		addText("t:"+character);
		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}

	@Override
	public void create() {


		HiFont.init();
		batch = new SpriteBatch();
		GLFWErrorCallback.createPrint(System.err).set();
		Gdx.input.setInputProcessor(this);
	}

	SpriteBatch batch;
	String textBeingDisplay="客户端正在准备...";
	public void addText(String text){
		textBeingDisplay=text;
	}
	public void render() {
		//清除上一帧的内容
		Gdx.gl.glClearColor( 0, 0, 0, 1 );
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );
		batch.begin();
		TextBeingDraw t1 = TextBeingDraw.create(textBeingDisplay, false, 100f, 200f, 16);
		BitmapFont bitmapFont = HiFont.drawText(batch, t1);
		batch.end();
		bitmapFont.dispose();
	}



    /*@Override
    public void run() {
        if(Util.getPlatform() != Util.OS.WINDOWS) return;
        RdiSystemTray.createTray();

        loadProgressBar.setMaximum(7000);
        loadStartTime=System.currentTimeMillis();
        loadProgressFrame.setLayout(new BorderLayout());
        loadProgressFrame.setAlwaysOnTop (true);
        loadProgressFrame.setBounds(0,0,400,300);
        DefaultCaret caret = (DefaultCaret)loadProgressInfo.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        loadProgressInfo.setCaret(caret);

        JScrollPane scroll = new JScrollPane (loadProgressInfo,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        loadProgressFrame.add(scroll,BorderLayout.CENTER);
        loadProgressFrame.add(loadProgressBar,BorderLayout.SOUTH);
        loadProgressFrame.setLocationRelativeTo(null);
        loadProgressFrame.setVisible(true);
    }*/

    public void appendLoadProgressInfo(String info){
        /*if(Util.getPlatform() != Util.OS.WINDOWS) return;
        if(loadProgressFrame==null) return;
        loadProgressInfo.append(info);
        int barValue = loadProgressBar.getValue();

        if(info.startsWith("#")){
            ++barValue;
        }else{
            barValue+=50;
        }
        if(info.contains("启动游戏主线程")){
            loadProgressFrame.setBounds(loadProgressFrame.getX(),loadProgressFrame.getY()+loadProgressFrame.getHeight(),
                    loadProgressFrame.getWidth(),loadProgressFrame.getHeight());
        }
        loadProgressInfo.append("\n");
        loadProgressBar.setValue(barValue);
        loadProgressInfo.setCaretPosition(loadProgressInfo.getDocument().getLength());*/

    }
    public void onFinish(){
       /* if(Util.getPlatform() != Util.OS.WINDOWS) return;
        if(loadProgressFrame!=null){
            //停止载入界面
            loadEndTime = System.currentTimeMillis();
            loadProgressFrame.dispose();
            loadProgressFrame=null;
            float usedTime = (loadEndTime-loadStartTime)/1000.0f;
            String displayTime = String.format("%.2f",usedTime);
            //最快载入20秒
            int standardLoadTime = 20;
            double beyondPlayerRatio = 1.0 / (usedTime / standardLoadTime);
            if(beyondPlayerRatio>=1.0)
                beyondPlayerRatio=0.999;
            String beyondPerc = String.format("%.2f",beyondPlayerRatio*100);
            DialogUtils.showPopup(TrayIcon.MessageType.INFO,"您本次载入游戏用时"+displayTime+"秒","超越了"+beyondPerc+"%的玩家！");
            MusicPlayer.playStartupMusic();
        }*/
    }
}
