package timeline;

//** This class is currently not used. For future refactoring. **//

import demos.TextExample;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolox.*;
import java.util.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.event.PStyledTextEventHandler;
import java.awt.*;

public class TimeLineFrame extends PFrame{
	private static final long serialVersionUID = 1L;

	private ArrayList<PText> textBoxes = new ArrayList<PText>();
	
    public TimeLineFrame() {
        this(null);
    }

    public TimeLineFrame(final PCanvas aCanvas) {
        super("TextExample", false, aCanvas);
    }

    public void initialize() {
    	TimeLineDataStruct data = new TimeLineDataStruct(10);
        //getCanvas().removeInputEventListener(getCanvas().getPanEventHandler());
        //final PStyledTextEventHandler textHandler = new PStyledTextEventHandler(getCanvas());
        //getCanvas().addInputEventListener(textHandler);
        PPath timeGrid = new PPath();
        timeGrid.setPathToRectangle(timeGrid.getBounds().OUT_LEFT, timeGrid.getBounds().OUT_BOTTOM, timeGrid.getBounds().OUT_RIGHT, timeGrid.getBounds().OUT_TOP);
        timeGrid.setWidth(200);
        timeGrid.setHeight(200);
        
        timeGrid.setPaint(Color.BLUE);
//        for (int i = 0; i < 10; i++){
//        	PText aText = new PText(data.nextData());
//        	//aText.setBounds(5,5,200,200);
//        	aText.setPaint(Color.red);
//        	
//        	//aText.setX(i*20);
//        	aText.setOffset(i*aText.getBounds().height, i*aText.getBounds().width);
//        	//getCanvas().getLayer().addChild(aText);
//        	timeGrid.addChild(aText);
//        }
        //getCanvas().getLayer().addChild(timeGrid);
        System.out.println(timeGrid.getScale());
//        System.out.println( timeGrid.getChildrenCount());
        timeGrid.setBounds(5,5,200,200);
        
        if (getCanvas().getCamera().getScale() < 0.5)
        	timeGrid.animateToBounds(20,20,100,100,1500);
        System.out.println(timeGrid.getBounds());
        System.out.println(timeGrid.getHeight());
        
        
        
        	
    }

    public static void main(final String[] args) {
    	
        new TimeLineFrame();
    }
}
