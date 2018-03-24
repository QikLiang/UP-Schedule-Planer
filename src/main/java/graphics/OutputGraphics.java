package graphics;

import core.Network;
import core.Schedule_Planer;
import data.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * this class takes care of ending graphics
 */
public class OutputGraphics extends Panel implements KeyEventDispatcher
{
	private final Schedule_Planer main;
	private final Course[] database;
	private final Preference preference;
	private Plan[] plan;
	private int currentPlan = 0;
	private int plans;

	private final JPanel menu;
	private JLabel planText;
	private JLabel scoreText;

	public static int RECTWIDTH = 100;
	public static int RECTHEIGHT = 50;
	public static int OFFSHIFTX = 75;
	public static int OFFSHIFTY = 50;
	public static int CHARTWIDTH = OFFSHIFTX+5*RECTWIDTH;
	public static int CHARTHEIGHT = OFFSHIFTY+RECTHEIGHT*14;
	public static int INFOWIDTH = 420;

	public void paint(Graphics g)
	{
		//when there's no plans
		if (plans==0) {
			g.drawString("No compatiple schedule exists for the courses selected.", 100, 20);
			return;
		}

		//draw white background
		g.setColor(Color.white);
		g.fillRect(0,0,this.getWidth(), this.getHeight());
		g.setColor(Color.black);

		//days marks above the chart
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
		String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday"};
		for (int day=0; day<5; day++) {
			g.drawString(days[day],100+day*RECTWIDTH,20);
		}

		//hours marks left of chart
		for (int hour = 8; hour <= 12; hour++) {
			g.drawString(hour+" AM", hour>9 ? 2:10, (hour-8)*RECTHEIGHT+OFFSHIFTY+5);
		}
		for (int hour = 1; hour <= 10; hour++) {
			g.drawString(hour+" PM", hour>9 ? 2:10, (hour+12-8)*RECTHEIGHT+OFFSHIFTY+5);
		}

		//grid lines for the chart
		for (int y = OFFSHIFTY; y<=CHARTHEIGHT; y+=RECTHEIGHT) {
			g.drawLine(50, y, CHARTWIDTH, y);
		}
		for (int x = OFFSHIFTX; x<=CHARTWIDTH; x+=RECTWIDTH) {
			g.drawLine(x, 30, x, CHARTHEIGHT);
		}

		//draw lines where user prefer day to start and end
		g.setColor(Color.red);
		g.drawLine(50, (int)(OFFSHIFTY+RECTHEIGHT*(preference.startTime.hour-8+preference.startTime.minute/60.0)),
				CHARTWIDTH, (int)(OFFSHIFTY+RECTHEIGHT*(preference.startTime.hour-8+preference.startTime.minute/60.0)));
		g.drawLine(50, (int)(OFFSHIFTY+RECTHEIGHT*(preference.endTime.hour-8+preference.endTime.minute/60.0)),
				CHARTWIDTH, (int)(OFFSHIFTY+RECTHEIGHT*(preference.endTime.hour-8+preference.endTime.minute/60.0)));

		//items in the chart itself
		Section section;
		int lines = 0;
		int titleLines;
		int nameLines;
		for (int course=0; course<plan[currentPlan].COURSES; course++) {
			section = database[course].section.get(plan[currentPlan].path[course]);
			//skip ElectiveSections, check sectionNumber instead of using instantof
			//saving to file could have changed data type
			if (section.sectionNumber == null){
				continue;
			}
			drawSection(g, course, section);
			g.drawString(String.format("%d %3s %s %s", section.crn,
						database[course].subject, database[course].courseNumber,
						section.sectionNumber), CHARTWIDTH+40, lines*30+30);
			titleLines = drawLongString(g, database[course].title, lines, 195);
			for (int i=0; i<preference.instructorList.size(); i++) {
				if(section.instructor.equals(preference.instructorList.get(i))){
					g.setColor(Color.green.darker());
					break;
				}
			}
			nameLines = drawLongString(g, section.instructor, lines, 350);
			lines += titleLines<nameLines ? nameLines:titleLines;
			g.setColor(Color.gray);
			g.drawLine(CHARTWIDTH+30, lines*30+10, CHARTWIDTH+INFOWIDTH, lines*30+10);
		}

		//num credits
		int xcord = this.getWidth() - 150;
		int ycord = this.getHeight() - 50;
		g.setColor(Color.white);
		g.fillRect(xcord,ycord,110, 30);
		g.setColor(Color.black);
		g.drawRect(xcord,ycord,110, 30);
		g.drawString("Credits: " + plan[currentPlan].credits, xcord+5, ycord+20);

		//external commitments
		for(int i=0; i<preference.events.size(); i++){
			drawSection(g, -1, preference.events.get(i));
		}

		g.drawLine(CHARTWIDTH+190, 10, CHARTWIDTH+190, CHARTHEIGHT);
		g.drawLine(CHARTWIDTH+340, 10, CHARTWIDTH+340, CHARTHEIGHT);
	}//paint

