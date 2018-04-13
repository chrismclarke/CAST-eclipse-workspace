package exerciseNumGraphProg;

import java.awt.*;

import dataView.*;
import exercise2.*;

import exerciseNumGraph.*;


public class FinishDotPlotApplet extends CoreDragBuildApplet {
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("dragCount", "int");
	}
	
	protected int getDragCount() {
		return getIntParam("dragCount");
	}
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			theList = new UsedValueList(data, this, UsedValueList.HEADING);
			theList.addVariableToList("y", UsedValueList.RAW_VALUE);
		thePanel.add("West", theList);
		
			theView = new FinishStackedCrossView(data, this, "y", getAxisInfo());
			registerStatusItem("crossPositions", (DragStackedCrossView)theView);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		valuesUsed.setAllUsed();
		boolean[] alreadyUsed = valuesUsed.getUsage();
		int numberToDrag = getDragCount();
		
		((UsedValueList)theList).setAlreadyUsed(alreadyUsed);
		((UsedValueList)theList).setDragCount(numberToDrag);
		theList.invalidate();
		
		((FinishStackedCrossView)theView).setDragCount(numberToDrag);
		((FinishStackedCrossView)theView).setAlreadyUsed(alreadyUsed);
		((FinishStackedCrossView)theView).changeAxis(getAxisInfo());
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("The stacked dot plot has been completed except for the last " + getDragCount() + " values in the list. Drag the " + getDragCount() + " crosses above the display into the correct stacks.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Incomplete!\n");
				messagePanel.insertText("You have not moved all of the crosses at the top of the diagram.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("This stacked dot plot describes the data. (Click any cross to see its value on the list.)");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly added the crosses for the last " + getDragCount() + " values in the list.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The highlighted crosses have been added in the wrong places.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
//-----------------------------------------------------------
	
	protected boolean completedCrosses() {
		return ((FinishStackedCrossView)theView).draggedAllCrosses();
	}
	
	protected boolean[] inWrongStack() {
		return ((FinishStackedCrossView)theView).inWrongStack();
	}
	
	protected void showCorrectCrosses() {
		((FinishStackedCrossView)theView).showCorrectCrosses();
	}
}