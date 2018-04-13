package scatterProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.OneValueView;
import coreGraphics.*;

import histo.HistoView;
import scatter.*;


public class ScatterMarginApplet extends ScatterApplet {
	static final private Color kDarkBlue = new Color(0x000099);
	
	static final private String X_CLASS_INFO_PARAM = "xClassInfo";
	static final private String Y_CLASS_INFO_PARAM = "yClassInfo";
	
	private XPanel displayPanel;
	private CardLayout displayCardLayout;
	
	private XChoice marginChoice;
	private int selectedMargin = 0;
	
	public void setupApplet() {
		data = readData();
		labelAxes = true;
		
		setLayout(new BorderLayout(10, 0));
		add("Center", displayPanel(data));
		
		add("South", controlPanel(data));
		add("North", topPanel(data));
	}
	
	protected XPanel displayPanel(DataSet data) {
		displayPanel = new XPanel();
		displayCardLayout = new CardLayout();
		displayPanel.setLayout(displayCardLayout);
		
		displayPanel.add("std", super.displayPanel(data));
		displayPanel.add("dot", dotPanel(data));
		displayPanel.add("histo", histoPanel(data));
		displayPanel.add("box", boxPanel(data));
		
		displayCardLayout.show(displayPanel, "std");
		return displayPanel;
	}
	
	private XPanel dotPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		
		DotPlotView xDotView = new DotPlotView(data, this, theHorizAxis, 0.6);
		xDotView.setActiveNumVariable("x");
		thePanel.add("BottomMargin", xDotView);
		
		DotPlotView yDotView = new DotPlotView(data, this, theVertAxis, 0.6);
		yDotView.setActiveNumVariable("y");
		thePanel.add("LeftMargin", yDotView);
		
		return thePanel;
	}
	
	private XPanel histoPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		
		HistoView xHistoView = createHistoMargin(theHorizAxis, X_CLASS_INFO_PARAM);
		xHistoView.setActiveNumVariable("x");
		thePanel.add("BottomMargin", xHistoView);
		
		HistoView yHistoView = createHistoMargin(theVertAxis, Y_CLASS_INFO_PARAM);
		yHistoView.setActiveNumVariable("y");
		thePanel.add("LeftMargin", yHistoView);
		
		return thePanel;
	}
	
	private HistoView createHistoMargin(NumCatAxis axis, String paramString) {
		String classInfo = getParameter(paramString);
		StringTokenizer theParams = new StringTokenizer(classInfo);
		double class0Start = Double.parseDouble(theParams.nextToken());
		double classWidth = Double.parseDouble(theParams.nextToken());
		
		HistoView theView = new HistoView(data, this, axis, new MarginalHistoInfo(), class0Start,
																																							classWidth);
		theView.setBarType(HistoView.VERT_BARS);
		return theView;
	}
	
	private XPanel boxPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		
		BoxView xBoxView = new BoxView(data, this, theHorizAxis);
		xBoxView.setActiveNumVariable("x");
		thePanel.add("BottomMargin", xBoxView);
		
		BoxView yBoxView = new BoxView(data, this, theVertAxis);
		yBoxView.setActiveNumVariable("y");
		thePanel.add("LeftMargin", yBoxView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 8));
		
		if (data.getVariable("label") != null) {
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
			OneValueView labelView = new OneValueView(data, "label", this);
			labelView.setForeground(kDarkBlue);
			labelView.setFont(getStandardBoldFont());
			labelView.addEqualsSign();
			bottomPanel.add(labelView);
			thePanel.add(bottomPanel);
		}
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				marginChoice = new XChoice(translate("Margin type") + ":", XChoice.HORIZONTAL, this);
				marginChoice.addItem(translate("No margins"));
				marginChoice.addItem(translate("Dot plots"));
				marginChoice.addItem(translate("Histograms"));
				marginChoice.addItem(translate("Box plots"));
				marginChoice.select(0);
			choicePanel.add(marginChoice);
		
		thePanel.add(choicePanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == marginChoice) {
			int newMargin = marginChoice.getSelectedIndex();
			if (newMargin != selectedMargin) {
				selectedMargin = newMargin;
				displayCardLayout.show(displayPanel, (newMargin == 0) ? "std"
																				: (newMargin == 1) ? "dot"
																				: (newMargin == 2) ? "histo" : "box");
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