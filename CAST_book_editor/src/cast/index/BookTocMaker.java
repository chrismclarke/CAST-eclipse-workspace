package cast.index;

import java.awt.*;
import java.io.*;
import java.util.regex.*;

import cast.bookManager.*;
import cast.utils.*;


public class BookTocMaker extends CoreBookProcessor {
	
	//******************************************************
	//	Not used any more
	//******************************************************
	
	static final private String kTitleStart = "<title>";
	static final private String kTitleEnd = "</title>";
	
	private Label finishedLabel;
	private Checkbox pageCheck;
	
	private int sectionIndex, chapterIndex;
	private boolean addPageInfo = false;
	
	protected BookTocMaker(BookReader theBook, CastEbook castEbook) {
		super(theBook, castEbook);
	}
	
	protected String getBuildName() {
		return "Make TOC";
	}
	
	protected void addUiControls() {
		finishedLabel = new Label("", Label.LEFT);
		add(finishedLabel);
	}
	
	protected void addButtonExtras(Panel buttonPanel) {
		pageCheck = new Checkbox("+ pg");
		buttonPanel.add(pageCheck);
	}
	
	public void run() {
//		theBook.setupBook();
		File tocOutputFile = castEbook.getTocFile();
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(tocOutputFile));
			tocWriter = new PrintWriter(bw);
			
			chapterIndex = -1;
			addPageInfo = pageCheck.getState();
			
			writeTOCStart();
			processBookTree();
			writeTOCEnd();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
			
		tocWriter = null;
		
