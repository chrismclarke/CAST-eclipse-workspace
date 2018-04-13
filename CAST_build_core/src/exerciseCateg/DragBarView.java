package exerciseCateg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import exercise2.*;


public class DragBarView extends DataView implements StatusInterface {
//	static public final String DRAG_BAR = "dragBar";
	
	static final private int kDragArrow = 5;
	static final private int kMaxHitSlop = 7;
	static final private int kCountGap = 5;
	static final private int kMinHalfBarWidth = 4;
	static final private int kMaxHalfBarWidth = 8;
	
	private String yKey;
	private HorizAxis valAxis;
	private VertAxis countAxis;
	
	private double classCount[];
	
	private boolean[] selectedBars = null;
	
	private boolean initialised = false;
	
	private int dragIndex = -1;
	private int hitOffset;
	
	public DragBarView(DataSet theData, XApplet applet,
												String yKey, HorizAxis valAxis, VertAxis countAxis) {
		super(theData, applet, new Insets(2, 5, 0, 5));
		this.yKey = yKey;
		this.valAxis = valAxis;
		this.countAxis = countAxis;
	}
	
	public String getStatus() {
		String s = "";
		for (int i=0 ; i<classCount.length ; i++)
			s += Math.round(classCount[i]) + " ";
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		classCount = new double[st.countTokens()];
		for (int i=0 ; i<classCount.length ; i++)
			classCount[i] = Integer.parseInt(st.nextToken());
		
		clearSelection();
		initialised = true;
		repaint();
	}
	
	private int[] correctCounts() {
		initialise();
		
		CatVariable yVar = (CatVariable)getVariable(yKey);
		return yVar.getCounts();
	}
	
	public boolean[] wrongBars() {
		int[] correctCount = correctCounts();
		boolean isWrong[] = new boolean[classCount.length];
		for (int i=0 ; i<classCount.length ; i++)
			isWrong[i] = (int)Math.round(classCount[i]) != correctCount[i];
		return isWrong;
	}
	
	public void setCorrectCounts() {
		int[] correctCount = correctCounts();
		for (int i=0 ; i<classCount.length ; i++)
			classCount[i] = correctCount[i];
	}
	
	public void setSelectedBars(boolean[] selectedBars) {
		this.selectedBars = selectedBars;
	}
	
	public void clearSelection() {
		selectedBars = null;
	}

//-------------------------------------------------------------------
	
	final public void initialise() {
		if (!initialised) {
			initialised = true;
			doInitialisation();
		}
	}
	
	protected void doInitialisation() {
		CatVariable yVar = (CatVariable)getVariable(yKey);
		int noOfClasses = yVar.noOfCategories();
		int nValues = yVar.noOfValues();
		
		classCount = new double[noOfClasses];
		
		int startCount = nValues / noOfClasses;
		for (int i=0 ; i<noOfClasses ; i++)
			classCount[i] = startCount;
	}

//-------------------------------------------------------------------
	
	private void drawBar(Graphics g, Point pTop, int halfWidth, int barIndex) {
		int width = 2 * halfWidth;
		int height = getSize().height - pTop.y;
		if (height > 1) {
			g.setColor(selectedBars != null && selectedBars[barIndex] ? Color.yellow : Color.lightGray);
			g.fillRect(pTop.x - halfWidth, pTop.y, width, height);
			g.setColor(Color.black);
			g.drawRect(pTop.x - halfWidth, pTop.y, width, height);
		}
		else if (selectedBars != null && selectedBars[barIndex]) {
			g.setColor(Color.yellow);
			g.fillOval(pTop.x - 8, pTop.y - kDragArrow - 18, 16, 16);
		}
		
		if (dragIndex < 0 || dragIndex == barIndex) {
			g.setColor(Color.red);
			
			g.drawLine(pTop.x, pTop.y - kDragArrow, pTop.x, pTop.y + kDragArrow);
			if (dragIndex == barIndex) {
				g.drawLine(0, pTop.y, pTop.x + width, pTop.y);
				g.drawLine(0, pTop.y, kDragArrow, pTop.y - kDragArrow);
				g.drawLine(0, pTop.y, kDragArrow, pTop.y + kDragArrow);
				
				g.drawLine(pTop.x - 1, pTop.y - kDragArrow + 1, pTop.x - 1, pTop.y + kDragArrow - 1);
				g.drawLine(pTop.x + 1, pTop.y - kDragArrow + 1, pTop.x + 1, pTop.y + kDragArrow - 1);
				for (int i=2 ; i<4 ; i++) {
					g.drawLine(pTop.x - i, pTop.y - kDragArrow + i, pTop.x + i, pTop.y - kDragArrow + i);
					g.drawLine(pTop.x - i, pTop.y + kDragArrow - i, pTop.x + i, pTop.y + kDragArrow - i);
				}
			}
			else {
				g.drawLine(pTop.x, pTop.y - kDragArrow, pTop.x + 3, pTop.y - kDragArrow + 3);
				g.drawLine(pTop.x, pTop.y - kDragArrow, pTop.x - 3, pTop.y - kDragArrow + 3);
				g.drawLine(pTop.x, pTop.y + kDragArrow, pTop.x + 3, pTop.y + kDragArrow - 3);
				g.drawLine(pTop.x, pTop.y + kDragArrow, pTop.x - 3, pTop.y + kDragArrow - 3);
			}
		}
	}
	
