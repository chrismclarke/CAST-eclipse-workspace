package regnProg;

import java.awt.*;

import axis.*;
import dataView.*;
import coreGraphics.*;

import regn.*;
import regnView.*;


public class MedianTraceApplet extends ScatterApplet {
	static final protected String BOUNDARY_PARAM = "boundary";
	
	private MedianTraceView theView;
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 3));
		
		ClassMedianView xMedianView = new ClassMedianView(data, this, "x");
		ClassMedianView yMedianView = new ClassMedianView(data, this, "y");
		theView.setMedianViews(xMedianView, yMedianView);
		
		thePanel.add(yMedianView);
		thePanel.add(xMedianView);
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new MedianTraceView(data, this, theHorizAxis, theVertAxis, "x", "y", getParameter(BOUNDARY_PARAM));
		return theView;
	}
}