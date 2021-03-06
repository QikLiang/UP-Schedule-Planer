package graphics;

import core.Network;
import core.Schedule_Planer;
import data.Course;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Creates a GUI for calling core.Network to select courses
 */
public class CourseSelectionGraphics extends JPanel{

	private HashMap<String, String> termVal;
	private JComboBox<String> term;
	private CheckboxList subjectsList;
	private String[][] subjects;
	private Schedule_Planer main;

	/**
	 * creates page 1 of GUI. This includes a dropbox for
	 * selecting term and check list for selecting subjects
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
		if(main.term != null){
			term.setSelectedItem(main.term);
		} else {
			term.setSelectedIndex(1);
		}
		this.add(term);

		//get list of subjects and put it in gui list
		subjects = Network.getSubjects(terms[1][1]);
		ArrayList<String> subjectTexts = new ArrayList<>(subjects.length);
		for(String[] subject : subjects){
			subjectTexts.add(subject[0]);
		}
		subjectsList = new CheckboxList(subjectTexts);
		subjectsList.setSelected(main.selectedSubjects);
		this.add(subjectsList);

		//button to next page (select courses)
		JButton next = new JButton("Next");
		next.addActionListener(e -> {
			try {
				selectCourses();
				main.term = (String)term.getSelectedItem();
			} catch (Network.NetworkErrorException e1) {
				JOptionPane.showMessageDialog( null,
					"Error: Internet access not available. Please try again later");
			}
		});
		this.add(next);
	}

	/**
	 * page 2 of GUI. Selects subject and courseNum for each
	 * course, and convert it into data.Course[] when Next is pressed.
	 */
	private void selectCourses() throws Network.NetworkErrorException {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(Box.createVerticalGlue());
		//get courses
		main.selectedSubjects = subjectsList.getSelected();
		Set<String> subjectVals = new HashSet<>(main.selectedSubjects.size());
		for(String[] subject : subjects){
			if(main.selectedSubjects.contains(subject[0])){
				subjectVals.add(subject[1]);
			}
		}
		String termValue = termVal.get(term.getSelectedItem());
		Course[] courses = Network.getCourses(termValue,
				subjectVals);

		HashMap<String, ArrayList<Course>> coursesBySubjects = new HashMap<>();
		HashMap<String, Course> courseMap = new HashMap<>();
		ArrayList<Course> list;
		for (Course course : courses) {
			list = coursesBySubjects.get(course.subject);
			if (list == null) {
				list = new ArrayList<>();
				coursesBySubjects.put(course.subject, list);
			}
			list.add(course);
			courseMap.put(course.subject + course.courseNumber, course);
		}

		//display courses
		this.removeAll();
		String[] subjects = coursesBySubjects.keySet().toArray(new String[0]);
		CourseList core = new CourseList(subjects, coursesBySubjects, courseMap, main.courSelection);
		CourseList electives = new CourseList(subjects, coursesBySubjects, courseMap, main.electiveSelection);
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
		credMin.setText(main.preference.minCred+"");
		JTextField credMax = new JTextField();
		credMax.setColumns(2);
		credMax.setText(main.preference.maxCred+"");

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
			main.courSelection = core.getCourses();
			main.electiveSelection = electives.getCourses();
			HashSet<Course> allCourses = new HashSet<>();
			allCourses.addAll(electives.getCourses());
			allCourses.forEach(Course::addElectiveSection);
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
		CourseList parent;

		CourseRow(CourseList parent, String[] subjectList,
		          HashMap<String, ArrayList<Course>> courseList) {
			this.parent = parent;
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
		JPanel toJPanel(){
			panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			JButton remove = new JButton("-");
			remove.addActionListener(actionEvent -> parent.removeRow(this));
			panel.add(remove);
			panel.add(subject);
			return panel;
		}

		JPanel toJPanel(Course preset){
			toJPanel();
			subject.setSelectedItem(preset.subject);
			course.setSelectedItem(preset.courseNumber);
			return panel;
		}

		private String getSubject(){ return (String) subject.getSelectedItem(); }
		private String getCourse(){ return (String) course.getSelectedItem(); }
	}

	/**
	 * Creates an extendable list of course selections
	 */
	private class CourseList extends JPanel {
		ArrayList<CourseRow> courses;
		HashMap<String, Course> courseMap;
		JPanel panel;
		CourseList(String[] subjectList, HashMap<String, ArrayList<Course>> courseList,
								HashMap<String, Course> courseMap, Set<Course> preset){
			this.courseMap = courseMap;
			courses = new ArrayList<>();
			panel = new JPanel();
			panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS));
			JScrollPane scrollBar = new JScrollPane(panel);
			scrollBar.setPreferredSize( new Dimension(250, 150) );
			this.add(scrollBar);

			JButton add = new JButton("+");
			this.add(add);
			add.addActionListener(e->{
				CourseRow row = new CourseRow(this, subjectList, courseList);
				panel.add(row.toJPanel());
				//scroll to bottom
				panel.revalidate();
				JScrollBar sb = scrollBar.getVerticalScrollBar();
				sb.setValue(sb.getMaximum());
				panel.revalidate();
				courses.add(row);
			});

			if(preset != null){
				for(Course course : preset){
					try {
						CourseRow row = new CourseRow(this, subjectList, courseList);
						panel.add(row.toJPanel(course));
						courses.add(row);
					} catch (NullPointerException e){
						//subject no longer in list, move on
					}
				}
				//scroll to bottom
				panel.revalidate();
				JScrollBar sb = scrollBar.getVerticalScrollBar();
				sb.setValue(sb.getMaximum());
				panel.revalidate();
			}
		}

		private void removeRow(CourseRow row){
			courses.remove(row);
			panel.remove(row.panel);
			panel.revalidate();
			panel.repaint();
		}

		/**
		 * Get a list of all courses entered into list
		 */
		private Set<Course> getCourses(){
			Set<Course> courseList = new HashSet<>();
			Course course;
			for (CourseRow row : courses) {
				course = courseMap.get(row.getSubject() + row.getCourse());
				if (course != null) {
					courseList.add(course);
				}
			}
			return courseList;
		}
	}
}
