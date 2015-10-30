package cz.matejcik.agents.mario;

import ch.idsia.agents.AgentOptions;
import ch.idsia.agents.IAgent;
import ch.idsia.agents.controllers.MarioHijackAIBase;
import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.generalization.Enemy;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.tasks.SystemOfValues;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.ReplayerOptions;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Agent that sprints forward, jumps and shoots.
 * 
 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
 */
public class EvolutionRunner {

	private static ArrayList<MutationAgent> currentAgents = new ArrayList<>();
	private static PriorityQueue<MutationAgent> fitnessResults = new PriorityQueue<>();
	private static ArrayList<MutationAgent> bestOfTheBunch = new ArrayList<>();

	private static final int AGENT_COUNT = 100;
	private static final int BEST_COUNT = 10;
	private static final int MUTATIONS_PER_AGENT = AGENT_COUNT / BEST_COUNT;

	private static Class agentClass = BetterBinAgent.class;

	private static void newAgents() throws Exception {
		currentAgents.clear();
		fitnessResults.clear();
		for (int i = 0; i < AGENT_COUNT; ++i) {
			currentAgents.add((MutationAgent)agentClass.newInstance());
		}
	}

	private static void mutatePreviousGeneration(boolean learning) {
		currentAgents.clear();
		bestOfTheBunch.clear();
		for (int i = 0; i < BEST_COUNT; ++i) {
			MutationAgent a = fitnessResults.poll();
			a.setLearning(false);
			currentAgents.add(a);
			bestOfTheBunch.add(a);
		}

		for (int i = 0; i < BEST_COUNT; ++i) {
			for (int j = 0; j < BEST_COUNT; ++j) {
				if (i == j) continue;
				MutationAgent a = bestOfTheBunch.get(i).offspring(bestOfTheBunch.get(j), 0.05);
				a.setLearning(learning);
				currentAgents.add(a);
			}
		}
	}

	private static void loadPreviousBestSet(int run, int gen)
	{
		for (int i = 0; i < BEST_COUNT; ++i) {
			String filename = String.format("agent-run-%d-gen-%d-best-%d.txt", run, gen, i);
			MutationAgent ma = new BetterBinAgent(filename);
			fitnessResults.add(ma);
		}
	}

	private static int calculateFitness(EvaluationInfo info) {
		return (info.distancePassedPhys - info.timeSpent)
			+ ((info.marioStatus == Mario.STATUS_WIN) ? 1024 : 0)
			+ info.marioMode.getCode() * 512;
	}

	public static void main(String[] args) throws Exception {
		String options = //FastOpts.VIS_ON_2X + FastOpts.LEVEL_02_JUMPING + FastOpts.L_ENEMY(Enemy.GOOMBA, Enemy.SPIKY) + FastOpts.L_TUBES_ON + FastOpts.L_RANDOMIZE;
			""
				//+ FastOpts.VIS_ON_2X
				+ FastOpts.VIS_OFF
				+ FastOpts.LEVEL_02_JUMPING
				+ FastOpts.L_ENEMY(Enemy.GOOMBA, Enemy.SPIKY)
				+ FastOpts.L_TUBES_ON
			// + FastOpts.L_RANDOMIZE
			;

		newAgents();
		runGenerations(options, 1, 200, 5, true);
//		runGenerations(options, 1, 5, 3, false);

/*		loadPreviousBestSet(2, 199);
		mutatePreviousGeneration(false);
		runGenerations(options, 3, 200, 5, false); */

		MutationAgent superbest = bestOfTheBunch.get(0);
		int c = 0;
		for (MutationAgent best : bestOfTheBunch) {
			best.dump("agent-cont" + c + ".txt");
			++c;
		}

		MarioSimulator simulator1 = new MarioSimulator(options + FastOpts.VIS_ON_2X);
		while(true) {
			System.out.println("fitness of this guy: " + superbest.getFitness());
			superbest.setLearning(false);
			simulator1.run(superbest);
		}
	}

	private static void runGenerations(String options, int run, int generations, int levels, boolean learning) {
		int prevBestFitness = Integer.MIN_VALUE;
		for (int i = 0; i < generations; ++i) {
			int bestFitness = Integer.MIN_VALUE;
			fitnessResults.clear();
			EvaluationInfo info = null;

			for (MutationAgent a : currentAgents) a.setFitness(0);

			for (int j = 0; j < levels; ++j) {
				MarioSimulator ms = new MarioSimulator(options + FastOpts.L_RANDOM_SEED(9 * i + 8 * j));
				int ac = 0;

				for (MutationAgent a : currentAgents) {
					info = ms.run(a);
					a.setFitness(a.getFitness() + calculateFitness(info));
					System.out.println(
						String.format("gen: %d (%d of %d); lvl: %d of %d; status: %d; fitness %d (best %d)",
							i + 1, ac, currentAgents.size(),
							j + 1, levels,
							info.marioStatus, a.getFitness() / (j + 1), bestFitness)
					);

					if (a.getFitness() / (j+1) > bestFitness) bestFitness = a.getFitness() / (j+1);
					++ac;
				}
			}

			for (MutationAgent a : currentAgents) {
				fitnessResults.add(a);
			}
			prevBestFitness = bestFitness;
			mutatePreviousGeneration(false);

			for (int bb = 0; bb < BEST_COUNT; ++bb) {
				MutationAgent best = bestOfTheBunch.get(bb);
				best.dump("agent-run-" + run + "-gen-" + i + "-best-" + bb + ".txt");
			}
		}
	}
}