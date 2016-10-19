import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.NoSuchElementException;
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
	}

	/*
	 * framework code. calls other methods for asking input, performing
	 * calculations, and output results
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

		//start with text display panel
		messagePane = new JPanel();
		messagePane.setLayout(new BoxLayout(messagePane, BoxLayout.Y_AXIS));
		messageBox = new JTextArea();
		messageBox.setPreferredSize(new Dimension(400, 400));
		messageBox.setEditable(false);
		((DefaultCaret) messageBox.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		messagePane.add(messageBox);

		//display GUI
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
					"and paste it below. Press enter after each course to make sure the next section is    on a new line.");
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
				parseDatabase(finalInput);
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
	 */
	private void parseDatabase(Scanner input){
		int tempInt=0;
		String temp;
		float credit;
		boolean newCourse = true;
		Course thisCourse = new Course();
		int courseIndex = 0;
		String classdays;
		Time startTime = new Time();
		Time endTime = new Time();

		// remove instructions before parsing
		do {
			temp = input.nextLine();
		} while (!temp.equals("paste below:"));

		// input courses
		while (input.hasNextLine() && courses < COURSES) { // keep inputing
			// until maxed out
			// or finished
			// clear the temp objects
			thisCourse = new Course();
			startTime = new Time();
			endTime = new Time();

			input.useDelimiter("\\t");
			temp = input.next();// check is there the SR
			if (temp.trim().length()>0) { // if the first thing is SR, it means it's a new
				// section
				thisCourse.section[0].crn = input.nextInt();
				thisCourse.subject = input.next();
				thisCourse.courseNumber = input.next();
				thisCourse.section[0].sectionNumber = input.next();// input
				// course
				// info
				temp = input.next();
				credit = input.nextFloat();
				thisCourse.title = input.next();
				classdays = input.next();

				// ignore classes with undetermined time
				if (classdays.equals("TBA")) {
					input.nextLine();
					classdays = "";
					continue;

				}

				// input class starting time
				inputTime(input, startTime);
				input.useDelimiter("-");
				temp = input.next();
				if (temp.equals(" pm")) {// convert time into military format
					startTime.hour = startTime.hour % 12 + 12;
				}

				// input class ending time
				input.useDelimiter("\\t");
				inputTime(input, endTime);
				temp = input.next();
				if (temp.equals(" pm")) {// convert time into military format
					endTime.hour = endTime.hour % 12 + 12;
				}

				// get rid of useless info
				for (int i = 0; i < 9; i++) {
					temp = input.next();
				}

				// finish inputing info
				thisCourse.section[0].instructor = input.next();
				temp = input.next(); // don't care about date
				thisCourse.section[0].location = input.nextLine().trim();

				// match course with database
				newCourse = true;
				for (int i = courses - 1; i >= 0; i--) {// go through all
					// already existing
					// courses
					if (thisCourse.subject.equals(database[i].subject)
							&& thisCourse.courseNumber.equals(database[i].courseNumber)) {// if
						// the
						// course
						// already
						// exist
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
					System.out.print("Error: the number of sections in course " + database[courseIndex].subject + ' '
							+ database[courseIndex].courseNumber
							+ " is larger than the maximum capacity of this program of " + SECTIONS + ' '
							+ thisCourse.section[0].sectionNumber);
					keyboard.nextLine();
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
				database[courseIndex].sections++;// cout << 'e';
			} else {// if there's no SR, it's probably continuation from // previous line
				if(!input.hasNext()){
					break;
				}
				do {
					classdays = input.next();
				} while (classdays.trim().length()==0);

				// input class starting time
				inputTime(input, startTime);
				input.useDelimiter("-");
				temp = input.next();
				if (temp.equals(" pm")) {// convert time into military format
					startTime.hour = startTime.hour % 12 + 12;
				}

				// input class ending time
				input.useDelimiter("\\t");
				inputTime(input, endTime);
				temp = input.next();
				if (temp.equals(" pm")) {// convert time into military format
					endTime.hour = endTime.hour % 12 + 12;
				}

				input.nextLine();// get rid of remaining stuff

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
					// cout << 'd';// << database[0].title[3000];
				}
			}
		} // cout << database[0].title[3000];
		input.close();
	}

	/**
	 * given a scanner variable, skip input from scanner until the next
	 * token is a number
	 * 
	 * @param input the sanner variable to skip from
	 */
	public static void skipTilNum(Scanner input) {
		while (!input.hasNextInt()) {
			try {
				input.skip("\\D");
			} catch (NoSuchElementException e) {
				return;
			}
		}
	}

	/**
	 * parse input from scanner variable and store it in a time variable
	 * does not include am/pm info
	 * 
	 * @param input the input stream
	 * @param time the time variable to store data into
	 */
	public static void inputTime(Scanner input, Time time) {
		// store the delimiter currently being used
		String delimiter = input.delimiter().pattern();
		input.useDelimiter("\\s");
		String[] string = input.next().split(":");
		time.hour = Math.abs(Integer.parseInt(string[0]));
		// when a dash is in front of a number, it can be interpreted as a
		// negative sign, so take the absolute value
		time.minute = Integer.parseInt(string[1]);
		input.useDelimiter(delimiter);
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
