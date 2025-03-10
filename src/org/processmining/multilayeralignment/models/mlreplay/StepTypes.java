package org.processmining.multilayeralignment.models.mlreplay;

public enum StepTypes {
	PLM("PLM"), //Process Log Move
	DLM("DLM"), //Data Log Move
	MM("MM"), //Model Move
	TSM("TSM"), //Totally Sync. Move
	PSM("PSM"), //Partially Sync. Move- missing mandatory data operation
	OPSM("OPSM"), //Partially Sync. Move- missing optional data operation
	TM("TM"), //Tau Move
	TSMP("TSMP"),//Totally Sync. Move with penalty cost
	PSMP("PSMP"), //Partially Sync. Move with penalty cost- missing mandatory data operation
	OPSMP("OPSMP");//Partially Sync. Move with penalty cost- missing optional data operation
	
	private final String name;

	StepTypes(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}

