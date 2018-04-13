package exerciseCategProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import exercise2.*;

import exerciseCateg.*;


public class BuildBarChartApplet extends CoreBuildChartApplet {
	private XLabel countLabel;
	private VertAxis countAxis;
	private HorizAxis xAxis;
	private DragBarView barView;
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("countAxis", "string");
		registerParameter("countLabel", "string");
	}
	
	private String getCountAxis() {
		return getStringParam("countAxis");
	}
	
	private String getCountLabel() {
		return getStringParam("countLabel");
	}
	
	
//-----------------------------------------------------------
	
	
	protected double getMaxAllowedCount() {
		StringTokenizer st = new StringTokenizer(getCountAxis());
		st.nextToken();
		return Double.parseDouble(st.nextToken());
	}
	
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				freqTable = new FrequencyTableView(data, this, "y");
				freqTable.setFont(getBigFont());
				
			leftPanel.add(freqTable);
				
		thePanel.add("West", leftPanel);
			
		thePanel.add("Center", scatterPanel(data));
		
		return thePanel;
	}
	
	private XPanel scatterPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			countLabel = new XLabel("", XLabel.LEFT, this);
		thePanel.add("North", countLabel);
			
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new AxisLayout());
			
				xAxis = new HorizAxis(this);
			displayPanel.add("Bottom", xAxis);
			
				countAxis = new VertAxis(this);
			displayPanel.add("Left", countAxis);
			
				barView = new DragBarView(data, this, "y", xAxis, countAxis);
				barView.lockBackground(Color.white);
				registerStatusItem("counts", barView);
			displayPanel.add("Center", barView);
			
		thePanel.add("Center", displayPanel);
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		countAxis.readNumLabels(getCountAxis());
		
		xAxis.setCatLabels((CatVariable)data.getVariable("y"));
		
		String labelString = getCountLabel();
		if (labelString == null)
			labelString = translate("Count");
		countLabel.setText(labelString);
		freqTable.setCountString(labelString);
		
		data.variableChanged("y");
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Drag the red arrows at the top of the bars to draw the bar chart.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The height of each bar is equal to the corresponding count in the frequency table.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly drawn the bar chart.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("The yellow bars have the wrong height. Each height should be equal to its category count in the frequency table.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed) {
			barView.clearSelection();
		}
		return changed;
	}
	
	protected int assessAnswer() {
		boolean wrong[] = barView.wrongBars();
		boolean allOK = true;
		for (int i=0 ; i<wrong.length ; i++)
			allOK = allOK && !wrong[i];
		
		return allOK ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		if (result == ANS_WRONG) {
			boolean wrong[] = barView.wrongBars();
			
			barView.setSelectedBars(wrong);
			barView.repaint();
			
			freqTable.selectCats(wrong);
			freqTable.repaint();
		}
	}
	
	protected void showCorrectWorking() {
		barView.setCorrectCounts();
		barView.clearSelection();
		barView.repaint();
		
		freqTable.clearSelection();
		freqTable.repaint();
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
	
}