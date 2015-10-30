package cz.matejcik.agents.mario;

import ch.idsia.agents.IAgent;
import ch.idsia.agents.controllers.MarioHijackAIBase;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by matejcik on 29.10.15.
 */
public class SmallHorizonAgent extends MarioHijackAIBase implements IAgent, Comparable<SmallHorizonAgent>, MutationAgent {

	private static final int JUMP = 0x01;
	private static final int SHOOT = 0x02;
	private static final int RUN = 0x04;

	private static final Random random = new Random();

	public static final int ACTION_TABLE_SIZE = 65536;

	private byte[] actionTable = new byte[ACTION_TABLE_SIZE];
	private boolean[] actionsUsed = new boolean[ACTION_TABLE_SIZE];

	public int fitness = Integer.MIN_VALUE;

	public SmallHorizonAgent() {
		for (int i = 0; i < actionTable.length; ++i) {
			final int HIGHEST_BIT = 0x8000;
			actionTable[i] = (byte)random.nextInt(8);
			if ((i & HIGHEST_BIT) == HIGHEST_BIT) {
				actionTable[i] |= JUMP;
			}
			actionsUsed[i] = false;
		}
	}

	public SmallHorizonAgent(String filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			fis.read(actionTable);
			fis.close();
		} catch (IOException e) {
			throw new RuntimeException("bla");
		}
	}


	@Override
	public MutationAgent mutate (double mutationChance) {
		SmallHorizonAgent next = new SmallHorizonAgent();

		for (int i = 0; i < actionTable.length; ++i) {
			if (!actionsUsed[i]) continue;

			byte actions = actionTable[i];
			if (random.nextDouble() < mutationChance) actions ^= JUMP;
			if (random.nextDouble() < mutationChance) actions ^= RUN;
			if (random.nextDouble() < mutationChance) actions ^= SHOOT;

			next.actionTable[i] = actions;
		}

		return next;
	}

	@Override
	public void dump(String filename) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			fos.write(actionTable);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public MarioInput actionSelectionAI() {

		int horizon = SmallHorizon.fromTerrain(t, e, mario);
		byte actions = actionTable[horizon];
		action.set(MarioKey.RIGHT, (actions & RUN) == RUN);
		action.set(MarioKey.JUMP, ((actions & JUMP) == JUMP) && (mario.mayJump || !mario.onGround));
		action.set(MarioKey.SPEED, (actions & SHOOT) == SHOOT);

		return action;
	}

	@Override
	public int compareTo(SmallHorizonAgent agent) {
		// REVERSE ORDERING
		return agent.fitness - fitness;
	}

	@Override
	public int getFitness() { return fitness; }
	@Override
	public void setFitness(int fitness) { this.fitness = fitness; }
	@Override
	public void setLearning(boolean learning) {}
}
