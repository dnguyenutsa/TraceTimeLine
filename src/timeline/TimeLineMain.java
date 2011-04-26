package timeline;

import java.util.Scanner; 
import java.io.*;
import java.util.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import timeline.TimeLine.ChangeEntry;


public class TimeLineMain {
	/**
	 * @param args
	 */
	static int DEFAULT_WIDTH = 800;
	static int DEFAULT_HEIGHT = 800;
	static HashMap<String, Color> authorMap = new HashMap<String, Color>();
	static int colormode = 100;
	static ArrayList<Calendar> calList = new ArrayList<Calendar>();
	static ArrayList<String> commitLogList = new ArrayList<String>();
	static ArrayList<Color> colorPalete = new ArrayList<Color>();
	static ArrayList<Color> boxColorPalete = new ArrayList<Color>();

	static ArrayList<String> logWordsList = new ArrayList<String>();
	static List<String> commonWordsList = new ArrayList<String>();
	static HashMap<String, Integer> wordCountMap = new HashMap<String, Integer>();
	static ArrayList<String> mostUsedWords = new ArrayList<String>();
	
	public static void main(String[] args) {

		//******************************************************************************
		// Set up color templates
		//******************************************************************************
		colorPalete.add(new Color(220,254,255)); 	// light white/background
		colorPalete.add(new Color(234,234,234)); 	// light purple
		colorPalete.add(new Color(192,192,192)); 	// dark gray
		colorPalete.add(new Color(222,222,222)); 	// light gray
		colorPalete.add(new Color(190,237,165)); 	// light green
		colorPalete.add(new Color(204,230,195)); 	// light green/background 2
		colorPalete.add(new Color(27,150,70));   	// dark green/text 2
		colorPalete.add(new Color(146,177,241)); 	// heavy blue
		colorPalete.add(new Color(189,159,189)); 	// light purple
		colorPalete.add(new Color(174,73,65)); 		// red
		colorPalete.add(new Color(214,237,251)); 	// light cyan/background
		colorPalete.add(new Color(61,84,154)); 		// dark cyan/text
		colorPalete.add(new Color(181,195,136)); 	// dark bronw yellow

		boxColorPalete.add(new Color(192,192,192)); 	// dark gray
		boxColorPalete.add(new Color(190,237,165)); 	// light green
		boxColorPalete.add(new Color(27,150,70));   	// dark green/text 2
		boxColorPalete.add(new Color(146,177,241)); 	// heavy blue
		boxColorPalete.add(new Color(174,73,65)); 		// red
		boxColorPalete.add(new Color(61,84,154)); 		// dark cyan/text
		boxColorPalete.add(new Color(181,195,136)); 	// dark bronw yellow
		boxColorPalete.add(new Color(255,124,64)); 	// dark gray

		Calendar cal = Calendar.getInstance();
		//		cal.set(Calendar.YEAR, 2009);
		//		cal.set(Calendar.DAY_OF_YEAR, 1);

		//		System.out.println(cal.getTime().toString());
		//		System.out.println(cal.get(Calendar.DAY_OF_WEEK));

		//** Initialize common words list **//
		String [] words = 
		{"2", "3", "a", "an", "and", "as", "add", "Add", "for", "from", "his", "her", 
				"in", "is", "it", "my", "of", "on", "our", "this", "This", "that", 
				"the", "their", "to", "up", "when", "with", "*" };

		commonWordsList = Arrays.asList(words);

		Random rand = new Random();

		try{
			Scanner scan = new Scanner(new FileReader(new File("log.txt")));
			while (scan.hasNext()){
				String aLine = scan.nextLine();
				if (!(aLine.startsWith("commit") ||  aLine.startsWith("Merge") || aLine.isEmpty())){
					commitLogList.add(aLine);
				}
			}
		}
		catch (FileNotFoundException e){
			System.out.println("not found");
		}
		catch (Exception e){}

		TimeLine.listData = new ArrayList<ChangeEntry>();

		Iterator<String> logListIter = TimeLineMain.commitLogList.iterator();
		String author = "";
		cal = Calendar.getInstance();

		String content = "";
		ChangeEntry nEntry = null;

		while (logListIter.hasNext()){
			String aLine = logListIter.next();


			if (aLine.startsWith("Author")){
				if (nEntry != null)
					nEntry.addContent(content);

				content = "";
				if (!author.equals("")){
					nEntry = new ChangeEntry(author,cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_WEEK), cal.get(Calendar.DAY_OF_MONTH));
					TimeLine.listData.add(nEntry);
				}

				Scanner sc = new Scanner(aLine);
				sc.useDelimiter(":");
				sc.next();
				author = sc.next().trim();
				if (!TimeLineMain.authorMap.containsKey(author))
					TimeLineMain.authorMap.put(author, boxColorPalete.get(rand.nextInt(boxColorPalete.size())));
			}
			else if (aLine.startsWith("Date")){
				String toParse = aLine.replaceFirst("Date:", "");

				SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy ZZZZZ");
				Date d;

				try {
					d = sdf.parse(toParse.trim());
					cal.setTime(d);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			else {
				//** Parse the content and generate word counts **//
				String delims = "[\"*()!,.\\s]+";
				String[] tokens = aLine.split(delims);

				for (String aWord : tokens){
					if (wordCountMap.containsKey(aWord))
						wordCountMap.put(aWord, wordCountMap.get(aWord)+1);
					else if (!commonWordsList.contains(aWord)) {
						logWordsList.add(aWord.trim());
						wordCountMap.put(aWord.trim(), 1);
					}
				}

				content += "\n"+aLine;
			}
		}

		
		String mostUsedWord = logWordsList.get(0);
		for (int i = 0; i < 10; i ++){
			for (String aWord : logWordsList){
				if (wordCountMap.get(aWord) > wordCountMap.get(mostUsedWord))
					mostUsedWord = aWord;
			}
			mostUsedWords.add(mostUsedWord);
			logWordsList.remove(mostUsedWord);
			mostUsedWord = logWordsList.get(0);
		}
		
//		for (String aWord : mostUsedWords){
//			System.out.println(aWord);
//		}

		TimeLine tLine = new TimeLine();
		tLine.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		//tLine.setFullScreenMode(true);



	}
}

