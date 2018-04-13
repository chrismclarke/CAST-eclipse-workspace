package samplingProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import sampling.*;
import random.RandomNormal;


public class NumSamplingApplet extends XApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String DIMENSION_PARAM = "dimension";
	static final private String SAMPLING_PARAM = "samplingSeed";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	
	static final private int[] kDefaultSampleSize = {5, 20, 40};
	
	private int[] sampleSize;
	
	private DataSet data;
	private XButton takeSampleButton;
	private XChoice sampleSizeChoice, sampleSchemeChoice;
	protected boolean currentWithReplacement;
	private int currentSizeIndex;
	
	public void setupApplet() {
		data = getData();
		
		readSampleSizes();
		
		setLayout(new BorderLayout(20, 0));
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
		add("East", rightPanel(data));
	}
	
	private void readSampleSizes() {
		String sampleSizeString = getParameter(SAMPLE_SIZE_PARAM);
		if (sampleSizeString == null)
			sampleSize = kDefaultSampleSize;
		else {
			StringTokenizer st = new StringTokenizer(sampleSizeString);
			int noOfSizes = 0;
			while(st.hasMoreTokens()) {
				noOfSizes ++;
				st.nextToken();
			}
			sampleSize = new int[noOfSizes];
			st = new StringTokenizer(sampleSizeString);
			for (int i=0 ; i<noOfSizes ; i++)
				sampleSize[i] = Integer.parseInt(st.nextToken());
		}
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		double vals[] = generateData();
		NumVariable y = new NumVariable(getParameter(VAR_NAME_PARAM));
		y.setValues(vals);
		y.setDecimals(Integer.parseInt(getParameter(DECIMALS_PARAM)));
		data.addVariable("y", y);
		
		FreqVariable f = new FreqVariable("frequency", y.noOfValues(),
															Long.parseLong(getParameter(SAMPLING_PARAM)));
		int intVal[] = new int[y.noOfValues()];
		f.setValues(intVal);
		data.addVariable("freq", f);
		
		return data;
	}
	
	private double[] generateData() {
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		RandomNormal generator = new RandomNormal(randomInfo);
		double vals[] = generator.generate();
		int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		double factor = Math.pow(10.0, decimals);
		for (int i=0 ; i<vals.length ; i++) {
			vals[i] *= vals[i];							//		square every value to give skew distn
			vals[i] = Math.round(factor * vals[i]) / factor;
		}
		return vals;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		StringTokenizer st = new StringTokenizer(getParameter(DIMENSION_PARAM));
		int cols = Integer.parseInt(st.nextToken());
		int rows = Integer.parseInt(st.nextToken());
		
		NumSamplingView theView = new NumSamplingView(data, this, "y", "freq", rows, cols, 9);
		theView.setFont(getBigFont());
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected XPanel sampleSizePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
		
		if (sampleSize.length > 1) {
				XPanel sizePanel = new XPanel();
				sizePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
				sizePanel.add(new XLabel(translate("Sample size") + ":", XLabel.LEFT, this));
					sampleSizeChoice = new XChoice(this);
					for (int i=0 ; i<sampleSize.length ; i++)
						sampleSizeChoice.addItem(String.valueOf(sampleSize[i]));
					currentSizeIndex = 0;
				sizePanel.add(sampleSizeChoice);
			
			thePanel.add(sizePanel);
		}
		
			XPanel samplePanel = new XPanel();
			samplePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				takeSampleButton = new XButton(translate("Take sample"), this);
			samplePanel.add(takeSampleButton);
		
		thePanel.add(samplePanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
		
		XPanel schemePanel = new XPanel();
		schemePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
		XLabel theLabel = new XLabel(translate("Sampling scheme") + ":", XLabel.LEFT, this);
		schemePanel.add(theLabel);
		sampleSchemeChoice = new XChoice(this);
		sampleSchemeChoice.addItem(translate("With replacement"));
		sampleSchemeChoice.addItem(translate("Without replacement"));
		currentWithReplacement = true;
		schemePanel.add(sampleSchemeChoice);
		
		controlPanel.add(schemePanel);
		
		controlPanel.add(sampleSizePanel());
		
		return controlPanel;
	}
	
	protected void doTakeSample() {
		((FreqVariable)data.getVariable("freq")).sample(sampleSize[currentSizeIndex],
																				currentWithReplacement);
		data.variableChanged("freq");
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			doTakeSample();
			return true;
		}
		else if (target == sampleSchemeChoice) {
			boolean newWithReplacement = (sampleSchemeChoice.getSelectedIndex() == 0);
			if (newWithReplacement != currentWithReplacement) {
				((FreqVariable)data.getVariable("freq")).clearCounts();
				data.variableChanged("freq");
				currentWithReplacement = newWithReplacement;
			}
		}
		else if (target == sampleSizeChoice) {
			int newSizeIndex = sampleSizeChoice.getSelectedIndex();
			if (newSizeIndex != currentSizeIndex) {
				((FreqVariable)data.getVariable("freq")).clearCounts();
				data.variableChanged("freq");
				currentSizeIndex = newSizeIndex;
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}