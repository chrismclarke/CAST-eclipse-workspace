package pageStructure;

import java.io.*;
import javax.swing.*;


public class TitleDrawer extends CoreDrawer {
	private String titleHtml;
	private boolean chapterNotModule;
	private File coreDir;
	
	public TitleDrawer(String titleHtml, boolean chapterNotModule, File coreDir) {
		this.titleHtml = titleHtml;
		this.chapterNotModule = chapterNotModule;
		this.coreDir = coreDir;
	}
	
	
	public JPanel createPanel() {
		return new ChapterTitle(titleHtml, chapterNotModule, coreDir);
	}
}
