package cast.bookEditor;

import javax.swing.*;


public class CoreContentsPanel extends JPanel {
	static private ElementTitle selectedTitle = null;			//	static so it can clear highlights in other windows
	
	public void select(ElementTitle newSelection) {
		if (selectedTitle != null)
			selectedTitle.doHighlight(false);
		selectedTitle = newSelection;
		if (selectedTitle != null)
			selectedTitle.doHighlight(true);
	}
}
