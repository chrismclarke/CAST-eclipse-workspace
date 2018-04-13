package structureProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import coreVariables.*;


public class PictTreatmentApplet extends PictValueApplet {
	private final static String RANDOM2_PARAM = "random2";
	private final static String VAR_NAME2_PARAM = "varName2";
	private final static String EFFECT_SIZE_PARAM = "effectSize";
	private final static String EFFECT_NAME_PARAM = "effectName";
	
	static final private Color kDarkBlue = new Color(0x000066);
	
	private RandomNormal generator2;
	
	private NumValue minValue, maxValue, startValue;
	
	private XCheckbox varyEffectCheck;
	private CardLayout effectLayout;
	private XPanel effectPanel;
	private ParameterSlider effectSlider;
	
	protected DataSet getData() {
		DataSet data = super.getData();
		
			generator2 = new RandomNormal(getParameter(RANDOM2_PARAM));
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			NumSampleVariable y2BaseVar = new NumSampleVariable("y2Base", generator2, decimals);
			y2BaseVar.generateNextSample();
		
		data.addVariable("y2Base", y2BaseVar);
		
						
			StringTokenizer st = new StringTokenizer(getParameter(EFFECT_SIZE_PARAM));
			minValue = new NumValue(st.nextToken());
			maxValue = new NumValue(st.nextToken());
			startValue = new NumValue(st.nextToken());
			ScaledVariable y2Var = new ScaledVariable(getParameter(VAR_NAME2_PARAM), y2BaseVar,
															"y2Base", startValue.toDouble(), 1.0, decimals);
		data.addVariable("y2", y2Var);
		
		return data;
	}
	
	protected XPanel dataPanel(DataSet data, String yKey, Color fontColor) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL,
																														ProportionLayout.REMAINDER));
		
		thePanel.add(ProportionLayout.TOP, super.dataPanel(data, "y", fontColor));
		thePanel.add(ProportionLayout.BOTTOM, super.dataPanel(data, "y2", kDarkBlue));
		
		return thePanel;
	}
	
	protected XPanel controlPanel() {
		XPanel fixedSizePanel = new XPanel();
		fixedSizePanel.setLayout(new FixedSizeLayout(usesBigFonts() ? 200 : 170, 100));
		
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new BorderLayout(0, 10));
			
			thePanel.add("Center", super.controlPanel());
			
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new BorderLayout(10, 0));
					
					XPanel checkPanel = new XPanel();
					checkPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
						varyEffectCheck = new XCheckbox(translate("Vary effect"), this);
						varyEffectCheck.setForeground(kDarkBlue);
					checkPanel.add(varyEffectCheck);
				
				bottomPanel.add("Center", checkPanel);
				
					effectPanel = new XPanel();
					effectLayout = new CardLayout();
					effectPanel.setLayout(effectLayout);
					
						String effectName = getParameter(EFFECT_NAME_PARAM);
					
						XPanel fixedEffectPanel = new InsetPanel(0, 16, 0, 0);
						fixedEffectPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
							XLabel fixedLabel = new XLabel(effectName + " = " + startValue.toString(),
																																					XLabel.LEFT, this);
							fixedLabel.setFont(getStandardBoldFont());
							fixedLabel.setForeground(kDarkBlue);
						fixedEffectPanel.add(fixedLabel);
					effectPanel.add("fixEffect", fixedEffectPanel);
					
						XPanel sliderPanel = new InsetPanel(0, 16, 0, 0);
						sliderPanel.setLayout(new BorderLayout(0, 0));
							effectSlider = new ParameterSlider(minValue, maxValue, startValue,
																																effectName, this);
							effectSlider.setFont(getStandardFont());
							effectSlider.setTitleFont(getStandardBoldFont());
							effectSlider.setForeground(kDarkBlue);
						sliderPanel.add("Center", effectSlider);
						
					effectPanel.add("varyEffect", sliderPanel);
				
				bottomPanel.add("South", effectPanel);
			
			thePanel.add("South", bottomPanel);
		
		fixedSizePanel.add(thePanel);
		
		return fixedSizePanel;
	}
	
	protected void doTakeSample() {
		super.doTakeSample();
		NumSampleVariable yVar = (NumSampleVariable)data.getVariable("y2Base");
		yVar.generateNextSample();
		data.variableChanged("y2Base");
	}

	
	private boolean localAction(Object target) {
		if (target == varyEffectCheck) {
			if (varyEffectCheck.getState())
				effectLayout.show(effectPanel, "varyEffect");
			else {
				effectLayout.show(effectPanel, "fixEffect");
				effectSlider.setParameter(startValue.toDouble());
				data.variableChanged("y2");
			}
			return true;
		}
		else if (target == effectSlider) {
			NumValue effect = effectSlider.getParameter();
			ScaledVariable y2Var = (ScaledVariable)data.getVariable("y2");
			y2Var.setParam(0, effect.toDouble());
			data.variableChanged("y2");
			
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