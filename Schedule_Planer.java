import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import java.io.*;

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

	public static boolean testing = false;
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
	JPanel contentpane;
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
		contentpane = new JPanel();
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
		window.add(contentpane);
		window.setVisible(true);
		window.setResizable(false);

		messageBox.append("Schedule planer ver 2.1 for University of Portland, by Qi Liang\n");

		// prompt user for input and load data into database and preference object
		setupDatabase();
	}

	/**
	 * create and setup database file, and prompt user to input data
	 *
	 * @return the number of courses in the database
	 */
	void setupDatabase() {
		File file = new File(DATABASE);
		Scanner input = null;
		PrintWriter write = null;
		boolean newFile;
		int tempInt = 0;

		// try to open text file for user to enter data
		newFile = true;
		if (file.exists()) {
			if(testing){
				newFile = false;
			}else{
				tempInt = JOptionPane.showConfirmDialog(null, //window,
						"Database seems to already exist, do you want to use it?",
						"Database exists", JOptionPane.YES_NO_OPTION);
				newFile = tempInt!=JOptionPane.YES_OPTION;
			}
		}
		if (newFile) {
			try {
				write = new PrintWriter(file);
			} catch (FileNotFoundException e) {
				messageBox.append("\nError: cannot open file to write.");
				System.exit(1);
			}

			// configure the file before showing it to user
			write.println(
					"copy information about the courses you want to take from selfserve (the whole row for each section) ");
			write.println(
					"and paste it below. Press enter after each course to make sure the next section is on a new line.");
			write.println("Save the document and close it. Example:\n");
			write.println(
					"SR   40299   EXP 203 A   1   3.000   Introduction to Computer Science    MWF 11:25 am-12:20 pm   30  2   28  0   0   0   0   0   0   James Michael Schmidt (P)   01/11-04/28 SHILEY 301\n\n");
			write.println("paste below:\n");
			write.close();
			if (!Desktop.isDesktopSupported()) {
				messageBox.append("\nError: Please open and edit '" + DATABASE + "' manually.");
			}
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.open(file);
			} catch (IOException e) {
				messageBox.append("\nError: Please open and edit '" + DATABASE + "' manually.");
			}

			messageBox.append("\nFollow instructions on the text file that just poped up.");
			messageBox.append("\nAfter the text file has been saved and exited, press Next.");
		}

		try {
			input = new Scanner(file);
		} catch (FileNotFoundException e) {
			messageBox.append("\nError: cannot open file to read.");
			System.exit(1);
		}
		
		final Scanner finalInput = input;//stop complier from complaining when used in button
		Schedule_Planer mainProgram = this;//to pass into button
		JButton next = new JButton("Next");
		next.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean parsed = parseDatabase(finalInput);
				//if parsing failed
				if(!parsed){
					next.setEnabled(false);
					return;
				}

				messageBox.append("\n\n\nfinish loading database");

				PreferenceGraphics pg = new PreferenceGraphics(mainProgram);
				contentpane.add(pg, "Preference");
				contentPaneLayout.show(contentpane, "Preference");
			}
		});
		JPanel div = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		div.add(next);
		messagePane.add(div);
		messagePane.revalidate();
	}
	
	/**
	 * parse the database file user has inputed
	 * 
	 * @param input a scanner variable connected to the database file
	 * @return true if database is successfully parsed and data is enter, false otherwise
	 */
	private boolean parseDatabase(Scanner input){
		boolean success = true;
		int tempInt=0;
		String temp;
		float credit;
		boolean newCourse = true;
		Course thisCourse = new Course();
		int courseIndex = 0;
		String classdays;
		Time startTime = new Time();
		Time endTime = new Time();
		String line[];

		// remove instructions before parsing
		do {
			temp = input.nextLine();
		} while (!temp.equals("paste below:"));

		//keep inputing until maxed out or finished
		while (input.hasNextLine() && courses < COURSES) { 
			// clear the temp objects
			thisCourse = new Course();
			startTime = new Time();
			endTime = new Time();

			line = input.nextLine().split("\\t");
			//stop of line doesn't have the right length
			if(line.length<21){
				continue;
			}

			//input shared between the two cases of whether the line is a new section
			classdays = line[8];
			Time.parseTime(line[9], startTime, endTime);

			// if the first thing is SR, it means it's a new section
			if (line[0].trim().length()>0) { 
				thisCourse.section[0].crn = Integer.parseInt(line[1]);
				thisCourse.subject = line[2];
				thisCourse.courseNumber = line[3];
				thisCourse.section[0].sectionNumber = line[4];
				// course info
				credit = Float.parseFloat(line[6]);
				thisCourse.title = line[7];

				// ignore classes with undetermined time
				if (classdays.equals("TBA")) {
					classdays = "";
					continue;

				}
				
				// finish inputing info
				thisCourse.section[0].instructor = line[19];
				thisCourse.section[0].location = line[21].trim();

				// match course with database
				newCourse = true;
				// go through all already existing courses
				for (int i = courses - 1; i >= 0; i--) {
					// if the course already exist
					if (thisCourse.subject.equals(database[i].subject)
							&& thisCourse.courseNumber.equals(database[i].courseNumber)) {
						courseIndex = i;
						newCourse = false;
					}
				}
				if (newCourse) {// if the course is not in database yet
					courseIndex = courses;
					database[courseIndex].subject = thisCourse.subject;
					database[courseIndex].courseNumber = thisCourse.courseNumber;
					database[courseIndex].credit = (int) (credit);
					database[courseIndex].title = thisCourse.title;

					courses++;
				}

				if (database[courseIndex].sections >= SECTIONS) {
					messageBox.append("\n\nError: the number of sections in course " + database[courseIndex].subject + ' '
							+ database[courseIndex].courseNumber
							+ " is larger than the maximum capacity of this program of " + SECTIONS + ' '
							+ thisCourse.section[0].sectionNumber);
					success = false;
				}

				// transfer info into database
				database[courseIndex].section[database[courseIndex].sections].crn = thisCourse.section[0].crn;
				database[courseIndex].section[database[courseIndex].sections].sectionNumber = thisCourse.section[0].sectionNumber;

				// inputing the times based on day of the week
				for (int i = 0; i < classdays.length(); i++) {
					switch (classdays.charAt(i)) {
					case 'M':
						tempInt = 0;
						break;
					case 'T':
						tempInt = 1;
						break;
					case 'W':
						tempInt = 2;
						break;
					case 'R':
						tempInt = 3;
						break;
					case 'F':
						tempInt = 4;
					}
					database[courseIndex].section[database[courseIndex].sections].schedule[tempInt][0] = startTime;
					database[courseIndex].section[database[courseIndex].sections].schedule[tempInt][1] = endTime;
					database[courseIndex].section[database[courseIndex].sections].instructor = thisCourse.section[0].instructor;
					database[courseIndex].section[database[courseIndex].sections].location = thisCourse.section[0].location;
				}
				database[courseIndex].sections++;
			} else {// if there's no SR, it's probably continuation from previous line
				// inputing the times based on day of the week
				for (int i = 0; i < classdays.length(); i++) {
					switch (classdays.charAt(i)) {
					case 'M':
						tempInt = 0;
						break;
					case 'T':
						tempInt = 1;
						break;
					case 'W':
						tempInt = 2;
						break;
					case 'R':
						tempInt = 3;
						break;
					case 'F':
						tempInt = 4;
					}
					database[courseIndex].section[database[courseIndex].sections - 1].schedule[tempInt][0] = startTime;
					database[courseIndex].section[database[courseIndex].sections - 1].schedule[tempInt][1] = endTime;
				}
			}
		} 
		input.close();
		return success;
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
		OutputGraphics og = new OutputGraphics(database, plan, plans, preference);
		contentpane.add(og, "Output");
		window.setSize(OutputGraphics.CHARTWIDTH+OutputGraphics.INFOWIDTH, OutputGraphics.CHARTHEIGHT+100);
		window.setLocationRelativeTo(null);
		((CardLayout) contentpane.getLayout()).show(contentpane, "Output");
	}
}
