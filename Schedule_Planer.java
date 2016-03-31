import java.util.NoSuchElementException;
import java.awt.Desktop;//for opening file
import java.util.Scanner;//for file and keyboard input
import java.io.*;//for writing into file

/**
 * Main class. The main method and all big methods called my main
 * 
 * @author Qi Liang
 * @version 2016.3.5
 */
public class Schedule_Planer
{
    //universal variables
    static final int SECTIONS = 25; //maximum number of sections per course
    static final int COURSES = 9; //maximum number of courses in database
    static final int PLANS = 100;//maximum number of different schedules the program can handdle
    Scanner keyboard = new Scanner(System.in);

    public static boolean testing = true;
    final String DATABASE = testing ? "C:\\Users\\Qi\\Desktop\\database.txt" : "database.txt";//address of the databse file

    public static void main (String[] args) {
        Course[] database = new Course[COURSES];
        Plan[] plan = new Plan[PLANS];
        int courses = 0;
        int plans = 0;
        Preference preference = new Preference();
        Schedule_Planer planer = new Schedule_Planer();

        for (int i = 0; i<COURSES; i++) {
            database[i] = new Course();
        }

        System.out.println(  "Schedule planer ver 2.0 for University of Portland, by Qi Liang");

        courses = planer.loadDatabase(database, preference);
        System.out.println(  "finish loading database\n\n\n");

        planer.loadInstructors(database, preference, courses);

        plans = planer.createPlans(database, plan, courses, preference);

        OutputGraphics graphics = new OutputGraphics(database, plan, plans, preference);
        graphics.startGraphics();

        for(int j=0; j<plans; j++){
            //showSchedule(database, plan[j], courses);
        }
    }

