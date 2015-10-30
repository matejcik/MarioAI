package cz.matejcik.agents.mario;

import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.generalization.Enemy;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.tools.EvaluationInfo;

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
		MarioSimulator simoff = new MarioSimulator(options + FastOpts.VIS_OFF);

		//MutationAgent ma = new BetterBinAgent();
		//Agent agent = new Agent("agent.txt");
		//Agent agent = Agent.randomAgent();
		//MutationAgent ma = new SemAgent();
		//MutationAgent ma = SemAgent.fromFilename("agent-cont0.txt");
		//MutationAgent ma = GrowingAgent.fromFile("agent-cont0.txt");
		MutationAgent ma = new BetterBinAgent("agent-run-2-gen-198-best-0.txt");
		ma.setLearning(false);

		int wins = 0;
		for (int i = 0; i < 100; ++i) {
			EvaluationInfo info = simoff.run(ma);
			if (info.marioStatus == Mario.STATUS_WIN) ++wins;
		}
		System.out.println("" + wins + " wins out of 100");


		while (true) {
			simulator.run(ma);
		}
	}
}