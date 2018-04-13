package exerciseNumGraphProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import exercise2.*;

import exerciseNumGraph.*;


public class BuildStemLeafApplet extends CoreDragBuildApplet {
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("maxLeaves", "int");
	}
	
	protected int getMaxLeaves() {
		return getIntParam("maxLeaves");
	}
	
	protected int getStemPower() {
		StringTokenizer st = new StringTokenizer(getAxisInfo());
		for (int i=0 ; i<2 ; i++)
			st.nextToken();
		return Integer.parseInt(st.nextToken());
	}
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(80, 0));
		
			theList = new UsedStemLeafList(data, this, UsedValueList.HEADING);
			theList.addVariableToList("y", UsedValueList.RAW_VALUE);
			theList.setFont(getBigFont());
		thePanel.add("West", theList);
		
			theView = new DragStemLeafView(data, this, "y", getAxisInfo());
			((DragStemLeafView)theView).setSmallestAtBottom(hasOption("smallestAtBottom"));
			registerStatusItem("leafPositions", (DragStemLeafView)theView);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		boolean[] alreadyUsed = valuesUsed.getUsage();
		
		((UsedStemLeafList)theList).setAlreadyUsed(alreadyUsed);
		((UsedStemLeafList)theList).setLeafPosition(getDecimals(), getStemPower());
		theList.invalidate();
		
		((DragStemLeafView)theView).setAlreadyUsed(alreadyUsed);
		((DragStemLeafView)theView).setMaxLeaves(getMaxLeaves());
		((DragStemLeafView)theView).changeAxis(getAxisInfo());
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Click on each value in the list to display its leaf on the right.\nThen drag the leaf to its correct position on the stem and leaf plot.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Incomplete!\n");
				messagePanel.insertText("You have not added leaves for all values in the list.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("This is the correct stem and leaf plot for the data.\nClick any leaf digit to see the corresponding value in the list.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("Your stem and leaf plot is complete.");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Almost there!\n");
				messagePanel.insertText("All leaves have been added to the correct stems, but the highlighted stacks of leaves are not in increasing order. Drag leaves to reorder them.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The highlighted leaves are on the wrong stems.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected boolean[] inWrongStack() {
		return ((DragStemLeafView)theView).inWrongStack();
	}
	
	protected void showCorrectCrosses() {
		((DragStemLeafView)theView).showCorrectCrosses();
	}
	
	protected boolean[] inWrongPositions() {
		return ((DragStemLeafView)theView).inWrongPositions();
	}
}