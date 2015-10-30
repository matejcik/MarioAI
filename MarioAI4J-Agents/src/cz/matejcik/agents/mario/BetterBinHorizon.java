package cz.matejcik.agents.mario;

import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;

/**
 * Created by matejcik on 26.10.15.
 */
public class BetterBinHorizon {

	public static int fromTerrain(Tiles t, Entities e, MarioEntity mario)
	{
		final int LOOK = 6;
		int height[] = new int[LOOK];
		boolean enemies[] = new boolean[LOOK];
		boolean spikes[] = new boolean[LOOK];

		for (int x = 0; x < LOOK; ++x) {
			int y;
			boolean danger = false;
			boolean spike = false;
			for (y = -1; y <= 2; ++y) {
				if (t.brick(x,y))
					break;
			}
			for (y = -2; y <= 2; ++y) {
				if (e.danger(x-1, y)) {
					danger = true;
					if (!e.squishy(x-1,y)) spike = true;
				}
			}
			height[x] = y - 1;
			enemies[x] = danger;
			spikes[x] = spike;
		}

		int spikebits = 0;
		for (int i = 0; i < 3; ++i) {
			spikebits <<= 1;
			if (spikes[i]) spikebits |= 1;
		}

		int result = spikebits & 0x07;
		result <<= 1;
		if (!mario.onGround) result |= 1;

		int enemybits = 0;
		for (int i = 0; i < LOOK; ++i) {
			enemybits <<= 1;
			if (enemies[i]) enemybits |= 1;
		}
		result <<= LOOK;
		result |= enemybits;

		int h3 = Math.max(Math.abs(height[2]), Math.abs(height[3]));
		if (h3 == Math.abs(height[2])) h3 = height[2];
		else h3 = height[3];
		int terrainnum = (height[0] + 2) * 25 + (height[1] + 2) * 5 + h3 + 2;
		result <<= 7;
		result |= terrainnum & 0x8f;

		int h4 = ((height[4] + height[5]) / 2) + 2;
		h4 >>= 1;
		result <<= 2;
		result |= h4 & 0x03;

		return result;
	}
}
