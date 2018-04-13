package cast.index;

import java.awt.*;
import java.io.*;
import java.util.regex.*;

import cast.bookManager.*;
import cast.utils.*;


public class BookPrintMaker extends CoreBookProcessor {
	static final private int SUMMARY_TYPE = 0;
	static final private int TOC_TYPE = 1;
	
	private Label finishedLabel;
	
	PrintWriter summaryOutputWriter = null, tocOutputWriter = null;
	
	private int pageIndex, sectionIndex, chapterIndex;
	private boolean previousWasExercise;
	
	protected BookPrintMaker(BookReader theBook, CastEbook castEbook) {
		super(theBook, castEbook);
	}
	
	protected String getBuildName() {
		return "Output Book";
	}
	
	protected void addUiControls() {
			finishedLabel = new Label("", Label.LEFT);
		add(finishedLabel);
	}
	
	protected void addButtonExtras(Panel buttonPanel) {
	}
	
	public void run() {
		chapterIndex = -2;
		
		finishedLabel.setText("");
		
		processBookTree();
		
		if (summaryOutputWriter != null)
			endOutputFile(summaryOutputWriter);
			
		if (tocOutputWriter != null)
			endOutputFile(tocOutputWriter);
		
		finishedLabel.setText("Finished");
	}
	
	
	private PrintWriter startOutputFile(String internalBookName, String index, String title, int fileType) {
		try {
			File chapterOutputFile = castEbook.getPrintFile(internalBookName + "_" + index);
			PrintWriter outputWriter = FileFinder.createUTF8Writer(chapterOutputFile);
			
			if (fileType == TOC_TYPE)
				insertInitialTocHtml(outputWriter, index, title);
			else
				insertInitialHtml(outputWriter, index, title);
			
			return outputWriter;
		} catch (IOException e) {
			System.err.println(e.toString());
			return null;
		}
	}
	
	private void insertInitialHtml(PrintWriter outputWriter, String index, String title) {
		outputWriter.println("<!DOCTYPE HTML>");
		outputWriter.println("<html>");
		outputWriter.println("<head>");
		outputWriter.println("  <title>" + index + ". " + title + "</title>");
		outputWriter.println("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		outputWriter.println("  <link rel=\"stylesheet\" href=\"../../../structure/summaryStyles.css\" type=\"text/css\">");
		outputWriter.println("  <link rel=\"stylesheet\" href=\"../../../structure/maths/mathStyles.css\" type=\"text/css\">");
		outputWriter.println("  <link rel=\"stylesheet\" href=\"../../../structure/printStyles.css\" type=\"text/css\">");
		
		outputWriter.println("  <script src=\"../../../structure/videoControls/jquery.js\"></script>");
		outputWriter.println("  <script src=\"../../../structure/maths/theorems.js\"></script>");
		outputWriter.println("  <script src=\"../../../structure/maths/mathJax/MathJax.js?config=TeX-AMS-MML_SVG,statMacros.js\"></script>");
		outputWriter.println("  <script src=\"../../../structure/printFixes.js\"></script>");				//		empty version of writeAppletParams() and shrink images
		
		outputWriter.println("</head>\n");
		outputWriter.println("<body id=\"body\" onLoad=\"showPrintDialog(true)\">");
		
		
		outputWriter.println("<div id='overlay'>");
		outputWriter.println("	<div id='dialogWindow'>");
		outputWriter.println("		<div class='printDialog'>");
		outputWriter.println("			<script type='text/javascript'>");
		outputWriter.println("				document.write(\"<div class='heading'>\" + top.document.title + \"</div>\");");
		outputWriter.println("				if (top.url != null) {");
		outputWriter.println("					document.write(\"<p class='text'>A version of this chapter has already been generated in PDF format and we recommend that it is used for printing. The button below will download and display it.</p>\");");
		outputWriter.println("					document.write(\"<p><button onClick='top.showPdf()'>Show PDF version of chapter</button></p>\");");
		outputWriter.println("					document.write(\"<p class='text'>However downloading could be slow depending on your internet connection. If this is a problem, click the button below to print the chapter without downloading (but perhaps not formatted as well as the PDF version).</p>\");");
		outputWriter.println("					document.write(\"<p><button onClick='top.doPrint()'>Show print dialog</button></p>\");");
		outputWriter.println("					document.write(\"<p class='text'>If you are <strong>not</strong> using the PDF version, the best print results are obtained if the text is reduced in size and printed on  sheets of paper that are smaller than A4. This can be done using your browser's Page Setup command to scale by 71% and then printing on A5 paper.</p>\");");
		outputWriter.println("				}");
		outputWriter.println("				else {");
		outputWriter.println("					document.write(\"<p class='text'>Click the button below to print this chapter.</p>\");");
		outputWriter.println("					document.write(\"<p><button onClick='top.doPrint()'>Show print dialog</button></p>\");");
		outputWriter.println("					document.write(\"<p class='text'>The best print results are obtained if the text is reduced in size and printed on  sheets of paper that are smaller than A4. This can be done using your browser's Page Setup command to scale by 71% and then printing on A5 paper.</p>\");");
		outputWriter.println("				}");
		outputWriter.println("			</script>");
		outputWriter.println("			");
		
		outputWriter.println("			<p class='text'>If you don't want to print now,</p>");
		outputWriter.println("			<p><button onClick='top.showPrintDialog(false)'>Browse formatted chapter</button></p>");
		outputWriter.println("		</div>");
		outputWriter.println("	</div>");
		outputWriter.println("</div>");
	}
	
	
	private void insertInitialTocHtml(PrintWriter outputWriter, String index, String title) {
		outputWriter.println("<html>");
		outputWriter.println("<head>");
		outputWriter.println("<title>" + index + ". " + title + "</title>");
		outputWriter.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		
		outputWriter.println("<link rel=\"stylesheet\" href=\"../../../structure/tocPrintStyles.css\" type=\"text/css\">");
		
		outputWriter.println("<script type='text/javascript'>");
		outputWriter.println("	function toggleDescriptions() {");
		outputWriter.println("		var showNotHide = document.getElementById('descriptionCheck').checked;");
		outputWriter.println("		var descriptions = document.getElementsByTagName('p');");
		outputWriter.println("		for (var i=0 ; i<descriptions.length ; i++)");
		outputWriter.println("			descriptions[i].style.display = showNotHide ? 'block' : 'none';");
		outputWriter.println("	}");
		outputWriter.println("</script>");
		
		outputWriter.println("</head>\n");
		
		outputWriter.println("<body>");
		outputWriter.println("<div style='position:absolute; top:3em; right:5; color:#FF0000; border:solid 1px #FF0000; background-color:#FFFFCC; padding:4px; margin:0px; line-height:0.8em'>");
		outputWriter.println("<input type='checkbox' id='descriptionCheck' checked onChange='toggleDescriptions()'/>Long page<br>descriptions");
		outputWriter.println("</div>");
	}
	
	
	private void endOutputFile(PrintWriter outputWriter) {
		outputWriter.println("</body>");
		outputWriter.println("</html>");
		
		outputWriter.flush();
		outputWriter.close();
	}
	

