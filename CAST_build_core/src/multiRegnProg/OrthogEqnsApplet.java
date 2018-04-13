package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import ssq.*;
import multivarProg.*;
import multiRegn.*;


public class OrthogEqnsApplet extends RotateApplet {
	static final private String MAX_COEFF_PARAM = "maxCoeff";
	static final private String MAX_R_PARAM = "maxR";
	
	static final private String kYVarName = "Y";
	static final private String kXVarName[] = {"X", "Z"};
	
	static final private boolean kOnlyShowX[] = {true, true, false};
	static final private boolean kOnlyShowZ[] = {true, false, true};
	
	static final private Color kFullPlaneColor = new Color(0xAADDFF);
	static final private Color kSinglePlaneColor = new Color(0xFFCCCC);
	
	private AdjustXZCorrDataSet data;
	protected SummaryDataSet summaryData;
	
	private MultiLinearEqnView xzEquationView, xEquationView, zEquationView;
	
	private NumValue maxCoeff[];
	
	private XButton sampleButton, spinButton;
	
	private XChoice mainVarChoice;
	private int currentMainVar = 0;
	
	private NumValue[] copyParams(MultipleRegnModel ls) {
		int nParam = ls.noOfParameters();
		NumValue paramCopy[] = new NumValue[nParam];
		for (int i=0 ; i<nParam ; i++)
			paramCopy[i] = new NumValue(ls.getParameter(i));
		return paramCopy;
	}
	
	protected DataSet readData() {
		data = new AdjustXZCorrDataSet(this);
		
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		
		MultipleRegnModel lsX = new MultipleRegnModel("X only", data, MultiRegnDataSet.xKeys,
																																						copyParams(ls));
		lsX.updateLSParams("y", data.getXOnlyConstraints());
		data.addVariable("lsX", lsX);
		
		MultipleRegnModel lsZ = new MultipleRegnModel("Z only", data, MultiRegnDataSet.xKeys,
																																						copyParams(ls));
		lsZ.updateLSParams("y", data.getZOnlyConstraints());
		data.addVariable("lsZ", lsZ);
		
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		return new AnovaSummaryData(data, "error");
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(regnData.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(regnData.getXAxisInfo());
			D3Axis yAxis = new D3Axis(regnData.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(regnData.getYAxisInfo());
			D3Axis zAxis = new D3Axis(regnData.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(regnData.getZAxisInfo());
			
			Rotate3DCrossPlanesView localView = new Rotate3DCrossPlanesView(data, this, xAxis, yAxis, zAxis,
																	"ls", "lsX", kFullPlaneColor, kSinglePlaneColor, null);
			localView.setComponentType(Rotate3DCrossPlanesView.NO_LINES);
			localView.lockBackground(Color.white);
			theView = localView;
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 20));
		
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
			rotatePanel.add(RotateButton.createRotationPanel(theView, this));
			
				spinButton = new XButton(translate("Spin"), this);
			rotatePanel.add(spinButton);
		
		thePanel.add(rotatePanel);
		
			XPanel dataControlPanel = new XPanel();
			dataControlPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 5));
		
				AdjustXZCorrDataSet data2 = (AdjustXZCorrDataSet)data;
				double initialR2 = data2.getInitialXZR2();
				String maxRString = getParameter(MAX_R_PARAM);
				double maxR = Double.parseDouble(maxRString);
				double maxR2 = maxR * maxR;
			dataControlPanel.add(new R2Slider(this, data, "z", "y", summaryData, translate("Correl(X, Z)"),
																												"0.0", maxRString, initialR2, maxR2));
			
				XPanel samplePanel = new XPanel();
				samplePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					sampleButton = new XButton(translate("Take sample"), this);
				samplePanel.add(sampleButton);
				
			dataControlPanel.add(samplePanel);
			
		thePanel.add(dataControlPanel);
		
