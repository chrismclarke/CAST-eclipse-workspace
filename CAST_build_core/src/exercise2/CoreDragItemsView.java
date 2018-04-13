package exercise2;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;


abstract public class CoreDragItemsView extends DataView implements StatusInterface {
	static final public int IMAGES = 0;
	static final public int TEXT_STRINGS = 1;		//	multi-line
	static final public int TEXT_LABELS = 2;
	
	static final private Color kFixedBackground = new Color(0xDDDDDD);
	static final private Color kSelectedBackground = new Color(0xFFFF99);
	
	private int[] order;
	protected int displayType;
	
	private boolean fixedTopDistn = false;
	
	public CoreDragItemsView(DataSet theData, XApplet applet, int[] order, int displayType, 
																																								Insets insets) {
		super(theData, applet, insets);
		
		this.order = order;
		this.displayType = displayType;
	}
	
	public void setFixedTopDistn(boolean fixedTopDistn) {
		this.fixedTopDistn = fixedTopDistn;
	}
	
	public String getStatus() {
		String s = "";
		for (int i=0 ; i<order.length ; i++)
			s += order[i] + " ";
		return s;
	}
	
	public void setStatus(String status) {
		StringTokenizer st = new StringTokenizer(status);
		for (int i=0 ; i<order.length ; i++)
			order[i] = Integer.parseInt(st.nextToken());
		repaint();
	}
	
	abstract protected int noOfItems();
	abstract protected void drawBackground(Graphics g);
	abstract protected void drawOneItem(Graphics g, int index, int baseline, int height);
	abstract protected String getItemName(int index);
	
	
	private void drawOneName(Graphics g, int index, int baseline, int height) {
		String theLabel = getItemName(index);
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int textWidth = fm.stringWidth(theLabel);
		
		int textBaseline = translateToScreen(0, baseline + height / 3 - ascent / 2, null).y;
		int textLeft = (getSize().width - textWidth) / 2;
		g.drawString(theLabel, textLeft, textBaseline);
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		
		int noOfDistns = noOfItems();
		int height = getSize().height / noOfDistns;
		
		if (fixedTopDistn && displayType != TEXT_LABELS) {
			g.setColor(kFixedBackground);
//			int baselineZero = getSize().height / noOfDistns;
			g.fillRect(0, 0, getSize().width, height + 1);
		}
		
		if (dragIndex >= 0 && displayType != TEXT_LABELS) {
			g.setColor(kSelectedBackground);
			g.fillRect(0, getSize().height - height - dragBaseline, getSize().width, height);
		}
		
		for (int i=0 ; i<noOfDistns ; i++) {
			int baseline;
			if (dragIndex == i) {
				baseline = dragBaseline;
				g.setColor(Color.red);
			}
			else if (dragIndex < 0) {
				baseline = getSize().height * i / noOfDistns;
				g.setColor(getForeground());
			}
			else {
				int distnHeight = getSize().height / noOfDistns;
				int lowDestIndex = dragBaseline / distnHeight;
				int offset = dragBaseline - lowDestIndex * distnHeight;
				if (i < Math.min(lowDestIndex, dragIndex) || i > Math.max(lowDestIndex + 1, dragIndex))
					baseline = getSize().height * i / noOfDistns;
				else if (lowDestIndex >= dragIndex) {
					if (i == lowDestIndex + 1)
						baseline = getSize().height * i / noOfDistns - offset;
					else
						baseline = getSize().height * (i - 1) / noOfDistns;
				}
				else {
					if (i == lowDestIndex)
						baseline = getSize().height * (i + 1) / noOfDistns - offset;
					else
						baseline = getSize().height * (i + 1) / noOfDistns;
				}
				g.setColor(getForeground());
			}
			
			if (displayType == TEXT_LABELS) {
				if (i == noOfDistns - 1 && fixedTopDistn)
					g.setColor(Color.gray);
				drawOneName(g, order[i], baseline, height);
			}
			else
				drawOneItem(g, order[i], baseline, height);
		}
	}
	

//-----------------------------------------------------------------------------------

	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	private int dragIndex = -1;
	private int hitOffset, dragBaseline;
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		int nItems = noOfItems();
		int hitIndex = hitPos.y * nItems / getSize().height;
		if (fixedTopDistn && hitIndex == nItems - 1)
			return null;
		int hitOffset = hitPos.y - getSize().height * hitIndex / nItems;		//	offset from baseline
		if (hitIndex < 0 || hitIndex >= nItems)
			return null;
		else
			return new VertDragPosInfo(hitPos.y, hitIndex, hitOffset);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		int nItems = noOfItems();
		int maxY = getSize().height * (nItems - (fixedTopDistn ? 2 : 1)) / nItems;
		if (hitPos.y - hitOffset < 0 || hitPos.y - hitOffset >= maxY)
			return null;
		else
			return new VertDragPosInfo(hitPos.y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		VertDragPosInfo dragPos = (VertDragPosInfo)startInfo;
		hitOffset = dragPos.hitOffset;
		dragIndex = dragPos.index;
		dragBaseline = dragPos.y - hitOffset;
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			dragBaseline = dragPos.y - hitOffset;
			repaint();
			
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		int distnHeight = getSize().height / noOfItems();
		int destIndex = (dragBaseline + distnHeight / 2) / distnHeight;
		
		int tempOrder = order[dragIndex];
		if (destIndex > dragIndex)
			for (int i=dragIndex ; i<destIndex ; i++)
				order[i] = order[i + 1];
		else if (destIndex < dragIndex)
			for (int i=dragIndex ; i>destIndex ; i--)
				order[i] = order[i - 1];
		order[destIndex] = tempOrder;
		
		dragIndex = -1;
		repaint();
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		if (displayType == TEXT_LABELS) {
			FontMetrics fm = getGraphics().getFontMetrics();
			int nItems = noOfItems();
			int maxWidth = 0;
			for (int i=0 ; i<nItems ; i++) {
				String theLabel = getItemName(i);
				maxWidth = Math.max(maxWidth, fm.stringWidth(theLabel));
			}
			return new Dimension(maxWidth, (fm.getAscent() + fm.getDescent()) * nItems);
		}
		else
			return super.getMinimumSize();
	}
	
	public Dimension getPreferredSize() {
		if (displayType == TEXT_LABELS)
			return getMinimumSize();
		else
			return super.getPreferredSize();
	}
}