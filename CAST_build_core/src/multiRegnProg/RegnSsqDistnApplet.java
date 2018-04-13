package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;
import distn.*;
import graphics3D.*;

import ssq.*;
import variance.*;
import linMod.*;
import multivarProg.*;
import multiRegn.*;


public class RegnSsqDistnApplet extends RotateApplet {
	static final private String MAX_COEFF_PARAM = "maxCoeff";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String SSQ_AXIS_PARAM = "ssqAxis";
	static final private String SSQ_NAMES_PARAM = "ssqNames";
	static final private String SSQ_AXIS_NAME_PARAM = "ssqAxisName";
	
	static final private Color kMeanPlaneColor = new Color(0x66CCFF);
	static final private Color kLSPlaneColor = new Color(0xCCCCCC);
	static final private Color kDarkBlue = new Color(0x000099);
	
	static final private NumValue kZero = new NumValue(0.0, 0);
	static final private NumValue kMaxR2 = new NumValue(1.0, 4);
	
	private MultiRegnDataSet data;
	private SummaryDataSet summaryData;
	
	private NumValue maxSsq;
	
	private XButton sampleButton;
	private XCheckbox accumulateCheck;
	
	protected DataSet readData() {
		data = new MultiRegnDataSet(this);
		
		data.addBasicComponents();
		
		MultipleRegnModel meanModel = new MultipleRegnModel("mean", data, MultiRegnDataSet.xKeys);
		data.addVariable("mean", meanModel);
		setMeanModel(data);
		
		summaryData = getSummaryData(data);
		
		return data;
	}
	
	private void setMeanModel(DataSet data) {
		double sy = 0.0;
		int n = 0;
		ValueEnumeration ye = ((NumVariable)data.getVariable("y")).values();
		while (ye.hasMoreValues()) {
			sy += ye.nextDouble();
			n++;
		}
		MultipleRegnModel meanModel = (MultipleRegnModel)data.getVariable("mean");
//		int meanDecimals = Integer.parseInt(getParameter(MEAN_DECIMALS_PARAM));
		meanModel.setParameter(0, new NumValue(sy / n));
		meanModel.setParameter(1, kZero);
		meanModel.setParameter(2, kZero);
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
											BasicComponentVariable.kComponentKey, maxSsq.decimals, kMaxR2.decimals);
		
			MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
			double errorSD = model.evaluateSD().toDouble();
			GammaDistnVariable chi2 = new GammaDistnVariable("chi2");
			chi2.setShape(1.0);
			chi2.setScale(errorSD * errorSD * 2.0);
		summaryData.addVariable("chi2", chi2);
		
		summaryData.takeSample();
		return summaryData;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 8);
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XPanel namePanel = new XPanel();
			namePanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_SPACED, 4));
			
				XLabel modelLabel = new XLabel(translate("Model") + ":", XLabel.RIGHT, this);
				modelLabel.setFont(getStandardBoldFont());
			namePanel.add(modelLabel);
				XLabel lsLabel = new XLabel(translate("Least sqrs") + ":", XLabel.RIGHT, this);
				lsLabel.setFont(getStandardBoldFont());
				lsLabel.setForeground(kDarkBlue);
			namePanel.add(lsLabel);
			
		thePanel.add("West", namePanel);
		
			XPanel eqnPanel = new XPanel();
			eqnPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_SPACED, 4));
			
				XPanel modelPanel = new XPanel();
				modelPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0));
					MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
				modelPanel.add(new YMeanView(data, this, "model", model.getParameter(0)));
				modelPanel.add(new YSDView(data, this, "model", model.evaluateSD()));
			eqnPanel.add(modelPanel);
			
				String yVarName = data.getVariable("y").name;
				String xVarName[] = new String[2];
				xVarName[0] = data.getVariable("x").name;
				xVarName[1] = data.getVariable("z").name;
				NumValue maxParam[] = new NumValue[3];
				StringTokenizer st = new StringTokenizer(getParameter(MAX_COEFF_PARAM));
				for (int i=0 ; i<3 ; i++)
					maxParam[i] = new NumValue(st.nextToken());
				
				MultiLinearEqnView eqnView = new MultiLinearEqnView(data, this, "ls",
																					yVarName, xVarName, maxParam, maxParam);
				eqnView.setForeground(kDarkBlue);
			eqnPanel.add(eqnView);
		
		thePanel.add("Center", eqnPanel);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		CoreVariable xVar = data.getVariable("x");
		D3Axis xAxis = new D3Axis(xVar == null ? "x" : xVar.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
		
		CoreVariable yVar = data.getVariable("y");
		D3Axis yAxis = new D3Axis(yVar == null ? "y" : yVar.name, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		
		CoreVariable zVar = data.getVariable("z");
		D3Axis zAxis = new D3Axis(zVar == null ? "z" : zVar.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
		
		theView = new Rotate3DCrossPlanesView(data, this, xAxis, yAxis, zAxis, "mean", "ls",
																	kMeanPlaneColor, kLSPlaneColor, BasicComponentVariable.kComponentColor);
		((Rotate3DCrossPlanesView)theView).setComponentType(Rotate3DCrossPlanesView.A_TO_B);
		
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(20, 0, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			NumValue maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
			StringTokenizer st = new StringTokenizer(getParameter(SSQ_NAMES_PARAM));
			String ssqNames[] = new String[3];
			for (int i=0 ; i<3 ; i++)
				ssqNames[i] = st.nextToken();
			AnovaTableView table = new AnovaTableView(summaryData, this, BasicComponentVariable.kComponentKey,
																								maxSsq, null, null, AnovaTableView.SSQ_AND_DF);
			table.setComponentNames(ssqNames);
		thePanel.add(table);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		thePanel.add("West", buttonPanel());
		thePanel.add("Center", chi2Panel(summaryData));
		
		return thePanel;
	}
	
	private XPanel buttonPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
			
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			rotatePanel.add(new RotateButton(RotateButton.XYZ_ROTATE, theView, this));
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
			
		thePanel.add("North", rotatePanel);
		
			XPanel samplePanel = new XPanel();
			samplePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
				sampleButton = new RepeatingButton(translate("Take sample"), this);
			samplePanel.add(sampleButton);
		
				accumulateCheck = new XCheckbox(translate("Accumulate"), this);
			samplePanel.add(accumulateCheck);
			
		thePanel.add("Center", samplePanel);
		return thePanel;
	}
	
	private XPanel chi2Panel(SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(0, 12, 0, 0);
		thePanel.setLayout(new FixedSizeLayout(150, 150));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				HorizAxis axis = new HorizAxis(this);
				axis.readNumLabels(getParameter(SSQ_AXIS_PARAM));
				axis.setForeground(BasicComponentVariable.kExplainedColor);
				axis.setAxisName(getParameter(SSQ_AXIS_NAME_PARAM));
				
			mainPanel.add("Bottom", axis);
			
				
				MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
				int sdDecimals = model.evaluateSD().decimals;
				ScaledChi2View ssqView = new ScaledChi2View(summaryData, this, axis, "chi2", sdDecimals * 2);
				ssqView.setActiveNumVariable(BasicComponentVariable.kComponentKey[1]);
				ssqView.setIsMeanSumOfSquares(false);
				ssqView.setForeground(BasicComponentVariable.kExplainedColor);
				ssqView.lockBackground(Color.white);
				
			mainPanel.add("Center", ssqView);
		
		thePanel.add(mainPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			summaryData.takeSample();
			setMeanModel(data);
			theView.repaint();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
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