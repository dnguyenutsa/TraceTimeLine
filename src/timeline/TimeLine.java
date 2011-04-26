package timeline;

import java.awt.BasicStroke; 
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.ListIterator;
import java.util.Random;
import java.util.Scanner;

import java.util.GregorianCalendar;
import java.util.Iterator;

import java.util.Calendar;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.event.PZoomEventHandler;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.PFrame;
import edu.umd.cs.piccolox.event.PNavigationEventHandler;
import edu.umd.cs.piccolox.handles.PStickyHandleManager;
import edu.umd.cs.piccolox.nodes.PStyledText;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.PLayer;

import java.awt.event.KeyEvent;

//import org.eclipse.swt.graphics.FontMetrics;



public class TimeLine extends PFrame{

	private int numOfDays;
	static int numOfYears = 3;
	static int DEFAULT_TEXT_HEIGHT = 30;
	static double MAXZOOMSCALE = 30;
	static double MINZOOMSCALE = 0.03;
	static PCanvas canvas;
	static PLayer layer;
	static PCamera camera;
	static PText dateText;
	static final int yearWidth = 600;
	static final int dayWidth = 20;
	static boolean displayContent = false;
	static int numOfDataEntries = 50;
	static int DEFAULT_ENTRY_BOX_SIZE = 20;
	static int monthFactor = 14;
	static int yearFactor = 120;
	static Random rand = new Random();
	static PPath path;
	static enum viewLevel{DAY, WEEK, MONTH, QUARTER, YEAR, NONE}
	static enum sortModes{AUTHOR, DATE, NONE}
	static int[] dayPositionArray;
	static int[] weekPositionArray;
	static int[] monthPositionArray;
	static int [] yearPositionArray;
	static ArrayList<ChangeEntry> listData;
	static GregorianCalendar gCalendar;
	static viewLevel view = viewLevel.NONE;
	static sortModes sortMode = sortModes.DATE;
	static PText groupByDay, groupByWeek, groupByMonth, groupByYear;
	static PText sortByAuthor, sortByDate;
	static CalendarNode cNode; 
	static PPath groupByDayBox, groupByWeekBox, groupByMonthBox, groupByYearBox;
	static PPath sortByAuthorBox, sortByDateBox;
	
	

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TimeLine() {
		this(null);
	}

	public TimeLine(final PCanvas aCanvas) {
		super("TimeLine", false, aCanvas);
	}

