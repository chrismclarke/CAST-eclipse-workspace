package stemLeafProg;

import java.awt.*;

import dataView.*;
import utils.*;
import random.RandomNormal;

import stemLeaf.*;


public class RandomStemLeafApplet extends XApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	
	static final private int kArrowBaseLength = 30;
	static final private int kHalfArrowBaseWidth = 5;
	static final private int kArrowHeadLength = 10;
	static final private int kHalfArrowHeadWidth = 10;
	
	static final private double kCenterPropn = 0.3;
	
	private DataSet data;
	private XButton sampleButton, rememberButton;
	private RandomNormal generator;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout((1 - kCenterPropn) / 2, 0));
			topPanel.add(ProportionLayout.LEFT, stemLeafView("y"));
			
				XPanel topRightPanel = new XPanel();
				topRightPanel.setLayout(new ProportionLayout(kCenterPropn / (1 - kCenterPropn), 0));
				topRightPanel.add(ProportionLayout.LEFT, copyPanel(data));
				topRightPanel.add(ProportionLayout.RIGHT, stemLeafView("yCopy"));
			
			topPanel.add(ProportionLayout.RIGHT, topRightPanel);
			
		add("Center", topPanel);
		
		add("South", samplePanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
			
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		generator = new RandomNormal(randomInfo);
		double vals[] = generator.generate();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), vals);
		
		for (int i=0 ; i<vals.length ; i++)
			vals[i] = Double.NaN;
		data.addNumVariable("yCopy", getParameter(VAR_NAME_PARAM), vals);
		
		return data;
	}
	
	private StemAndLeafView stemLeafView(String yKey) {
		StemAndLeafView theStemAndLeaf = new StemAndLeafView(data, this, getParameter(STEM_AXIS_PARAM));
		theStemAndLeaf.setActiveNumVariable(yKey);
		theStemAndLeaf.lockBackground(Color.white);
		return theStemAndLeaf;
	}
	
	private XPanel copyPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 12));
			
			ArrowCanvas arrow = new ArrowCanvas(kArrowBaseLength, kHalfArrowBaseWidth,
																	kArrowHeadLength, kHalfArrowHeadWidth, ArrowCanvas.RIGHT);
			arrow.setForeground(Color.red);
		thePanel.add(arrow);
			
			rememberButton = new XButton(translate("Remember"), this);
		thePanel.add(rememberButton);
		
			arrow = new ArrowCanvas(kArrowBaseLength, kHalfArrowBaseWidth,
																	kArrowHeadLength, kHalfArrowHeadWidth, ArrowCanvas.RIGHT);
			arrow.setForeground(Color.red);
		thePanel.add(arrow);
		
		return thePanel;
	}
	
	private XPanel samplePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout((1 - kCenterPropn) / 2, 0));
			
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 3));
				sampleButton = new XButton(translate("Another sample"), this);
			leftPanel.add(sampleButton);
			
		thePanel.add(ProportionLayout.LEFT, leftPanel);
		
		thePanel.add(ProportionLayout.RIGHT, new XPanel());
			
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			NumVariable yVar = (NumVariable)data.getVariable("y");
			double vals[] = generator.generate();
			yVar.setValues(vals);
			data.variableChanged("y");
			return true;
		}
		else if (target == rememberButton) {
			NumVariable yVar = (NumVariable)data.getVariable("y");
			double vals[] = new double[yVar.noOfValues()];
			for (int i=0 ; i<vals.length ; i++)
				vals[i] = yVar.doubleValueAt(i);
			
			NumVariable yCopyVar = (NumVariable)data.getVariable("yCopy");
			yCopyVar.setValues(vals);
			data.variableChanged("yCopy");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}