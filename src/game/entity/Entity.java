package game.entity;

import java.util.List;
import java.util.Random;

import game.level.Level;
import game.level.tile.Tile;
import game.math.BB;
import game.math.BBOwner;
import game.math.Vec2;
import game.gfx.Art;
import game.gfx.Screen;

public abstract class Entity implements BBOwner {

	protected Random random = new Random();
	
	public Level level;
	public boolean removed;
	
	public Vec2 pos = new Vec2(0, 0);
	public Vec2 radius = new Vec2(10, 10);

	public boolean isBlocking = true;
	public boolean physicsSlide = true;

	public int xto;
	public int yto;
	public double xd, yd;
	
	public int minimapIcon = -1;
	public int minimapColor = -1;
	public int team;

	public void setPos(double x, double y) {
		pos.set(x, y);
	}
	
	public void setPos(Vec2 position) {
		if(position != null) {
			pos.x = position.x;
	    	pos.y = position.y;
		}
    }

	public void setSize(int xr, int yr) {
		radius.set(xr, yr);
	}

	public final void init(Level l) {
		level = l;
		init();
	}

	public void init() {
	}

	public void tick(){
	}
	
	protected boolean move(double xa, double ya) {
		List<BB> bbs = level.getClipBBs(this);
		if (physicsSlide || (xa==0||ya==0)) {
			boolean moved = false;
			if (!removed)
				moved |= partMove(bbs, xa, 0);
			if (!removed)
				moved |= partMove(bbs, 0, ya);
			return moved;
		} else {
			boolean moved = true;
			if (!removed)
				moved &= partMove(bbs, xa, 0);
			if (!removed)
				moved &= partMove(bbs, 0, ya);
			return moved;
		}
	}

	private boolean partMove(List<BB> bbs, double xa, double ya) {
		double oxa = xa;
		double oya = ya;
		BB from = getBB();

		BB closest = null;
		double epsilon = 0.01;
		for (int i = 0; i < bbs.size(); i++) {
			BB to = bbs.get(i);
			if (from.intersects(to))
				continue;

			if (ya == 0) {
				if (to.y0 >= from.y1 || to.y1 <= from.y0)
					continue;
				if (xa > 0) {
					double xrd = to.x0 - from.x1;
					if (xrd >= 0 && xa > xrd) {
						closest = to;
						xa = xrd - epsilon;
						if (xa < 0)
							xa = 0;
					}
				} else if (xa < 0) {
					double xld = to.x1 - from.x0;
					if (xld <= 0 && xa < xld) {
						closest = to;
						xa = xld + epsilon;
						if (xa > 0)
							xa = 0;
					}
				}
			}

			if (xa == 0) {
				if (to.x0 >= from.x1 || to.x1 <= from.x0)
					continue;
				if (ya > 0) {
					double yrd = to.y0 - from.y1;
					if (yrd >= 0 && ya > yrd) {
						closest = to;
						ya = yrd - epsilon;
						if (ya < 0)
							ya = 0;
					}
				} else if (ya < 0) {
					double yld = to.y1 - from.y0;
					if (yld <= 0 && ya < yld) {
						closest = to;
						ya = yld + epsilon;
						if (ya > 0)
							ya = 0;
					}
				}
			}
		}
		if (closest != null && closest.owner != null) {
			closest.owner.handleCollision(this, oxa, oya);
		}
		if (xa != 0 || ya != 0) {
			pos.x += xa;
			pos.y += ya;
			return true;
		}
		return false;
	}
	
	public void render(Screen s) {
		s.draw(Art.entityFiller, pos.x - Tile.WIDTH / 2, pos.y - Tile.HEIGHT / 2 - 8);
	}
	
	public void renderTop(Screen s) {
	}
	
	public final boolean blocks(Entity e) {
		return isBlocking && e.isBlocking && shouldBlock(e) && e.shouldBlock(this);
	}

	protected boolean shouldBlock(Entity e) {
		return true;
	}
	
	public BB getBB() {
		return new BB(this, pos.x - radius.x, pos.y - radius.y, pos.x + radius.x, pos.y + radius.y);
	}

	public boolean intersects(double x0, double y0, double x1, double y1) {
		return getBB().intersects(x0, y0, x1, y1);
	}

	@Override
	public void handleCollision(Entity entity, double xa, double ya) {
		if (this.blocks(entity)) {
			this.collide(entity, xa, ya);
			entity.collide(this, -xa, -ya);
		}
	}

	public void collide(Entity entity, double xa, double ya) {
	}
	
	public void remove() {
		removed = true;
	}

	public void hurt() {
	}
}