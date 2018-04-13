package exper;

import java.awt.*;

import dataView.*;


public class TreatmentBiasView extends DataView {
//	static final public String TREATMENT_BIAS_VIEW = "treatmentBias";
	
	static final private int kBoxTopBottom = 4;
	static final private int kBoxLeftRight = 6;
	static final private int kTextBoxHorizGap = 10;
	static final private int kTopBottomSlop = 3;
	static final private int kLeftRightSlop = 3;
	
	static final private String kBiasString = "Design Bias";
	
	private String treatmentKey, plotEffectKey;
	private NumValue maxMean;
	
	private boolean initialised = false;
	
	private int ascent, boxHeight, boxWidth, biasStringWidth;
	
	public TreatmentBiasView(DataSet theData, XApplet applet, String treatmentKey,
						String plotEffectKey, NumValue maxMean) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.treatmentKey = treatmentKey;
		this.plotEffectKey = plotEffectKey;
		this.maxMean = maxMean;
	}
	
	private boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			boxHeight = ascent + 2 * kBoxTopBottom;
			
			boxWidth = maxMean.stringWidth(g) + 2 * kBoxLeftRight;
			biasStringWidth = fm.stringWidth(kBiasString);
			
			initialised = true;
			return true;
		}
		return false;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		CatVariable treat = (CatVariable)getVariable(treatmentKey);
		Value treatmentCat = treat.getLabel(0);
		NumVariable effect = (NumVariable)getVariable(plotEffectKey);
		
		int baseline = kTopBottomSlop + boxHeight - kBoxTopBottom;
		int left = kLeftRightSlop;
		g.drawString(kBiasString, left, baseline);
		left += (biasStringWidth + kTextBoxHorizGap);
		
		ValueEnumeration ee = effect.values();
		ValueEnumeration te = treat.values();
		int count[] = new int[2];
		double sum[] = new double[2];
		while (te.hasMoreValues()) {
			int cat = (te.nextValue() == treatmentCat) ? 0 : 1;
			count[cat] ++;
			sum[cat] += ee.nextDouble();
		}
		
		NumValue bias = new NumValue(sum[0] / count[0] - sum[1] / count[1], maxMean.decimals);
		
		g.drawRect(left, baseline - ascent - kBoxTopBottom, boxWidth - 1, boxHeight - 1);
		g.setColor(Color.white);
		g.fillRect(left + 1, baseline - ascent - kBoxTopBottom + 1, boxWidth - 2, boxHeight - 2);
		g.setColor(Color.black);
		bias.drawLeft(g, left + boxWidth - kBoxLeftRight, baseline);
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		int height = boxHeight + 2 * kTopBottomSlop;
		int width = biasStringWidth + boxWidth + kTextBoxHorizGap + 2 * kLeftRightSlop;
		return new Dimension(width, height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(treatmentKey))
			repaint();
	}
	
	protected void doChangeSelection(Graphics g) {
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
