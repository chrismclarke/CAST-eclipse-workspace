package regnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;

import regn.*;
import regnView.*;


public class DragLineRssApplet extends DragLineApplet {
	static final protected String BIGGEST_RSS_PARAM = "biggestRss";
	static final protected String DATA_NAMES_PARAM = "dataNames";
	static final protected String PIXEL_SQUARE_PARAM = "pixelSquare";
	
	private XButton lsButton;
	private XChoice dataChoice;
	private int dataSetIndex;
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		DragLine2View theView = new DragLine2View(data, this, theHorizAxis, theVertAxis, "x", "y", "model");
		String squarePixString = getParameter(PIXEL_SQUARE_PARAM);
		if (squarePixString != null && squarePixString.equals("true"))
			theView.setPixelSquare(true);
		return theView;
	}

	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		XPanel topPanel = new XPanel();
		topPanel.setLayout(new BorderLayout(0, 0));
		topPanel.add("Center", super.controlPanel(data));
		
		String dataNames = getParameter(DATA_NAMES_PARAM);
		if (dataNames != null) {
			StringTokenizer st = new StringTokenizer(dataNames, "#");
			dataChoice = new XChoice(this);
			while (st.hasMoreTokens())
				dataChoice.addItem(st.nextToken());
			dataChoice.select(0);
			dataSetIndex = 0;
			topPanel.add("West", dataChoice);
		}
		
		thePanel.add("North", topPanel);
		
		XPanel bottomPanel = new XPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 3));
		
		NumValue biggestRss = new NumValue(getParameter(BIGGEST_RSS_PARAM));
		bottomPanel.add(new ResidSsqView(data, "x", "y", "model", biggestRss, this));
		
		lsButton = new XButton(translate("Least squares"), this);
		bottomPanel.add(lsButton);
		
		thePanel.add("South", bottomPanel);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == lsButton) {
			LinearModel model = (LinearModel)data.getVariable("model");
			model.setLSParams("y", intStart.decimals, slopeStart.decimals, 0);
			data.variableChanged("model");
			return true;
		}
		else if (target == dataChoice) {
			int chosenIndex = dataChoice.getSelectedIndex();
			if (chosenIndex != dataSetIndex) {
				dataSetIndex = chosenIndex;
				NumVariable y = (NumVariable)data.getVariable("y");
				
				String valueParam = "y" + ((chosenIndex == 0) ? "" : Integer.toString(chosenIndex + 1)) + "Values";
				y.readValues(getParameter(valueParam));
				data.variableChanged("y");
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