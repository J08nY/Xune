package sk.neuromancer.Xune.game;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.Entity.Flag;
import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.level.Level;

import java.util.LinkedList;
import java.util.List;

import static sk.neuromancer.Xune.level.Tile.tileX;
import static sk.neuromancer.Xune.level.Tile.tileY;

public class Player extends EntityOwner {
    private final Game game;
    private final Level level;
    public int money;

    private List<PlayableEntity> selected = new LinkedList<>();
    private boolean drag;
    private float fromX;
    private float fromY;

    public Player(Game g, Level level, Flag flag, int money) {
        super(flag, money);
        this.game = g;
        this.level = level;
        this.addEntity(new Base(tileX(1, 4), tileY(1, 4), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Refinery(tileX(2, 4), tileY(2, 4), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Silo(tileX(2, 3), tileY(2, 3), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Helipad(tileX(3, 3), tileY(3, 3), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Factory(tileX(2, 5), tileY(2, 5), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Harvester(tileX(1, 7), tileY(1, 7), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Heli(tileX(7, 7), tileY(7, 7), Entity.Orientation.EAST, this, this.flag));
        this.selected = null;
    }

    @Override
    public void tick(int tickCount) {
        if (game.getInput().mouse.isLeftReleased()) {
            float mouseX = (float) game.getInput().mouse.getX();
            float mouseY = (float) game.getInput().mouse.getY();
            float levelX = game.getLevel().getLevelX(mouseX);
            float levelY = game.getLevel().getLevelY(mouseY);

            if (drag) {
                // drag select
                float fromLevelX = game.getLevel().getLevelX(fromX);
                float fromLevelY = game.getLevel().getLevelY(fromY);
                //TODO Make the BB such that from is always lower than to
                System.out.printf("drag! %f %f %f %f\n", fromLevelX, fromLevelY, levelX, levelY);
                for (PlayableEntity e : entities) {
                    if (e.intersects(fromLevelX, fromLevelY, levelX, levelY, Clickable.Button.LEFT)) {
                        e.select();
                    }
                }

            } else {
                // click
            }
        }
        if (game.getInput().mouse.isLeftDrag()) {
            fromX = (float) game.getInput().mouse.getLastLeftX();
            fromY = (float) game.getInput().mouse.getLastLeftY();
            drag = true;
        } else {
            drag = false;
        }
            /*
            boolean handled = false;
            if (selected != null) {
                if (selected.onClick(levelX, levelY, Button.LEFT)) {
                    selected.unselect();
                    game.getSound().play(SoundManager.SOUND_BLIP_1, false);
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
                        game.getSound().play(SoundManager.SOUND_BLIP_1, false);
                        break;
                    }
                }
            }
             */

        super.tick(tickCount);
    }


}
