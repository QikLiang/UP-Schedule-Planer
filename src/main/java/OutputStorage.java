import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Stores all data used by OutputGraphics into a file
 * and recreate the OutputGraphics when supplied a file.
 * The file is in JSON format by using the GSON library
 * to covert between JSON and Java objects.
 */
public class OutputStorage {
	Course[] database;
	Plan[] plan;
	int plans;
	Preference preference;

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
			try{//write to file
				Gson gson = new Gson();
				writer.print(gson.toJson(this));
			}catch (Exception e){ }
			writer.close();
		}
	}

	/**
	 * use file chooser to read an OutputStorage object from file
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
