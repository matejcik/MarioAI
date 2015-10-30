package cz.matejcik.agents.mario;

import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by matejcik on 30.10.15.
 */
public class SemHorizon implements Serializable {

	private static final Random random = new Random();

	private static final int MAXHEIGHT = 6;
	public static final int MAXDIST = 8;

	private byte distanceToObstacle = MAXDIST;
	private byte obstacleHeight = MAXHEIGHT;

	private byte distanceToEnemy = MAXDIST;
	private byte distanceToDrop = MAXDIST;

	public boolean onGround;
	private byte myHeight = MAXHEIGHT;

	public static SemHorizon randomHorizon()
	{
		SemHorizon s = new SemHorizon();
		s.distanceToObstacle = (byte)random.nextInt(MAXDIST);
		s.distanceToDrop = (byte)random.nextInt(MAXDIST);
		s.distanceToEnemy = (byte)random.nextInt(MAXDIST);
		s.myHeight = (byte)random.nextInt(MAXHEIGHT);
		s.obstacleHeight = (byte)random.nextInt(MAXHEIGHT);
		s.onGround = random.nextBoolean();
		return s;
	}

	public static SemHorizon fromTerrain(Tiles t, Entities e, MarioEntity m)
	{
		SemHorizon s = new SemHorizon();

		for (int y = MAXHEIGHT - 1; y > 0; --y) {
			if (t.brick(0, y)) {
				s.myHeight = (byte)y;
			} else {
				break;
			}
		}

		for (int x = 1; x < MAXDIST; ++x) {
			if (t.brick(x, 0) && s.distanceToObstacle < x) {
				s.distanceToObstacle = (byte)x;
				byte h = 1;
				while (t.brick(x, -h)) ++h;
				s.obstacleHeight = (byte)Math.min(h, MAXHEIGHT);
			}
			if (s.distanceToEnemy < x) {
				for (int y = 0; y >= -2; --y) {
					if (e.danger(x,y)) {
						s.distanceToEnemy = (byte)x;
						break;
					}
				}
			}
			if (s.distanceToDrop < x && !t.brick(x, 1)) {
				s.distanceToDrop = (byte)x;
			}
		}

		s.onGround = m.onGround;
		return s;
	}

	public double distance(SemHorizon other) {
		return Math.sqrt(
			Math.pow(distanceToDrop - other.distanceToDrop, 2)
			+ Math.pow(distanceToEnemy - other.distanceToEnemy, 2)
			+ Math.pow(distanceToObstacle - other.distanceToObstacle, 2)
			+ Math.pow(obstacleHeight - other.obstacleHeight, 2) * 0.5
			+ Math.pow(myHeight - other.myHeight, 2) * 0.3
			+ ((onGround == other.onGround) ? 0 : 25)
		);
	}

	private static byte mutateWithLimit (byte what, int limit, double chance)
	{
		if (random.nextDouble() > chance) return what;
		/*double r = Math.pow(random.nextDouble() * limit, 2.5);
		int rr = 0;
		if (r > 0) rr = (int)Math.floor(r);
		else rr = (int)Math.ceil(r);*/
		int rr = random.nextInt(7) - 3;
		int res = what + rr;
		res = Math.max(res, 0);
		res = Math.min(res, limit - 1);

		return (byte)res;
	}

	public SemHorizon mutate(double mutationChance)
	{
		SemHorizon s = new SemHorizon();
		s.distanceToEnemy = mutateWithLimit(distanceToEnemy, MAXDIST, mutationChance);
		s.distanceToDrop = mutateWithLimit(distanceToDrop, MAXDIST, mutationChance);
		s.distanceToObstacle = mutateWithLimit(distanceToObstacle, MAXDIST, mutationChance);
		s.myHeight = mutateWithLimit(myHeight, MAXHEIGHT, mutationChance);
		s.obstacleHeight = mutateWithLimit(obstacleHeight, MAXHEIGHT, mutationChance);
		return s;
	}

	@Override
	public int hashCode() {
		return (distanceToDrop << 26)
			| (distanceToEnemy << 20)
			| (distanceToObstacle << 14)
			| (obstacleHeight << 6)
			| myHeight;
	}

	@Override
	public boolean equals (Object o) {
		if (!(o instanceof SemHorizon)) return false;
		SemHorizon other = (SemHorizon)o;

		return
			other.obstacleHeight == obstacleHeight
			&& other.distanceToObstacle == distanceToObstacle
			&& other.onGround == onGround
			&& other.distanceToEnemy == distanceToEnemy
			&& other.myHeight == myHeight;
	}
}
