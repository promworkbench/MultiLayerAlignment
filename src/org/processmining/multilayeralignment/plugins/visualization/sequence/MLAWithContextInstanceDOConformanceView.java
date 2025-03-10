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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XTrace;
import org.processmining.multilayeralignment.models.mlreplay.StepTypes;
import org.processmining.multilayeralignment.models.mlreplay.StepTypesVOL;
import org.processmining.multilayeralignment.plugins.visualization.tracealignment.MultiLayerAlignmentConstantsWithContext;
import org.processmining.plugins.petrinet.visualization.AlignmentConstants;;


public class MLAWithContextInstanceDOConformanceView extends JComponent implements MouseListener, MouseMotionListener {
	
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
	
	
	public MLAWithContextInstanceDOConformanceView(String traceLabel, List<Object> nodeInstance, List<StepTypes> stepTypes, List<Object> nodeInstanceVOL, List<StepTypesVOL> stepTypesVOL,
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


		addMouseListener(this);
		addMouseMotionListener(this);
		int width = (nodeInstance.size() * elementWidth) + trackPadding + 300;
		setMinimumSize(new Dimension(width, 120));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
		setPreferredSize(new Dimension(width, 120));
		setDoubleBuffered(true);

	}

	public MLAWithContextInstanceDOConformanceView(String traceLabel, List<Object> nodeInstance, List<StepTypes> stepTypes, List<Object> nodeInstanceVOL, List<StepTypesVOL> stepTypesVOL,List<Object> rearrangedNodeInstances,XTrace processTrace,XTrace dataTrace
			) {
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
		if (nodeInstance != null) {
			trackRightX = trackPadding + (nodeInstance.size() * elementWidth);
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
			drawInstanceFlag(g2d, clip.x, 25, 35); //trackY, trackHeight);
			// draw event flag
			if (activeEvent >= 0) {
				int eventX = trackPadding + (activeEvent * elementWidth);
				drawEventFlag(g2d, activeEvent, eventX, 5, 30);
			}
		}

	}
	
