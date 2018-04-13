package linModProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import linMod.*;


public class RotateModelApplet extends RotatePDFApplet {
	static final private String X_LIMITS_PARAM = "xLimits";
	
	private XValueSlider xSlider;
	
	protected NumValue minX, maxX, xStep, startX;
	
	public void setupApplet() {
		StringTokenizer theParams = new StringTokenizer(getParameter(X_LIMITS_PARAM));
		minX = new NumValue(theParams.nextToken());
		maxX = new NumValue(theParams.nextToken());
		xStep = new NumValue(theParams.nextToken());
		startX = new NumValue(theParams.nextToken());
		super.setupApplet();
	}
	
	protected Rotate3DView getView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis densityAxis) {
		RotateDragXView theView =  new RotateDragXView(data, this, xAxis, yAxis, densityAxis, "model", "x", "y", startX);
		theView.setModelDrawType(RotateDragXView.DRAW_BAND_PDF);
		theView.setBigHitRadius();
		return theView;
	}
	
	protected void addChoice(XPanel targetPanel) {
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL));
			
				topPanel.add(ProportionLayout.TOP, rotationPanel());
				
				XPanel samplingPanel = new XPanel();
				samplingPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
				
				addChoice(samplingPanel);
				addSampleButton(samplingPanel);
				
				topPanel.add(ProportionLayout.BOTTOM, samplingPanel);
			thePanel.add("Center", topPanel);
			
			xSlider = new XValueSlider(minX, maxX, xStep, startX, this);
			thePanel.add("South", xSlider);
		
		return thePanel;
	}
	
	protected void takeSample() {
		((RotateDragXView)theView).setShowData(true);
		super.takeSample();
	}
	
	private boolean localAction(Object target) {
		if (target == xSlider) {
			NumValue newX = xSlider.getNumValue();
			((RotateDragXView)theView).setPDFDrawX(newX);
			theView.repaint();
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