	public void initialize() {

		//* The main canvas *//
		canvas = this.getCanvas();
		layer = canvas.getLayer();
		canvas.setBackground(new Color(250,254,255));

		//* Set up camera and default settings *//
		
		// Set up camera to use default zoom handler
		camera = canvas.getCamera();
		camera.setViewConstraint(PCamera.VIEW_CONSTRAINT_ALL);
		
//		canvas.getZoomEventHandler().setMaxScale(MAXZOOMSCALE);
//		canvas.getZoomEventHandler().setMinScale(MINZOOMSCALE);
		canvas.setZoomEventHandler(null);
		
		// Set up camera to zoom with mouse scroll
		camera.addInputEventListener(new PBasicInputEventHandler() {
			@Override
			public void mouseWheelRotated(PInputEvent event) {
				double currentScale = camera.getViewScale();
				double scaleDelta = (1.0f - (0.1f * event.getWheelRotation()));
				double newScale = currentScale * scaleDelta;
				if (newScale < MINZOOMSCALE) {
					return;
				}

				if ((MAXZOOMSCALE > 0) && (newScale > MAXZOOMSCALE)) {
					return;
				}

				Point2D pos = event.getPosition();
				camera.scaleViewAboutPoint(scaleDelta, pos.getX(),pos.getY());

			}
		});

		// Set up camera to pan with mouse drag
		canvas.getPanEventHandler().setAutopan(false); // remove autonpan
		final PZoomEventHandler zoomHandler = canvas.getZoomEventHandler();
		// Modify the pan event handler to have different pan speed
		canvas.setPanEventHandler(new PPanEventHandler(){
			protected void pan(final PInputEvent event){
		        final PCamera c = event.getCamera();
		        final Point2D l = event.getPosition();
		        final double panSpeed = 0.5;
		        
		        if (c.getViewBounds().contains(l)) {
		            final PDimension d = event.getDelta();
		            c.translateView(panSpeed*d.getWidth(), panSpeed*d.getHeight());
		        }
			}
		});

		dateText = new PText(""+camera.getViewBounds());
		dateText.setPaint(new Color(200,200,200));
		dateText.setBounds(500, 500, 250, 250);

		//******************************************************************
		// Set up dayViewLevel, monthViewLevel, yearViewLevel nodes.
		// Set up sortByAuthor, sortByDate nodes;
		// Add them to camera and add corresponding listeners.
		//******************************************************************
		
		int numOfButtons = 6;
		//**Set day grouping**
		groupByDay = new PText("Group By Day");
		groupByDay.setConstrainWidthToTextWidth(false);
		groupByDay.setTextPaint(TimeLineMain.colorPalete.get(6));
		groupByDay.setHorizontalAlignment(Component.CENTER_ALIGNMENT);
		groupByDayBox = PPath.createRoundRectangle(0, 0, (float) groupByDay.getWidth()*2, (float) groupByDay.getHeight()*2, 10, 10);
		groupByDayBox.addChild(groupByDay);
		groupByDayBox.setPaint(TimeLineMain.colorPalete.get(5));
		groupByDayBox.setStrokePaint(TimeLineMain.colorPalete.get(6));
		groupByDayBox.setBounds(0, camera.getViewBounds().getMaxY()-20, camera.getViewBounds().width/numOfButtons, groupByDay.getHeight()*2);
		groupByDay.setBounds(groupByDay.getParent().getBounds());
		camera.addChild(groupByDayBox);

		groupByDay.addInputEventListener(new PBasicInputEventHandler(){
			public void mouseClicked(PInputEvent event){
				if (!view.equals(viewLevel.DAY)){
//					canvas.setZoomEventHandler(null);

					view = viewLevel.DAY;
					groupByDayBox.setPaint(TimeLineMain.colorPalete.get(2));
					groupByWeekBox.setPaint(TimeLineMain.colorPalete.get(5));
					groupByMonthBox.setPaint(TimeLineMain.colorPalete.get(5));
					groupByYearBox.setPaint(TimeLineMain.colorPalete.get(5));
					cNode.layoutChildren(view, sortMode);
				}
//				canvas.setZoomEventHandler(zoomHandler);
			}
		});
		
		//**Set week grouping**
		groupByWeek = new PText("Group By Week");
		groupByWeek.setConstrainWidthToTextWidth(false);
		groupByWeek.setTextPaint(TimeLineMain.colorPalete.get(6));
		groupByWeek.setHorizontalAlignment(Component.CENTER_ALIGNMENT);
		groupByWeekBox = PPath.createRoundRectangle(0, 0, (float) groupByWeek.getWidth()*2, (float) groupByWeek.getHeight()*2, 10, 10);
		groupByWeekBox.addChild(groupByWeek);
		groupByWeekBox.setPaint(TimeLineMain.colorPalete.get(5));
		groupByWeekBox.setStrokePaint(TimeLineMain.colorPalete.get(6));
		groupByWeekBox.setBounds(camera.getViewBounds().width/numOfButtons, camera.getViewBounds().getMaxY()-20, camera.getViewBounds().width/numOfButtons, groupByWeek.getHeight()*2);
		groupByWeek.setBounds(groupByWeek.getParent().getBounds());
		camera.addChild(groupByWeekBox);

		groupByWeek.addInputEventListener(new PBasicInputEventHandler(){
			public void mouseClicked(PInputEvent event){
				if (!view.equals(viewLevel.WEEK)){
//					canvas.setZoomEventHandler(null);

					view = viewLevel.WEEK;
					groupByDayBox.setPaint(TimeLineMain.colorPalete.get(5));
					groupByWeekBox.setPaint(TimeLineMain.colorPalete.get(2));
					groupByMonthBox.setPaint(TimeLineMain.colorPalete.get(5));
					groupByYearBox.setPaint(TimeLineMain.colorPalete.get(5));
					cNode.layoutChildren(view, sortMode);
				}
//				canvas.setZoomEventHandler(zoomHandler);
			}
		});

		//**Set month grouping**
		groupByMonth = new PText("Group By Month");
		groupByMonth.setConstrainWidthToTextWidth(false);
		groupByMonth.setBounds(camera.getViewBounds().width/numOfButtons, 0, camera.getViewBounds().width/numOfButtons, 10);
		groupByMonth.setTextPaint(TimeLineMain.colorPalete.get(6));
		groupByMonth.setHorizontalAlignment(Component.CENTER_ALIGNMENT);
		groupByMonthBox = PPath.createRoundRectangle(0, 0, (float) groupByMonth.getWidth()*2, (float) groupByMonth.getHeight()*2, 10, 10);
		groupByMonthBox.addChild(groupByMonth);
		groupByMonthBox.setPaint(TimeLineMain.colorPalete.get(5));
		groupByMonthBox.setStrokePaint(TimeLineMain.colorPalete.get(6));
		groupByMonthBox.setBounds(2*camera.getViewBounds().width/numOfButtons, camera.getViewBounds().getMaxY()-20, camera.getViewBounds().width/numOfButtons, groupByMonth.getHeight()*2);
		groupByMonth.setBounds(groupByMonth.getParent().getBounds());
		camera.addChild(groupByMonthBox);

		groupByMonth.addInputEventListener(new PBasicInputEventHandler(){
			public void mouseClicked(PInputEvent event){
				if (!view.equals(viewLevel.MONTH)){
//					canvas.setZoomEventHandler(null);

					view = viewLevel.MONTH;
					groupByDayBox.setPaint(TimeLineMain.colorPalete.get(5));
					groupByWeekBox.setPaint(TimeLineMain.colorPalete.get(5));
					groupByMonthBox.setPaint(TimeLineMain.colorPalete.get(2));
					groupByYearBox.setPaint(TimeLineMain.colorPalete.get(5));
					cNode.layoutChildren(view, sortMode);
				}
//				canvas.setZoomEventHandler(zoomHandler);
			}
		});

		//**Set year grouping**
		groupByYear = new PText("Group By Year");
		groupByYear.setConstrainWidthToTextWidth(false);
		groupByYear.setBounds(camera.getViewBounds().width/numOfButtons, 0, camera.getViewBounds().width/5, 10);
		groupByYear.setTextPaint(TimeLineMain.colorPalete.get(6));
		groupByYear.setHorizontalAlignment(Component.CENTER_ALIGNMENT);
		groupByYearBox = PPath.createRoundRectangle(0, 0, (float) groupByYear.getWidth()*2, (float) groupByYear.getHeight()*2, 10, 10);
		groupByYearBox.addChild(groupByYear);
		groupByYearBox.setPaint(TimeLineMain.colorPalete.get(5));
		groupByYearBox.setStrokePaint(TimeLineMain.colorPalete.get(6));
		groupByYearBox.setBounds(3*camera.getViewBounds().width/numOfButtons, camera.getViewBounds().getMaxY()-20, camera.getViewBounds().width/numOfButtons, groupByYear.getHeight()*2);
		groupByYear.setBounds(groupByYear.getParent().getBounds());
		camera.addChild(groupByYearBox);

		groupByYear.addInputEventListener(new PBasicInputEventHandler(){
			public void mouseClicked(PInputEvent event){
				if (!view.equals(viewLevel.YEAR)){
//					canvas.setZoomEventHandler(null);

					view = viewLevel.YEAR;
					groupByDayBox.setPaint(TimeLineMain.colorPalete.get(5));
					groupByWeekBox.setPaint(TimeLineMain.colorPalete.get(5));
					groupByMonthBox.setPaint(TimeLineMain.colorPalete.get(5));
					groupByYearBox.setPaint(TimeLineMain.colorPalete.get(2));
					cNode.layoutChildren(view, sortMode);
				}
//				canvas.setZoomEventHandler(zoomHandler);
			}
		});

		//**Set sorting by author mode**
		sortByAuthor = new PText("Sort by Author");
		sortByAuthor.setConstrainWidthToTextWidth(false);
		sortByAuthor.setBounds(3*camera.getViewBounds().width/numOfButtons, 0, camera.getViewBounds().width/numOfButtons, 10);
		sortByAuthor.setTextPaint(TimeLineMain.colorPalete.get(11));
		sortByAuthor.setHorizontalAlignment(Component.CENTER_ALIGNMENT);
		sortByAuthorBox = PPath.createRoundRectangle(0, 0, (float) sortByAuthor.getWidth()*2, (float) sortByAuthor.getHeight()*2, 10, 10);
		sortByAuthorBox.addChild(sortByAuthor);
		sortByAuthorBox.setPaint(TimeLineMain.colorPalete.get(10));
		sortByAuthorBox.setStrokePaint(TimeLineMain.colorPalete.get(11));
		sortByAuthorBox.setBounds(4*camera.getViewBounds().width/numOfButtons, camera.getViewBounds().getMaxY()-20, camera.getViewBounds().width/numOfButtons, sortByAuthor.getHeight()*2);
		sortByAuthor.setBounds(sortByAuthor.getParent().getBounds());
		camera.addChild(sortByAuthorBox);

		sortByAuthor.addInputEventListener(new PBasicInputEventHandler(){
			public void mouseClicked(PInputEvent event){
				if (!sortMode.equals(sortModes.AUTHOR)){
//					canvas.setZoomEventHandler(null);

					sortMode = sortModes.AUTHOR;
					sortByAuthorBox.setPaint(TimeLineMain.colorPalete.get(2));
					sortByDateBox.setPaint(TimeLineMain.colorPalete.get(10));
					cNode.layoutChildren(view, sortMode);
				}
//				canvas.setZoomEventHandler(zoomHandler);
			}
		});

		//**Set sorting by date mode**
		sortByDate = new PText("Sort by Date");
		sortByDate.setConstrainWidthToTextWidth(false);
		sortByDate.setBounds(4*camera.getViewBounds().width/numOfButtons, 0, camera.getViewBounds().width/numOfButtons, 10);
		sortByDate.setTextPaint(TimeLineMain.colorPalete.get(11));
		sortByDate.setHorizontalAlignment(Component.CENTER_ALIGNMENT);
		sortByDateBox = PPath.createRoundRectangle(0, 0, (float) sortByDate.getWidth()*2, (float) sortByDate.getHeight()*2, 10, 10);
		sortByDateBox.addChild(sortByDate);
		sortByDateBox.setPaint(TimeLineMain.colorPalete.get(10));
		sortByDateBox.setStrokePaint(TimeLineMain.colorPalete.get(11));
		sortByDateBox.setBounds(5*camera.getViewBounds().width/numOfButtons, camera.getViewBounds().getMaxY()-20, camera.getViewBounds().width/numOfButtons, sortByDate.getHeight()*2);
		sortByDate.setBounds(sortByDate.getParent().getBounds());
		camera.addChild(sortByDateBox);

		sortByDate.addInputEventListener(new PBasicInputEventHandler(){
			public void mouseClicked(PInputEvent event){
				if (!sortMode.equals(sortModes.DATE)){
//					canvas.setZoomEventHandler(null);

					sortMode = sortModes.DATE;
					sortByAuthorBox.setPaint(TimeLineMain.colorPalete.get(10));
					sortByDateBox.setPaint(TimeLineMain.colorPalete.get(2));
					cNode.layoutChildren(view, sortMode);
				}
//				canvas.setZoomEventHandler(zoomHandler);
			}
		});

		gCalendar = new GregorianCalendar();

		//**Create the timeGrid**//
		TimeLineGrid tGrid = new TimeLineGrid(2009, 2011);
		tGrid.setBounds(0,0,10,10);
		layer.addChild(tGrid);

		String QUOTE =
			"Everyone generalizes from one example. \nAt least, I do. -- Vlad Taltos (Issola, Steven Brust)";

		final PText text = new PText(QUOTE);
		text.setConstrainWidthToTextWidth(false);
		text.setWidth(15);
		
		text.setPaint(new Color(199,200,205));
		text.setTextPaint(new Color(100,100,150));
		path = PPath.createRoundRectangle(0, 0, 20, 20, 5, 5);
		path.offset(100,100);
		path.setStrokePaint(new Color(222,122,122));
//		path.addChild(text);
		text.setBounds(path.getBounds());
		
//		FontMetrics fm = new FontMetrics(text.getFont());
//		System.out.println(text.getFont().getSize2D());
		
		getCanvas().getLayer().addChild(path);
		cNode = new CalendarNode();

		camera.addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				//				if (camera.getViewScale() <= 1){
				//					if (!view.equals(viewLevel.YEAR)){
				//						canvas.setZoomEventHandler(null);
				//
				//						view = viewLevel.YEAR;
				//						cNode.layoutChildren(view);
				//					}
				//					canvas.setZoomEventHandler(zoomHandler);
				//				}
				//				else if (camera.getViewScale() <= 2){
				//					if (!view.equals(viewLevel.QUARTER)){
				//						canvas.setZoomEventHandler(null);
				//
				//						view = viewLevel.QUARTER;
				//						//						cNode.layoutChildren(view);
				//					}
				//					canvas.setZoomEventHandler(zoomHandler);
				//				}
				//				else if (camera.getViewScale() <= 3){
				//					if (!view.equals(viewLevel.MONTH)){
				//						canvas.setZoomEventHandler(null);
				//
				//						view = viewLevel.MONTH;
				//						cNode.layoutChildren(view);
				//					}
				//					canvas.setZoomEventHandler(zoomHandler);
				//				}
				//
				//				else if (camera.getViewScale() <= 4){
				//					if (!view.equals(viewLevel.WEEK)){
				//						canvas.setZoomEventHandler(null);
				//
				//						view = viewLevel.WEEK;
				//						//						cNode.layoutChildren(view);
				//					}
				//					canvas.setZoomEventHandler(zoomHandler);
				//				}
				//				else {
				//					if (!view.equals(viewLevel.DAY)){
				//						canvas.setZoomEventHandler(null);
				//
				//						view = viewLevel.DAY;
				//						cNode.layoutChildren(view);
				//					}
				//					canvas.setZoomEventHandler(zoomHandler);
				//				}
//				text.setFont(text.getFont().deriveFont((float) 0.1));
				//text.scale(0.2);
//				System.out.println(""+text.getFont());
//				System.out.println(camera.getViewScale());
				if (camera.getViewScale()>10){
					if (!displayContent){
						displayContent = true;
						cNode.layoutChildren(view, sortMode);
					}
					//					camera.animateViewToCenterBounds(event.getPickedNode().getBounds(), true, 750);
					//					camera.animateViewToPanToBounds(event.getPickedNode().getBounds(), 750);
				}
				else {
					if (displayContent){
						displayContent = false;
						cNode.layoutChildren(view, sortMode);
					}
				}
				dateText.setText(""+text.getFont());
				//				year2010.animateTransformToBounds(0, 0, 20, 20, 1500);
				//				year2011.animateTransformToBounds(100, 100, 40, 30, 1000);
				//				System.out.println("canvas bound " + canvas.getBounds());
				//				System.out.println("camera bound " + camera.getBounds());		System.out.println("dateText bound " + dateText.getBounds());
				//				System.out.println("dateText gbound "+ dateText.getGlobalBounds());
				//				camera.animateViewToCenterBounds(canvas.getLayer().getFullBoundsReference(), true, 500);
			}

		});

		canvas.addInputEventListener(new PBasicInputEventHandler() {
			public void mouseClicked(PInputEvent event){
				PBounds vbound = camera.getViewBounds();
				//				n1.animateTransformToBounds(n1.getX()+vbound.getX(), 
				//				n1.getY()+vbound.getY(), 
				//				n1.getHeight()*camera.getViewScale(), 
				//				n1.getWidth()*camera.getViewScale(), 1000);
				//				n1.animateTransformToBounds(n1.getX()+vbound.getX(), 
				//				n1.getY()+vbound.getY(), 
				//				n1.getHeight(), 
				//				n1.getWidth(), 1000);
				//				n2.animateToRelativePosition(getLocationOnScreen(), getLocation(), vbound, HEIGHT);
				//				n1.animateToBounds(vbound.getCenterX(), vbound.getCenterY(), n1.getHeight(), 
				//				n1.getWidth(), 750);
				//				n3.animateToBounds(vbound.getX(), vbound.getY(), vbound.getHeight(), 

				//				vbound.getWidth(), 700);
				//camera.scaleView(2.0);
				//				camera.animateViewToCenterBounds(camera.getBounds(), true, 750);

				if (camera.getViewScale()>=20){
					//					camera.animateViewToCenterBounds(event.getPickedNode().getBounds(), true, 750);
					//					camera.animateViewToPanToBounds(event.getPickedNode().getBounds(), 750);
				}


			}

			//			public void keyPressed(final PInputEvent event) {
			//				final PNode node = event.getPickedNode();
			//				//				System.out.println(event.getButton());
			//				System.out.println("Key is pressed");
			//				System.out.println(event.getKeyCode());
			//
			//				switch (event.getKeyCode()) {
			//				case KeyEvent.VK_UP:
			//					System.out.println("key pressed");
			//					node.translate(0, -10f);
			//					break;
			//				case KeyEvent.VK_DOWN:
			//					camera.translate(0, 10f);
			//					break;
			//				case KeyEvent.VK_LEFT:
			//					camera.translate(-10f, 0);
			//					break;
			//				case KeyEvent.VK_RIGHT:
			//					camera.translate(10f, 0);
			//					break;
			//				}
			//			}

		});



		// Generate sample test data
		dayPositionArray = new int[365*3];
		double numWeeks = Math.ceil((365*3-4)/7.0);
		weekPositionArray = new int[(int) numWeeks+1];
		monthPositionArray = new int[12*3];
		yearPositionArray = new int[3];

		double xOffset = 0;
		double yOffset = 100;
		int count = 0;

		int pos = 0;

		// Initialize boxes on timeline on dayView
		for (int i = 0; i < listData.size(); i++){
			ChangeEntry entry = listData.get(i);

			entry.dayViewPosition += entry.day;

			for (int m = 0; m < entry.month; m++){
				if (m == 0 || m == 2 || m == 4 || m ==6 || m == 7 || m == 9 || m == 11){
					entry.dayViewPosition += 31;
					entry.monthViewPosition += 31;
				}
				else if (m == 1){
					if (gCalendar.isLeapYear(entry.year)){
						entry.dayViewPosition += 29;
						entry.monthViewPosition += 29;
					}
					else{
						entry.dayViewPosition += 28;
						entry.monthViewPosition += 28;
					}
				}
				else {
					entry.dayViewPosition += 30;
					entry.monthViewPosition += 30;
				}
			}

			for (int y = 2009; y < entry.year; y++ ){
				if (gCalendar.isLeapYear(y)){
					entry.dayViewPosition += 366;
					entry.monthViewPosition += 366;
					entry.yearViewPosition += 366;
				}
				else{
					entry.dayViewPosition += 365;
					entry.monthViewPosition += 365;
					entry.yearViewPosition += 365;
				}
			}
			
			//** Calculate week view position **//
			entry.weekViewPosition = (entry.dayViewPosition-4)/7;
			
			//** Set entry's bound and add to the calendar node **//
			//entry.setBounds(0, 0, DEFAULT_ENTRY_BOX_SIZE, DEFAULT_ENTRY_BOX_SIZE);
			//entry.offset(xOffset - entry.getX(), yOffset);
			//xOffset += entry.getFullBoundsReference().getWidth();

			cNode.addChild(entry);
		}

		cNode.layoutChildren(viewLevel.DAY, sortModes.DATE);
		layer.addChild(cNode);

		//new PStickyHandleManager(getCanvas().getCamera(), n3);
		//getCanvas().removeInputEventListener(getCanvas().getPanEventHandler());
		//getCanvas().addInputEventListener(new PDragEventHandler());
		//getCanvas().removeInputEventListener(getCanvas().getPanEventHandler());
		//getCanvas().addInputEventListener(new PNavigationEventHandler());
	}
	

	//***********************************************************************************
	// 
	//***********************************************************************************
	static class ChangeEntry extends PPath implements Comparable{

		Color boxColor = new Color(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255));
		int year;
		int month;
		int week;
		int day;
		String author;
		PText content;

		int yearViewPosition = 123;
		int monthViewPosition = 8;
		int weekViewPosition = 0;
		int dayViewPosition = 0;

		public ChangeEntry(String a, int y, int m, int w, int d){
			super();
			author = a;
			year = y;
			month = m;
			week = w;
			day = d;

			content = new PText("");
			content.setConstrainWidthToTextWidth(false);
			//System.out.println(content.getFont());
			content.setFont(content.getFont().deriveFont((float) 0.5));
			
			//System.out.println(content.getFont());
			
			this.setPathToRectangle(0, 0, DEFAULT_ENTRY_BOX_SIZE, DEFAULT_ENTRY_BOX_SIZE);
			content.setBounds(this.getX() , this.getY() , this.getWidth(), this.getHeight());
			content.setHeight(10);

			content.setHorizontalAlignment(Component.CENTER_ALIGNMENT);
			content.setTextPaint(new Color(100,100,150));
			this.addChild(content);
			
			content.setBounds(content.getParent().getBounds().inset(3, 3));
			
			content.setText(author);
			this.setStrokePaint(TimeLineMain.authorMap.get(author));
			//this.setPaint(TimeLineMain.authorMap.get(author));

		}

		public void addContent(String cont){
			content.setText(content.getText().concat("\n"+cont));
		}
		
		public String getEntryContent(){
			return content.getText();
		}

		@Override
		public int compareTo(Object arg0) {
			ChangeEntry oEntry = (ChangeEntry) arg0;
			/*if (this.author.equals(oEntry.author))
				if (this.year == oEntry.year)
					if (this.month == oEntry.month)
						return (this.day - oEntry.day);
					else
						return (this.month - oEntry.month);
				else
					return (this.year - oEntry.year);
			else
				return this.author.compareTo(oEntry.author);*/
			return this.author.compareTo(oEntry.author);
		}

	}

	//***********************************************************************************
	// 
	//***********************************************************************************
	static class CalendarNode extends PNode{

		ArrayList<ChangeEntry> tempList = (ArrayList<ChangeEntry>) listData.clone();

		public CalendarNode(){
			this.setBounds(0,0,800,800);
		}

		public void layoutChildren(viewLevel view, sortModes mode) {
			int animationTime = 500;

			for (int i = 0; i < dayPositionArray.length; i++){
				dayPositionArray[i] = 0;
			}
			
			for (int i = 0 ; i < weekPositionArray.length; i++){
				weekPositionArray[i] = 0;
			}

			for (int i = 0 ; i < monthPositionArray.length; i++){
				monthPositionArray[i] = 0;
			}

			for (int i = 0; i < yearPositionArray.length; i++){
				yearPositionArray[i] = 0;
			}

			if (mode.equals(sortModes.AUTHOR))
				Collections.sort(tempList);
			else if (mode.equals(sortModes.DATE))
				tempList = (ArrayList<ChangeEntry>) listData.clone();

			// Initialize boxes on timeline on dayView
			for (int i = tempList.size()-1; i >=0; i--){
				ChangeEntry entry = tempList.get(i);

				
				if (view.equals(viewLevel.DAY)){
					entry.animateTransformToBounds((double) entry.dayViewPosition*dayWidth, (double) (100 + (DEFAULT_ENTRY_BOX_SIZE+1)*(dayPositionArray[entry.dayViewPosition]++)),DEFAULT_ENTRY_BOX_SIZE, DEFAULT_ENTRY_BOX_SIZE, animationTime);
					entry.getChild(0).setVisible(displayContent);
				}
				else if (view.equals(viewLevel.WEEK)){
					entry.animateTransformToBounds((double) (entry.dayViewPosition-entry.week+3)*dayWidth, (double) (100 + (DEFAULT_ENTRY_BOX_SIZE+1)*(weekPositionArray[entry.weekViewPosition]++)), 3*DEFAULT_ENTRY_BOX_SIZE, DEFAULT_ENTRY_BOX_SIZE, animationTime);
					entry.getChild(0).setVisible(false);
				}
				else if (view.equals(viewLevel.MONTH)){
					entry.animateTransformToBounds((double) entry.monthViewPosition*dayWidth, (double) (100 + (DEFAULT_ENTRY_BOX_SIZE+1)*(monthPositionArray[12*(entry.year-2009) + entry.month]++)), monthFactor*DEFAULT_ENTRY_BOX_SIZE, DEFAULT_ENTRY_BOX_SIZE, animationTime);
					entry.getChild(0).setVisible(false);
				}
				else if (view.equals(viewLevel.YEAR)){
					entry.animateTransformToBounds((double) entry.yearViewPosition*dayWidth, (double) (100 + (DEFAULT_ENTRY_BOX_SIZE+1)*(yearPositionArray[entry.year-2009]++)), yearFactor*DEFAULT_ENTRY_BOX_SIZE, DEFAULT_ENTRY_BOX_SIZE, animationTime);
					entry.getChild(0).setVisible(false);
				}
			}
		}
	}

	//***********************************************************************************
	// 
	//***********************************************************************************
	static class TimeLineGrid extends PNode {

		int sYear;
		int eYear;
		GregorianCalendar gCalendar;

		public TimeLineGrid(int startYear, int endYear){
			sYear = startYear;
			eYear = endYear;
			gCalendar = new GregorianCalendar();
		}

		protected void paint(PPaintContext paintContext) {

			Graphics2D g2 = paintContext.getGraphics();
			int numOfDays = 0;

			Stroke originalStroke = g2.getStroke();
			Font originalFont = g2.getFont();

			for (int i = sYear; i <= eYear; i++){
				if (gCalendar.isLeapYear(i))
					numOfDays += 366;
				else
					numOfDays += 365;
			}
			
			// Draw grid line
			g2.setPaint(Color.BLACK);
			g2.drawLine(0, 30, dayWidth*numOfDays, 50);
			this.setBounds(0,0, numOfDays*dayWidth, 50);

			int tickPos = 0;

			// Draw minute ticks
			// Draw hour ticks

			// Draw day ticks
			for (int day = 0; day <= numOfDays; day++){
				g2.drawLine(day*dayWidth, 30, day*dayWidth, 35);
			}
			
			// Draw week ticks
			g2.setPaint(Color.ORANGE);
			
			int startWeek = 4;
			for (int week = startWeek; week <= 365*3; week += 7){
				g2.drawLine(week*dayWidth, 30, week*dayWidth, 40);
			}

			// Draw month ticks
			g2.setPaint(Color.BLUE);

			for (int y = sYear; y <= eYear; y++){
				for (int m = 0; m < 12; m++){
					g2.drawLine(tickPos*dayWidth, 30, tickPos*dayWidth, 45);
					if (m == 0 || m == 2 || m == 4 || m == 6 || m == 7 || m == 9 || m == 11){
						tickPos += 31;
					}
					else if (m == 1){
						if (gCalendar.isLeapYear(y))
							tickPos += 29;
						else
							tickPos += 28;
					}
					else {
						tickPos += 30;
					}
				}
			}

			tickPos = 0;

			// Draw year ticks
			g2.setPaint(Color.RED);
			g2.setStroke(new BasicStroke(2));

			for (int y = sYear; y <= eYear+1; y++){
				g2.drawLine(tickPos*dayWidth, (int) (30/camera.getViewScale()), tickPos*dayWidth, 0);
				if (gCalendar.isLeapYear(y)){
					tickPos += 366;
				}
				else
					tickPos += 365;
			}

			// Draw year labels
			g2.setPaint(Color.BLACK);
			java.awt.FontMetrics fm = g2.getFontMetrics();
			int labelPos = 180;
			g2.setFont(new Font("Serif", Font.PLAIN, Math.max(24,(int) (24/camera.getViewScale()))));
			for (int y = sYear; y <= eYear; y++){
				g2.drawString(y+"", dayWidth * labelPos - fm.stringWidth(y+"")/2, 10);

				if (gCalendar.isLeapYear(y))
					labelPos += 366;
				else
					labelPos += 365;
			}

			// Draw month labels	
			g2.setFont(new Font("Serif", Font.PLAIN, dayWidth*4));
			g2.setPaint(Color.GRAY);

			labelPos = 15;
			for (int y = sYear; y <= eYear; y++){
				String monthString = "";
				for (int m = 0; m < 12; m++){
					switch (m) {
					case 0:
						monthString = "January";
						g2.drawString(monthString, dayWidth * labelPos - g2.getFont().getSize()/2, 20);
						labelPos += 30;
						break;
					case 1:
						monthString = "February";
						g2.drawString(monthString, dayWidth * labelPos - fm.stringWidth(monthString)/2, 20);
						if (gCalendar.isLeapYear(y))
							labelPos += 30;
						else
							labelPos += 29;
						break;
					case 2:
						monthString = "March";
						g2.drawString(monthString, dayWidth * labelPos - fm.stringWidth(monthString)/2, 10);
						labelPos += 31;
						break;
					case 3:
						monthString = "April";
						g2.drawString(monthString, dayWidth * labelPos - fm.stringWidth(monthString)/2, 10);
						labelPos += 30;
						break;
					case 4:
						monthString = "May";
						g2.drawString(monthString, dayWidth * labelPos - fm.stringWidth(monthString)/2, 10);
						labelPos += 31;
						break;
					case 5:
						monthString = "June";
						g2.drawString(monthString, dayWidth * labelPos - fm.stringWidth(monthString)/2, 10);
						labelPos += 30;
						break;
					case 6:
						monthString = "July";
						g2.drawString(monthString, dayWidth * labelPos - fm.stringWidth(monthString)/2, 10);
						labelPos += 31;
						break;
					case 7:
						monthString = "August";
						g2.drawString(monthString, dayWidth * labelPos - fm.stringWidth(monthString)/2, 10);
						labelPos += 31;
						break;
					case 8:
						monthString = "September";
						g2.drawString(monthString, dayWidth * labelPos - fm.stringWidth(monthString)/2, 10);
						labelPos += 30;
						break;
					case 9:
						monthString = "October";
						g2.drawString(monthString, dayWidth * labelPos - fm.stringWidth(monthString)/2, 10);
						labelPos += 31;
						break;
					case 10:
						monthString = "November";
						g2.drawString(monthString, dayWidth * labelPos - fm.stringWidth(monthString)/2, 10);
						labelPos += 30;
						break;
					case 11:
						monthString = "December";
						g2.drawString(monthString, dayWidth * labelPos - fm.stringWidth(monthString)/2, 10);
						labelPos += 31;
						break;
					default:
						break;
					}
				}
			}
		}
	}

}


