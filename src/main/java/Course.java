import java.io.Serializable;

/**
 * 
 */
public class Course implements Serializable
{
	public String subject;
	public String courseNumber;
	public int credit = 0;
	public String title;
	public Section[] section= new Section[Schedule_Planer.SECTIONS];
	public int sections = 0; //amount of sections in this course

	public Course(){
		for (int i = 0; i<section.length; i++) {
			section[i] = new Section();
		}
	}

	public String toString(){
		return subject + courseNumber;
	}
}
