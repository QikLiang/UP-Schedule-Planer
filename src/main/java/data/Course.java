package data;

import core.Schedule_Planer;
import data.Section;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 */
public class Course
{
	public String subject;
	public String courseNumber;
	public int credit = 0;
	public String title;
	public ArrayList<Section> section = new ArrayList<>();

	public void addElectiveSection(){
		section.add(new ElectiveSection());
	}

	public String toString(){
		return subject + courseNumber;
	}
}
