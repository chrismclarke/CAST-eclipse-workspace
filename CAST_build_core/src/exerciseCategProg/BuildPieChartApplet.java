package exerciseCategProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;

import cat.*;
import exerciseCateg.*;


public class BuildPieChartApplet extends CoreBuildChartApplet {
	private PieDrawer pieDrawer = new PieDrawer();
	private CatKey3View keyView;
	private DragPieView pieView;
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("oneOrCum", "choice");
	}
	
	private int getOneOrCum() {
		return getIntParam("oneOrCum");
	}
	
	
//-----------------------------------------------------------
	
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
			
				freqTable = new FrequencyTableView(data, this, "y");
				freqTable.setHasCumColumn(true);
				freqTable.setFont(getBigFont());
				
			leftPanel.add(freqTable);
			
				keyView = new CatKey3View(data, this, "y");
				keyView.setShowHeading(false);
				keyView.setFont(getBigFont());
			leftPanel.add(keyView);
				
		thePanel.add("West", leftPanel);
			
		thePanel.add("Center", piePanel(data));
		
		return thePanel;
	}
	
	private XPanel piePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			pieView = new DragPieView(data, this, "y", pieDrawer);
			Font pieFont = new Font(FONT, Font.BOLD, getStandardFont().getSize() * 3 / 2);
			pieView.setFont(pieFont);
			registerStatusItem("cumCounts", pieView);
		
		thePanel.add("Center", pieView);
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		permute(pieDrawer.getColorPerm());
		
		CatVariable yVar = (CatVariable)data.getVariable("y");
		int nCats = yVar.noOfCategories();
		Color catColor[] = new Color[nCats];
		for (int i=0 ; i<nCats ; i++)
			catColor[i] = pieDrawer.getCatColor(i);
		keyView.setCatColour(catColor);
		
		pieView.setDragCumNotOne(getOneOrCum() == 0);
		pieView.clearWrongCats();
		pieView.setDefaultCounts();
		
		data.variableChanged("y");
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Drag the straight boundaries between the pie chart segments to adjust the pie chart (except for the 12-o'clock boundary). ");
				messagePanel.insertText((getOneOrCum() == 0) ? "The cumulative proportion for each category is displayed while its upper boundary is dragged."
																			 : "The proportion for each category is displayed while its upper boundary is dragged.");
				messagePanel.insertBoldText("\nHints: ");
				messagePanel.insertText("Work clockwise from the top category in the frequency table. If the total count for the table is large, it is easier to get the proportions correct if you drag near the circumference of the pie chart.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The angle (and area) of each pie slice is equal to the corresponding count in the frequency table.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("The angle (and area) of each pie slice is equal to the corresponding count in the frequency table.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("The pie segments have the wrong size for the categories that are highlighted in yellow in the table and pie chart.");
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
			pieView.clearWrongCats();
		}
		return changed;
	}
	
	protected int assessAnswer() {
		boolean wrong[] = pieView.wrongCats();
		boolean allOK = true;
		for (int i=0 ; i<wrong.length ; i++)
			allOK = allOK && !wrong[i];
		
		return allOK ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		if (result == ANS_WRONG) {
			boolean wrong[] = pieView.wrongCats();
			
			pieView.setWrongCats(wrong);
			pieView.repaint();
			
			freqTable.selectCats(wrong);
			freqTable.repaint();
		}
	}
	
	protected void showCorrectWorking() {
		pieView.setCorrectCounts();
		pieView.clearWrongCats();
		pieView.repaint();
		
		freqTable.clearSelection();
		freqTable.repaint();
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
	
}