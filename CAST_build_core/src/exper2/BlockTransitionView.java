package exper2;

import java.awt.*;
import java.util.*;

import dataView.*;


public class BlockTransitionView extends XPanel {
//	static final private int kCellTopBottomBorder = 4;
//	static final private int kCellLeftRightBorder = 8;
	static final private int kHeadingGap = 5;
	
	static final private Color kLevelColor[] = {Color.black, Color.red, Color.green, Color.blue, Color.magenta};
	
	private int[] currentLevels;
	private int currentStep = 0;
	
	private LabelValue[] blockNames;
	private LabelValue[] factorNames;
	
	private int nMoves;
	private int[] moveTo;
	private int[] moveFrom;
	
	private Font factorFont;
	
	public BlockTransitionView(int[] startLevels, int[] endLevels, LabelValue[] blockNames,
																																	LabelValue[] factorNames) {
		this.blockNames = blockNames;
		this.factorNames = factorNames;
		int n = startLevels.length;
		
		nMoves = 0;
		moveTo = new int[n];
		moveFrom = new int[n];
		
		boolean error[] = new boolean[n];
		int nErrors = 0;
		for (int i=0 ; i<n ; i++) {
			boolean wrong = startLevels[i] != endLevels[i];
			if (wrong)
				nErrors ++;
			error[i] = wrong;
		}
		Random rand = new Random();
			
		currentLevels = (int[])startLevels.clone();
		int targets[] = new int[n];
		
		while (nErrors > 0) {
			int errorIndex = rand.nextInt(nErrors);
			int indexToMove = 0;
			for (int i=0 ; i<n ; i++)
				if (error[i]) {
					if (errorIndex == 0) {
						indexToMove = i;
						break;
					}
					else
						errorIndex --;
				}
			int levelToMove = currentLevels[indexToMove];
			
			int nTargets = 0;
			for (int i=0 ; i<n ; i++)
				if (endLevels[i] == levelToMove && currentLevels[i] != levelToMove)
					targets[nTargets ++] = i;
			
			int targetIndex = targets[rand.nextInt(nTargets)];
			
			int temp = currentLevels[targetIndex];
			currentLevels[targetIndex] = currentLevels[indexToMove];
			currentLevels[indexToMove] = temp;
			error[targetIndex] = false;
			nErrors --;
			
			if (currentLevels[indexToMove] == endLevels[indexToMove]) {
				error[indexToMove] = false;
				nErrors --;
			}
			
			moveFrom[nMoves] = indexToMove;
			moveTo[nMoves ++] = targetIndex;
		}
		currentLevels = (int[])startLevels.clone();
	}
	
	public void setFactorFont(Font f) {
		factorFont = f;
	}
	
	public int noOfSteps() {
		return nMoves;
	}
	
	public void showStep(int step) {
		if (step > currentStep)
			for (int i=currentStep ; i<step ; i++) {
				int temp = currentLevels[moveTo[i]];
				currentLevels[moveTo[i]] = currentLevels[moveFrom[i]];
				currentLevels[moveFrom[i]] = temp;
			}
		else if (step < currentStep)
			for (int i=currentStep-1 ; i>=step ; i--) {
				int temp = currentLevels[moveTo[i]];
				currentLevels[moveTo[i]] = currentLevels[moveFrom[i]];
				currentLevels[moveFrom[i]] = temp;
			}
		currentStep = step;
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		
		int tableTop = ascent + descent + kHeadingGap;
		
		g.setColor(Color.white);
		g.fillRect(0, tableTop, getSize().width, getSize().height - tableTop);
		
		int nBlocks = blockNames.length;
		int nLevels = factorNames.length;
		int colLeft = 0;
		for (int i=0 ; i<nBlocks ; i++) {
			int colRight = getSize().width * (i + 1) / nBlocks;
			int colCenter = (colLeft + colRight) / 2;
			g.setColor(getForeground());
			g.setFont(getFont());
			blockNames[i].drawCentred(g, colCenter, ascent);
			
			g.setFont(factorFont);
			for (int j=0 ; j<nLevels ; j++) {
				int baseline = tableTop + (getSize().height - tableTop - nLevels * ascent) * (j + 1) / (nLevels + 1)
																																								+ (j + 1) * ascent;
				int level = currentLevels[i * nLevels + j];
				g.setColor(kLevelColor[level % kLevelColor.length]);
				factorNames[level].drawCentred(g, colCenter, baseline);
			}
			
			colLeft = colRight;
		}
	}
	
	public Dimension getMinimumSize() {
		int width = 0;
		int height = 0;
		return new Dimension(width, height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}