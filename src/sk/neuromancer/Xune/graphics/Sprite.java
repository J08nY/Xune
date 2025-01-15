package sk.neuromancer.Xune.graphics;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Sprite implements Renderable {
    private final int textureId;
    private final int width;
    private final int height;
    private final int[] pixels;

    public static final int TEXTURE_UNIT = GL_TEXTURE0;

    public Sprite(int[] pixels, int width, int height, boolean keepData) {
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
        if (keepData) {
            this.pixels = Arrays.copyOf(pixels, pixels.length);
        } else {
            this.pixels = null;
        }

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

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getPixels() {
        return pixels;
    }

    public int getPixelAt(int x, int y, boolean withAlpha) {
        if (pixels == null) {
            return 0;
        }
        if (withAlpha) {
            return pixels[(y * width + x) * 4] << 24 | pixels[(y * width + x) * 4 + 1] << 16 | pixels[(y * width + x) * 4 + 2] << 8 | pixels[(y * width + x) * 4 + 3];
        } else {
            return pixels[(y * width + x) * 4] << 16 | pixels[(y * width + x) * 4 + 1] << 8 | pixels[(y * width + x) * 4 + 2];
        }
    }


    public void destroy() {
        glDeleteTextures(textureId);
    }
}
