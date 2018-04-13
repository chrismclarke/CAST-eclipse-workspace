package exerciseEstim;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import utils.*;
import exercise2.*;


abstract public class CoreLookupView extends DataView implements LayoutManager {
	protected HorizAxis axis;
	protected String distnKey;
	protected AccurateDistn2Artist distnDrawer;
	
	protected XNumberEditPanel theEdit;
	
	protected boolean initialised = false;
	
	public CoreLookupView(DataSet data, XApplet applet, HorizAxis axis, String distnKey) {
		super(data, applet, new Insets(0, 0, 0, 0));
		this.axis = axis;
		this.distnKey = distnKey;
		distnDrawer = new AccurateDistn2Artist(distnKey, data);
		distnDrawer.setAreaProportion(0.23);
		
		setLayout(this);
	}
	
	public void setAreaProportion(double areaProportion) {
		distnDrawer.setAreaProportion(areaProportion);
	}
	
	public void setDistnKey(String distnKey) {
		this.distnKey = distnKey;
		distnDrawer.setDistnKey(distnKey);
		initialised = false;
		repaint();
	}
	
	public void setEditValue(NumValue value) {
		theEdit.setDoubleValue(value);
		initialised = false;
		repaint();
	}
	
	public NumValue getEditValue() {
		return theEdit.getNumValue();
	}
	
	public void paint(Graphics g) {
		if (!initialised) {
			setFixedEdit();
			initialised = true;
		}
		super.paint(g);
	}
	
	abstract public void paintView(Graphics g);

//-----------------------------------------------------------------------------------
	
	
	abstract protected void setFixedEdit();
	abstract protected void setPendingEdit();

//-----------------------------------------------------------------------------------

	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(distnKey)) {
			initialised = false;
			distnDrawer.resetDistn();
		}
		super.doChangeVariable(g, key);
	}
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------
	
	
	public void addLayoutComponent(String name, Component comp) {
											//	we already know the names of the components
	}
	
	public void removeLayoutComponent(Component comp) {
	}
	
	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(10, 10);				//	should never be called
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}
	
	abstract public void layoutContainer(Container parent);
	

	
	private boolean localAction(Object target, Object arg) {
		if (target == theEdit) {
			boolean pending = (arg != null) && ((Boolean)arg).booleanValue();
			if (pending)
				setPendingEdit();
			else {
				initialised = false;
				if (getApplet() instanceof ExerciseApplet)
					((ExerciseApplet)getApplet()).noteChangedWorking();
			}
			repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target, evt.arg);
	}
}