package v2;

import static v2.Util.checkPositive;

import java.util.ArrayList;
import java.util.List;

/**
 * A plate layer that performs max pooling on a specified window. There is no overlap between different placements of
 * the window.
 * 
 * TODO: This implementation does not let you vary stride. In the future, we may want to add that feature.
 */
public class PoolingLayer implements PlateLayer {
	private final int windowHeight;
	private final int windowWidth;
	// quite similar to a plate, except its booleans so more memory efficient
	private boolean[][][] maximumOfWindow;

	private PoolingLayer(int windowHeight, int windowWidth) {
		this.windowHeight = windowHeight;
		this.windowWidth = windowWidth;
	}

	@Override
	public int calculateNumOutputs(int numInputs) {
		return numInputs;
	}

	@Override
	public int calculateOutputHeight(int inputHeight) {
		int outputHeight = inputHeight / windowHeight;
		if (inputHeight % windowHeight > 0) {
			outputHeight++;
		}
		return outputHeight;
	}

	@Override
	public int calculateOutputWidth(int inputWidth) {
		int outputWidth = inputWidth / windowWidth;
		if (inputWidth % windowWidth > 0) {
			outputWidth++;
		}
		return outputWidth;
	}

	@Override
	public List<Plate> computeOutput(List<Plate> input) {
		// TODO: Reuse memory.
		List<Plate> output = new ArrayList<Plate>(input.size());
		for (Plate inputPlate : input) {
			output.add(inputPlate.maxPool(windowHeight, windowWidth));
		}
		return output;
	}

	@Override
	public List<Plate> propagateError(List<Plate> gradients, double learningRate) {
		// TODO: Reuse memory.
		List<Plate> output = new ArrayList<Plate>(gradients.size());
		for (int i = 0; i < gradients.size(); i++) {
			Plate errorPlate = gradients.get(i);
			double[][] upscaledValues = new double[maximumOfWindow[0].length][maximumOfWindow[0][0].length];
			for (int j = 0; j < maximumOfWindow[i].length; j++) {
				for (int k = 0; k < maximumOfWindow[i][j].length; k++) {
					// gradient is either copied from upper layer or zero - Ran Manor's answer at
					// https://www.quora.com/In-neural-networks-how-does-backpropagation-get-carried-through-maxpool-layers
					upscaledValues[j][k] = maximumOfWindow[i][j][k]
							? errorPlate.valueAt(j / windowHeight, k / windowWidth)
							: 0;
				}
			}
			output.add(new Plate(upscaledValues));
		}
		return output;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n------\tPooling Layer\t------\n\n");
		builder.append(String.format("Window height: %d\n", windowHeight));
		builder.append(String.format("Window width: %d\n", windowWidth));
		builder.append("\n\t------------\t\n");
		return builder.toString();
	}

	/** Returns a new builder. */
	public static Builder newBuilder() {
		return new Builder();
	}

	/**
	 * Simple builder pattern for creating PoolingLayers. (Not very helpful now, but later we may want to add other
	 * parameters.)
	 */
	public static class Builder {
		private int windowHeight = 0;
		private int windowWidth = 0;

		private Builder() {
		}

		public Builder setWindowSize(int height, int width) {
			checkPositive(height, "Window height", false);
			checkPositive(width, "Window width", false);
			this.windowHeight = height;
			this.windowWidth = width;
			return this;
		}

		public PoolingLayer build() {
			checkPositive(windowHeight, "Window height", true);
			checkPositive(windowWidth, "Window width", true);
			return new PoolingLayer(windowHeight, windowWidth);
		}
	}
}
