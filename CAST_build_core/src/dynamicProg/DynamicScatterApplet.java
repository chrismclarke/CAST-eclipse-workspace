package dynamicProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.*;
import coreGraphics.*;

import transform.LogAxis;
import cat.*;
import dynamic.*;


public class DynamicScatterApplet extends ScatterApplet {
	static final private String YEARS_PARAM = "years";
	static final private String YEAR_LABELS_PARAM = "yearLabels";
	static final private String SIZE_VAR_NAME_PARAM = "sizeVarName";
	static final private String SIZE_VALUES_PARAM = "sizeValues";
	static final private String GROUP_NAME_PARAM = "groupName";
	static final private String GROUP_LABELS_PARAM = "groupLabels";
	static final private String GROUP_VALUES_PARAM = "groupValues";
	
	
	private DataSet data;
	
	private ScatterSeriesView theView;
	
	private int startYear, yearStep;
	private YearSlider yearSlider;
	
	private XChoice displayTypeChoice, selectionChoice;
	private int currentDisplayType = 0;
	private int currentSelectionType = 0;
	
	protected DataSet readData() {
		data = new DataSet();
		
			NumSeriesVariable yVar = new NumSeriesVariable(getParameter(Y_VAR_NAME_PARAM));
			yVar.readValues(getParameter(Y_VALUES_PARAM));
		
		data.addVariable("y", yVar);
		
			NumSeriesVariable xVar = new NumSeriesVariable(getParameter(X_VAR_NAME_PARAM));
			xVar.readValues(getParameter(X_VALUES_PARAM));
		
		data.addVariable("x", xVar);
		
			NumSeriesVariable sizeVar = new NumSeriesVariable(getParameter(SIZE_VAR_NAME_PARAM));
			sizeVar.readValues(getParameter(SIZE_VALUES_PARAM));
		
		data.addVariable("size", sizeVar);
		
		data.addLabelVariable("label", getParameter(LABEL_NAME_PARAM), getParameter(LABELS_PARAM));
		
			CatVariable groupVar = new CatVariable(getParameter(GROUP_NAME_PARAM));
			groupVar.readLabels(getParameter(GROUP_LABELS_PARAM));
			groupVar.readValues(getParameter(GROUP_VALUES_PARAM));
		
		data.addVariable("group", groupVar);
		
		return data;
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		String horizAxisString = getParameter(X_AXIS_INFO_PARAM);
		
		if (horizAxisString.substring(0, 4).equals("log ")) {
			LogAxis axis = new LogAxis(this);
			axis.readExtremes(horizAxisString.substring(4));
			axis.setTransValueDisplay(true);
			if (labelAxes)
				axis.setAxisName(getParameter(X_VAR_NAME_PARAM) + " (log scale)");
			return axis;
		}
		else
			return super.createHorizAxis(data);
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new ScatterSeriesView(data, this, theHorizAxis, theVertAxis, "x", "y", "size", "group");
		theView.setRetainLastSelection(true);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 4));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
				
				XPanel selectionPanel = new XPanel();
				selectionPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				
					XLabel selectLabel = new XLabel(translate("Selection") + ":", XLabel.LEFT, this);
					selectLabel.setFont(getStandardBoldFont());
				selectionPanel.add(selectLabel);
				
					selectionChoice = new XChoice(this);
					selectionChoice.addItem(translate("With mouse"));
					CatVariable groupVar = (CatVariable)data.getVariable("group");
					for (int i=0 ; i<groupVar.noOfCategories() ; i++)
						selectionChoice.addItem(groupVar.getLabel(i).toString());
				selectionPanel.add(selectionChoice);
				
			topPanel.add(selectionPanel);
				
				XPanel labelPanel = new XPanel();
				labelPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				
					XLabel labelLabel = new XLabel(data.getVariable("label").name, XLabel.LEFT, this);
					labelLabel.setFont(getStandardBoldFont());
				labelPanel.add(labelLabel);
					
					OneValueView labelView = new OneValueView(data, "label", this);
					labelView.setNameDraw(false);
				labelPanel.add(labelView);
				
			topPanel.add(labelPanel);
			
				XPanel displayTypePanel = new XPanel();
				displayTypePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
					
					XLabel typeLabel = new XLabel(translate("Display values as"), XLabel.LEFT, this);
					typeLabel.setFont(getStandardBoldFont());
				displayTypePanel.add(typeLabel);
				
					displayTypeChoice = new XChoice(this);
					displayTypeChoice.add(translate("Symbols"));
					displayTypeChoice.add(translate("Circles"));
				displayTypePanel.add(displayTypeChoice);
				
			topPanel.add(displayTypePanel);
			
		thePanel.add(topPanel);
		
			StringTokenizer st = new StringTokenizer(getParameter(YEARS_PARAM));
			startYear = Integer.parseInt(st.nextToken());
			int endYear = Integer.parseInt(st.nextToken());
			yearStep = Integer.parseInt(st.nextToken());
			
			st = new StringTokenizer(getParameter(YEAR_LABELS_PARAM));
			int startYearLabel = Integer.parseInt(st.nextToken());
			int labelStep = Integer.parseInt(st.nextToken());
			
			yearSlider = new YearSlider(translate("Year"), startYear, endYear, startYear, yearStep,
																													startYearLabel, labelStep, this);
			
		thePanel.add(yearSlider);
		
		return thePanel;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
			CatKey3View groupKey = new CatKey3View(data, this, "group");
			groupKey.setCatColour(ScatterSeriesView.kGroupColor);
			
		thePanel.add(groupKey);
		
		return thePanel;
	}
	
	public void notifyDataChange(DataView theView) {
		selectionChoice.select(0);
		currentSelectionType = 0;
	}
	
	private boolean localAction(Object target) {
		if (target == yearSlider) {
			double yearIndex = (yearSlider.getYear() - startYear) / (double)yearStep;
			
			NumSeriesVariable yVar = (NumSeriesVariable)data.getVariable("y");
			yVar.setSeriesIndex(yearIndex);
			
			NumSeriesVariable xVar = (NumSeriesVariable)data.getVariable("x");
			xVar.setSeriesIndex(yearIndex);
			
			NumSeriesVariable sizeVar = (NumSeriesVariable)data.getVariable("size");
			sizeVar.setSeriesIndex(yearIndex);
			
			data.valueChanged(0);		//	so that selection does not get altered
			
			return true;
		}
		else if (target == displayTypeChoice) {
			int newChoice = displayTypeChoice.getSelectedIndex();
			if (newChoice != currentDisplayType) {
				currentDisplayType = newChoice;
				
				theView.setDrawType(newChoice);
				theView.repaint();
			}
			return true;
		}
		else if (target == selectionChoice) {
			int newChoice = selectionChoice.getSelectedIndex();
			if (newChoice != currentSelectionType) {
				currentSelectionType = newChoice;
				
				CatVariable groupVar = (CatVariable)data.getVariable("group");
				int nVals = groupVar.noOfValues();
				boolean selection[] = new boolean[nVals];		//	all false
				for (int i=0 ; i<nVals ; i++)
					selection[i] = groupVar.getItemCategory(i) == (newChoice - 1);
				
				data.setSelection(selection);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}