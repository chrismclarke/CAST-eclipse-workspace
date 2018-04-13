package twoFactor;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import utils.*;
import imageUtils.*;


public class SelectModelDiffView extends XPanel implements Runnable, MouseListener, MouseMotionListener {
	static final private int ADD_X = 0;
	static final private int ADD_Z = 1;
	static final private int ADD_X_AFTER_Z = 2;
	static final private int ADD_Z_AFTER_X = 3;
	static final private int ADD_INTERACTION = 4;
	
	static final private int kMilliSecsPerModel = 1500;
	
	static final private String kLeftArrowGifs[] = {"exper/leftArrowBlue.gif", "exper/leftArrowRed.gif"};
	static final private String kRightArrowGifs[] = {"exper/rightArrowBlue.gif", "exper/rightArrowRed.gif"};
	static final private String kDownArrowGifs[] = {"exper/downArrowBlue.gif", "exper/downArrowRed.gif"};
	static final private int kArrowWidth = 46;
	static final private int kArrowHeight = 46;
	
	static final private Color kHiliteModelBackground = new Color(0xFFFF99);
	
	private DataSet data;
	private String factor1Name, factor2Name;
	private String model1Key, model2Key, yKey;
	private RotateDiffModelsView linkedView;
	private XLabel[] ssqLabel;
	
	private int ssqType = ADD_X;
	
	private boolean showFirstModel = true;
	
	private Thread animationThread = null;
	
	private XLabel noFactorModel, factorXModel, factorZModel, factorXZModel, interactModel;
	private ImageSwapCanvas arrow[];
	
	private Color appletBackgroundColor;
	
	public SelectModelDiffView(DataSet data, XApplet applet, String factor1Name, String factor2Name,
										String model1Key, String model2Key, String yKey, RotateDiffModelsView linkedView,
										XLabel[] ssqLabel, boolean allowInteraction, boolean blockFactorMode) {
		this.data = data;
		this.factor1Name = factor1Name;
		this.factor2Name = factor2Name;
		this.model1Key = model1Key;
		this.model2Key = model2Key;
		this.yKey = yKey;
		this.linkedView = linkedView;
		this.ssqLabel = ssqLabel;
		
		appletBackgroundColor = applet.getBackground();
		arrow = new ImageSwapCanvas[5];
		
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 3));
		
			noFactorModel = new XLabel(applet.translate("Mean only"), XLabel.CENTER, applet);
			noFactorModel.setFont(applet.getBigBoldFont());
		add(noFactorModel);
		
		if (blockFactorMode) {
			add(getDownArrowPanel(applet, ADD_X));
		
					factorXModel = new XLabel(factor1Name, XLabel.CENTER, applet);
					factorXModel.setFont(applet.getBigBoldFont());
			add(factorXModel);
			
			add(getDownArrowPanel(applet, ADD_Z_AFTER_X));
		}
		else {
			add(getArrowPanel(applet, ADD_X, ADD_Z, true));
			
				XPanel oneFactorPanel = new XPanel();
				oneFactorPanel.setLayout(new ProportionLayout(0.5, 40));
					
					factorXModel = new XLabel(factor1Name, XLabel.CENTER, applet);
					factorXModel.setFont(applet.getBigBoldFont());
				oneFactorPanel.add(ProportionLayout.LEFT, factorXModel);
					
					factorZModel = new XLabel(factor2Name, XLabel.CENTER, applet);
					factorZModel.setFont(applet.getBigBoldFont());
				oneFactorPanel.add(ProportionLayout.RIGHT, factorZModel);
			
			add(oneFactorPanel);
				
			add(getArrowPanel(applet, ADD_Z_AFTER_X, ADD_X_AFTER_Z, false));
		}
		
			factorXZModel = new XLabel(factor1Name + "+" + factor2Name, XLabel.CENTER, applet);
			factorXZModel.setFont(applet.getBigBoldFont());
		add(factorXZModel);
		
		if (allowInteraction) {
			add(getDownArrowPanel(applet, ADD_INTERACTION));
			
				interactModel = new XLabel(applet.translate("Model with interaction"), XLabel.CENTER, applet);
				interactModel.setFont(applet.getBigBoldFont());
			add(interactModel);
		}
		
		for (int i=0 ; i<arrow.length ; i++)
			if (arrow[i] != null) {
				arrow[i].addMouseListener(this);
				arrow[i].addMouseMotionListener(this);
			}
		
		setSsq(ssqType, applet);
	}
	
	private XPanel getDownArrowPanel(XApplet applet, int index) {
		XPanel downArrowPanel = new XPanel();
		downArrowPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			arrow[index] = new ImageSwapCanvas(kDownArrowGifs, applet, kArrowWidth, kArrowHeight);
		downArrowPanel.add(arrow[index]);
		return downArrowPanel;
	}
	
	private XPanel getArrowPanel(XApplet applet, int leftIndex, int rightIndex, boolean outNotIn) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			arrow[leftIndex] = new ImageSwapCanvas(outNotIn ? kLeftArrowGifs : kRightArrowGifs,
																					applet, kArrowWidth, kArrowHeight);
		thePanel.add(arrow[leftIndex]);
		
			arrow[rightIndex] = new ImageSwapCanvas(outNotIn ? kRightArrowGifs : kLeftArrowGifs,
																					applet, kArrowWidth, kArrowHeight);
		thePanel.add(arrow[rightIndex]);
		
		return thePanel;
	}