//**********************************************Unused Code***************************************//
/*
 * PText year1 = new PText("2010");
		final PPath n1 = PPath.createRectangle(0, 0, TimeLineMain.DEFAULT_WIDTH/numOfYears, (float) year1.getHeight());
		PPath quarter1 = PPath.createRectangle(0, 0, (float) n1.getWidth()/4, (float) year1.getHeight());
		PPath quarter2 = PPath.createRectangle(0, 0, (float) n1.getWidth()/4, (float) year1.getHeight());
		PPath quarter3 = PPath.createRectangle(0, 0, (float) n1.getWidth()/4, (float) year1.getHeight());
		PPath quarter4 = PPath.createRectangle(0, 0, (float) n1.getWidth()/4, (float) year1.getHeight());
		quarter1.setPaint(Color.orange);
		quarter2.setPaint(Color.cyan);
		quarter3.setPaint(Color.green);
		quarter4.setPaint(Color.yellow);
		quarter1.offset(0, (float) year1.getHeight());
		quarter2.offset((float) n1.getWidth()/4, (float) year1.getHeight()); 
		quarter3.offset((float) n1.getWidth()/4*2, (float) year1.getHeight()); 
		quarter4.offset((float) n1.getWidth()/4*3, (float) year1.getHeight()); 

		PPath jan1 = PPath.createRectangle(0,0, (float) quarter1.getWidth()/3, (float) quarter1.getHeight()+1);
		PPath feb1 = PPath.createRectangle(0,0, (float) quarter1.getWidth()/3, (float) quarter1.getHeight()+1);
		PPath mar1 = PPath.createRectangle(0,0, (float) quarter1.getWidth()/3, (float) quarter1.getHeight()+1);
		PPath apr1 = PPath.createRectangle(0,0, (float) quarter1.getWidth()/3, (float) quarter1.getHeight()+1);
		PPath may1 = PPath.createRectangle(0,0, (float) quarter1.getWidth()/3, (float) quarter1.getHeight()+1);
		PPath jun1 = PPath.createRectangle(0,0, (float) quarter1.getWidth()/3, (float) quarter1.getHeight()+1);
		PPath jul1 = PPath.createRectangle(0,0, (float) quarter1.getWidth()/3, (float) quarter1.getHeight()+1);
		PPath aug1 = PPath.createRectangle(0,0, (float) quarter1.getWidth()/3, (float) quarter1.getHeight()+1);
		PPath sep1 = PPath.createRectangle(0,0, (float) quarter1.getWidth()/3, (float) quarter1.getHeight()+1);
		PPath oct1 = PPath.createRectangle(0,0, (float) quarter1.getWidth()/3, (float) quarter1.getHeight()+1);
		PPath nov1 = PPath.createRectangle(0,0, (float) quarter1.getWidth()/3, (float) quarter1.getHeight()+1);
		PPath dec1 = PPath.createRectangle(0,0, (float) quarter1.getWidth()/3, (float) quarter1.getHeight()+1);

		jan1.setPaint(Color.orange);
		feb1.setPaint(Color.orange);
		mar1.setPaint(Color.orange);
		apr1.setPaint(Color.cyan);
		may1.setPaint(Color.cyan);
		jun1.setPaint(Color.cyan);
		jul1.setPaint(Color.green);
		aug1.setPaint(Color.green);
		sep1.setPaint(Color.green);
		oct1.setPaint(Color.yellow);
		nov1.setPaint(Color.yellow);
		dec1.setPaint(Color.yellow);


		jan1.offset(50, 50);
		jan1.setBounds(700, 700, 50, 50);

		jan1.addChild(new PText("January"));
		((PText) jan1.getChild(0)).setConstrainWidthToTextWidth(false);
		((PText) jan1.getChild(0)).setWidth(jan1.getWidth());
		((PText) jan1.getChild(0)).offset(100, 100);
		feb1.addChild(new PText("February"));
		mar1.addChild(new PText("March"));
		apr1.addChild(new PText("April"));
		may1.addChild(new PText("May"));
		jun1.addChild(new PText("June"));
		jul1.addChild(new PText("July"));
		aug1.addChild(new PText("August"));
		sep1.addChild(new PText("September"));
		oct1.addChild(new PText("October"));
		nov1.addChild(new PText("November"));
		dec1.addChild(new PText("December"));

		jan1.setVisible(false);
		feb1.setVisible(false);
		mar1.setVisible(false);
		apr1.setVisible(false);
		may1.setVisible(false);
		jun1.setVisible(false);
		jul1.setVisible(false);
		aug1.setVisible(false);
		sep1.setVisible(false);
		oct1.setVisible(false);
		nov1.setVisible(false);
		dec1.setVisible(false);

		jan1.offset(0, (float) year1.getHeight());
		feb1.offset(0, (float) year1.getHeight());
		mar1.offset(0, (float) year1.getHeight());
		apr1.offset(0, (float) year1.getHeight());
		may1.offset(0, (float) year1.getHeight());
		jun1.offset(0, (float) year1.getHeight());
		jul1.offset(0, (float) year1.getHeight());
		aug1.offset(0, (float) year1.getHeight());
		sep1.offset(0, (float) year1.getHeight());
		oct1.offset(0, (float) year1.getHeight());
		nov1.offset(0, (float) year1.getHeight());
		dec1.offset(0, (float) year1.getHeight());


		PText tq1 = new PText("Jan-Mar");
		PText tq2 = new PText("Apr-Jun");
		PText tq3 = new PText("Jul-Sep");
		PText tq4 = new PText("Oct-Dec");

		quarter1.addChild(tq1);
		quarter2.addChild(tq2);
		quarter3.addChild(tq3);
		quarter4.addChild(tq4);

		n1.addChild(year1);
		n1.addChild(quarter1);
		n1.addChild(quarter2);
		n1.addChild(quarter3);
		n1.addChild(quarter4);
		n1.addChild(jan1);
		n1.addChild(feb1);
		n1.addChild(mar1);
		n1.addChild(apr1);
		n1.addChild(may1);
		n1.addChild(jun1);
		n1.addChild(jul1);
		n1.addChild(aug1);
		n1.addChild(sep1);
		n1.addChild(oct1);
		n1.addChild(nov1);
		n1.addChild(dec1);

		year1.setX(TimeLineMain.DEFAULT_WIDTH/2/numOfYears);




		final PPath n2 = PPath.createEllipse(100, 100, 200, 34);

		n1.addInputEventListener(new PBasicInputEventHandler() {
			public void mouseClicked(PInputEvent event){
				if (false){
					n1.setStroke(new BasicStroke(5));
					n1.setStrokePaint(Color.red);

					n1.animateToBounds(camera.getViewBounds().getX(), camera.getViewBounds().getY(), camera.getViewBounds().getWidth(), camera.getViewBounds().getHeight(), 750);
					n1.getChild(0).animateTransformToBounds(camera.getViewBounds().getWidth()/2, camera.getViewBounds().getY(), n1.getChild(0).getHeight()+10,n1.getChild(0).getWidth()+10 , 750);
					n2.centerBoundsOnPoint(camera.getViewBounds().getCenterX(), camera.getViewBounds().getCenterY());
					n1.getChild(1).animateToBounds(camera.getViewBounds().getX(), n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(2).animateToBounds(camera.getViewBounds().getWidth()/4-100 , n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(3).animateToBounds(camera.getViewBounds().getWidth()/4*2-200, n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(4).animateToBounds(camera.getViewBounds().getWidth()/4*3-300, n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4, n1.getChild(0).getHeight()*2, 500);

					n1.getChild(1).setVisible(false);
					n1.getChild(2).setVisible(false);
					n1.getChild(3).setVisible(false);
					n1.getChild(4).setVisible(false);

					n1.getChild(5).setVisible(true);
					n1.getChild(6).setVisible(true);
					n1.getChild(7).setVisible(true);
					n1.getChild(8).setVisible(true);
					n1.getChild(9).setVisible(true);
					n1.getChild(10).setVisible(true);
					n1.getChild(11).setVisible(true);
					n1.getChild(12).setVisible(true);
					n1.getChild(13).setVisible(true);
					n1.getChild(14).setVisible(true);
					n1.getChild(15).setVisible(true);
					n1.getChild(16).setVisible(true);

					n1.getChild(5).animateToBounds(camera.getViewBounds().getX(), n1.getChild(0).getHeight()+100, camera.getViewBounds().getWidth()/4/3, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(6).animateToBounds(camera.getViewBounds().getWidth()/4/3 , n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4/3, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(7).animateToBounds(camera.getViewBounds().getWidth()/4/3*2, n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4/3, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(8).animateToBounds(camera.getViewBounds().getWidth()/4/3*3, n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4/3, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(9).animateToBounds(camera.getViewBounds().getWidth()/4/3*4 , n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4/3, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(10).animateToBounds(camera.getViewBounds().getWidth()/4/3*5, n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4/3, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(11).animateToBounds(camera.getViewBounds().getWidth()/4/3*6, n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4/3, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(12).animateToBounds(camera.getViewBounds().getWidth()/4/3*7 , n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4/3, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(13).animateToBounds(camera.getViewBounds().getWidth()/4/3*8, n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4/3, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(14).animateToBounds(camera.getViewBounds().getWidth()/4/3*9, n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4/3, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(15).animateToBounds(camera.getViewBounds().getWidth()/4/3*10 , n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4/3, n1.getChild(0).getHeight()*2, 500);
					n1.getChild(16).animateToBounds(camera.getViewBounds().getWidth()/4/3*11, n1.getChild(0).getHeight()+1, camera.getViewBounds().getWidth()/4/3, n1.getChild(0).getHeight()*2, 500);

					n1.getChild(5).getChild(0).offset(camera.getViewBounds().getX(), n1.getChild(0).getHeight()+5);
					n1.getChild(6).getChild(0).offset(camera.getViewBounds().getWidth()/4/3, n1.getChild(0).getHeight()+5);
					n1.getChild(7).getChild(0).offset(camera.getViewBounds().getWidth()/4/3*2, n1.getChild(0).getHeight()+5);
					n1.getChild(8).getChild(0).offset(camera.getViewBounds().getWidth()/4/3*3, n1.getChild(0).getHeight()+5);
					n1.getChild(9).getChild(0).offset(camera.getViewBounds().getWidth()/4/3*4, n1.getChild(0).getHeight()+5);
					n1.getChild(10).getChild(0).offset(camera.getViewBounds().getWidth()/4/3*5, n1.getChild(0).getHeight()+5);
					n1.getChild(11).getChild(0).offset(camera.getViewBounds().getWidth()/4/3*6, n1.getChild(0).getHeight()+5);
					n1.getChild(12).getChild(0).offset(camera.getViewBounds().getWidth()/4/3*7, n1.getChild(0).getHeight()+5);
					n1.getChild(13).getChild(0).offset(camera.getViewBounds().getWidth()/4/3*8, n1.getChild(0).getHeight()+5);
					n1.getChild(14).getChild(0).offset(camera.getViewBounds().getWidth()/4/3*9, n1.getChild(0).getHeight()+5);
					n1.getChild(15).getChild(0).offset(camera.getViewBounds().getWidth()/4/3*10, n1.getChild(0).getHeight()+5);
					n1.getChild(16).getChild(0).offset(camera.getViewBounds().getWidth()/4/3*11, n1.getChild(0).getHeight()+5);


					yearView = true;
				}
			}
		});


		n2.setStroke(new PFixedWidthStroke());

		//		n3.setStroke(new PFixedWidthStroke());
		// n3.setStroke(null);
 */

