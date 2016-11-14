import java.io.Serializable;

/**
 * 
 */
public class Course implements Serializable
{
	String subject;
	String courseNumber;
	int credit = 0;
	String title;
	Section[] section= new Section[Schedule_Planer.SECTIONS];
	int sections = 0; //amount of sections in this course

	public Course(){
		for (int i = 0; i<section.length; i++) {
			section[i] = new Section();
		}
	}
}
