package sk.neuromancer.Xune.game.screens;

import sk.neuromancer.Xune.input.InputHandler;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.graphics.Renderable;
import sk.neuromancer.Xune.graphics.elements.Text;

import java.util.Arrays;

public class Menu implements Renderable, Tickable {
    private final String[] options;
    private final String name;
    private final InputHandler input;
    private final String spacer;
    private final float scale;
    private final float w, h;

    private int selected = 0;
    private boolean active = false;

    public Menu(InputHandler input, String name, String[] options, float scale) {
        this.options = options;
        this.name = name;
        this.input = input;
        int len = Arrays.stream(options).map(String::length).max(Integer::compareTo).orElse(0);
        this.spacer = "-".repeat(len);
        this.scale = scale;
        this.h = Text.heightOf("X", scale);
        this.w = Text.widthOf(">", scale);
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    @Override
    public void tick(int tickCount) {
        if (!active) {
            return;
        }
        if ((input.W.wasPressed() || input.UP.wasPressed()) && selected > 0) {
            selected = (selected - 1) % options.length;
        }
        if ((input.S.wasPressed() || input.DOWN.wasPressed()) && selected < options.length - 1) {
            selected = (selected + 1) % options.length;
        }
    }

    @Override
    public void render() {
        if (options.length > 1) {
            new Text(">", -w, selected * h, true, scale).render();
        }
        new Text(name, 0, -h * 3, true, scale).render();
        for (int i = 0; i < options.length; i++) {
            new Text(options[i], 0, i * h, true, scale).render();
        }
        if (active) {
            new Text(spacer, 0, -h, true, scale).render();
            new Text(spacer, 0, options.length * h, true, scale).render();
        }
    }
}
