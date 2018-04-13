package regn;

import dataView.*;
import axis.*;


public class DragExplanAxis extends DragValAxis {
	private String variableName;
	
	public DragExplanAxis(String variableName, XApplet applet) {
		super(applet);
		this.variableName = variableName;
	}
	
	protected String getConstName() {
		return variableName;
	}
}