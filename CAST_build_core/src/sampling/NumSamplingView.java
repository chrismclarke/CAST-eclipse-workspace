package sampling;

import java.awt.*;

import dataView.*;


public class NumSamplingView extends DataView {
//	static final public String SAMPLING_PLOT = "numSampling";
	
	static final private int kVertGap = 10;
	static final private int kHorizGap = 20;
	static final private int kFreqValGap = 7;		//		gap contains cross
	
	private boolean initialised = false;
	
	private int rows, cols, maxFreq;
	private String yKey, freqKey;
	
	private int ascent, valWidth, maxFreqWidth;
	
	private int preferredWidth, preferredHeight;
	
	public NumSamplingView(DataSet theData, XApplet applet, String yKey, String freqKey,
																														int rows, int cols, int maxFreq) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.rows = rows;
		this.cols = cols;
		this.yKey = yKey;
		this.freqKey = freqKey;
		this.maxFreq = maxFreq;
	}
	
	protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		
		NumVariable y = (NumVariable)getVariable(yKey);
		valWidth = y.getMaxWidth(g);
		maxFreqWidth = fm.stringWidth(String.valueOf(maxFreq));
		
		preferredWidth = (cols + 1) * kHorizGap + cols * (maxFreqWidth + kFreqValGap + valWidth);
		preferredHeight = (rows + 1) * kVertGap + rows * ascent;
		
		initialised = true;
		
		return true;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int horizGap = kHorizGap;
		int leftOffset = 0;
		int dWidth = getSize().width - preferredWidth;
		if (dWidth > 0)
			leftOffset = dWidth / 2;
		else
			horizGap += dWidth / (cols + 1);
		
		int vertGap = kVertGap;
		int topOffset = 0;
		int dHeight = getSize().height - preferredHeight;
		if (dHeight > 0)
			topOffset = dHeight / 2;
		else
			vertGap += dHeight / (rows + 1);
		
		g.setColor(Color.white);
		g.fillRect(leftOffset, topOffset, preferredWidth, preferredHeight);
		g.setColor(getForeground());
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		FreqVariable fVar = (FreqVariable)getVariable(freqKey);
		ValueEnumeration ye = yVar.values();
		ValueEnumeration fe = fVar.values();
		
		int baseline = topOffset + vertGap + ascent;
		for (int i=0 ; i<rows ; i++) {
			int horiz = leftOffset + horizGap + maxFreqWidth;
			for (int j=0 ; j<cols ; j++) {
				NumValue y = (NumValue)ye.nextValue();
				FreqValue f = (FreqValue)fe.nextValue();
				if (f.intValue > 1) {
					g.setColor(Color.red);
					f.drawLeft(g, horiz, baseline);
					g.drawLine(horiz + 1, baseline - 2, horiz + 5, baseline - 6);
					g.drawLine(horiz + 1, baseline - 6, horiz + 5, baseline - 2);
				}
				g.setColor(f.intValue == 0 ? Color.lightGray : Color.black);
				y.drawLeft(g, horiz + valWidth + kFreqValGap, baseline);
				
				horiz += (maxFreqWidth + kFreqValGap + valWidth + horizGap);
			}
			baseline += (vertGap + ascent);
		}
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		return new Dimension(preferredWidth, preferredHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
