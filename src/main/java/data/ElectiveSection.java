package data;

/**
 * If a course is optional, it will contain an
 * data.ElectiveSection as one of its sections. Having
 * the data.ElectiveSection in a data.Plan means the data.Course
 * will not be taken.
 */
public class ElectiveSection extends Section {
	public ElectiveSection(){
		super();
		for(int day=0; day<5; day++){
			schedule[day][0].hour = 25;
			schedule[day][1].hour = 25;
		}
		instructor = "";
	}
}
