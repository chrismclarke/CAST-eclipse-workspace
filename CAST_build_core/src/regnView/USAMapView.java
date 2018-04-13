package regnView;

import java.awt.*;

import dataView.*;


public class USAMapView extends DataView {
//	static public final String USA_MAP = "usaMap";
	
	static private final int kMapHeight = 170;
	static private final int kMapWidth = 250;
	
	static private final int kMinLong = 65;
	static private final int kMaxLong = 130;
	static private final int kMinLat = 22;
	static private final int kMaxLat = 50;
	
	static private final int kMinHitDist = 16;
	
	static private final int kLowXPos = 4;
	static private final int kHighXPos = kMapWidth - 5;
	static private final int kLowYPos = 4;
	static private final int kHighYPos = kMapHeight - 5;
	
	static final private double mapPos[][] = {{49, 123.1}, {49, 95}, {48, 90}, {46.8, 92.2},
					{46.5, 84.4}, {45.7, 87.1}, {41.7, 87.7}, {43, 85.7}, {45, 86}, {46, 85},
					{45, 83.5}, {43, 81.3}, {41.7, 83.6}, {41.5, 81.7}, {43, 79}, {45, 75},
					{45, 72}, {47.5, 69.2}, {47.4, 68.3}, {45, 67}, {42.3, 71}, {41.8, 70},
					{40.7, 74.2}, {39.4, 74.4}, {36.9, 76.3}, {35.2, 75.5}, {32, 81},
					{29.9, 81.3}, {26.7, 80}, {25.3, 80.3}, {25.3, 81.3}, {27.8, 82.7},
					{29, 83}, {30.1, 84}, {29.7, 85}, {30.5, 87.2}, {30.5, 89}, {28.7, 89},
					{30, 94}, {28, 97.2}, {26, 97.4}, {26, 99}, {29.3, 100.8}, {30, 102.2},
					{29, 103.2}, {29.5, 104.3}, {31.8, 106.5}, {31.8, 108}, {31.5, 108},
					{31.5, 111}, {32.8, 114.8}, {32.5, 117.2}, {34, 118.1}, {34.2, 121},
					{37.9, 122.5}, {40.5, 124.2}, {48.3, 124.8}, {48, 122.2}, {49, 123.1}};
	static private int mapCoord[][] = null;
	
	private String longKey, latKey;
	
	private int alaskaX[] = {2, 8, 6, 9, 7, 4, 2, 2};
	private int alaskaY[] = {2, 2, 4, 7, 9, 6, 8, 2};
	
	private int hawaiiX[] = {2, 8, 6, 9, 7, 4, 2, 2};
	private int hawaiiY[] = {kMapHeight - 2, kMapHeight - 2, kMapHeight - 4, kMapHeight - 7,
											kMapHeight - 9, kMapHeight - 6, kMapHeight - 8, kMapHeight - 2};
	
	private int sanJuanX[] = {kMapWidth - 3, kMapWidth - 6, kMapWidth - 7, kMapWidth - 10,
							kMapWidth - 8, kMapWidth - 8, kMapWidth - 5, kMapWidth - 5, kMapWidth - 3};
	private int sanJuanY[] = {kMapHeight - 6, kMapHeight - 3, kMapHeight - 3, kMapHeight - 6,
					kMapHeight - 6, kMapHeight - 10, kMapHeight - 10, kMapHeight - 6, kMapHeight - 6};
	
	private Point cityCoord[] = null;
	
	static final private Color kMapFillColor = new Color(0x3399FF);
	static final private Color kMapEdgeColor = new Color(0x0000FF);
	
	public USAMapView(DataSet theData, XApplet applet, String longKey, String latKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.longKey = longKey;
		this.latKey = latKey;
		setRetainLastSelection(true);
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(kMapWidth, kMapHeight);
	}
	
