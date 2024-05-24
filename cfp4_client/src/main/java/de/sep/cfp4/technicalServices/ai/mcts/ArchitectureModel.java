package de.sep.cfp4.technicalServices.ai.mcts;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.*;
import ai.djl.nn.convolutional.Conv2d;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.BatchNorm;
import ai.djl.training.ParameterStore;
import ai.djl.training.initializer.Initializer;
import ai.djl.util.PairList;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The ArchitectureModel class is responsible for creating and managing the neural network model used in the Monte Carlo Tree Search algorithm.
 * It uses a convolutional neural network (CNN) to predict the policy and value of a given state.
 *
 * @author mfilippo (Mikhail Filippov)
 * @version 04.05.2024
 */
public class ArchitectureModel extends AbstractBlock {
    // Version of the ArchitectureModel class
    private static final byte VERSION = 2;

    // The NDManager used to create NDArrays
    private final NDManager manager;

    // Start block of the neural network model
    private final SequentialBlock startBlock;
    // Backbone of the neural network model
    private final List<ResidualBlock> backBone;
    // Policy head of the neural network model
    private final SequentialBlock policyHead;
    // Value head of the neural network model
    private final SequentialBlock valueHead;
    // Position head of the neural network model
    private final SequentialBlock positionHead;

    /**
     * Constructor for the ArchitectureModel class.
     *
     * @param client      {@link MCTSClient} the client which interacts with the game
     * @param numResBlocks int the number of residual blocks in the neural network model
     * @param numHidden   int the number of hidden units in the neural network model
     */
    public ArchitectureModel(MCTSClient client, int numResBlocks, int numHidden) {
        // Call the constructor of the parent class
        super(VERSION);
        this.manager = client.getManager();

        // Get the number of rows, columns, and actions in the game
        int numRows = client.getRowCount();
        int numColumns = client.getColumnCount();
        int numActions = client.getActionSize();

        // Create the start block of the neural network model
        this.startBlock = new SequentialBlock();
        this.startBlock
                .add(Conv2d.builder()
                        .setKernelShape(new Shape(3, 3))
                        .optPadding(new Shape(1, 1))
                        .setFilters(numHidden)
                        .build())
                .add(BatchNorm.builder().build())
                .add(Activation::relu);
        this.addChildBlock("startBlock", startBlock);

        // Create the backbone of the neural network model
        this.backBone = new ArrayList<>();
        for (int i = 0; i < numResBlocks; i++) {
            ResidualBlock block = new ResidualBlock(numHidden);
            this.backBone.add(block);
            this.addChildBlock("resBlock" + i, block);
        }

        // Create the policy head of the neural network model
        policyHead = new SequentialBlock();
        policyHead.add(Conv2d.builder()
                        .setKernelShape(new Shape(3, 3))
                        .optPadding(new Shape(1, 1))
                        .setFilters(32)
                        .build())
                .add(BatchNorm.builder().build())
                .add(Activation::relu)
                .add(Blocks.batchFlattenBlock())
                .add(Linear.builder().setUnits(32L * numRows * numColumns).build())
                .add(Linear.builder().setUnits(numActions).build());
        this.addChildBlock("policyHead", policyHead);

        // Create the value head of the neural network model
        valueHead = new SequentialBlock();
        valueHead.add(Conv2d.builder()
                        .setKernelShape(new Shape(3, 3))
                        .optPadding(new Shape(1, 1))
                        .setFilters(3)
                        .build())
                .add(BatchNorm.builder().build())
                .add(Activation::relu)
                .add(Blocks.batchFlattenBlock())
                .add(Linear.builder().setUnits(3L * numRows * numColumns).build())
                .add(Linear.builder().setUnits(1).build())
                .add(Activation::tanh);
        this.addChildBlock("valueHead", valueHead);

        // Create the position head of the neural network model
        positionHead = new SequentialBlock();
        positionHead.add(Conv2d.builder()
                        .setKernelShape(new Shape(3, 3))
                        .optPadding(new Shape(1, 1))
                        .setFilters(32)
                        .build())
                .add(BatchNorm.builder().build())
                .add(Activation::relu)
                .add(Blocks.batchFlattenBlock())
                .add(Linear.builder().setUnits(32L * numRows * numColumns).build())
                .add(Linear.builder().setUnits(numActions).build());  // Predict position probabilities
        this.addChildBlock("positionHead", positionHead);
    }

