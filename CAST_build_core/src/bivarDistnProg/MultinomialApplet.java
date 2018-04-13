package bivarDistnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import formula.*;
import graphics3D.*;

import contin.*;
import multivarProg.RotateApplet;


public class MultinomialApplet extends RotateApplet {
	static final private String CAT_PROBS_PARAM = "catProbs";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String PROB_AXIS_PARAM = "probAxis";		//	one for each sample size, separated by "*"
	static final private String TYPE_PARAM = "transitions";
	
	private DataSet data;
	private CatDistnVariable xVariable;
	private ContinResponseVariable yVariable;
	
	private double[] catProbs;
	
	private XChoice sampleSizeChoice;
	private int sampleSizeIndex = 0;
	private String probAxisInfo[];
	
	private XCheckbox sliceCheck;
	private ParameterSlider sliceSlider;
	
	private TransGraphicChoice displayChoice;
	private int currentType = RotateContinView.JOINT;
	
	private D3Axis xAxis, zAxis, probAxis;
	
	public void setupApplet() {
		super.setupApplet();
		
		catProbs = new double[3];
		StringTokenizer st = new StringTokenizer(getParameter(CAT_PROBS_PARAM));
		for (int i=0 ; i<3 ; i++)
			catProbs[i] = Double.parseDouble(st.nextToken());
		
		st = new StringTokenizer(getParameter(PROB_AXIS_PARAM), "#");
		probAxisInfo = new String[st.countTokens()];
		for (int i=0 ; i<probAxisInfo.length ; i++)
			probAxisInfo[i] = st.nextToken();
		
		updateSampleSize();
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
			xVariable = new CatDistnVariable(getParameter(X_VAR_NAME_PARAM));
		data.addVariable("x", xVariable);
		
			yVariable = new ContinResponseVariable(getParameter(Y_VAR_NAME_PARAM), data, "x");
		data.addVariable("y", yVariable);
		
		return data;
	}
	
	private void updateSampleSize() {
		int n = Integer.parseInt(sampleSizeChoice.getSelectedItem());
		
		String labels = "0";
		for (int i=1 ; i<=n ; i++)
			labels += " " + i;
		xVariable.readLabels(labels);
		yVariable.readLabels(labels);
		
		xVariable.setProbs(getBinomialProbs(n, catProbs[0]));
		yVariable.setConditProbs(getConditProbs(n, catProbs[1] / (catProbs[1] + catProbs[2])));
		
		xAxis.setCatScale((CatVariableInterface)yVariable);
		zAxis.setCatScale((CatVariableInterface)xVariable);
		probAxis.setNumScale(probAxisInfo[sampleSizeIndex]);
		
		if (sliceSlider != null) {
			sliceSlider.changeLimits(new NumValue(0,0), new NumValue(n,0), new NumValue(0,0));
		}
		
		data.variableChanged("x");
		data.variableChanged("y");
	}
	
	private double[] getBinomialProbs(int n, double p) {		// p = p(Y = cat 1)
		double[] prob = new double[n+1];		//	could be improved by working down from i=n if p>0.5
		
		double pOverQ = p / (1.0 - p);
		prob[0] = Math.pow(1.0 - p, n);
		for (int i=1 ; i<=n ; i++)
			prob[i] = prob[i-1] * ((double)(n-i + 1)) / i * pOverQ;
		return prob;
	}
	
	private double[] getConditProbs(int n, double p) {		// p = p(Y = cat 2 | Y != cat 1)
		double[] prob = new double[(n+1)*(n+1)];
		
		for (int i=0 ; i<=n ; i++) {
			double[] pCondit = getBinomialProbs(n-i, p);
			for (int j=0 ; j<pCondit.length ; j++)
				prob[i * (n+1) + j] = pCondit[j];		//	remaining conditional probs are zero
		}
		return prob;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		xAxis = new D3Axis(yVariable.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		zAxis = new D3Axis(xVariable.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		probAxis = new D3Axis("Prob", D3Axis.Y_AXIS, D3Axis.X_AXIS, this);
		
		theView = new RotateContinView(data, this, xAxis, probAxis, zAxis,"y", "x");
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
		
		sampleSizeChoice = new XChoice(translate("Sample size"), XChoice.HORIZONTAL, this);
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		int nSizes = st.countTokens();
		for (int i=0 ; i<nSizes ; i++)
			sampleSizeChoice.addItem(st.nextToken());
		thePanel.add(sampleSizeChoice);
		
		String type = getParameter(TYPE_PARAM);
		if ((type != null) && type.indexOf("condit") >= 0) {
			sliceCheck = new XCheckbox(translate("Restrict to..."), this);
			sliceCheck.setEnabled(false);
			thePanel.add(sliceCheck);
			
			sliceSlider = new ParameterSlider(new NumValue(0,0), new NumValue(1,0), new NumValue(0,0), "X", this);
			sliceSlider.setEnabled(false);
			thePanel.add(sliceSlider);
		}
		
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 0));
		
		thePanel.add(rotationPanel());
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
			
				displayChoice = createChoice();
			if (displayChoice != null)
				choicePanel.add(displayChoice);
		
		thePanel.add(choicePanel);
		
		return thePanel;
	}
	
