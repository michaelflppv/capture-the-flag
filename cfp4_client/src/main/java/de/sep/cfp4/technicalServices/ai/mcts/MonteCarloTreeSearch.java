package de.sep.cfp4.technicalServices.ai.mcts;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.training.ParameterStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

/**
 * The MonteCarloTreeSearch class is responsible for performing the Monte Carlo Tree Search algorithm.
 * It uses a neural network model to predict the policy and value of a given state.
 * The MCTS algorithm is used to search the game tree and find the best move to play.
 *
 * @author mfilippo (Mikhail Filippov)
 * @version 10.05.2024
 */
public class MonteCarloTreeSearch {
    // Model used for the MCTS algorithm
    private final ArchitectureModel model;
    // Game on which the MCTS algorithm is applied
    private final MCTSClient client;
    // Hyperparameters for the MCTS algorithm
    private final Arguments args;

    /**
     * Constructor for the MonteCarloTreeSearch class.
     *
     * @param model  {@link ArchitectureModel} the model used for the MCTS algorithm
     * @param client {@link MCTSClient} the game on which the MCTS algorithm is applied
     * @param args   {@link Arguments} map for hyperparameters of MCTS
     */
    public MonteCarloTreeSearch(ArchitectureModel model, MCTSClient client, Arguments args) {
        this.model = model;
        this.client = client;
        this.args = args;
    }

    /**
     * The parallelSearch method performs the Monte Carlo Tree Search algorithm in parallel using multiple threads.
     * It creates a pool of 4 threads and submits the search task to each thread.
     * The results of the search are then retrieved and returned as a 2D float array.
     *
     * @param state {@link String[][]} the current state of the game
     * @return float[][] the probabilities of each action
     */
    public float[][] parallelSearch(String[][] state) {
        ExecutorService executor = Executors.newFixedThreadPool(4); // create a pool of 4 threads
        List<Future<float[]>> futures = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            Callable<float[]> callable = () -> search(state); // define the task
            Future<float[]> future = executor.submit(callable); // submit the task for execution
            futures.add(future);
        }

