package org.processmining.multilayeralignment.plugins.visualization.sequence;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XTrace;
import org.processmining.multilayeralignment.models.mlreplay.StepTypes;
import org.processmining.multilayeralignment.models.mlreplay.StepTypesVOL;
import org.processmining.multilayeralignment.plugins.visualization.tracealignment.MultiLayerAlignmentConstants;
import org.processmining.plugins.petrinet.visualization.AlignmentConstants;;


public class MultiLayerInstanceConformanceView extends JComponent implements MouseListener, MouseMotionListener {
	
	private static final long serialVersionUID = -3966399549433794592L;
	protected static Color colorAttenuationDark = new Color(0, 0, 0, 160);
	protected static Color colorAttenuationBright = new Color(0, 0, 0, 80);
	protected static Color colorBgInstanceflag = new Color(70, 70, 70, 210);
	protected static Color colorBgEventFlag = new Color(30, 30, 30, 200);

	protected static DecimalFormat format = new DecimalFormat("##0.00%");
	protected static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

	protected static int trackPadding = 80;
	protected static int trackY = 40;
	protected static int trackHeight = 40;
	protected final int elementWidth;
	protected static int elementTriOffset = 6;
	protected int shiftX = 4, shiftY = 2;

	protected XLogInfo info;
	protected String traceLabel;
	protected XTrace processTrace;
	protected XTrace dataTrace;
	protected int maxOccurrenceCount;
	protected XTrace instance;
	protected boolean mouseOver = false;
	protected int mouseX;
	protected int mouseY;

	// modification
	protected List<Object> nodeInstance = null;
	protected List<StepTypes> stepTypes = null;
	protected List<Object> nodeInstanceVOL = null;
	protected List<StepTypesVOL> stepTypesVOL = null;
	
	protected List<Object> rearrangedNodeInstance = new LinkedList<Object>();
	protected List<Object> rearrangedNodeInstanceVOL = new LinkedList<Object>();
	protected List<StepTypesVOL> rearrangedStepTypesVOL = new LinkedList<StepTypesVOL>();
	protected List<String> readList =new ArrayList<String>();
	//order DLM Moves
	protected List<Object> OrderedNodeInstanceVOL = new LinkedList<Object>();
	protected List<StepTypesVOL> OrderedStepTypesVOL = new LinkedList<StepTypesVOL>();
	
	//process eventId
	static Map<Integer,Integer> AlignmentProcessEventIDMap1= new HashMap<Integer, Integer>(); 
	
	
	

	
	public MultiLayerInstanceConformanceView(String traceLabel, List<Object> nodeInstance, List<StepTypes> stepTypes, List<Object> nodeInstanceVOL, List<StepTypesVOL> stepTypesVOL,
			List<Object> rearrangedNodeInstances,int elementWidth,XTrace processTrace,XTrace dataTrace) {
		this.elementWidth = elementWidth;
		this.traceLabel = traceLabel;
		this.processTrace= processTrace;
		this.dataTrace= dataTrace;
		this.nodeInstance = nodeInstance;
		this.stepTypes = stepTypes;
		this.nodeInstanceVOL = nodeInstanceVOL;
		this.stepTypesVOL = stepTypesVOL;
		this.rearrangedNodeInstance=rearrangedNodeInstances;

		rearrange(traceLabel, nodeInstance, stepTypes, nodeInstanceVOL, stepTypesVOL,rearrangedNodeInstances);
		addMouseListener(this);
		addMouseMotionListener(this);
		int width = (nodeInstanceVOL.size() * elementWidth) + trackPadding + 300;
		setMinimumSize(new Dimension(width, 120));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
		setPreferredSize(new Dimension(width, 120));
		setDoubleBuffered(true);


	}

	public MultiLayerInstanceConformanceView(String traceLabel, List<Object> nodeInstance, List<StepTypes> stepTypes, List<Object> nodeInstanceVOL, List<StepTypesVOL> stepTypesVOL,List<Object> rearrangedNodeInstances
			,XTrace processTrace,XTrace dataTrace) {
		this(traceLabel, nodeInstance, stepTypes, nodeInstanceVOL, stepTypesVOL,rearrangedNodeInstances,5,processTrace,dataTrace);
	}
	
