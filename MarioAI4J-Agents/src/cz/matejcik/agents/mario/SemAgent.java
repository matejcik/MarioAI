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
public class SemAgent extends MarioHijackAIBase implements Comparable<SemAgent>, MutationAgent, Serializable {

	private static final long serialVersionUID = 1L;

	private static final int JUMP = 0x01;
	private static final int SHOOT = 0x02;
	private static final int RUN = 0x04;

	private static final Random random = new Random();

	public static final int ACTION_TABLE_SIZE = 8192;

	public int fitness = Integer.MIN_VALUE;

	private Map<SemHorizon, Byte> actionMap = new HashMap<>();
	private Map<SemHorizon, Integer> usageCounter = new HashMap<>();

	private void randomize() {
		for (int i = 0; i < ACTION_TABLE_SIZE; ++i) {
			SemHorizon h = SemHorizon.randomHorizon();
			byte actions = (byte)random.nextInt(8);
			if (!h.onGround) actions |= JUMP;
			actionMap.put(h, actions);
		}
	}

	public SemAgent() {
		randomize();
	}

	public SemAgent(boolean doRandomize) {
		if (doRandomize) randomize();
	}

	public static SemAgent fromFilename(String filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			SemAgent ga = (SemAgent)ois.readObject();
			ois.close();
			fis.close();
			return ga;
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("bla");
		}
	}


	@Override
	public MutationAgent mutate (double mutationChance) {
		SemAgent next = new SemAgent(false);

		for (Map.Entry<SemHorizon, Byte> entry : actionMap.entrySet()) {
			byte actions = entry.getValue();
			if (random.nextDouble() < mutationChance) actions ^= JUMP;
			if (random.nextDouble() < mutationChance) actions ^= RUN;
			if (random.nextDouble() < mutationChance) actions ^= SHOOT;

			SemHorizon horizon = entry.getKey().mutate(mutationChance);
			next.actionMap.put(horizon, actions);
		}

		return next;
	}

	@Override
	public MutationAgent offspring(MutationAgent other, double mutationChance) {
		return null;
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

		SemHorizon horizon = SemHorizon.fromTerrain(t, e, mario);
		SemHorizon best = null;
		double distance = Double.MAX_VALUE;
		for (SemHorizon k : actionMap.keySet()) {
			double d = k.distance(horizon);
			if (distance > d) {
				distance = d;
				best = k;
			}
		}

		if (usageCounter.containsKey(best)) {
			usageCounter.put(best, usageCounter.get(best) + 1);
		} else {
			usageCounter.put(best, 1);
		}

		byte actions = actionMap.get(best);
		action.set(MarioKey.RIGHT, (actions & RUN) == RUN);
		action.set(MarioKey.JUMP, ((actions & JUMP) == JUMP) && (mario.mayJump || !mario.onGround));
		action.set(MarioKey.SPEED, (actions & SHOOT) == SHOOT);

		return action;
	}

	@Override
	public int compareTo(SemAgent agent) {
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
