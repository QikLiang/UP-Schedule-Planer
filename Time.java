
/**
 * store time as an object
 */
public class Time
{
	int hour;
	int minute;

	public Time(){
		hour =25;
		minute = 0;
	}

	public boolean isEarlierthan(Time anotherTime){
		return this.hour + 0.01*this.minute <
					anotherTime.hour + 0.01*anotherTime.minute;
	}

	public static Time laterTime(Time t1, Time t2){
		if (t1.hour + 0.01*t1.minute < t2.hour + 0.01*t2.minute){
			return t2;
		}
		else{
			return t1;
		}
	}

	public static Time earlierTime(Time t1, Time t2){
		if (t1.hour + 0.01*t1.minute > t2.hour + 0.01*t2.minute){
			return t2;
		}
		else{
			return t1;
		}
	};
}
