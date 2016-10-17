import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("serial")
/**
 * this class takes care of ending graphics
 */
public class OutputGraphics extends Panel implements KeyEventDispatcher
{
	final Course[] database;
	final Plan[] plan;
	private int currentPlan = 0;
	private final int plans;
	public static final int RECTWIDTH = 100;
	public static final int RECTHEIGHT = 50;
	public static final int OFFSHIFTX = 75;
	public static final int OFFSHIFTY = 50;
	public static final int CHARTWIDTH = OFFSHIFTX+5*RECTWIDTH;
	public static final int CHARTHEIGHT = OFFSHIFTY+RECTHEIGHT*14;
	public static final int INFOWIDTH = 520;
	private final Preference preference;

	public OutputGraphics (Course[] initDatabase, Plan[] initPlan, int initPlans, Preference initPreference){
		database = initDatabase;
		plan = initPlan;
		plans = initPlans;
		preference = initPreference;

		//Ask Java to tell me about what keys the user presses on the keyboard.
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
	}

	public void paint(Graphics g)
	{
		if (plans==0) {
			g.drawString("No compatiple schedule exists for the courses selected.", 100, 20);
			return;
		}
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
		String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday"};
		for (int day=0; day<5; day++) {
			g.drawString(days[day],100+day*RECTWIDTH,20);
		}

		for (int hour = 8; hour <= 12; hour++) {
			g.drawString(hour+" AM", 10, (hour-8)*RECTHEIGHT+OFFSHIFTY+5);
		}

		for (int hour = 1; hour <= 10; hour++) {
			g.drawString(hour+" PM", 10, (hour+12-8)*RECTHEIGHT+OFFSHIFTY+5);
		}

		for (int y = OFFSHIFTY; y<=CHARTHEIGHT; y+=RECTHEIGHT) {
			g.drawLine(50, y, CHARTWIDTH, y);
		}

		for (int x = OFFSHIFTX; x<=CHARTWIDTH; x+=RECTWIDTH) {
			g.drawLine(x, 30, x, CHARTHEIGHT);
		}

		g.setColor(Color.red);
		g.drawLine(50, (int)(OFFSHIFTY+RECTHEIGHT*(preference.startTime.hour-8+preference.startTime.minute/60.0)),
				CHARTWIDTH, (int)(OFFSHIFTY+RECTHEIGHT*(preference.startTime.hour-8+preference.startTime.minute/60.0)));
		g.drawLine(50, (int)(OFFSHIFTY+RECTHEIGHT*(preference.endTime.hour-8+preference.endTime.minute/60.0)),
				CHARTWIDTH, (int)(OFFSHIFTY+RECTHEIGHT*(preference.endTime.hour-8+preference.endTime.minute/60.0)));

		g.setColor(Color.black);
		g.drawString("Current Plan: "+(currentPlan+1)+"/"+plans
				+String.format(" Score for current plan: %.1f", plan[currentPlan].score), 10, CHARTHEIGHT+20);
		g.drawString("Use the arrow keys <- and -> to move between plans.",
				10, CHARTHEIGHT+40);

		Section section;
		int lines = 0;
		int titleLines = 0;
		int nameLines = 0;
		for (int course=0; course<plan[currentPlan].COURSES; course++) {
			section = database[course].section[plan[currentPlan].path[course]];
			drawSection(g, course, section);
			g.drawString(String.format("%d %3s %s %s", section.crn,
						database[course].subject, database[course].courseNumber,
						section.sectionNumber), CHARTWIDTH+50, lines*30+30);
			titleLines = drawLongString(g, database[course].title, lines, 200);
			for (int i=0; i<preference.Instructors; i++) {
				if(section.instructor.equals(preference.instructors[i])){
					g.setColor(Color.green);
					break;
				}
			}
			nameLines = drawLongString(g, section.instructor, lines, 350);
			lines += titleLines<nameLines ? nameLines:titleLines;
			g.setColor(Color.gray);
			g.drawLine(CHARTWIDTH+30, lines*30+10, CHARTWIDTH+INFOWIDTH, lines*30+10);
		}
		g.drawLine(CHARTWIDTH+190, 10, CHARTWIDTH+190, CHARTHEIGHT);
		g.drawLine(CHARTWIDTH+340, 10, CHARTWIDTH+340, CHARTHEIGHT);
	}//paint

	/**
	 * helper method that line-wraps a string to not exceed 16 characters per line
	 * @param g graphics variable for calling drawString
	 * @param string string to draw
	 * @param lines y coordinate in units of number of lines from top of screen
	 * @param xCoord x coordinate for calling drawString
	 * @return the new lines value for the next call to this function to not overlap text
	 */
	private int drawLongString(Graphics g, String string, int lines, int xCoord){
		int StringLines = 0;
		int lastIndex;
		while (string.length()>16){
			lastIndex = string.lastIndexOf(" ",15);
			if(lastIndex==-1){
				lastIndex=15;
			}
			g.drawString(string.substring(0,lastIndex), CHARTWIDTH+xCoord, (StringLines+lines)*30+30);
			string = string.substring(lastIndex+1);
			StringLines++;
		}
		if(string.length()!=0){
			g.drawString(string, CHARTWIDTH+xCoord, (StringLines+lines)*30+30);
			StringLines++;
		}
		return StringLines;
	}

	/**
	 * given a section variable, draw its class time on the chart
	 * @param g graphics variable
	 * @param course course variable for the name of the course
	 * @param section the section variable to draw
	 */
	private void drawSection(Graphics g, int course, Section section){
		Time startTime, endTime;
		int rectX, rectY, rectHeight;
		for (int day =0; day<5; day++) {
			if(section.schedule[day][0].hour!=25){
				startTime = section.schedule[day][0];
				endTime = section.schedule[day][1];
				rectX = OFFSHIFTX+day*RECTWIDTH;
				rectY = (int)(OFFSHIFTY+RECTHEIGHT*(startTime.hour-8+startTime.minute/60.0));
				rectHeight = (int)(RECTHEIGHT*(endTime.hour-startTime.hour+(endTime.minute-startTime.minute)/60.0));
				g.setColor(Color.yellow);
				g.fillRect(rectX, rectY, RECTWIDTH, rectHeight);
				g.setColor(Color.black);
				g.drawRect(rectX, rectY, RECTWIDTH, rectHeight);
				g.drawString(String.format("%d:%02d", startTime.hour, startTime.minute), rectX+10, rectY+14);
				g.drawString(database[course].subject+" "+database[course].courseNumber, rectX+10, rectY+rectHeight/2+6);
				g.drawString(String.format("%d:%02d", endTime.hour, endTime.minute), rectX+10, rectY+rectHeight-2);
			}
		}
	}

	/**
	 * dispatchKeyEvent
	 * turn to the next or previous page when arrow keys are pressed
	 */
	public boolean dispatchKeyEvent(KeyEvent e) {
		//Ignore KEY_RELEASED events (we only care when the key is pressed)
		String params = e.paramString();
		if (params.contains("KEY_RELEASED"))
		{
			return false;
		}

		//IF the keycode is an arrow key then handle it, otherwise ignore it.
		int code = e.getKeyCode();
		switch(code)
		{
			case KeyEvent.VK_LEFT:
				if(currentPlan>0){
					currentPlan--;
				}
				break;
			case KeyEvent.VK_RIGHT:
				if(currentPlan+1<plans){
					currentPlan++;
				}
				break;
			default:
				return false;         // this is a key I don't handle
		}

		//Update the screen to show the avatar has moved
		repaint();

		return true;
	}//dispatchKeyEvent
}