    int loadDatabase(Course database[], Preference preference){
        File file = new File(DATABASE);
        Scanner input = null;
        PrintWriter write = null;
        String temp;
        int tempInt = 0;
        float credit;
        boolean newFile;
        char Temp;//dummy variables
        boolean newCourse=true;
        Course thisCourse = new Course();
        int courseIndex = 0;
        String classdays;
        Time startTime = new Time();
        Time endTime = new Time();
        int courses=0; //amount of courses in the database

        newFile=true;
        if (file.exists()){
            System.out.print("Database seems to already exist, do you want to use it?\n1. Yes  2. No\n");
            tempInt = keyboard.nextInt();
            keyboard.nextLine();
            if (tempInt==1){
                newFile = false;
            }
        }
        if (newFile){
            try{
                write = new PrintWriter(file);
            } catch (FileNotFoundException e) {
                System.out.println("Error: cannot open file to write.");
                System.exit(1);
            }

            write.println( "Please rate how important is each factor below\n");
            write.println( "have my day start after certian time: \n");
            write.println( "\tthat time being HR:MN AM \n(replace the cap letters with what you want, AM/PM is cap sensitive)\n");
            write.println( "have my day end before certain time: \n");
            write.println( "\tthat time being HR:MN PM\n");
            write.println( "have a break around noon: \n");
            write.println( "\tthe break must be between HR:MN PM and HR:MN PM for a duration of : MIN minutes\n");
            write.println( "havng certain professors (list will be available for selection after the database is loaded): \n\n");
            write.println("having classes clustered on certain days (easy days and hard days): ");
            write.println();
            write.println( "copy information about the courses you want to take from selfserve (the whole row for each section) ");
            write.println( "and paste it below. Press enter after each course to make sure the next section is    on a new line.");
            write.println( "Save the document and close it. Example:\n");
            write.println( "SR   40299   EXP 203 A   1   3.000   Introduction to Computer Science    MWF 11:25 am-12:20 pm   30  2   28  0   0   0   0   0   0   James Michael Schmidt (P)   01/11-04/28 SHILEY 301\n\n");
            write.println( "paste below:\n");
            write.close();
            if(!Desktop.isDesktopSupported()){
                System.out.println("Error: Please open and edit '" + DATABASE + "' manually.");
            }
            Desktop desktop = Desktop.getDesktop(); 
            try{
                desktop.open(file);
            }catch (IOException e){
                System.out.println("Error: Please open and edit '" + DATABASE + "' manually.");
            }

            System.out.println("After the text file has been saved and exited, press enter.");
            keyboard.nextLine();
        }

        try{
            input = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("Error: cannot open file to read.");
            System.exit(1);
        }

        //read preferences
        skipTilNum(input);
        preference.start = input.nextInt();
        skipTilNum(input);
        /*preference.startTime.hour= input.nextInt();
        skipTilNum(input);
        preference.startTime.minute= input.nextInt();
         */
        inputTime(input, preference.startTime);
        temp= input.next();
        if (temp.equals("PM")){
            preference.startTime.hour = preference.startTime.hour % 12 + 12;
        }
        skipTilNum(input);
        preference.end = input.nextInt();
        skipTilNum(input);
        inputTime(input, preference.endTime);
        temp= input.next();
        if (temp.equals("PM")){
            preference.endTime.hour = preference.endTime.hour % 12 + 12;
        }
        skipTilNum(input);
        preference.noon = input.nextInt();
        skipTilNum(input);
        inputTime(input, preference.noonBegin);
        temp = input.next();
        if (temp.equals("PM")){
            preference.noonBegin.hour = preference.noonBegin.hour % 12 + 12;
        }
        skipTilNum(input);
        inputTime(input, preference.noonEnd);
        temp = input.next();
        if (temp.equals("PM")){
            preference.noonEnd.hour = preference.noonEnd.hour % 12 + 12;
        }
        skipTilNum(input);
        preference.duration = input.nextInt();
        skipTilNum(input);
        preference.instructor = input.nextInt();
        skipTilNum(input);
        preference.clustering = input.nextInt();
        while (!temp.equals("paste below:")){
            temp = input.nextLine();
        }
        //Temp = temp[1000];

        //input couses
        while (input.hasNextLine() && courses < COURSES){ //keep inputing until maxed out or finished
            //clear the temp objects
            thisCourse = new Course();
            startTime = new Time();
            endTime = new Time();

            input.useDelimiter("\\t");
            temp = input.next();//check is there the SR
            if (temp != " "){ //if the first thing is SR, it means it's a new section
                //cout << temp;
                thisCourse.section[0].crn = input.nextInt(); 
                thisCourse.subject = input.next();
                thisCourse.courseNumber = input.next(); 
                thisCourse.section[0].sectionNumber = input.next();//input course info
                temp = input.next(); 
                credit = input.nextFloat();
                thisCourse.title = input.next();
                classdays = input.next();

                //ignore classes with undetermined time
                if (classdays.equals("TBA")){
                    input.nextLine();
                    classdays = "";
                    continue;

                }

                //input class starting time
                inputTime(input, startTime);
                input.useDelimiter("-");
                temp = input.next();
                if (temp.equals(" pm")){//convert time into military format
                    startTime.hour = startTime.hour%12+ 12;
                }

                //input class ending time
                input.useDelimiter("\\t");
                inputTime(input, endTime);
                temp = input.next();
                if (temp.equals(" pm")){//convert time into military format
                    endTime.hour = endTime.hour%12+ 12;
                }

                //get rid of useless info
                for (int i = 0; i < 9; i++){
                    temp = input.next();
                }

                //finish inputing info
                thisCourse.section[0].instructor = input.next();
                temp = input.next(); //don't care about date
                thisCourse.section[0].location = input.nextLine().trim();

                //match course with database
                newCourse = true;
                for (int i = courses - 1; i >= 0; i--){//go through all already existing courses
                    if (thisCourse.subject.equals(database[i].subject) && thisCourse.courseNumber.equals(database[i].courseNumber)){//if the course already exist
                        courseIndex = i;
                        newCourse = false;
                    }
                }
                if (newCourse){//if the course is not in database yet
                    courseIndex = courses;
                    database[courseIndex].subject = thisCourse.subject;
                    database[courseIndex].courseNumber = thisCourse.courseNumber;
                    database[courseIndex].credit = (int)(credit);
                    database[courseIndex].title = thisCourse.title;

                    courses++; 
                }

                if (database[courseIndex].sections >= SECTIONS){
                    System.out.print( "Error: the number of sections in course " + database[courseIndex].subject + ' ' + database[courseIndex].courseNumber
                        + " is larger than the maximum capacity of this program of " + SECTIONS+' '+thisCourse.section[0].sectionNumber); 
                    keyboard.nextLine();
                }

                //transfer info into database
                database[courseIndex].section[database[courseIndex].sections].crn = thisCourse.section[0].crn;
                database[courseIndex].section[database[courseIndex].sections].sectionNumber = thisCourse.section[0].sectionNumber;

                //inputing the times based on day of the week
                for (int i = 0; i < classdays.length(); i++){
                    switch (classdays.charAt(i)){
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
                classdays = input.next();

                //input class starting time
                startTime.hour = input.nextInt();
                skipTilNum(input);
                startTime.minute = input.nextInt();
                input.useDelimiter("-");
                temp = input.next();
                if (temp.equals(" pm")){//convert time into military format
                    startTime.hour = startTime.hour%12+ 12;
                }

                //input class ending time
                input.useDelimiter("\\t");
                endTime.hour = input.nextInt();
                skipTilNum(input);
                endTime.minute = input.nextInt();
                temp = input.next();
                if (temp.equals(" pm")){//convert time into military format
                    endTime.hour = endTime.hour%12+ 12;
                }

                input.nextLine();//get rid of remainning stuff

                //inputing the times based on day of the week
                for (int i = 0; i < classdays.length(); i++){
                    switch (classdays.charAt(i)){
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

    public static void skipTilNum(Scanner input){
        while (!input.hasNextInt()){
            try {
                input.skip("\\D");
            } catch(NoSuchElementException e) {
                return;
            }
        }
    }
    
    public static void inputTime(Scanner input, Time time){
        //store the delimiter currently being used
        String delimiter = input.delimiter().pattern();
        input.useDelimiter("\\s");
        String[] string = input.next().split(":");
        time.hour = Math.abs(Integer.parseInt(string[0]));
        //when a dash is infront of a number, it can be interpreted as a negative sign, so take the absolute value
        time.minute = Integer.parseInt(string[1]);
        input.useDelimiter(delimiter);
    }

    void loadInstructors(Course database[], Preference preference, int courses){
        String[] instructors = new String[Schedule_Planer.COURSES*Schedule_Planer.SECTIONS];
        int Instructors = 0;
        String instructor;
        boolean newInstructor;
        int input=1;
        if (preference.instructor==0){
            return;
        }
        
        for (int course = 0; course < courses; course++){
            for (int section = 0; section < database[course].sections; section++){
                instructor = database[course].section[section].instructor;
                newInstructor = true;
                for (int i = 0; i < Instructors; i++){
                    if (instructors[i].equals(instructor)){
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
            System.out.println(i+1 + ". " + instructors[i]);
        }//cout << database[0].title[3000];
        System.out.print("enter the number left of the professor(s) you want to take \n"
            + "and separate them by a space, enter 0 to finish: ");

        input = keyboard.nextInt();
        while (input != 0){
            preference.instructors[preference.Instructors] = instructors[input - 1];
            preference.Instructors++;
            input = keyboard.nextInt();
        }
        return;
    }

    int createPlans(final Course database[], Plan plan[], final int courses, Preference preference){
        int plans = 0;
        Plan thisPlan;
        int[] thisPlanPath = new int[courses];

        while (thisPlanPath[0] < database[0].sections){// && plans < PLANS){
            thisPlan = new Plan(courses, thisPlanPath);
            if (plans >= PLANS - 1){
                sortPlans(plan, plans);
                plans = plans / 2;
            }//*/

            thisPlan.evaluateScore(database, preference);
            //cout << thisPlan.score << endl;
            //getchar();
            if (thisPlan.score != 0){
                plan[plans] = thisPlan;
                plans++;
            }

            //increment to next path
            thisPlanPath[courses-1]++;
            for (int i = courses - 1; i > 0; i--){
                if (thisPlanPath[i] >= database[i].sections){
                    thisPlanPath[i] = thisPlanPath[i] % database[i].sections;
                    thisPlanPath[i - 1]++;
                }
            }
        }

        sortPlans(plan, plans);
        return plans;
    }

    void sortPlans(Plan plan[], final int plans){
        Plan temp;             // holding variable
        for (int i = 0; i < plans; i++){
            for (int j = 0; j < plans - 1 - i; j++){
                if (plan[j].score < plan[j+1].score){
                    temp = plan[j];
                    plan[j] = plan[j + 1];
                    plan[j + 1] = temp;
                }
            }
        }
        return;
    }
}
