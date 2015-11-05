package cz.matejcik.agents.mario;

import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;

/**
 * Created by matejcik on 26.10.15.
 */
public class BetterBinHorizon {

	private static final byte ABOVE   = 0b11;
	private static final byte SPIKE = 0b10;
	private static final byte SQUISHY = 0b01;
	private static final byte NONE    = 0b00;

	public static int fromTerrain(Tiles t, Entities e, MarioEntity mario)
	{
		final int LOOK = 5;
		byte data[] = new byte[LOOK];

		for (int x = 0; x < LOOK; ++x) {
			int y;
			boolean danger = false;
			boolean spike = false;
			boolean above = false;

			for (y = -4; y <= 4; ++y) {
				if (e.danger(x-1, y)) {
					danger = true;
					if (!e.squishy(x-1,y)) spike = true;
					if (y < 0) above = true;
				}
			}

			if (above) data[x] = ABOVE;
			else if (spike) data[x] = SPIKE;
			else if (danger) data[x] = SQUISHY;
			else data[x] = NONE;
		}

		int result = mario.onGround ? 0 : 1;

		for (int i = 0; i < LOOK; ++i) {
			result <<= 1;
			result |= (data[i] != NONE) ? 1 : 0;
		}

		return result;
	}

	/*
	public static String decode(int horizon)
	{
		int spikebits = horizon >> 14;
		boolean marioFlying = ((horizon >> 13) & 1) == 1;
		int enemybits = (horizon >> 6) & 0x3f;
		int terrainnum = horizon & 0x7f;
		int height[] = new int[3];
		height[2] = (terrainnum % 5) - 2;
		height[1] = ((terrainnum/5) % 5) - 2;
		height[0] = ((terrainnum/25) % 5) - 2;

		return String.format("spikes %s : %s : enemies %s : heights %d %d %d",
			Integer.toBinaryString(spikebits),
			marioFlying ? "flying" : "standing",
			Integer.toBinaryString(enemybits),
			height[0],height[1],height[2]);
	}*/
}
