package sk.neuromancer.Xune.prototype;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GLContext;

public class GameClass {
	private long window;
	public static final int WIDTH = 900;
	public static final int HEIGHT = 600;

	private GLFWKeyCallback keyCallback;
	private GLFWCursorPosCallback cursorCallback;
	private GLFWMouseButtonCallback mouseCallback;

	private boolean keepRunning = true;

	private boolean keyW,keyA,keyS,keyD;
	private int mouseX,mouseY;
	private boolean mouseRight,mouseLeft;

	private int[] level =  {0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,4,0,0,0,0,0,0,0,0,0,0,
			0,3,5,0,0,0,0,0,0,0,0,0,0,
			0,6,7,8,0,0,0,0,0,0,0,0,0,
			0,9,11,0,0,0,0,0,0,0,0,0,0,
			0,0,10,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,1,0,1,1,0,0,0,0,0,
			0,0,0,0,0,0,1,0,1,0,0,0,0,
			0,0,0,1,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,1,1,0,0,0,0,0,0,
			0,0,0,0,1,1,1,0,0,0,0,0,0,
			0,0,0,0,0,1,0,0,0,0,0,0,0,
			0,0,0,0,0,0,16,0,0,0,0,0,0,
			0,0,0,0,0,0,4,0,0,0,0,0,0,
			0,0,0,0,0,4,6,5,0,0,0,0,0,
			0,0,0,0,3,5,15,8,0,0,0,0,0,
			0,0,0,0,3,7,13,14,0,0,0,0,0,
			0,0,0,3,7,2,2,0,0,0,0,0,0,
			0,0,0,6,7,7,2,0,0,0,0,0,0,
			0,0,0,9,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0};

	private int levelWidth = 13;
	private int levelHeight = level.length/levelWidth;
	
	private int[] tiles;
	private int[] entities;
	
	private float carX = WIDTH/2,carY = HEIGHT/2;
	private int carCommandX = -1,carCommandY = -1;
	private float heliX = WIDTH/3,heliY = HEIGHT/4;
	private boolean carSelected = false;
	private int carSpeed = 4;
	
	private float rocketX = heliX,rocketY = heliY;
	private int rocketSpeed = 10;
	
	public static final float SCALE_FACTOR = 3f;
	
	private int[] showcase = {0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			1,2,3,4,5,6,7,8,9,10,11,12,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			13,14,15,16,17,18,19,20,21,22,23,24,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			25,26,27,28,29,30,31,32,33,34,35,36,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			37,38,39,40,41,42,43,44,45,46,47,48,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0};
	
	private void start(){
		init();
		run();

		glfwDestroyWindow(window);
		keyCallback.release();
		cursorCallback.release();
		mouseCallback.release();
		glfwTerminate();
	}

	private void init(){
		glfwInit();

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);