    /**
     * The forward method is used to pass the input through the neural network model and get the output.
     *
     * @param parameterStore {@link ParameterStore} the parameter store used to store the parameters of the neural network model
     * @param inputs         {@link NDList} the input to the neural network model
     * @param training       {@link boolean} whether the neural network model is in training mode
     * @param pairList       the list of key-value pairs
     * @return {@link NDList} the output of the neural network model
     */
    @Override
    protected NDList forwardInternal(ParameterStore parameterStore, NDList inputs, boolean training, PairList<String, Object> pairList) {
        // Get the input NDArray
        NDArray x = inputs.singletonOrThrow();

        // Pass the input through the start block
        x = startBlock.forward(parameterStore, new NDList(x), training).singletonOrThrow();
        for (ResidualBlock resBlock : backBone) {
            x = resBlock.forward(parameterStore, new NDList(x), training).singletonOrThrow();
        }
        // Pass the input through the policy head and the value head
        NDArray policy = policyHead.forward(parameterStore, new NDList(x), training).singletonOrThrow();
        NDArray value = valueHead.forward(parameterStore, new NDList(x), training).singletonOrThrow();
        NDArray position = positionHead.forward(parameterStore, new NDList(x), training).singletonOrThrow();

        // Return the output of the neural network model
        return new NDList(policy, value, position);
    }

    /**
     * The getOutputShapes method is used to get the output shapes of the neural network model.
     *
     * @param shapes {@link Shape[]} the input shapes to the neural network model
     * @return {@link Shape[]} the output shapes of the neural network model
     */
    @Override
    public Shape[] getOutputShapes(Shape[] shapes) {
        // Get the output shapes of the start block, the backbone, the policy head, and the value head
        Shape[] current = shapes;
        // Pass the input shapes through the start block, the backbone, the policy head, and the value head
        current = this.startBlock.getOutputShapes(current);
        for (ResidualBlock resBlock : this.backBone) {
            current = resBlock.getOutputShapes(current);
        }
        current = this.policyHead.getOutputShapes(current);
        current = this.valueHead.getOutputShapes(current);
        current = this.positionHead.getOutputShapes(current);
        // Return the output shapes of the neural network model
        return current;
    }

    /**
     * The initializeChildBlocks method is used to initialize the child blocks of the neural network model.
     *
     * @param manager     {@link NDManager} the NDManager used to create NDArrays
     * @param dataType    {@link DataType} the data type of the neural network model
     * @param inputShapes {@link Shape[]} the input shapes to the neural network model
     */
    @Override
    protected void initializeChildBlocks(NDManager manager, DataType dataType, Shape... inputShapes) {
        // Initialize the start block
        this.startBlock.initialize(manager, dataType, inputShapes);
        // Initialize the backbone
        for (ResidualBlock resBlock : this.backBone) {
            resBlock.initialize(manager, dataType, inputShapes);
        }
        // Initialize the policy head and the value head
        this.policyHead.initialize(manager, dataType, inputShapes);
        this.valueHead.initialize(manager, dataType, inputShapes);
        this.positionHead.initialize(manager, dataType, inputShapes);
    }

    /**
     * The toString method is used to get the string representation of the ArchitectureModel class.
     *
     * @return {@link String} the string representation of the ArchitectureModel class
     */
    @Override
    public String toString() {
        return "ArchitectureModel()";
    }

    /**
     * The createNDArray method is used to create an NDArray from a 3D float array.
     *
     * @param input {@link float[][][]} the 3D float array
     * @return {@link NDArray} the NDArray created from the 3D float array
     */
    public NDArray createNDArray(float[][][] input) {
        // Get the depth, height, and width of the 3D array
        int depth = input.length;
        int height = input[0].length;
        int width = input[0][0].length;

        // Create a buffer and fill it with the data from the 3D array
        FloatBuffer buffer = FloatBuffer.allocate(depth * height * width);
        for (float[][] matrix : input) {
            for (float[] row : matrix) {
                for (float value : row) {
                    buffer.put(value);
                }
            }
        }
        // Flip the buffer to prepare it for reading
        buffer.flip();

        // Create an NDArray using the buffer and the shape of the 3D array
        return manager.create(buffer, new Shape(1, depth, height, width), DataType.FLOAT32);
    }

    /**
     * Sets the model to training mode.
     */
    public void toTrain() {
        this.startBlock.setInitializer(Initializer.ONES, Parameter.Type.WEIGHT);
        for (ResidualBlock block : this.backBone) {
            block.setInitializer(Initializer.ONES, Parameter.Type.WEIGHT);
        }
        this.policyHead.setInitializer(Initializer.ONES, Parameter.Type.WEIGHT);
        this.valueHead.setInitializer(Initializer.ONES, Parameter.Type.WEIGHT);
        this.positionHead.setInitializer(Initializer.ONES, Parameter.Type.WEIGHT);
    }

