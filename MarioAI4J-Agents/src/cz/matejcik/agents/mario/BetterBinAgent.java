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
public class BetterBinAgent extends MarioHijackAIBase implements IAgent, Comparable<BetterBinAgent>, MutationAgent {

	private static final int JUMP = 0x01;
	private static final int SHOOT = 0x02;
	private static final int RUN = 0x04;
	private static final int LEFT = 0x08;

	private static final Random random = new Random();

	public static final int ACTION_TABLE_SIZE = 65536 * 8;

	private byte[] actionTable = new byte[ACTION_TABLE_SIZE];
	private int[] actionUsed = new int[ACTION_TABLE_SIZE];

	public int fitness = Integer.MIN_VALUE;

	public BetterBinAgent() {
		for (int i = 0; i < actionTable.length; ++i) {
			final int HIGHEST_BIT = 0x8000;
			actionTable[i] = (byte)random.nextInt(16);
			if ((i & HIGHEST_BIT) == HIGHEST_BIT) {
				actionTable[i] |= JUMP;
			}
			actionUsed[i] = 0;
		}
	}

	public BetterBinAgent(String filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			int rb = fis.read(actionTable);
			if (rb < ACTION_TABLE_SIZE) {
				// extend action table with the new high bits
				System.out.println("extending definition");
				for (int i = 1; i < 8; ++i) {
					System.arraycopy(actionTable, 0, actionTable, rb * i, rb);
				}
			}
			fis.close();
		} catch (IOException e) {
			throw new RuntimeException("bla");
		}
	}


	@Override
	public MutationAgent mutate (double mutationChance) {
		BetterBinAgent next = new BetterBinAgent();

		for (int i = 0; i < actionTable.length; ++i) {
			if (actionUsed[i] > 500) mutationChance *= 0.01;
			else if (actionUsed[i] > 100) mutationChance *= 0.2;
			else if (actionUsed[i] > 50) mutationChance *= 0.5;

			byte actions = actionTable[i];
			if (random.nextDouble() < mutationChance) actions ^= JUMP;
			if (random.nextDouble() < mutationChance) actions ^= RUN;
			if (random.nextDouble() < mutationChance) actions ^= SHOOT;
			if (random.nextDouble() < mutationChance) actions ^= LEFT;

			next.actionTable[i] = actions;
		}

		return next;
	}

	@Override
	public MutationAgent offspring(MutationAgent other, double mutationChance) {
		BetterBinAgent dad = (BetterBinAgent)other;
		BetterBinAgent kid = new BetterBinAgent();
		for (int i = 0; i < actionTable.length; ++i) {
			if (random.nextBoolean()) {
				kid.actionTable[i] = dad.actionTable[i];
				kid.actionUsed[i] = dad.actionUsed[i];
			} else {
				kid.actionTable[i] = actionTable[i];
				kid.actionUsed[i] = actionUsed[i];
			}
		}
		return kid.mutate(mutationChance);
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

		int horizon = BetterBinHorizon.fromTerrain(t, e, mario);
		byte actions = actionTable[horizon];
		action.set(MarioKey.RIGHT, (actions & RUN) == RUN);
		action.set(MarioKey.JUMP, ((actions & JUMP) == JUMP) && (mario.mayJump || !mario.onGround));
		action.set(MarioKey.SPEED, (actions & SHOOT) == SHOOT);
		action.set(MarioKey.LEFT, ((actions & LEFT) == LEFT));

		return action;
	}

	@Override
	public int compareTo(BetterBinAgent agent) {
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
