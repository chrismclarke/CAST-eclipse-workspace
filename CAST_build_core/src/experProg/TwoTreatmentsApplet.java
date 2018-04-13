package experProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;

import exper.*;


public class TwoTreatmentsApplet extends CoreMultiFactorApplet {
	static final private String ERROR_SD_PARAM = "errorSD";
	
	static final private String SEED_INFO_PARAM = "randomSeeds";
	static final private String RESPONSE_AXIS_INFO_PARAM = "responseAxis";
	static final private String MAX_ROW_TREAT_PARAM = "maxRowTreatment";
	static final private String TREAT_EFFECT_SCALING_PARAM = "treatEffectScaling";
	
	private long popnRandomSeed;
	
	private XButton sampleButton;
	private XNoValueSlider blockEffectSlider;
	
	protected SummaryDataSet summaryData;
	
	private TreatDotView responseView;
	private TwoTreatPictView designView;
	
	public void setupApplet() {
		readEffects();
		
		data = readData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new ProportionLayout(getHorizLayoutPropn(), 10, ProportionLayout.HORIZONTAL,
																																	ProportionLayout.TOTAL));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(0, 7));
			leftPanel.add("North", effectSliderPanel(data));
			leftPanel.add("Center", displayPanel(data));
			leftPanel.add("South", controlPanel(data));
			
		add(ProportionLayout.LEFT, leftPanel);
		add(ProportionLayout.RIGHT, responsePlotPanel(data));
	}
	
	protected double getHorizLayoutPropn() {
		return 0.65;
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
			CatVariable treat1Var = (CatVariable)data.getVariable("treat1");
			int nValues = treat1Var.noOfValues();
			double errorSD = Double.parseDouble(getParameter(ERROR_SD_PARAM));
			RandomNormal errorGenerator = new RandomNormal(nValues, 0.0, errorSD, 3.0);
			NumSampleVariable error = new NumSampleVariable("error", errorGenerator, 10);
			error.setSampleSize(errorGenerator.getSampleSize());
		data.addVariable("error", error);
		
			String effectKeys[] = {"treat1", "treat2"};
			FactorResponseVariable response = new FactorResponseVariable(getParameter(RESPONSE_NAME_PARAM),
											data, constant, effectKeys, effects, "error");
		data.addVariable("response", response);
		
		response.setFactorEffectScaling(1, 0.0);
		
		StringTokenizer popnTok = new StringTokenizer(getParameter(SEED_INFO_PARAM));
		long genderSeed = Long.parseLong(popnTok.nextToken());
		popnRandomSeed = Long.parseLong(popnTok.nextToken());
		
			CatVariable genderVar = new CatVariable(getParameter(CAT_NAME_PARAM));
			genderVar.readLabels(getParameter(CAT_LABELS_PARAM));
			
			Random genderGenerator = new Random(genderSeed);
			int popnSize = treat1Var.noOfValues();
			int values[] = new int[popnSize];
			for (int i=0 ; i<popnSize ; i++)
				values[i] = (genderGenerator.nextDouble() <= 0.5) ? 0 : 1;
			genderVar.setValues(values);
		
		data.addVariable("gender", genderVar);
		
			RandomisedNumVariable permVar = new RandomisedNumVariable("perm");
			double perm[] = new double[popnSize];
			for (int i=0 ; i<popnSize ; i++)
				perm[i] = i;
			permVar.setValues(perm);
		data.addVariable("perm", permVar);
		
		data.addVariable("biSamp", new BiSampleVariable(data, "error", "perm"));
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		return new SummaryDataSet(data, "biSamp");
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			NumValue maxRowTreat = new NumValue(getParameter(MAX_ROW_TREAT_PARAM));
			StringTokenizer st = new StringTokenizer(getParameter(TREAT_EFFECT_SCALING_PARAM));
			double treatEffectConst = Double.parseDouble(st.nextToken());
			double treatEffectScaling = Double.parseDouble(st.nextToken());
			designView = new TwoTreatPictView(data, this, popnRandomSeed, "perm", "treat2",
															"treat1", "response", maxRowTreat, treatEffectConst, treatEffectScaling);
			designView.doInitialisation(this);
//			theView.setFont(getBigBoldFont());
			designView.setActiveCatVariable("gender");
		
		thePanel.add("Center", designView);
		return thePanel;
	}
	
	protected XPanel effectSliderPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			CatVariable treat2Var = (CatVariable)data.getVariable("treat2");
			blockEffectSlider = new XNoValueSlider(translate("Unused (all same)"), translate("Varying"), treat2Var.name,
																																						0, 100, 0, this);
			blockEffectSlider.setFont(getStandardBoldFont());
			blockEffectSlider.setForeground(TwoTreatPictView.kRowColour);
			
		thePanel.add("Center", blockEffectSlider);
		return thePanel;
	}
	
	protected XPanel responsePlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(RESPONSE_AXIS_INFO_PARAM));
			plotPanel.add("Left", yAxis);
			
				HorizAxis treatAxis = new HorizAxis(this);
				CatVariable treat1Var = (CatVariable)data.getVariable("treat1");
				treatAxis.setCatLabels(treat1Var);
				treatAxis.setAxisName(treat1Var.name);
				treatAxis.setCenterAxisName(true);
				
			plotPanel.add("Bottom", treatAxis);
			
				responseView = new TreatDotView(data, this, yAxis, treatAxis, "response", "treat1", 1.0);
				responseView.lockBackground(Color.white);
				responseView.setSymbolVariable("treat2");
			plotPanel.add("Center", responseView);
		
		thePanel.add("Center", plotPanel);
		
			NumVariable respVar = (NumVariable)data.getVariable("response");
			XLabel respLabel = new XLabel(respVar.name, XLabel.LEFT, this);
			respLabel.setFont(yAxis.getFont());
		thePanel.add("North", respLabel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
		
			sampleButton = new RepeatingButton(translate("Repeat experiment"), this);
		thePanel.add(sampleButton);
		
		return thePanel;
	}
	
	protected void perpareForAnimation() {
		responseView.show(false);
		sampleButton.disable();
	}
	
	protected void startAnimation() {
		designView.doSampleAnimation();
	}
	
	public void finishAnimation() {
		responseView.show(true);
		sampleButton.enable();
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			perpareForAnimation();
			summaryData.takeSample();
			startAnimation();
			return true;
		}
		else if (target == blockEffectSlider) {
			double scaling = blockEffectSlider.getValue() / (double)blockEffectSlider.getMaxValue();
			FactorResponseVariable response = (FactorResponseVariable)data.getVariable("response");
			response.setFactorEffectScaling(1, scaling);
			data.variableChanged("response");
			summaryData.setSingleSummaryFromData();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}