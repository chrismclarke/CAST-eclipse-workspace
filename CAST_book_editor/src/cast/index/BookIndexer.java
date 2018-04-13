package cast.index;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import cast.bookManager.*;
import cast.utils.*;
import cast.core.*;
import cast.server.*;


public class BookIndexer extends CoreBookProcessor {
	
	private Label chapterName, sectionName, pageName;
	private Checkbox debugCheck;
	
	private PrintWriter structureWriter, dataSetWriter, indexWriter;
	
	private Book bk;
	private Hashtable indexTable, dataSetTable, uiTermsTranslation;
	@SuppressWarnings("unused")
	private Hashtable indexTranslation;
	private HashSet videoSet;
	private int pageIndex;
	private boolean hadDownloadedVideos;
	
	private StringBuffer versionInfo;
	private Vector versionsUsed;
	
	static public String stripSpaces(String s) {
		int indexStart = 0;
		while (indexStart < s.length() && s.charAt(indexStart) == ' ')
			indexStart ++;
		int indexEnd = s.length();
		while (indexEnd > 0 && s.charAt(indexEnd - 1) == ' ')
			indexEnd --;
		if (indexStart == 0 && indexEnd == s.length())
			return s;
		else
			return s.substring(indexStart, indexEnd);
	}
	

	static private String translateToUnicode(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i=0 ; i<s.length() ; i++) {
			char c = s.charAt(i);
			if ((c & 0xFF00) == 0)	//	standard character
				sb.append(c);
			else {
				String cs = Integer.toHexString(c);
				while (cs.length() < 4)
					cs = "0" + cs;
				sb.append("\\u" + cs);
			}
		}
		