		finishedLabel.setText("Finished");
	}
	

	protected void processBook(BookTree bookTree) {
		processChapter(bookTree);
	}
	
	
	protected void processPart(BookTree partTree) {	//	ignore any parts in ToC
	}
	
	
	protected void processChapter(BookTree chapterTree) {
		String chapterTitle = readTitle(chapterTree.dir, chapterTree.filePrefix);
		if (chapterIndex >= 0)
			writeTOCChapter(chapterIndex + ". " + chapterTitle);
		chapterIndex ++;
		sectionIndex = 1;
	}
	
	protected void processSection(BookTree sectionTree) {
		coreProcessSection(sectionTree.dir, sectionTree.filePrefix, sectionIndex);
		sectionIndex ++;
	}
	
	private void coreProcessSection(String dir, String filePrefix, int sectionIndex) {
		String fileAsString = HtmlHelper.getFileAsString(dir, filePrefix, castEbook);
		String sectionTitle = HtmlHelper.getTagInFile(fileAsString, "title");
		
		sectionTitle = XmlHelper.convertHtmlToRtf(sectionTitle);
		writeTOCSection(sectionIndex + ". " + sectionTitle);
		
		Pattern pagePattern = Pattern.compile("<dt[^>]*><a href=[^>]*>[. \\d]*([^\\n\\r]*)</a>[^<]*(?:<span[^>]*>)?([^<]*)(?:</span[^>]*>)?[^<]*</dt>[^<]*<dd[^>]*>([^<]*)</dd>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher pageMatcher = pagePattern.matcher(fileAsString);
		
		int pageIndex = 1;
		while (pageMatcher.find()) {
			String pageTitle = pageMatcher.group(1);
			String note = pageMatcher.group(2);
			String pageInfo = pageMatcher.group(3);
			if (note != null) {
				while (note.length() > 0 && note.charAt(0) == ' ')
					note = note.substring(1);
				if (note.length() > 0 && note.charAt(0) != '(')
					note = "(" + note + ")";
				if (note.length() > 0)
					pageTitle += "  " + note;
			}
			
			pageInfo = stripTags(pageInfo);
			
			pageTitle = XmlHelper.convertHtmlToRtf(pageIndex + ". " + pageTitle);
			pageInfo = XmlHelper.convertHtmlToRtf(pageInfo);
			
			writeTOCPage(pageTitle, pageInfo);
			pageIndex ++;
		}
	}
	
	private String stripTags(String s) {
		while (true) {
			int tagStart = s.indexOf('<');
			if (tagStart < 0)
				break;
			int tagEnd = s.indexOf('>', tagStart);
			if (tagEnd < 0)
				tagEnd = s.length();
			else
				tagEnd ++;
			s = s.substring(0, tagStart) + s.substring(tagEnd, s.length());
		}
		return s;
	}
	
/*
	private String joinStrings(String s1, String s2) {
		if (s1.length() > 0)
			s1 += " ";
		int i1 = 0;
		while (i1 < s2.length() && (s2.charAt(i1) == ' ' || s2.charAt(i1) == '\t'))
			i1++;
		int i2 = s2.length();
		while (i2 > i1 && (s2.charAt(i2 - 1) == ' ' || s2.charAt(i2 - 1) == '\t'))
			i2--;
		return s1 + s2.substring(i1, i2);
	}
*/
	
	protected void processPage(BookTree pageTree) {
	}
	
	private String readTitle(String dir, String filePrefix) {
		try {
			File fileObject = castEbook.getPageHtmlFile(dir, filePrefix);
			BufferedReader reader = new BufferedReader(new FileReader(fileObject));
			
			String s;
			while ((s = reader.readLine()) != null) {
				int titleStart = s.indexOf(kTitleStart);
				int titleEnd = s.indexOf(kTitleEnd);
				if (titleStart >= 0 && titleEnd > titleStart) {
					reader.close();
					return s.substring(titleStart + kTitleStart.length(), titleEnd);
				}
			}
			reader.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return "Unknown";
	}
	
	
	private PrintWriter tocWriter;
	
	private void writeTOCStart() {
		if (tocWriter != null) {
			tocWriter.println("{\\rtf1\\mac \\deff0");
			tocWriter.println("");
			tocWriter.println("{\\upr{\\fonttbl{\\f0\\fnil\\fcharset256 Times New Roman;}}");
			tocWriter.println("{\\*\\ud{\\fonttbl{\\f0\\fnil\\fcharset256 Times New Roman;}}}}");
			tocWriter.println("");
			tocWriter.println("{\\stylesheet");
			tocWriter.println("{\\li227 \\f0 Normal;}");
			tocWriter.println("{\\s1\\sb240\\sa20 \\b\\f0\\fs32 heading 1;}");
			tocWriter.println("{\\s2\\sb120\\sa60 \\b\\i\\f0\\fs28 heading 2;}");
			tocWriter.println("{\\s17\\li600 \\v\\f0 Page description;}");
			tocWriter.println("}");
			tocWriter.println("");
			
			tocWriter.println("\\pard\\plain \\b\\f0\\fs48");
			tocWriter.println("{" + theBook.getTOCTitle() + "\\par }\n");
			
		}
	}
	
	private void writeTOCEnd() {
		if (tocWriter != null) {
			tocWriter.println("}");
			
			tocWriter.flush();
			tocWriter.close();
		}
	}
	
	private void writeTOCChapter(String title) {
		if (tocWriter != null) {
			tocWriter.println("\\pard\\plain \\s1\\sb240\\sa20 \\b\\f0\\fs32");
			tocWriter.println("{" + title + "\\par }\n");
		}
	}
	
	private void writeTOCSection(String title) {
		if (tocWriter != null) {
			tocWriter.println("\\pard\\plain \\s2\\sb120\\sa60 \\b\\i\\f0\\fs28");
			tocWriter.println("{" + title + "\\par }\n");
		}
	}
	
	private void writeTOCPage(String title, String description) {
		if (tocWriter != null) {
			tocWriter.println("\\pard\\plain \\li227 \\f0");
			tocWriter.println("{" + title + "\\par }");
			if (addPageInfo && description != null) {
				tocWriter.println("\\pard\\plain \\s17\\li600 \\v\\f0");
				tocWriter.println("{" + description + "\\par }\n");
			}
		}
	}
}
