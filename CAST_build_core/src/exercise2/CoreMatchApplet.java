package exercise2;

import java.awt.*;

import dataView.*;
import random.*;


abstract public class CoreMatchApplet extends ExerciseApplet {
	
	private XPanel workingPanel;
	private int[] leftOrder, rightOrder;
	private XPanel leftPanel, rightPanel;
	
	private boolean correct[] = null;
	
	private double arrowPropn = 0.333333;	//	proportion from bottom of item rect for arrow centre
	
	abstract protected int noOfItems();
	abstract protected int getDragMatchHeight();
	abstract protected void setWorkingPanelLayout(XPanel thePanel);
	abstract protected XPanel addLeftItems(XPanel thePanel, int[] leftOrder);
	abstract protected XPanel addRightItems(XPanel thePanel, int[] rightOrder);
	abstract protected boolean retainFirstItems();
	
	public void setArrowPropn(double p) {
		arrowPropn = p;
	}
	
	private int[] initialOrdering() {
		int nItems = noOfItems();
		int order[] = new int[nItems];
		for (int i=0 ; i<nItems ; i++)
			order[i] = i;
		return order;
	}
	
	private void randomiseOrdering(int order[]) {
		int nRand = retainFirstItems() ? (order.length - 1) : order.length;
		RandomInteger rand = new RandomInteger(0, nRand - 1, 1, nextSeed());
		for (int i=0 ; i<nRand ; i++) {		//	don't change last item (identity)
			int j = rand.generateOne();
			if (i != j) {
				int temp = order[i];
				order[i] = order[j];
				order[j] = temp;
			}
		}
	}
	
	protected XPanel getWorkingPanels(DataSet data) {
		workingPanel = new XPanel() {
			private int xPos[] = new int[8];
			private int yPos[] = new int[8];
			public void paintComponent(Graphics g) {
				Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_ON);
				super.paintComponent(g);
				int dotPlotHeight = getDragMatchHeight();
				int arrowBandLeft = leftPanel.getSize().width;
				int arrowBandRight = rightPanel.getLocation().x;
				int left = arrowBandLeft + (arrowBandRight - arrowBandLeft - 14) / 2;
				
				xPos[0] = xPos[6] = xPos[7] = left;
				xPos[1] = xPos[2] = xPos[4] = xPos[5] = left + 5;
				xPos[3] = left + 14;
				
				int nItems = noOfItems();
				int nArrows = retainFirstItems() ? (nItems - 1) : nItems;
				for (int i=0 ; i<nArrows ; i++) {
					g.setColor(correct == null ? Color.blue : correct[i] ? Color.green : Color.red);
					int center = dotPlotHeight - i * dotPlotHeight / nItems - (int)Math.round(arrowPropn * dotPlotHeight / nItems);
					yPos[0] = yPos[1] = yPos[7] = center - 3;
					yPos[2] = center - 9;
					yPos[3] = center;
					yPos[4] = center + 9;
					yPos[5] = yPos[6] = center + 3;
					g.fillPolygon(xPos, yPos, 8);
				}
			}
		};
		
		setWorkingPanelLayout(workingPanel);
			
			leftOrder = initialOrdering();
			rightOrder = initialOrdering();
		
		leftPanel = addLeftItems(workingPanel, leftOrder);
		rightPanel = addRightItems(workingPanel, rightOrder);
		
		workingPanel.lockBackground(getBackground());
		return workingPanel;
	}
	
	private boolean sameOrdering(int[] leftOrder, int[] rightOrder) {
		for (int i=0 ; i<leftOrder.length ; i++)
			if (leftOrder[i] != rightOrder[i])
				return false;
		return true;
	}
	
	protected void setDisplayForQuestion() {
		do {
			randomiseOrdering(leftOrder);
			randomiseOrdering(rightOrder);
		} while (sameOrdering(leftOrder, rightOrder));
		
		workingPanel.repaint();
	}
	
	protected void setDataForQuestion() {
		correct = null;
	}
	
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed) {
			correct = null;
			workingPanel.repaint();
		}
		return changed;
	}
	
	protected int assessAnswer() {
		int nItems = noOfItems();
		for (int i=0 ; i<nItems ; i++)
			if (leftOrder[i] != rightOrder[i])
				return ANS_WRONG;
				
		return ANS_CORRECT;
	}
	
	protected void giveFeedback() {
		int nItems = noOfItems();
		correct = new boolean[nItems];
		for (int i=0 ; i<correct.length ; i++)
			correct[i] = leftOrder[i] == rightOrder[i];
		workingPanel.repaint();
	}
	
	protected void showCorrectWorking() {
		int nItems = noOfItems();
		correct = new boolean[nItems];
		for (int i=0 ; i<correct.length ; i++) {
			rightOrder[i] = leftOrder[i];
			correct[i] = true;
		}
		workingPanel.repaint();
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
}