package ebookStructure;

import java.util.regex.*;
import java.io.*;


public class HtmlHelper {
	
	static public String getFileEncoding(File f, String charsetPattern) {
		String encoding = "UTF-8";			//	default and used for "xxx.properties" files
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String s;
			Pattern encodingPattern = Pattern.compile(charsetPattern, Pattern.CASE_INSENSITIVE);
			while ((s = reader.readLine()) != null) {
				Matcher encodingMatcher = encodingPattern.matcher(s);
				if (encodingMatcher.find()) {
					encoding = encodingMatcher.group(1).toUpperCase();
					break;
				}
			}
			reader.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return encoding;
	}
	
	static public String getFileAsString(File f) {
		String charset = getFileEncoding(f, "<meta .* charset=([^\"']*)[\"']\\s*/?>");
		return getFileAsString(f, charset);
	}
	
	static public String getFileAsString(File f, String charset) {
		try {
			if (charset != null) {
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader isr = new InputStreamReader(fis, charset);
				
				BufferedReader reader = new BufferedReader(isr);
					
				StringBuffer sb = new StringBuffer();
				String s;
				while ((s = reader.readLine()) != null) {
					sb.append(s);
					sb.append("\n");
				}
				
				reader.close();
				
				return sb.toString();
			}
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return null;
	}
	
	static public String getFileAsString(String dir, String filePrefix, CastEbook castEbook) {
		File fileObject = castEbook.getPageHtmlFile(dir, filePrefix);
		return getFileAsString(fileObject);
	}
	
	static public String getTagInFile(String fileString, String tag) {
		Pattern titlePattern = Pattern.compile("<" + tag + ">(.*)</" + tag + ">", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher titleMatcher = titlePattern.matcher(fileString);
		titleMatcher.find();
		return titleMatcher.group(1).replaceAll("\\s", " ");		//	change all whitespace characters to spaces
	}
	
	static public String getTagInFile(String dir, String filePrefix, CastEbook castEbook, String tag) {
		File page = castEbook.getPageHtmlFile(dir, filePrefix);
		String fileString = getFileAsString(page);
		return getTagInFile(fileString, tag);
	}
	
}
