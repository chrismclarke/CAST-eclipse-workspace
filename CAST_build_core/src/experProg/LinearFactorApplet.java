package experProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;

import exper.*;


public class LinearFactorApplet extends CoreMultiFactorApplet {
	static final protected String RESPONSE_AXIS_INFO_PARAM = "responseAxis";
	static final protected String TREAT_AXIS_PARAM = "treatAxis";
	static final protected String TREAT_NUM_VALUE_PARAM = "treatNumValues";
	static final protected String TREAT_EFFECT_AXIS_PARAM = "treatEffectAxis";
	
	static final private String effect1Keys[] = {"treat1"};
	
	protected double catToNum[];
	protected double meanX;
	
	private XButton lsButton;
	private ConstantSliderPanel constantSliderPanel;
	
	public void setupApplet() {
		readEffects();
		
		data = readData();
		
		setLayout(new ProportionLayout(0.6, 10, ProportionLayout.HORIZONTAL,
																																ProportionLayout.TOTAL));
		add(ProportionLayout.LEFT, displayPanel(data));
		add(ProportionLayout.RIGHT, parameterPanel(data));
	}
	
	protected String[] getEffectKeys() {
		return effect1Keys;
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
			FactorsModel model = new FactorsModel(getParameter(RESPONSE_NAME_PARAM),
																												data, effect1Keys, constant, effects, 0);
			model.setIdentConstraints(FactorsModel.ZERO_MEAN_EFFECTS);
		data.addVariable("model", model);
			
			CatVariable treatVar = (CatVariable)data.getVariable("treat1");
				catToNum = new double[treatVar.noOfCategories()];
				int counts[] = treatVar.getCounts();
				StringTokenizer st = new StringTokenizer(getParameter(TREAT_NUM_VALUE_PARAM));
				double sumX = 0.0;
				for (int i=0 ; i<catToNum.length ; i++) {
					double nextX = Double.parseDouble(st.nextToken());
					catToNum[i] = nextX;
					sumX += nextX * counts[i];
				}
			meanX = sumX / treatVar.noOfValues();
			
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(RESPONSE_AXIS_INFO_PARAM));
			plotPanel.add("Left", yAxis);
			
				HorizAxis treatAxis = new HorizAxis(this);
				treatAxis.readNumLabels(getParameter(TREAT_AXIS_PARAM));
				
			plotPanel.add("Bottom", treatAxis);
			
				AdjustBaseTreatView yView = new AdjustBaseTreatView(data, this, yAxis, treatAxis, "response",
												"treat1", "model", catToNum, meanX);
				yView.lockBackground(Color.white);
			plotPanel.add("Center", yView);
		
		thePanel.add("Center", plotPanel);
		
			NumVariable respVar = (NumVariable)data.getVariable("response");
			XLabel respLabel = new XLabel(respVar.name, XLabel.LEFT, this);
			respLabel.setFont(yAxis.getFont());
		thePanel.add("North", respLabel);
		
		return thePanel;
	}
	
	protected XPanel parameterPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("Center", new EffectSlidersPanel(data, "treat1", "model", 0,
															getParameter(TREAT_EFFECT_AXIS_PARAM), false, true,
															getParameter(TREAT_AXIS_PARAM), catToNum, meanX, this));
			
			constantSliderPanel = new ConstantSliderPanel(data, "model", this);
		thePanel.add("North", constantSliderPanel);
		
			XPanel controlPanel = new XPanel();
			controlPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			
			controlPanel.add(new LinFactorChoicePanel(data, "model", 0, 1, catToNum, meanX, this));
			
				lsButton = new XButton(translate("Least squares"), this);
			controlPanel.add(lsButton);
		
		thePanel.add("South", controlPanel);
		
		return thePanel;
	}
	
	public void finishAnimation() {
	}
	
	private boolean localAction(Object target) {
		if (target == lsButton) {
			FactorsModel model = (FactorsModel)data.getVariable("model");
			model.setLSParams("response", null, false);
			constantSliderPanel.setConstant(model.getConstant());
			data.variableChanged("model");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}