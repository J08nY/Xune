package sk.neuromancer.Xune.gfx;

import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_HIDDEN;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glVertex3d;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Sprite.ScalableSprite;

public class HUD implements Tickable, Renderable{
	private Game game;

	private ScalableSprite currentCursor;
	private ScalableSprite logo;
	private ScalableSprite hudPanel;
	
	private double mouseX,mouseY;
	private double fromX,fromY;

	public HUD(Game game) {
		this.game = game;
		glfwSetInputMode(game.getWindow(),GLFW_CURSOR,GLFW_CURSOR_DISABLED);
		currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(3);
		currentCursor.setScaleFactor(2f);

		logo = SpriteSheet.LOGO.getSprite(0);
		logo.setScaleFactor(1f);

		hudPanel = SpriteSheet.HUD_PANEL.getSprite(0);
		hudPanel.setScaleFactor(Game.WIDTH/(float) hudPanel.getWidth());
		fromX = fromY = 0;
	}

	@Override
	public void render() {
		if(game.getInput().mouse.isLeftPressed()){
			glPushMatrix();
			glBegin(GL_QUADS);
			glColor4f(0f,1f,0f,0.2f);
			glVertex3d(fromX,fromY,0);
			glVertex3d(fromX,mouseY,0);
			glVertex3d(mouseX,mouseY,0);
			glVertex3d(mouseX,fromY,0);
			glEnd();
			glColor4f(1.f, 1.f, 1.f, 1.f);
			glPopMatrix();

		}
		
		glPushMatrix();
		glTranslated(0,Game.HEIGHT-(hudPanel.getHeight()*hudPanel.getScaleFactor()),0);
		glPushMatrix();
		glTranslated(10,10,0);
		glPopMatrix();
		hudPanel.render();
		glPopMatrix();
		
		glPushMatrix();
		renderText(150, Game.HEIGHT-(hudPanel.getHeight()*hudPanel.getScaleFactor())+30, "MONEY: " + game.getLevel().getPlayer().money);
		double mx = game.getInput().mouse.getX();
		double my = game.getInput().mouse.getY();
		
		renderText(150, Game.HEIGHT-(hudPanel.getHeight()*hudPanel.getScaleFactor())+60, "X: " + mx);
		renderText(150, Game.HEIGHT-(hudPanel.getHeight()*hudPanel.getScaleFactor())+90, "Y: " + my);
		renderText(300, Game.HEIGHT-(hudPanel.getHeight()*hudPanel.getScaleFactor())+60, "LEVELX: " + game.getLevel().getLevelX(mx));
		renderText(300, Game.HEIGHT-(hudPanel.getHeight()*hudPanel.getScaleFactor())+90, "LEVELY: " + game.getLevel().getLevelY(my));
		renderText(450, Game.HEIGHT-(hudPanel.getHeight()*hudPanel.getScaleFactor())+60, "SCREENX: " + game.getLevel().getScreenX(game.getLevel().getLevelX(mx)));
		renderText(450, Game.HEIGHT-(hudPanel.getHeight()*hudPanel.getScaleFactor())+90, "SCREENY: " + game.getLevel().getScreenY(game.getLevel().getLevelY(my)));
		
		glPopMatrix();
		
		glPushMatrix();
		glTranslated(mouseX - (currentCursor.getWidth() * currentCursor.getScaleFactor()) /2, mouseY - (currentCursor.getHeight() * currentCursor.getScaleFactor()) /2, 0);
		currentCursor.render();
		glPopMatrix();
		
		logo.render();
	}

	@Override
	public void tick(int tickCount) {
		mouseX = game.getInput().mouse.getX();
		mouseY = game.getInput().mouse.getY();
		
		if(mouseX < -20){
			mouseX = -20;
			
		}else if(mouseX > Game.WIDTH + 20){
			mouseX = Game.WIDTH + 20;
		}
		if(mouseY < -20){
			mouseY = -20;
		}else if(mouseY > Game.HEIGHT + 20){
			mouseY = Game.HEIGHT + 20;
		}
		glfwSetCursorPos(game.getWindow(), mouseX, mouseY);
		
		if(game.getInput().mouse.isLeftPressed()){
			fromX = game.getInput().mouse.getLastX();
			fromY = game.getInput().mouse.getLastY();
		}
	}
	
	private void renderText(float x, float y, String text){
		String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ  0123456789.,!?'\"-+=/\\%()<>:;";
		float textScaleFactor = SpriteSheet.TEXT_SHEET.getSprite(0).getScaleFactor();
		float scaledSpriteWidth = SpriteSheet.TEXT_SHEET.getSprite(0).getWidth()*textScaleFactor;
		text = text.toUpperCase();
		for(int i = 0;i<text.length();i++){
			int spriteId = charset.indexOf(text.charAt(i));
			glPushMatrix();
			glTranslated(x + scaledSpriteWidth*i,y,0);
			SpriteSheet.TEXT_SHEET.getSprite(spriteId).render();
			glPopMatrix();
		}
	}

}
