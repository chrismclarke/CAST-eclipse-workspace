package exerciseBivarProg;

import dataView.*;
import axis.*;
import exercise2.*;

import exerciseNumGraph.*;
import exerciseBivar.*;


public class FinishScatterApplet extends DragBuildScatterApplet {
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("dragCount", "int");
	}
	
	protected int getDragCount() {
		return getIntParam("dragCount");
	}
	
//-----------------------------------------------------------
	
	
	protected DragScatterView getScatterView(DataSet data, HorizAxis xAxis, VertAxis yAxis) {
		return new FinishScatterView(data, this, xAxis, yAxis, "x", "y");
	}
	
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		valuesUsed.setAllUsed();
		boolean[] alreadyUsed = valuesUsed.getUsage();
		int numberToDrag = getDragCount();
		
		((UsedValueList)theList).setAlreadyUsed(alreadyUsed);
		((UsedValueList)theList).setDragCount(numberToDrag);
		theList.invalidate();
		
		((FinishScatterView)theView).setDragCount(numberToDrag);
		((FinishScatterView)theView).setAlreadyUsed(alreadyUsed);
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("The scatterplot correctly shows crosses for all rows except the last ones. Drag the two coloured crosses to move them to their correct positions.");
				messagePanel.insertText("\n(The arrow keys can be used to fine-tune the position of a selected cross after dragging to its rough position.)");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Incomplete!\n");
				messagePanel.insertText("You have not dragged all of the coloured crosses at the top.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("This scatterplot describes the data. (Click any cross to see its value on the list.)");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("Your crosses are all in the correct places (or very close).");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				messagePanel.insertText("The highlighted crosses are close to their correct positions but need to be moved a little.");
				messagePanel.insertText("\n(The arrow keys can be used to fine-tune the position of a selected cross.)");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("The highlighted crosses are far from their correct places.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
	
//-----------------------------------------------------------
	
	protected boolean completedCrosses() {
		return ((FinishScatterView)theView).draggedAllCrosses();
	}
}