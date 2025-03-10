package org.processmining.multilayeralignment.models.mlreplay;

import java.util.Date;
import java.util.HashMap;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class Utility {
	public static HashMap<XEventClass, Transition> ev2TrMapping;
	public static TransEvClassMapping tr2EvMapping;
	public static XEventClasses eventClasses;
	public static String getAttributeValue(XEvent event, String attName){
		if(! event.getAttributes().containsKey(attName))
			return "";
		XAttribute attribute = event.getAttributes().get(attName);
		if(attribute instanceof XAttributeTimestampImpl)
			return event.getAttributes().get(attName).toString();
		if(attribute instanceof XAttributeDiscreteImpl)
			return String.valueOf(((XAttributeDiscreteImpl) event.getAttributes().get(attName)).getValue());
		if(attribute instanceof XAttributeBooleanImpl)
			return String.valueOf(((XAttributeBooleanImpl) event.getAttributes().get(attName)).getValue());
		if(attribute instanceof XAttributeContinuousImpl)
			return String.valueOf(((XAttributeContinuousImpl) event.getAttributes().get(attName)).getValue());
		
		return ((XAttributeLiteralImpl) event.getAttributes().get(attName)).getValue();
	}
	public static Date getTime(XEvent event, String attName){
		Date date = ((XAttributeTimestampImpl) event.getAttributes().get(attName)).getValue();		
		return date;
	}
	public static String getTransitionName(SyncReplayResult syncReplayResult, int i, HashMap<XEventClass, Transition> eventClassTransMapping){
		String transitionName = "";
		Object nodeInstance = syncReplayResult.getNodeInstance().get(i);
		StepTypes st = syncReplayResult.getStepTypes().get(i);
		if(st.equals(StepTypes.L))
			transitionName = eventClassTransMapping.get(nodeInstance).toString();
		else
			transitionName = nodeInstance.toString();
		return transitionName;
	}
	public static void setAttributeName(XEvent event, String attName, String value){
		XAttributeLiteralImpl attr = null;
		if( event.getAttributes().get(attName) == null){
			attr = new XAttributeLiteralImpl(attName, value);
			event.getAttributes().put(attName, attr);
		}else{			
			attr = ((XAttributeLiteralImpl) event.getAttributes().get(attName));			
			attr.setValue(value);
			event.getAttributes().put(attName, attr);
		}
		
	}
	public static void setAttributeName(XEvent event, String attName, Date value){
		XAttributeTimestampImpl time = null;
		if(event.getAttributes().get(attName) == null)
			time = new XAttributeTimestampImpl(attName, value);
		else{
			time = ((XAttributeTimestampImpl) event.getAttributes().get(attName));					
			time.setValue(value);
		}
		event.getAttributes().put(attName, time);
	}
	
	public static String getTraceAttribute(XTrace trace, String attName){
		return trace.getAttributes().get(attName).toString();
	}
}
