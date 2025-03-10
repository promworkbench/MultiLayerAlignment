package org.processmining.multilayeralignment.models.mlreplay;

public enum StepTypesVOL {
	TM("TM"), //Tau Move
	TSM("TSM"), //Totally Sync. Move
	TSMP("TSMP"),//Totally Sync. Move with penalty cost
	PSM("PSM"), //Partially Sync. Move
	PSMP("PSMP"), //Partially Sync. Move with penalty cost
	MM("MM"), //Model Move
	PLM("PLM"), //Process Log Move
	DLM("DLM"),//Data Log Move
	TSMPSM("TSMPSM"), //Totally Sync. Move + Partially Sync. Move
	PSMTSM("PSMTSM"),//Totally Sync. Move + Partially Sync. Move
	TSMPPSMP("TSMPPSMP"), //Totally Sync. Move + Partially Sync. Move with penalty cost
	PSMPTSMP("PSMPTSMP"),
	OPSM("OPSM"), //Partially Sync. Move- Optional data operation
	OPSMP("OPSMP"), //Partially Sync. Move with penalty cost- Optional data operation
	TSMOPSM("TSMOPSM"), //Totally Sync. Move + Partially Sync. Move
	OPSMTSM("OPSMTSM"),//Totally Sync. Move + Partially Sync. Move
	TSMPOPSMP("TSMPOPSMP"), //Totally Sync. Move + Partially Sync. Move with penalty cost
	OPSMPTSMP("OPSMPTSMP"); //Totally Sync. Move + Partially Sync. Move with penalty cost
	private final String name;

	StepTypesVOL(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
