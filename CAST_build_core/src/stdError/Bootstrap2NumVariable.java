package stdError;

import dataView.*;


public class Bootstrap2NumVariable extends BootstrapNumVariable {
	private BootstrapNumVariable linkedVar;
							//		used to get same map for X & Y in bivariate bootstrap
							//		resetMap() and randomiseMap() should only be called here (not for linkedVar)
	
	public Bootstrap2NumVariable(String theName, BootstrapNumVariable linkedVar) {
		super(theName);
		this.linkedVar = linkedVar;
		linkedVar.setGenerator(getGenerator());
	}
	
	public void resetMap() {
		linkedVar.resetMap();
		
		setUsageCounts(linkedVar.getUsageCounts());
		setMap(linkedVar.getMap());
	}
	
	public void randomiseMap() {
		linkedVar.randomiseMap();
		setUsageCounts(linkedVar.getUsageCounts());
		setMap(linkedVar.getMap());
		clearSortedValues();
	}
}
