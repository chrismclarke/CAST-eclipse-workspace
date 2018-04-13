package structureProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;

import structure.*;


public class PictValueApplet extends XApplet {
	private final static String NO_OF_COLS_PARAM = "noOfCols";
	private final static String PICT_NAME_PARAM = "pictName";
	private final static String PICT_SIZE_PARAM = "pictSize";
	protected final static String DECIMALS_PARAM = "decimals";
	private final static String RANDOM_PARAM = "random";
	private final static String SAMPLE_NAME_PARAM = "sampleButtonName";
	
	static final private Color kDarkRed = new Color(0x990000);
	
	protected DataSet data;
	private RandomNormal generator;
	
	private XButton sampleButton;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(10, 0));
			
		add("Center", dataPanel(data, "y", kDarkRed));
		
		add("East", controlPanel());
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			generator = new RandomNormal(getParameter(RANDOM_PARAM));
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			NumSampleVariable yVar = new NumSampleVariable(getParameter(VAR_NAME_PARAM),
																																	generator, decimals);
			yVar.generateNextSample();
		
		data.addVariable("y", yVar);
		
		return data;
	}
	
	protected XPanel dataPanel(DataSet data, String yKey, Color fontColor) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 3));
			
			XLabel varNameLabel = new XLabel(data.getVariable(yKey).name, XLabel.CENTER, this);
			varNameLabel.setFont(getBigBoldFont());
			varNameLabel.setForeground(fontColor);
		thePanel.add("North", varNameLabel);
		
			String pictName = getParameter(PICT_NAME_PARAM) + ".png";
			StringTokenizer st = new StringTokenizer(getParameter(PICT_SIZE_PARAM));
			int pictWidth = Integer.parseInt(st.nextToken());
			int pictHeight = Integer.parseInt(st.nextToken());
			int noOfCols = Integer.parseInt(getParameter(NO_OF_COLS_PARAM));
			PictValueView pictView = new PictValueView(data, this, pictName, pictWidth, pictHeight, yKey, noOfCols);
			pictView.setFont(getBigBoldFont());
			pictView.setForeground(fontColor);
			pictView.lockBackground(Color.white);
		thePanel.add("Center", pictView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			sampleButton = new XButton(getParameter(SAMPLE_NAME_PARAM), this);
		thePanel.add(sampleButton);
		
		return thePanel;
	}
	
	protected void doTakeSample() {
		NumSampleVariable yVar = (NumSampleVariable)data.getVariable("y");
		yVar.generateNextSample();
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