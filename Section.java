
/**
 * The info of one section in a course
 */
public class Section
{
	int crn = 0;
	String sectionNumber;
	Time[][] schedule;
	String instructor;
	String location;

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
}
