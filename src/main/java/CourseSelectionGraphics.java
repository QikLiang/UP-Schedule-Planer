import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Creates a GUI for calling Network to select courses
 */
public class CourseSelectionGraphics extends JPanel{

	HashMap<String, String> termVal;
	JComboBox<String> term;
	CheckboxList subjectsList;
	String[][] subjects;
	Schedule_Planer main;

	/**
	 * creates page 1 of GUI. This includes a dropbox for
	 * selecting term and check list for selecting subjects
	 * @param main
	 */
	public CourseSelectionGraphics (Schedule_Planer main) throws Network.NetworkErrorException {
		this.main = main;
		//get list of terms and add them into dropdown
		String[][] terms = Network.getTerms();
		String[] termText = new String[terms.length];
		termVal = new HashMap<>();
		for(int i=0; i<terms.length; i++){
			termVal.put(terms[i][0],terms[i][1]);
			termText[i] = terms[i][0];
		}
		term = new JComboBox<>(termText);
		term.setSelectedIndex(1);
		this.add(term);

		//get list of subjects and put it in gui list
		subjects = Network.getSubjects(terms[1][1]);
		String[] subjectTexts = new String[subjects.length];
		for(int i=0; i<subjectTexts.length; i++){
			subjectTexts[i] = subjects[i][0];
		}
		subjectsList = new CheckboxList(subjectTexts);
		this.add(subjectsList);

		//button to next page (select courses)
		JButton next = new JButton("Next");
		next.addActionListener(e -> {
			try {
				selectCourses();
			} catch (Network.NetworkErrorException e1) {
				JOptionPane.showMessageDialog( null,
					"Error: Internet access not available. Please try again later");
			}
		});
		this.add(next);
	}

	/**
	 * page 2 of GUI. Selects subject and courseNum for each
	 * course, and convert it into Course[] when Next is pressed.
	 */
	private void selectCourses() throws Network.NetworkErrorException {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(Box.createVerticalGlue());
		//get courses
		boolean[] selected = subjectsList.getSelected();
		ArrayList<String> subjectVals = new ArrayList<>();
		for(int i=0; i<selected.length; i++){
			if(selected[i]){
				subjectVals.add(subjects[i][1]);
			}
		}
		String termValue = termVal.get(term.getSelectedItem());
		Course[] courses = Network.getCourses(termValue,
				subjectVals.toArray(new String[0]));

		//display courses
		HashMap<String, ArrayList<Course>> coursesBySubjects = new HashMap<>();
		HashMap<String, Course> courseMap = new HashMap<>();
		ArrayList<Course> list;
		for (int i=0; i<courses.length; i++){
			list = coursesBySubjects.get(courses[i].subject);
			if (list == null){
				list = new ArrayList<>();
				coursesBySubjects.put(courses[i].subject, list);
			}
			list.add(courses[i]);
			courseMap.put(courses[i].subject+courses[i].courseNumber, courses[i]);
		}
		this.removeAll();
		String[] subjects = coursesBySubjects.keySet().toArray(new String[0]);
		CourseList core = new CourseList(subjects, coursesBySubjects, courseMap);
		CourseList electives = new CourseList(subjects, coursesBySubjects, courseMap);
		JPanel div1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		div1.add(new JLabel("Courses you have to take:"));
		this.add(div1);
		this.add(core);
		JPanel div2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		div2.add(new JLabel("Courses you consider taking:"));
		this.add(div2);
		this.add(electives);

		//num credits specification
		JTextField credMin = new JTextField();
		credMin.setColumns(2);
		credMin.setText("15");
		JTextField credMax = new JTextField();
		credMax.setColumns(2);
		credMax.setText("18");

		JPanel credRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0,0));
		credRow.add(new JLabel("Credits: between "));
		credRow.add(credMin);
		credRow.add(new JLabel(" and "));
		credRow.add(credMax);
		this.add(credRow);

		JButton next = new JButton("Next");
		this.add(next);
		next.addActionListener(e -> {
			main.preference.minCred = Integer.parseInt(credMin.getText());
			main.preference.maxCred = Integer.parseInt(credMax.getText());
			HashSet<Course> allCourses = new HashSet<>();
			allCourses.addAll(electives.getCourses());
			allCourses.forEach(course -> course.addElectiveSection());
			allCourses.addAll(core.getCourses());
			main.database = allCourses.toArray(new Course[0]);
			main.courses = main.database.length;
			Container parent = this.getParent();
			parent.add(new PreferenceGraphics(main), "preference");
			((CardLayout)parent.getLayout()).show(parent,"preference");
		});
		this.add(Box.createVerticalGlue());
		this.revalidate();
		this.repaint();
	}

	/**
	 * Contains the graphics and data for one single course selection.
	 */
	private class CourseRow {
		private JComboBox<String> subject, course;
		JPanel panel;

		CourseRow(String[] subjectList, HashMap<String, ArrayList<Course>> courseList) {
			subject = new JComboBox<>(subjectList);
			subject.insertItemAt("", 0);
			subject.setSelectedIndex(0);
			subject.addActionListener(e -> {
				if(subject.getSelectedIndex() == 0){
					return;
				}
				if(course!=null){
					panel.remove(course);
				}
				String sub = (String) subject.getSelectedItem();
				ArrayList<Course> courses = courseList.get(sub);
				String[] courseNums = new String[courses.size()];
				for(int i=0; i<courseNums.length; i++){
					courseNums[i] = courses.get(i).courseNumber;
				}
				Arrays.sort(courseNums);
				course = new JComboBox<>(courseNums);
				panel.add(course);
				panel.revalidate();
			});
		}

		/**
		 * Creates the GUI object to be placed into GUI list
		 */
		public JPanel toJPanel(){
			panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			panel.add(subject);
			return panel;
		}

		public String getSubject(){ return (String) subject.getSelectedItem(); }
		public String getCourse(){ return (String) course.getSelectedItem(); }
	}

	/**
	 * Creates an extendable list of course selections
	 */
	private class CourseList extends JPanel {
		ArrayList<CourseRow> courses;
		HashMap<String, Course> courseMap;
		CourseList(String[] subjectList, HashMap<String, ArrayList<Course>> courseList,
								HashMap<String, Course> courseMap){
			this.courseMap = courseMap;
			courses = new ArrayList<>();
			JPanel panel = new JPanel();
			panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS));
			JScrollPane scrollBar = new JScrollPane(panel);
			scrollBar.setPreferredSize( new Dimension(250, 150) );
			this.add(scrollBar);

			JButton add = new JButton("+");
			this.add(add);
			add.addActionListener(e->{
				CourseRow row = new CourseRow(subjectList, courseList);
				panel.add(row.toJPanel());
				panel.revalidate();
				courses.add(row);
			});
		}

		/**
		 * Get a list of all courses entered into list
		 */
		public Set<Course> getCourses(){
			Set<Course> courseList = new HashSet<>();
			Course course;
			CourseRow row;
			for(int i=0; i<courses.size(); i++){
				row = courses.get(i);
				course = courseMap.get(row.getSubject() + row.getCourse());
				if (course != null){
					courseList.add(course);
				}
			}
			return courseList;
		}
	}
}
