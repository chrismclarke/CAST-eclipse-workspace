package cast.bookEditor;

import javax.swing.*;

import cast.bookManager.*;
import cast.utils.*;


public class ChapterContents extends JPanel {
	
	public ChapterContents(DomChapter domChapter, CastEbook castEbook, int chapterIndex) {
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 2));
		setOpaque(false);
		
		add(new ChapterTitle(domChapter, castEbook, chapterIndex));
		
		int nChildren = domChapter.noOfChildren();
		if (nChildren > 0)
			for (int i=0 ; i<nChildren ; i++) {
				DomElement element = domChapter.getChild(i);
				if (element instanceof DomSection)
					add(new SectionTitle((DomSection)element, castEbook, i + 1));
				else
					add(new PageTitle((DomPage)element, castEbook, i + 1));
			}
	}
}
