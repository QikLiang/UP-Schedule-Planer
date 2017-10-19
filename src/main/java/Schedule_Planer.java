import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import java.io.*;
import java.util.Set;

/**
 * Main class. The main method and all big methods called by main
 *
 * @author Qi Liang
 * @version 2016.10.16
 */
public class Schedule_Planer {
	//universal variables
	static final int SECTIONS = 25; //maximum number of sections per course
	static final int COURSES = 9; //maximum number of courses in database
	static final int PLANS = 100;//maximum number of different schedules the
	//program can handle
	Scanner keyboard = new Scanner(System.in);

	public static boolean testing = true;
	//address of the database file
	final String DATABASE = "database.txt";

	//instance variables
	Course[] database;
	Plan[] plan;
	int courses;
	int plans;
	Preference preference;

	//gui variables
	JFrame window;
	Container contentpane;
	CardLayout contentPaneLayout;
	JPanel messagePane;
	JTextArea messageBox;

	public static void main(String[] args) {
		Schedule_Planer planer = new Schedule_Planer();
		planer.displayGUI();
	}

	/*
	 * framework code. instantiate attributes
	 */
	private Schedule_Planer(){
		// declaration and instantiation
		database = new Course[COURSES];
		plan = new Plan[PLANS];
		courses = 0;
		plans = 0;
		preference = new Preference();

		for (int i = 0; i < COURSES; i++) {
			database[i] = new Course();
		}

		//set up GUI
		window = new JFrame("UP Schedule Planer");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(430, 450);
		window.setLocationByPlatform(true);
		contentpane = window.getContentPane();
		contentPaneLayout = new CardLayout();
		contentpane.setLayout(contentPaneLayout);

		//add text display panel
		messagePane = new JPanel();
		messagePane.setLayout(new BoxLayout(messagePane, BoxLayout.Y_AXIS));
		messageBox = new JTextArea();
		messageBox.setPreferredSize(new Dimension(400, 400));
		messageBox.setEditable(false);
		((DefaultCaret) messageBox.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		messagePane.add(messageBox);

	}
	
	/**
	 * set gui JFrame to visible
	 */
	private void displayGUI(){
		contentpane.add(messagePane, "Message");
		window.setVisible(true);
		//window.setResizable(false);

		messageBox.append("Schedule planer ver 2.2.1 for University of Portland, by Qi Liang\n");

		//create buttons for options
		Schedule_Planer temp = this;
		JPanel div = new JPanel();
		JButton open = new JButton("Open Saved Schedule");
		open.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				temp.readSchedule();
			}
		});
		JButton start = new JButton("Start");
		start.addActionListener(event -> {
			try {
				contentpane.add(new CourseSelectionGraphics(this), "select courses");
				contentPaneLayout.show( contentpane,"select courses");
			} catch (Network.NetworkErrorException e) {
				JOptionPane.showMessageDialog( null,
						"Error: Internet access not available. Please try again later");
			}
		});
		
		//add buttons to bottom of window
		div.setLayout( new BoxLayout(div, BoxLayout.X_AXIS));
		div.add(open);
		div.add(Box.createHorizontalGlue());
		div.add(start);
		messagePane.add(div);
		messagePane.revalidate();
		/*
		// prompt user for input and load data into database and preference object
		setupDatabase();
		*/
	}
	
	/**
	 * read from a file and set up output graphics
	 */
	public void readSchedule(){
		boolean readSuccessful = true;
		JFileChooser fc = new JFileChooser();
		int val = fc.showOpenDialog(window);
		if (val == JFileChooser.APPROVE_OPTION){
			ObjectInputStream input;
			try {//open file
				input = new ObjectInputStream(new FileInputStream(fc.getSelectedFile()));
			} catch (IOException e) {
				//stop if fail to open file
				return;
			}
			try{//write to file
				database = (Course[]) input.readObject();
				plan = (Plan[]) input.readObject();
				plans = input.readInt();
				preference = (Preference) input.readObject();
			}catch (Exception e){ 
				readSuccessful = false;
			}
			try {//close file
				input.close();
			} catch (IOException e) { }
			if(readSuccessful){
				startOutputGraphics();
			}
		}
	}

	/**
	 * Enumerate through all possible class schedules and put all viable schedules
	 * input plan[]. When number of plans exceed array size, the array is shorted
	 * and the half with lower scores is discarded, and then continue.
	 */
	void createPlans() {
		plans = 0;
		Plan thisPlan;
		int[] thisPlanPath = new int[courses];

		while (thisPlanPath[0] < database[0].sections) {// && plans < PLANS){
			thisPlan = new Plan(courses, thisPlanPath);
			if (plans >= PLANS - 1) {
				sortPlans(plan, plans);
				plans = plans / 2;
			} // */

			thisPlan.evaluateScore(database, preference);
			if (thisPlan.score != 0) {
				plan[plans] = thisPlan;
				plans++;
			}

			// increment to next path
			thisPlanPath[courses - 1]++;
			for (int i = courses - 1; i > 0; i--) {
				if (thisPlanPath[i] >= database[i].sections) {
					thisPlanPath[i] = thisPlanPath[i] % database[i].sections;
					thisPlanPath[i - 1]++;
				}
			}
		}

		sortPlans(plan, plans);
	}

	/**
	 * a bubble short algorithm that shorts the plans into descending order by score
	 * @param plan array with all the plans in it
	 * @param plans how many elements of the array is filled
	 */
	static void sortPlans(Plan plan[], final int plans) {
		Plan temp; // holding variable
		for (int i = 0; i < plans; i++) {
			for (int j = 0; j < plans - 1 - i; j++) {
				if (plan[j].score < plan[j + 1].score) {
					temp = plan[j];
					plan[j] = plan[j + 1];
					plan[j + 1] = temp;
				}
			}
		}
		return;
	}

	/**
	 * create and display output graphics using database and preference info
	 */
	public void startOutputGraphics(){
		JPanel output = OutputGraphics.createGraphicsJPanel(database, plan, plans, preference);
		contentpane.add(output, "Output");
		window.setSize(OutputGraphics.CHARTWIDTH+OutputGraphics.INFOWIDTH+OutputGraphics.OFFSHIFTX, 
				OutputGraphics.CHARTHEIGHT+OutputGraphics.OFFSHIFTY+10);
		window.setLocationRelativeTo(null);
		((CardLayout) contentpane.getLayout()).show(contentpane, "Output");
	}
}
