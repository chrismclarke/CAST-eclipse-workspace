package normalProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import valueList.*;

import normal.*;


public class StdNormalApplet extends XApplet {
	static final private String Z_SCORE_PARAM = "zScore";
	
//	static final private String kStdNormalAxis = "-3.5 3.5 -3 1";
	static final private String kStdNormalDistn = "0 1";
	
	static final private String[] kDefaultZ = {"1", "2", "3"};
	
	static final private Color kInnerColor = new Color(0x3366FF);
	static final private Color kTailColor = new Color(0xCCCCCC);
	
	private DataSet data;
	
	private XChoice zChoice;
	private int currentZIndex;
	private double currentZ;
	private String[] zConstants;
	
	private StdNormalView normView;
	
	public void setupApplet() {
		String zParam = getParameter(Z_SCORE_PARAM);
		if (zParam == null)
			zConstants = kDefaultZ;
		else {
			StringTokenizer st = new StringTokenizer(zParam);
			zConstants = new String[st.countTokens()];
			for (int i=0 ; i<zConstants.length ; i++)
				zConstants[i] = st.nextToken();
		}
		currentZ = Double.parseDouble(zConstants[0]);
		currentZIndex = 0;
		
		data = getData();
		
		setLayout(new BorderLayout(0, 20));
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		NormalDistnVariable z = new NormalDistnVariable("z");
		z.setParams(kStdNormalDistn);
		z.setMinSelection(-currentZ);
		z.setMaxSelection(currentZ);
		data.addVariable("z", z);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			StdNormalAxis theHorizAxis = new StdNormalAxis(this);
			theHorizAxis.setupStdAxis();
		thePanel.add("Bottom", theHorizAxis);
		
			normView = new StdNormalView(data, this, theHorizAxis, "z");
			normView.setZValue(currentZ);
			normView.setDistnColors(kInnerColor, kTailColor);
			normView.lockBackground(Color.white);
		thePanel.add("Center", normView);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			StringTokenizer st = new StringTokenizer(translate("Propn within*of"), "*");
			String propnString = st.nextToken();
			String ofString = st.nextToken();
		
			XLabel label1 = new XLabel(propnString + " ", XLabel.LEFT, this);
			label1.setFont(getBigFont());
		thePanel.add(label1);
		
			zChoice = new XChoice(this);
			for (int i=0 ; i<zConstants.length ; i++)
				zChoice.addItem(zConstants[i]);
		thePanel.add(zChoice);
		
			XLabel label2 = new XLabel("\u03C3 " + ofString + " \u03BC = ", XLabel.LEFT, this);
			label2.setFont(getBigFont());
		thePanel.add(label2);
		
			ProportionView propnView = new ProportionView(data, "z", this);
			propnView.setLabel("");
			propnView.setFont(getBigFont());
		thePanel.add(propnView);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == zChoice) {
			int newZIndex = zChoice.getSelectedIndex();
			if (newZIndex != currentZIndex) {
				currentZIndex = newZIndex;
				currentZ = Double.parseDouble(zConstants[currentZIndex]);
				
				normView.setZValue(currentZ);
				NormalDistnVariable zDistn = (NormalDistnVariable)data.getVariable("z");
				zDistn.setMinSelection(-currentZ);
				zDistn.setMaxSelection(currentZ);
				
				data.variableChanged("z");
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