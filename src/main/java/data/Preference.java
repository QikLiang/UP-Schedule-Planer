package data;

import core.Schedule_Planer;

import java.util.ArrayList;

/**
 * Write a description of class data.Preference here.
 */
public class Preference
{
		public int minCred = 15;
		public int maxCred = 18;
		public int start;
		public Time startTime = new Time();
		public int end;
		public Time endTime = new Time();
		public int noon;
		public Time noonBegin = new Time();
		public Time noonEnd = new Time();
		public int duration;
		public int instructor;//how much user values having specific instructor
		public ArrayList<String> instructorList = new ArrayList<>();
		public int clustering=0;
		public int externalCommitments;
		public ArrayList<Section> events = new ArrayList<>();//user's external commitments

}
