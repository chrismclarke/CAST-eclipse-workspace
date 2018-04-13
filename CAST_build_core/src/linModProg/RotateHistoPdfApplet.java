package linModProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import linMod.*;


public class RotateHistoPdfApplet extends RotateApplet {
	static final private String CLASS_INFO_PARAM = "classInfo";
	static final private String SORTED_X_PARAM = "sortedX";
	static final private String JITTERING_PARAM = "jittering";
	static final private String X_CAT_NAMES_PARAM = "xCatNames";
	static final private String MODEL_PARAM = "model";
	static final private String INV_PDF_SCALING_PARAM = "invPdfScaling";
									//	scaling of pdf must be manually adjusted to make pdf area = histo area
	
									//	For categorical X, we also need numerical version to get RotateHistoPdfView
									//		to draw histogram at correct place.
									//	Messy hack to get applet to work with multiple groups
	
	static final protected String kDefaultZAxis = "0.0 1.0 2.0 1.0";
	
	static final private Color kMeanLineColor = Color.blue;
	
	private boolean categoricalX;
	
	private XChoice dataModelChoice;
	private int currentDataModelChoice = 0;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
		String xCatLabels = getParameter(X_CAT_NAMES_PARAM);
		categoricalX = xCatLabels != null;
		if (categoricalX)
			data.addCatVariable("xCat", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM),
																																											xCatLabels);
		CoreModelVariable yDistn;
		if (categoricalX) {
			yDistn = new GroupsModelVariable(getParameter(Y_VAR_NAME_PARAM), data, "xCat");
			((GroupsModelVariable)yDistn).setUsePooledSd(true);
		}
		else {
			String modelTypeString = getParameter(MODEL_PARAM);
			if (modelTypeString.equals("linear"))
				yDistn = new LinearModel(getParameter(Y_VAR_NAME_PARAM), data, "x");
			else
				yDistn = new QuadraticModel(getParameter(Y_VAR_NAME_PARAM), data, "x");
		}
		yDistn.updateLSParams("y");
		data.addVariable("model", yDistn);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(getParameter(X_VAR_NAME_PARAM), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		if (categoricalX) {
			CatVariable xCat = (CatVariable)data.getVariable("xCat");
			xAxis.setNumScale("-0.5 " + (xCat.noOfCategories() - 0.5) + " 0 1");
																		//	to set min and max for drawing histo
			xAxis.setCatScale(xCat);			//	replaces values by strings
		}
		else
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
		
		D3Axis yAxis = new D3Axis(getParameter(Y_VAR_NAME_PARAM), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		D3Axis zAxis = new D3Axis("", D3Axis.Y_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(kDefaultZAxis);
		
			double invPdfScaling = Double.parseDouble(getParameter(INV_PDF_SCALING_PARAM));
			theView = new RotateHistoPDFView(data, this, xAxis, yAxis, zAxis,"x", "y", getParameter(CLASS_INFO_PARAM),
										getParameter(SORTED_X_PARAM), invPdfScaling, "model", (categoricalX ? "xCat" : null));
			String jitterString = getParameter(JITTERING_PARAM);
			if (jitterString != null)
				((RotateHistoPDFView)theView).setJitterFraction(Double.parseDouble(jitterString));
			((RotateHistoPDFView)theView).setPopnMeanColor(kMeanLineColor);
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel rotationPanel() {
		XPanel thePanel = RotateButton.create2DRotationPanel(theView, this, RotateButton.VERTICAL);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
//		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.setLayout(new EqualSpacingLayout(EqualSpacingLayout.VERTICAL));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				dataModelChoice = new XChoice(this);
				dataModelChoice.addItem(translate("Data"));
				dataModelChoice.addItem(translate("Model"));
			topPanel.add(dataModelChoice);
//		thePanel.add("North", topPanel);
		thePanel.add(topPanel);
		
//		thePanel.add("Center", rotationPanel());
		thePanel.add(rotationPanel());
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == dataModelChoice) {
			int newChoice = dataModelChoice.getSelectedIndex();
			if (newChoice != currentDataModelChoice) {
				currentDataModelChoice = newChoice;
				((RotateHistoPDFView)theView).setDisplayType(newChoice == 0 ? RotateHistoPDFView.HISTO
																							: RotateHistoPDFView.PDF);
				theView.repaint();
			}
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