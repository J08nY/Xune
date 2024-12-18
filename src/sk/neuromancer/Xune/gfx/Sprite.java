package sk.neuromancer.Xune.gfx;

import static org.lwjgl.opengl.GL11.GL_NEAREST_MIPMAP_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

import java.nio.ByteBuffer;

public class Sprite implements Renderable {
    protected int textureId;
    protected int width;
    protected int height;

    public static final int TEXTURE_UNIT = GL_TEXTURE0;

    public static final float DEFAULT_SCALE_FACTOR = 1f;

    public Sprite(int[] pixels, int width, int height) {
        this.width = width;
        this.height = height;
        ByteBuffer buff = ByteBuffer.allocateDirect((width) * (height) * 4);
        for (int i = 0; i < pixels.length / 4; i++) {
            buff.put((byte) pixels[i * 4]);
            buff.put((byte) pixels[i * 4 + 1]);
            buff.put((byte) pixels[i * 4 + 2]);
            buff.put((byte) pixels[i * 4 + 3]);
        }
        buff.flip();

        this.textureId = glGenTextures();
        glActiveTexture(TEXTURE_UNIT);
        glBindTexture(GL_TEXTURE_2D, textureId);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buff);
        glGenerateMipmap(GL_TEXTURE_2D);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
    }

    @Override
    public void render() {
        glEnable(GL_TEXTURE_2D);
        glActiveTexture(TEXTURE_UNIT);
        glBindTexture(GL_TEXTURE_2D, textureId);

        glPushMatrix();
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex2f(0, 0);

        glTexCoord2f(0.0f, 1.0f);
        glVertex2f(0, height);

        glTexCoord2f(1.0f, 1.0f);
        glVertex2f(width, height);

        glTexCoord2f(1.0f, 0.0f);
        glVertex2f(width, 0);
        glEnd();
        glPopMatrix();

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void destroy() {
        glDeleteTextures(textureId);
    }


    public static class ScalableSprite extends Sprite {
        private float scaleFactor = DEFAULT_SCALE_FACTOR;

        public ScalableSprite(int[] pixels, int width, int height) {
            super(pixels, width, height);
        }

        @Override
        public void render() {
            glEnable(GL_TEXTURE_2D);
            glActiveTexture(TEXTURE_UNIT);
            glBindTexture(GL_TEXTURE_2D, textureId);

            glPushMatrix();
            glScalef(scaleFactor, scaleFactor, 0);
            glBegin(GL_QUADS);
            glTexCoord2f(0.0f, 0.0f);
            glVertex2f(0, 0);

            glTexCoord2f(0.0f, 1.0f);
            glVertex2f(0, height);

            glTexCoord2f(1.0f, 1.0f);
            glVertex2f(width, height);

            glTexCoord2f(1.0f, 0.0f);
            glVertex2f(width, 0);
            glEnd();
            glPopMatrix();

            glBindTexture(GL_TEXTURE_2D, 0);
            glDisable(GL_TEXTURE_2D);
        }

        public float getScaleFactor() {
            return scaleFactor;
        }

        public void setScaleFactor(float scaleFactor) {
            this.scaleFactor = scaleFactor;
        }

    }

}
