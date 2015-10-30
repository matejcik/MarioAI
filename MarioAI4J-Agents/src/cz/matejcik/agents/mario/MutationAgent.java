package cz.matejcik.agents.mario;

import ch.idsia.agents.IAgent;

/**
 * Created by matejcik on 30.10.15.
 */
public interface MutationAgent extends IAgent {

	void dump (String filename);
	MutationAgent mutate(double mutationChance);

	int getFitness();
	void setFitness(int fitness);

	void setLearning(boolean learning);
}
