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


public class ScatterHistoApplet extends ScatterApplet {
	static final private Color kDarkBlue = new Color(0x000099);
	
	static final private String X_CLASS_INFO_PARAM = "xClassInfo";
	static final private String Y_CLASS_INFO_PARAM = "yClassInfo";
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		
		HistoView xHistoView = createMargin(theHorizAxis, X_CLASS_INFO_PARAM);
		xHistoView.setActiveNumVariable("x");
		thePanel.add("BottomMargin", xHistoView);
		
		HistoView yHistoView = createMargin(theVertAxis, Y_CLASS_INFO_PARAM);
		yHistoView.setActiveNumVariable("y");
		thePanel.add("LeftMargin", yHistoView);
		
		return thePanel;
	}
	
	private HistoView createMargin(NumCatAxis axis, String paramString) {
		String classInfo = getParameter(paramString);
		StringTokenizer theParams = new StringTokenizer(classInfo);
		double class0Start = Double.parseDouble(theParams.nextToken());
		double classWidth = Double.parseDouble(theParams.nextToken());
		
		HistoView theView = new HistoView(data, this, axis, new MarginalHistoInfo(), class0Start,
																																							classWidth);
		theView.setBarType(HistoView.VERT_BARS);
		return theView;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
			OneValueView yValueView = new OneValueView(data, "y", this);
				yValueView.addEqualsSign();
//			yValueView.setFont(getBigFont());
		thePanel.add(yValueView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 8));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
				OneValueView xValueView = new OneValueView(data, "x", this);
				xValueView.addEqualsSign();
			topPanel.add(xValueView);
			
		thePanel.add(topPanel);
		
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
		
		return thePanel;
	}
}