	private void checkCoords() {
		if (mapCoord == null) {
			mapCoord = new int[2][];
			mapCoord[0] = new int[mapPos.length];
			mapCoord[1] = new int[mapPos.length];
			for (int i=0 ; i<mapPos.length ; i++) {
				mapCoord[0][i] = (int)Math.round((kMaxLong - mapPos[i][1]) / (kMaxLong - kMinLong)
																										* kMapWidth);
				mapCoord[1][i] = (int)Math.round((kMaxLat - mapPos[i][0]) / (kMaxLat - kMinLat)
																										* kMapHeight);
			}
		}
		if (cityCoord == null) {
			int cityIndex = 0;
			NumVariable xVariable = (NumVariable)getVariable(longKey);
			NumVariable yVariable = (NumVariable)getVariable(latKey);
			cityCoord = new Point[xVariable.noOfValues()];
			ValueEnumeration xe = xVariable.values();
			ValueEnumeration ye = yVariable.values();
			while (xe.hasMoreValues() && ye.hasMoreValues()) {
				double longVal = xe.nextDouble();
				double latVal = ye.nextDouble();
				if (Double.isNaN(longVal) || Double.isNaN(latVal))
					cityCoord[cityIndex] = null;
				else {
					int horiz = (int)Math.round((kMaxLong - longVal) / (kMaxLong - kMinLong)
																											* kMapWidth);
					horiz = Math.max(kLowXPos, Math.min(kHighXPos, horiz));
					int vert = (int)Math.round((kMaxLat - latVal) / (kMaxLat - kMinLat)
																										* kMapHeight);
					vert = Math.max(kLowYPos, Math.min(kHighYPos, vert));
					cityCoord[cityIndex] = new Point(horiz, vert);
				}
				cityIndex ++;
			}
		}
	}
	
	public void paintView(Graphics g) {
		checkCoords();
		g.setColor(kMapFillColor);
		g.fillPolygon(mapCoord[0], mapCoord[1], mapCoord[0].length);
		g.setColor(kMapEdgeColor);
		g.drawPolygon(mapCoord[0], mapCoord[1], mapCoord[0].length);
		g.setColor(getForeground());
		
		ValueEnumeration xe = ((NumVariable)getVariable(longKey)).values();
		FlagEnumeration fe = getSelection().getEnumeration();
		int cityIndex = 0;
		while (xe.hasMoreValues()) {
			xe.nextValue();
			boolean nextSel = fe.nextFlag();
			Point p = cityCoord[cityIndex];
			if (p != null) {
				if (p.x != kLowXPos && p.x != kHighXPos && p.y != kLowYPos && p.y != kHighYPos) {
					if (nextSel) {
						g.setColor(Color.red);
						drawBlob(g, p);
						g.setColor(getForeground());
					}
					else
						drawCross(g, p);
				}
				else if (nextSel) {
					g.setColor(Color.red);
					if (p.y == kLowYPos && p.x == kLowXPos)
						g.fillPolygon(alaskaX, alaskaY, alaskaX.length);
					else if (p.y == kHighYPos && p.x == kLowXPos)
						g.fillPolygon(hawaiiX, hawaiiY, hawaiiX.length);
					else if (p.y == kHighYPos)
						g.fillPolygon(sanJuanX, sanJuanY, sanJuanX.length);
					g.setColor(getForeground());
				}
				else {
					if (p.y == kLowYPos && p.x == kLowXPos) {
						g.drawLine(2, 2, 7, 2);
						g.drawLine(2, 2, 2, 7);
						g.drawLine(2, 2, 7, 7);
					}
					else if (p.y == kHighYPos && p.x == kLowXPos) {
						g.drawLine(2, kMapHeight - 3, 7, kMapHeight - 3);
						g.drawLine(2, kMapHeight - 3, 2, kMapHeight - 8);
						g.drawLine(2, kMapHeight - 3, 7, kMapHeight - 8);
					}
					else if (p.y == kHighYPos) {
						g.drawLine(kMapWidth - 6, kMapHeight - 3, kMapWidth - 6, kMapHeight - 9);
						g.drawLine(kMapWidth - 6, kMapHeight - 3, kMapWidth - 3, kMapHeight - 6);
						g.drawLine(kMapWidth - 6, kMapHeight - 3, kMapWidth - 9, kMapHeight - 6);
					}
				}
			}
			cityIndex++;
		}
	}
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		checkCoords();
		
		int minIndex = -1;
		int minDist = 0;
		boolean gotPoint = false;
		for (int i=0 ; i<cityCoord.length ; i++)
			if (cityCoord[i] != null) {
				int xDist = cityCoord[i].x - x;
				int yDist = cityCoord[i].y - y;
				int dist = xDist*xDist + yDist*yDist;
				if (!gotPoint) {
					gotPoint = true;
					minIndex = i;
					minDist = dist;
				}
				else if (dist < minDist) {
					minIndex = i;
					minDist = dist;
				}
		}
		if (gotPoint && minDist < kMinHitDist)
			return new IndexPosInfo(minIndex);
		else
			return null;
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(longKey) || key.equals(latKey)) {
			cityCoord = null;
			super.doChangeVariable(g, key);
		}
	}
}
	