			XPanel mainVarPanel = new XPanel();
			mainVarPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 0));
			
				XLabel mainVarLabel = new XLabel(translate("Display models") + ":", XLabel.LEFT, this);
				mainVarLabel.setFont(getStandardBoldFont());
			
			mainVarPanel.add(mainVarLabel);
			
				mainVarChoice = new XChoice(this);
				mainVarChoice.addItem(translate("Involving") + " X");
				mainVarChoice.addItem(translate("Involving") + " Z");
			mainVarPanel.add(mainVarChoice);
		
		thePanel.add(mainVarPanel);
		
		return thePanel;
	}
	
	private XPanel xzEquationPanel(DataSet data, Color backgroundColor) {
		XPanel thePanel = new InsetPanel(0, 5, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			xzEquationView = new MultiLinearEqnView(data, this, "ls",
															kYVarName, kXVarName, maxCoeff, maxCoeff);
			xzEquationView.setHighlightIndex(1);
			xzEquationView.lockBackground(backgroundColor);
		thePanel.add(xzEquationView);
		
		return thePanel;
	}
	
	private XPanel xEquationPanel(DataSet data, Color backgroundColor) {
		XPanel xPanel = new XPanel();
		xPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			xEquationView = new MultiLinearEqnView(data, this, "lsX",
														kYVarName, kXVarName, maxCoeff, maxCoeff);
			xEquationView.setHighlightIndex(1);
			xEquationView.setDrawParameters(kOnlyShowX);
			xEquationView.lockBackground(backgroundColor);
		xPanel.add("Center", xEquationView);
		
		return xPanel;
	}
	
	private XPanel zEquationPanel(DataSet data, Color backgroundColor) {
		XPanel zPanel = new XPanel();
		zPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			zEquationView = new MultiLinearEqnView(data, this, "lsZ",
														kYVarName, kXVarName, maxCoeff, maxCoeff);
			zEquationView.setHighlightIndex(-1);
			zEquationView.setDrawParameters(kOnlyShowZ);
			zEquationView.lockBackground(backgroundColor);
		zPanel.add("Center", zEquationView);
		
		return zPanel;
	}
	
	protected XPanel leftSummaryPanel(DataSet data) {
		XPanel jointEqnPanel = new InsetPanel(0, 10);
		jointEqnPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		jointEqnPanel.add(xzEquationPanel(data, kFullPlaneColor));
		
		jointEqnPanel.lockBackground(kFullPlaneColor);
		return jointEqnPanel;
	}
	
	protected XPanel rightSummaryPanel(DataSet data) {
		XPanel singleEqnPanel = new InsetPanel(0, 10);
		singleEqnPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		singleEqnPanel.add(xEquationPanel(data, kSinglePlaneColor));
		singleEqnPanel.add(zEquationPanel(data, kSinglePlaneColor));
		
		singleEqnPanel.lockBackground(kSinglePlaneColor);
		return singleEqnPanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		maxCoeff = new NumValue[3];
		StringTokenizer st = new StringTokenizer(getParameter(MAX_COEFF_PARAM));
		for (int i=0 ; i<3 ; i++)
			maxCoeff[i] = new NumValue(st.nextToken());
		
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
		
		thePanel.add(ProportionLayout.LEFT, leftSummaryPanel(data));
		thePanel.add(ProportionLayout.RIGHT, rightSummaryPanel(data));
		
		return thePanel;
	}
	
	protected void changeMainVar(int newMainVar) {
		if (newMainVar == 0) {
			xzEquationView.setHighlightIndex(1);
			xzEquationView.repaint();
			xEquationView.setHighlightIndex(1);
			xEquationView.repaint();
			zEquationView.setHighlightIndex(-1);
			zEquationView.repaint();
			((Rotate3DCrossPlanesView)theView).setPlaneKeys("ls", "lsX");
			theView.repaint();
		}
		else {
			xzEquationView.setHighlightIndex(2);
			xzEquationView.repaint();
			xEquationView.setHighlightIndex(-1);
			xEquationView.repaint();
			zEquationView.setHighlightIndex(2);
			zEquationView.repaint();
			((Rotate3DCrossPlanesView)theView).setPlaneKeys("ls", "lsZ");
			theView.repaint();
		}
	}
	
	private boolean localAction(Object target) {
		if (target == spinButton) {
			theView.startAutoRotation();
			return true;
		}
		else if (target == sampleButton) {
			summaryData.takeSample();
			data.updateForNewSample();
			data.variableChanged("y");
			return true;
		}
		else if (target == mainVarChoice) {
			if (mainVarChoice.getSelectedIndex() != currentMainVar) {
				currentMainVar = mainVarChoice.getSelectedIndex();
				changeMainVar(currentMainVar);
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