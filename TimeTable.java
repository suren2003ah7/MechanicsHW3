import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TimeTable extends JFrame implements ActionListener 
{

	private JPanel screen = new JPanel();

	private JPanel tools = new JPanel();

	private JButton[] tool;

	private JTextField[] field;

	private CourseArray courses;

	private Color[] CRScolor = {Color.RED, Color.GREEN, Color.BLACK};

	private Autoassociator autoassociator = null;

	private static int updatePeriod;

	private static int updateCount;
	
	public TimeTable() 
	{
		super("Dynamic Time Table");
		setSize(500, 800);
		setLayout(new FlowLayout());
		
		screen.setPreferredSize(new Dimension(400, 800));
		add(screen);
		
		setTools();
		add(tools);

		updatePeriod = 1;
		updateCount = 1;
		
		setVisible(true);
	}
	
	public void setTools() 
	{
		String[] capField = {"Slots:", "Courses:", "Clash File:", "Iters:", "Shift:", "Update Period:", "Update Count:"};
		field = new JTextField[capField.length];
		
		String[] capButton = {"Load", "Start", "Step", "Print", "Exit", "Continue", "Train"};
		tool = new JButton[capButton.length];
		
		tools.setLayout(new GridLayout(2 * capField.length + capButton.length, 1));
		
		for (int i = 0; i < field.length; i++) 
		{
			tools.add(new JLabel(capField[i]));
			field[i] = new JTextField(5);
			tools.add(field[i]);
		}
		
		for (int i = 0; i < tool.length; i++) 
		{
			tool[i] = new JButton(capButton[i]);
			tool[i].addActionListener(this);
			tools.add(tool[i]);
		}
		
		field[0].setText("30");
		field[1].setText("622");
		field[2].setText("uta-s-92.stu");
		field[3].setText("1");
	}
	
	public void draw() 
	{
		Graphics g = screen.getGraphics();
		int width = Integer.parseInt(field[0].getText()) * 10;
		for (int courseIndex = 1; courseIndex < courses.length(); courseIndex++) 
		{
			g.setColor(CRScolor[courses.status(courseIndex) > 0 ? 0 : 1]);
			g.drawLine(0, courseIndex, width, courseIndex);
			g.setColor(CRScolor[CRScolor.length - 1]);
			g.drawLine(10 * courses.slot(courseIndex), courseIndex, 10 * courses.slot(courseIndex) + 10, courseIndex);
		}
	}
	
	private int getButtonIndex(JButton source) 
	{
		int result = 0;
		while (source != tool[result]) result++;
		return result;
	}
	
	public void actionPerformed(ActionEvent click) 
	{
		int min;
		int step;
		int clashes;
		switch (getButtonIndex((JButton) click.getSource())) 
		{
			case 0:
				int slots = Integer.parseInt(field[0].getText());
				courses = new CourseArray(Integer.parseInt(field[1].getText()) + 1, slots);
				courses.readClashes(field[2].getText());
				if (autoassociator == null)
				{
					autoassociator = new Autoassociator(courses);
					System.out.println(autoassociator.getTrainingCapacity());
				}
				draw();
				break;
			case 1:
				min = Integer.MAX_VALUE;
				updatePeriod = Integer.parseInt(field[5].getText());
				updateCount = Integer.parseInt(field[6].getText());
				step = 0;
				for (int i = 1; i < courses.length(); i++)
				{
					courses.setSlot(i, 0);
				}
				for (int iteration = 1; iteration <= Integer.parseInt(field[3].getText()); iteration++) 
				{
					courses.iterate(Integer.parseInt(field[4].getText()));
					draw();
					clashes = courses.clashesLeft();
					if (clashes < min) 
					{
						min = clashes;
						step = iteration;
					}
					if (iteration % updatePeriod == 0)
					{
						for (int k = 0; k < updateCount; k++)
						{
							executeUnitUpdate();
						}
					}
				}
				System.out.println("Shift = " + field[4].getText() + "\tMin clashes = " + min + "\tat step " + step);
				setVisible(true);
				break;
			case 2:
				courses.iterate(Integer.parseInt(field[4].getText()));
				draw();
				break;
			case 3:
				System.out.println("Exam\tSlot\tClashes");
				for (int i = 1; i < courses.length(); i++)
				{
					System.out.println(i + "\t" + courses.slot(i) + "\t" + courses.status(i));
				}
				break;
			case 4:
				System.exit(0);
				break;
			case 5:
				min = courses.clashesLeft();
				step = 0;
				for (int iteration = 1; iteration <= Integer.parseInt(field[3].getText()); iteration++) 
				{
					courses.iterate(Integer.parseInt(field[4].getText()));
					draw();
					clashes = courses.clashesLeft();
					if (clashes < min) 
					{
						min = clashes;
						step = iteration;
					}
				}
				System.out.println("Shift = " + field[4].getText() + "\tMin clashes = " + min + "\tat step " + step);
				setVisible(true);
				break;
			case 6:
				min = Integer.MAX_VALUE;
				step = 0;
				for (int i = 1; i < courses.length(); i++)
				{
					courses.setSlot(i, 0);
				}
				for (int iteration = 1; iteration <= Integer.parseInt(field[3].getText()); iteration++) 
				{
					courses.iterate(Integer.parseInt(field[4].getText()));
					draw();
					clashes = courses.clashesLeft();
					if (clashes < min) 
					{
						min = clashes;
						step = iteration;
					}
				}
				setVisible(true);
				trainAssociator(Integer.parseInt(field[0].getText()));
				System.out.println(autoassociator.getTrainingCapacity());
		}
	}

	public static void main(String[] args)
	{
		new TimeTable();
	}

	private void executeUnitUpdate()
	{
		int[] clashedTimeSlot = getClashedTimeSlot();
		if (clashedTimeSlot != null)
		{
			int numOfSlots = Integer.parseInt(field[0].getText());
			int timeSlotIndex = clashedTimeSlot[0];
			int updatedNeuronIndex = autoassociator.unitUpdate(clashedTimeSlot);
			if (clashedTimeSlot[updatedNeuronIndex] == 1 && courses.slot(updatedNeuronIndex) != timeSlotIndex)
			{
				courses.setSlot(updatedNeuronIndex, timeSlotIndex);
			}
			if (clashedTimeSlot[updatedNeuronIndex] == -1 && courses.slot(updatedNeuronIndex) == timeSlotIndex)
			{
				int newTimeSlotIndex = (int) (Math.floor(Math.random() * numOfSlots));
				courses.setSlot(updatedNeuronIndex, newTimeSlotIndex);
			}
			draw();
		} else 
		{
			System.out.println("Minimum already reached!");
		}
	}

	private int[] getClashedTimeSlot()
	{
		for (int i = 1; i < courses.length(); i++)
		{
			if (doesCourseWithGivenIndexHaveClashes(i))
			{
				int[] clashedTimeSlot = courses.getTimeSlot(courses.slot(i));
				clashedTimeSlot[0] = courses.slot(i);
				return clashedTimeSlot;
			}
		}
		return null;
	}

	private void trainAssociator(int numOfTimeSlots)
	{
		if (autoassociator.getTrainingCapacity() == 0)
		{
			return;
		}
		int[] timeSlots = initializeTimeSlotArray(numOfTimeSlots);
		for (int i = 1; i < courses.length(); i++)
		{
			if (doesCourseWithGivenIndexHaveClashes(i))
			{
				timeSlots[courses.slot(i)] = -1;
			}
		}
		for (int i = 0; i < timeSlots.length; i++)
		{
			if (autoassociator.getTrainingCapacity() == 0)
			{
				return;
			}
			if (isTimeSlotClashFree(timeSlots[i]))
			{
				autoassociator.training(courses.getTimeSlot(timeSlots[i]));
			}
		}
	}

	private int[] initializeTimeSlotArray(int numOfTimeSlots)
	{
		int[] timeSlots = new int[numOfTimeSlots];
		for (int i = 0; i < numOfTimeSlots; i++)
		{
			timeSlots[i] = i;
		}
		return timeSlots;
	}

	private boolean doesCourseWithGivenIndexHaveClashes(int index)
	{
		return courses.status(index) != 0;
	}

	private boolean isTimeSlotClashFree(int timeSlot)
	{
		return timeSlot != -1;	
	}
}