	protected void processBook(BookTree bookTree) {
		processChapter(bookTree);
	}
	
	protected void processPart(BookTree partTree) {	//	ignore any parts when printing book
	}
	
	protected void processChapter(BookTree chapterTree) {
		String dir = chapterTree.dir;
		String filePrefix = chapterTree.filePrefix;
//		String title = chapterTree.title;
		chapterIndex ++;
		
		if (summaryOutputWriter != null)
			endOutputFile(summaryOutputWriter);
		
		if (tocOutputWriter != null)
			endOutputFile(tocOutputWriter);
		
		if (chapterIndex > 0) {
			String fileString = getFileAsString(dir, filePrefix);
			String chapterName = HtmlHelper.getTagInFile(fileString, "title");
			
			tocOutputWriter = startOutputFile("Chapter_t", String.valueOf(chapterIndex), chapterName, TOC_TYPE);
			tocOutputWriter.println("<h1>Chapter " + chapterIndex + " &nbsp; " + chapterName + "</h1>");
			
			if (theBook.hasSummaries()) {
				summaryOutputWriter = startOutputFile("Chapter_s", String.valueOf(chapterIndex), chapterName, SUMMARY_TYPE);
				summaryOutputWriter.println("<h1 class=\"chapterName\">Chapter " + chapterIndex + " &nbsp; " + chapterName + "</h1>");
			}
		}
		
		sectionIndex = 0;
	}
	
	protected void processSection(BookTree sectionTree) {
		String dir = sectionTree.dir;
		String filePrefix = sectionTree.filePrefix;
		String title = sectionTree.title;
		sectionIndex ++;
		
		if (chapterIndex <= 0)
			return;
		
		String indexString = chapterIndex + "." + sectionIndex;
		finishedLabel.setText(indexString + "  " + title);
		
		insertSectionTocHtml(tocOutputWriter, indexString, title);
		
		if (sectionTree.children == null) {			//	page at section level
			String fileString = getFileAsString(dir, filePrefix);
			fileString = cleanPageHtml(fileString, title, dir);
			fileString = "<h1 class=\"sectionName\">" + indexString + " &nbsp; " + title + "</h1>\n" + fileString;
			if (summaryOutputWriter != null)
				summaryOutputWriter.println(fileString);
		}
		else if (summaryOutputWriter != null)
				insertSectionHtml(summaryOutputWriter, indexString, title, sectionTree);
		
		pageIndex = 0;
		previousWasExercise = false;
	}
	