		return sb.toString();
	}

	
	public BookIndexer(BookReader theBook, CastEbook castEbook) {
		super(theBook, castEbook);
	}
	
	protected void addUiControls() {
		if (Options.hasMultipleCollections) {
			debugCheck = new Checkbox("Show Output in System.out");
			add(debugCheck);
		}
		
			chapterName = new Label("", Label.LEFT);
		add(chapterName);
		
			sectionName = new Label("", Label.LEFT);
		add(sectionName);
		
			pageName = new Label("", Label.LEFT);
		add(pageName);
	}
	
	private Hashtable getTermTranslations(File translationFile) {		//	we have already checked that the file exists
		Hashtable theTranslationTable = new Hashtable(100);
		
		try {
			FileInputStream fis = new FileInputStream(translationFile);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader reader = new BufferedReader(isr);
			
			String s;
			while ((s = reader.readLine()) != null) {
				int equalsStart = s.indexOf("=");
				String term = s.substring(0, equalsStart);
				String translation = s.substring(equalsStart + 1);
				
				theTranslationTable.put(term, translation);
			}
			reader.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return theTranslationTable;
	}
	
	private String translateTerm(String term, Hashtable translationTable) {
		if (translationTable == null)
			return term;
		else
			return (String)translationTable.get(term);
	}
	
	public void run() {
		File indexOutputFile = castEbook.getIndexFile();
		File dataSetsOutputFile = castEbook.getDataSetsFile();
		File structureOutputFile = castEbook.getStructureFile();
		File dataSourceFile = castEbook.getDataSourceFile();
		hadDownloadedVideos = CoreDownloadTask.hasDownloadedVideos(castEbook.getBookDir());
																	//	we don't want to change this setting
		
		try {
			if (debugCheck == null || !debugCheck.getState()) {
				structureWriter = FileFinder.createUTF8Writer(structureOutputFile);
				dataSetWriter = FileFinder.createUTF8Writer(dataSetsOutputFile);
				indexWriter = FileFinder.createUTF8Writer(indexOutputFile);
			}
			else {
				structureWriter = null;
				dataSetWriter = null;
				indexWriter = null;
			}
			
			String language = theBook.getLanguage();
			File indexTranslationFile = castEbook.getIndexTermsFile(language);
			if (indexTranslationFile != null)
				indexTranslation = getTermTranslations(indexTranslationFile);
				
			File uiTermsTranslationFile = castEbook.getUiTermsFile(language);
			if (uiTermsTranslationFile != null)
				uiTermsTranslation = getTermTranslations(uiTermsTranslationFile);
			
			bk = new Book();
			indexTable = new Hashtable(100);
			dataSetTable = new Hashtable(100);
			videoSet = new HashSet();
			pageIndex = -1;
			
			writeFileStarts();
			processBookTree();
			
			writeBookInfo();
			
			IndexTerm[] sortedIndex = getSortedIndex(indexTable);
			IndexTerm[] sortedDataSets = getSortedIndex(dataSetTable);
			String[] dataSources = getDataSources(sortedDataSets, dataSourceFile);
			Object[] videoObjects = videoSet.toArray();
			String[] videoFiles = new String[videoObjects.length];
			for (int i=0 ; i<videoFiles.length ; i++)
				videoFiles[i] = (String)videoObjects[i];
			Arrays.sort(videoFiles);
			
			outputIndex(sortedIndex);
			outputDataSets(sortedDataSets, dataSources);
			outputVideos(videoFiles);
			outputBookDatesFile();
			updateCustomBooksFile();
			
			writeFileEnds();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
			
		structureWriter = null;
		
		chapterName.setText("Finished " + bk.countPages() + "pg, " + bk.countApplets() + "ap");
		sectionName.setText("");
		pageName.setText("");
	}
	
	private void writeFileStarts() {
		if (structureWriter != null) {
			versionInfo = new StringBuffer();
			versionsUsed = new Vector();
		}
		
		if (indexWriter != null) {
			indexWriter.println("<!DOCTYPE HTML>");
			indexWriter.println("<html>");
			indexWriter.println("<head>");
			indexWriter.println("<title>Index</title>");
			indexWriter.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
			indexWriter.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n");
			
			indexWriter.println("<link href=\"../../terms/indexStyles.css\" rel=\"stylesheet\" type=\"text/css\" />");
			indexWriter.println("<script src=\"../../structure/environment.js\"></script>");
			indexWriter.println("<script src=\"../../terms/indexCreation.js\"></script>\n");
			indexWriter.println("<script>");
		}
		
		if (dataSetWriter != null) {
			dataSetWriter.println("<!DOCTYPE HTML>");
			dataSetWriter.println("<html>");
			dataSetWriter.println("<head>");
			dataSetWriter.println("<title>Data sets</title>");
			dataSetWriter.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
			dataSetWriter.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n");
			
			dataSetWriter.println("<link href=\"../../terms/indexStyles.css\" rel=\"stylesheet\" type=\"text/css\" />");
			dataSetWriter.println("<script src=\"../../structure/environment.js\"></script>");
			dataSetWriter.println("<script src=\"../../terms/indexCreation.js\"></script>\n");
			dataSetWriter.println("<script>");
		}
	}
	
	private void writeFileEnds() {
		if (structureWriter != null) {
			structureWriter.println("var fullTextString = '" + translateTerm("FullText", uiTermsTranslation) + "';");
			structureWriter.println("var summaryTextString = '" + translateTerm("SummaryText", uiTermsTranslation) + "';");
			structureWriter.println("var videoTextString = '" + translateTerm("VideoText", uiTermsTranslation) + "';");
			structureWriter.println("var appletTextString = '" + translateTerm("AppletText", uiTermsTranslation) + "';");
			
			structureWriter.println("fullTabTooltip = '" + translateTerm("FullTooltip", uiTermsTranslation) + "';");
			structureWriter.println("summaryTabTooltip = '" + translateTerm("SummaryTooltip", uiTermsTranslation) + "';");
			structureWriter.println("videoTabTooltip = '" + translateTerm("VideoTooltip", uiTermsTranslation) + "';");
			structureWriter.println("appletTabTooltip = '" + translateTerm("AppletTooltip", uiTermsTranslation) + "';");
			
			structureWriter.println("var chapterString = '" + translateTerm("Chapter", uiTermsTranslation) + "';\n");
			structureWriter.print("var versionName = {");
			for (int i=0 ; i<versionsUsed.size() ; i++) {
				if (i > 0)
					structureWriter.print(", ");
				String v = (String)versionsUsed.elementAt(i);
				structureWriter.print("'" + v + "': '" + translateTerm(v, uiTermsTranslation) + "'");
			}
			structureWriter.println("};");
			
			structureWriter.println("var versions = {};");
			structureWriter.println(versionInfo.toString());
		
			structureWriter.println("bookLoaded = true;");
			
			structureWriter.flush();
			structureWriter.close();
		}
		
		if (indexWriter != null) {
			String entriesTitle = translateTerm("IndexEntries", uiTermsTranslation);
			String referencesTitle = translateTerm("References", uiTermsTranslation);
			
			indexWriter.println("</script>");
			indexWriter.println("</head>\n\n");
			
			indexWriter.println("<body onLoad='writeTermEntries()'>");
			indexWriter.println("  <div id='indexTermsPanel'>");
			indexWriter.println("    <div class='heading' id='termsTitle'><iframe src='../../terms/entryTitle.html?title=" + entriesTitle + "' scrolling='no'></iframe></div>");
			indexWriter.println("    <div class='content' id='terms'></div>");
			indexWriter.println("  </div>");
			indexWriter.println("  <div id='indexReferencesPanel'>");
			indexWriter.println("    <div class='heading' id='referenceTitle'><iframe src='../../terms/referenceTitle.html?title=" + referencesTitle + "' scrolling='no'></iframe></div>");
			indexWriter.println("    <div class='content' id='references'></div>");
			indexWriter.println("  </div>");
			indexWriter.println("</body>\n");
			
			indexWriter.println("</html>");

			
			indexWriter.flush();
			indexWriter.close();
		}
		
		if (dataSetWriter != null) {
			String datasetsTitle = translateTerm("Datasets", uiTermsTranslation);
			String referencesTitle = translateTerm("References", uiTermsTranslation);
			String sourcesTitle = translateTerm("Sources", uiTermsTranslation);
			
			dataSetWriter.println("</script>");
			dataSetWriter.println("</head>\n\n");
			
			
			dataSetWriter.println("<body onLoad='writeTermEntries()'>");
			dataSetWriter.println("  <div id='datasetsPanel'>");
			dataSetWriter.println("    <div class='heading' id='termsTitle'><iframe src='../../terms/datasetTitle.html?title=" + datasetsTitle + "' scrolling='no'></iframe></div>");
			dataSetWriter.println("    <div class='content' id='terms'></div>");
			dataSetWriter.println("  </div>");
			dataSetWriter.println("  <div id='dataReferencesPanel'>");
			dataSetWriter.println("    <div class='heading' id='referenceTitle'><iframe src='../../terms/referenceTitle.html?title=" + referencesTitle + "' scrolling='no'></iframe></div>");
			dataSetWriter.println("    <div class='content' id='references'></div>");
			dataSetWriter.println("  </div>");
			dataSetWriter.println("  <div id='dataSourcePanel'>");
			dataSetWriter.println("    <div class='heading' id='sourceTitle'><iframe src='../../terms/sourceTitle.html?title=" + sourcesTitle + "' scrolling='no'></iframe></div>");
			dataSetWriter.println("    <div class='content' id='source'></div>");
			dataSetWriter.println("  </div>");
			dataSetWriter.println("</body>\n");
			
			dataSetWriter.println("</html>");

			
			dataSetWriter.flush();
			dataSetWriter.close();
		}
	}
	
	protected void processBook(BookTree bookTree) {
		coreProcessChapter(bookTree.dir, bookTree.filePrefix, bookTree.title, "addBook");
	}
	
	protected void processPart(BookTree partTree) {
		if (structureWriter != null)
			structureWriter.println("\naddPart(\"" + partTree.title + "\");");
	}
	
	protected void processChapter(BookTree chapterTree) {
		coreProcessChapter(chapterTree.dir, chapterTree.filePrefix, chapterTree.title, "addChapter");
	}
	
	private void coreProcessChapter(String dir, String filePrefix, String title, String command) {
		pageIndex ++;
		bk.addChapter();
		chapterName.setText(filePrefix);
		
		if (structureWriter != null) {
			title = translateToUnicode(title);
			String params = dir + "', '" + filePrefix + "', \"" + title + "\"";
			File summaryFile = castEbook.getPageHtmlFile(dir, "s_" + filePrefix);
			if (summaryFile.exists())
				params += ", 's_" + filePrefix + "'";
			File videoFile = castEbook.getPageHtmlFile(dir, "v_" + filePrefix);
			if (videoFile.exists()) {
				findVideos(dir, "v_" + filePrefix);
				if (!summaryFile.exists())
					params += ", null";
				params += ", 'v_" + filePrefix + "'";
			}
			structureWriter.println("\n" + command + "('" + params + ");");
		}
		
		if (indexWriter != null)
			indexWriter.println("aC();");
			
		if (dataSetWriter != null)
			dataSetWriter.println("aC();");
	}
	
	
	private String escapeApostrophe(String s) {
		return s.replaceAll("'", "\\\\'");
	}

	
	protected void processSection(BookTree sectionTree) {
		String dir = sectionTree.dir;
		String filePrefix = sectionTree.filePrefix;
		String title = sectionTree.title;
		
		if (castEbook.isHomeDirName(dir)) {
			SectionPageGenerator sectionGenerator = new SectionPageGenerator(castEbook);
			sectionGenerator.processSection(dir, filePrefix);
		}
		
		Section sec = bk.addSection(dir, filePrefix, castEbook);
		if (sec.hasOverview())
			pageIndex += 2;
		else
			pageIndex ++;
		sectionName.setText(filePrefix);
		
		if (structureWriter != null) {
			title = translateToUnicode(title);
			structureWriter.println("addSection('" + dir + "', '" + filePrefix + "', \"" + title + "\");");
		}
		
		String escapedName = escapeApostrophe(title);
		if (indexWriter != null) {
			if (sec.hasOverview())
				indexWriter.println("\taOS('" + escapedName + "');");
			else
				indexWriter.println("\taS('" + escapedName + "');");
		}
		
		if (dataSetWriter != null) {
			if (sec.hasOverview())
				dataSetWriter.println("\taOS('" + escapedName + "');");
			else
				dataSetWriter.println("\taS('" + escapedName + "');");
		}
	}
	
	private class FileName {
		String prefix, core, suffix, index;
		FileName(String name) {
			if (name.charAt(1) == '_') {
				prefix = name.substring(0, 2);
				core = name.substring(2);
			}
			else {
				core = name;
				prefix = "";
			}
			index = "";
			do {
				char c = core.charAt(core.length() - 1);
				if (c >= '0' && c <= '9') {
					index = c + index;
					core = core.substring(0, core.length() - 1);
				}
				else
					break;
			} while (core.length() > 0);
			
			if (core.charAt(core.length() - 2) == '_') {
				suffix = core.substring(core.length() - 2);
				core = core.substring(0, core.length() - 2);
			}
			else
				suffix = "_g";
		}
		
		boolean versionOfSame(FileName fn) {
			return prefix.equals(fn.prefix) && core.equals(fn.core) && index.equals(fn.index);
		}
		
		boolean differentVersionOfSame(FileName fn) {
			return versionOfSame(fn) && !suffix.equals(fn.suffix);
		}
	}
	
/*
	private boolean hasSummaries(String dir, String filePrefix) {
		if (!theBook.hasSummaries())
			return false;
		
		String fileString = HtmlHelper.getFileAsString(dir, filePrefix, castEbook);
		Pattern titlePattern = Pattern.compile("<div\\s+class=['\"]summary_text['\"]", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher titleMatcher = titlePattern.matcher(fileString);
		return titleMatcher.find();
	}
*/
	
	protected void processPage(BookTree pageTree) {
		String dir = pageTree.dir;
		String filePrefix = pageTree.filePrefix;
		String title = pageTree.title;
		String summaryDir = pageTree.summaryDir;
		String summaryFilePrefix = pageTree.summaryFilePrefix;
		String videoDir = pageTree.videoDir;
		String videoFilePrefix = pageTree.videoFilePrefix;
		
		pageIndex ++;
		Page page = bk.addPage(dir, filePrefix, pageIndex, indexTable, dataSetTable, uiTermsTranslation, castEbook);
		if (videoDir != null && videoFilePrefix != null)
			findVideos(videoDir, videoFilePrefix);
		pageName.setText(filePrefix);
		
		if (structureWriter != null) {
			title = translateToUnicode(title);
			String extrasString = (summaryDir != null && summaryFilePrefix != null) ? (", '" + summaryDir + "', '" + summaryFilePrefix + "'") : "";
			if (videoDir != null && videoFilePrefix != null)
				extrasString += ", '" + videoDir + "', '" + videoFilePrefix + "'";
			String appletsString = (page.countApplets() > 0) ? "interact" : "static";
			structureWriter.println("addPage_2('" + dir + "', '" + filePrefix + "', \"" + title + "\", '" + appletsString + "'" + extrasString + ");");
			
			Vector versions = new Vector();
			int versionCount = 0;
			boolean isCustom = dir.indexOf("bk/") == 0;
			FileName fileName = new FileName(filePrefix);
			
			if (isCustom) {
				versions.add("+" + fileName.suffix.charAt(1));
				versionCount = 1;
				
				String languagePrefix = theBook.getLanguage() + "/";
				
				File sectionDir = castEbook.getSectionDir(languagePrefix + fileName.core);
				if (sectionDir.exists()) {
					File file[] = sectionDir.listFiles();
					for (int i=0 ; i<file.length ; i++) {
						String name = file[i].getName();
						if (name.endsWith(".html")) {
							FileName fn = new FileName(name.substring(0, name.length() - 5));
							if (fileName.versionOfSame(fn)) {
								versions.add(fn.suffix);
								versionCount ++;
							}
						}
					}
				}
			}
			else {
				versions.add(fileName.suffix);
				versionCount = 1;
				
				File sectionDir = new File(castEbook.getCoreDir(), dir);
				
				File file[] = sectionDir.listFiles();
				for (int i=0 ; i<file.length ; i++) {
				 String name = file[i].getName();
				 if (name.endsWith(".html")) {
						FileName fn = new FileName(name.substring(0, name.length() - 5));
						if (fileName.differentVersionOfSame(fn)) {
							versions.add(fn.suffix);
							versionCount ++;
						}
					}
				}
			}
			
			if (versionCount > 1) {
				String baseFile = fileName.prefix + fileName.core + fileName.index;
				String versionString = "versions['" + baseFile + "'] = new Array(";
				for (int i=0 ; i<versionCount ; i++) {
					if (i > 0)
						versionString += ", ";
					String v = (String)versions.elementAt(i);
					versionString += "'" + v + "'";
					if (v.charAt(0) == '+')						// do not distinguish between "+g", "+b", etc.
						v = "++";
					if (!versionsUsed.contains(v))
						versionsUsed.add(v);
				}
				versionString += ");\n";
				versionInfo.append(versionString);
			}
		}
		
		String escapedName = escapeApostrophe(title);
		if (indexWriter != null)
			indexWriter.println("\t\taP('" + escapedName + "','" + dir + "','" + filePrefix + "');");
			
		if (dataSetWriter != null)
			dataSetWriter.println("\t\taP('" + escapedName + "','" + dir + "','" + filePrefix + "');");
			
		Thread.yield();
	}
	
	private void findVideos(String videoDir, String videoFilePrefix) {
		File inFile = castEbook.getPageHtmlFile(videoDir, videoFilePrefix);
		String s = HtmlHelper.getFileAsString(inFile);
		
		Pattern thePattern = Pattern.compile("writeVideo\\(['\"]bk/([^'\"]*)['\"],\\s*['\"]([^'\"]*)['\"],\\s*['\"]([^'\"]*)['\"]", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher theMatcher = thePattern.matcher(s);
		while (theMatcher.find()) {
			String book = theMatcher.group(1);
			String dir = theMatcher.group(2);
			String video = theMatcher.group(3);
			videoSet.add(book + "/" + dir + "/" + video);
		}
	}
	
	private void writeBookInfo() {
		if (structureWriter != null) {
			structureWriter.println("");
			String language = theBook.getLanguage();
			if (language == null)
				structureWriter.println("var language = null;");
			else
				structureWriter.println("var language = \"" + language + "\";");
			
			String logoGif = theBook.getLogoGif();
			if (logoGif == null)
				structureWriter.println("var logoGif = null;");
			else
				structureWriter.println("var logoGif = \"" + logoGif + "\";");
				
			String homeDir = castEbook.getHomeDirName();
			structureWriter.println("var homeDir = \"" + homeDir + "\";");
			
			structureWriter.println("var versionGif = \"" + theBook.getVersionImage() + "\";");
			
			structureWriter.println("var isLecturingVersion = " + (theBook.isLecturingVersion() ? "true" : "false") + ";");
			structureWriter.println("var isModule = " + (theBook.isModule() ? "true" : "false") + ";");
			
			structureWriter.println("var hasSummaries = " + (theBook.hasSummaries() ? "true" : "false") + ";");
			structureWriter.println("var hasVideos = " + (theBook.hasVideos() ? "true" : "false") + ";");
			
			String summaryPdfUrl = theBook.getSummaryPdfUrl();
			if (summaryPdfUrl == null)
				structureWriter.println("var summaryPdfUrl = null;");
			else
				structureWriter.println("var summaryPdfUrl = \"" + summaryPdfUrl + "\";");
			structureWriter.println("var castWebServerUrl = \"http://" + Options.kCastDownloadUrl + "\";");
			
			structureWriter.println("var noOfChapters = \"" + bk.countChapters() + "\";\n");		//	includes preface but not splash 'chapter'
			structureWriter.println("var localVideos = " + hadDownloadedVideos + ";\n");
			
		}
	}
	
	private String[] getDataSources(IndexTerm[] sortedDataSets, File dataSourceFile) {
		Hashtable dataSourceTable = readDataSources(dataSourceFile);
		
		String[] sources = new String[sortedDataSets.length];
		for (int i=0 ; i<sortedDataSets.length ; i++)
			sources[i] = (String)dataSourceTable.get(sortedDataSets[i].term);
		return sources;
	}
	
	private void outputDataSets(IndexTerm[] sortedDataSets, String[] sources) {
		if (dataSetWriter == null) {
			for (int i=0 ; i<sortedDataSets.length ; i++) {
				System.out.println(sortedDataSets[i].term);
				System.out.println(sources[i] == null ? "No reference" : sources[i]);
				for (int j=0 ; j<sortedDataSets[i].reference.length ; j++) {
					IndexReference ir = sortedDataSets[i].reference[j];
					String theRef = "   " + ir.page.name;
					if (ir.comment != null)
						theRef += (" (" + ir.comment + ")");
					System.out.println(theRef);
				}
			}
		}
		else
			for (int i=0 ; i<sortedDataSets.length ; i++) {
				dataSetWriter.print("aT('" + sortedDataSets[i].term + "'");
				
				IndexReference[] refs = sortedDataSets[i].reference;
				for (int j=0 ; j<refs.length ; j++)
					dataSetWriter.print("," + refs[j].page.pageIndex);
				dataSetWriter.println(");");
				if (sources[i] != null)				//		assumes sortedDataSets[] and sources[] are same length
					dataSetWriter.println("aSrc(\"" + sources[i] + "\");");
			}
	}
	
	private void outputIndex(IndexTerm[] sortedIndex) {
		if (indexWriter == null) {
			for (int i=0 ; i<sortedIndex.length ; i++) {
				System.out.println(sortedIndex[i].term);
				for (int j=0 ; j<sortedIndex[i].reference.length ; j++) {
					IndexReference ir = sortedIndex[i].reference[j];
					String theRef = "   " + ir.page.name;
					if (ir.comment != null)
						theRef += (" (" + ir.comment + ")");
					System.out.println(theRef);
				}
			}
		}
		else
			for (int i=0 ; i<sortedIndex.length ; i++) {
				indexWriter.print("aT('" + sortedIndex[i].term + "'");
				
				IndexReference[] refs = sortedIndex[i].reference;
				for (int j=0 ; j<refs.length ; j++)
					indexWriter.print("," + refs[j].page.pageIndex);
				indexWriter.println(");");
			}
	}
	
	private IndexTerm[] getSortedIndex(Hashtable indexTable) {
		IndexTerm[] sortedIndex = new IndexTerm[indexTable.size()];
		Enumeration eKey = indexTable.keys();
		Enumeration eEntry = indexTable.elements();
		for (int i=0 ; i<sortedIndex.length ; i++)
			sortedIndex[i] = new IndexTerm((String)eKey.nextElement(), vectorToArray((Vector)eEntry.nextElement()));
		
		quickSort(sortedIndex, 0, sortedIndex.length - 1);
		return sortedIndex;
	}
	
	private IndexReference[] vectorToArray(Vector referenceVector) {
		Enumeration e = referenceVector.elements();
		IndexReference reference[] = new IndexReference[referenceVector.size()];
		for (int i=0 ; i<reference.length ; i++)
			reference[i] = (IndexReference)e.nextElement();
		return reference;
	}
	
	private void quickSort (IndexTerm[] termArray, int lo0, int hi0) {
      int lo = lo0;
      int hi = hi0;
      if (hi0 > lo0) {
         String mid = termArray[(lo0 + hi0) / 2].term;
         while(lo <= hi) {
            while((lo < hi0) && (termArray[lo].term.compareTo(mid) < 0))
               ++lo;
            while((hi > lo0) && (termArray[hi].term.compareTo(mid) > 0))
               --hi;
            if(lo <= hi) {
               IndexTerm temp = termArray[lo];
               termArray[lo] = termArray[hi];
               termArray[hi] = temp;
               ++lo;
               --hi;
            }
         }
         if(lo0 < hi)
            quickSort(termArray, lo0, hi);
         if(lo < hi0)
            quickSort(termArray, lo, hi0);

      }
	}
	
	private Hashtable readDataSources(File dataSourceFile) {
		Hashtable sourceTable = new Hashtable(100);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(dataSourceFile));
			
			String s;
			while ((s = reader.readLine()) != null) {
				int slashIndex = s.indexOf('/');
				String dataName = null;
				String sourceReference = null;
				if (slashIndex > 0) {
					dataName = stripSpaces(s.substring(0, slashIndex));
					sourceReference = stripSpaces(s.substring(slashIndex + 1, s.length()));
				}
				else
					dataName = stripSpaces(s);
				
				Object existingEntry = sourceTable.get(dataName);
				if (existingEntry != null)
					System.out.println("Duplicate data set, " + s);
				if (sourceReference != null)
					sourceTable.put(dataName, sourceReference);
			}
			
			reader.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return sourceTable;
	}
	
	private void outputVideos(String[] videoFiles) {
		if (debugCheck == null || !debugCheck.getState())
			try {
				File videoXmlFile = castEbook.getXmlFile(castEbook.getHomeDirName(), "videos");
				PrintWriter videoListWriter = FileFinder.createUTF8Writer(videoXmlFile);
				
				videoListWriter.println("<?xml version='1.0' encoding='UTF-8' standalone='no'?>");
				videoListWriter.println("<!DOCTYPE book SYSTEM '../../../structure/videoXmlDefn.dtd'>");
				videoListWriter.println("<videoList>");
				
				String currentBook = null;
				String currentDir = null;
				for (int i=0 ; i<videoFiles.length ; i++) {
					String[] location = videoFiles[i].split("/");
					boolean sameBook = location[0].equals(currentBook);
					boolean sameDir = sameBook && location[1].equals(currentDir);
					
					if (!sameDir && currentDir != null)
						videoListWriter.println("    </dir>");
					if (!sameBook && currentBook != null)
						videoListWriter.println("  </book>");
					
					if (!sameBook) {
						currentBook = location[0];
						videoListWriter.println("  <book name='" + currentBook + "'>");
					}
					
					if (!sameDir) {
						currentDir = location[1];
						videoListWriter.println("    <dir name='" + currentDir + "'>");
					}
					
					videoListWriter.println("      <video name='" + location[2] + "'/>");
				}
				
				if (currentDir != null)
					videoListWriter.println("    </dir>");
				
				if (currentBook != null)
					videoListWriter.println("  </book>");
				
				videoListWriter.println("</videoList>");
				videoListWriter.flush();
				videoListWriter.close();
			} catch (IOException e) {
				System.err.println(e.toString());
				e.printStackTrace(System.out);
			}
	}
	
	
	private void outputBookDatesFile() {
		File bookDir = castEbook.getBookDir();
		new DatesHash(bookDir).saveToFile(DatesHash.getDateStampFile(bookDir));
	}
	
	private void updateCustomBooksFile() {
//		String bookName = castEbook.getShortBookName();
		CoreDownloadTask.updateInstalledBook(castEbook, true);
	}
}
