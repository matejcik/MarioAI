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
		MutationAgent ma = new BetterBinAgent("agent-run-3-gen-199-best-0.txt");
		ma.setLearning(false);

		int wins = 0;
		int acts = 0;
		int over50 = 0;
		int over100 = 0;
		int over500 = 0;
		int sum = 0;
		for (int i = 0; i < 100; ++i) {
			EvaluationInfo info = simoff.run(ma);
			if (info.marioStatus == Mario.STATUS_WIN) ++wins;
		}
		for (int i = 0; i < 65536 * 8; ++i) {
			BetterBinAgent ba = (BetterBinAgent)ma;
			int ac = ba.actionUsed[i];
			if (ac > 0) acts++;
			if (ac > 50) over50++;
			if (ac > 100) over100++;
			if (ac > 500) over500++;
			sum += ac;
			if (ac > 0) {
				System.out.println(BetterBinHorizon.decode(i) + " === " + ac);
			}
		}
		System.out.println("" + wins + " wins out of 100");
		System.out.println("actions used " + acts);
		System.out.println("actions over 50 " + over50);
		System.out.println("actions over 100 " + over100);
		System.out.println("actions over 500 " + over500);
		System.out.println("total usages " + sum);


		while (true) {
			simulator.run(ma);
		}
	}
}