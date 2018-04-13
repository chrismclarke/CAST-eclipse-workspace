package cast.index;

import java.util.*;
import java.io.*;
import java.util.regex.*;

import cast.bookManager.*;
import cast.utils.*;

public class Page {
//	static final private String kIndexStart = "<index terms=\"";
//	static final private String kDataStart = "<dataset name=\"";
//	static final private String kTitleStart = "<title>";
//	static final private String kIndexEnd = "\" />";
//	static final private String kDataEnd = "\" />";
//	static final private String kTitleEnd = "</title>";
//	static final private String kAppletStart = "<applet ";
	
//	private Section parent;
	public String name;
	public int pageIndex;
	public int appletCount = 0;
	
	public Page(String dir, String filePrefix, int pageIndex, Section sec, Hashtable indexTable,
																	Hashtable dataSetTable, Hashtable indexTranslationTable, CastEbook castEbook) {
//		parent = sec;
		this.pageIndex = pageIndex;
		
		File inFile = castEbook.getPageHtmlFile(dir, filePrefix);
		String s = HtmlHelper.getFileAsString(inFile);
		
		Pattern thePattern;
		Matcher theMatcher;
		
		thePattern = Pattern.compile("<title>(.*)</title>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		theMatcher = thePattern.matcher(s);
		if (theMatcher.find())
			name = theMatcher.group(1);
		
		thePattern = Pattern.compile("<index\\s*terms=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		theMatcher = thePattern.matcher(s);
		if (theMatcher.find()) {
			String indexTerms = theMatcher.group(1);
			processIndexEntries(indexTerms, indexTranslationTable, indexTable);
		}
		
		thePattern = Pattern.compile("<meta\\s*name=\"index\"\\s*content=\"([,\\- \\w]*)\"\\s*>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		theMatcher = thePattern.matcher(s);
		if (theMatcher.find()) {
			String indexTerms = theMatcher.group(1);
			processIndexEntries(indexTerms, indexTranslationTable, indexTable);
		}
		
		thePattern = Pattern.compile("<dataset\\s*name=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		theMatcher = thePattern.matcher(s);
		while (theMatcher.find()) {
			String dataReference = theMatcher.group(1);
			addEntry(dataSetTable, dataReference);
		}
		
		thePattern = Pattern.compile("<meta\\s*name=\"dataset\"\\s*content=\"([,\\- \\w]*)\"\\s*>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		theMatcher = thePattern.matcher(s);
		while (theMatcher.find()) {
			String dataReference = theMatcher.group(1);
			addEntry(dataSetTable, dataReference);
		}
		
		thePattern = Pattern.compile("<applet", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		theMatcher = thePattern.matcher(s);
		while (theMatcher.find())
					appletCount ++;
		
/*
		try {
			File fileObject = castEbook.getPageHtmlFile(dir, filePrefix);
			BufferedReader reader = new BufferedReader(new FileReader(fileObject));
			
			String s;
			while ((s = reader.readLine()) != null) {
				int titleStart = s.indexOf(kTitleStart);
				int titleEnd = s.indexOf(kTitleEnd);
				if (titleStart >= 0 && titleEnd > titleStart)
					name = s.substring(titleStart + kTitleStart.length(), titleEnd);
				
				int indexStart = s.indexOf(kIndexStart);
				int indexEnd = s.indexOf(kIndexEnd);
				if (indexStart >= 0 && indexEnd > indexStart) {
					String indexLine = s.substring(indexStart + kIndexStart.length(), indexEnd);
					StringTokenizer st = new StringTokenizer(indexLine, ",");
					while (st.hasMoreTokens()) {
						String term = st.nextToken();
						while (term.length() > 0 && term.charAt(0) == ' ')
							term = term.substring(1);
						if (indexTranslationTable != null) {
							String translatedTerm = (String)indexTranslationTable.get(term);
							if (translatedTerm != null)
								term = translatedTerm;
						}
						addEntry(indexTable, term);
					}
				}
				
				int dataStart = s.indexOf(kDataStart);
				int dataEnd = s.indexOf(kDataEnd);
				if (dataStart >= 0 && dataEnd > dataStart) {
					String dataReference = s.substring(dataStart + kDataStart.length(), dataEnd);
					addEntry(dataSetTable, dataReference);
				}
				int appletStart = s.indexOf(kAppletStart);
				if (appletStart >= 0)
					appletCount ++;
			}
			reader.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
*/
	}
	
	private void processIndexEntries(String s, Hashtable indexTranslationTable, Hashtable indexTable) {
			StringTokenizer st = new StringTokenizer(s, ",");
			while (st.hasMoreTokens()) {
				String term = st.nextToken();
				while (term.length() > 0 && term.charAt(0) == ' ')
					term = term.substring(1);
				if (indexTranslationTable != null) {
					String translatedTerm = (String)indexTranslationTable.get(term);
					if (translatedTerm != null)
						term = translatedTerm;
				}
				addEntry(indexTable, term);
			}
	}
	
	public int countApplets() {
		return appletCount;
	}
	
	private void addEntry(Hashtable indexTable, String s) {
		int slashIndex = s.indexOf('/');
		String key = null;
		String note = null;
		if (slashIndex > 0) {
			key = BookIndexer.stripSpaces(s.substring(0, slashIndex));
			note = BookIndexer.stripSpaces(s.substring(slashIndex + 1, s.length()));
		}
		else
			key = BookIndexer.stripSpaces(s);
		
		Vector lineEntry = (Vector)indexTable.get(key);
		if (lineEntry == null) {
			lineEntry = new Vector(5);
			indexTable.put(key, lineEntry);
		}
		
		lineEntry.addElement(new IndexReference(this, note));
	}
}
