package exerciseNumGraphProg;

import java.awt.*;

import dataView.*;
import exercise2.*;

import exerciseNumGraph.*;


public class BuildDotPlotApplet extends CoreDragBuildApplet {
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			theList = new UsedValueList(data, this, UsedValueList.HEADING);
			theList.addVariableToList("y", UsedValueList.RAW_VALUE);
		thePanel.add("West", theList);
		
			theView = new DragStackedCrossView(data, this, "y", getAxisInfo());
			registerStatusItem("crossPositions", (DragStackedCrossView)theView);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		boolean[] alreadyUsed = valuesUsed.getUsage();
		
		((UsedValueList)theList).setAlreadyUsed(alreadyUsed);
		theList.invalidate();
		
		((DragStackedCrossView)theView).setAlreadyUsed(alreadyUsed);
		((DragStackedCrossView)theView).changeAxis(getAxisInfo());
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Click on each value in the list to display it as a cross; then drag the cross to form a stacked dot plot.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Incomplete!\n");
				messagePanel.insertText("You have not added crosses for all values in the list.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("This stacked dot plot describes the data. (Click any cross to see its value on the list.)");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have added a cross in the correct place for each value in the list.");
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
	
	protected boolean[] inWrongStack() {
		return ((DragStackedCrossView)theView).inWrongStack();
	}
	
	protected void showCorrectCrosses() {
		((DragStackedCrossView)theView).showCorrectCrosses();
	}
}