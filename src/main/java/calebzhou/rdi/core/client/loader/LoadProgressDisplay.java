package calebzhou.rdi.core.client.loader;

import calebzhou.rdi.core.client.util.LazyBitmapFont;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;

public class LoadProgressDisplay extends ApplicationAdapter {
	public static void main(String[] args) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("My GDX Game");
		new Lwjgl3Application(new LoadProgressDisplay(), config);
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
	public void create() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");
		init();
	}
	LazyBitmapFont font;
	SpriteBatch batch;
	private void init() {
		font= new LazyBitmapFont(new FreeTypeFontGenerator(new FileHandle("C:\\Users\\liberatorch\\IdeaProjects\\RDICore3\\src\\main\\resources\\pingfang.ttf")), 32);
		batch = new SpriteBatch();
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

	}
	public void render() {
		// Set the clear color
		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
		batch.begin();
		font.draw(batch,"啊啊123传送到摧毁",100f,100f);
		batch.end();
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
