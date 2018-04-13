package inferenceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import imageUtils.*;

import sampling.*;
import stdError.*;
import inference.*;
import imageGroups.*;



class SuccessSlider extends XSlider {
	public SuccessSlider(int n, int startX, String successName, XApplet applet) {
		super("0", String.valueOf(n), applet.translate("No. of") + " " + successName + ", x = ", 0, n, startX, applet);
	}
	
	protected Value translateValue(int val) {
		return new NumValue(val, 0);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return translateValue(getMaxValue()).stringWidth(g);
	}
}


public class SePGraphApplet extends XApplet {
	static final private String N_PARAM = "n";
	static final private String SE_INFO_PARAM = "seAxis";
	static final protected String P_INFO_PARAM = "pAxis";
	static final protected String DECIMALS_PARAM = "decimals";
	
	static final protected Color kDarkRedColor = new Color(0x990000);
	static final private Color kCIBackground = new Color(0xFFF3CE);
	
	private int counts[] = new int[2];
	
	private int sampleSize[];
	private int currentSampleSizeIndex;
	
	protected DataSet data;
	
	private SuccessSlider xSlider;
	private XChoice sampleSizeChoice;
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		
		readSampleSizes();
		data = getData();
		
		setLayout(new BorderLayout(15, 10));
		
		add("North", controlPanel(data));
		add("West", sePanel(data));
		add("Center", graphPanel(data));
		
			XPanel ciP = ciPanel(data);
		if (ciP != null)
			add("South", ciPanel(data));
	}
	
	private void readSampleSizes() {
		StringTokenizer st = new StringTokenizer(getParameter(N_PARAM));
		sampleSize = new int[st.countTokens()];
		sampleSizeChoice = new XChoice(this);
		for (int i=0 ; i<sampleSize.length ; i++) {
			String nextSize = st.nextToken();
			if (nextSize.indexOf("*") == 0) {
				nextSize = nextSize.substring(1);
				currentSampleSizeIndex = i;
			}
			sampleSizeChoice.addItem("n = " + nextSize);
			sampleSize[i] = Integer.parseInt(nextSize);
		}
		sampleSizeChoice.select(currentSampleSizeIndex);
	}
	
	private DataSet getData() {
		data = new DataSet();
		
			int n = sampleSize[currentSampleSizeIndex];
			counts[0] = n / 3;
			counts[1] = n - counts[0];
		
		CatVariable x = new CatVariable("p");
		x.readLabels(getParameter(CAT_LABELS_PARAM));
		x.setCounts(counts);
		
		data.addVariable("x", x);
		
		return data;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
			XPanel nPanel = new XPanel();
			nPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				if (sampleSize.length == 1) {
					int n = sampleSize[currentSampleSizeIndex];
					XLabel nLabel = new XLabel("n = " + n, XLabel.CENTER, this);
					nLabel.setFont(getBigBoldFont());
					nLabel.setForeground(Color.blue);
					nPanel.add(nLabel);
				}
				else
					nPanel.add(sampleSizeChoice);
			
		thePanel.add("West", nPanel);
		
			CatVariable x = (CatVariable)data.getVariable("x");
			String success = x.getLabel(0).toString();
			int n = sampleSize[currentSampleSizeIndex];
			xSlider = new SuccessSlider(n, counts[0], success, this);
			xSlider.setFont(getStandardBoldFont());
		thePanel.add("Center", xSlider);
		
			XPanel pPanel = new XPanel();
			pPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			pPanel.add(new SummaryView(data, this, "x", null, SummaryView.PROPN, decimals, SummaryView.SAMPLE));
			
		thePanel.add("East", pPanel);
		
		return thePanel;
	}
	
	protected XPanel graphPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis seAxis = new VertAxis(this);
			seAxis.readNumLabels(getParameter(SE_INFO_PARAM));
			seAxis.setForeground(kDarkRedColor);
		thePanel.add("Left", seAxis);
		
			HorizAxis pAxis = new HorizAxis(this);
			pAxis.readNumLabels(getParameter(P_INFO_PARAM));
			pAxis.setAxisName(translate("Sample proportion") + ", p");
			pAxis.setForeground(Color.blue);
		thePanel.add("Bottom", pAxis);
		
			SePropnGraphView view = new SePropnGraphView(data, this, "x", pAxis, seAxis);
			view.lockBackground(Color.white);
		thePanel.add("Center", view);
		
		return thePanel;
	}
	
	
	protected XPanel sePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			
			ImageCanvas eqn = new ImageCanvas("ci/sePEqn.png", this);
		thePanel.add(eqn);
			
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			PErrorSDValueView seView = new PErrorSDValueView(data, this, null, "x", decimals);
			seView.setForeground(kDarkRedColor);
		thePanel.add(seView);
		
		return thePanel;
	}
	
	
	protected XPanel ciPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 5);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
			XPanel titlePanel = new XPanel();
			titlePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				XLabel title = new XLabel(translate("95% Conf Interval") + ":", XLabel.LEFT, this);
				title.setFont(getStandardBoldFont());
			titlePanel.add(title);
		
		thePanel.add(titlePanel);
		
			XPanel valuePanel = new XPanel();
			valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
				int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
				PCIValueView valueView = new PCIValueView(data, this, "x", decimals);
				valueView.setFont(getBigFont());
			valuePanel.add(valueView);
		
		thePanel.add(valuePanel);
		
		thePanel.lockBackground(kCIBackground);
		return thePanel;
	}
	
	protected void changeSampleSize(int n, int x) {
		CatVariable xVar = (CatVariable)data.getVariable("x");
		counts[0] = x;
		counts[1] = n - x;
		xVar.setCounts(counts);
		
		xSlider.setMaxValue(String.valueOf(n), n);
		xSlider.setValue(x);
		xSlider.repaint();
	}
	
	private boolean localAction(Object target) {
		if (target == sampleSizeChoice) {
			int newSampleSizeIndex = sampleSizeChoice.getSelectedIndex();
			if (newSampleSizeIndex != currentSampleSizeIndex) {
				int oldN = sampleSize[currentSampleSizeIndex];
				currentSampleSizeIndex = newSampleSizeIndex;
				
				int n = sampleSize[currentSampleSizeIndex];
				changeSampleSize(n, (counts[0] * n) / oldN);
			}
			return true;
		}
		else if (target == xSlider) {
			int newX = xSlider.getValue();
			int n = counts[0] + counts[1];
			counts[0] = newX;
			counts[1] = n - newX;
			
			CatVariable x = (CatVariable)data.getVariable("x");
			x.setCounts(counts);
			
			data.variableChanged("x");
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}