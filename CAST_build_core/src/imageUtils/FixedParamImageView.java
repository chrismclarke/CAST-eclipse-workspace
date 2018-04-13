package imageUtils;

import java.awt.*;

import dataView.*;
import images.*;

public class FixedParamImageView extends BufferedCanvas {
	static final private int kImageGap = 5;
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	private NumValue value;
	private Image paramImage;
	private int ascent, descent, gifWidth;
	
	public FixedParamImageView(XApplet applet, String gifName, int ascent, int descent,
																													int gifWidth, NumValue value) {
		super(applet);
		if (paramImage == null) {
			paramImage = CoreImageReader.getImage(gifName);
			MediaTracker tracker = new MediaTracker(applet);
			tracker.addImage(paramImage, 0);
			try {
				tracker.waitForAll(kMaxWait);
			} catch (InterruptedException e) {
			}
		}
		this.value = value;
		this.ascent = ascent;
		this.descent = descent;
		this.gifWidth = gifWidth;
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		FontMetrics fm = g.getFontMetrics();
		return new Dimension(gifWidth + kImageGap + value.stringWidth(g),
						Math.max(fm.getAscent(), ascent) + Math.max(fm.getDescent(), descent));
	}
	
	public void corePaint(Graphics g) {
		int baseline = Math.max(g.getFontMetrics().getAscent(), ascent);
		g.drawImage(paramImage, 0, baseline - ascent, this);
		
		value.drawRight(g, gifWidth + kImageGap, baseline);
	}
	

	protected boolean needsHitToDrag() { return true; }
	protected boolean canDrag() { return false; }
	
	protected boolean startDrag(PositionInfo startInfo) { return false; }
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {}
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {}
}