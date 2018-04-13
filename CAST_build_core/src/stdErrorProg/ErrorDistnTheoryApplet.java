package stdErrorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class ErrorDistnTheoryApplet extends XApplet {
	static final private String POPN_DISTN_PARAM = "popnDistn";
	static final private String POPN_PARAMS_PARAM = "popnParams";
	static final protected String AXIS_INFO_PARAM = "dataAxis";
	static final private String MEAN_ERROR_NAME_PARAM = "meanErrorName";
	static final protected String ERROR_AXIS_PARAM = "errorAxis";
	static final protected String ERROR_SD_DECIMALS_PARAM = "errorSdDecimals";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final protected String MEAN_MED_AXIS_PARAM = "meanMedAxis";
	static final protected String TARGET_PARAM = "targetParam";
	
	static final protected Color kPopnColor = new Color(0x990000);
	static final protected Color kErrorColor = new Color(0x0000BB);
	
	static final protected Color kNormalPopnColor = new Color(0xE2BBE6);
	static final protected Color kNormalErrorColor = new Color(0x99CCFF);
	
	protected DataSet data;
	protected boolean isNormalPopn, showError, isMedianTarget;
	
	protected NumValue modelMean, modelSD, meanErrorSD;
	
	private SimpleDistnView popnDistnView, meanErrorDistnView;
	private boolean savedPopnDistnView = false, savedMeanDistnView = false;
	
	protected ParameterSlider sampleSizeSlider;
	
	public void setupApplet() {
		showError = getParameter(ERROR_AXIS_PARAM) != null;
		isMedianTarget = (getParameter(TARGET_PARAM) != null) && getParameter(TARGET_PARAM).equals("median");
		data = getData();
		
		setLayout(new BorderLayout(10, 0));
		
			XPanel titlesPanel = new XPanel();
			titlesPanel.setLayout(new ProportionLayout(0.58, 12, ProportionLayout.VERTICAL,
																												ProportionLayout.TOTAL));
			titlesPanel.add(ProportionLayout.TOP, popnTitlePanel(kPopnColor));
			titlesPanel.add(ProportionLayout.BOTTOM, errorDistnTitlePanel(kErrorColor));
		
		add("West", titlesPanel);
		
			XPanel distnsPanel = new XPanel();
			distnsPanel.setLayout(new ProportionLayout(0.58, 12, ProportionLayout.VERTICAL,
																												ProportionLayout.TOTAL));
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new BorderLayout(0, 0));
				topPanel.add("South", sampleSizePanel(true));
				topPanel.add("Center", distributionPanel(data, "model", kPopnColor, kNormalPopnColor,
																	getParameter(AXIS_INFO_PARAM)));
			distnsPanel.add(ProportionLayout.TOP, topPanel);
			
			distnsPanel.add(ProportionLayout.BOTTOM, errorPanel(data));
			
		add("Center", distnsPanel);
		
		setTheoryParameters(data);
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			String distnString = getParameter(POPN_DISTN_PARAM);
			isNormalPopn = (distnString == null) || distnString.equals("normal");
			
			ContinDistnVariable popnDistn;
			String params = getParameter(POPN_PARAMS_PARAM);
			if (isNormalPopn)
				popnDistn = new NormalDistnVariable(getParameter(VAR_NAME_PARAM));
			else
				popnDistn = new GammaDistnVariable(getParameter(VAR_NAME_PARAM));
			
			popnDistn.setParams(params);
			modelMean = popnDistn.getMean();
			modelSD = popnDistn.getSD();
		
		data.addVariable("model", popnDistn);
		
			int errorSdDecimals = Integer.parseInt(getParameter(ERROR_SD_DECIMALS_PARAM));
			meanErrorSD = new NumValue(0.0, errorSdDecimals);
			
			ContinDistnVariable meanDistn;
			if (isNormalPopn) {
				NormalDistnVariable distn = new NormalDistnVariable(showError ? getParameter(MEAN_ERROR_NAME_PARAM)
																																												: translate("Mean"));
				distn.setDecimals(0, errorSdDecimals);
				meanDistn = distn;
			}
			else {
				GammaDistnVariable distn = new GammaDistnVariable(showError ? getParameter(MEAN_ERROR_NAME_PARAM)
																																												: translate("Mean"));
				distn.setMeanSdDecimals(0, errorSdDecimals);
				meanDistn = distn;
			}
		
		data.addVariable(showError ? "meanErrorDistn" : "meanDistn", meanDistn);
		
		return data;
	}
	
	protected void setTheoryParameters(DataSet data) {
		int sampleSize = (int)Math.round(sampleSizeSlider.getParameter().toDouble());
		
		String varName = showError ? "meanErrorDistn" : "meanDistn";
		CoreVariable distnVar = data.getVariable(varName);
		if (isNormalPopn) {
			NormalDistnVariable meanDistn = (NormalDistnVariable)distnVar;
			double sd = modelSD.toDouble() / Math.sqrt(sampleSize);
			meanErrorSD.setValue(sd);
			
			meanDistn.setMean(showError ? 0.0 : modelMean.toDouble());
			meanDistn.setSD(meanErrorSD.toDouble());
		
			String meanString = showError ? "0" : modelMean.toString();
			LabelValue label = new LabelValue(translate("normal") + " (" + meanString + ", " + meanErrorSD.toString() + ")");
			meanErrorDistnView.setLabel(label, Color.gray);
		}
		else {
			GammaDistnVariable meanErrorDistn = (GammaDistnVariable)distnVar;
			GammaDistnVariable popnDistn = (GammaDistnVariable)data.getVariable("model");
			double popnShape = popnDistn.getShape().toDouble();
			double popnScale = popnDistn.getScale().toDouble();
			
			double scale = popnScale / sampleSize;
			double shape = popnShape * sampleSize;
			meanErrorDistn.setScale(scale);
			meanErrorDistn.setShape(shape);
			meanErrorDistn.setZeroPos(showError ? (isMedianTarget ? -meanErrorDistn.getQuantile(0.5)
																														: -popnDistn.getMean().toDouble()) : 0.0);
			meanErrorSD.setValue(modelSD.toDouble() / Math.sqrt(sampleSize));
			
			String meanString;
			if (showError) {
				if (isMedianTarget) {
					double bias = meanErrorDistn.getMean().toDouble() - popnDistn.getQuantile(0.5);
					meanString = (new NumValue(bias, modelMean.decimals)).toString();
				}
				else
					meanString = "0";
			}
			else
				meanString = modelMean.toString();
			LabelValue label = new LabelValue(translate("mean") + " = " + meanString + ", " + translate("sd") + " = " + meanErrorSD.toString());
			meanErrorDistnView.setLabel(label, Color.gray);
		}
		
		data.variableChanged(varName);
	}
	
	protected XPanel titlePanel(String title, Color c) {
		StringTokenizer st = new StringTokenizer(translate(title), "*");
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 0));
		while (st.hasMoreTokens()) {
			XLabel titleLabel = new XLabel(st.nextToken(), XLabel.LEFT, this);
			titleLabel.setFont(getStandardBoldFont());
			titleLabel.setForeground(c);
			thePanel.add(titleLabel);
		}
		return thePanel;
	}
	
	protected XPanel popnTitlePanel(Color c) {
		return titlePanel("Population distribution", c);		//	titlePanel() calls translate()
	}
	
	protected XPanel errorDistnTitlePanel(Color c) {
		return titlePanel("Error distribution*for mean", c);		//	titlePanel() calls translate()
	}
	
	protected XPanel distributionPanel(DataSet data, String distnKey, Color c, Color densityColor,
																													String axisInfo) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(axisInfo);
			horizAxis.setAxisName(data.getVariable(distnKey).name);
			horizAxis.setForeground(c);
		thePanel.add("Bottom", horizAxis);
		
			SimpleDistnView view = new SimpleDistnView(data, this, horizAxis, distnKey);
			view.setDensityScaling(0.85);
			view.setDensityColor(densityColor);
			view.lockBackground(Color.white);
			view.setForeground(c);
			view.setFont(getBigBoldFont());
		thePanel.add("Center", view);
		
			if (popnDistnView == null) {
				LabelValue popnLabel;
				if (isMedianTarget) {
					if (isNormalPopn)
						popnLabel = new LabelValue(translate("Median") + " = " + modelMean.toString());
					else {
						GammaDistnVariable popnDistn = (GammaDistnVariable)data.getVariable("model");
						double median = popnDistn.getQuantile(0.5);
						NumValue medianVal = new NumValue(median, meanErrorSD.decimals);
						popnLabel = new LabelValue(translate("Median") + " = " + medianVal);
					}
				} else {
					if (isNormalPopn)
						popnLabel = new LabelValue(translate("normal") + " (" + modelMean.toString() + ", "
																																	+ modelSD.toString() + ")");
					else
						popnLabel = new LabelValue(translate("skew") + ", " + translate("mean") + " = " + modelMean.toString()
																						+ ", " + translate("sd") + " = " + modelSD.toString());
				}
				view.setLabel(popnLabel, Color.gray);
			}
			saveDistnView(view);
		
		return thePanel;
	}
	
	protected boolean saveDistnView(SimpleDistnView view) {
		if (!savedPopnDistnView) {
			popnDistnView = view;
			savedPopnDistnView = true;
			return true;
		}
		else if (!savedMeanDistnView) {
			meanErrorDistnView = view;
			savedMeanDistnView = true;
			return true;
		}
		return false;
	}
	
	protected XPanel errorPanel(DataSet data) {
		if (showError)
			return distributionPanel(data, "meanErrorDistn", kErrorColor,
																	kNormalErrorColor, getParameter(ERROR_AXIS_PARAM));
		else
			return distributionPanel(data, "meanDistn", kErrorColor,
															 kNormalErrorColor, getParameter(MEAN_MED_AXIS_PARAM));
	}
	
	protected XPanel sampleSizePanel(boolean withArrows) {
		XPanel thePanel = new InsetPanel(50, 0);
		thePanel.setLayout(new BorderLayout(12, 0));
		
		if (withArrows) {
			ArrowCanvas arrow = new ArrowCanvas(12, 4, 7, 8, ArrowCanvas.DOWN);
			arrow.setForeground(kPopnColor);
			thePanel.add("West", arrow);
		}
		
			StringTokenizer st = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		
			NumValue minSize = new NumValue(st.nextToken());
			NumValue maxSize = new NumValue(st.nextToken());
			NumValue startSize = new NumValue(st.nextToken());
			sampleSizeSlider = new ParameterSlider(minSize, maxSize, startSize, translate("Sample size"), this);
		thePanel.add("Center", sampleSizeSlider);
		
		if (withArrows) {
			ArrowCanvas arrow = new ArrowCanvas(12, 4, 7, 8, ArrowCanvas.DOWN);
			arrow.setForeground(kPopnColor);
			thePanel.add("East", arrow);
		}
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == sampleSizeSlider) {
			setTheoryParameters(data);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}