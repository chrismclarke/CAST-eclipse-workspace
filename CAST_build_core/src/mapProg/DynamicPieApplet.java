package mapProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import dynamic.*;


public class DynamicPieApplet extends PieRegionsApplet {
	static final private String YEARS_PARAM = "years";
	static final private String YEAR_LABELS_PARAM = "yearLabels";
	
//	static final private Color kDarkBlue = new Color(0x000099);
	
	private int startYear, yearStep;
	private YearSlider yearSlider;
	
	protected void addNumVariable(DataSet data, String varName, String varKey, String valuesString) {
		NumSeriesVariable yVar = new NumSeriesVariable(varName);
		yVar.readValues(valuesString);
		
		data.addVariable(varKey, yVar);
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
		thePanel.setLayout(new BorderLayout(0, 7));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(20, 0));
		
			topPanel.add("Center", radiusPanel(data));
			topPanel.add("East", valuePanel(data));
		
		thePanel.add("Center", topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(50, 0));
			
			bottomPanel.add("West", keyPanel(data));
			bottomPanel.add("Center", yearPanel(data));
			
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == yearSlider) {
			double yearIndex = (yearSlider.getYear() - startYear) / (double)yearStep;
			
			for (int i=0 ; i<yKey.length ; i++) {
				NumSeriesVariable yVar = (NumSeriesVariable)data.getVariable(yKey[i]);
				yVar.setSeriesIndex(yearIndex);
			}
			
			data.variableChanged("total");
			
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