	private void insertSectionHtml(PrintWriter outputWriter, String indexString, String sectionName, BookTree sectionTree) {
		outputWriter.println("<h1 class=\"sectionName" + (sectionIndex > 1 ? " breakBefore" : "") + "\">" + indexString + " &nbsp; " + sectionName + "</h1>");
		
/*
		int nPages = sectionTree.children.length;
		int nPagesInCol1 = (nPages + 1) / 2;
		
		outputWriter.println("<div class='leftTocCol'>");
		outputWriter.println("<ol class='toc'>");
		for (int i=0 ; i<nPagesInCol1 ; i++) {
			String title = sectionTree.children[i].title.replaceAll("#[r+?]#", "");			//	remove code for red and optional parts of titles
			outputWriter.println("<li>" + title + "</li>");
		}
		outputWriter.println("</ol>");
		outputWriter.println("</div>");
		
		outputWriter.println("<div class='rightTocCol'>");
		outputWriter.println("<ol class='toc' start='" + (nPagesInCol1 + 1) + "'>");
		for (int i=nPagesInCol1 ; i<nPages ; i++) {
			String title = sectionTree.children[i].title.replaceAll("#[r+?]#", "");			//	remove code for red and optional parts of titles
			outputWriter.println("<li>" + title + "</li>");
		}
		outputWriter.println("</ol>");
		outputWriter.println("</div>");
		
		outputWriter.println("<br clear='all'>");
*/
	}
	
	private void insertSectionTocHtml(PrintWriter outputWriter, String indexString, String sectionName) {
		outputWriter.println("<h2>" + indexString + " &nbsp; " + sectionName + "</h2>");
	}
	
	
	protected void processPage(BookTree pageTree) {
		String dir = pageTree.dir;
		String filePrefix = pageTree.filePrefix;
		String title = pageTree.title.replaceAll("#[r+?]#", "");			//	remove code for red and optional parts of titles
		String description = pageTree.description;
		String summaryDir = pageTree.summaryDir;
		String summaryFilePrefix = pageTree.summaryFilePrefix;
		boolean isExercise = filePrefix.startsWith("e_");
		
		int noteIndex = description.indexOf("#?#");
		if (noteIndex >= 0) {
			title = title + " (" + description.substring(0, noteIndex) + ")";
			description = description.substring(noteIndex + 3);
		}
		
		pageIndex ++;
		
		if (tocOutputWriter != null) {
			String indexString = chapterIndex + "." + sectionIndex + "." + pageIndex;
			tocOutputWriter.println("<h3>" + indexString + " &nbsp; " + title + "</h3>");
			tocOutputWriter.println("<p>" + description + "</p>");
		}
		
		if (summaryOutputWriter != null) {
			if (summaryDir == null || summaryFilePrefix == null)
				return;
//			if (summaryDir == null)
//				summaryDir = dir;
			
			String summaryFileString;
			String smallerGap = "";
			if (isExercise) {
				smallerGap = previousWasExercise ? " style='margin-top:2em;'" : "";
				summaryFileString = "<div class='summary_text'><p>Exercises are only available online.</p></div>";
			}
			else {
				summaryFileString = (summaryFilePrefix == null)? getFileAsString(dir, filePrefix) : getFileAsString(summaryDir, summaryFilePrefix);
				summaryFileString = cleanPageHtml(summaryFileString, title, summaryDir);
			}
			summaryOutputWriter.println("<h2 class=\"pageName\"" + smallerGap + ">" + chapterIndex + "." + sectionIndex + "." + pageIndex + " &nbsp; " + title + "</h2>" + summaryFileString);
		}
		previousWasExercise = isExercise;
	}
	
