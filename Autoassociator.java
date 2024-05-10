import java.util.Arrays;

public class Autoassociator 
{

	private int[][] weights;

	private final int trainingCapacity;
	
	public Autoassociator(CourseArray courses) 
	{
		// TO DO
		// creates a new Hopfield network with the same number of neurons 
		// as the number of courses in the input CourseArray

		weights = new int[courses.length()][courses.length()];
		trainingCapacity = (int) (0.139 * courses.length());
		initializeWeights();
	}
	
	public int getTrainingCapacity() 
	{
		return trainingCapacity;
	}
	
	public void training(int pattern[]) 
	{
		// TO DO

		for (int i = 0; i < weights.length; i++)
		{
			for (int j = 0; j < weights.length; j++)
			{
				if (isDiagonalElement(i, j))
				{
					continue;
				}
				weights[i][j] += (pattern[i] * pattern[j]);
			}
		}
	}

	public void fullUpdate(int neurons[]) 
	{
		// TO DO
		// updates the input until the final state achieved

		int[] previousNeurons = neurons.clone();
		int[] currentNeurons = null;
		while (true) 
		{
			unitUpdate(neurons);
			currentNeurons = neurons.clone();
			if (hasNetworkReachedItsMinimum(previousNeurons, currentNeurons))
			{
				return;
			}
			previousNeurons = currentNeurons.clone();
		}
	}

	public void chainUpdate(int neurons[], int steps) 
	{
		// TO DO
		// implements the specified number of update steps

		for (int i = 0; i < steps; i++)
		{
			unitUpdate(neurons);
		}
	}
	
	public int unitUpdate(int neurons[]) 
	{
		// TO DO
		// implements a single update step and
		// returns the index of the randomly selected and updated neuron

		int indexOfNeuronToBeUpdated = (int) (Math.floor(Math.random() * neurons.length));
		unitUpdate(neurons, indexOfNeuronToBeUpdated);
		return indexOfNeuronToBeUpdated;
	}
	
	public void unitUpdate(int neurons[], int index) 
	{
		// TO DO
		// implements the update step of a single neuron specified by index

		neurons[index] = calculateNextStateOfNeuron(neurons, index);
	}

	private void initializeWeights()
	{
		for (int i = 0; i < weights.length; i++)
		{
			for (int j = 0; j < weights.length; j++)
			{
				weights[i][j] = 0;
			}
		}
	}

	private int calculateNextStateOfNeuron(int[] neurons, int i)
	{
		int result = 0;
		for (int j = 0; j < neurons.length; j++)
		{
			result += (weights[i][j] * neurons[j]);
		}
		return thresholdFunction(result);
	}

	private int thresholdFunction(int s)
	{
		return s >= 0 ? 1 : -1;
	}

	private boolean hasNetworkReachedItsMinimum(int[] previousNeurons, int[] currentNeurons)
	{
		return Arrays.equals(previousNeurons, currentNeurons);
	}

	private boolean isDiagonalElement(int i, int j)
	{
		return i == j;
	}
}
