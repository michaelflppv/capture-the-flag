package de.sep.cfp4.technicalServices.ai.mcts;

/**
 * The MemoryElement class represents a memory element that is stored in the replay buffer.
 * It contains the memory state, the action probabilities and the outcome of the memory element.
 * The memory state is the state of the game at a certain point in time.
 * The action probabilities are the probabilities of each action that can be taken in the given state.
 * The outcome is the result of the game after taking the action.
 * The memory element is used to train the neural network model.
 *
 * @author mfilippo (Mikhail Filippov)
 * @version 05.05.2024
 */
public record MemoryElement(float[][][] memoryState, float[] actionProbs, String player, float outcome) {
    /**
     * Constructor for the MemoryElement class.
     *
     * @param memoryState {@link float[][][]} the memory state of the game
     * @param actionProbs {@link float[]} the action probabilities
     * @param player      {@link String} the outcome of the memory element
     */
    public MemoryElement {
    }

    /**
     * Getter for the memory state.
     *
     * @return memory state
     */
    @Override
    public float[][][] memoryState() {
        return this.memoryState;
    }

    /**
     * Getter for the action probabilities.
     *
     * @return action probabilities
     */
    @Override
    public float[] actionProbs() {
        return this.actionProbs;
    }

    /**
     * Getter for the outcome.
     *
     * @return player
     */
    @Override
    public String player() {
        return this.player;
    }

    /**
     * Getter for the outcome.
     *
     * @return outcome
     */
    @Override
    public float outcome() {
        return this.outcome;
    }
}
