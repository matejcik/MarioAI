package cz.matejcik.agents.mario;

import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.generalization.Enemy;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.tools.EvaluationInfo;

import java.io.File;

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
		MutationAgent ma = null;
		for (int g = 500; g >= 0; --g) {
			File file = new File(String.format("agent-run-2-gen-%d-best-0.txt", g));
			if (file.exists()) {
				ma = new BetterBinAgent(file.getName());
				System.out.println("generation " + g);
				break;
			}
		}
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