package sk.neuromancer.Xune.game;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.Clickable.Button;
import sk.neuromancer.Xune.entity.Entity.Flag;
import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.sfx.SoundManager;

public class Player extends EntityOwner {
    private Game game;
    private Level level;
    public int money;

    private PlayableEntity selected;

    public Player(Game g, Level level, Flag flag, int money) {
        super(flag, money);
        this.game = g;
        this.level = level;
        this.addEntity(new Base(24 * 3.5f, 11 * 0.5f, this, this.flag));
        this.addEntity(new Refinery(24 * 4f, 11 * 0f, this, this.flag));
        this.addEntity(new Heli(24 * 7.5f, 11 * 7.5f, this, this.flag));
        this.selected = null;
    }

    @Override
    public void tick(int tickCount) {
        float mouseX = (float) game.getInput().mouse.getX();
        float mouseY = (float) game.getInput().mouse.getY();

        if (game.getInput().mouse.isLeftPressed()) {
            float levelX = (float) game.getLevel().getLevelX(mouseX);
            float levelY = (float) game.getLevel().getLevelY(mouseY);

            boolean handled = false;
            if (selected != null) {
                if (!selected.onClick(levelX, levelY, Button.LEFT)) {
                    selected = null;
                } else {
                    handled = true;
                }
            }
            if (!handled) {
                for (Entity e : entities) {
                    boolean clicked = e.onClick(levelX, levelY, Button.LEFT);
                    if (clicked) {
                        selected = (PlayableEntity) e;
                        break;
                    }
                }
            }
        }

        super.tick(tickCount);
    }


}
