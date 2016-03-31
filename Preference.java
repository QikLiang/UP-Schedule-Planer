
/**
 * Write a description of class Preference here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Preference
{
	int start;
	Time startTime = new Time();
	int end;
	Time endTime = new Time();
	int noon;
	Time noonBegin = new Time();
	Time noonEnd = new Time();
	int duration;
	int instructor;//how much user values having specific instrctor
	String[] instructors = new String[Schedule_Planer.COURSES];//list of preferred instructors
	int Instructors=0;//number of instructors in the instructors array
	int clustering=0;
}