        float[][] results = new float[4][];
        for (int i = 0; i < 4; i++) {
            try {
                results[i] = futures.get(i).get(); // retrieve the result
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        executor.shutdown(); // shut down the executor service

        return results;
    }

    /**
     * The search method performs the Monte Carlo Tree Search algorithm to find the best move to play.
     *
     * @param state {@link String[][]} the current state of the game
     * @return float[] the probabilities of each action
     */
    public float[] search(String[][] state) {
        // Create the root node of the search tree
        Node root = new Node(client, args, state, null, 0, 0, 1);

        try (NDManager manager = NDManager.newBaseManager()) {
            // Predict the policy for the current state
            float[] policy = predictPolicy(manager, client.getEncodedState(state));
            policy = softmax(policy);
            policy = dirichletNoise(policy, args.getDirichletEpsilon(), args.getDirichletAlpha());

            // Mask the policy with the valid moves
            int[] validMoves = client.getValidMoves(state);
            policy = elementwiseMultiply(policy, validMoves);
            policy = normalize(policy);
            root.expand(policy);

            // Perform the MCTS algorithm for a given number of searches
            for (int search = 0; search < args.getNumMCTSSearches(); search++) {
                Node node = root;

                // Selection
                while (node.isExpanded()) {
                    node = node.select();
                }

                // Simulation
                String teamId = client.getTeamID();
                for (String opponent: this.client.getBoardModel().getAllTeamIDs()) {
                    if (!opponent.equals(teamId)) {
                        teamId = opponent;
                    }
                }
                float value = client.getValue(node.getMove());
                boolean isTerminal = client.getTerminated(state, node.getMove());
                value = client.getOpponentValue(value);

                // Expansion
                if (!isTerminal) {
                    policy = predictPolicy(manager, client.getEncodedState(node.getState()));
                    validMoves = client.getValidMoves(node.getState());
                    policy = elementwiseMultiply(policy, validMoves);
                    policy = normalize(policy);

                    // Predict the value of the state
                    float[] valueProbs = predictValue(manager, client.getEncodedState(node.getState()));
                    value = valueProbs[0];

                    node.expand(policy);
                }

                node.backPropagation(value);
            }

            float[] actionProbs = new float[client.getActionSize()];
            for (Node child : root.getChildren()) {
                actionProbs[child.getMove()] = child.getVisitCount();
            }
            actionProbs = normalize(actionProbs);

            return actionProbs;
        }
    }

    /**
     * The createPolicy method uses the neural network model to predict the policy of a given state.
     *
     * @param manager {@link NDManager} the NDManager used to create NDArrays
     * @param encodedState {@link float[][][]} the encoded state of the game
     * @return float[] the policy predicted by the model
     */
    public float[] predictPolicy(NDManager manager, float[][][] encodedState) {

        NDArray stateNDArray = model.createNDArray(encodedState);
        NDList stateNDList = new NDList(stateNDArray);

        // Perform a forward pass through the model
        NDList outputList = model.forward(new ParameterStore(manager, false), stateNDList, false);
        NDArray output = outputList.get(0);

        // Convert the output to a float array
        return output.toFloatArray();
    }


    /**
     * The predictValue method uses the neural network model to predict the value of a given state.
     *
     * @param manager {@link NDManager} the NDManager used to create NDArrays
     * @param encodedState {@link float[][][]} the encoded state of the game
     * @return float[] the value predicted by the model
     */
    public float[] predictValue(NDManager manager, float[][][] encodedState) {

        NDArray stateNDArray = model.createNDArray(encodedState);
        NDList stateNDList = new NDList(stateNDArray);

        // Perform a forward pass through the model
        NDList outputList = model.forward(new ParameterStore(manager, false), stateNDList, false);
        NDArray output = outputList.get(1);

        // Convert the output to a float array
        return output.toFloatArray();
    }

    /**
     * The softmax method applies the softmax function to a given array.
     * The softmax function is used to convert a vector of real numbers into a probability distribution.
     * Each output number in the array will be in the range (0, 1), and the sum of all numbers will be 1.
     *
     * @param x {@link float[]} the array to which the softmax function is applied
     * @return float[] the array after applying the softmax function
     */
    private float[] softmax(float[] x) {
        float max = Float.NEGATIVE_INFINITY;
        for (float v : x) {
            max = Math.max(max, v);
        }
        float sum = 0;
        for (int i = 0; i < x.length; i++) {
            x[i] = (float) Math.exp(x[i] - max);
            sum += x[i];
        }
        for (int i = 0; i < x.length; i++) {
            x[i] /= sum;
        }
        return x;
    }

    /**
     * The dirichletNoise method applies the Dirichlet noise to a given array.
     * Dirichlet noise is used in the MCTS algorithm to encourage exploration of the search space.
     *
     * @param x       {@link float[]} the array to which the Dirichlet noise is applied
     * @param epsilon {@link double} the epsilon value for the Dirichlet noise
     * @param alpha   {@link double} the alpha value for the Dirichlet noise
     * @return float[] the array after applying the Dirichlet noise
     */
    private float[] dirichletNoise(float[] x, float epsilon, float alpha) {
        float[] noise = new float[x.length];
        for (int i = 0; i < x.length; i++) {
            noise[i] = (float) ((1 - epsilon) * x[i] + epsilon * Math.random() * alpha);
        }
        return noise;
    }

    /**
     * The elementwiseMultiply method performs element-wise multiplication of two arrays.
     * Element-wise multiplication means that the same index in each array are multiplied together.
     *
     * @param a {@link float[]} the first array
     * @param b {@link float[]} the second array
     * @return float[] the result of the element-wise multiplication
     */
    private float[] elementwiseMultiply(float[] a, int[] b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * b[i];
        }
        return result;
    }

    /**
     * The normalize method normalizes a given array.
     * Normalization means adjusting the values in the array so that the sum of all values is 1.
     *
     * @param a {@link float[]} the array to be normalized
     * @return float[] the normalized array
     */
    public float[] normalize(float[] a) {
        float sum = 0;
        for (float v : a) {
            sum += v;
        }
        for (int i = 0; i < a.length; i++) {
            a[i] /= sum;
        }
        return a;
    }
}
