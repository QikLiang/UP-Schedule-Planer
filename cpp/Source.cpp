#include <fstream>
#include <iostream>
#include <String>
using namespace std;




void formatDatabase(ifstream);

//loads the database and return how many courses are in the database
int loadDatabase(course[], Preference &);

void loadInstructors(course[], Preference &, int);

time laterTime(time, time);
time earlierTime(time, time);

//give score for a given plan
float evaluateScore(course const database[], Plan &plan, int const courses, Preference);

void sortPlans(Plan[]);

//scan for viable schedules and store them
int createPlans(course const[], Plan[], int, Preference);

void loadSchedule(course const database[], Plan plan,int courses, Schedule schedule[5][COURSES], int[]);
void sortSchedule(course const database[], Plan plan, int courses, Schedule schedule[5][COURSES]);

//show schedule
void showSchedule(course const database[], Plan plan, int const courses);

int main(){
	course database[COURSES];
	Plan plan[PLANS];
	int courses = 0;
	int plans = 0;
	Preference preference;

	cout << "Schedule planer ver 1.0 for University of Portland, by Qi Liang"<<endl;

	courses = loadDatabase(database, preference);
	cout << "finish loading database\n\n\n";
//	cout << database[0].title[3000];

	loadInstructors(database, preference, courses);

	plans = createPlans(database, plan, courses, preference);
	
	//cout << plans<<endl;

	getchar();
	cout << endl << endl;
	for(int j=0; j<plans; j++){
		showSchedule(database, plan[j], courses);
		getchar();
	}
	
	getchar();
	getchar();
	return 0;
}