	private XPanel rotationPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		String type = getParameter(TYPE_PARAM);
		boolean basicGraph = type == null || type.equals("none");
			XPanel buttonPanel = RotateButton.createXYDRotationPanel(theView, this,
																basicGraph ? RotateButton.VERTICAL : RotateButton.HORIZONTAL);
		
		thePanel.add(buttonPanel);
			rotateButton = new XButton(translate("Spin"), this);
		
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	protected TransGraphicChoice createChoice() {
		String type = getParameter(TYPE_PARAM);
		boolean marginal = (type != null) && type.indexOf("margin") >= 0;
		boolean conditional = (type != null) && type.indexOf("condit") >= 0;
		
		if (marginal || conditional)
			return new TransGraphicChoice(marginal ? TransGraphicChoice.MARGINAL
																	: TransGraphicChoice.CONDITIONAL, this);
		else
			return null;
	}
	
	protected String getYAxisName() {
		return "Prob";
	}
	
	private void updateAnnotation() {
		currentType = (displayChoice == null) ? RotateContinView.JOINT : displayChoice.getCurrentType();
		String nString = null, piString = null;
		if (currentType == RotateContinView.X_MARGIN) {
			StringTokenizer st = new StringTokenizer(getParameter(CAT_PROBS_PARAM));
			nString = sampleSizeChoice.getSelectedItem();
			piString = st.nextToken();
		}
		else if (currentType == RotateContinView.Y_MARGIN) {
			StringTokenizer st = new StringTokenizer(getParameter(CAT_PROBS_PARAM));
			st.nextToken();
			nString = sampleSizeChoice.getSelectedItem();
			piString = st.nextToken();
		}
		else if (currentType == RotateContinView.X_CONDIT && sliceCheck != null && sliceCheck.getState()) {
			StringTokenizer st = new StringTokenizer(getParameter(CAT_PROBS_PARAM));
			double p1 = Double.parseDouble(st.nextToken());
			double p2 = Double.parseDouble(st.nextToken());
			double p3 = 1 - p1 - p2;
			int n = Integer.parseInt(sampleSizeChoice.getSelectedItem());
			int yVal = sliceSlider.getValue();
			int nLeft = n - yVal;
			nString = String.valueOf(nLeft);
			piString = new NumValue(p1 / (p1 + p3), 4).toString();
		}
		else if (currentType == RotateContinView.Y_CONDIT && sliceCheck != null && sliceCheck.getState()) {
			StringTokenizer st = new StringTokenizer(getParameter(CAT_PROBS_PARAM));
			double p1 = Double.parseDouble(st.nextToken());
			double p2 = Double.parseDouble(st.nextToken());
			double p3 = 1 - p1 - p2;
			int n = Integer.parseInt(sampleSizeChoice.getSelectedItem());
			int xVal = sliceSlider.getValue();
			int nLeft = n - xVal;
			nString = String.valueOf(nLeft);
			piString = new NumValue(p2 / (p2 + p3), 4).toString();
		}
		
		if (nString != null && piString != null) {
			String distnString = MText.expandText("Binomial(n=" + nString + ", #pi#=" + piString + ")");
			((RotateContinView)theView).setAnnotation(distnString);
		}
		else
			((RotateContinView)theView).setAnnotation(null);

	}
	
	private void updateControls() {
		currentType = (displayChoice == null) ? RotateContinView.JOINT : displayChoice.getCurrentType();
		if (sliceCheck != null) {
			boolean canSlice = currentType == RotateContinView.X_CONDIT || currentType == RotateContinView.Y_CONDIT;
			sliceCheck.setEnabled(canSlice);
			boolean doingSlice = canSlice && sliceCheck.getState();
			sliceSlider.setEnabled(doingSlice);
			if (currentType == RotateContinView.X_CONDIT)
				sliceSlider.setTitle("Y", this);
			else if (currentType == RotateContinView.Y_CONDIT)
				sliceSlider.setTitle("X", this);
			
			int sliderValue = sliceSlider.getValue();		//	same as parameter value, but int
			int xSlice = doingSlice && (currentType == RotateContinView.Y_CONDIT) ? sliderValue : -1;
			int ySlice = doingSlice && (currentType == RotateContinView.X_CONDIT) ? sliderValue : -1;
			((RotateContinView)theView).setSliceIndices(xSlice, ySlice);
		}
	}
	
	private boolean localAction(Object target) {
		if (target == displayChoice) {
			if (displayChoice.getCurrentType() != currentType) {
				if (sliceCheck != null)
					sliceCheck.setState(false);
				updateAnnotation();
				updateControls();
				((RotateContinView)theView).animateChange(currentType);
			}
			return true;
		}
		else if (target == sampleSizeChoice) {
			if (sampleSizeChoice.getSelectedIndex() != sampleSizeIndex) {
				sampleSizeIndex = sampleSizeChoice.getSelectedIndex();
				if (sliceCheck != null)
					sliceCheck.setState(false);
				updateAnnotation();
				updateControls();
				updateSampleSize();
			}
			return true;
		}
		else if (target == sliceCheck) {
			updateAnnotation();
			updateControls();
			return true;
		}
		else if (target == sliceSlider) {
			updateAnnotation();
			updateControls();
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