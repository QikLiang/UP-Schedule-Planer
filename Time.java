
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
	
	public Time(int setHour, int setMinute){
		hour = setHour;
		minute = setMinute;
	}
	
	public void increment12Hours(){
		hour = hour%12 +12;
	}

	/**
	 * parse string into time variable
	 * @param string in the form of "09:15 am-10:10 am"
	 * @param startTime the variable that will hold the starting time
	 * @param endTime the variable that will hold the ending time
	 */
	public static void parseTime(String string, Time startTime, Time endTime){
		String[] times = string.split("-");//split into beginning and end time
		String startApm = times[0].split(" ")[1];//am or pm
		String endApm = times[1].split(" ")[1];//am or pm
		times[0] = times[0].split(" ")[0];//hh:mm
		times[1] = times[1].split(" ")[0];//hh:mm

		startTime.hour = Integer.parseInt(times[0].split(":")[0]);
		startTime.minute = Integer.parseInt(times[0].split(":")[1]);
		endTime.hour = Integer.parseInt(times[1].split(":")[0]);
		endTime.minute = Integer.parseInt(times[1].split(":")[1]);
		
		if(startApm.equals("pm")){
			startTime.hour=startTime.hour%12 + 12;
		}
		if(endApm.equals("pm")){
			endTime.hour=endTime.hour%12 + 12;
		}
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
