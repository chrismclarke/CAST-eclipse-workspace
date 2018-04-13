package mapProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import dynamic.*;


public class DynamicCirclesApplet extends CircleRegionsApplet {
	static final private String YEARS_PARAM = "years";
	static final private String YEAR_LABELS_PARAM = "yearLabels";
	
	private int startYear, yearStep;
	private YearSlider yearSlider;
	
	protected DataSet getData() {
		DataSet data = getMapData();
			
			NumSeriesVariable sizeVar = new NumSeriesVariable(getParameter(SIZE_VAR_NAME_PARAM));
			sizeVar.readValues(getParameter(SIZE_VALUES_PARAM));
		
		data.addVariable("size", sizeVar);
			
			NumSeriesVariable densityVar = new NumSeriesVariable(getParameter(DENSITY_VAR_NAME_PARAM));
			densityVar.readValues(getParameter(DENSITY_VALUES_PARAM));
		
		data.addVariable("density", densityVar);
		
		return data;
	}
	
	protected XPanel yearPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
	
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
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.5, 20));
			topPanel.add(ProportionLayout.LEFT, radiusPanel(data));
			topPanel.add(ProportionLayout.RIGHT, yearPanel(data));
			
		thePanel.add("Center", topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
			
			bottomPanel.add(valuePanel(data));
			bottomPanel.add(densityKeyPanel(data));
			
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == yearSlider) {
			double yearIndex = (yearSlider.getYear() - startYear) / (double)yearStep;
			
			NumSeriesVariable sizeVar = (NumSeriesVariable)data.getVariable("size");
			sizeVar.setSeriesIndex(yearIndex);
			
			NumSeriesVariable densityVar = (NumSeriesVariable)data.getVariable("density");
			densityVar.setSeriesIndex(yearIndex);
			
			data.variableChanged("size");
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
	
}