	private String cleanPageHtml(String fileString, String title, String dir) {
		Pattern headerPattern = Pattern.compile("(<!DOCTYPE HTML>)?.*<html>.*<head>.*</head>.*<body>.*<script[^>]*>\\s*writePageStart[^>]*>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher headerMatcher = headerPattern.matcher(fileString);
		fileString = headerMatcher.replaceFirst("");
		
		headerPattern = Pattern.compile("(<!DOCTYPE HTML>)?.*	<html>.*<head>.*</head>.*<body>.*<script[^>]*>\\s*addPageControls[^>]*>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		headerMatcher = headerPattern.matcher(fileString);				//		legacy code for old-style pages. Current pages use writePageStart()
		fileString = headerMatcher.replaceFirst("");
		
		Pattern footerPattern = Pattern.compile("<script[^<>]*>[^<>]*writePageEnd.*</html>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher footerMatcher = footerPattern.matcher(fileString);
		fileString = footerMatcher.replaceFirst("");
		
		footerPattern = Pattern.compile("<script[^<>]*>[^<>]*top.writeNextButton.*</html>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		footerMatcher = footerPattern.matcher(fileString);				//		legacy code for old-style pages.  Current pages use writePageEnd()
		fileString = footerMatcher.replaceFirst("");
		
		Pattern indexPattern = Pattern.compile("<(index|dataset)[^>]*>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher indexMatcher = indexPattern.matcher(fileString);
		fileString = indexMatcher.replaceAll("");
		
		String language = dir.substring(0, 2);
		if (language.equals("bk")) {		//	file in a book directory
			//	for all images, add one "../"
			Pattern imagePattern = Pattern.compile("<img(\\s*class=\"gif\")?\\s*src=(\"|\')", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher imageMatcher = imagePattern.matcher(fileString);
			fileString = imageMatcher.replaceAll("<img$1 src=$2../");
			
			Pattern svgPattern = Pattern.compile("<iframe\\s*class=\"svg\"\\s*src=(\"|\')", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher svgMatcher = svgPattern.matcher(fileString);
			fileString = svgMatcher.replaceAll("<iframe class=\"svg\" src=$1../");
		}
		else {		//	file in a section directory
			//	for image in different directory, add one "../"
			Pattern imagePattern0 = Pattern.compile("<img(\\s*class=\"svgImage\")?\\s*src=(\"|\')\\.\\./([^/]+)/images", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher imageMatcher0 = imagePattern0.matcher(fileString);
			fileString = imageMatcher0.replaceAll("<img$1 src=$2../../../" + language + "/$3/images");
			
			Pattern imagePattern = Pattern.compile("<img(\\s*class=\"gif\")?\\s*src=(\"|\')\\.\\./([^/]+)/images", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher imageMatcher = imagePattern.matcher(fileString);
			fileString = imageMatcher.replaceAll("<img$1 src=$2../../../" + language + "/$3/images");
			
			Pattern svgPattern = Pattern.compile("<iframe\\s*class=\"svg\"\\s*src=(\"|\')\\.\\./([^/]+)/images", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher svgMatcher = svgPattern.matcher(fileString);
			fileString = svgMatcher.replaceAll("<iframe class=\"svg\" src=$1../../../" + language + "/$2/images");
			
			//	for image in same directory, add one "../"
			Pattern imagePattern2 = Pattern.compile("<img(\\s*class=\"svgImage\")?\\s*src=(\"|\')images", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher imageMatcher2 = imagePattern2.matcher(fileString);
			fileString = imageMatcher2.replaceAll("<img$1 src=$2../../../" + dir + "/images");
			
			Pattern imagePattern3 = Pattern.compile("<img(\\s*class=\"gif\")?\\s*src=(\"|\')images", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher imageMatcher3 = imagePattern3.matcher(fileString);
			fileString = imageMatcher3.replaceAll("<img$1 src=$2../../../" + dir + "/images");
			
			Pattern svgPattern2 = Pattern.compile("<iframe\\s*class=\"svg\"\\s*src=(\"|\')images", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher svgMatcher2 = svgPattern2.matcher(fileString);
			fileString = svgMatcher2.replaceAll("<iframe class=\"svg\" src=$1../../../" + dir + "/images");
		}
		
						//	for common images, add one "../"
		Pattern imagePattern3 = Pattern.compile("<img\\s*src=(\"|\')\\.\\./\\.\\./images", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher imageMatcher3 = imagePattern3.matcher(fileString);
		fileString = imageMatcher3.replaceAll("<img src=$1../../../images");
		
						//	for images with both gif and svg versions, add pageIndex to id to distinguish versions in different pages
		Pattern imageIdPattern = Pattern.compile("gif_image(.*?)svg_image(.*?)showCorrectImage\\(\"image", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher imageIdMatcher = imageIdPattern.matcher(fileString);
		fileString = imageIdMatcher.replaceAll("gif_image_" + sectionIndex + "_" + pageIndex + "$1svg_image_" + sectionIndex + "_" + pageIndex + "$2showCorrectImage(\"image_" + sectionIndex + "_" + pageIndex);
		
						//	for applet
		Pattern appletPattern = Pattern.compile("\\.\\./java", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher appletMatcher = appletPattern.matcher(fileString);
		fileString = appletMatcher.replaceAll("../../java");
		
						//	for exercise
		Pattern exercisePattern = Pattern.compile("outputExercise\\(([^\\)]*)\\)", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher exerciseMatcher = exercisePattern.matcher(fileString);
		fileString = exerciseMatcher.replaceAll("outputExercise($1, \"../\")");
		
		return fileString;
	}
}
