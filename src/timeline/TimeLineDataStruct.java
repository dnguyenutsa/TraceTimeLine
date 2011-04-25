package timeline;

//** This class is currently not used. For future refactoring. **//

 
import java.util.*;
import java.io.*;

public class TimeLineDataStruct {
	ArrayList<String> array;
	
	public TimeLineDataStruct(int numEntries){
		try {
			Scanner scan = new Scanner(new File("data.txt"));
			array = new ArrayList<String>();
			for (int i = 0; i < numEntries; i++){
				array.add(scan.nextLine());
			}
		}
		catch (FileNotFoundException e){
			//e.printStackTrace();
		}
	}
	
	public String nextData(){
		if (!array.isEmpty())
			return array.iterator().next();
		return ""+array.hashCode();
	}

}
