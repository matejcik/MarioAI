package cz.matejcik.agents.mario;

import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.generalization.Enemy;
import ch.idsia.benchmark.mario.options.FastOpts;

/**
 * Agent that sprints forward, jumps and shoots.
 * 
 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
 */
public class SpecificRunner {

	public static void main(String[] args) {
		String options = //FastOpts.VIS_ON_2X + FastOpts.LEVEL_02_JUMPING + FastOpts.L_ENEMY(Enemy.GOOMBA, Enemy.SPIKY) + FastOpts.L_TUBES_ON + FastOpts.L_RANDOMIZE;
			""
				+ FastOpts.VIS_ON_2X
				//+ FastOpts.VIS_OFF
				+ FastOpts.LEVEL_02_JUMPING
				+ FastOpts.L_ENEMY(Enemy.GOOMBA, Enemy.SPIKY)
				+ FastOpts.L_TUBES_ON
				+ FastOpts.L_RANDOMIZE
			;

		MarioSimulator simulator = new MarioSimulator(options);
		//Agent agent = new Agent("agent.txt");
		//Agent agent = Agent.randomAgent();
		GrowingAgent ga = GrowingAgent.fromFile("agent-cont0.txt");
		ga.learning = false;
		while (true) {
			simulator.run(ga);
		}
	}
}