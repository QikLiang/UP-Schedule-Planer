package graphics;

import com.google.gson.Gson;
import core.Schedule_Planer;
import data.Course;
import data.Plan;
import data.Preference;
import data.Schedule;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Set;

/**
 * Stores all data used by graphics.OutputGraphics into a file
 * and recreate the graphics.OutputGraphics when supplied a file.
 * The file is in JSON format by using the GSON library
 * to covert between JSON and Java objects.
 */
public class OutputStorage {
	public Course[] database;
	public Plan[] plan;
	public int plans;
	public Preference preference;
	public boolean[] subjectSelection;
	public Set<Course> courSelection;
	public Set<Course> electiveSelection;

	public OutputStorage(Schedule_Planer main){
		this.database = main.database;
		this.plan = main.plan;
		this.plans = main.plans;
		this.preference = main.preference;
		this.subjectSelection = main.subjectSelection;
		this.courSelection = main.courSelection;
		this.electiveSelection = main.electiveSelection;
	}

	/**
	 * open file chooser for user to pick file location, then save
	 * this object in that file
	 * @param anchor anchor for file chooser window
	 */
	public void store(Component anchor){
		JFileChooser fc = new JFileChooser();
		int val = fc.showSaveDialog(anchor);
		if(val == JFileChooser.APPROVE_OPTION){
			PrintWriter writer;
			try {//open file
				writer = new PrintWriter(fc.getSelectedFile());
			} catch (IOException e) {
				//stop if fail to open file
				return;
			}
			Gson gson = new Gson();
			writer.print(gson.toJson(this));
			writer.close();
		}
	}

	/**
	 * Use file chooser to read an graphics.OutputStorage object from file.
	 * Modifies Schedule_Planer object passed in to contain the information stored.
	 * @param anchor anchor for file chooser
	 * @return object, or null if error
	 */
	public static OutputStorage getFromFile(Component anchor, Schedule_Planer main){
		JFileChooser fc = new JFileChooser();
		int val = fc.showOpenDialog(anchor);
		if (val == JFileChooser.APPROVE_OPTION){
			Scanner input;
			try {//open file
				input = new Scanner(fc.getSelectedFile());
			} catch (IOException e) {
				//stop if fail to open file
				return null;
			}
			try{
				return new Gson().fromJson(
						input.nextLine(), OutputStorage.class);
			}catch (Exception e){
				return null;
			} finally {
				input.close();
			}
		}
		return null;
	}

}