	private int getHalfBarWidth() {
		int nCats = classCount.length;
		return Math.min(kMaxHalfBarWidth, Math.max(kMinHalfBarWidth, getSize().width / (6 * nCats)));
	}
	
	public void paintView(Graphics g) {
//		System.out.print("when painting, initialised = " + initialised);
//		System.out.println(", selection = " + ((selectedBars == null) ? "null" : selectedBars[0] + " " + selectedBars[1] + " " + selectedBars[2] + " " + selectedBars[3]));
		initialise();
//		System.out.print("after paint-initialise, initialised = " + initialised);
//		System.out.println(", selection = " + ((selectedBars == null) ? "null" : selectedBars[0] + " " + selectedBars[1] + " " + selectedBars[2] + " " + selectedBars[3]));
		
		Point p = null;
		
		NumValue countVal = new NumValue(0.0, 0);
//		int ascent = g.getFontMetrics().getAscent();
		int halfBarWidth = getHalfBarWidth();
		
		for (int i=0 ; i<classCount.length ; i++) {
			int barHt = countAxis.numValToRawPosition(classCount[i]);
			int xPos = valAxis.catValToPosition(i);			
			p = translateToScreen(xPos, barHt, p);
			
			drawBar(g, p, halfBarWidth, i);
			
			if (dragIndex < 0 && classCount[i] > 0) {
				g.setColor(Color.gray);
				
				double count = Math.rint(classCount[i]);
				countVal.setValue(count);
				
				int baseline = p.y - kCountGap;
				countVal.drawCentred(g, p.x, baseline);
			}
		}
		
		if (dragIndex >= 0) {
			int barHt = countAxis.numValToRawPosition(classCount[dragIndex]);
			int xPos = valAxis.catValToPosition(dragIndex);
			p = translateToScreen(xPos, barHt, p);

			g.setColor(Color.red);
			g.setFont(getApplet().getStandardBoldFont());
			
			countVal.setValue(classCount[dragIndex]);
			int baseline = p.y - kCountGap;
			countVal.drawCentred(g, p.x, baseline);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(yKey)) {
			initialised = false;
			selectedBars = null;
			repaint();
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		initialise();
		
		Point p = translateFromScreen(x, y, null);
		int halfBarWidth = getHalfBarWidth();
		
		for (int i=0 ; i<classCount.length ; i++) {
			int xPos = valAxis.catValToPosition(i);
			if (Math.abs(p.x - xPos) < halfBarWidth + kMaxHitSlop) {
				int barHt = countAxis.numValToRawPosition(classCount[i]);
				
				if (Math.abs(p.y - barHt) <= kMaxHitSlop)
					return new VertDragPosInfo(y, i, p.y - barHt);
				
				break;
			}
		}
		return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
//		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
		if (x < 0 || x >= getSize().width)
			return null;
		
		return new VertDragPosInfo(y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		VertDragPosInfo dragPos = (VertDragPosInfo)startInfo;
		dragIndex = dragPos.index;
		hitOffset = dragPos.hitOffset;
		
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			Point p = translateFromScreen(0, dragPos.y, null);
			p.y -= hitOffset;
			
			try {
				int correctCount = correctCounts()[dragIndex];
				double trialCount = countAxis.positionToNumVal(p.y);
				
				int correctPos = countAxis.numValToRawPosition(correctCount);
				
				classCount[dragIndex] = (p.y == correctPos) ? correctCount : trialCount;
			} catch (AxisException e) {
			}
			repaint();
			
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		classCount[dragIndex] = Math.rint(classCount[dragIndex]);
		
		dragIndex = -1;
		repaint();
	}
	
}
	