    /**
     * Sets the model to prediction mode.
     */
    public void toPredictor() {
        this.startBlock.setInitializer(Initializer.ZEROS, Parameter.Type.WEIGHT);
        for (ResidualBlock block : this.backBone) {
            block.setInitializer(Initializer.ZEROS, Parameter.Type.WEIGHT);
        }
        this.policyHead.setInitializer(Initializer.ZEROS, Parameter.Type.WEIGHT);
        this.valueHead.setInitializer(Initializer.ZEROS, Parameter.Type.WEIGHT);
        this.positionHead.setInitializer(Initializer.ZEROS, Parameter.Type.WEIGHT);
    }

    /**
     * The private ResidualBlock class is used to create a residual block in the neural network model.
     * It consists of two convolutional layers and two batch normalization layers.
     * The input is added to the output of the second convolutional layer, and the result is passed through a ReLU activation function.
     *
     * @author mfilippo (Mikhail Filippov)
     * @version 02.05.2024
     */
    private class ResidualBlock extends AbstractBlock {
        // Version of the ResidualBlock class
        private static final byte VERSION = 2;

        // The first convolutional layer
        private final Conv2d convLayer1;
        // The first batch normalization layer
        private final BatchNorm batchNorm1;
        // The second convolutional layer
        private final Conv2d convLayer2;
        // The second batch normalization layer
        private final BatchNorm batchNorm2;

        /**
         * Constructor for the ResidualBlock class.
         *
         * @param numHidden int the number of hidden units in the residual block
         */
        public ResidualBlock(int numHidden) {
            // Call the constructor of the parent class
            super(VERSION);
            // Create the first convolutional layer
            this.convLayer1 = Conv2d.builder()
                    .setKernelShape(new Shape(3, 3))
                    .optPadding(new Shape(1, 1))
                    .setFilters(numHidden)
                    .build();
            // Create the first batch normalization layer
            this.batchNorm1 = BatchNorm.builder().build();
            // Create the second convolutional layer
            this.convLayer2 = Conv2d.builder()
                    .setKernelShape(new Shape(3, 3))
                    .optPadding(new Shape(1, 1))
                    .setFilters(numHidden)
                    .build();
            // Create the second batch normalization layer
            this.batchNorm2 = BatchNorm.builder().build();

            // Add the child blocks to the current block
            this.addChildBlock("conv1", convLayer1);
            this.addChildBlock("bn1", batchNorm1);
            this.addChildBlock("conv2", convLayer2);
            this.addChildBlock("bn2", batchNorm2);
        }

        /**
         * The forward method is used to pass the input through the residual block and get the output.
         *
         * @param parameterStore {@link ParameterStore} the parameter store used to store the parameters of the residual block
         * @param inputs         {@link NDList} the input to the residual block
         * @param training       {@link boolean} whether the residual block is in training mode
         * @param pairList       the list of key-value pairs
         * @return {@link NDList} the output of the residual block
         */
        @Override
        protected NDList forwardInternal(ParameterStore parameterStore, NDList inputs, boolean training, PairList<String, Object> pairList) {
            NDArray x = inputs.singletonOrThrow();
            NDArray residual = x;

            x = this.batchNorm1.forward(parameterStore, new NDList(this.convLayer1.forward(parameterStore, new NDList(x), training).singletonOrThrow()), training).singletonOrThrow();
            x = x.getNDArrayInternal().relu();
            x = this.batchNorm2.forward(parameterStore, new NDList(this.convLayer2.forward(parameterStore, new NDList(x), training).singletonOrThrow()), training).singletonOrThrow();
            x = x.add(residual);
            x = x.getNDArrayInternal().relu();

            return new NDList(x);
        }

        /**
         * The getOutputShapes method is used to get the output shapes of the residual block.
         *
         * @param inputShapes {@link Shape[]} the input shapes to the residual block
         * @return {@link Shape[]} the output shapes of the residual block
         */
        @Override
        public Shape[] getOutputShapes(Shape[] inputShapes) {
            Shape[] current = inputShapes;
            current = this.convLayer1.getOutputShapes(current);
            current = this.batchNorm1.getOutputShapes(current);
            current = this.convLayer2.getOutputShapes(current);
            current = this.batchNorm2.getOutputShapes(current);
            return current;
        }

        /**
         * The initializeChildBlocks method is used to initialize the child blocks of the residual block.
         *
         * @param manager     {@link NDManager} the NDManager used to create NDArrays
         * @param dataType    {@link DataType} the data type of the residual block
         * @param inputShapes {@link Shape[]} the input shapes to the residual block
         */
        @Override
        protected void initializeChildBlocks(NDManager manager, DataType dataType, Shape... inputShapes) {
            this.convLayer1.initialize(manager, dataType, inputShapes);
            this.batchNorm1.initialize(manager, dataType, inputShapes);
            this.convLayer2.initialize(manager, dataType, inputShapes);
            this.batchNorm2.initialize(manager, dataType, inputShapes);
        }
    }
}