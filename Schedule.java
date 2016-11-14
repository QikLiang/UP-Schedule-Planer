import java.io.Serializable;

/**
 * This is a more compact version of section with unnecessary
 * variables removed. It is used to check in what times does 
 * a plan have class.
 */
public class Schedule implements Serializable
{
	int course = 0;
	Time startTime = new Time();
	Time endTime = new Time();
}
