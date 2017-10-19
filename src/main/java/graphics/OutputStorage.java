package graphics;

import com.google.gson.Gson;
import data.Course;
import data.Plan;
import data.Preference;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Stores all data used by graphics.OutputGraphics into a file
 * and recreate the graphics.OutputGraphics when supplied a file.
 * The file is in JSON format by using the GSON library
 * to covert between JSON and Java objects.
 */
public class OutputStorage {
	private Course[] database;
	private Plan[] plan;
	private int plans;
	private Preference preference;

	public OutputStorage(Course[] database, Plan[] plan, int plans, Preference preference){
		this.database = database;
		this.plan = plan;
		this.plans = plans;
		this.preference = preference;
	}

	public JPanel toGraphics(){
		return OutputGraphics.createGraphicsJPanel(database, plan, plans, preference);
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
	 * use file chooser to read an graphics.OutputStorage object from file
	 * @param anchor anchor for file chooser
	 * @return object, or null if error
	 */
	public static OutputStorage getFromFile(Component anchor){
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
