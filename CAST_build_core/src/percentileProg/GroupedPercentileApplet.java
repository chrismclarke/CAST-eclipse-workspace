package percentileProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import percentile.*;


public class GroupedPercentileApplet extends GroupedQuartileApplet {
	static final protected String BOXPLOT_OPTIONS_PARAM = "boxPlotOptions";
	
	private PercentilesSlider percentileSlider;
	
	private boolean hasBoxPlotOptions = false;
	
	private XPanel sliderPanel;
	private CardLayout sliderPanelLayout;
	
	protected XPanel dataDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
				CatVariable xVar = (CatVariable)data.getVariable("x");
			horizAxis.setCatLabels(xVar);
			horizAxis.setAxisName(xVar.name);
		thePanel.add("Bottom", horizAxis);
		
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
		thePanel.add("Left", vertAxis);
		
			theView = new GroupedPercentileView(data, this, vertAxis, horizAxis);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		String boxPlotOptionsString = getParameter(BOXPLOT_OPTIONS_PARAM);
		hasBoxPlotOptions = (boxPlotOptionsString != null && boxPlotOptionsString.equals("true"));
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 20));
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				displayChoice = new XChoice(this);
			
				displayChoice.addItem(translate("Jittered dot plots"));
				if (hasBoxPlotOptions) {
					displayChoice.addItem(translate("Box plots"));
					displayChoice.addItem(translate("Box plots and quartile bands"));
				}
				displayChoice.addItem(translate("Dot plots and percentile bands"));
				displayChoice.addItem(translate("Percentile bands only"));
			choicePanel.add(displayChoice);
			
		thePanel.add(ProportionLayout.LEFT, choicePanel);
		
			sliderPanel = new XPanel();
			sliderPanelLayout = new CardLayout();
			sliderPanel.setLayout(sliderPanelLayout);
			
			sliderPanel.add("blank", new XPanel());
			
				XPanel innerPanel = new XPanel();
				innerPanel.setLayout(new BorderLayout(0, 0));
				
					percentileSlider = new PercentilesSlider(this, (GroupedPercentileView)theView);
					percentileSlider.setFont(getStandardBoldFont());
				innerPanel.add(percentileSlider);
				
			sliderPanel.add("slider", innerPanel);
			sliderPanelLayout.show(sliderPanel, "blank");
			
		thePanel.add(ProportionLayout.RIGHT, sliderPanel);
		
		return thePanel;
	}
	
	private int menuChoiceToDisplayType(int menuChoice) {
		if (hasBoxPlotOptions)
			return (menuChoice == 0) ? GroupedPercentileView.DOT_PLOT
											: (menuChoice == 1) ? GroupedPercentileView.BOX_PLOT
											: (menuChoice == 2) ? GroupedPercentileView.QUARTILE_AND_BOX_PLOT
											: (menuChoice == 3) ? GroupedPercentileView.PERCENTILE_AND_DOT_PLOT
											: GroupedPercentileView.PERCENTILE_PLOT;
		else
			return (menuChoice == 0) ? GroupedPercentileView.DOT_PLOT
											: (menuChoice == 1) ? GroupedPercentileView.PERCENTILE_AND_DOT_PLOT
											: GroupedPercentileView.PERCENTILE_PLOT;
	}
	
	private boolean showingQuartiles(int menuChoice) {
		int displayType = menuChoiceToDisplayType(menuChoice);
		return displayType == GroupedPercentileView.BOX_PLOT
													|| displayType == GroupedPercentileView.QUARTILE_AND_BOX_PLOT;
	}
	
	private boolean showingQuantiles(int menuChoice) {
		int displayType = menuChoiceToDisplayType(menuChoice);
		return displayType == GroupedPercentileView.PERCENTILE_AND_DOT_PLOT
													|| displayType == GroupedPercentileView.PERCENTILE_PLOT;
	}

	
	private boolean localAction(Object target) {
		if (target == displayChoice) {
			int newChoice = displayChoice.getSelectedIndex();
			if (newChoice != currentDisplay) {
				if (showingQuartiles(currentDisplay) && showingQuantiles(newChoice))
					percentileSlider.setBoxValue();
				currentDisplay = newChoice;
				int displayType = menuChoiceToDisplayType(newChoice);
				theView.setPlotType(displayType);
				
				sliderPanelLayout.show(sliderPanel, showingQuantiles(newChoice) ? "slider" : "blank");
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