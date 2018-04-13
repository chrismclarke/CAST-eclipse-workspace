package cast.index;

import java.awt.*;

import cast.bookManager.*;
import cast.utils.*;


abstract public class CoreBookProcessor extends Panel implements Runnable {
	
	protected BookReader theBook;
	protected CastEbook castEbook;
	
	protected Thread runner = null;
	
	public CoreBookProcessor(BookReader theBook, CastEbook castEbook) {
		this.theBook = theBook;
		this.castEbook = castEbook;
		
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
		setBackground(Color.white);
		
		addUiControls();
	}
	
	abstract protected void addUiControls();
	abstract public void run();
	
	public Insets getInsets() {
		return new Insets(3, 5, 5, 5);
	}
	
	public String getFileAsString(String sectionDir, String filePrefix) {
		return HtmlHelper.getFileAsString(sectionDir,  filePrefix, castEbook);
	}
	
	@SuppressWarnings("deprecation")
	public void doIndex() {
		if (runner != null) {
			runner.stop();
			runner = null;
		}
		runner = new Thread(this);
		runner.start();
	}
	
	protected void processBookTree() {
		BookTree bookTree = theBook.getBookTree();
		processBook(bookTree);
		for (int i=0 ; i<bookTree.children.length ; i++) {
			BookTree chapterTree = bookTree.children[i];
			if (chapterTree.dir == null)
				processPart(chapterTree);
			else
				processChapter(chapterTree);
			
			if (chapterTree.children != null)
				for (int j=0 ; j<chapterTree.children.length ; j++) {
					BookTree sectionTree = chapterTree.children[j];
					processSection(sectionTree);
					
					if (sectionTree.children != null)
						for (int k=0 ; k<sectionTree.children.length ; k++) {
							BookTree pageTree = sectionTree.children[k];
							processPage(pageTree);
						}
				}
		}
	}
	
	abstract void processBook(BookTree bookTree);
	abstract void processPart(BookTree partTree);
	abstract void processChapter(BookTree chapterTree);
	abstract void processSection(BookTree sectionTree);
	abstract void processPage(BookTree pageTree);
}
