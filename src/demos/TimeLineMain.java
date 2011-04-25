package demos;

import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.text.SimpleDateFormat;



public class TimeLineMain {
	/**
	 * @param args
	 */
	static int DEFAULT_WIDTH = 800;
	static int DEFAULT_HEIGHT = 800;
	static HashMap authorMap = new HashMap();
	static int colormode = 100;
	static ArrayList<Calendar> calList = new ArrayList<Calendar>();

	public static void main(String[] args) {

		try{
			Scanner scan = new Scanner(new FileReader(new File("log.txt")));
			while (scan.hasNext()){
				String aLine = scan.nextLine();
				if (aLine.startsWith("Author")){
					Scanner sc = new Scanner(aLine);
					sc.useDelimiter(":");
					sc.next();
					authorMap.put(sc.next().trim(), new Color(colormode++,colormode++,colormode++));
				}
					
			}
		}
		catch (Exception e){
			System.out.println("not found");
		}
		
//		System.out.println(authorMap);
		
		try{
			Scanner scan = new Scanner(new FileReader(new File("log.txt")));
			while (scan.hasNext()){
				String aLine = scan.nextLine();
				if (aLine.startsWith("Date")){
					Scanner sc = new Scanner(aLine);
					String toParse = aLine.replaceFirst("Date:", "");
//					System.out.println(toParse.trim());
					
					SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy ZZZZZ");
					Date d = sdf.parse(toParse.trim());
					Calendar cal = Calendar.getInstance();
					cal.setTime(d);
					
					
					calList.add(cal);
					
//					System.out.println("Date");
//					System.out.println(cal.get(Calendar.DAY_OF_MONTH));
//					System.out.println(cal.get(Calendar.MONTH));
//					System.out.println(cal.get(Calendar.YEAR)); automatic meter reading
					
					
				}
					
			}
		}
		catch (Exception e){
			e.printStackTrace();
			System.out.println("not found");
		}
		

		TimeLine tLine = new TimeLine();
		tLine.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
//		tLine.setFullScreenMode(true);



	}
}
