package cz.matejcik.agents.mario;

import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;
import ch.idsia.benchmark.mario.engine.sprites.Mario;

/**
 * Created by matejcik on 26.10.15.
 */
public class SmallHorizon {

	public static int fromTerrain(Tiles t, Entities e, MarioEntity mario)
	{
		int height[] = new int[5];
		boolean enemies[] = new boolean[5];

		for (int x = 0; x <= 4; ++x) {
			int y;
			for (y = 1; y >= -1; --y) {
				if (t.brick(x,y))
					break;
			}
			height[x] = y;

			enemies[x] = e.danger(x,y+1);
		}

		return fromArrays(height, enemies, mario.onGround);
	}

	public static int fromArrays(int[] height, boolean[] enemies, boolean onGround) {
		int result = 0;

		for (int i = 0; i < height.length; ++i) {
			result <<= 3;
			result |= 0x03 & (height[i] + 1);
			if (enemies[i])
				result |= 0x04;
		}

		if (!onGround) result |= 0x8000;

		return result;
	}
}
