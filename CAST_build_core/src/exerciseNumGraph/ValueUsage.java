package exerciseNumGraph;

import dataView.*;
import utils.*;
import valueList.*;

import exerciseBivar.*;


public class ValueUsage implements StatusInterface {
	private boolean[] alreadyUsed;
	
	private DataSet data;
	private ScrollValueList theList;
	private DataView theView;

	
	public void setListAndView(DataSet data, ScrollValueList theList, DataView theView) {
		this.data = data;
		this.theList = theList;
		this.theView = theView;
	}
	
	public boolean[] getUsage() {
		return alreadyUsed;
	}
	
	public void setAllUsed() {
		for (int i=0 ; i<alreadyUsed.length ; i++)
			alreadyUsed[i] = true;
	}
	
	public void initialise() {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		alreadyUsed = new boolean[yVar.noOfValues()];						//	all false
	}

	public String getStatus() {
		String s = "";
		for (int i=0 ; i<alreadyUsed.length ; i++)
			s += alreadyUsed[i] ? "1" : "0";
		return s;
	}
	
	public void setStatus(String statusString) {
		if (alreadyUsed == null || alreadyUsed.length != statusString.length())
			alreadyUsed = new boolean[statusString.length()];
		for (int i=0 ; i<alreadyUsed.length ; i++)
			alreadyUsed[i] = statusString.charAt(i) == '1';
		if (theList instanceof UsedValueList)
			((UsedValueList)theList).setAlreadyUsed(alreadyUsed);
		else
			((UsedStemLeafList)theList).setAlreadyUsed(alreadyUsed);
		
		if (theView instanceof CoreDragStackedView)
			((CoreDragStackedView)theView).setAlreadyUsed(alreadyUsed);
		else
			((DragScatterView)theView).setAlreadyUsed(alreadyUsed);
		
		data.clearSelection();
	}
}
