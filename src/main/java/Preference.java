import java.io.Serializable;
import java.util.ArrayList;

/**
 * Write a description of class Preference here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Preference implements Serializable
{
		int minCred;
		int maxCred;
		int start;
		Time startTime = new Time();
		int end;
		Time endTime = new Time();
		int noon;
		Time noonBegin = new Time();
		Time noonEnd = new Time();
		int duration;
		int instructor;//how much user values having specific instructor
		String[] instructors = new String[Schedule_Planer.COURSES];//list of preferred instructors
		ArrayList<String> instructorList = new ArrayList<>(Schedule_Planer.COURSES);
		int Instructors=0;//number of instructors in the instructors array
		int clustering=0;
		int externalCommitments;
		ArrayList<Section> events = new ArrayList<>();//user's external commitments

}