/*static class YearNode extends PNode {}
	static class MonthNode extends PNode {}
	static class WeekNode extends PNode {}

	static class DayNode extends PNode {
		boolean hasWidthFocus;
		boolean hasHeightFocus;
		ArrayList lines;
		int week;
		int day;
		String dayOfMonthString;

		public DayNode(int week, int day) {	
			lines = new ArrayList();
			lines.add("7:00 AM Walk the dog.");
			lines.add("9:30 AM Meet John for Breakfast.");
			lines.add("12:00 PM Lunch with Peter.");
			lines.add("3:00 PM Research Demo.");
			lines.add("6:00 PM Pickup Sarah from gymnastics.");
			lines.add("7:00 PM Pickup Tommy from karate.");
			this.week = week;
			this.day = day;
			this.dayOfMonthString = Integer.toString((week * 7) + day);
			setPaint(Color.BLACK);
		}

		public int getWeek() {
			return week;
		}

		public int getDay() {
			return day;
		}

		public boolean hasHeightFocus() {
			return hasHeightFocus;
		}

		public void setHasHeightFocus(boolean hasHeightFocus) {
			this.hasHeightFocus = hasHeightFocus;
		}

		public boolean hasWidthFocus() {
			return hasWidthFocus;
		}

		public void setHasWidthFocus(boolean hasWidthFocus) {
			this.hasWidthFocus = hasWidthFocus;
		}

		protected void paint(PPaintContext paintContext) {
			Graphics2D g2 = paintContext.getGraphics();
			g2.setPaint(getPaint());
			g2.draw(getBoundsReference());


			float y = (float) getY()  ;
			paintContext.getGraphics().drawString(dayOfMonthString, (float) getX() , y);
			if (hasWidthFocus && hasHeightFocus) {
				paintContext.pushClip(getBoundsReference());
				for (int i = 0; i < lines.size(); i++) {
					y += 10;
					g2.drawString((String)lines.get(i), (float) getX()  , y);				
				}
				paintContext.popClip(getBoundsReference());
			}
		}
	}	

			//Set up the timeline grid for 3 years 2009, 2010, 2011
//		PPath year2009 = PPath.createRectangle(0, 0, yearWidth, DEFAULT_TEXT_HEIGHT);
//		PText year2009Label = new PText("2009");
//		year2009.addChild(year2009Label);
//		year2009Label.setBounds(yearWidth/2-5, 0, 20, DEFAULT_TEXT_HEIGHT);
//		System.out.println(year2009.getBounds());
//		System.out.println(year2009Label.getBounds());
//
//		final PPath year2010 = PPath.createRectangle(yearWidth, 0, yearWidth, DEFAULT_TEXT_HEIGHT);
//		PText year2010Label = new PText("2010");
//		year2010.addChild(year2010Label);
//		year2010Label.setBounds(3*yearWidth/2-5, 0, 20, DEFAULT_TEXT_HEIGHT);
//		System.out.println(year2010.getBounds());
//		System.out.println(year2010Label.getBounds());
//
//		final PPath year2011 = PPath.createRectangle(2*yearWidth, 0, yearWidth, DEFAULT_TEXT_HEIGHT);
//		PText year2011Label = new PText("2011");
//		year2011.addChild(year2011Label);
//		year2011Label.setBounds(5*yearWidth/2-5, 0, 20, DEFAULT_TEXT_HEIGHT);
//		System.out.println(year2011.getBounds());
//		System.out.println(year2011Label.getBounds());

		//		layer.addChild(year2009);
		//		layer.addChild(year2010);
		//		layer.addChild(year2011);
 */