	@Override
	protected void paintComponent(Graphics g) {

		Rectangle clip = getVisibleRect();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// draw background
		g2d.setColor(new Color(30, 30, 30));
		g2d.fillRect(clip.x, clip.y, clip.width, clip.height);
		// determine active event
		int activeEvent = -1;
		if (mouseOver == true) {
			activeEvent = mapEventIndex(mouseX, mouseY);
		}
		// draw events
		int clipRightX = clip.x + clip.width;
		int trackRightX;
		if (OrderedNodeInstanceVOL != null) {
			trackRightX = trackPadding + (OrderedNodeInstanceVOL.size() * elementWidth);
		} else {
			trackRightX = trackPadding + (instance.size() * elementWidth);
		}
		int startX = clip.x - (clip.x % elementWidth); // shift to left if necessary
		if (startX < trackPadding) {
			startX = trackPadding;
		}
		int eventIndex = (startX - trackPadding) / elementWidth;
		for (int x = startX; ((x < clipRightX) && (x < trackRightX)); x += elementWidth) {
						
			drawEvent(g2d, eventIndex, (eventIndex == activeEvent), x, trackY, elementWidth, trackHeight);
			eventIndex++;
		}
		if (clip.x <= trackRightX) {
			// draw instance flag
			drawInstanceFlag(g2d, clip.x, 25, 35);
			// draw event flag
			if (activeEvent >= 0) {
				int eventX = trackPadding + (activeEvent * elementWidth);
				drawEventFlag(g2d, activeEvent, eventX, 5, 30);
			}
		}

	}
	
	protected int mapEventIndex(int x, int y) {
		if ((y >= trackY) && (y <= (trackY + trackHeight))) {

			x -= trackPadding;
			x /= elementWidth;
			if (OrderedNodeInstanceVOL != null) {
				if ((x >= 0) && (x < OrderedNodeInstanceVOL.size())) {
					return x;
				} else {
					return -1;
				}
			} else {
				if ((x >= 0) && (x < instance.size())) {
					return x;
				} else {
					return -1;
				}
			}
		} else {
			return -1;
		}

	}
	
	protected void drawInstanceFlag(Graphics2D g2d, int x, int y, int height) {

		String size;
		if (OrderedNodeInstanceVOL != null) {
			size = OrderedNodeInstanceVOL.size() + " Moves";
		} else {
			size = instance.size() + " Moves";
		}
		// calculate width
		g2d.setFont(g2d.getFont().deriveFont(11f));
		FontMetrics fm = g2d.getFontMetrics();
		int nameWidth = fm.stringWidth(traceLabel);
		int sizeWidth = fm.stringWidth(size);
		int width = (nameWidth > sizeWidth) ? nameWidth + 15 : sizeWidth + 15;
		width = Math.max(width, trackPadding - 10);
		// draw flag shadow
		int shadowOffset = 4;
		int[] xSCoords = new int[] { x, x + width - elementTriOffset + shadowOffset, x + width + shadowOffset,
				x + width - elementTriOffset + shadowOffset, x };
		int[] ySCoords = new int[] { y + shadowOffset, y + shadowOffset, y + (height / 2) + shadowOffset,
				y + height + shadowOffset, y + height + shadowOffset };
		g2d.setColor(new Color(0, 0, 0, 100));
		g2d.fillPolygon(xSCoords, ySCoords, 5);
		// draw flag background
		g2d.setColor(colorBgInstanceflag);
		int[] xCoords = new int[] { x, x + width - elementTriOffset, x + width, x + width - elementTriOffset, x };
		int[] yCoords = new int[] { y, y, y + (height / 2), y + height, y + height };
		g2d.fillPolygon(xCoords, yCoords, 5);
		// draw string
		int fontHeight = fm.getHeight();
		int fontOffset = (height - fontHeight - fontHeight) / 3;
		g2d.setColor(new Color(220, 220, 220));
		//g2d.drawString("Process Instance:", x + 5, y + fontOffset + fontHeight - 1);
		g2d.drawString(traceLabel, x + 5, y + fontOffset + fontHeight - 1);
		g2d.setColor(new Color(200, 200, 200));
		g2d.drawString(size, x + 5, y + height - fontOffset - 3);
	}

