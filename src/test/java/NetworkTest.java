import core.Network;
import data.Course;
import data.Section;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

public class NetworkTest {
	@Test
	void getTerms() throws Network.NetworkErrorException {
		String[][] terms = Network.getTerms();

		Assert.assertTrue(terms != null && terms.length > 0);

		Pattern term = Pattern.compile("(Fall|Summer|Spring) \\d{4} \\(View only\\)");
		Pattern value = Pattern.compile("\\d{6}");

		Assert.assertEquals(terms[0][0], "None");
		for (int i = 1; i < terms.length; i++) {
			Assert.assertTrue(term.matcher(terms[i][0]).matches());
			Assert.assertTrue(value.matcher(terms[i][1]).matches());
		}
	}

	@Test
	void getSubjects() throws Network.NetworkErrorException {
		String[][] terms = Network.getTerms();
		//the first item is "None", so select the select item from terms
		String[][] subjects = Network.getSubjects(terms[1][1]);

		Assert.assertTrue("No subjects found", subjects != null && subjects.length > 0);

		//pattern matches 'Word & More Optional Words'
		Pattern subject = Pattern.compile("[A-Z][a-z]*(\\s(&|[A-Za-z][a-z]*))*");
		Pattern value = Pattern.compile("[A-Z]{1,4}");

		for (int i = 0; i < subjects.length; i++) {
			Assert.assertTrue("Subject name not match expected: " + subjects[i][0],
					subject.matcher(subjects[i][0]).matches());
			Assert.assertTrue("Subject value not match expected: " + subjects[i][1],
					value.matcher(subjects[i][1]).matches());
		}
	}

	@Test
	void getCourses() throws Network.NetworkErrorException {
		//the first item is "None", so select the select item from terms
		String termVal = Network.getTerms()[1][1];
		String[][] subjects = Network.getSubjects(termVal);
		String[] subVals = new String[4];
		for (int i = 0; i < 4; i++) {
			subVals[i] = subjects[i][1];
		}

		Course[] courses = Network.getCourses(termVal, subVals);

		Assert.assertTrue("No courses found", courses != null && courses.length > 0);

		for (int i = 0; i < subjects.length; i++) {
			Assert.assertNotNull("data.Course subject empty", courses[i].subject);
			Assert.assertNotNull("data.Course number empty", courses[i].courseNumber);
			Assert.assertNotNull("data.Course title empty", courses[i].title);

			Assert.assertTrue("sections empty", courses[i].sections != 0);
			for(int j=0; j<courses[j].sections; j++){
				Assert.assertNotNull("seciton empty", courses[i].section[j]);
				Assert.assertNotNull("seciton number empty", courses[i].section[j].sectionNumber);
				Assert.assertNotNull("seciton instructor empty", courses[i].section[j].instructor);
			}
		}
	}

	@Test
	void sectionFull() throws Network.NetworkErrorException{
		String fall2017 = "201801";
		Section CS203A = new Section();
		CS203A.crn = 10366;
		Section CS301A = new Section();
		CS301A.crn = 10368;

		Assert.assertTrue("Section should be full", Network.sectionFull(fall2017, CS203A));
		Assert.assertTrue("Section shouldn't be full", !Network.sectionFull(fall2017, CS301A));
	}
}
