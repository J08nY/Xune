package sk.neuromancer.Xune.game;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

public class InputHandler implements Tickable{
	
	private GLFWKeyCallback keyCallback;
	private GLFWCursorPosCallback cursorCallback;
	private GLFWMouseButtonCallback mouseCallback;
	private GLFWScrollCallback scrollCallback;
	
	public class Key{
		private boolean isPressed = false;
		
		public void toggle(boolean isPressed){
			this.isPressed = isPressed;
		}
		
		public boolean isPressed(){
			return this.isPressed;
		}
	}
	
	public class Mouse{
		private double x,y;
		private boolean leftMB = false,rightMB = false;
		private double lastX,lastY;
		private boolean wasDragged = false;
		
		public void setPosition(double x, double y){
			this.x = x;
			this.y = y;
		}
		
		public void pressLeft(){
			this.leftMB = true;
			this.lastX = x;
			this.lastY = y;
			this.wasDragged = false;
		}
		
		public void pressRight(){
			this.rightMB = true;
		}
		
		public void releaseLeft(){
			this.leftMB = false;
			if(this.lastX != this.x || this.lastY != this.y){
				wasDragged = true;
			}
		}
		
		public void releaseRight(){
			this.rightMB = false;
		}
		
		public double getX(){
			return this.x;
		}
		
		public double getY(){
			return this.y;
		}
		
		public double getLastX() {
			return lastX;
		}

		public double getLastY() {
			return lastY;
		}
		
		public boolean isLeftPressed(){
			return this.leftMB;
		}
		
		public boolean isRightPressed(){
			return this.rightMB;
		}
		
		public boolean wasDragged(){
			return this.wasDragged;
		}
		
		public void resetPress() {
			this.leftMB = false;
			this.rightMB = false;
		}
		
		public void resetDrag() {
			this.wasDragged = false;
		}


	}
	
	public class Scroller{
		private float x,y;
		private float deltaX,deltaY;
		
		public void scroll(double xOff, double yOff){
			deltaX=(float) xOff;
			deltaY=(float) yOff;
			x+=xOff;
			y+=yOff;
		}
		
		public float getXoffset(){
			return x;
		}
		
		public float getYoffset(){
			return y;
		}
		
		public float getDeltaX() {
			return deltaX;
		}

		public float getDeltaY() {
			return deltaY;
		}

		public void resetDelta() {
			deltaX = 0;
			deltaY = 0;
		}
	}
	
	public Key W = new Key();
	public Key A = new Key();
	public Key S = new Key();
	public Key D = new Key();
	public Key ESC = new Key();
	
	public Key PLUS = new Key();
	public Key MINUS = new Key();
	
	public Mouse mouse = new Mouse();
	
	public Scroller scroller = new Scroller();
	
	public InputHandler(Game g){
		long window = g.getWindow();
		glfwSetKeyCallback(window, keyCallback =  new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				boolean isPress = action != GLFW_RELEASE;
				switch(key){
				case GLFW_KEY_W:	W.toggle(isPress);
									break;
				case GLFW_KEY_A:	A.toggle(isPress);
									break;
				case GLFW_KEY_S:	S.toggle(isPress);
									break;
				case GLFW_KEY_D:	D.toggle(isPress);
									break;
				case GLFW_KEY_ESCAPE:ESC.toggle(isPress);
									break;
				case GLFW_KEY_KP_ADD: PLUS.toggle(isPress);
									break;
				case GLFW_KEY_KP_SUBTRACT: MINUS.toggle(isPress);
									break;

				}
			}
		});
		glfwSetCursorPosCallback(window, cursorCallback = new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xpos, double ypos) {
				mouse.setPosition(xpos, ypos);
			}
		});
		glfwSetMouseButtonCallback(window, mouseCallback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				if(action == GLFW_PRESS){
					switch(button){
					case GLFW_MOUSE_BUTTON_LEFT:	mouse.pressLeft();
													break;
					case GLFW_MOUSE_BUTTON_RIGHT:	mouse.pressRight();
													break;
					}
				}else{
					switch(button){
					case GLFW_MOUSE_BUTTON_LEFT:	mouse.releaseLeft();
													break;
					case GLFW_MOUSE_BUTTON_RIGHT:	mouse.releaseRight();
													break;
					}
				}
			}
		});
		glfwSetScrollCallback(window, scrollCallback = new GLFWScrollCallback() {
			@Override
			public void invoke(long window, double xoffset, double yoffset) {
				scroller.scroll(xoffset, yoffset);
			}
		}); 
	}
	
	@Override
	public void tick(int tickCount) {
		mouse.resetDrag();
		mouse.resetPress();
		scroller.resetDelta();
	}
	
	public void quit(){
		keyCallback.release();
		mouseCallback.release();
		cursorCallback.release();
		scrollCallback.release();
	}


	
}