int loadDatabase(course database[], Preference &preference){
	ifstream input;
	ofstream write;
	String temp;
	int tempInt;
	float credit;
	bool format=true;
	char Temp;//dummy variables
	bool newCourse=true;
	course thisCourse;
	int courseIndex = 0;
	String classdays;
	time startTime;
	time endTime;
	int courses=0; //amount of courses in the database

	input.open(DATABASE);
	cout << ',';
	if (input.is_open()){
		cout << "Database seems to already exist, do you want to use it?\n1. Yes  2. No\n";
		cin >> tempInt;
		if (tempInt==1){
			format = false;
		}
	}
	if (format){
		write.open(DATABASE);
		write << "Please rate how important is each factor below\n"
			<< "have my day start after certian time: \n"
			<< "\tthat time being HR:MN AM \n(replace the cap letters with what you want, AM/PM is cap sensitive)\n"
			<< "have my day end before certain time: \n"
			<< "\tthat time being HR:MN PM\n"
			<< "have a break around noon: \n"
			<< "\tthe break must be between HR:MN PM and HR:MN PM for a duration of : MIN minutes\n"
			<< "havng certain professors (list will be available for selection after the database is loaded): \n\n"
			<< "copy information about the courses you want to take from selfserve (the whole row for each section) "
			<< "and paste it below. Press enter after each course to make sure the next section is on a new line."
			<<"Save the document and close it. Example:\n"
			<< "SR	40299	EXP	203	A	1	3.000	Introduction to Computer Science	MWF	11:25 am-12:20 pm	30	2	28	0	0	0	0	0	0	James Michael Schmidt (P)	01/11-04/28	SHILEY 301\n\n"
			<<"paste below:\n";
		write.close();
		system(DATABASE.c_str());
		input.close();
		input.open(DATABASE);
	}

	//read preferences
	getline(input, temp, ':');
	input >> preference.start;
	getline(input, temp, 'g');
	input >> preference.startTime.hour;
	input.get(Temp);
	input >> preference.startTime.minute>>temp;
	if (temp == "PM"){
		preference.startTime.hour = preference.startTime.hour % 12 + 12;
	}
	getline(input, temp, ':');
	input >> preference.end;
	getline(input, temp, 'g');
	input >> preference.endTime.hour;
	input.get(Temp);
	input >> preference.endTime.minute >> temp;
	if (temp == "PM"){
		preference.endTime.hour = preference.endTime.hour % 12 + 12;
	}
	getline(input, temp, ':');
	input >> preference.noon;
	getline(input, temp, 'n');
	input >> preference.noonBegin.hour;
	input.get(Temp);
	input >> preference.noonBegin.minute >> temp;
	if (temp == "PM"){
		preference.noonBegin.hour = preference.noonBegin.hour % 12 + 12;
	}
	input >> temp;
	input >> preference.noonEnd.hour;
	input.get(Temp);
	input >> preference.noonEnd.minute >> temp;
	if (temp == "PM"){
		preference.noonEnd.hour = preference.noonEnd.hour % 12 + 12;
	}
	getline(input, temp, ':');
	input >> preference.duration;
	getline(input, temp, ':');
	input >> preference.instructor;
	getline(input, temp, ':');
	getline(input, temp, 'w');
	getline(input, temp, ':');
	//Temp = temp[1000];

	//input couses
	while (!input.eof() && courses < COURSES){ //keep inputing until maxed out or finished
		getline(input, temp, '\t'); //check is there the SR
		if (temp != " "){ //if the first thing is SR, it means it's a new section
			//cout << temp;
			input >> thisCourse.section[0].crn >> thisCourse.subject
				>> thisCourse.courseNumber >> thisCourse.section[0].sectionNumber//input course info
				>> temp >> credit;
			input.get(Temp);
			getline(input, thisCourse.title, '\t');
			input >> classdays;
			
			//ignore classes with undetermined time
			if (classdays == "TBA"){
				getline(input, temp, '\n');
				classdays = "";
				continue;
				
			}

			//input class starting time
			input >> startTime.hour;
			input.get(Temp);
			input >> startTime.minute;
			getline(input, temp, '-');
			if (temp == " pm"){//convert time into military format
				startTime.hour = startTime.hour%12+ 12;
			}

			//input class ending time
			input >> endTime.hour;
			input.get(Temp);
			input >> endTime.minute >> temp;
			if (temp == "pm"){//convert time into military format
				endTime.hour = endTime.hour%12+ 12;
			}

			//get rid of useless info
			for (int i = 0; i < 9; i++){
				input >> tempInt;
			}
		
			input.get(Temp);//remove extra \t
				
			//finish inputing info
			getline(input, thisCourse.section[0].instructor, '\t');
			getline(input, temp, '\t'); //don't care about date
			getline(input, thisCourse.section[0].location, '\n');
			
			//match course with database
			newCourse = true;
			for (int i = courses - 1; i >= 0; i--){//go through all already existing courses
				if (thisCourse.subject == database[i].subject && thisCourse.courseNumber == database[i].courseNumber){//if the course already exist
					courseIndex = i;
					newCourse = false;
				}
			}
			if (newCourse){//if the course is not in database yet
				courseIndex = courses;
				database[courseIndex].subject = thisCourse.subject;
				database[courseIndex].courseNumber = thisCourse.courseNumber;
				database[courseIndex].credit = int(credit);
				database[courseIndex].title = thisCourse.title;

				courses++; 
			}

			if (database[courseIndex].sections >= SECTIONS){
				cout << "Error: the number of sections in course " << database[courseIndex].subject << ' ' << database[courseIndex].courseNumber
					<< " is larger than the maximum capacity of this program of " << SECTIONS<<' '<<thisCourse.section[0].sectionNumber; 
				getchar();
			}
			
			//transfer info into database
			database[courseIndex].section[database[courseIndex].sections].crn = thisCourse.section[0].crn;
			database[courseIndex].section[database[courseIndex].sections].sectionNumber = thisCourse.section[0].sectionNumber;
			
			//inputing the times based on day of the week
			for (int i = 0; i < classdays.length(); i++){
				switch (classdays[i]){
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
				database[courseIndex].section[database[courseIndex].sections].schedule[tempInt][0] = startTime;
				database[courseIndex].section[database[courseIndex].sections].schedule[tempInt][1] = endTime;
				database[courseIndex].section[database[courseIndex].sections].instructor = thisCourse.section[0].instructor;
				database[courseIndex].section[database[courseIndex].sections].location = thisCourse.section[0].location;
			}
			database[courseIndex].sections++;// cout << 'e';
		}
		else{//if there's no SR, it's probably continuation from previous line
			input >> classdays;
			//input class starting time
			input >> startTime.hour;
			input.get(Temp);
			input >> startTime.minute;
			getline(input, temp, '-');
			
			if (temp == " pm"){//convert time into military format
				startTime.hour = startTime.hour%12+ 12;
			}

			//input class ending time
			input >> endTime.hour;
			input.get(Temp);
			input >> endTime.minute >> temp;
			if (temp == "pm"){//convert time into military format
				endTime.hour = endTime.hour%12+ 12;
			}

			getline(input, temp, '\n');//get rid of remainning stuff

			//inputing the times based on day of the week
			for (int i = 0; i < classdays.length(); i++){
				switch (classdays[i]){
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
				database[courseIndex].section[database[courseIndex].sections - 1].schedule[tempInt][0] = startTime;
				database[courseIndex].section[database[courseIndex].sections - 1].schedule[tempInt][1] = endTime;
				//cout << 'd';// << database[0].title[3000];
			}
		}
	}//cout << database[0].title[3000];
	input.close();
	return courses;
}

void loadInstructors(course database[], Preference &preference, int courses){
	String instructors[COURSES*SECTIONS];
	int Instructors = 0;
	String instructor;
	bool newInstructor;
	int input=1;
	
	for (int course = 0; course < courses; course++){
		for (int section = 0; section < database[course].sections; section++){
			instructor = database[course].section[section].instructor;
			newInstructor = true;
			for (int i = 0; i < Instructors; i++){
				if (instructors[i] == instructor){
					newInstructor = false;
				}
			}
			if (newInstructor){
				//cout << 's';
				instructors[Instructors] = instructor;
				Instructors++;
			}
		}
	}

	for (int i = 0; i < Instructors; i++){
		cout << i+1 << ". " << instructors[i] << endl;
	}//cout << database[0].title[3000];
	cout << "enter the number left of the prfessor(s) you want to take and separate them by a space, enter 0 to finish: ";

	cin >> input;
	while (input != 0){
		preference.instructors[preference.Instructors] = instructors[input - 1];
		preference.Instructors++;
		cin >> input;
	}
	return;
}

void sortPlans(Plan plan[]){
	Plan temp;             // holding variable
	for (int i = 0; i < PLANS; i++){
		for (int j = 0; j < PLANS - 1 - i; j++){
			if (plan[j].score < plan[j+1].score){
				temp = plan[j];
				plan[j] = plan[j + 1];
				plan[j + 1] = temp;
			}
		}
	}
	return;
}

int createPlans(course const database[], Plan plan[], int const courses, Preference preference){
	int plans = 0;
	Plan thisPlan;
	//initialize thisPlan
	for (int j = 0; j < COURSES; j++){
		thisPlan.path[j] = 0;
	}

	while (thisPlan.path[0] < database[0].sections){// && plans < PLANS){
		if (plans >= PLANS - 2){
			sortPlans(plan);
			plans = plans / 2;
		}//*/

		thisPlan.score = evaluateScore(database, thisPlan, courses, preference);
		//cout << thisPlan.score << endl;
		//getchar();
		if (thisPlan.score != 0){
			plan[plans] = thisPlan;
			plans++;
		}

		//increment to next path
		thisPlan.path[courses-1]++;
		for (int i = courses - 1; i > 0; i--){
			if (thisPlan.path[i] >= database[i].sections){
				thisPlan.path[i] = thisPlan.path[i] % database[i].sections;
				thisPlan.path[i - 1]++;
			}
		}
	}

	sortPlans(plan);
	return plans;
}

void loadSchedule(course const database[], Plan plan, int courses, Schedule schedule[5][COURSES], int Courses[]){
	//load times into schedule
	for (int course = 0; course < courses; course++){
		for (int day = 0; day < 5; day++){
			if (database[course].section[plan.path[course]].schedule[day][0].hour != 25){ //if the course has class that day
				schedule[day][course].startTime = database[course].section[plan.path[course]].schedule[day][0];//set start time
				schedule[day][course].endTime = database[course].section[plan.path[course]].schedule[day][1];//set end time
				schedule[day][course].course = course;//what course it is
				Courses[day]++;
			}
		}
	}
}

void sortSchedule(course const database[], Plan plan, int courses, Schedule schedule[5][COURSES]){
	//sort classes based on start time
	Schedule temp;             // holding variable
	for (int day = 0; day < 5; day++){
		for (int i = 0; i < COURSES; i++){
			for (int j = 0; j < COURSES - 1 - i; j++){
				if (schedule[day][j].startTime.hour + 0.01*schedule[day][j].startTime.minute >
					schedule[day][j + 1].startTime.hour + 0.01*schedule[day][j + 1].startTime.minute){
					temp = schedule[day][j];
					schedule[day][j] = schedule[day][j + 1];
					schedule[day][j + 1] = temp;
				}
			}
		}
	}
}

time laterTime(time t1, time t2){
	if (t1.hour + 0.01*t1.minute < t2.hour + 0.01*t2.minute){
		return t2;
	}
	else{
		return t1;
	}
}

time earlierTime(time t1, time t2){
	if (t1.hour + 0.01*t1.minute > t2.hour + 0.01*t2.minute){
		return t2;
	}
	else{
		return t1;
	}
};

float evaluateScore(course const database[], Plan &plan, int const courses, Preference preference){
	int Courses[5] = { 0 };
	Schedule schedule[5][COURSES];//five days a week and COURSES number of courses a day
	float score = 1;
	time temp1;
	time temp2;

	loadSchedule(database, plan, courses, schedule, Courses);

	sortSchedule(database, plan, courses, schedule);

	for (int day = 0; day < 5; day++){
		//see does classes contradict
		for (int course = 0; course < courses - 1; course++){
			if (schedule[day][course + 1].startTime.hour + 0.01*schedule[day][course + 1].startTime.minute <
				schedule[day][course].endTime.hour + 0.01*schedule[day][course].endTime.minute){//if one class starts before another ends
				return 0;
			}
		}
	}

	for (int day = 0; day < 5; day++){
		//check when does each day start
		if (schedule[day][0].startTime.hour + 0.01*schedule[day][0].startTime.minute >
			preference.startTime.hour + 0.01*preference.startTime.minute){
			score += preference.start*0.2;
			plan.start++;
		}//*/

		//check when does each day end
		if (Courses[day]>0){
			if (schedule[day][Courses[day] - 1].endTime.hour + 0.01*schedule[day][Courses[day] - 1].endTime.minute <
				preference.endTime.hour + 0.01*preference.endTime.minute){
				score += preference.end *0.2;
				plan.end++;
			}
		}//*/

		//check for noon break
		for (int course = 0; course < Courses[day]; course++){
			temp1 = laterTime(schedule[day][course].endTime, preference.noonBegin);
			temp2 = earlierTime(schedule[day][course + 1].startTime, preference.noonEnd);
			if (temp2.hour*60+temp2.minute-(temp1.hour*60+temp2.minute)>preference.duration){
				score += preference.noon*0.2;
				plan.noon++;
				break;
			}
		}//*/
		
	}

	for (int course = 0; course < courses; course++){
		for (int i = 0; i < preference.Instructors; i++){
			if (database[course].section[plan.path[course]].instructor == preference.instructors[i]){
				score += preference.instructor;
				plan.instructor++;
			}
		}
	}

	return score;
}

void showSchedule(course const database[], Plan plan, int const courses){
	Schedule schedule[5][COURSES];//five days a week and COURSES number of courses a day
	int Courses[5] = { 0 };

	loadSchedule(database, plan, courses, schedule, Courses);

	sortSchedule(database, plan, courses, schedule);

	//cout << endl<<plan.score<< ' '<<plan.start<<' '<<plan.end<<' '<<plan.noon<<' '<<plan.instructor << endl;
	//return;
	for (int day = 0; day < 5; day++){
	for (int course = 0; course < COURSES; course++){
		if (schedule[day][course].startTime.hour != 25){
			cout << schedule[day][course].startTime.hour << ':' << schedule[day][course].startTime.minute << '-'
				<< schedule[day][course].endTime.hour << ':' << schedule[day][course].endTime.minute << ' '
				<< database[schedule[day][course].course].subject << ' ' << database[schedule[day][course].course].courseNumber << '\t';
		}
	}
	cout << endl;
	}
	for (int i = 0; i < courses; i++){
		cout << database[i].section[plan.path[i]].crn << '\t' << database[i].subject << ' ' << database[i].courseNumber
			<< ' ' << database[i].section[plan.path[i]].sectionNumber << ' ' << database[i].title << endl << '\t'
			<< database[i].section[plan.path[i]]. instructor<< endl;
	}
	cout << endl << endl<<"press enter to view the next schedule";
	return;
}
