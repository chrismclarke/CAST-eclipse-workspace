package contin;

import dataView.*;
import imageGroups.TickCrossImages;


public class UnknownJointView extends JointView {
//	static public final String UNKNOWNJOINTVIEW = "unknownJoint";
	
	public UnknownJointView(DataSet theData, XApplet applet, String yKey, String xKey, int probDecimals) {
		super(theData, applet, yKey, xKey, probDecimals);
		
		TickCrossImages.loadCrossAndTick(applet);
	}
	
	protected double[][] getProbs(CoreVariable yVar, CoreVariable xVar) {
		return null;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
