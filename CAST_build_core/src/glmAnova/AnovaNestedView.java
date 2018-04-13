package glmAnova;

import java.awt.*;

import dataView.*;


public class AnovaNestedView extends AnovaCombineTableView {
	
	private int[] testDenom;
	
	public AnovaNestedView(DataSet theData, XApplet applet, String[] componentKey, NumValue maxSsq,
									String[] componentName, Color[] componentColor, String[] variableName, int[] testDenom) {
		super(theData, applet, componentKey, maxSsq, componentName, componentColor, variableName);
		this.testDenom = testDenom;
	}
	
	protected int getResidIndex(int i) {
		return testDenom[i];
	}
	
}