		window = glfwCreateWindow(WIDTH, HEIGHT, "Xune", NULL, NULL);

		ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(
				window,
				(GLFWvidmode.width(vidmode) - WIDTH) / 2,
				(GLFWvidmode.height(vidmode) - HEIGHT) / 2
				);
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);

		glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				boolean keyValue = true;
				if(action == GLFW_RELEASE)
					keyValue = false;

				if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
					keepRunning = false;
				else if(key == GLFW_KEY_W)
					keyW = keyValue;
				else if(key == GLFW_KEY_A)
					keyA = keyValue;
				else if(key == GLFW_KEY_S)
					keyS = keyValue;
				else if(key == GLFW_KEY_D)
					keyD = keyValue;
			}
		});

		glfwSetMouseButtonCallback(window, mouseCallback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				boolean buttonValue = true;
				if(action == GLFW_RELEASE)
					buttonValue = false;
				
				if(button == GLFW_MOUSE_BUTTON_LEFT){
					mouseLeft = buttonValue;
				}
				else if(button == GLFW_MOUSE_BUTTON_RIGHT){
					mouseRight = buttonValue;
				}
					
			}
		});

		glfwSetCursorPosCallback(window, cursorCallback = new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xpos, double ypos) {
				mouseX = (int) xpos;
				mouseY = (int) ypos;
			}
		}); 
		
		glfwShowWindow(window);

		GLContext.createFromCurrent();

		tiles = new int[18];
		for(int i = 0;i<18;i++){
			tiles[i] = getTexture("tilemap.png",(i%3)*24 , (i/3)*11, ((i%3)+1)*24, ((i/3)+1)*11);
		}
		
		entities = new int[48];
		for(int i = 0;i<48;i++){
			entities[i] = getTexture("entities.png",(i%12)*24 , (i/12)*11, ((i%12)+1)*24, ((i/12)+1)*11);
		}
		
		glViewport(0,0,WIDTH,HEIGHT);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, WIDTH, HEIGHT, 0, -1, 1);
		
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_DEPTH_TEST);
		
		glEnable(GL_BLEND); 
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	}

	private void run(){
		while(keepRunning){
			glClear(GL_COLOR_BUFFER_BIT);
			if(glfwWindowShouldClose(window) == GL_TRUE)
				keepRunning = false;

			glMatrixMode(GL_MODELVIEW);                             
			glLoadIdentity();
			glActiveTexture(GL_TEXTURE0);
			
			for(int x = 0;x<levelWidth;x++){
				for(int y = 0;y<levelHeight;y++){
					glBindTexture(GL_TEXTURE_2D, tiles[level[y*levelWidth+x]]);
					glLoadIdentity();
					
					float xx = x-0.5f;
					float yy = y*0.5f- 0.5f;
					if(y%2 != 0){
						xx+=0.5;
					}
					
					glScalef(SCALE_FACTOR, SCALE_FACTOR, 0);
					glBegin(GL_QUADS);
					glTexCoord2f(0.0f,0.0f); 
					glVertex2f(xx*24,yy*11);
					
					glTexCoord2f(0.0f,1.0f); 
					glVertex2f(xx*24,(yy+1)*11);
					
					glTexCoord2f(1.0f,1.0f); 
					glVertex2f((xx+1)*24,(yy+1)*11);
					
					glTexCoord2f(1.0f,0.0f); 
					glVertex2f((xx+1)*24,yy*11);
					glEnd();
					
					glBindTexture(GL_TEXTURE_2D, tiles[17]);
					glBegin(GL_QUADS);
					glTexCoord2f(0.0f,0.0f); 
					glVertex2f(xx*24,yy*11);
					
					glTexCoord2f(0.0f,1.0f); 
					glVertex2f(xx*24,(yy+1)*11);
					
					glTexCoord2f(1.0f,1.0f); 
					glVertex2f((xx+1)*24,(yy+1)*11);
					
					glTexCoord2f(1.0f,0.0f); 
					glVertex2f((xx+1)*24,yy*11);
					glEnd();
					
					
					if(showcase[y*levelWidth+x] == 0)
						continue;
					glBindTexture(GL_TEXTURE_2D, entities[showcase[y*levelWidth+x] - 1]);
					glBegin(GL_QUADS);
					glTexCoord2f(0.0f,0.0f); 
					glVertex2f(xx*24,yy*11);
					
					glTexCoord2f(0.0f,1.0f); 
					glVertex2f(xx*24,(yy+1)*11);
					
					glTexCoord2f(1.0f,1.0f); 
					glVertex2f((xx+1)*24,(yy+1)*11);
					
					glTexCoord2f(1.0f,0.0f); 
					glVertex2f((xx+1)*24,yy*11);
					glEnd();
					
				}
			}
			glBindTexture(GL_TEXTURE_2D, entities[5]);//bluecar
			glLoadIdentity();
			glTranslatef(carX, carY, 0);
			
			glScalef(SCALE_FACTOR, SCALE_FACTOR, 0);
			glBegin(GL_QUADS);
			glTexCoord2f(0.0f,0.0f); 
			glVertex2f(0,0);
			
			glTexCoord2f(0.0f,1.0f); 
			glVertex2f(0,11);
			
			glTexCoord2f(1.0f,1.0f); 
			glVertex2f(24,11);
			
			glTexCoord2f(1.0f,0.0f); 
			glVertex2f(24,0);
			glEnd();
			
			if(carSelected){
				glBindTexture(GL_TEXTURE_2D, tiles[17]);
				glBegin(GL_QUADS);
				glTexCoord2f(0.0f,0.0f); 
				glVertex2f(0,0);
				
				glTexCoord2f(0.0f,1.0f); 
				glVertex2f(0,11);
				
				glTexCoord2f(1.0f,1.0f); 
				glVertex2f(24,11);
				
				glTexCoord2f(1.0f,0.0f); 
				glVertex2f(24,0);
				glEnd();
			}
			
			
			System.out.println(rocketX + " " + rocketY);
			glLoadIdentity();
			glBindTexture(GL_TEXTURE_2D, tiles[17]);
			glTranslatef(rocketX, rocketY, 0);
			glScalef(SCALE_FACTOR,SCALE_FACTOR,0f);
			glBegin(GL_QUADS);
			glTexCoord2f(0.0f,0.0f); 
			glVertex2f(0,0);
			
			glTexCoord2f(0.0f,1.0f); 
			glVertex2f(0,11);
			
			glTexCoord2f(1.0f,1.0f); 
			glVertex2f(24,11);
			
			glTexCoord2f(1.0f,0.0f); 
			glVertex2f(24,0);
			glEnd();
			
			glBindTexture(GL_TEXTURE_2D, entities[18]);//red heli
			glLoadIdentity();
			glTranslatef(heliX, heliY, 0);
			
			glScalef(SCALE_FACTOR, SCALE_FACTOR, 0);
			glBegin(GL_QUADS);
			glTexCoord2f(0.0f,0.0f); 
			glVertex2f(0,0);
			
			glTexCoord2f(0.0f,1.0f); 
			glVertex2f(0,11);
			
			glTexCoord2f(1.0f,1.0f); 
			glVertex2f(24,11);
			
			glTexCoord2f(1.0f,0.0f); 
			glVertex2f(24,0);
			glEnd();

			System.out.println("Mouse x:" + mouseX + " y:" + mouseY);
			if(keyW)
				heliY-=carSpeed;
				System.out.print("W");
			if(keyA)
				heliX-=carSpeed;
				System.out.print("A");
			if(keyS)
				heliY+=carSpeed;
				System.out.print("S");
			if(keyD)
				heliX+=carSpeed;
				System.out.print("D");
			System.out.println();
			if(mouseLeft){
				System.out.print("LMB ");
				if(carSelected){
					if(mouseX > carX && mouseY > carY && mouseX < (carX + 24*SCALE_FACTOR) && mouseY < (carY + 11*SCALE_FACTOR)){
						//niè
					}else{
						carCommandX = mouseX-36;
						carCommandY = mouseY-16;
						carSelected = false;
					}
				}else{
					if(mouseX > carX && mouseY > carY && mouseX < (carX + 24*SCALE_FACTOR) && mouseY < (carY + 11*SCALE_FACTOR)){
						carSelected = true;
					}
				}
				
			}
			if(mouseRight){
				System.out.print("RMB");
				if(carSelected){
					carSelected = false;
				}
			}
			System.out.println();
			
			
			if(rocketX != carX && rocketY != carY){
				float dx = Math.abs(rocketX-carX);
				float dy = Math.abs(rocketY-carY);
				double dist = Math.sqrt(dx*dx + dy*dy);
				double xChange = (rocketSpeed*dx)/dist;
				double yChange = (rocketSpeed*dy)/dist;
				if(rocketSpeed > dist){
					rocketX = carX;
					rocketY = carY;
				}else{
					if(rocketX > carX){
						rocketX-=xChange;
					}else{
						rocketX+=xChange;
					}
					if(rocketY > carY){
						rocketY-=yChange;
					}else{
						rocketY+=yChange;
					}
				}
			}else{
				rocketX = heliX;
				rocketY = heliY;
			}
			
			if((carX != carCommandX && carCommandX != -1 )||(carY != carCommandY && carCommandY != -1)){
				float dx = Math.abs(carCommandX-carX);
				float dy = Math.abs(carCommandY-carY);
				double dist = Math.sqrt(dx*dx + dy*dy);
				double xChange = (carSpeed*dx)/dist;
				double yChange = (carSpeed*dy)/dist;
				if(carSpeed > dist){
					carX = carCommandX;
					carY = carCommandY;
				}else{
					if(carX > carCommandX){
						carX-=xChange;
					}else{
						carX+=xChange;
					}
					if(carY > carCommandY){
						carY-=yChange;
					}else{
						carY+=yChange;
					}
				}
			}
			

			
			
			glfwSwapBuffers(window);
			glfwPollEvents();
			try {
				Thread.sleep(33);
			} catch (InterruptedException e) {
				e.printStackTrace();
				keepRunning = false;
			}
		}
	}
	
	private int getTexture(String imageName, int fromX, int fromY, int toX, int toY){
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("res/"+imageName));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		if(fromX == -1)
			fromX = 0;
		if(fromY == -1)
			fromY = 0;
		if(toX == -1)
			toX = img.getWidth();
		if(toY == -1)
			toY = img.getHeight();
		
		int width = toX-fromX;
		int height = toY-fromY;
		
		int[] RGBAData = null;
		RGBAData = img.getData().getPixels(fromX, fromY, width, height, RGBAData);
		ByteBuffer buff = ByteBuffer.allocateDirect((width)*(height)*4);
		for(int i = 0;i<RGBAData.length/4;i++){
			buff.put((byte) RGBAData[i*4]);
			buff.put((byte) RGBAData[i*4+1]);
			buff.put((byte) RGBAData[i*4+2]);
			buff.put((byte) RGBAData[i*4+3]);
		}
		buff.flip();
		
		int id = glGenTextures();
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, id);
		
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buff);
		
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		return id;
	}

	public static void main(String[] args){
		GameClass g = new GameClass();
		g.start();
	}
}
