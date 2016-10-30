import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EventGraphics {
	private final String ampm[]= {"AM", "PM"};

	public JTextField name;
	public JTextField startHour;
	public JTextField startMinute;
	public JComboBox<String> startApm;
	public JTextField endHour;
	public JTextField endMinute;
	public JComboBox<String> endApm;
	public JCheckBox weekDays[];

	public EventGraphics(){
		name = new JTextField();
		name.setColumns(15);
		startHour = new JTextField(2);
		startMinute = new JTextField(2);
		startApm = new JComboBox<>(ampm);
		endHour = new JTextField(2);
		endMinute = new JTextField(2);
		endApm = new JComboBox<>(ampm);
		weekDays = new JCheckBox[5];
		String days[] = {"M", "Tu", "W", "Th", "F"};
		for(int i=0; i<5; i++){
			weekDays[i] = new JCheckBox(days[i]);
		}
	}
	
	/**
	 * create a JPanel with all of the attributes in it formated
	 * for adding into preference graphics' page3
	 * @return
	 */
	public JPanel toJPanel(){
		JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		row1.setMaximumSize(new Dimension(400, 30));
		row1.add(new JLabel("Event: "));
		row1.add(name);
		row1.add(new JLabel(" is from "));
		row1.add(startHour);
		row1.add(new JLabel(":"));
		row1.add(startMinute);
		row1.add(startApm);

		JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		row2.setMaximumSize(new Dimension(400, 30));
		row2.add(new JLabel(" to "));
		row2.add(endHour);
		row2.add(new JLabel(":"));
		row2.add(endMinute);
		row2.add(endApm);
		row2.add(new JLabel(" on "));
		for(int i=0; i<5; i++){
			row2.add(weekDays[i]);
		}

		JPanel div = new JPanel();
		div.setLayout( new BoxLayout(div, BoxLayout.Y_AXIS));
		div.setMaximumSize(new Dimension(400, 50));
		div.add(row1);
		div.add(row2);
		return div;
	}
	
	/**
	 * convert an event's information to a section with the section number set to the name of the event
	 * @return
	 */
	public Section toSection(){
		Section section = new Section();
		
		//name
		section.sectionNumber = name.getText();
		
		//schedule
		for(int day=0; day<5; day++){
			if(weekDays[day].isSelected()){
				section.schedule[day][0] = new Time(Integer.parseInt(startHour.getText()), Integer.parseInt(startMinute.getText()));
				if( ((String)startApm.getSelectedItem()).equals(ampm[1]) ){
					section.schedule[day][0].increment12Hours();
				}
				section.schedule[day][1] = new Time(Integer.parseInt(endHour.getText()), Integer.parseInt(startMinute.getText()));
				if( ((String)endApm.getSelectedItem()).equals(ampm[1]) ){
					section.schedule[day][1].increment12Hours();
				}
			} else{
				section.schedule[day][0] = new Time();
				section.schedule[day][1] = new Time();
			}
		}
		return section;
	}
}
