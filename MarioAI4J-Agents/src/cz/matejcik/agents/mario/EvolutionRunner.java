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

	private static final int AGENT_COUNT = 1000;
	private static final int BEST_COUNT = 10;
	private static final int MUTATIONS_PER_AGENT = AGENT_COUNT / BEST_COUNT;

	private static Class agentClass = SmallHorizonAgent.class;

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
			for (int j = 1; j < MUTATIONS_PER_AGENT; j++) {
				MutationAgent ga = a.mutate(0.01);
				ga.setLearning(learning);
				currentAgents.add(ga);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		String options = //FastOpts.VIS_ON_2X + FastOpts.LEVEL_02_JUMPING + FastOpts.L_ENEMY(Enemy.GOOMBA, Enemy.SPIKY) + FastOpts.L_TUBES_ON + FastOpts.L_RANDOMIZE;
			""
				//+ FastOpts.VIS_ON_2X
				//+ FastOpts.VIS_OFF
				+ FastOpts.LEVEL_02_JUMPING
				+ FastOpts.L_ENEMY(Enemy.GOOMBA, Enemy.SPIKY)
				+ FastOpts.L_TUBES_ON
			// + FastOpts.L_RANDOMIZE
			;

		MarioSimulator simulator = new MarioSimulator(options + FastOpts.VIS_OFF);

		int prevBestFitness = Integer.MIN_VALUE;
		newAgents();

		for (int i = 0; i < 10; ++i) {
			fitnessResults.clear();
			for (MutationAgent a : currentAgents) {
				EvaluationInfo info = simulator.run(a);

				int fitness = info.computeBasicFitness();
				a.setFitness(fitness);
				System.out.println("current fitness was " + fitness);

				fitnessResults.add(a);
			}
			System.out.println("best fitness: " + fitnessResults.peek().getFitness() + " (from: " + prevBestFitness + ")");
			prevBestFitness = fitnessResults.peek().getFitness();
			mutatePreviousGeneration(true);
		}

		for (int i = 0; i < 10; ++i) {
			fitnessResults.clear();
			for (MutationAgent a : currentAgents) {
				EvaluationInfo info = simulator.run(a);

				int fitness = info.computeBasicFitness();
				a.setFitness(fitness);
				System.out.println("current fitness was " + fitness);

				fitnessResults.add(a);
			}
			System.out.println("best fitness: " + fitnessResults.peek().getFitness() + " (from: " + prevBestFitness + ")");
			prevBestFitness = fitnessResults.peek().getFitness();
			mutatePreviousGeneration(false);
		}

		/*
		for (int i = 0; i < BEST_COUNT; ++i) {
			MutationAgent best = bestOfTheBunch.get(i);
			best.dump("agent" + i + ".txt");
		}

		for (int i = 0; i < 5; ++i) {
			fitnessResults.clear();
			for (MutationAgent a : currentAgents) {
				int fitness = 0;
				for (int j = 0; j < 5; ++j) {
					MarioSimulator ms = new MarioSimulator(options + FastOpts.L_RANDOMIZE + FastOpts.L_RANDOM_SEED(9 * i + 8 * j));
					EvaluationInfo info = ms.run(a);
					fitness += info.computeBasicFitness();
				}
				a.setFitness(fitness);
				System.out.println("current fitness was " + fitness);
				fitnessResults.add(a);
			}
			System.out.println("best fitness: " + fitnessResults.peek().getFitness() + " (from: " + prevBestFitness + ")");
			prevBestFitness = fitnessResults.peek().getFitness();
			mutatePreviousGeneration(false);
		}*/

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
}