package sk.neuromancer.Xune.game;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.Clickable.Button;
import sk.neuromancer.Xune.entity.Entity.Flag;
import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.sfx.SoundManager;

import static sk.neuromancer.Xune.level.Tile.*;

public class Player extends EntityOwner {
    private Game game;
    private Level level;
    public int money;

    private PlayableEntity selected;

    public Player(Game g, Level level, Flag flag, int money) {
        super(flag, money);
        this.game = g;
        this.level = level;
        this.addEntity(new Base(tileX(1, 4), tileY(1, 4), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Refinery(tileX(2, 4), tileY(2, 4), Entity.Orientation.NORTH,this, this.flag));
        this.addEntity(new Silo(tileX(2, 3), tileY(2, 3), Entity.Orientation.NORTH,this, this.flag));
        this.addEntity(new Harvester(tileX(1, 7), tileY(1, 7), Entity.Orientation.NORTH,this, this.flag));
        this.addEntity(new Heli(tileX(7, 7), tileY(7, 7), Entity.Orientation.EAST,this, this.flag));
        this.selected = null;
    }

    @Override
    public void tick(int tickCount) {
        float mouseX = (float) game.getInput().mouse.getX();
        float mouseY = (float) game.getInput().mouse.getY();

        if (game.getInput().mouse.isLeftPressed()) {
            float levelX = game.getLevel().getLevelX(mouseX);
            float levelY = game.getLevel().getLevelY(mouseY);

            boolean handled = false;
            if (selected != null) {
                if (selected.onClick(levelX, levelY, Button.LEFT)) {
                    selected.unselect();
                    game.getSound().play(SoundManager.SOUND_BLIP_1);
                    selected = null;
                    handled = true;
                }
            }
            if (!handled) {
                for (Entity e : entities) {
                    boolean clicked = e.onClick(levelX, levelY, Button.LEFT);
                    if (clicked) {
                        selected = (PlayableEntity) e;
                        selected.select();
                        System.out.println("Selected " + e.getClass().getSimpleName());
                        game.getSound().play(SoundManager.SOUND_BLIP_1);
                        break;
                    }
                }
            }
        }

        super.tick(tickCount);
    }


}
