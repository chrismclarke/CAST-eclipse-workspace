package cast.bookEditor;

import java.awt.*;


public class NewPanel extends CoreContentsPanel {
	
	public NewPanel() {
		setLayout(new GridLayout(0, 4, 10, 4));
//		setOpaque(false);
		setBackground(BookEditor.kHeadingBackground);
		
		add(new NewTitle(NewTitle.NEW_PART, "New Part"));
		add(new NewTitle(NewTitle.NEW_CHAPTER, "New Chapter"));
		add(new NewTitle(NewTitle.NEW_SECTION, "New Section"));
		add(new NewTitle(NewTitle.NEW_PAGE, "New Page"));
	}
	
	public Insets getInsets() {
		return new Insets(3, 10, 3, 10);
	}
}
