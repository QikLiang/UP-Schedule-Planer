package data;

import data.Time;

/**
 * The info of one section in a course
 */
public class Section
{
	public int crn = 0;
	public String sectionNumber;
	public Time[][] schedule;
	public String instructor;
	public String location;

	public Section(){
		final int DAYS = 5;
		final int STARTANDENDTIME = 2;
		schedule = new Time[DAYS][STARTANDENDTIME];

		for (int day = 0; day<DAYS; day++) {
			for (int time=0; time<STARTANDENDTIME; time++) {
				schedule[day][time]=new Time();
			}
		}
	}

	public void setSchedule(String classdays, String time){
		if(classdays == null || !classdays.matches("[MTWRF]+") ||
			time == null ){
			return;
		}

		// inputing the times based on day of the week
		Time startTime = new Time();
		Time endTime = new Time();
		Time.parseTime(time,startTime,endTime);
		int tempInt = 0;
		for (int i = 0; i < classdays.length(); i++) {
			switch (classdays.charAt(i)) {
				case 'M':
					tempInt = 0;
					break;
				case 'T':
					tempInt = 1;
					break;
				case 'W':
					tempInt = 2;
					break;
				case 'R':
					tempInt = 3;
					break;
				case 'F':
					tempInt = 4;
			}
			schedule[tempInt][0] = startTime;
			schedule[tempInt][1] = endTime;
		}
	}
}
