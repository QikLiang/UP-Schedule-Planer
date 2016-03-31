
/**
 * Write a description of class Course here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Course
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
