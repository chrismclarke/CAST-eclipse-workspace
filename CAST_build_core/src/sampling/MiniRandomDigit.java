package sampling;

import java.awt.*;

import dataView.*;


public class MiniRandomDigit extends CoreRandomDigit {
	static final private int kEndExtra[] = {120, 100, 80, 60, 50};
	static final protected int kTopBorder = 2;
	static final protected int kBottomBorder = 4;
	static final protected int kLeftRightBorder = 4;
	
	static final private int kFullCycles = 3;
	
	static final private Color kPaleBackground = new Color(0xFFFFCC);
	static final protected Color kDigitColor = new Color(0x000099);
	static final private Color kBorderColor = new Color(0x999999);
	
	private boolean initialised = false;
	private int ascent, zeroWidth;
	
	static public Font getBiggerFont(Font standardFont) {
		return new Font(standardFont.getName(), standardFont.getStyle(), standardFont.getSize() * 2);
	}
	
	public MiniRandomDigit(int maxDigit, XApplet applet, RandDigActionInterface digitAction, long seed) {
		super(maxDigit, applet, digitAction, seed);
		lockBackground(kPaleBackground);
	}
	
	public MiniRandomDigit(XApplet applet, RandDigActionInterface digitAction, long seed) {
		super(applet, digitAction, seed);
		lockBackground(kPaleBackground);
	}
	
	private boolean initialise(Graphics g) {
		if (initialised)
			return false;
		initialised = true;
		
		Font newFont = getBiggerFont(getFont());
		setFont(newFont);
		g.setFont(newFont);
		
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		zeroWidth = fm.stringWidth("0");
		return true;
	}
	
	public void corePaint(Graphics g) {
		initialise(g);
		int digit = (oldDigit + currentFrame) % (maxDigit + 1);
		
		g.setColor(kDigitColor);
		g.drawString(String.valueOf(digit), kLeftRightBorder, kTopBorder + ascent);
		
		g.setColor(kBorderColor);
		g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
	}
	
//---------------------------------------------------------------------
	
	protected void setupNewDigit() {
		oldDigit = currentDigit;
		currentDigit = generator.generateOne();
		
		int mainSteps = currentDigit - oldDigit - 1;
		if (mainSteps < 1)
			mainSteps += (maxDigit + 1);
		mainSteps += kFullCycles * (maxDigit + 1);
		finalFrame = mainSteps;
	}
	
	protected int getNextDelay() {
		int nextDelay = super.getNextDelay();
		if (currentFrame < kEndExtra.length)
			nextDelay += kEndExtra[currentFrame];
		else if (currentFrame > finalFrame - kEndExtra.length)
			nextDelay += kEndExtra[finalFrame - currentFrame];
		return (nextDelay / 3);				//	scales 60 millisec to 20 millisec
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		g.setFont(getFont());					//		shouldn't really be needed, but font doesn't seem to always get initialised
		initialise(g);
		return new Dimension(zeroWidth + 2 * kLeftRightBorder, ascent + kTopBorder + kBottomBorder);
	}
}