	protected void drawEventFlag(Graphics2D g2d, int index, int x, int y, int height) throws IndexOutOfBoundsException {
		if (OrderedNodeInstanceVOL!= null) {
			
			Map<Integer,Integer> AlignmentProcessEventIDMap= new HashMap<Integer, Integer>(); 
	    	int j=0;
	    	for(int i=0; i<OrderedNodeInstanceVOL.size();i++) {
	    		
	    		if(!(OrderedStepTypesVOL.get(i).equals(StepTypesVOL.MM) || OrderedStepTypesVOL.get(i).equals(StepTypesVOL.DLM) || OrderedStepTypesVOL.get(i).equals(StepTypesVOL.TM))) {
	    			AlignmentProcessEventIDMap.put(i, j);
	    			j=j+1;
	    		}else {
	    			AlignmentProcessEventIDMap.put(i, null);
	    		    j=j;
	    		}
	    	}
			
			String name=OrderedNodeInstanceVOL.get(index).toString(); 
			String ProcessInfo=name.substring(0,OrderedNodeInstanceVOL.get(index).toString().indexOf("+")-1);
			ProcessInfo=ProcessInfo.replace("|", "_");
			
			String AllDataOperations=name.substring(name.indexOf("+"),name.length());
			if(AllDataOperations.contains("TSM")&& AllDataOperations.contains("PSM")) {
				AllDataOperations=name.substring(name.indexOf("+")+4,name.length()); //preparing for counting removing first ":" to find PSM part correctly
			}
			long countTSM=0;
			long countPSM=0;
			
			
				String TSMOPRs=AllDataOperations.substring(0,AllDataOperations.indexOf(":"));
				String PSMOPRs=AllDataOperations.substring(AllDataOperations.indexOf(":"),AllDataOperations.length() );
				countTSM = TSMOPRs.chars().filter(ch -> ch == '+').count()+1;
				countPSM = PSMOPRs.chars().filter(ch -> ch == '+').count();
				
			

			String processEvent_Activity="";
			String roleInfo="";
			String processEvent_Actor="";
			
			
			//retrieving process eventID
			if(!(OrderedStepTypesVOL.get(index).equals(StepTypesVOL.MM) || OrderedStepTypesVOL.get(index).equals(StepTypesVOL.DLM) ||OrderedStepTypesVOL.get(index).equals(StepTypesVOL.TM))) {
				
				int processEventID= AlignmentProcessEventIDMap.get(index);
				processEvent_Activity= processTrace.get(processEventID).getAttributes().get("concept:name").toString();
				processEvent_Actor=processTrace.get(processEventID).getAttributes().get("org:resource").toString();
			}
			
			String dataInfo="";
			String numOfDO_onModel="";
			String numOfExpectedDO="";
			String numOfMissingDO="";
			long count = name.chars().filter(ch -> ch == '+').count();

			if(OrderedStepTypesVOL.get(index).equals(StepTypesVOL.TSM)) {
				roleInfo="Legitimate Role";
				numOfExpectedDO=String.valueOf(count);
				dataInfo=  "# Expected D.O="+" "+numOfExpectedDO;
			
			}
			else if(OrderedStepTypesVOL.get(index).equals(StepTypesVOL.TSMP)) {
				roleInfo="Illegitimate Role, Actore: "+processEvent_Actor;
				numOfExpectedDO=String.valueOf(count);
				numOfMissingDO="0";
				dataInfo=  "# Expected D.O="+" "+numOfExpectedDO;
			
			}
			else if(OrderedStepTypesVOL.get(index).equals(StepTypesVOL.PSM)) {
				roleInfo="Legitimate Role";
				numOfExpectedDO="0";
				numOfMissingDO=String.valueOf(count);
				dataInfo="# Missing D.O="+" "+numOfMissingDO;
			}
			else if(OrderedStepTypesVOL.get(index).equals(StepTypesVOL.PSMP)) {
				roleInfo="Illegitimate Role, Actore: "+processEvent_Actor;
				numOfExpectedDO="0";
				numOfMissingDO=String.valueOf(count);
				dataInfo="# Missing D.O="+" "+numOfMissingDO;
			}
			else if(OrderedStepTypesVOL.get(index).equals(StepTypesVOL.PLM)) {
				roleInfo="Illegitimate Role, Actore: "+processEvent_Actor;
				ProcessInfo="Unexpected Activity: "+ processEvent_Activity;
				dataInfo="N/A";
				
			}
			else if(OrderedStepTypesVOL.get(index).equals(StepTypesVOL.DLM)) {
				//extract index of data event
				String index1= ProcessInfo.substring(0, ProcessInfo.indexOf("_"));
				int dataEventID= Integer.parseInt(index1);
				//extract dataEvent Info from DataLog- note that there is a connection with DataLog Object in the visualization plugin.
				roleInfo= "Illegitimate Role, Actore: "+ dataTrace.get(dataEventID).getAttributes().get("org:resource").toString();
				ProcessInfo="Unknown context";
				dataInfo= "Unexpected D.O.: "+ dataTrace.get(dataEventID).getAttributes().get("concept:name").toString();
			}
			else if(OrderedStepTypesVOL.get(index).equals(StepTypesVOL.MM)) {
				roleInfo="N/A";
				ProcessInfo="Missing Activity: "+ProcessInfo.substring(2,ProcessInfo.length());
				numOfExpectedDO="0";
				numOfMissingDO=String.valueOf(count);
				dataInfo="# Missing D.O="+" "+numOfMissingDO;
			}
			
			else if(OrderedStepTypesVOL.get(index).equals(StepTypesVOL.TSMPSM) || OrderedStepTypesVOL.get(index).equals(StepTypesVOL.PSMTSM)) {
				roleInfo="Legitimate Role";
				numOfExpectedDO=String.valueOf(countTSM);
				numOfMissingDO=String.valueOf(countPSM);
				dataInfo="# Expected D.O="+" "+numOfExpectedDO+", "+"# Ignored  D.O="+" "+numOfMissingDO;
			}
			
			else if(OrderedStepTypesVOL.get(index).equals(StepTypesVOL.TSMPPSMP) || OrderedStepTypesVOL.get(index).equals(StepTypesVOL.PSMPTSMP)) {
				roleInfo="Illegitimate Role, Actore: "+processEvent_Actor;
				numOfExpectedDO=String.valueOf(countTSM);
				numOfMissingDO=String.valueOf(countPSM);
				dataInfo="# Expected D.O="+" "+numOfExpectedDO+", "+"# Ignored D.O="+" "+numOfMissingDO;
			}
			else
			   dataInfo= "unknown";
				
			
			drawMultiLineFlag(g2d, getColor(OrderedStepTypesVOL.get(index)), x, y, height, roleInfo, ProcessInfo, dataInfo);
		} else {
			drawXEventFlag(g2d, index, x, y, height);
		}
	}
	
	
	protected void drawEvent(Graphics2D g2d, int index, boolean active, int x, int y, int width, int heightThis) {
		Color moveColor;

		if (OrderedNodeInstanceVOL != null) {
			// set correct color for event
			moveColor = getColorOfIndex(index);
		} else {

			moveColor =MultiLayerAlignmentConstants.MOVEMODELCOLOR ;
		}

		
		int height = 40 - 2- 1;
		int midPointBX = x + elementTriOffset;
		int midPointAX = x + width + elementTriOffset;
		int midPointY = y + (height / 2);
		Color prColor, pColor,dColor,dColor2;
		String moveType="";
		
        if(moveColor.equals(Color.green)){
        	moveType="TSM";
        	prColor=Color.green;
        	pColor=Color.green;
        	dColor= Color.green;
        	drawGroup1(g2d,moveType,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, prColor,pColor,dColor);
		}
        else if(moveColor.equals(Color.orange)){
        	moveType="TSMP";
        	prColor=Color.red;
        	pColor=Color.green;
        	dColor= Color.green;
        	drawGroup1(g2d,moveType, moveColor, index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, prColor,pColor,dColor);
		}
        else if(moveColor.equals(Color.magenta)){
        	moveType="PSM";
        	prColor=Color.green;
        	pColor=Color.green;
        	dColor= Color.red;
        	drawGroup1(g2d,moveType, moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, prColor,pColor,dColor);
		}
        else if(moveColor.equals(Color.gray)){
        	moveType="PSMP";
        	prColor=Color.red;
        	pColor=Color.green;
        	dColor= Color.red;
        	drawGroup1(g2d,moveType ,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, prColor,pColor,dColor);
		}
        else if(moveColor.equals(Color.cyan)){
        	moveType="MM";
        	prColor=Color.red;
        	pColor=Color.red;
        	dColor= Color.red;
        	drawGroup1(g2d,moveType ,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, prColor,pColor,dColor);
		}
        else if(moveColor.equals(Color.yellow)){
        	moveType="PLM";
        	prColor=Color.red;
        	pColor=Color.red;
        	drawGroup2(g2d,moveType,moveColor ,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, prColor,pColor);
		}
        else if(moveColor.equals(Color.pink)){
        	moveType="DLM";
        	prColor=Color.red;
        	dColor=Color.red;
        	drawGroup3(g2d,moveType ,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, prColor,dColor);
		}
        else if(moveColor.equals(Color.white)){
        	moveType="TSMPSM";
        	prColor=Color.green;
        	pColor=Color.green;
        	dColor=Color.green;
        	dColor2=Color.red;
        	drawGroup4(g2d,moveType ,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, prColor,pColor,dColor,dColor2);
		}
        else if(moveColor.equals(Color.white)){
        	moveType="PSMTSM";
        	prColor=Color.green;
        	pColor=Color.green;
        	dColor=Color.green;
        	dColor2=Color.red;
        	drawGroup4(g2d,moveType ,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, prColor,pColor,dColor,dColor2);
		}
        else if(moveColor.equals(Color.black)){
        	moveType="TSMPPSMP";
        	prColor=Color.red;
        	pColor=Color.green;
        	dColor=Color.green;
        	dColor2=Color.red;
        	drawGroup4(g2d,moveType ,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, prColor,pColor,dColor,dColor2);
		}
        else if(moveColor.equals(Color.black)){
        	moveType="PSMPTSMP";
        	prColor=Color.red;
        	pColor=Color.green;
        	dColor=Color.green;
        	dColor2=Color.red;
        	drawGroup4(g2d,moveType ,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, prColor,pColor,dColor,dColor2);
		}
        else {
        	prColor=Color.white;
        	pColor=Color.white;
        	dColor= Color.white;
        	drawGroup1(g2d,moveType ,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, prColor,pColor,dColor);
        }

	}
	
