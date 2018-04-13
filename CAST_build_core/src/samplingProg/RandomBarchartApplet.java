package samplingProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import sampling.*;


public class RandomBarchartApplet extends XApplet {
	static final private String HORIZ_AXIS_PARAM = "horizAxis";
	static final private String PROB_AXIS_PARAM = "probAxis";
	static final private String SAMPLING_PARAM = "sampling";		//		sample sizes, then seed
	
	static final protected String PARAM_PARAM = "parameters";
	
	protected RandomHistoView theView;
	protected VertAxis theProbAxis;
	
	private int[] sampleSize;
	
	private XButton takeSampleButton;
	private XChoice sampleSizeChoice;
	private int currentSizeIndex = 0;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("North", topPanel(data));
		add("South", controlPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		BinomialDistnVariable y = new BinomialDistnVariable(getParameter(VAR_NAME_PARAM));
		y.setParams(getParameter(PARAM_PARAM));
		data.addVariable("y", y);
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		XLabel probLabel = new XLabel(translate("Proportion"), XLabel.LEFT, this);
		probLabel.setFont(theProbAxis.getFont());
		thePanel.add(probLabel);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(HORIZ_AXIS_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		theProbAxis = new VertAxis(this);
		labelInfo = getParameter(PROB_AXIS_PARAM);
		theProbAxis.readNumLabels(labelInfo);
		thePanel.add("Left", theProbAxis);
		
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLING_PARAM));
		int noOfSizes = st.countTokens() - 1;
		sampleSize = new int[noOfSizes];
		for (int i=0 ; i<noOfSizes ; i++)
			sampleSize[i] = Integer.parseInt(st.nextToken());
		long samplingSeed = Long.parseLong(st.nextToken());
		
		theView = new RandomHistoView(data, this, theHorizAxis, theProbAxis, sampleSize[currentSizeIndex],
																										samplingSeed, "y");
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
//		controlPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		if (sampleSize.length > 1 || sampleSize[0] > 0) {
			XPanel sizePanel = new XPanel();
			sizePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			
			if (sampleSize.length == 1 && sampleSize[0] > 0) {
				XLabel sampSizeLabel = new XLabel(translate("Sample size") + ": " + sampleSize[0], XLabel.LEFT, this);
				sampSizeLabel.setFont(getStandardBoldFont());
				sizePanel.add(sampSizeLabel);
			}
			else {
					XLabel sampSizeLabel = new XLabel(translate("Sample size") + ":", XLabel.LEFT, this);
					sampSizeLabel.setFont(getStandardBoldFont());
				sizePanel.add(sampSizeLabel);
				sampleSizeChoice = new XChoice(this);
				for (int i=0 ; i<sampleSize.length ; i++)
					sampleSizeChoice.addItem((sampleSize[i] > 0) ? Integer.toString(sampleSize[i])
																				: "Infinite");
				sizePanel.add(sampleSizeChoice);
			}
		
			controlPanel.add(sizePanel);
		
			XPanel samplePanel = new XPanel();
			samplePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			takeSampleButton = new XButton(translate("Take sample"), this);
			samplePanel.add(takeSampleButton);
		
			controlPanel.add(samplePanel);
		}
		
		return controlPanel;
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			theView.takeSample();
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newSizeIndex = sampleSizeChoice.getSelectedIndex();
			if (newSizeIndex != currentSizeIndex) {
				theView.setSampleSize(sampleSize[newSizeIndex]);
				currentSizeIndex = newSizeIndex;
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