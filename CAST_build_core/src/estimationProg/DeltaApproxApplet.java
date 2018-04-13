package estimationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;

import estimation.*;


public class DeltaApproxApplet extends XApplet {
	static final private String X_AXIS_PARAM = "xAxis";
	static final private String X_AXIS_NAME_PARAM = "xAxisName";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String TRANSFORM_AXIS_PARAM = "transformAxis";
	static final private String MEAN_PARAM = "mean";
	
	static final private String kEmptyVertAxis = "0 1 2 1";	//axis not used and does not show labels
	
	private DataSet data;
	
	private int sampleSize[];
	private String xAxisInfo[];
	private String transformAxisInfo[];
	private double mean;
	
	private XPanel displayPanel;
	private NumCatAxis horizAxis, transformAxis;
	protected DeltaApproxView pdfView;
	
	private XChoice	nChoice;
	
	public void setupApplet() {
		initialise();
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
		add("North", topPanel());
		
		add("South", sampleSizePanel());
		
		displayPanel = pdfPanel(data, this);
		add("Center", displayPanel);
		
		updateSampleSize();
	}
	
	private void initialise() {
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		int nSizes = st.countTokens();
		sampleSize = new int[nSizes];
		xAxisInfo = new String[nSizes];
		transformAxisInfo = new String[nSizes];
		for (int i=0 ; i<nSizes ; i++) {
			sampleSize[i] = Integer.parseInt(st.nextToken());
			xAxisInfo[i] = getParameter(X_AXIS_PARAM + i);
			transformAxisInfo[i] = getParameter(TRANSFORM_AXIS_PARAM + i);
		}
		mean = Double.parseDouble(getParameter(MEAN_PARAM));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		GammaDistnVariable theDistn = new GammaDistnVariable("x");
		data.addVariable("distn", theDistn);
		data.setSelection("distn", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		return data;
	}
	
	private XPanel topPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		String pdfLabel = translate("pdf of sample mean");
		thePanel.add("West", new XLabel(pdfLabel, XLabel.LEFT, this));
		
		String transformString = "y = g(x) = x\u00B2";
		XLabel transformLabel = new XLabel(transformString, XLabel.LEFT, this);
		transformLabel.setForeground(DeltaApproxView.kTransformColor);
		thePanel.add("East", transformLabel);
		
		return thePanel;
	}
	
	protected XPanel pdfPanel(DataSet data, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		horizAxis = new HorizAxis(applet);
		horizAxis.setAxisName(getParameter(X_AXIS_NAME_PARAM));
		thePanel.add("Bottom", horizAxis);
		
		VertAxis densityAxis = new VertAxis(applet);
		densityAxis.readNumLabels(kEmptyVertAxis);
		thePanel.add("Left", densityAxis);
		
		transformAxis = new VertAxis(applet);
		transformAxis.setForeground(DeltaApproxView.kTransformColor);
		thePanel.add("Right", transformAxis);
		
		pdfView = new DeltaApproxView(data, applet, "distn", horizAxis, transformAxis, mean);
		pdfView.setSupport(0.0, Double.POSITIVE_INFINITY);
		pdfView.lockBackground(Color.white);
		thePanel.add("Center", pdfView);
		
		return thePanel;
	}
	
	protected void updateSampleSize() {
		int nIndex = nChoice.getSelectedIndex();
		
		int n = sampleSize[nIndex];
		double alpha = n * mean;
		double beta = n;
		
		GammaDistnVariable gammaVar = (GammaDistnVariable)data.getVariable("distn");
		gammaVar.setShape(alpha);
		gammaVar.setScale(1 / beta);
		
		horizAxis.readNumLabels(xAxisInfo[nIndex]);
		transformAxis.readNumLabels(transformAxisInfo[nIndex]);
		
		data.variableChanged("distn");
		displayPanel.revalidate();
	}
	
	protected XPanel sampleSizePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		nChoice = new XChoice("Sample size:", XChoice.HORIZONTAL, this);
		for (int i=0 ; i<sampleSize.length ; i++)
			nChoice.addItem(String.valueOf(sampleSize[i]));
		
		thePanel.add(nChoice);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == nChoice) {
			updateSampleSize();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}