	/* PAINT HELPER METHODS */

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
	 * @param course index in course array. -1 for a preference event instead of course section
	 * @param section the section variable to draw
	 */
	private void drawSection(Graphics g, int course, Section section){
		Time startTime, endTime;
		int rectX, rectY, rectHeight;
		for (int day =0; day<5; day++) {
			if(section.schedule[day][0].hour!=25){
				//the box
				startTime = section.schedule[day][0];
				endTime = section.schedule[day][1];
				rectX = OFFSHIFTX+day*RECTWIDTH;
				rectY = (int)(OFFSHIFTY+RECTHEIGHT*(startTime.hour-8+startTime.minute/60.0));
				rectHeight = (int)(RECTHEIGHT*(endTime.hour-startTime.hour+(endTime.minute-startTime.minute)/60.0));
				if(course>=0){
					g.setColor(Color.yellow);
				}else{
					g.setColor( new Color(0,0,255,80));
				}
				g.fillRect(rectX, rectY, RECTWIDTH, rectHeight);
				g.setColor(Color.black);
				g.drawRect(rectX, rectY, RECTWIDTH, rectHeight);

				//info inside the box
				if(course>=0){
					//start time
					String words = String.format("%d:%02d", startTime.hour, startTime.minute);
					int stringWidth = g.getFontMetrics().stringWidth(words);
					g.drawString(words, rectX+(RECTWIDTH-stringWidth)/2, rectY+14);
					//course name
					words = database[course].subject+" "+database[course].courseNumber;
					stringWidth = g.getFontMetrics().stringWidth(words);
					g.drawString(words, rectX+(RECTWIDTH-stringWidth)/2, rectY+rectHeight/2+6);
					//end time
					words = String.format("%d:%02d", endTime.hour, endTime.minute);
					stringWidth = g.getFontMetrics().stringWidth(words);
					g.drawString(words, rectX+(RECTWIDTH-stringWidth)/2, rectY+rectHeight-2);
				} else{
					g.setColor(Color.WHITE);
					String words = section.sectionNumber;
					int stringWidth = g.getFontMetrics().stringWidth(words);
					while(stringWidth>RECTWIDTH){
						words = words.substring(0, words.length()-3) + "..";
						stringWidth = g.getFontMetrics().stringWidth(words);
					}
					g.drawString(words, rectX+(RECTWIDTH-stringWidth)/2, rectY+rectHeight/2+6);
				}
			}
		}
		planText.setText(" "+(currentPlan+1)+"/"+plans+" ");
		scoreText.setText(String.format(" Score: %.1f", plan[currentPlan].score));
		menu.repaint();
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

		repaint();

		if(Schedule_Planer.testing){
			System.out.printf("plan: %d,%s\n", currentPlan, plan[currentPlan].toString());
		}

		return true;
	}//dispatchKeyEvent

