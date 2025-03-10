/**
 * 
 */
package org.processmining.multilayeralignment.models.mlreplay;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author aadrians
 * 
 */
public class SyncReplayResult {
	private List<Object> nodeInstance = null;
	private List<Object> nodeInstanceVOL = null;
	private List<Object> rearrangedNodeInstance=new LinkedList<Object>();
	private List<StepTypes> stepTypes = null;	
	private List<StepTypesVOL> stepTypesVOL = null;
	private SortedSet<Integer> traceIndex = new TreeSet<Integer>();
	private boolean isReliable = false;

	/**
	 * Information and key to stored information
	 */
	private Map<String, Double> info = new HashMap<String, Double>(3);
	
	@SuppressWarnings("unused")
	private SyncReplayResult() {
	};

	public SyncReplayResult(List<Object> nodeInstance, List<StepTypes> stepTypes, int traceIndex) {
		this.nodeInstance = nodeInstance;
		this.stepTypes = stepTypes;
		this.traceIndex.add(traceIndex);
	}
    
	public SyncReplayResult(List<Object> nodeInstance,List<StepTypes> stepTypes, List<Object> nodeInstanceVOL, List<StepTypesVOL> stepTypesVOL, int traceIndex,List<Object> rearrangedNodeInstance ,String str) {
		this.nodeInstance = nodeInstance;
		this.nodeInstanceVOL = nodeInstanceVOL;
		this.stepTypes = stepTypes;
		this.stepTypesVOL = stepTypesVOL;
		this.traceIndex.add(traceIndex);
		this.rearrangedNodeInstance=rearrangedNodeInstance;
	}
	/**
	 * @return the info
	 */
	public Map<String, Double> getInfo() {
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(Map<String, Double> info) {
		this.info = info;
	}
	
	/**
	 * add additional info to sync replay result
	 * @param property
	 * @param value
	 */
	public void addInfo(String property, Double value){
		info.put(property, value);
	}

	public void addNewCase(int traceIndex) {
		this.traceIndex.add(traceIndex);
	}

	/**
	 * @return the traceIndex
	 */
	public SortedSet<Integer> getTraceIndex() {
		return traceIndex;
	}

	/**
	 * @param traceIndex
	 *            the traceIndex to set
	 */
	public void setTraceIndex(SortedSet<Integer> traceIndex) {
		this.traceIndex = traceIndex;
	}

	/**
	 * @return the nodeInstance
	 */
	public List<Object> getNodeInstance() {
		return nodeInstance;
	}
	
	public List<Object> getNodeInstanceVOL() {
		return nodeInstanceVOL;
	}
	
	public List<Object> getRearrangedNodeInstance(){
		return rearrangedNodeInstance;
	}

	/**
	 * @param nodeInstance
	 *            the nodeInstance to set
	 */
	public void setNodeInstance(List<Object> nodeInstance) {
		this.nodeInstance = nodeInstance;
	}

	/**
	 * @return the stepTypes
	 */
	public List<StepTypes> getStepTypes() {
		return stepTypes;
	}
	
	public List<StepTypesVOL> getStepTypesVOL() {
		return stepTypesVOL;
	}

	/**
	 * @param stepTypes
	 *            the stepTypes to set
	 */
	public void setStepTypes(List<StepTypes> stepTypes) {
		this.stepTypes = stepTypes;
	}
	public void setStepTypesVOL(List<StepTypesVOL> stepTypesVOL) {
		this.stepTypesVOL = stepTypesVOL;
	}
	public void setReliable(boolean isReliable) {
		this.isReliable = isReliable;
	}

	public boolean isReliable() {
		return isReliable;
	}

}