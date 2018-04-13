package ebookStructure;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import org.w3c.dom.*;


public class XmlHelper {
	
	static public String getFileAsString(File f) {
		String charset = HtmlHelper.getFileEncoding(f, "<?xml [^?]* encoding=['\"]([^\"']*)[\"']");
		return HtmlHelper.getFileAsString(f, charset);
	}
	
	static public String getUniqueTagAsString(Element e, String tagName) {
		Element tagElement = getUniqueTag(e, tagName);
		if (tagElement == null)
			return null;
		else
			return getTagInterior(tagElement);
	}
	
	static public int getUniqueTagAsInt(Element e, String tagName) {
		String tagString = XmlHelper.getUniqueTagAsString(e, tagName);
		if (tagString == null)
			return 0;
		else
			return Integer.parseInt(tagString);
	}
	
	static public Element getUniqueTag(Element e, String tagName) {
		NodeList nl = e.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0)
			return (Element)nl.item(0);
		else
			return null;
	}
	
	static public String getTagInterior(Element e) {
		Node child = e.getFirstChild();
		if (child == null)
			return null;
		else
			return child.getNodeValue();
	}
	
	static public void setTagInterior(Element e, String s) {
		e.getFirstChild().setNodeValue(s);
	}
	
	
	static final public boolean WITH_PARAGRAPHS = true;
	static final public boolean WITHOUT_PARAGRAPHS = false;
	
	static final private String RAW_HTML_ENTITY_TABLE =
            "nbsp   #160 00A0   iexcl  #161 00A1   cent   #162 00A2   pound  #163 00A3   curren #164 00A4   yen    #165 00A5 " +
            "brvbar #166 00A6   sect   #167 00A7   uml    #168 00A8   copy   #169 00A9   ordf   #170 00AA   laquo  #171 00AB " +
            "not    #172 00AC   shy    #173 00AD   reg    #174 00AE   macr   #175 00AF   deg    #176 00B0   plusmn #177 00B1 " +
            "sup2   #178 00B2   sup3   #179 00B3   acute  #180 00B4   micro  #181 00B5   para   #182 00B6   middot #183 00B7 " +
            "cedil  #184 00B8   sup1   #185 00B9   ordm   #186 00BA   raquo  #187 00BB   frac14 #188 00BC   frac12 #189 00BD " +
            "frac34 #190 00BE   iquest #191 00BF   Agrave #192 00C0   Aacute #193 00C1   Acirc  #194 00C2   Atilde #195 00C3 " +
            "Auml   #196 00C4   Aring  #197 00C5   AElig  #198 00C6   Ccedil #199 00C7   Egrave #200 00C8   Eacute #201 00C9 " +
            "Ecirc  #202 00CA   Euml   #203 00CB   Igrave #204 00CC   Iacute #205 00CD   Icirc  #206 00CE   Iuml   #207 00CF " +
            "ETH    #208 00D0   Ntilde #209 00D1   Ograve #210 00D2   Oacute #211 00D3   Ocirc  #212 00D4   Otilde #213 00D5 " +
            "Ouml   #214 00D6   times  #215 00D7   Oslash #216 00D8   Ugrave #217 00D9   Uacute #218 00DA   Ucirc  #219 00DB " +
            "Uuml   #220 00DC   Yacute #221 00DD   THORN  #222 00DE   szlig  #223 00DF   agrave #224 00E0   aacute #225 00E1 " +
            "acirc  #226 00E2   atilde #227 00E3   auml   #228 00E4   aring  #229 00E5   aelig  #230 00E6   ccedil #231 00E7 " +
            "egrave #232 00E8   eacute #233 00E9   ecirc  #234 00EA   euml   #235 00EB   igrave #236 00EC   iacute #237 00ED " +
            "icirc  #238 00EE   iuml   #239 00EF   eth    #240 00F0   ntilde #241 00F1   ograve #242 00F2   oacute #243 00F3 " +
            "ocirc  #244 00F4   otilde #245 00F5   ouml   #246 00F6   divide #247 00F7   oslash #248 00F8   ugrave #249 00F9 " +
            "uacute #250 00FA   ucirc  #251 00FB   uuml   #252 00FC   yacute #253 00FD   thorn  #254 00FE   yuml   #255 00FF " +
            "fnof     #402 0192   Alpha    #913 0391   Beta     #914 0392   Gamma    #915 0393   Delta    #916 0394   Epsilon  #917 0395 " +
            "Zeta     #918 0396   Eta      #919 0397   Theta    #920 0398   Iota     #921 0399   Kappa    #922 039A   Lambda   #923 039B " +
            "Mu       #924 039C   Nu       #925 039D   Xi       #926 039E   Omicron  #927 039F   Pi       #928 03A0   Rho      #929 03A1 " +
            "Sigma    #931 03A3   Tau      #932 03A4   Upsilon  #933 03A5   Phi      #934 03A6   Chi      #935 03A7   Psi      #936 03A8 " +
            "Omega    #937 03A9   alpha    #945 03B1   beta     #946 03B2   gamma    #947 03B3   delta    #948 03B4   epsilon  #949 03B5 " +
            "zeta     #950 03B6   eta      #951 03B7   theta    #952 03B8   iota     #953 03B9   kappa    #954 03BA   lambda   #955 03BB " +
            "mu       #956 03BC   nu       #957 03BD   xi       #958 03BE   omicron  #959 03BF   pi       #960 03C0   rho      #961 03C1 " +
            "sigmaf   #962 03C2   sigma    #963 03C3   tau      #964 03C4   upsilon  #965 03C5   phi      #966 03C6   chi      #967 03C7 " +
            "psi      #968 03C8   omega    #969 03C9   thetasym #977 03D1   upsih    #978 03D2   piv      #982 03D6   bull     #8226 2022 " +
            "hellip   #8230 2026   prime    #8242 2032   Prime    #8243 2033   oline    #8254 203E   frasl    #8260 2044   weierp   #8472 2118 " +
            "image    #8465 2111   real     #8476 211C   trade    #8482 2122   alefsym  #8501 2135   larr     #8592 2190   uarr     #8593 2191 " +
            "rarr     #8594 2192   darr     #8595 2193   harr     #8596 2194   crarr    #8629 21B5   lArr     #8656 21D0   uArr     #8657 21D1 " +
            "rArr     #8658 21D2   dArr     #8659 21D3   hArr     #8660 21D4   forall   #8704 2200   part     #8706 2202   exist    #8707 2203 " +
            "empty    #8709 2205   nabla    #8711 2207   isin     #8712 2208   notin    #8713 2209   ni       #8715 220B   prod     #8719 220F " +
            "sum      #8721 2211   minus    #8722 2212   lowast   #8727 2217   radic    #8730 221A   prop     #8733 221D   infin    #8734 221E " +
            "ang      #8736 2220   and      #8743 2227   or       #8744 2228   cap      #8745 2229   cup      #8746 222A   int      #8747 222B " +
            "there4   #8756 2234   sim      #8764 223C   cong     #8773 2245   asymp    #8776 2248   ne       #8800 2260   equiv    #8801 2261 " +
            "le       #8804 2264   ge       #8805 2265   sub      #8834 2282   sup      #8835 2283   nsub     #8836 2284   sube     #8838 2286 " +
            "supe     #8839 2287   oplus    #8853 2295   otimes   #8855 2297   perp     #8869 22A5   sdot     #8901 22C5   lceil    #8968 2308 " +
            "rceil    #8969 2309   lfloor   #8970 230A   rfloor   #8971 230B   lang     #9001 2329   rang     #9002 232A   loz      #9674 25CA " +
            "spades   #9824 2660   clubs    #9827 2663   hearts   #9829 2665   diams    #9830 2666 " +
            "quot    #34   0022   amp     #38   0026   lt      #60   003C   gt      #62   003E   OElig   #338  0152   oelig   #339  0153 " +
            "Scaron  #352  0160   Yuml    #376  0178   circ    #710  02C6   tilde   #732  02DC   ensp    #8194 2002   emsp    #8195 2003 " +
            "thinsp  #8201 2009   zwnj    #8204 200C   zwj     #8205 200D   lrm     #8206 200E   rlm     #8207 200F   ndash   #8211 2013 " +
            "mdash   #8212 2014   lsquo   #8216 2018   rsquo   #8217 2019   sbquo   #8218 201A   ldquo   #8220 201C   rdquo   #8221 201D " +
            "bdquo   #8222 201E   dagger  #8224 2020   Dagger  #8225 2021   permil  #8240 2030   lsaquo  #8249 2039   rsaquo  #8250 203A " +
            "euro    #8364 20AC";

	/** mapping: HTML entity ---> Unicode character */
	private static final Map<String,Character> HTML_ENTITY_TO_UNICODE_MAP = new HashMap<String,Character>();

	/** mapping: Unicode character ---> HTML entity */
	private static final Map<Character,String> UNICODE_TO_HTML_ENTITY_MAP = new HashMap<Character,String>();

	/** mapping: HTML entity ---> RTF unicode */
	private static final Map<String,String> HTML_ENTITY_TO_RTF_MAP = new HashMap<String,String>();


	/**
	* Static initialization block.
	* Populates HTML_ENTITY_TO_UNICODE_MAP and UNICODE_TO_HTML_ENTITY_MAP.
	*/
	static {
		final String[] elements = RAW_HTML_ENTITY_TABLE.split("[\\s]++");

		for (int i = 0; i < elements.length; i += 3) {
			final char unicode = (char) Integer.parseInt(elements[i + 2], 16);
			final String htmlElement = elements[i];
			final String htmlUnicode = elements[i + 1];
			HTML_ENTITY_TO_UNICODE_MAP.put(htmlElement, unicode);
			HTML_ENTITY_TO_UNICODE_MAP.put(htmlUnicode, unicode);
			UNICODE_TO_HTML_ENTITY_MAP.put(unicode, htmlElement);
			
			if (htmlElement == "amp")
				HTML_ENTITY_TO_RTF_MAP.put(htmlElement, "&");
			else if (htmlElement == "nbsp")
				HTML_ENTITY_TO_RTF_MAP.put(htmlElement, " ");
			else if (htmlElement == "lt")
				HTML_ENTITY_TO_RTF_MAP.put(htmlElement, "<");
			else if (htmlElement == "gt")
				HTML_ENTITY_TO_RTF_MAP.put(htmlElement, ">");
			else {
				final String unicodeDigits = htmlUnicode.substring(1);		//	to delete the hash
				HTML_ENTITY_TO_RTF_MAP.put(htmlElement, "\\uc1\\u" + unicodeDigits + "*");
			}
		}
	}


	public static String decodeHtml(String s, boolean withParagraphs) {
		if (s == null || s.length() == 0)
			return null;
		
		if (withParagraphs) {
			Pattern newLinePattern = Pattern.compile("\\n");
			Matcher newLineMatcher = newLinePattern.matcher(s);
			s = newLineMatcher.replaceAll(" ");
			
			Pattern newParaPattern = Pattern.compile("\\s*</p>\\s*<p>\\s*");
			Matcher newParaMatcher = newParaPattern.matcher(s);
			s = newParaMatcher.replaceAll("\n");
			
			Pattern orphanParaPattern = Pattern.compile("\\s*</?p>\\s*");
			Matcher orphanParaMatcher = orphanParaPattern.matcher(s);
			s = orphanParaMatcher.replaceAll("");
		}
		
		StringBuilder t = new StringBuilder();
		for (int i=0, n=s.length() ; i<n; i++) {
			char c = s.charAt(i);
			
			if (c == '&') {
				final int j = s.indexOf(';', i);
				if (j >= 0) {
					Character unicode = HTML_ENTITY_TO_UNICODE_MAP.get(s.substring(i + 1, j));

					if (unicode != null) {
						t.append((char) unicode);
						i = j; /* advance index */
						continue;
					}
				}
			}

			t.append(c);
		}
		
		return t.toString();
	}


	public static String translateHtmlToUtf8(String s) {
														//	translates all HTML entities except core ones
		
		if (s == null || s.length() == 0)
			return null;
		
		s = s.replaceFirst("<meta (.*) charset=([^\"']*)([\"']\\s*/?>)", "<meta $1 charset=UTF-8$3");
		
		StringBuilder t = new StringBuilder();
		for (int i=0, n=s.length() ; i<n; i++) {
			char c = s.charAt(i);
			
			if (c == '&') {
				final int j = s.indexOf(';', i);
				if (j >= 0 && !s.substring(i + 1, j).equals("nbsp")) {
					Character unicode = HTML_ENTITY_TO_UNICODE_MAP.get(s.substring(i + 1, j));

					if (unicode != null && (unicode != '"' && unicode != '&' && unicode != '<' && unicode != '>')) {
						t.append((char) unicode);
						i = j; /* advance index */
						continue;
					}
				}
			}

			t.append(c);
		}
		
		return t.toString();
	}
	
	
	public static String convertHtmlToRtf(String s) {
		StringBuilder t = new StringBuilder();
		for (int i=0, n=s.length() ; i<n; i++) {
			char c = s.charAt(i);
			
			if (c == '&') {
				final int j = s.indexOf(';', i);
				if (j >= 0) {
					String rtf = HTML_ENTITY_TO_RTF_MAP.get(s.substring(i + 1, j));

					if (rtf != null) {
						t.append(rtf);
						i = j; /* advance index */
						continue;
					}
				}
			}

			t.append(c);
		}
		
		return t.toString();
	}


	public static String encodeHtml(final String s, boolean withParagraphs) {
		StringBuilder t = new StringBuilder();
		if (withParagraphs)
			t.append("<p>");
		
		for (int i=0, n=s.length() ; i<n ; i++) {
			char c = s.charAt(i);
			
			if (c == '&') {									//	does not encode HTML entities again if already encoded
				int j = s.indexOf(';', i);
				if (j >= 0 && j < i + 8) {							//	assumes code is no more than 7 characters;
					Character unicode = HTML_ENTITY_TO_UNICODE_MAP.get(s.substring(i + 1, j));
					if (unicode != null) {
						for (int k=i ; k<=j ; k++)
							t.append(s.charAt(k));
						i = j;
						continue;
					}
				}
			}
			
			if (withParagraphs && c == '\n')
				t.append("</p>\n<p>");
			else {
				if (withParagraphs && (c == '<' || c == '>'))		//	we don't want to encode tags such as <span> in paragraphs
					t.append(c);
				else {
					if (c == '&')
						t.append("&amp;");
					else if (c == '<')
						t.append("&lt;");
					else if (c == '>')
						t.append("&gt;");
					else if (c == '"')
						t.append("&quot;");
					else
						t.append(c);
					
//					String entity = UNICODE_TO_HTML_ENTITY_MAP.get(c);
//					if (entity == null)
//						t.append(c);
//					else {
//						t.append('&');
//						t.append(entity);
//						t.append(';');
//					}
				}
			}
		}
		
		if (withParagraphs)
			t.append("</p>");

		return t.toString();
	}
}