	/**
	 * makes a new OutputGraphics object and wraps it in another JPanel along with a menu at the bottom
	 */
	public static JPanel createGraphicsJPanel(Schedule_Planer main){
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JPanel menu = new JPanel();
		//menu.setMaximumSize(new Dimension(800, 20));
		OutputGraphics graphics = new OutputGraphics(main, menu, panel);
		panel.add(graphics);
		panel.add(menu, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * constructor only called by createGraphicsJPanel
	 */
	private OutputGraphics (Schedule_Planer main, JPanel menu, JPanel parent){
		this.main = main;
		database = main.database;
		plan = main.plan;
		plans = main.plans;
		preference = main.preference;

		//Ask Java to tell me about what keys the user presses on the keyboard.
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		this.menu = menu;
		menu.setLayout( new BoxLayout(menu, BoxLayout.X_AXIS));

		//create buttons for switching plans
		JButton left = new JButton("<-");
		left.addActionListener(arg0 -> {
			if(currentPlan>0){
				currentPlan--;
			}
			repaint();
		});
		JButton right= new JButton("->");
		right.addActionListener(arg0 -> {
			if(currentPlan+1<plans){
				currentPlan++;
			}
			repaint();
		});

		JButton back = new JButton("Back");
		back.addActionListener(actionEvent -> {
					((CardLayout) parent.getParent().getLayout()).show(parent.getParent(), "preference");
					SwingUtilities.getWindowAncestor(parent).setSize(430, 450);
				}
		);

		JButton update = new JButton("Remove plans with sections full capacity");
		//update.setPreferredSize(new Dimension(310, 30));
		update.addActionListener(e -> {
			try {
				removePlansWithFullSections();
			} catch (Network.NetworkErrorException e1) {
				JOptionPane.showMessageDialog(this, "Error: internet connection failed");
			}
		});

		JButton save = new JButton("Save Schedules");
		save.addActionListener(actionEvent ->
				new OutputStorage(main).store(this));

		JButton print = new JButton("Save as images");
		print.addActionListener(actionEvent -> saveScheduleImages());

		//add elements to menu
		planText = new JLabel();
		scoreText = new JLabel();
		menu.add(Box.createHorizontalGlue());
		menu.add(left);
		menu.add(planText);
		menu.add(right);
		menu.add(scoreText);
		menu.add(Box.createHorizontalGlue());
		menu.add(back);
		menu.add(update);
		menu.add(save);
		menu.add(print);
		menu.add(Box.createHorizontalGlue());
	}

	private void removePlansWithFullSections() throws Network.NetworkErrorException {
		ArrayList<Plan> newPlans = new ArrayList<>();
		HashMap<Section, Boolean> sectionsFull = new HashMap<>();
		Section section;
		for(int plan=0; plan<plans; plan++){
			boolean planNotFull = true;
			for(int course=0; course<this.plan[plan].COURSES; course++){
				section = database[course].section.get(this.plan[plan].path[course]);
				if (section instanceof ElectiveSection){
					continue;
				}
				if(!sectionsFull.containsKey(section)){
					sectionsFull.put(section, Network.sectionFull(section));
				}
				planNotFull = planNotFull && !sectionsFull.get(section);
			}
			if(planNotFull){
				newPlans.add(this.plan[plan]);
			}
		}
		plan = newPlans.toArray(new Plan[0]);
		plans = plan.length;
		currentPlan = 0;
		repaint();
	}

	private void saveScheduleImages() {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Select where to save schedules");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//show dialog, return if file not chosen
		if(fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION){
			return;
		}

		File dir = new File(fc.getSelectedFile(), "Schedules");
		dir.mkdir();

		//save currentPlan before iterating through plans
		int displayPlan = currentPlan;

		//iterate through plans
		File file;
		int width = this.getWidth();
		int height = this.getHeight();
		BufferedImage image;
		for(int i=0; i<plans; i++){
			//create image of plan
			currentPlan = i;
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			this.paint(image.createGraphics());
			//write image to file
			file = new File(dir, "schedule" + i + ".jpg");
			try {
				ImageIO.write(image, "jpg", file);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Error: failed to create file.");
			}
		}
		//go back to previous currentPlan
		currentPlan = displayPlan;

		//open folder in desktop
		try {
			Desktop.getDesktop().open(dir);
		} catch (IOException e) {
			//don't care
		}
	}
}
