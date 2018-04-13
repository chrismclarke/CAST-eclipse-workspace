package exper2;

import java.awt.*;
import java.util.*;

import dataView.*;


public class ReplicateTable extends XPanel {
	static final private String kMaxRepString = "999";
	
	static final private Color kTableBorderColor = new Color(0x999999);
	
	static final private int kCellTopBottomBorder = 4;
	static final private int kCellLeftRightBorder = 8;
	static final private int kRowHeadingGap = 5;
	static final private int kColHeadingGap = 2;
	
	private String[] colNames;
	private String[] rowNames = null;
	
	private int[][] reps;
	
	public ReplicateTable(String tableNames, String repString) {
		StringTokenizer st = new StringTokenizer(tableNames, "*");
		
		LabelEnumeration le = new LabelEnumeration(st.nextToken());
		colNames = new String[le.countElements()];
		for (int i=0 ; i<colNames.length ; i++)
			colNames[i] = (String)le.nextElement();
		
		if (st.hasMoreTokens()) {
			rowNames = new String[st.countTokens()];
			for (int i=0 ; i<rowNames.length ; i++)
				rowNames[i] = st.nextToken();
		}
		setReplicates(repString);
	}
	
	public void setReplicates(String repString) {
		StringTokenizer st = new StringTokenizer(repString, "*");
		reps = new int[st.countTokens()][];
		for (int i=0 ; i<reps.length ; i++) {
			StringTokenizer st2 = new StringTokenizer(st.nextToken());
			reps[i] = new int[st2.countTokens()];
			for (int j=0 ; j<reps[i].length ; j++)
				reps[i][j] = Integer.parseInt(st2.nextToken());
		}
	}
	
	private int getMaxWidth(String[] names, String maxReps, Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int maxNameWidth = 0;
		if (names != null)
			for (int i=0 ; i<names.length ; i++)
				maxNameWidth = Math.max(maxNameWidth, fm.stringWidth(names[i]));
		if (maxReps != null)
			maxNameWidth = Math.max(maxNameWidth, fm.stringWidth(maxReps));
		return maxNameWidth;
	}
	
	private int getCellHeight(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		return fm.getAscent() + 2 * kCellTopBottomBorder;
	}
	
	private Point getTableTopLeft(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int top = fm.getAscent() + fm.getDescent() + kColHeadingGap;
		int left = (rowNames == null) ? 0 : (getMaxWidth(rowNames, null, g) + kRowHeadingGap);
		return new Point(left, top);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int cellWidth = getMaxWidth(colNames, kMaxRepString, g) + 2 * kCellLeftRightBorder;
		int cellHeight = getCellHeight(g);
		Point topLeft = getTableTopLeft(g);
		
		int interiorWidth = cellWidth * reps[0].length;
		int interiorHeight = cellHeight * reps.length;
		
		g.setColor(Color.white);
		g.fillRect(topLeft.x, topLeft.y, interiorWidth, interiorHeight);
		g.setColor(kTableBorderColor);
		g.drawRect(topLeft.x, topLeft.y, interiorWidth - 1, interiorHeight - 1);
		g.setColor(getForeground());
		
		FontMetrics fm = g.getFontMetrics();
		int colHeadingBaseline = fm.getAscent();
		LabelValue tempLabel = new LabelValue(null);
		NumValue tempVal = new NumValue(0, 0);
		
		for (int i=0 ; i<colNames.length ; i++) {
			tempLabel.label = colNames[i];
			int center = topLeft.x + i * cellWidth + cellWidth / 2;
			tempLabel.drawCentred(g, center, colHeadingBaseline);
		}
		
		int rowBaseline = topLeft.y + cellHeight - kCellTopBottomBorder;
		for (int i=0 ; i<reps.length ; i++) {
			if (rowNames != null)
				g.drawString(rowNames[i], 0, rowBaseline);
			
			int cellCenter = topLeft.x + cellWidth / 2;
			for (int j=0 ; j<reps[i].length ; j++) {
				tempVal.setValue(reps[i][j]);
				tempVal.drawCentred(g, cellCenter, rowBaseline);
				cellCenter += cellWidth;
			}
			rowBaseline += cellHeight;
		}
	}
	
	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		int cellWidth = getMaxWidth(colNames, kMaxRepString, g) + 2 * kCellLeftRightBorder;
		int cellHeight = getCellHeight(g);
		Point topLeft = getTableTopLeft(g);
		
		int interiorWidth = cellWidth * reps[0].length;
		int interiorHeight = cellHeight * reps.length;
		return new Dimension(topLeft.x + interiorWidth, topLeft.y + interiorHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}