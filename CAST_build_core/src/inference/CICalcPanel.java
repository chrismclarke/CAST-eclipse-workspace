package inference;

import java.awt.*;

import dataView.*;
import utils.*;


public class CICalcPanel extends XPanel implements LayoutManager {
	static final private Insets insets = new Insets(3, 3, 3, 3);
	
	static final private int kLeftRightTGap = 6;
	static final private int kLineGap = 4;
	
	static final private String nameString = "Interval";
	static final public String equalsString = "  =  ";
	
	private XApplet applet;
	
	private SummaryDataSet summaryData;
	private XNumberEditPanel tEdit;
	private double currentT;
	public FirstValueView interval;
	
	private boolean initialised = false;
	private Dimension tEditSize, intervalSize;
	private int equalsWidth, nameWidth, topLineHt, baseline1, image1Start, image2Start, editTop;
	
	public CICalcPanel(SummaryDataSet summaryData, XApplet applet,
																										String ciKey) {
		this.applet = applet;
		
		setLayout(this);
		
		MeanCIVariable ciVar = (MeanCIVariable)summaryData.getVariable(ciKey);
		currentT = ciVar.getT();
		String tString = (new NumValue(currentT, 2)).toString();
		
		tEdit = new XNumberEditPanel(null, tString, 3, applet);
		tEdit.setDoubleType(0.0, 9.9);
		add(tEdit);
		
		interval = new FirstValueView(summaryData, ciKey, applet);
		interval.setLabel(equalsString);
		add(interval);
		
		this.summaryData = summaryData;
		
		setForeground(Color.red);
	}
	
	public void setFont(Font f) {
		super.setFont(f);
		tEdit.setFont(f);
		interval.setFont(f);
	}
	
	public void setForeground(Color c) {
		super.setForeground(c);
		tEdit.setForeground(c);
		interval.setForeground(c);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		BufferedCanvas.checkAliasing(g);
		paintAroundItems(g);
	}
	
	private void initialise(Graphics g) {
		if (!initialised) {
			tEditSize = tEdit.getPreferredSize();
			intervalSize = interval.getMinimumSize();
			
			FontMetrics fm = g.getFontMetrics();
			
			equalsWidth = fm.stringWidth(equalsString);
			nameWidth = fm.stringWidth(nameString);
			
			baseline1 = IntervalImages.kParamHeight - IntervalImages.kParamDescent;
			image1Start = nameWidth + equalsWidth;
			image2Start = image1Start + IntervalImages.kMeanPlusMinusWidth + 2 * kLeftRightTGap
																										+ tEditSize.width;
			
			topLineHt = Math.max(IntervalImages.kParamHeight, tEditSize.height);
			editTop = (topLineHt - tEditSize.height) / 2;
			
			initialised = true;
		}
	}
	
	protected void paintAroundItems(Graphics g) {
		Dimension minimumSize = minimumLayoutSize(this);
		
		int leftStart = insets.left + (getSize().width - minimumSize.width) / 2;
		int topStart = insets.top + (getSize().height - minimumSize.height) / 2;
		
		g.drawString(nameString, leftStart, topStart + baseline1);
		g.drawString(equalsString, leftStart + nameWidth, topStart + baseline1);
		
		g.drawImage(IntervalImages.meanPlusMinus, leftStart + image1Start,
												topStart + baseline1 - IntervalImages.kParamAscent, this);
		g.drawImage(IntervalImages.timesSDMean, leftStart + image2Start,
												topStart + baseline1 - IntervalImages.kParamAscent, this);
	}
	
	protected void checkEditValues() {
			double newT = tEdit.getDoubleValue();
			if (newT != currentT) {
				currentT = newT;
				MeanCIVariable ciVar = (MeanCIVariable)summaryData.getVariable("ci");
				ciVar.setT(newT);
				summaryData.variableChanged("ci");
			}
	}
	
	private boolean localAction(Object target) {
		if (target == tEdit) {
			checkEditValues();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}

//----------------------------------------------------------------------

	public void addLayoutComponent(String name, Component comp) {
	}

	public void removeLayoutComponent(Component comp) {
	}
	
	public Dimension preferredLayoutSize(Container parent) {
		Graphics g = applet.getGraphics();
		g.setFont(getFont());
		initialise(g);
		
		int height = topLineHt + kLineGap + intervalSize.height;
		int width = nameWidth + equalsWidth + tEditSize.width + IntervalImages.kMeanPlusMinusWidth
												+ IntervalImages.kTimesSDMeanWidth + 2 * kLeftRightTGap;
		
		return new Dimension(width + insets.left + insets.right,
																			height + insets.top + insets.bottom);
	}
	
	public void layoutContainer(Container parent) {
		Dimension minimumSize = minimumLayoutSize(parent);
		
		int leftStart = insets.left + (getSize().width - minimumSize.width) / 2;
		int topStart = insets.top + (getSize().height - minimumSize.height) / 2;
		
		tEdit.setBounds(leftStart + nameWidth + equalsWidth + IntervalImages.kMeanPlusMinusWidth
							+ kLeftRightTGap, topStart + editTop, tEditSize.width, tEditSize.height);
		
		interval.setBounds(leftStart + nameWidth, topStart + topLineHt + kLineGap,
																		intervalSize.width, intervalSize.height);
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}
	
//	public Dimension maximumLayoutSize(Container parent) {
//		return preferredLayoutSize(parent);
//	}

	public void addLayoutComponent(Component comp, Object constraints) {
	}

	public void invalidateLayout(Container target) {
	}
}