	protected void drawGroup1(Graphics2D g2d,String moveType,Color moveColor ,int index, boolean active, int x, int y, int midPointBX,int midPointAX,int midPointY,int width, int height,
			Color prColor,Color pColor,Color dColor) {
		    Color WhitColor= Color.white;
		    Color pColorMM= new Color(102, 0 ,153) ;
		    
		       if (active == false) {
		    	   prColor= attenuateColor(prColor);
		    	   pColor= attenuateColor(pColor);
		    	   dColor= attenuateColor(dColor);
		    	   WhitColor = attenuateColor(WhitColor);
		    	  
		    	   
		       }
		       GradientPaint redtowhite = new GradientPaint(5, 5,pColor, 20, 20, WhitColor, true);
		       GradientPaint redtowhite2 = new GradientPaint(25, 25,dColor, 15, 25, WhitColor, true);

		      
		       
		       if(moveType.equalsIgnoreCase("MM")) {
		    	    g2d.setPaint(redtowhite2);
		       }else {
		        g2d.setColor(prColor);
		       }
				g2d.fillOval(x + 9 , y, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 9 , y, 16, 16);
				
				//process layer
				midPointY+= 18;
				y+= 18;
				
				int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
				int[] yCoords = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				if (active == true) {
					for (int i = 0; i < xCoords.length; i++) {
						xCoords[i] -= 1;
						yCoords[i] -= 3;
					}
				}
				
				if (active == true) {
			    	   if(moveType.equalsIgnoreCase("MM")) {
				    	    g2d.setPaint(redtowhite2);
				       }
			    	   else
			    	   g2d.setColor(pColor);   
			       }else 
			       {   if(moveType.equalsIgnoreCase("MM")) {
			    	    g2d.setPaint(pColorMM);
			           }else
			    	   g2d.setColor(pColor); 
			       }
				
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				
				//Data Layer
				y+= 40;
				int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
				int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
		       
				if(moveType.equalsIgnoreCase("MM") || moveType.equalsIgnoreCase("PSM") || moveType.equalsIgnoreCase("PSMP")) {
		    	    g2d.setPaint(redtowhite2);
		       }else {
		            g2d.setColor(dColor);
		       }
		       
				
				g2d.fillPolygon(xCoords2, yCoords2, 3);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2, 3);	
		
	}
	
	protected void drawGroup2(Graphics2D g2d,String moveType, Color moveColor ,int index, boolean active, int x, int y, int midPointBX,int midPointAX,int midPointY,int width, int height,
			Color prColor,Color pColor) {
		       Color pColor2= Color.yellow;
		       if (active == false) {
		    	   prColor= attenuateColor(prColor);
		    	 
		       }
		       
		        g2d.setColor(prColor);
				g2d.fillOval(x + 9 , y, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 9 , y, 16, 16);
				
				//process layer
				midPointY+= 18;
				y+= 18;
				
				int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
				int[] yCoords = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				if (active == true) {
					for (int i = 0; i < xCoords.length; i++) {
						xCoords[i] -= 1;
						yCoords[i] -= 3;
					}
				}
				
				if (active == true) {
			    	   g2d.setColor(pColor);
			    	   
			       }else 
			    	   g2d.setColor(pColor2); 
			    	   
			
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
         
	}
	
	protected void drawGroup3(Graphics2D g2d,String moveType , Color moveColor, int index, boolean active, int x, int y, int midPointBX,int midPointAX,int midPointY,int width, int height,
			Color prColor,Color dColor) {
		       
		 if (active == false) {
	    	   prColor= attenuateColor(prColor);
	    	   dColor= attenuateColor(dColor);
	       }
	       

	       
	        g2d.setColor(prColor);
			g2d.fillOval(x + 12 , y, 16, 16);
			g2d.setColor(Color.black);
			g2d.drawOval(x + 12 , y, 16, 16);
			int y1=y+1;
			//process layer
			midPointY+= 18;
			y+= 18;
			
			//Data Layer
			y+= 40;
			int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
			int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
	        
			g2d.setColor(dColor); 
			g2d.fillPolygon(xCoords2, yCoords2, 3);
			g2d.setColor(Color.black);
			g2d.drawPolygon(xCoords2, yCoords2, 3);	
			
			//draw Line
			g2d.setColor(dColor);
			g2d.drawLine(x + width / 2, y1, x + width / 2, y);
			
	}
	
	protected void drawGroup4(Graphics2D g2d,String moveType, Color moveColor ,int index, boolean active, int x, int y, int midPointBX,int midPointAX,int midPointY,int width, int height,
			Color prColor,Color pColor,Color dColor,Color dColor2) {
		    Color WhitColor= Color.white; 
		       if (active == false) {
		    	   prColor= attenuateColor(prColor);
		    	   pColor= attenuateColor(pColor);
		    	   dColor= attenuateColor(dColor);
		    	   dColor2= attenuateColor(dColor2);
                   WhitColor = attenuateColor(WhitColor);
		    	   
		       }
		       
		       GradientPaint redtowhite = new GradientPaint(25, 25,dColor2, 15, 25, WhitColor, true);
		       
		       
		       
		        g2d.setColor(prColor);
				g2d.fillOval(x + 9 , y, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 9 , y, 16, 16);
				
				//process layer
				midPointY+= 18;
				y+= 18;
				
				int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
				int[] yCoords = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				if (active == true) {
					for (int i = 0; i < xCoords.length; i++) {
						xCoords[i] -= 1;
						yCoords[i] -= 3;
					}
				}
				
				g2d.setColor(pColor);
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				
				//Data Layer
				y+= 40;
				int[] xCoords2 = new int[] { x, x + width / 2-2 , x + width / 2-2 };
				int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
		        
				
				g2d.setColor(dColor); 
				g2d.fillPolygon(xCoords2, yCoords2, 3);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2, 3);		
				
				x+= width / 2+2;
				int[] xCoords3 = new int[] { x, x , x + width / 2, };
				int[] yCoords3 = new int[] { y + height / 2, y, y + (height / 2)};
		        
				
				
				g2d.setPaint(redtowhite);
				g2d.fillPolygon(xCoords3, yCoords3, 3);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords3, yCoords3, 3);	
		
	}

	protected void drawXEventFlag(Graphics2D g2d, int index, int x, int y, int height) {
		XExtendedEvent ate = new XExtendedEvent(instance.get(index));
		XEventClass eventClass = info.getEventClasses().getClassOf(instance.get(index));
		int occurrence = (eventClass != null ? eventClass.size() : 0);

		double frequency = (maxOccurrenceCount == 0 ? 0.0 : (double) occurrence / (double) maxOccurrenceCount);

		String ateName = (ate.getName() != null ? ate.getName() : "<no name>");
		String ateTransition = (ate.getTransition() != null ? ate.getTransition() : "<no transition>");
		String ateResource = (ate.getResource() != null ? ate.getResource() : "<no resource>");
		String name = index + ": " + ateName + " (" + ateTransition + ")";
		String originator = ateResource + "; freq: " + format.format(frequency);
		Date ts = ate.getTimestamp();
		String timestamp;
		if (ts != null) {
			timestamp = dateFormat.format(ate.getTimestamp());
		} else {
			timestamp = "<no timestamp>";
		}
		drawMultiLineFlag(g2d, AlignmentConstants.MOVESYNCCOLOR, x, y, height, name, originator, timestamp);
	}
	
	protected void drawMultiLineFlag(Graphics2D g2d, Color color, int x, int y, int height, String... labels)
	
	{
		// calculate width
		g2d.setFont(g2d.getFont().deriveFont(11f));
		FontMetrics fm = g2d.getFontMetrics();
		int width = 0;
		for (String s : labels) {
			if (s != null) {
				int w = fm.stringWidth(s) + 10;
				if (w > width) {
					width = w;
				}
			}
		}

		// draw background
		g2d.setColor(colorBgEventFlag);
		g2d.fillRect(x, y, width, height);
		// set color
		g2d.setColor(color);
		// draw anchor line
		g2d.drawLine(x, y, x, y + height);
		// draw strings
		int fontHeight = fm.getHeight();
		int fontOffset = (height - fontHeight * labels.length) / (labels.length + 1);
		y += 3;
		for (String s : labels) {
			if (s != null) {
				g2d.drawString(s, x + 5, y);
				y += fontHeight + fontOffset;
			}
		}
	}
	

	protected Color getColor(StepTypesVOL OrderedStepTypesVOL) {
		switch (OrderedStepTypesVOL) {
			case PLM :
				return MultiLayerAlignmentConstants.MOVEPROCESSLOGCOLOR;
			case DLM :
				return MultiLayerAlignmentConstants.MOVEDATALOGCOLOR;
			case TSM :
				return MultiLayerAlignmentConstants.MOVETOTALLYCOLOR;
			case TSMP:
				return MultiLayerAlignmentConstants.MOVETOTALLYT2COLOR;
			case PSM :
				return MultiLayerAlignmentConstants.MOVEPARTIALLYCOLOR;
			case PSMP :
				return MultiLayerAlignmentConstants.MOVEPARTIALLYT2COLOR;
			case MM :
				return MultiLayerAlignmentConstants.MOVEMODELCOLOR;
			case TSMPSM :
				return MultiLayerAlignmentConstants.MOVETSMPSMCOLOR;
			case PSMTSM :
				return MultiLayerAlignmentConstants.MOVEPSMTSMCOLOR;
			case TSMPPSMP :
				return MultiLayerAlignmentConstants.MOVETSMPPSMPCOLOR;
			case PSMPTSMP :
				return MultiLayerAlignmentConstants.MOVEPSMPTSMPCOLOR;
			default :
				return Color.cyan; // unknown
		}
	}
	
	protected Color getColorOfIndex(int index) {
		return getColor(OrderedStepTypesVOL.get(index));
	}

	protected Color attenuateColor(Color color) {
		int red = (int) (color.getRed() * 0.5);
		int green = (int) (color.getGreen() * 0.5);
		int blue = (int) (color.getBlue() * 0.5);
		return new Color(red, green, blue);
	}
	
	public void mouseMoved(MouseEvent evt) {
		mouseX = evt.getX();
		mouseY = evt.getY();
		repaint();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
	 */
	public void mouseDragged(MouseEvent evt) {
		mouseX = evt.getX();
		mouseY = evt.getY();
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void rearrange(String traceLabel, List<Object> nodeInstance, List<StepTypes> stepTypes, List<Object> nodeInstanceVOL, List<StepTypesVOL> stepTypesVOL,List<Object> rearrangedNodeInstances) {
		OrderedNodeInstanceVOL.clear();
		OrderedStepTypesVOL.clear();
		
		readList.add("*test");
		for (Object obj : rearrangedNodeInstances) {
			String readMove= obj.toString();
			
			if(!checkReadList(traceLabel,readList,readMove) ){
				find(traceLabel,stepTypesVOL,nodeInstanceVOL,readMove);
				readList.add(readMove);
			}
			
			
		}
		
		rearrangeDLM( rearrangedNodeInstanceVOL, rearrangedStepTypesVOL);
	}
	
	public void rearrangeDLM(List<Object> rearrangedNodeInstanceVOLIn, List<StepTypesVOL> rearrangedStepTypesVOLIn) {
		// for reordering Data log moves
		 List<Object> DLMrearrangedNodeInstanceVOL= new LinkedList<Object>();
		 List<StepTypesVOL> DLMrearrangedStepTypesVOL = new LinkedList<StepTypesVOL>();
		
		for (int step=0; step < rearrangedStepTypesVOL.size(); step++) {
			
			if(rearrangedStepTypesVOL.get(step).equals(StepTypesVOL.DLM)) {
				DLMrearrangedNodeInstanceVOL.add(rearrangedNodeInstanceVOLIn.get(step));
				DLMrearrangedStepTypesVOL.add(rearrangedStepTypesVOLIn.get(step));
				
				rearrangedNodeInstanceVOL.remove(step);
				rearrangedStepTypesVOL.remove(step);
				
			}
			
			
		}

		List<Object> newInputNodesToSort= new LinkedList<Object>(rearrangedNodeInstanceVOL);
		List<StepTypesVOL> newInputSteptypesToSort=new LinkedList<StepTypesVOL>(rearrangedStepTypesVOL);
		
		for (int step=0; step < DLMrearrangedNodeInstanceVOL.size(); step++) {
			Object DLMNode = DLMrearrangedNodeInstanceVOL.get(step);
	
			String idofDataEvent=DLMNode.toString().substring(0,DLMNode.toString().indexOf("|"));
			int dataEventID= Integer.parseInt(idofDataEvent);
			//try/catch
			String dataEventTimeStamp= dataTrace.get(dataEventID).getAttributes().get("time:timestamp").toString();
			dataEventTimeStamp= dataEventTimeStamp.substring(0,dataEventTimeStamp.indexOf("+")).replace("T", " ");
			Date oprEvent = null;
			try {
				oprEvent = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
				        .parse(dataEventTimeStamp);
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			int j=0;

        	List<Object> beforeDLMNodeInstanceVOL= new LinkedList<Object>();
        	List<StepTypesVOL> beforeDLMStepTypesVOL = new LinkedList<StepTypesVOL>();
        
        	
			for(int i=0; i<newInputNodesToSort.size();i++) {
				
            Object currentNode= newInputNodesToSort.get(i);
            StepTypesVOL currentStepType= newInputSteptypesToSort.get(i);
            
				
                 if(!(newInputSteptypesToSort.get(i).equals(StepTypesVOL.MM)|| newInputSteptypesToSort.get(i).equals(StepTypesVOL.DLM))) {
                	 
                	 try {
                	 
                	 String processEventTimeStamp= processTrace.get(j).getAttributes().get("time:timestamp").toString();
                	 processEventTimeStamp= processEventTimeStamp.substring(0,processEventTimeStamp.indexOf("+")).replace("T", " ");
                	
                	 
             		Date processEvent = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
                             .parse(processEventTimeStamp);
             		
             		
             		if(processEvent.compareTo(oprEvent) < 0 ) {
             			
             		}else {
             			for(int z=0;z<i; z++) {
             			beforeDLMNodeInstanceVOL.add(newInputNodesToSort.get(z));
             			beforeDLMStepTypesVOL.add(newInputSteptypesToSort.get(z));		
             			}
             			beforeDLMNodeInstanceVOL.add(DLMNode);
             			beforeDLMStepTypesVOL.add(StepTypesVOL.DLM);
             			
             			for(int z=i;z<newInputNodesToSort.size(); z++) {
                 			beforeDLMNodeInstanceVOL.add(newInputNodesToSort.get(z));
                 			beforeDLMStepTypesVOL.add(newInputSteptypesToSort.get(z));		
                 			}
           
             			break;
             			
             		}
                	 
                 } catch (ParseException e) {
             		//System.out.println("the format of time is wrong-->");
             		
                 }
                	 j=j+1;
                	 
                 }else if(newInputSteptypesToSort.get(i).equals(StepTypesVOL.MM) || newInputSteptypesToSort.get(i).equals(StepTypesVOL.TM) ) {
                	 
          			 j=j;
                 }
                 else if(newInputSteptypesToSort.get(i).equals(StepTypesVOL.DLM)){
                	 
                	 
                	String idofDLMEvent=currentNode.toString().substring(0,currentNode.toString().indexOf("|"));
         			int DLMEventID= Integer.parseInt(idofDLMEvent);
                		 
                	 String DLMEventTimeStamp= dataTrace.get(DLMEventID).getAttributes().get("time:timestamp").toString();
                	 DLMEventTimeStamp= DLMEventTimeStamp.substring(0,DLMEventTimeStamp.indexOf("+")).replace("T", " "); 
         			 
         			Date DLMEvent = null;
					try {
						DLMEvent = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
						        .parse(DLMEventTimeStamp);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(DLMEvent.compareTo(oprEvent) <0 || DLMEvent.compareTo(oprEvent)==0 ) {
						
					}else {
						for(int z=0;z<i; z++) {
	             			beforeDLMNodeInstanceVOL.add(newInputNodesToSort.get(z));
	             			beforeDLMStepTypesVOL.add(newInputSteptypesToSort.get(z));		
	             			}
	             			beforeDLMNodeInstanceVOL.add(DLMNode);
	             			beforeDLMStepTypesVOL.add(StepTypesVOL.DLM);
	             			
	             			for(int z=i;z<newInputNodesToSort.size(); z++) {
	                 			beforeDLMNodeInstanceVOL.add(newInputNodesToSort.get(z));
	                 			beforeDLMStepTypesVOL.add(newInputSteptypesToSort.get(z));		
	                 			}
		
						break;
						
						
					}
                		j=j; 
                		 
                	 }


				 
				
			}
			
			//Merge DLM, BeforeList and afterList
			
			newInputNodesToSort.clear();
			for (int m=0; m <beforeDLMNodeInstanceVOL.size(); m++) {
				
				newInputNodesToSort.add(beforeDLMNodeInstanceVOL.get(m));
			}
			newInputSteptypesToSort.clear();
			for (int m=0; m <beforeDLMStepTypesVOL.size(); m++) {
				
				newInputSteptypesToSort.add(beforeDLMStepTypesVOL.get(m));
			}

			
			beforeDLMNodeInstanceVOL.clear();
			beforeDLMStepTypesVOL.clear();
			}

		    OrderedNodeInstanceVOL.clear();
		    for (int n=0; n <newInputNodesToSort.size(); n++) {
		    	
		    	OrderedNodeInstanceVOL.add(newInputNodesToSort.get(n));
		    }
		    OrderedStepTypesVOL.clear();
		    for (int n=0; n < newInputSteptypesToSort.size(); n++) {
		    	
		    	OrderedStepTypesVOL.add( newInputSteptypesToSort.get(n));
		    }
		    
	}
	

	
    private boolean checkReadList(String traceLabel ,List<String> readList, String Move) {
	
	for(String str: readList) {

	    if(str.contains(Move)) {
	
	       return true;
	    }
	}
	return false;
		
	}
    
   
	private void find(String traceLabel,List<StepTypesVOL> stepTypesVOL,List<Object> nodeInstanceVOL,String readMove) {
		
		for (int step=0; step < nodeInstanceVOL.size(); step++) {
			Object node = nodeInstanceVOL.get(step);
			
			String idActivityType=node.toString().substring(0,node.toString().indexOf("+"));
			
			
			if(idActivityType.equalsIgnoreCase(readMove)) {
				rearrangedNodeInstanceVOL.add(nodeInstanceVOL.get(step));
				rearrangedStepTypesVOL.add(stepTypesVOL.get(step));
			}
			
			
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent arg0) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent arg0) {
		mouseOver = true;
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) {
		mouseOver = false;
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent arg0) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent arg0) {
		// ignore
	}
	
}