//-----------------------------------------------------------------------------------
	
	private void updateModel(XApplet applet) {
		for (int i=0 ; i<arrow.length ; i++)
			if (arrow[i] != null)
				arrow[i].showVersion(ssqType == i ? 1 : 0);
		
		TwoFactorModel model1 = (TwoFactorModel)data.getVariable(model1Key);
		TwoFactorModel model2 = (TwoFactorModel)data.getVariable(model2Key);
		
		if (ssqType == ADD_INTERACTION) {
			model1.setModelType(TwoFactorModel.FACTOR, TwoFactorModel.FACTOR, false);
			model2.setModelType(TwoFactorModel.FACTOR, TwoFactorModel.FACTOR, true);
		}
		else {
			int xTerm1 = (ssqType == ADD_X || ssqType == ADD_Z || ssqType == ADD_X_AFTER_Z)
																			? TwoFactorModel.NONE : TwoFactorModel.FACTOR;
			int zTerm1 = (ssqType == ADD_X || ssqType == ADD_Z || ssqType == ADD_Z_AFTER_X)
																			? TwoFactorModel.NONE : TwoFactorModel.FACTOR;
			model1.setModelType(xTerm1, zTerm1, false);
			
			int xTerm2 = (ssqType == ADD_X || ssqType == ADD_X_AFTER_Z || ssqType == ADD_Z_AFTER_X)
																			? TwoFactorModel.FACTOR : TwoFactorModel.NONE;
			int zTerm2 = (ssqType == ADD_Z || ssqType == ADD_X_AFTER_Z || ssqType == ADD_Z_AFTER_X)
																			? TwoFactorModel.FACTOR : TwoFactorModel.NONE;
			model2.setModelType(xTerm2, zTerm2, false);
		}
		model1.setLSParams(yKey);
		model2.setLSParams(yKey);
		
		switch (ssqType) {
			case ADD_X:
			case ADD_Z:
				ssqLabel[0].setText("");
				ssqLabel[1].setText("");
				ssqLabel[2].setText(applet.translate("Ssq explained by"));
				ssqLabel[3].setText(ssqType == ADD_X ? factor1Name : factor2Name);
				break;
			case ADD_X_AFTER_Z:
			case ADD_Z_AFTER_X:
				ssqLabel[0].setText(applet.translate("Ssq explained by"));
				ssqLabel[1].setText(ssqType == ADD_X_AFTER_Z ? factor1Name : factor2Name);
				ssqLabel[2].setText(applet.translate("after"));
				ssqLabel[3].setText(ssqType == ADD_Z_AFTER_X ? factor1Name : factor2Name);
				break;
			case ADD_INTERACTION:
				ssqLabel[0].setText("");
				ssqLabel[1].setText("");
				ssqLabel[2].setText(applet.translate("Ssq explained by"));
				ssqLabel[3].setText(applet.translate("Interaction"));
				break;
		}
		data.variableChanged("model1");
	}
	
	private void setSsq(int ssqType, XApplet applet) {
		this.ssqType = ssqType;
		updateModel(applet);
		if (animationThread == null) {
			animationThread = new Thread(this);
			animationThread.start();
		}
	}
	
	@SuppressWarnings("deprecation")
	private void stopAnimation() {
		if (animationThread != null) {
			animationThread.stop();
			animationThread = null;
			
			getModelLabel(0).setForeground(Color.black);
			getModelLabel(1).setForeground(Color.black);
			getModelLabel(0).setBackground(appletBackgroundColor);
			getModelLabel(1).setBackground(appletBackgroundColor);
		}
	}
	
	public void run() {
		try {
			while (true) {
				showFirstModel = !showFirstModel;
				getModelLabel(0).setForeground(showFirstModel ? Color.red : Color.black);
				getModelLabel(1).setForeground(showFirstModel ? Color.black : Color.red);
				getModelLabel(0).setBackground(showFirstModel ? kHiliteModelBackground : appletBackgroundColor);
				getModelLabel(1).setBackground(showFirstModel ? appletBackgroundColor : kHiliteModelBackground);
				
				linkedView.setShowFirstModel(showFirstModel);
				linkedView.repaint();
				Thread.sleep(kMilliSecsPerModel);
			}
		} catch (InterruptedException e) {
			System.out.println("Animation interrupted: " + e);
		}
		animationThread = null;
	}
	
	private XLabel getModelLabel(int i) {
		switch (ssqType) {
			case ADD_X:
				return (i==0) ? noFactorModel : factorXModel;
			case ADD_Z:
				return (i==0) ? noFactorModel : factorZModel;
			case ADD_X_AFTER_Z:
				return (i==0) ? factorZModel : factorXZModel;
			case ADD_Z_AFTER_X:
				return (i==0) ? factorXModel : factorXZModel;
			case ADD_INTERACTION:
				return (i==0) ? factorXZModel : interactModel;
		}
		return null;
	}

//-----------------------------------------------------------------------------------
	
	private int dragSsqType = -1;


	public void mouseClicked(MouseEvent e) {		//		Not used
	}

	public void mousePressed(MouseEvent e) {
		Object hitArrow = e.getSource();
		for (int i=0 ; i<arrow.length ; i++)
			if (hitArrow == arrow[i])
				dragSsqType = i;
		if (dragSsqType != ssqType) {
			stopAnimation();
			arrow[dragSsqType].showVersion(1);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (dragSsqType != -1 && dragSsqType != ssqType) {
			arrow[ssqType].showVersion(0);
			arrow[dragSsqType].showVersion(1);
			ssqType = dragSsqType;
			dragSsqType = -1;
		}
		setSsq(ssqType, getApplet());
	}

	public void mouseEntered(MouseEvent e) {		//		Not used
	}

	public void mouseExited(MouseEvent e) {
		dragSsqType = -1;
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {			//		Not used
	}
	
}
	
