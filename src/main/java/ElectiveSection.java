/**
 * If a course is optional, it will contain an
 * ElectiveSection as one of its sections. Having
 * the ElectiveSection in a Plan means the Course
 * will not be taken.
 */
public class ElectiveSection extends Section {
	public ElectiveSection(){
		super();
		for(int day=0; day<5; day++){
			schedule[day][0].hour = 25;
			schedule[day][1].hour = 25;
		}
	}
}