	protected int mapEventIndex(int x, int y) {
		if ((y >= trackY) && (y <= (trackY + trackHeight))) {
			// y-coordinate matches, remap x to index
			x -= trackPadding;
			x /= elementWidth;
			if (nodeInstance != null) {
				if ((x >= 0) && (x < nodeInstance.size())) {
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
		if (nodeInstance != null) {
			size = nodeInstance.size() + " Moves";
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
		if (nodeInstance!= null) {
			String label=nodeInstance.get(index).toString(); 
			String processInfo="";
			String dataInfo= "";
			String roleInfo="";
			String moveType= stepTypes.get(index).toString();
			// be jaye movetype--->steptype.get(index).toString()

			if(moveType.equals("PLM")){
				processInfo=label.substring(0,ordinalIndexOf(label, "|", 2)).replace("+", "_");
				String processEventIndex=label.substring(ordinalIndexOf(label, "|", 3)+1, label.length());
				int processEventID=Integer.parseInt(processEventIndex);
				processInfo="Unexpected Activity: "+ processInfo;
				dataInfo= "";
				roleInfo="Illegitimate Role, Actore: "+processTrace.get(processEventID).getAttributes().get("org:resource").toString();
			    }
			if(moveType.equals("DLM")){
				
				dataInfo=label.substring(ordinalIndexOf(label, "|", 2)+2,label.length()).replace("#N", "");
				String dataEventIndex=dataInfo.substring(ordinalIndexOf(dataInfo, "|", 1)+1 ,dataInfo.length());
				int dataEventID=Integer.parseInt(dataEventIndex);
				processInfo="Unknown Context";
				dataInfo="Unexpected D.O.: "+ dataTrace.get(dataEventID).getAttributes().get("concept:name").toString();
				roleInfo="Illegitimate Role, Actore: "+dataTrace.get(dataEventID).getAttributes().get("org:resource").toString();
			    }
			if(moveType.equals("TSM")){
				processInfo=label.substring(0,ordinalIndexOf(label, "|", 2));
				String dataEventIndex=label.substring(ordinalIndexOf(label, "|", 3)+1, label.length());
				int dataEventID=Integer.parseInt(dataEventIndex);
	
				processInfo="Context: "+ processInfo;
				dataInfo= "Expected D.O.: "+ label.substring(ordinalIndexOf(label, "|", 2)+1,ordinalIndexOf(label, "|", 3)).replace("#N", "");
				roleInfo="Legitimate Role, Actore: "+dataTrace.get(dataEventID).getAttributes().get("org:resource").toString();
			
			    }
			if(moveType.equals("TSMP")){
				processInfo=label.substring(0,ordinalIndexOf(label, "|", 2));
				String dataEventIndex=label.substring(ordinalIndexOf(label, "|", 3)+1, label.length());
				int dataEventID=Integer.parseInt(dataEventIndex);
				processInfo="Context: "+ processInfo;
				dataInfo= "Expected D.O.: "+ label.substring(ordinalIndexOf(label, "|", 2)+1,ordinalIndexOf(label, "|", 3)).replace("#N", "");
				roleInfo="Illegitimate Role, Actore: "+dataTrace.get(dataEventID).getAttributes().get("org:resource").toString();
			    }
			if(moveType.equals("PSM")){
				processInfo=label.substring(0,ordinalIndexOf(label, "|", 3)).replace("|N#", "_");
				
				//String processEventIndex=processInfo.substring(0 ,ordinalIndexOf(processInfo, "|", 1));
				//int processEventID=Integer.parseInt(processEventIndex);
				String processEventIndex=label.substring(ordinalIndexOf(label, "|", 3)+1, label.length());
				int processEventID=Integer.parseInt(processEventIndex);
				processInfo="Expected Activity: "+ processInfo;
				dataInfo= "";
				roleInfo="Legitimate Role, Actore: "+processTrace.get(processEventID).getAttributes().get("org:resource").toString();
			    }
			if(moveType.equals("PSMP")){
				processInfo=label.substring(0,ordinalIndexOf(label, "|", 3)).replace("|N#", "_");
				String processEventIndex=label.substring(ordinalIndexOf(label, "|", 3)+1, label.length());
				int processEventID=Integer.parseInt(processEventIndex);
				processInfo="Expected Activity: "+ processInfo;
				dataInfo= "";
				roleInfo="Illegitimate Role, Actore: "+processTrace.get(processEventID).getAttributes().get("org:resource").toString();
			    }
			if(moveType.equals("TM")){
				processInfo="Invisible Transition: "+label;
				dataInfo= "";
				roleInfo="";
			    }
			if(moveType.equals("MM")){
				
				if(label.contains("|N#")){
					processInfo=label.substring(ordinalIndexOf(label, "|", 1)+1, label.length()).replace("|N#", "_");
					processInfo="Skipped Activity: "+ processInfo;
					dataInfo= "";
					roleInfo="";
				}
				if(label.contains("$M#N")){
					processInfo=label.substring(ordinalIndexOf(label, "|", 1)+1,ordinalIndexOf(label, "|", 2));
					processInfo="Context: "+ processInfo;
					dataInfo= "Ignored Mandatory D.O: "+label.substring(ordinalIndexOf(label, "|", 2)+1,label.length()).replace("$M#N", "");
					roleInfo="";
				}
				//if((label.contains("$O#N")) & !(label.contains("Na(Na)$O#N"))){
				if(label.contains("$O#N")){
					processInfo=label.substring(ordinalIndexOf(label, "|", 1)+1,ordinalIndexOf(label, "|", 2));
					processInfo="Context: "+ processInfo;
					dataInfo= "Ignored Optional D.O: "+label.substring(ordinalIndexOf(label, "|", 2)+1,label.length()).replace("$O#N", "");
					roleInfo="";
				}
				
				}

			//************************Correct Labels
			//bring processTrace and data trace
			
			//drawMultiLineFlag(g2d, getColor(stepTypes.get(index)), x, y, height, stepTypes.get(index).toString(), ProcessInfo, dataInfo);
			drawMultiLineFlag(g2d, Color.white, x, y, height, roleInfo, processInfo, dataInfo);
		} else {
			drawXEventFlag(g2d, index, x, y, height);
		}
	}
	
	
	protected void drawEvent(Graphics2D g2d, int index, boolean active, int x, int y, int width, int heightThis) {
		Color moveColor;
		String name=nodeInstance.get(index).toString();
		String modelMoveType="";
		if (nodeInstance != null) {

			moveColor = getColorOfIndex(index);
		} else {

			moveColor =MultiLayerAlignmentConstantsWithContext.MOVEMODELCOLOR ;
		}

		// draw triangularish shape

		int height = 40 - 2- 1;
		int midPointBX = x + elementTriOffset;
		int midPointAX = x + width + elementTriOffset;
		int midPointY = y + (height / 2);
		Color prColor, pColor,dColor,dColor2;
		String moveType="";
		
        if(moveColor.equals(Color.green)){
        	moveType="TSM";
        	//System.out.println("TSM: "+name);
        	pColor=Color.green;
        	
        	drawGroupData(g2d,moveType,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height,pColor);
		}
        else if(moveColor.equals(Color.orange)){
        	moveType="TSMP";
        	//System.out.println("TSMP: "+name);
        	pColor=Color.orange;
        	
        	drawGroupData(g2d,moveType, moveColor, index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height,pColor);
		}
        else if(moveColor.equals(Color.BLUE)){
        	moveType="PSM";
        	//System.out.println("PSM: "+name);
        	pColor=Color.BLUE;
        	
        	drawGroup1(g2d,moveType, moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height,pColor);
		}
        else if(moveColor.equals(Color.cyan)){
        	moveType="PSMP";
        	//System.out.println("PSMP: "+name);
        	pColor=Color.cyan;
        	
        	drawGroup1(g2d,moveType ,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height,pColor);
		}
        else if(moveColor.equals(Color.pink)){
        	moveType="MM";
        	
        	
        	//modelMoveType=name.substring(name.indexOf("!")+1,name.indexOf(":"));
        	//System.out.println("MM: "+name);
        	if(name.contains("$M#")) {
        		pColor=Color.pink;
        		drawGroupDataMMM(g2d,moveType,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height,pColor);
        	//}else if(name.contains("$O#") & !name.contains("Na(Na)$O#")){
        	}else if(name.contains("$O#")){
        		pColor=Color.white;
        		drawGroupDataMMO(g2d,moveType,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height,pColor);
        	//}else if(name.contains("Na(Na)$O#")){
        	//	pColor=Color.pink;
        	//	drawGroupDataMMONa(g2d,moveType,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height,pColor);
        	}
        	else {
        		pColor=Color.pink;
            	drawGroup1(g2d,moveType ,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, pColor);	
        	}
        	
		}
        else if(moveColor.equals(Color.yellow)){
        	moveType="PLM";
        	//System.out.println("PLM: "+name);
        	pColor=Color.yellow;
        	drawGroup1(g2d,moveType,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height,pColor);
		}
        else if(moveColor.equals(Color.red)){
        	moveType="DLM";
        	pColor=Color.red;
        	//System.out.println("DLM: "+name);
        	drawGroupData(g2d,moveType,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, pColor);
		}
       else if(moveColor.equals(Color.white)){
    	    //System.out.println("xxxxxx2");
        	//moveType="TM";
        	//pColor=Color.gray;
        	//System.out.println("TM: "+name);
        	//drawGroup1(g2d,moveType,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height,pColor);
		}
        
        else {
        	
        	pColor=moveColor;
        
        	drawGroup1(g2d,moveType ,moveColor,index, active, x,  y, midPointBX,midPointAX,midPointY,width,  height, pColor);
        }

	}
	
	protected void drawGroup1(Graphics2D g2d,String moveType,Color moveColor ,int index, boolean active, int x, int y, int midPointBX,int midPointAX,int midPointY,int width, int height,
			Color pColor) {
		       /*
		       if (active == false) {
		    	   //prColor= attenuateColor(prColor);
		    	   pColor= attenuateColor(pColor);
		    	   //dColor= attenuateColor(dColor);
		       }
		       */
		       Color WhitColor= Color.white;
			   Color pColorMM= new Color(102, 0 ,153) ;
		       GradientPaint redtowhite = new GradientPaint(5, 5,pColor, 20, 20, WhitColor, true);
		       GradientPaint redtowhite2 = new GradientPaint(25, 25,pColor, 15, 25, WhitColor, true);

		      
		       
		       if(moveType.equalsIgnoreCase("MM")) {
		    	    g2d.setPaint(redtowhite2);
		       }else {
		        g2d.setColor(pColor);
		       }
		       //privacy layer
	
		        //g2d.setColor(prColor);
				//g2d.fillOval(x + 9 , y, 16, 16);
				//g2d.setColor(Color.black);
				//g2d.drawOval(x + 9 , y, 16, 16);
				
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
				    	    //g2d.setPaint(redtowhite2);
			    		   g2d.setColor(pColorMM);
				       }
			    	   else
			    	   g2d.setColor(pColor);   
			       }else 
			       {   if(moveType.equalsIgnoreCase("MM")) {
			    	    g2d.setPaint(pColorMM);
			           }else
			    	   g2d.setColor(pColor); 
			       }
			    	  
				//g2d.setColor(pColor); 
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				
				//Data Layer
				//y+= 40;
				//int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
				//int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
		   
				//g2d.setColor(dColor); 
				//g2d.fillPolygon(xCoords2, yCoords2, 3);
				//g2d.setColor(Color.black);
				//g2d.drawPolygon(xCoords2, yCoords2, 3);	
			
		
	}
	
	protected void drawGroupData(Graphics2D g2d,String moveType,Color moveColor ,int index, boolean active, int x, int y, int midPointBX,int midPointAX,int midPointY,int width, int height,
			Color pColor) {
		       
		      Color pColorMM= new Color(102, 0 ,153) ;
		      /*
		       if (active == false) {
		    	   //prColor= attenuateColor(prColor);
		    	   pColor= attenuateColor(pColor);
		    	   //dColor= attenuateColor(dColor);
		       }
		       */

		       //privacy layer
	
		        g2d.setColor(pColor);
				g2d.fillOval(x + 9 , y+5, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 9 , y+5, 16, 16);
				
				//process layer
				midPointY+= 18;
				y+= 18;
				
				int[] xCoords = new int[] { x, x + width-2, midPointAX, x + width-2, x, midPointBX };
				int[] yCoords = new int[] { y+5, y+5, midPointY, y + height-5, y + height-5, midPointY };
				
				if (active == true) {
					for (int i = 0; i < xCoords.length; i++) {
						xCoords[i] -= 1;
						yCoords[i] -= 3;
					}
				}
				
				if (active == true) {
			    	   g2d.setColor(pColor);   
			       }else
			    	   g2d.setColor(moveColor); 
			    	  
				//g2d.setColor(pColor); 
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				
				//Data Layer
				//y+= 40;
				//int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
				//int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
		   
				//g2d.setColor(dColor); 
				//g2d.fillPolygon(xCoords2, yCoords2, 3);
				//g2d.setColor(Color.black);
				//g2d.drawPolygon(xCoords2, yCoords2, 3);	
			
		
	}
	protected void drawGroupDataMMM(Graphics2D g2d,String moveType,Color moveColor ,int index, boolean active, int x, int y, int midPointBX,int midPointAX,int midPointY,int width, int height,
			Color pColor) {
		    Color pColorMM= new Color(102, 0 ,153) ;
		    /*
		       if (active == false) {
		    	   //prColor= attenuateColor(prColor);
		    	   pColor= attenuateColor(pColorMM);
		    	   //dColor= attenuateColor(dColor);
		       }
		     */  
		       

		       //privacy layer
	
		        g2d.setColor(pColorMM);
				g2d.fillOval(x + 9 , y+5, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 9 , y+5, 16, 16);
				
				//process layer
				midPointY+= 18;
				y+= 18;
				
				int[] xCoords = new int[] { x, x + width-2, midPointAX, x + width-2, x, midPointBX };
				int[] yCoords = new int[] { y+5, y+5, midPointY, y + height-5, y + height-5, midPointY };
				
				if (active == true) {
					for (int i = 0; i < xCoords.length; i++) {
						xCoords[i] -= 1;
						yCoords[i] -= 3;
					}
				}
				
				if (active == true) {
			    	   g2d.setColor(pColorMM);   
			       }else
			    	   g2d.setColor(pColorMM); 
			    	  
				//g2d.setColor(pColor); 
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				
				//Data Layer
				//y+= 40;
				//int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
				//int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
		   
				//g2d.setColor(dColor); 
				//g2d.fillPolygon(xCoords2, yCoords2, 3);
				//g2d.setColor(Color.black);
				//g2d.drawPolygon(xCoords2, yCoords2, 3);	
			
		
	}
	protected void drawGroupDataMMO(Graphics2D g2d,String moveType,Color moveColor ,int index, boolean active, int x, int y, int midPointBX,int midPointAX,int midPointY,int width, int height,
			Color pColor) {
		    Color pColorMM= new Color(102, 0 ,153) ;
		    /*
		       if (active == false) {
		    	   //prColor= attenuateColor(prColor);
		    	   pColor= attenuateColor(pColorMM);
		    	   //dColor= attenuateColor(dColor);
		       }
		       */
		       Color WhitColor= Color.white;
		       GradientPaint redtowhite2 = new GradientPaint(25, 25,pColorMM, 15, 25, WhitColor, true);

		       //privacy layer
	
		       if (active == true) {
		    	   g2d.setPaint(redtowhite2);  
		       }else
		    	   g2d.setColor(pColorMM); 
				g2d.fillOval(x + 9 , y+5, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 9 , y+5, 16, 16);
				
				//process layer
				midPointY+= 18;
				y+= 18;
				
				int[] xCoords = new int[] { x, x + width-2, midPointAX, x + width-2, x, midPointBX };
				int[] yCoords = new int[] { y+5, y+5, midPointY, y + height-5, y + height-5, midPointY };
				
				if (active == true) {
					for (int i = 0; i < xCoords.length; i++) {
						xCoords[i] -= 1;
						yCoords[i] -= 3;
					}
				}
				
				
			   	  
				if (active == true) {
			    	   g2d.setPaint(redtowhite2);  
			       }else
			    	   g2d.setColor(pColorMM); 
			      
				//g2d.setColor(pColor); 
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				
				//Data Layer
				//y+= 40;
				//int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
				//int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
		   
				//g2d.setColor(dColor); 
				//g2d.fillPolygon(xCoords2, yCoords2, 3);
				//g2d.setColor(Color.black);
				//g2d.drawPolygon(xCoords2, yCoords2, 3);	
			
		
	}
	protected void drawGroupDataMMONa(Graphics2D g2d,String moveType,Color moveColor ,int index, boolean active, int x, int y, int midPointBX,int midPointAX,int midPointY,int width, int height,
			Color pColor) {
		Color pColorMM= new Color(102, 0 ,153) ;
	    /*
	       if (active == false) {
	    	   //prColor= attenuateColor(prColor);
	    	   pColor= attenuateColor(pColorMM);
	    	   //dColor= attenuateColor(dColor);
	       }
	       */
	       Color WhitColor= Color.white;
	       GradientPaint redtowhite2 = new GradientPaint(25, 25,pColorMM, 15, 25, WhitColor, true);

	       //privacy layer

	     
			//process layer
			midPointY+= 18;
			y+= 18;
			
			int[] xCoords = new int[] { x, x , midPointAX, x, x, midPointAX };
			int[] yCoords = new int[] { y+5, y+5, midPointY, y + height-5, y + height-5, midPointY };
			
			if (active == true) {
				for (int i = 0; i < xCoords.length; i++) {
					xCoords[i] -= 1;
					yCoords[i] -= 3;
				}
			}
			
			
		   	  
			if (active == true) {
		    	   g2d.setPaint(redtowhite2);  
		       }else
		    	   g2d.setColor(pColorMM); 
		      
			//g2d.setColor(pColor); 
			//g2d.fillPolygon(xCoords, yCoords, 6);
			//g2d.setColor(Color.black);
			//g2d.drawPolygon(xCoords, yCoords, 6);
			
			//Data Layer
			//y+= 40;
			//int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
			//int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
	   
			//g2d.setColor(dColor); 
			//g2d.fillPolygon(xCoords2, yCoords2, 3);
			//g2d.setColor(Color.black);
			//g2d.drawPolygon(xCoords2, yCoords2, 3);	
		
	}
	protected void drawGroupx(Graphics2D g2d,String moveType,Color moveColor ,int index, boolean active, int x, int y, int midPointBX,int midPointAX,int midPointY,int width, int height,
			Color prColor,Color pColor,Color dColor) {
		       
		       if (active == false) {
		    	   prColor= attenuateColor(prColor);
		    	   pColor= attenuateColor(pColor);
		    	   dColor= attenuateColor(dColor);
		       }
		       

		       //privacy layer
	
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
			    	   g2d.setColor(moveColor); 
			    	  
				//g2d.setColor(pColor); 
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				
				//Data Layer
				y+= 40;
				int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
				int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
		   
				g2d.setColor(dColor); 
				g2d.fillPolygon(xCoords2, yCoords2, 3);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2, 3);	
			
		
	}
	protected void drawGroup2(Graphics2D g2d,String moveType, Color moveColor ,int index, boolean active, int x, int y, int midPointBX,int midPointAX,int midPointY,int width, int height,
			Color prColor,Color pColor) {
		       
		       if (active == false) {
		    	   prColor= attenuateColor(prColor);
		    	   pColor= attenuateColor(pColor);
		
		       }
		       
		       //privacy layer
		      
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
			    	   g2d.setColor(moveColor); 
			    	   
			
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
	       

	       //privacy layer
	       
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
		       
		       if (active == false) {
		    	   prColor= attenuateColor(prColor);
		    	   pColor= attenuateColor(pColor);
		    	   dColor= attenuateColor(dColor);
		    	   dColor2= attenuateColor(dColor2);
		       }
		       
		       //privacy layer
		      
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
			    	   g2d.setColor(moveColor); 
			    
			
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
		        
				
				g2d.setColor(dColor2);   
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
	
	//should be corrected
	protected Color getColor(StepTypes stepTypes2) {
		switch (stepTypes2) {
			case PLM :
				return MultiLayerAlignmentConstantsWithContext.MOVEPROCESSLOGCOLOR;
			case DLM :
				return MultiLayerAlignmentConstantsWithContext.MOVEDATALOGCOLOR;
			case TSM :
				return MultiLayerAlignmentConstantsWithContext.MOVETOTALLYCOLOR;
			case TSMP:
				return MultiLayerAlignmentConstantsWithContext.MOVETOTALLYT2COLOR;
			case PSM :
				return MultiLayerAlignmentConstantsWithContext.MOVEPARTIALLYCOLOR;
			case PSMP :
				return MultiLayerAlignmentConstantsWithContext.MOVEPARTIALLYT2COLOR;
			case MM :
				return MultiLayerAlignmentConstantsWithContext.MOVEMODELCOLOR;
			default :
				return Color.white; // unknown
		}
	}
	
	protected Color getColorOfIndex(int index) {
		return getColor(stepTypes.get(index));
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
	    
		readList.add("*test");
		for (Object obj : rearrangedNodeInstances) {
			String readMove= obj.toString();
			
			if(!checkReadList(traceLabel,readList,readMove) ){
				find(traceLabel,stepTypesVOL,nodeInstanceVOL,readMove);
				readList.add(readMove);
			}
			
		}
		
		
	}
	
    private boolean checkReadList(String traceLabel,List<String> readList, String Move) {
	
	for(String str: readList) {
		//System.out.println("**********************this read list is");
		//System.out.println(str);
	    if(str.contains(Move)) {
	    	//System.out.println("**********************this read move is");
			//System.out.println(Move);
	    	
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
	
	public static int ordinalIndexOf(String str, String substr, int n) {
	    int pos = str.indexOf(substr);
	    while (--n > 0 && pos != -1)
	        pos = str.indexOf(substr, pos + 1);
	    return pos;
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
