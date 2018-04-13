package residTwoProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multiRegn.*;
import residTwo.*;


public class PartialResidApplet extends ResidPlotXZApplet {
	static final private String PARTIAL_RESID_AXIS_PARAM = "partialResidAxis";
	
//	private VertAxis partialResidAxis;
	
	private XButton partialRotateButton;
	
	protected MultiRegnDataSet readData() {
		MultiRegnDataSet data = super.readData();
		
		data.addVariable("partialResid", new PartialResidVariable("PartialResid", data,
																									MultiRegnDataSet.xKeys, "y", "ls", 0, 9));
		
		return data;
	}
	
	protected XPanel rotatePanel(Rotate3DView theView) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																												VerticalLayout.VERT_CENTER, 2));
		thePanel.add(super.rotatePanel(theView));
		
			partialRotateButton = new XButton(translate("Rotate to Comp+Resid"), this);
		thePanel.add(partialRotateButton);
		return thePanel;
	}
	
	protected XChoice getYChoice(MultiRegnDataSet data) {
		XChoice yChoice = new XChoice(this);
		yChoice.addItem(translate("Residual"));
		yChoice.addItem(translate("Component plus residual"));
		return yChoice;
	}
	
	protected XPanel scatterPanel(MultiRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			horizAxis = getHorizAxis(data);
		thePanel.add("Bottom", horizAxis);
		
			vertAxis = new MultiVertAxis(this, 2);
			vertAxis.readNumLabels(getParameter(RESID_AXIS_INFO_PARAM));
			vertAxis.readExtraNumLabels(getParameter(PARTIAL_RESID_AXIS_PARAM));
			vertAxis.setChangeMinMax(false);
		thePanel.add("Left", vertAxis);
		
			VertAxis dummyVertAxis = new VertAxis(this);
			dummyVertAxis.readNumLabels(getFakeVertAxisInfo(PARTIAL_RESID_AXIS_PARAM));
			dummyVertAxis.setShowUnlabelledAxis(false);
		thePanel.add("Right", dummyVertAxis);
		
			d2View = new PartialResidScatterView(data, this, horizAxis, vertAxis, dummyVertAxis,
																												"x", "resid", "partialResid", d3View);
			d2View.setRetainLastSelection(true);
			d2View.lockBackground(Color.white);
		
			MultipleRegnModel theModel = (MultipleRegnModel)data.getVariable("ls");
			double slope = theModel.getParameter(1).toDouble();
			((PartialResidScatterView)d2View).setPartialSlope(slope);
			
		thePanel.add("Center", d2View);
		
		return thePanel;
	}
	
	protected void setHorizVariable() {
		PartialResidVariable partResid = (PartialResidVariable)data.getVariable("partialResid");
		partResid.setComponentIndex(currentXChoice);
		
		MultipleRegnModel theModel = (MultipleRegnModel)data.getVariable("ls");
		double slope = theModel.getParameter(currentXChoice + 1).toDouble();
		((PartialResidScatterView)d2View).setPartialSlope(slope);
		
		super.setHorizVariable();
	}
	
	private void rotateToPartial(boolean xNotZ) {
		MultipleRegnModel theModel = (MultipleRegnModel)data.getVariable("ls");
		double roundDens = xNotZ ? 0 : 90;
		
		double slope = xNotZ ? theModel.getParameter(2).toDouble() : -theModel.getParameter(1).toDouble();
		double yAxisLength = yAxis.getMaxOnAxis() - yAxis.getMinOnAxis();
		D3Axis otherAxis = xNotZ ? zAxis : xAxis;
		double otherAxisLength = otherAxis.getMaxOnAxis() - otherAxis.getMinOnAxis();
		
		double tan = slope * otherAxisLength / yAxisLength;
		double ofDens = 180 / Math.PI * Math.atan(tan);
		if (ofDens < 0)
			ofDens = 360 + ofDens;
		
		d3View.animateRotateTo(roundDens, ofDens);
	}
	
	protected void doShowColors() {
		d3View.setColoredAxis((currentXChoice == 0) ? ColoredXZView.X_COLOR
																								: ColoredXZView.Z_COLOR);
		d3View.repaint();
		d2View.setColorKey((currentXChoice == 0) ? "x" : "z");
		d2View.repaint();
	}

	
	private boolean localAction(Object target) {
		if (target == partialRotateButton) {
			rotateToPartial(currentXChoice == 0);
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

