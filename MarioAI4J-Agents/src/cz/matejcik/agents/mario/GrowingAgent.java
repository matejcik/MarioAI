package cz.matejcik.agents.mario;

import ch.idsia.agents.IAgent;
import ch.idsia.agents.controllers.MarioHijackAIBase;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by matejcik on 29.10.15.
 */
public class GrowingAgent extends MarioHijackAIBase implements IAgent, Comparable<GrowingAgent>, Serializable, MutationAgent {

	private static final int JUMP = 0x01;
	private static final int SHOOT = 0x02;
	private static final int RUN = 0x04;

	private static final Random random = new Random();

	public static final int ACTION_TABLE_SIZE = 65536;

	private Map<GrowingHorizon, Byte> actionMap = new HashMap<>();

	public int fitness = Integer.MIN_VALUE;

	public boolean learning = true;

	public static GrowingAgent fromFile(String filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			GrowingAgent ga = (GrowingAgent)ois.readObject();
			ois.close();
			fis.close();
			return ga;
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("bla");
		}
	}

	@Override
	public MutationAgent mutate (double mutationChance) {
		GrowingAgent next = new GrowingAgent();

		for (Map.Entry<GrowingHorizon, Byte> entry : actionMap.entrySet()) {
			byte actions = entry.getValue();
			if (random.nextDouble() < mutationChance) actions ^= JUMP;
			if (random.nextDouble() < mutationChance) actions ^= RUN;
			if (random.nextDouble() < mutationChance) actions ^= SHOOT;

			next.actionMap.put(entry.getKey(), actions);
		}

		return next;
	}

	@Override
	public void dump(String filename) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			oos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public MarioInput actionSelectionAI() {
		GrowingHorizon validHorizon = null;
		int specificity = 1;
		GrowingHorizon horizon = GrowingHorizon.fromTerrain(specificity, t, e, mario);
		while (actionMap.containsKey(horizon)) {
			validHorizon = horizon;
			specificity++;
			horizon = GrowingHorizon.fromTerrain(specificity, t, e, mario);
			if (specificity > 5) break;
		}

		// INVARIANTS
		// specificity = farthest level on which we have no recorded action
		// horizon = horizon at specificity level
		// validHorizon = horizon at specificity-1 (we have an action for it)

		byte actions;

		if (learning && actionMap.size() >= ACTION_TABLE_SIZE) {
			System.out.println("exceeded action table size");
		}


		if (validHorizon == null) {
			// no entry for this horizon at spec 1
			actions = (byte)random.nextInt(8);
			if (!mario.onGround) actions |= JUMP;
			if (learning)
				actionMap.put(horizon, actions);
		} else if (learning && specificity < 6 && actionMap.size() < ACTION_TABLE_SIZE) {
			// LEARNING MODE, generate more specific action
			actions = actionMap.get(validHorizon);
			actionMap.put(horizon, actions);
		} else {
			// take last valid action and use it
			actions = actionMap.get(validHorizon);
		}

		action.set(MarioKey.RIGHT, (actions & RUN) == RUN);
		action.set(MarioKey.JUMP, ((actions & JUMP) == JUMP) && (mario.mayJump || !mario.onGround));
		action.set(MarioKey.SPEED, (actions & SHOOT) == SHOOT);

		return action;
	}

	@Override
	public int compareTo(GrowingAgent agent) {
		// REVERSE ORDERING
		return agent.fitness - fitness;
	}

	public int getFitness() { return fitness; }
	public void setFitness(int fitness) { this.fitness = fitness; }

	public void setLearning(boolean learning) { this.learning = learning; }
}
