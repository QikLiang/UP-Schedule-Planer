import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Network {

	//URLs for fetching data
	private static final String TERMS_URL =
			"https://selfserve-db.up.edu/prd/bwckschd.p_disp_dyn_sched";
	private static final String SUBJECTS_URL =
			"https://selfserve-db.up.edu/prd/bwckgens.p_proc_term_date";
	private static final String COURSES_URL =
			"https://selfserve-db.up.edu/prd/bwckschd.p_get_crse_unsec";

	//CSS selectors for extracting data from response webpages
	private static final String TERMS_DROPDOWN = "#term_input_id option";
	private static final String SUBJECTS_DROPDOWN = "#subj_id option";
	private static final String COURSE_TITLES = "table.datadisplaytable th.ddtitle a";
	private static final String COURSE_TABLE = "table.datadisplaytable td.dddefault table";
	private static final String COURSE_DETAILS = "td.dddefault";

	/**
	 * gets the list of terms
	 *
	 * @return [[termText, termValue]], null if connection fails
	 */
	public static String[][] getTerms() {
		String[][] result = getDropdownValues(
				Jsoup.connect(TERMS_URL).method(Connection.Method.GET),
				TERMS_DROPDOWN);
		if (result == null) {
			System.out.println("Error: Failed to fetch terms");
		}
		return result;
	}

	/**
	 * given the term, get list of available subjects
	 * @param termVal dropdown option value of given term
	 * @return [[subjectText, subjectValue]]
	 */
	public static String[][] getSubjects(String termVal) {
		String[][] result = getDropdownValues(
				Jsoup.connect(SUBJECTS_URL)
						.data("p_calling_proc", "bwckschd.p_disp_dyn_sched",
								"p_term", termVal)
						.method(Connection.Method.POST),
				SUBJECTS_DROPDOWN);
		if (result == null) {
			System.out.println("Error: Failed to fetch subjects");
		}
		return result;
	}

	/**
	 * fetches all available courses of given term and subjects
	 * @param termVal dropdown option value of given term
	 * @param subjects list of option values for subjects
	 * @return list of Courses, sections with TBA time skipped
	 */
	public static Course[] getCourses(String termVal, String[] subjects) {
		Connection conn = Jsoup.connect(COURSES_URL);
		conn = conn.data("term_in", termVal);

		//add subjects
		for (int i = 0; i < subjects.length; i++) {
			conn = conn.data("sel_subj", subjects[i]);
		}

		//add dummy variables required by form
		conn = conn
			.data("sel_subj", "dummy")
			.data("sel_day", "dummy")
			.data("sel_schd", "dummy")
			.data("sel_insm", "dummy")
			.data("sel_camp", "dummy")
			.data("sel_levl", "dummy")
			.data("sel_sess", "dummy")
			.data("sel_instr", "dummy")
			.data("sel_ptrm", "dummy")
			.data("sel_attr", "dummy")
			.data("sel_crse", "")
			.data("sel_title", "")
			.data("sel_schd", "%")
			.data("sel_from_cred", "")
			.data("sel_to_cred", "")
			.data("sel_levl", "%")
			.data("sel_instr", "%")
			.data("begin_hh", "0")
			.data("begin_mi", "0")
			.data("begin_ap", "a")
			.data("end_hh", "0")
			.data("end_mi", "0")
			.data("end_ap", "a");

		//fetch webpage
		Document doc;
		try {
			doc = conn.validateTLSCertificates(false).post();
		} catch (IOException e) {
			System.out.println("Error: Failed to fetch courses: " + e.getMessage());
			return null;
		}

		//extract course titles and details
		List<String> titles = doc.select(COURSE_TITLES).eachText();
		Elements tables = doc.select(COURSE_TABLE);

		//vars used in for loop
		int numSections = titles.size();
		HashMap<String, Course> courses = new HashMap<>();
		Section section;
		Course course;
		String courseStr;
		//captures string "(Name) - (crn) - ((subject) (course)) - (section)"
		Pattern title = Pattern.compile("(.+?) - (\\d+) - (([A-Z]+) ([\\dA-Z]+)) - ([A-Z]+)");
		Matcher match;
		Elements details;

		//add sections into courses
		for (int i = 0; i < numSections; i++) {
			match = title.matcher(titles.get(i));
			match.find();
			details = tables.get(i).select(COURSE_DETAILS);

			//ignore section if time undetermined
			if(details.get(1).text().equals("TBA")){
				continue;
			}

			//create course if it doesn't already exist
			courseStr = match.group(3);
			course = courses.get(courseStr);
			if (course == null) {
				course = new Course();
				course.subject = match.group(4);
				course.courseNumber = match.group(5);
				course.title = match.group(1);
				courses.put(courseStr, course);
			}

			//create section
			section = new Section();
			section.crn = Integer.parseInt(match.group(2));
			section.sectionNumber = match.group(6);
			section.setSchedule(details.get(2).text(),details.get(1).text());
			section.location = details.get(3).text();
			section.instructor = details.get(6).text().replaceAll("\\(.\\)", "").trim();

			//add section into course
			course.section[course.sections] = section;
			course.sections++;
		}

		return courses.values().toArray(new Course[0]);
	}

	/**
	 * helper method used by getTerms and getSubjects for reading values from a
	 * dropdown in the response webpage
	 * @param conn Jsoup.Connection object for opening the necessary webpage
	 * @param dropdown CSS selector for the dropdown to read from
	 * @return [[dropdownText, dropdownValue]]
	 */
	private static String[][] getDropdownValues(Connection conn, String dropdown) {
		//fetch webpage
		Document doc;
		try {
	        /*have to use HTTPS, so always accept certificate to
            prevent certificate exception
             */
			doc = conn.validateTLSCertificates(false).execute().parse();
		} catch (IOException e) {
			System.out.println("Error: Connection failed when fetching dropdown");
			return null;
		}

		//extract term text and value from dropdown
		Elements terms = doc.select(dropdown);
		String[] texts = terms.eachText().toArray(new String[0]);
		String[] values = terms.eachAttr("value").toArray(new String[0]);

		//put it into nested array
		String[][] results = new String[texts.length][2];
		for (int i = 0; i < texts.length; i++) {
			results[i][0] = texts[i];
			results[i][1] = values[i];
		}
		return results;
	}
}
