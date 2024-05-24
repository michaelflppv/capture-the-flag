package de.sep.cfp4.technicalServices.ai.mcts;

/**
 * The Arguments class represents the hyperparameters for the MCTS algorithm.
 *
 * @author mfilippo (Mikhail Filippov)
 * @version 02.04.2024
 */
public class Arguments {
    private final int numIterations; // number of iterations for the MCTS algorithm
    private final int numSelfPlayIterations; // number of self-play iterations
    private final int numMCTSSearches; // number of MCTS searches
    private final int numEpochs; // number of epochs
    private final int batchSize; // batch size
    private final double temperature; // temperature for the softmax function
    private final double c; // exploration constant
    private final float dirichletAlpha; // alpha for the dirichlet noise
    private final float dirichletEpsilon; // epsilon for the dirichlet noise

    /**
     * Constructor for the Arguments class.
     */
    public Arguments() {
        // default values for the hyperparameters
        this.numIterations = 48;
        this.numSelfPlayIterations = 500;
        this.numMCTSSearches = 100;
        this.numEpochs = 4;
        this.batchSize = 64;
        this.temperature = 1.25;
        this.c = 2;
        this.dirichletAlpha = 0.3F;
        this.dirichletEpsilon = 0.125F;

    }

    /**
     * Getter for the number of iterations.
     *
     * @return number of iterations
     */
    public int getNumIterations() {
        return numIterations;
    }

    /**
     * Getter for the number of self-play iterations.
     *
     * @return number of self-play iterations
     */
    public int getNumSelfPlayIterations() {
        return numSelfPlayIterations;
    }

    /**
     * Getter for the number of MCTS searches.
     *
     * @return number of MCTS searches
     */
    public int getNumMCTSSearches() {
        return numMCTSSearches;
    }

    /**
     * Getter for the number of epochs.
     *
     * @return number of epochs
     */
    public int getNumEpochs() {
        return numEpochs;
    }

    /**
     * Getter for the batch size.
     *
     * @return batch size
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Getter for the temperature.
     *
     * @return temperature
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Getter for the exploration constant.
     *
     * @return exploration constant
     */
    public double getC() {
        return c;
    }

    /**
     * Getter for the alpha of the dirichlet noise.
     *
     * @return alpha of the dirichlet noise
     */
    public float getDirichletAlpha() {
        return dirichletAlpha;
    }

    /**
     * Getter for the epsilon of the dirichlet noise.
     *
     * @return epsilon of the dirichlet noise
     */
    public float getDirichletEpsilon() {
        return dirichletEpsilon;
    }
}
