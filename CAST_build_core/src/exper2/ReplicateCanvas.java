package exper2;

import java.awt.*;

import dataView.*;
import utils.*;


public class ReplicateCanvas extends XPanel {
	static final private Color kGridColor = new Color(0xBBBBBB);
	
	private XNumberEditPanel[] aFactorEdit;
	private XNumberEditPanel[] bFactorEdit;
	
	public ReplicateCanvas(XNumberEditPanel[] aFactorEdit, XNumberEditPanel[] bFactorEdit) {
		this.aFactorEdit = aFactorEdit;
		this.bFactorEdit = bFactorEdit;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		NumValue tempVal = new NumValue(0, 0);
		
		int na = aFactorEdit.length;
		int nb = bFactorEdit.length;
		
		int cellWidth = getSize().width / (na + 1);
		int cellHeight = getSize().height / (nb + 1);
		
		int horizCenterOffset = cellWidth / 2;
		FontMetrics fm = g.getFontMetrics();
		int baselineOffset = (cellHeight + fm.getAscent()) / 2;
		
		g.setColor(Color.white);
		g.fillRect(0, 0, getSize().width * na / (na + 1), getSize().height * nb / (nb + 1));
		
		g.setColor(kGridColor);
		int right = getSize().width * na / (na + 1);
		for (int i=0 ; i<=nb+1 ; i++) {
			int vert = getSize().height * i / (nb + 1);
			
			g.drawLine(0, vert, right, vert);
		}
		int bottom = getSize().height * nb / (nb + 1);
		for (int i=0 ; i<=na+1 ; i++) {
			int horiz = getSize().width * i / (na + 1);
			
			g.drawLine(horiz, 0, horiz, bottom);
		}
		g.setColor(getForeground());
		
		int aReps[] = new int[na];
		int aTotal = 0;
		int bReps[] = new int[nb];
		int bTotal = 0;
		
		for (int i=0 ; i<na ; i++) {
			aReps[i] = aFactorEdit[i].getIntValue();
			aTotal += aReps[i];
		}
		
		for (int i=0 ; i<nb ; i++) {
			bReps[i] = bFactorEdit[i].getIntValue();
			bTotal += bReps[i];
		}
		
		for (int i=0 ; i<=na ; i++) {
			int aFactor = (i < na) ? aReps[i] : aTotal;
			
			for (int j=0 ; j<=nb ; j++) {
				int bFactor = (j < nb) ? bReps[j] : bTotal;
				
				tempVal.setValue(aFactor * bFactor);
				
				int top = getSize().height * j / (nb + 1);
				int left = getSize().width * i / (na + 1);
				
				tempVal.drawCentred(g, left + horizCenterOffset, top + baselineOffset);
			}
		}
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(50, 50);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}