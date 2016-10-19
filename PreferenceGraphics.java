import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class PreferenceGraphics extends JPanel{

		//program wide variables
		Schedule_Planer mainProgram;
		Preference preference;
		Course database[];
		int courses;

		//gui variables
		ArrayList<String> instructorList;
		JTextField startHour;
		JTextField startMinute;
		JTextField endHour;
		JTextField endMinute;
		JTextField noonStartHour;
		JTextField noonStartMinute;
		JTextField noonEndHour;
		JTextField noonEndMinute;
		JTextField noonLength;
		JComboBox<String> startApm;
		JComboBox<String> endApm;
		JComboBox<String> noonStartApm;
		JComboBox<String> noonEndApm;
		JSlider start;
		JSlider end;
		JSlider noon;
		JSlider cluster;
		JSlider instructor;
		JCheckBox checkBox[];

		PreferenceGraphics(Schedule_Planer mainProgram){
				this.mainProgram = mainProgram;
				preference = mainProgram.preference;
				database = mainProgram.database;
				courses = mainProgram.courses;
				loadInstructors( database, courses );
				setLayout( new CardLayout() );
				add( page1(), "page1" );
				add( page2(), "page2" );
		}

		/**
		 * extract a list of instructors from database and add them to instructorList
		 * @param database
		 * @param courses
		 */
		void loadInstructors(Course database[], int courses) {
				//add instructors
				instructorList= new ArrayList<>();
				for (int course = 0; course < courses; course++) {
						for (int section = 0; section < database[course].sections; section++) {
								String temp = database[course].section[section].instructor;
								if(instructorList.indexOf(temp)==-1){
										instructorList.add(temp);
								}
						}
				}
		}

		/**
		 * graphics on page 1 of preference menu
		 * @return
		 */
		JPanel page1(){
				final String ampm[]= {"AM", "PM"};

				//page1
				JPanel page1 = new JPanel();
				page1.setLayout( new BoxLayout(page1, BoxLayout.Y_AXIS));

				//instructions
				JPanel div01 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
				div01.setMaximumSize(new Dimension(500, 200));
				div01.add( new JLabel("Please set your preference in the text boxes"));
				page1.add(div01);
				JPanel div02 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
				div02.setMaximumSize(new Dimension(500, 200));
				div02.add( new JLabel("and use the slide bars to evaluate how important it is"));
				page1.add(div02);

				//start text
				JPanel div1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
				div1.setMaximumSize(new Dimension(500,200));
				startHour = new JTextField();
				startHour.setColumns(2);
				startHour.setText("8");
				div1.add( new JLabel("have my day start after ") );
				div1.add( startHour );
				div1.add( new JLabel(":") );
				startMinute = new JTextField();
				startMinute.setColumns(2);
				startMinute.setText("11");
				div1.add(startMinute);
				startApm = new JComboBox<>( ampm );
				div1.add( startApm );
				page1.add( div1 );

				//start slider
				start = new JSlider( JSlider.HORIZONTAL, -10, 10, 0 );
				start.setMajorTickSpacing(10);
				start.setPaintLabels(true);
				JPanel div2 = new JPanel();
				div2.add(start);
				page1.add( div2 );

				//end text
				JPanel div3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
				div3.setMaximumSize(new Dimension(500,200));
				//div3.setPreferredSize( new Dimension(500, 10) );
				endHour = new JTextField();
				endHour.setColumns(2);
				endHour.setText("5");
				div3.add( new JLabel("have my day end before ") );
				div3.add( endHour );
				div3.add( new JLabel(":") );
				endMinute = new JTextField();
				endMinute.setColumns(2);
				endMinute.setText("00");
				div3.add(endMinute);
				endApm = new JComboBox<>( ampm );
				endApm.setSelectedIndex(1);
				div3.add( endApm );
				page1.add( div3 );

				//end slider
				end = new JSlider( JSlider.HORIZONTAL, -10, 10, 0 );
				end.setMajorTickSpacing(10);
				end.setPaintLabels(true);
				JPanel div4 = new JPanel();
				div4.add(end);
				page1.add( div4 );

				//noon
				JPanel div5 = new JPanel();
				div5.setLayout(new BoxLayout(div5, BoxLayout.Y_AXIS));
				div5.setMinimumSize(new Dimension(400, 250));
				div5.setMaximumSize(new Dimension(400, 300));

				//noon start
				JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
				noonStartHour = new JTextField();
				noonStartHour.setColumns(2);
				noonStartHour.setText("11");
				row1.add( new JLabel("have a lunch break sometime between ") );
				row1.add( noonStartHour );
				row1.add( new JLabel(":") );
				noonStartMinute = new JTextField();
				noonStartMinute.setColumns(2);
				noonStartMinute.setText("00");
				row1.add(noonStartMinute);
				noonStartApm = new JComboBox<>( ampm );
				row1.add( noonStartApm );
				div5.add(row1);

				//noon end
				JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
				noonEndHour = new JTextField();
				noonEndHour.setColumns(2);
				noonEndHour.setText("1");
				row2.add( new JLabel(" and\n") );
				row2.add( noonEndHour );
				row2.add( new JLabel(":") );
				noonEndMinute = new JTextField();
				noonEndMinute.setColumns(2);
				noonEndMinute.setText("00");
				row2.add(noonEndMinute);
				noonEndApm = new JComboBox<>( ampm );
				noonEndApm.setSelectedIndex(1);
				row2.add( noonEndApm );

				//noon length
				row2.add( new JLabel(" that lasts for at least ") );
				noonLength = new JTextField();
				noonLength.setColumns(2);
				noonLength.setText("45");
				row2.add(noonLength);
				row2.add( new JLabel(" minutes") );
				div5.add(row2);
				page1.add( div5 );

				noon = new JSlider( JSlider.HORIZONTAL, 0, 10, 0 );
				noon.setMajorTickSpacing(10);
				noon.setPaintLabels(true);
				JPanel div6 = new JPanel();
				div6.add(noon);
				page1.add( div6 );

				//clustering
				JPanel div7 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
				div7.add( new JLabel("have classes clustered together") );
				page1.add(div7);
				div7.setMaximumSize(new Dimension(500,200));
				cluster = new JSlider( JSlider.HORIZONTAL, -10, 10, 0 );
				cluster.setMajorTickSpacing(10);
				cluster.setPaintLabels(true);
				Hashtable <Integer, JLabel> clusterLabel = new Hashtable<>();
				clusterLabel.put( new Integer(-10), new JLabel("evenly lengthed days") );
				clusterLabel.put( new Integer(10), new JLabel("easy day\n and hard day") );
				cluster.setLabelTable( clusterLabel );
				cluster.setPreferredSize( new Dimension(400,30) );
				JPanel div8 = new JPanel();
				div8.add(cluster);
				page1.add( div8 );
				
				//glue
				page1.add(Box.createVerticalGlue());

				//next
				JButton next = new JButton("Next");
				JPanel temp = this;
				next.addActionListener( new ActionListener(){
						public void actionPerformed(ActionEvent e){
								CardLayout cl = (CardLayout) getLayout();
								cl.next(temp);
						}
				} );
				JPanel div9 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
				div9.setMaximumSize(new Dimension(500,50));
				div9.add(next);
				page1.add(div9);

				return page1;
		}

		/**
		 * graphics on page 2 of preference menu
		 * @return
		 */
		JPanel page2(){
				JPanel page2 = new JPanel();

				JPanel div1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
				page2.setLayout( new BoxLayout(page2, BoxLayout.Y_AXIS));
				div1.setMaximumSize(new Dimension(500,200));
				div1.add( new JLabel("have specific instructors for courses") );
				page2.add( div1 );

				instructor = new JSlider( JSlider.HORIZONTAL, -10, 10, 0 );
				instructor.setMajorTickSpacing(10);
				instructor.setPaintLabels(true);
				JPanel div2 = new JPanel();
				div2.add(instructor);
				page2.add( div2 );

				JPanel list = new JPanel();
				list.setLayout( new BoxLayout(list, BoxLayout.Y_AXIS));
				checkBox= new JCheckBox[instructorList.size()];
				for( int i=0; i<instructorList.size(); i++ ){
						JCheckBox cb = new JCheckBox(instructorList.get(i));
						checkBox[i]=cb;
						list.add(cb);
				}

				JScrollPane scrollBar = new JScrollPane(list);
				scrollBar.setPreferredSize( new Dimension(400, 300) );
				page2.add(scrollBar);

				PreferenceGraphics panel = this;//for changing panel inside button

				//prev
				JButton prev = new JButton("Previous");
				prev.addActionListener( new ActionListener(){
						public void actionPerformed(ActionEvent e){
								CardLayout cl = (CardLayout) getLayout();
								cl.show(panel, "page1");
						}
				} );

				//finish
				JButton finish = new JButton("Finish");
				finish.addActionListener( new ActionListener(){
						public void actionPerformed(ActionEvent e){
								panel.loadPreference();
								mainProgram.createPlans();
								mainProgram.startOutputGraphics();
						}
				} );

				//button div
				JPanel div3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
				div3.setMaximumSize(new Dimension(500,200));
				div3.add(prev);
				div3.add(finish);
				page2.add(div3);

				return page2;
		}

		/**
		 * put info from gui into preference variable
		 */
		private void loadPreference(){
				//start
				preference.start = start.getValue();
				preference.startTime.hour = Integer.parseInt(startHour.getText());
				preference.startTime.minute = Integer.parseInt(startMinute.getText());
				if(startApm.getSelectedIndex()==1){
					preference.startTime.hour+=12;
				}

				//end
				preference.end = end.getValue();
				preference.endTime.hour = Integer.parseInt(endHour.getText());
				preference.endTime.minute = Integer.parseInt(endMinute.getText());
				if(endApm.getSelectedIndex()==1){
					preference.endTime.hour+=12;
				}

				//noon
				preference.noon = noon.getValue();
				preference.noonBegin.hour = Integer.parseInt(noonStartHour.getText());
				preference.noonBegin.minute = Integer.parseInt(noonStartMinute.getText());
				preference.noonEnd.hour = Integer.parseInt(noonEndHour.getText());
				preference.noonEnd.minute = Integer.parseInt(noonEndMinute.getText());
				preference.duration = Integer.parseInt(noonLength.getText());
				if(noonStartApm.getSelectedIndex()==1){
					preference.noonBegin.hour+=12;
				}
				if(noonEndApm.getSelectedIndex()==1){
					preference.noonEnd.hour+=12;
				}

				//instructors
				preference.instructor = instructor.getValue();
				for(int i=0; i<instructorList.size(); i++){
						if(checkBox[i].isSelected()){
								preference.instructorList.add(instructorList.get(i));
								preference.instructors[preference.Instructors]=instructorList.get(i);
								preference.Instructors++;
						}
				}

				//clustering
				preference.clustering = cluster.getValue();
		}
}
