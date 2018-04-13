package propnVenn;

import java.awt.*;

import dataView.*;
import utils.*;


public class PickMarginPanel extends XPanel {
	private MarginConditPanel xMarginLabel, yMarginLabel;
	private AreaContinCoreView theView;
	
	private XChoice marginChoice = null;
	private int currentMarginChoice = 0;
	private JointProbChoice marginProbChoice = null;
	
	public PickMarginPanel(MarginConditPanel xMarginLabel, MarginConditPanel yMarginLabel,
															AreaContinCoreView theView, XChoice marginChoice) {
		this.xMarginLabel = xMarginLabel;
		this.yMarginLabel = yMarginLabel;
		this.theView = theView;
		this.marginChoice = marginChoice;
		setLayout(new BorderLayout(0, 0));
		add("Center", marginChoice);
		
		theView.setJointProbChoice(this);
	}
	
	public PickMarginPanel(MarginConditPanel xMarginLabel, MarginConditPanel yMarginLabel,
														AreaContinCoreView theView, JointProbChoice marginProbChoice) {
		this.xMarginLabel = xMarginLabel;
		this.yMarginLabel = yMarginLabel;
		this.theView = theView;
		this.marginProbChoice = marginProbChoice;
		setLayout(new BorderLayout(0, 0));
		add("Center", marginProbChoice);
		
		theView.setJointProbChoice(this);
	}
	
	public void endAnimation() {
		if (marginChoice != null)
			marginChoice.enable();
		else
			marginProbChoice.endTransition();
	}
	
	private void startAnimation(boolean toYMargin) {
		if (yMarginLabel !=  null)
			yMarginLabel.changeMarginNotCondit(toYMargin);
		if (xMarginLabel !=  null)
			xMarginLabel.changeMarginNotCondit(!toYMargin);
		theView.animateChange(toYMargin);
	}

	
	private boolean localAction(Object target) {
		if (target == marginChoice) {
			if (marginChoice.getSelectedIndex() != currentMarginChoice) {
				currentMarginChoice = marginChoice.getSelectedIndex();
				marginChoice.disable();
				startAnimation(currentMarginChoice == 0);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
	
	@SuppressWarnings("deprecation")
	public boolean handleEvent(Event evt) {
		if (evt.target == marginProbChoice && (evt.id == JointProbChoice.TO_Y_MARGIN
													|| evt.id == JointProbChoice.TO_X_MARGIN)) {
			marginProbChoice.startTransition();
			startAnimation(evt.id == JointProbChoice.TO_Y_MARGIN);
			return true;
		}
		else
			return super.handleEvent(evt);
	}
}
