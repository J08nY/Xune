package sk.neuromancer.Xune.entity;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;
import sk.neuromancer.Xune.entity.Entity.Orientation;
import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Harvester extends PlayableEntity {
	private float spice;
	
	public Harvester(float x, float y, EntityOwner owner, Flag flag){
		super(x,y,owner,flag);
		this.orientation = Orientation.NORTH;
		int spriteOffset = orientation.ordinal();
		this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(Entity.SPRITE_ID_HARVESTER + PlayableEntity.getOffsetonFlag(flag) + spriteOffset * Entity.SPRITE_ROW_LENGTH);
	}

	@Override
	public void render() {
		glPushMatrix();
		glTranslatef(x,y,0);
		this.sprite.render();
		glPopMatrix();
	}

	@Override
	public void tick(int tickCount) {

	}

}
