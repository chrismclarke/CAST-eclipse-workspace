package exerciseNumGraphProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import exercise2.*;
import valueList.*;

import exerciseNumGraph.*;


public class BuildHistoApplet extends BuildBoxPlotApplet {
	static final private Color kOddClassListColor = new Color(0xDDDDDD);
	
	private VertAxis countAxis;
	private DragHistoView histoView;
	
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("classInfo", "string");
		super.registerParameterTypes();
	}
	
	protected String getClassInfo() {
		return getStringParam("classInfo");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			theList = new ScrollValueList(data, this, ScrollValueList.HEADING);
			theList.addVariableToList("y", ScrollValueList.RAW_VALUE);
			theList.sortByVariable("y", ScrollValueList.SMALL_FIRST);
			theList.setCanSelectRows(false);
			theList.setSelectionColors(kOddClassListColor, null, null);
		thePanel.add("West", theList);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout(0, 0));
			
			if (!hasOption("mixedClassWidths")) {
				XLabel freqLabel = new XLabel(translate("Frequency"), XLabel.LEFT, this);
				rightPanel.add("North", freqLabel);
			}
			
				XPanel histoPanel = new XPanel();
				histoPanel.setLayout(new AxisLayout());
				
					valAxis = new HorizAxis(this);
				histoPanel.add("Bottom", valAxis);
				
					countAxis = new VertAxis(this);
				histoPanel.add("Left", countAxis);
				
					histoView = new DragHistoView(data, this, "y", valAxis, countAxis);
					histoView.lockBackground(Color.white);
					registerStatusItem("classCount", histoView);
				histoPanel.add("Center", histoView);
			
			rightPanel.add("Center", histoPanel);
			
		thePanel.add("Center", rightPanel);
		
		return thePanel;
	}
	
	private class HistoClassInfo {
		double class0Start, classWidth;
		int baseMultiple = 1;
		int classMultiples[] = null;
		
		HistoClassInfo() {
			StringTokenizer st = new StringTokenizer(getClassInfo());
			class0Start = Double.parseDouble(st.nextToken());
			classWidth = Double.parseDouble(st.nextToken());
			if (st.hasMoreTokens()) {
				baseMultiple = Integer.parseInt(st.nextToken());
				classMultiples = new int[st.countTokens()];
				for (int i=0 ; i<classMultiples.length ; i++)
					classMultiples[i] = Integer.parseInt(st.nextToken());
			}
		}
		
		private boolean matches(int yInt, double factor, double boundary) {
			int boundaryInt = (int)Math.round(boundary * factor);
			return (yInt == boundaryInt);
		}
		
		boolean onClassBoundary(double y, int decimals) {
			double factor = Math.pow(10.0, decimals);
			int yInt = (int)Math.round(y * factor);
			double boundary = class0Start;
			
			if (matches(yInt, factor, boundary))
				return true;
			
			if (classMultiples == null)
				while (boundary < y) {
					boundary += classWidth;
					if (matches(yInt, factor, boundary))
						return true;
				}
			else {
				for (int i=0 ; i<classMultiples.length ; i++) {
					boundary += classMultiples[i] * baseMultiple * classWidth;
					if (matches(yInt, factor, boundary))
						return true;
				}
			}
			
			return false;
		}
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		HistoClassInfo classInfo = new HistoClassInfo();
		histoView.changeClasses(classInfo.class0Start, classInfo.classWidth, classInfo.baseMultiple,
																																				classInfo.classMultiples);
		histoView.clearSelection();
	
		if (hasOption("mixedClassWidths"))			//	always show groups
			data.setSelection(histoView.getClassGroups());
			
		int maxCount = histoView.maxCount();
		String axisString = (maxCount < 10) ? "0 10 0 1"
												: (maxCount < 15) ? "0 15 0 2"
												: (maxCount < 20) ? "0 20 0 2"
												: (maxCount < 30) ? "0 30 0 5"
												: "0 50 0 5";
		countAxis.readNumLabels(axisString);
		countAxis.invalidate();
	}
	
	protected void setDataForQuestion() {
		HistoClassInfo classInfo = new HistoClassInfo();
		do {
			super.setDataForQuestion();
		} while (valuesOnClassBoundary(classInfo));
	}
	
	private boolean valuesOnClassBoundary(HistoClassInfo classInfo) {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		int decimals = getDecimals();
		ValueEnumeration ye = yVar.values();
		while (ye.hasMoreValues()) {
			if (classInfo.onClassBoundary(ye.nextDouble(), decimals)) {
//				System.out.println("rejected data set");
				return true;
			}
		}
		return false;
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Drag the top of class rectangles to create a histogram of the data.\n");
				messagePanel.insertBoldRedText("(Any values on class boundaries should be placed in the class above.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("This is the correct histogram for the data.");
				if (hasOption("mixedClassWidths"))
					messagePanel.insertText("\nThe height for classes whose width is twice that of the base classes should be half the number of values in the class. Classes half the width of the base classes should have height that is double the frequency.");
				else
					messagePanel.insertText("\nThe height of each rectangle equals the number of values in the class.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("This is the correct histogram for the data.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The yellow rectangles have the wrong height.\n");
				if (hasOption("mixedClassWidths"))
					messagePanel.insertText("Classes that are narrower than the fixed classes (pale blue) should be taller than their frequencies suggest; wider classes should be shorter.");
				else
					messagePanel.insertText("Use the highlighting of values in the list to help count the number of values in each histogram class.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed)
			histoView.clearSelection();
		return changed;
	}
	
	protected int assessAnswer() {
		boolean wrong[] = histoView.wrongBars();
		boolean allOK = true;
		for (int i=0 ; i<wrong.length ; i++)
			allOK = allOK && !wrong[i];
		
		return allOK ?  ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		if (result == ANS_WRONG) {
			boolean wrong[] = histoView.wrongBars();
			histoView.setSelectedBars(wrong);
			histoView.repaint();
		}
		
		data.setSelection(histoView.getClassGroups());
	}
	
	protected void showCorrectWorking() {
		histoView.setCorrectCounts();
		histoView.clearSelection();
		
		data.setSelection(histoView.getClassGroups());
		histoView.repaint();
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
}