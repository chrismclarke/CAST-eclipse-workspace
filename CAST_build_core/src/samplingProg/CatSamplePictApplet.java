package samplingProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;

import survey.*;
import sampling.*;


public class CatSamplePictApplet extends XApplet {
	static final private String CAT_PROBS_PARAM = "catProbs";	//	First prob must be 0.0 since first
																														//	categ is ignored by TreatmentPictView
	static final private String POPN_INFO_PARAM = "popnInfo";
	static final private String SEX_VAR_NAME_PARAM = "sexVarName";
	static final private String SEX_LABELS_PARAM = "sexLabels";
	static final private String SEEDS_PARAM = "randomSeeds";
	static final private String SAMPLE_NAME_PARAM = "sampleButtonName";
	static final private String PEOPLE_COLOUR_PARAM = "peopleColor";
	static final private String PERCENT_NAME_PARAM = "percentageName";
	
	static final protected Color kPropnBackground = new Color(0xFFEEBB);
	
	private DataSet data;
	private RandomCat sexGenerator, catGenerator;
	
	private int rows, cols, rowCycle, maxHorizOffset, maxVertOffset;
	private long seeds[];
	
	private TreatmentPictView theView;
	
	private XButton sampleButton;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(20, 10));
			
		add("Center", dataPanel(data));
		
		add("South", controlPanel());
		
		add("East", proportionPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		StringTokenizer popnTok = new StringTokenizer(getParameter(POPN_INFO_PARAM));
		rows = Integer.parseInt(popnTok.nextToken());
		cols = Integer.parseInt(popnTok.nextToken());
		rowCycle = Integer.parseInt(popnTok.nextToken());
		maxHorizOffset = Integer.parseInt(popnTok.nextToken());
		maxVertOffset = Integer.parseInt(popnTok.nextToken());
		double maleProb = Double.parseDouble(popnTok.nextToken());
		
		StringTokenizer st = new StringTokenizer(getParameter(SEEDS_PARAM));
		seeds = new long[st.countTokens()];
		for (int i=0 ; i<seeds.length ; i++)
			seeds[i] = Long.parseLong(st.nextToken());
		
			double sexProbs[] = {maleProb, 1.0 - maleProb};
			sexGenerator = new RandomCat(sexProbs, rows * cols, seeds[0]);
			CatSampleVariable sexVar = new CatSampleVariable(getParameter(SEX_VAR_NAME_PARAM),
																																				sexGenerator);
			sexVar.readLabels(getParameter(SEX_LABELS_PARAM));
			sexVar.generateNextSample();
		
		data.addVariable("sex", sexVar);
		
			st = new StringTokenizer(getParameter(CAT_PROBS_PARAM));
			double catProbs[] = new double[st.countTokens()];
			for (int i=0 ; i<catProbs.length ; i++)
				catProbs[i] = Double.parseDouble(st.nextToken());
			catGenerator = new RandomCat(catProbs, rows * cols, seeds[1]);
			CatSampleVariable catVar = new CatSampleVariable(getParameter(CAT_NAME_PARAM),
																																				catGenerator);
			catVar.readLabels(getParameter(CAT_LABELS_PARAM));
			catVar.generateNextSample();
		
		data.addVariable("y", catVar);
		
		return data;
	}
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 3));
		
			theView = new TreatmentPictView(data, this, rows * cols, seeds[2], 0, rows, cols, rowCycle,
																								maxHorizOffset, maxVertOffset, "y");
			theView.setActiveCatVariable("sex");
			theView.setDrawIndices(false);
			
			String colourString = getParameter(PEOPLE_COLOUR_PARAM);
			theView.setPeopleColor(colourString == null || colourString.equals("white") ?
																						SamplePictView.WHITE : SamplePictView.BLACK);
			theView.doInitialisation(this);
			
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			sampleButton = new XButton(getParameter(SAMPLE_NAME_PARAM), this);
		thePanel.add(sampleButton);
		
		return thePanel;
	}
	
	private XPanel proportionPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			XPanel innerPanel = new InsetPanel(10, 5);
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(PERCENT_NAME_PARAM), "#");
			while (st.hasMoreTokens()) {
				XLabel l = new XLabel(st.nextToken(), XLabel.CENTER, this);
				l.setFont(getStandardBoldFont());
				innerPanel.add(l);
			}
		
				CatProportionView catPropnView = new CatProportionView(data, this, "y", 1, CatProportionView.PERCENTAGE, 2);
				catPropnView.setLabel("=");
				catPropnView.setFont(getStandardBoldFont());
			innerPanel.add(catPropnView);
			
			innerPanel.lockBackground(kPropnBackground);
			
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	protected void doTakeSample() {
		CatSampleVariable yVar = (CatSampleVariable)data.getVariable("y");
		yVar.generateNextSample();
		CatSampleVariable sexVar = (CatSampleVariable)data.getVariable("sex");
		sexVar.generateNextSample();
		
		theView.generateNewPictures();
		
		data.variableChanged("y");
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			doTakeSample();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}