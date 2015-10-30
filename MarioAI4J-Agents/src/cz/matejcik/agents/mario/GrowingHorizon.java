package cz.matejcik.agents.mario;

import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;

import java.io.Serializable;
import java.util.Arrays;

public class GrowingHorizon implements Cloneable, Serializable {

	private boolean onGround;
	private boolean enemyBack;
	private int[] heights;
	private boolean[] enemies;

	private GrowingHorizon() { }

	@Override
	protected Object clone() throws CloneNotSupportedException {
		GrowingHorizon g = new GrowingHorizon();
		g.enemyBack = enemyBack;
		g.heights = heights.clone();
		g.enemies = enemies.clone();

		return g;
	}

	public static GrowingHorizon fromTerrain(int look, Tiles t, Entities e, MarioEntity mario)
	{
		GrowingHorizon g = new GrowingHorizon();
		g.heights = new int[look + 1];
		g.enemies = new boolean[look + 1];

		for (int x = 0; x <= look; ++x) {
			int y;
			boolean danger = false;
			for (y = -3; y <= 2; ++y) {
				if (e.danger(x,y)) danger = true;
				if (t.brick(x, y))
					break;
			}
			g.heights[x] = y;
			g.enemies[x] = danger;
		}

		for (int x = -1; x > -3; --x) {
			for (int y = -3; y <= 0; ++y) {
				if (e.danger(x,y)) {
					g.enemyBack = true;
					break;
				}
			}
		}

		g.onGround = mario.onGround;
		return g;
	}

	@Override
	public int hashCode() {
		int result = 0;

		for (int i = 0; i < heights.length; ++i) {
			result <<= 3;
			result |= 0x03 & (heights[i] + 2);
			if (enemies[i])
				result |= 0x04;
		}

		return result;
	}

	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof GrowingHorizon)) return false;
		GrowingHorizon g = (GrowingHorizon)other;

		if (!Arrays.equals(g.enemies ,enemies)) return false;
		if (!Arrays.equals(g.heights, heights)) return false;
		if (g.onGround != onGround) return false;
		return g.enemyBack == enemyBack;
	}
}
