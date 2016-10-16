
/**
 * This is a collection of Schedule objects that combine to create a possible semester plan. It will eventually be
 * passed to the OutputGraphics class to be used to generate the final out put to the user.
 * 
 * @author Qi Liang
 * @version 
 */
public class Plan
{
	int[] path;
	float score = 0;
	private int start = 0;//these few variables are data for viewing the score decomposition using debugger
	private int end = 0;//they play no role in the functioning of the program
	private int noon = 0;
	private int instructor = 0;
	private double dispersion=0;
	final int COURSES;
	int Courses[] = {0,0,0,0,0};//number of courses a day, start out with no classes each day until schedule is loaded
	Schedule[][] schedule;

	public Plan(final int courses, final int[] initPath){
		path = initPath.clone();
		COURSES = courses;
		schedule = new Schedule[5][COURSES];//five days a week and COURSES number of courses a day
		for (int day=0; day<5; day++) {
			for (int course = 0; course<COURSES; course++) {
				schedule[day][course] = new Schedule();
			}
		}
	}

	void evaluateScore(final Course database[], final Preference preference){
		Time temp1;
		Time temp2;

		loadSchedule(database);

		sortSchedule(database);

		for (int day = 0; day < 5; day++){
			//see does classes contradict
			for (int course = 0; course < COURSES - 1; course++){
			    if (schedule[day][course + 1].startTime.isEarlierthan(
					schedule[day][course].endTime)){//if one class starts before another ends
					return;
				}
			}
		}
		//set score above zero for schedules that have no conflict just in case
		//user set all other preference variables to zero
		score+=0.001;

		for (int day = 0; day < 5; day++){
			//check when does each day start
			if (!schedule[day][0].startTime.isEarlierthan(
				preference.startTime)){
				score += preference.start*0.2;
				start++;
			}//*/

			//check when does each day end
			if (Courses[day]>0){
				if (schedule[day][Courses[day] - 1].endTime.isEarlierthan(
					preference.endTime)){
					score += preference.end *0.2;
					end++;
				}
			}//*/

			//check for noon break
			for (int course = 0; course < Courses[day]; course++){
				temp1 = Time.laterTime(schedule[day][course].endTime, preference.noonBegin);
				temp2 = Time.earlierTime(schedule[day][course + 1].startTime, preference.noonEnd);
				if (temp2.hour*60+temp2.minute-(temp1.hour*60+temp2.minute)>preference.duration){
					score += preference.noon*0.2;
					noon++;
					break;
				}
			}//*/
			
		}

		for (int course = 0; course < COURSES; course++){
			for (int i = 0; i < preference.Instructors; i++){
				if (database[course].section[path[course]].instructor.equals(preference.instructors[i])){
					score += preference.instructor;
					instructor++;
				}
			}
		}

		//use coefficient of variation as a estimation of clustering of classes
		int[] classTime = {0, 0, 0, 0, 0};//how much class time each day in minutes
		for (int day =0; day <5; day++) {
			for (int course=0; course<COURSES; course++) {
				temp1 = schedule[day][course].startTime;
				temp2 = schedule[day][course].endTime;
				classTime[day]+=(temp2.hour-temp1.hour)*60+temp2.minute-temp1.minute;
			}
		}
		double mean = 0;
		for (int day=0; day<5; day++) {
			mean+=classTime[day];
		}
		mean/=5;
		double cov = 0;//coefficient of variation
		for (int day=0; day<5; day++) {
			cov+=(classTime[day]-mean)*(classTime[day]-mean);
		}
		cov=Math.sqrt(cov)/mean;
		dispersion=cov;
		score+=preference.clustering*cov;
	}

	void loadSchedule(final Course database[]){
		//load times into schedule
		for (int course = 0; course < COURSES; course++){//for every course
			for (int day = 0; day < 5; day++){//for each day of the week
				if (database[course].section[path[course]].schedule[day][0].hour != 25){ //if the course has class that day
				    schedule[day][course].startTime = database[course].section[path[course]].schedule[day][0];//set start time
					schedule[day][course].endTime = database[course].section[path[course]].schedule[day][1];//set end time
					schedule[day][course].course = course;//what course it is
					Courses[day]++;
				}
			}
		}
	}

	void sortSchedule(final Course database[]){
		//a bubble sort classes based on start time, too lazy to change it
		Schedule temp;             // holding variable
		for (int day = 0; day < 5; day++){
			for (int i = 0; i < COURSES; i++){
				for (int j = 0; j < COURSES - 1 - i; j++){
					if (!schedule[day][j].startTime.isEarlierthan(
						schedule[day][j + 1].startTime)){
						temp = schedule[day][j];
						schedule[day][j] = schedule[day][j + 1];
						schedule[day][j + 1] = temp;
					}
				}
			}
		}
	}
}
