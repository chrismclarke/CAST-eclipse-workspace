package cast.other;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.regex.*;

import javax.swing.*;

import cast.utils.*;



class FileChanges {
	boolean changed = false;
	String s;
	int svgIndex = 1;
	FileChanges(String s) {
		this.s = s;
	}
}

//	Changes <center><table> to boxed <div>


public class TidyHtml extends JFrame {
	public static final Color kBackgroundColor = new Color(0xeeeeff);
	
	
	static private void replacePattern_all(FileChanges contents, String sourcePattern, String destPattern) {
		replacePattern_all(contents, sourcePattern, destPattern, true);
	}
	
	static private void replacePattern_all(FileChanges contents, String sourcePattern, String destPattern, boolean caseSensitive) {
		int flags = Pattern.DOTALL;
		if (!caseSensitive)
			flags += Pattern.CASE_INSENSITIVE;
		Pattern thePattern = Pattern.compile(sourcePattern, flags);
		Matcher theMatcher = thePattern.matcher(contents.s);
		
		if (theMatcher.find()) {
			contents.changed = true;
			contents.s = theMatcher.replaceAll(destPattern);
		}
	}
	
	static private boolean replacePattern_once(FileChanges contents, String sourcePattern, String destPattern) {
		int flags = Pattern.DOTALL + Pattern.CASE_INSENSITIVE;
		Pattern thePattern = Pattern.compile(sourcePattern, flags);
		Matcher theMatcher = thePattern.matcher(contents.s);
		
		if (theMatcher.find()) {
			contents.changed = true;
			contents.s = theMatcher.replaceFirst(destPattern);
			contents.svgIndex ++;
			return true;
		}
		return false;
	}
	
	static private void replacePattern_repeatedly(FileChanges contents, String sourcePattern, String destPattern) {
		boolean madeChange = false;
		do {
			madeChange = replacePattern_once(contents, sourcePattern, destPattern);
		} while (madeChange);
	}
	
/*
	static boolean recursive = true;
	
	static private String makeChanges(String s) {
			Pattern thePattern = Pattern.compile("(\\s)<table ([^>]*)>(.*?)</table>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher theMatcher = thePattern.matcher(s);
			
			if (!theMatcher.find())
				return;
			
			System.out.println("Found basic match in : " + inFile.getParentFile().getName() + "," + inFile.getName());
			
			String space = theMatcher.group(1);
			String params = theMatcher.group(2);
			String text = theMatcher.group(3);
			
			if (params.indexOf("class=\"centred\"") < 0)
				return null;
			
			if (text.indexOf("<table") >= 0 || text.indexOf("</table>") >= 0)
				return null;
			
			if (text.indexOf("$") >= 0) {
				System.out.println("Error: ***** found $ in table");
				return null;
			}
			
			System.out.println("Full match");
			
			String newS = theMatcher.replaceFirst(space + "<div class=\"centred\"><table " + params + ">" + text + "</table></div>");
//			String newS = theMatcher.replaceAll("</div>\n<br>\n\n<div class=\"centred\"><div class=\"boxed\">$1</div></div>");
		
		if (!newS.equals(s))
			return newS;
		else
			return null;
	}
*/
	
	
//**************    changes to writePageStart() and writePageEnd() at start and end of page, and fixes showNamedPage()
/*
	static boolean recursive = false;
	
	static private String makeChanges(String s) {
		FileChanges contents = new FileChanges(s);
		
		replacePattern_once(contents, "addPageControls",
															"writePageStart");
		replacePattern_once(contents, "top.showNamedPage",
															"showNamedPage");
		replacePattern_once(contents, "<script type=['\"]text/javascript['\"]>\\s*top.writeNextButton\\((document)?\\);\\s*showCopyright\\((document)?\\);\\s*</script>",
															"<script type='text/javascript'>writePageEnd();</script>");
		
		if (changed)
			return s;
		else
			return null;
	}
*/
	
	
//**************
//**************    adds width and height parameters to writeAppletParams()
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_writeAppletParams(FileChanges contents) {
		replacePattern_all(contents, "(<applet [^>]*width=['\"]([0-9]*)[^>]*height=['\"]([0-9]*)[^>]*>\\s*)<script type=['\"]text/javascript['\"]>writeAppletParams\\(\\)",
															"$1<script type=\"text/javascript\">writeAppletParams($2, $3)");
		replacePattern_all(contents, "(<applet [^>]*height=['\"]([0-9]*)[^>]*width=['\"]([0-9]*)[^>]*>\\s*)<script type=['\"]text/javascript['\"]>writeAppletParams\\(\\)",
															"$1<script type=\"text/javascript\">writeAppletParams($3, $2)");
	}

	
//**************
//**************    fix <index> and <dataset> tags
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_index_and_dataset(FileChanges contents) {
		
	//**************    removes </index> and </dataset> tags

		replacePattern_all(contents, "</index\\s*>", "");
		replacePattern_all(contents, "</dataset\\s*>", "");
		
	//**************    moves <index> and <dataset> tags into <head>

		replacePattern_repeatedly(contents, "<head>(.*)</head>(.*?)(<index[^>]*>)\\s*", "<head>$1\n\t$3\n</head>$2");
		replacePattern_repeatedly(contents, "<head>(.*)</head>(.*?)(<dataset[^>]*>)\\s*", "<head>$1\n\t$3\n</head>$2");
		
	//**************    changes <index> and <dataset> tags to have " />" at end
		replacePattern_all(contents, "<index\\s([^>]*[^/])>", "<index $1 />");
		replacePattern_all(contents, "<dataset\\s([^>]*[^/])>", "<dataset $1 />");
		
	//**************    changes <index> and <dataset> tags into <meta> tags
		
		replacePattern_all(contents, "<index\\s*terms=['\"]([,/\\- \\w]*)\"[^>]*>",
															"<meta name=\"index\" content=\"$1\">");
		replacePattern_all(contents, "<dataset\\s*name=['\"]([,/\\- \\w]*)\"[^>]*>",
															"<meta name=\"dataset\" content=\"$1\">");
	}
	
//**************
//**************    reformat
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_reformat(FileChanges contents) {
				// remove blank lines between <meta> tags
		replacePattern_all(contents, "(<meta[^>]*>)\\s*(<meta[^>]*>)", "$1\n\t$2");
				// two blank lines between </head> and <body>
		replacePattern_all(contents, "</head>\\s*<body>", "</head>\n\n\n<body>");
				// no blank lines between <body> and <script> and one blank line after
		replacePattern_all(contents, "<body>\\s*(<script[^>]*>)\\s*writePageStart\\(\\);\\s*</script>\\s*",
																"<body>\n$1writePageStart();</script>\n\n");
	}
	
//**************
//**************    changes for other language versions
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_foreign(FileChanges contents) {
		replacePattern_all(contents, "function changeImage\\(select\\)", "function changeSvgImage(select)");
		replacePattern_all(contents, "changeImage\\(this\\)", "changeSvgImage(this)");
	}
	
//**************
//**************    comment out video controller setup in <head>
/*		format was previously:
	<script src="../structure/videoControls/jquery.js"></script>
	<script src="../structure/videoControls/jquery-ui.js"></script>
	<script src="../structure/videoControls/videoControls.js"></script>
	<link href="../structure/videoControls/combinedStyles.css" rel="stylesheet" type="text/css">
	<script>
		jQuery(function() {
			jQuery('#poDrugScreening').setupVideo();
			jQuery('#alcoholNicotine').setupVideo();
		});
	</script>
*/
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_video2(FileChanges contents) {
		replacePattern_all(contents, "<script src=\"\\.\\./structure/videoControls[^#]*#(\\w*)[^#]*?</script>",
															"<!--   setupVideo1('$1');   -->");
		replacePattern_all(contents, "<script src=\"\\.\\./structure/videoControls[^#]*#(\\w*)[^#]*#(\\w*)[^#]*?</script>",
															"<!--   setupVideo2('$1', '$2');   -->");
		replacePattern_all(contents, "<script src=\"\\.\\./structure/videoControls[^#]*#(\\w*)[^#]*#(\\w*)[^#]*#(\\w*)[^#]*?</script>",
															"<!--   setupVideo3('$1', '$2', '$3');   -->");
	}
	
//**************
//**************    comment out video controller setup in <head>
/*		format was previously:
	<script src="../structure/videoControls/jquery.js"></script>
	<script src="../structure/videoControls/jquery-ui.js"></script>
	<script src="../structure/videoControls/videoControls.js"></script>
	<link href="../structure/videoControls/combinedStyles.css" rel="stylesheet" type="text/css">
	<script>
		jQuery(function() {
			jQuery('#poDrugScreening').setupVideo();
			jQuery('#alcoholNicotine').setupVideo();
		});
	</script>
*/
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_video3(FileChanges contents) {
		replacePattern_all(contents, "<!--   setupVideo[^>]*>",
										"<link rel='stylesheet' href='../structure/videoControls/combinedStyles.css' type='text/css'>\n"
										+ "\t<script src='../structure/environment.js'></script>\n"
										+ "\t<script src='../structure/videoControls/jquery.js'></script>\n"
										+ "\t<script src='../structure/videoControls/jquery-ui.js'></script>\n"
										+ "\t<script src='../structure/videoControls/videoControls.js'></script>");
	}
	
//**************
//**************    remove meta tags for data sets in video files <head>
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_video4(FileChanges contents) {
		replacePattern_all(contents, "\\s*<meta name=\"dataset\"[^>]*>", "");
	}
	
//**************
//**************    remove meta tags for data sets in video files <head>
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_videoBody(FileChanges contents) {
		replacePattern_all(contents, "<body>", "<body class=\"video\">");
	}
	
//**************
//**************    changes many "symbol.xxx.gif" images to unicode symbols
//**************    changes remaining "symbol.xxx.gif" images to "symbol.xxx.png"
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_symbols(FileChanges contents) {
				// change -- to long dash
		replacePattern_all(contents, " -- ", " &mdash; ");
				// change -> to word
		replacePattern_all(contents, " -> ", " to ");
		
		replacePattern_all(contents, "symbol.xbar.gif", "symbol.xBar.gif", true);
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.mu1.gif\"[^>]*>", "&mu;<sub>1</sub>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.mu2.gif\"[^>]*>", "&mu;<sub>2</sub>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.approxEq.gif\"[^>]*>", "&asymp;");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.muDiff.gif\"[^>]*>", "<span class=\"black\">&mu;<sub>2</sub>&nbsp;&minus;&nbsp;&mu;<sub>1</sub></span>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.x.gif\"[^>]*>", "<em>x</em>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.s.gif\"[^>]*>", "<em>s</em>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.sigma.gif\"[^>]*>", "&sigma;");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.pxCondit.gif\"[^>]*>", "<span class=\"black\"><em>p</em><sub><em>x</em>&nbsp;|&nbsp;<em>y</em></sub></span>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.pyCondit.gif\"[^>]*>", "<span class=\"black\"><em>p</em><sub><em>y</em>&nbsp;|&nbsp;<em>x</em></sub></span>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.sigmaPDiff.gif\"[^>]*>", "<span class=\"green\">&sigma;<sub><em>p</em><sub>2</sub>&minus;<em>p</em><sub>1</sub></sub></span>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.piDiffBlue.gif\"[^>]*>", "<span class=\"blue\">&pi;<sub>2</sub>&minus;&pi;<sub>1</sub></span>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.pDiffRed.gif\"[^>]*>", "<span class=\"red\"><em>p</em><sub>2</sub>&minus;<em>p</em><sub>1</sub></span>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.xiyi.gif\"[^>]*>", "(<em>x<sub>i</sub></em>,&nbsp;<em>y<sub>i</sub></em>)");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.epsilon.gif\"[^>]*>", "&epsilon;");
		
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.xBar.gif\"[^>]*>", "<img src=\"../images/symbol.xBar.png\" width=\"10\" height=\"10\" align=\"baseline\">");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.xBarDiff.gif\"[^>]*>", "<span style=\"position:relative; top:5px\"><img src=\"../images/symbol.xBarDiff.png\" width=\"36\" height=\"15\" align=\"baseline\"></span>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.xBarDiffRed.gif\"[^>]*>", "<span style=\"position:relative; top:5px\"><img src=\"../images/symbol.xBarDiffRed.png\" width=\"36\" height=\"15\" align=\"baseline\"></span>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.xHat.gif\"[^>]*>", "<img src=\"../images/symbol.xHat.png\" width=\"10\" height=\"13\" align=\"baseline\">");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.xPlusMn.gif\"[^>]*>", "<span style=\"position:relative; top:3px\"><img src=\"../images/symbol.xPlusMn.png\" width=\"61\" height=\"14\" align=\"baseline\"></span>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.xTwiddle.gif\"[^>]*>", "<img src=\"../images/symbol.xTwiddle.png\" width=\"11\" height=\"13\" align=\"baseline\">");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.yBar.gif\"[^>]*>", "<span style=\"position:relative; top:4px\"><img src=\"../images/symbol.yBar.png\" width=\"10\" height=\"14\" align=\"baseline\"></span>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.yBarCap.gif\"[^>]*>", "<img src=\"../images/symbol.yBarCap.png\" width=\"11\" height=\"15\" align=\"baseline\">");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.yHat.gif\"[^>]*>", "<span style=\"position:relative; top:4px\"><img src=\"../images/symbol.yHat.png\" width=\"10\" height=\"17\" align=\"baseline\"></span>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.yiBar.gif\"[^>]*>", "<span style=\"position:relative; top:4px\"><img src=\"../images/symbol.yiBar.png\" width=\"12\" height=\"14\" align=\"baseline\"></span>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.yiHat.gif\"[^>]*>", "<span style=\"position:relative; top:4px\"><img src=\"../images/symbol.yiHat.png\" width=\"11\" height=\"18\" align=\"baseline\"></span>");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.sigmaHat.gif\"[^>]*>", "<img src=\"../images/symbol.sigmaHat.png\" width=\"9\" height=\"14\" align=\"baseline\">");
		replacePattern_all(contents, "<img\\s*src=\"../images/symbol.sdDiffGreen.gif\"[^>]*>", "<span style=\"position:relative; top:7px\"><img src=\"symbol.sdDiffGreen.png\" width=\"42\" height=\"16\" align=\"baseline\"></span>");
	}
	
	
	
//**************
//**************    Adds <DOCTYPE> comment to start of HTML files
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_docType(FileChanges contents) {
		replacePattern_once(contents, "^<html>", "<!DOCTYPE HTML>\n<html>");
	
		replacePattern_once(contents, "charset=UTF-8\">(\\s*)<link", "charset=UTF-8\">$1<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">$1<link");
}

	
	
	static final private void swapGifForPng(FileChanges contents, String coreName) {
		String sourcePattern = "(<img\\s*img\\s*src=\"[^>]*/)" + coreName + ".gif(\"[^>]*)>";
		String destPattern = "$1" + coreName + ".png$2>";
		replacePattern_all(contents, sourcePattern, destPattern);
		
		replacePattern_all(contents, "images/" + coreName + ".gif", "images/" + coreName + ".png");
	}
	
	static final private void enableSvgImage(FileChanges contents, String coreName) {
		boolean finished = false;		//	makes sure that contents.svgIndex is incremented for each image
		while (!finished) {
			String sourcePattern1 = "<img\\s*src=\"([^>]*/" + coreName + ").gif\"[^>]*width=\"(\\d*)\"[^>]*height=\"(\\d*)\"[^>]*>";
			String destPattern1 = "<img class=\"gif\" src=\"$1.gif\" id=\"gif_image_" + contents.svgIndex + "\" width=\"$2\" height=\"$3\"><iframe class=\"svg\" src=\"$1.svg\" id=\"svg_image_" + contents.svgIndex + "\" width=\"$2\" height=\"$3\" frameborder=\"0\"></iframe><script type=\"text/javascript\">showCorrectImage(\"image_" + contents.svgIndex + "\");</script>";
			
			String sourcePattern2 = "<img\\s*src=\"([^>]*/" + coreName + ").gif\"[^>]*height=\"(\\d*)\"[^>]*width=\"(\\d*)\"[^>]*>";
			String destPattern2 = "<img class=\"gif\" src=\"$1.gif\" id=\"gif_image_" + contents.svgIndex + "\" width=\"$3\" height=\"$2\"><iframe class=\"svg\" src=\"$1.svg\" id=\"svg_image_" + contents.svgIndex + "\" width=\"$3\" height=\"$2\" frameborder=\"0\"></iframe><script type=\"text/javascript\">showCorrectImage(\"image_" + contents.svgIndex + "\");</script>";
			
			finished = !replacePattern_once(contents, sourcePattern1, destPattern1)
																				&& !replacePattern_once(contents, sourcePattern2, destPattern2);
		}
	}
	
//**************
//**************    changes "gif" images to "png", "svg" or HTML
//**************

	@SuppressWarnings("unused")
	static private void makeChanges_gifs(FileChanges contents) {
		
//***	HbivarCat
		enableSvgImage(contents, "propnVennExplan");
		enableSvgImage(contents, "perspectiveBar");
		enableSvgImage(contents, "xMarginEqn");
		enableSvgImage(contents, "yMarginEqn");
		
//***	HboxPlot
		enableSvgImage(contents, "boxPlotValues");
		enableSvgImage(contents, "histoQuantiles");
		enableSvgImage(contents, "lqRank");
		enableSvgImage(contents, "medianRank");
		enableSvgImage(contents, "outlierDefn");
	
//***	Hcausal
		replacePattern_all(contents, "\"images/causal1.gif\"", "\"../Hcausal/images/reln1.gif\"");		//	same image was called "causal1.gif" in Hrelationship
		replacePattern_all(contents, "\"images/causal2.gif\"", "\"../Hcausal/images/reln2.gif\"");		//	same image was called "causal2.gif" in Hrelationship
		replacePattern_all(contents, "\"images/causal3.gif\"", "\"../Hcausal/images/reln3.gif\"");		//	same image was called "causal3.gif" in Hrelationship
		enableSvgImage(contents, "cabbageDisease");
		enableSvgImage(contents, "cancerChurch");
		enableSvgImage(contents, "reln1");
		enableSvgImage(contents, "reln2");
		enableSvgImage(contents, "reln3");
		enableSvgImage(contents, "smoking");
		
//***	HcenterSpread
		enableSvgImage(contents, "anscombe1");
		enableSvgImage(contents, "anscombe2");
		enableSvgImage(contents, "anscombe3");
		enableSvgImage(contents, "anscombe4");
		enableSvgImage(contents, "anscombeAll");
		enableSvgImage(contents, "centerSpreadIdea");
		enableSvgImage(contents, "deviations");
		enableSvgImage(contents, "deviations2");
		enableSvgImage(contents, "differentCentres_b");
		enableSvgImage(contents, "differentCentres");
		enableSvgImage(contents, "differentSpreads_b");
		enableSvgImage(contents, "differentSpreads");
		enableSvgImage(contents, "guessSD");
		enableSvgImage(contents, "iqrAndHisto");
		enableSvgImage(contents, "medianProperties");
		enableSvgImage(contents, "sunshineHours");
		enableSvgImage(contents, "meanEqn");
		enableSvgImage(contents, "sdDefn");
		enableSvgImage(contents, "ssqDevns");
		
//***	HciExtra
		replacePattern_all(contents, "HciExtra/images/ci.gif", "HciMean/images/ci.gif");		//	later replaced by SVG version
		replacePattern_all(contents, "HciExtra/images/ciK.gif", "HciMean/images/ciK.gif");		//	later replaced by SVG version
		enableSvgImage(contents, "ciLevel");
		replacePattern_all(contents, "HciExtra/images/ciP.gif", "HestPropn/images/ciP.gif");		//	later replaced by SVG version
		enableSvgImage(contents, "marginOfError");
		enableSvgImage(contents, "maxSE");
		enableSvgImage(contents, "meanInequality");
		enableSvgImage(contents, "meanInequality2");
		enableSvgImage(contents, "nForMean");
		enableSvgImage(contents, "nForPropn");
		replacePattern_all(contents, "HciExtra/images/plusMinusTSE.gif", "HciMean/images/plusMinusTSE.gif");		//	later replaced by SVG version
		enableSvgImage(contents, "propInequality");
		enableSvgImage(contents, "seP");
		
//***	HciMean
		enableSvgImage(contents, "bad95CI");
		enableSvgImage(contents, "ci");
		enableSvgImage(contents, "ciK");
		enableSvgImage(contents, "ciApproxEqn");
		enableSvgImage(contents, "ciByEye");
		enableSvgImage(contents, "errorBounds");
		enableSvgImage(contents, "errorBounds2");
		enableSvgImage(contents, "pagesPrinted");
		enableSvgImage(contents, "samaru");
		enableSvgImage(contents, "plusMinusTSE");
		enableSvgImage(contents, "silkworm");
		replacePattern_all(contents, "<img[^>]*images/errorDefn.gif\"[^>]*>", "<span class=\"black\"><em>error</em>&nbsp; =&nbsp; <img src=\"../images/symbol.xBar.png\" width=\"10\" height=\"10\" align=\"baseline\"> &minus; &mu;</span>");
		replacePattern_all(contents, "<img[^>]*images/errorDistn.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-12px\"><em>error</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">normal</font> (0, &nbsp;</span><img src=\"../images/symbol.sigmaOverRootN.png\" width=\"26\" height=\"31\" align=\"baseline\"><span style=\"position:relative; top:-12px\">)</span></span>");
		replacePattern_all(contents, "<img[^>]*images/errorDistn2.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-12px\"><em>error</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">normal</font> (0, &nbsp;</span><img src=\"../images/symbol.sOverRootN.png\" width=\"26\" height=\"31\" align=\"baseline\"><span style=\"position:relative; top:-12px\">)</span></span>");
		replacePattern_all(contents, "<img[^>]*images/concDistn.gif\"[^>]*>", "<em>X</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">normal</font> (&mu;&nbsp;, &sigma; = 0.0068)");
		replacePattern_all(contents, "<img[^>]*images/sugarDistn.gif\"[^>]*>", "<em>X</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">normal</font> (&mu;&nbsp;, &sigma; = 3)");
		replacePattern_all(contents, "<img[^>]*images/se.gif\"[^>]*>", "<span style=\"position:relative; top:12px\"><img src=\"../images/symbol.sigmaOverRootN.png\" width=\"26\" height=\"31\" align=\"baseline\"></span>");
		replacePattern_all(contents, "<img[^>]*images/muHatEqualsXBar.gif[^>\"]*>", "<span style=\"position:relative; top:4px\"><img src=\"../images/symbol.muHat.png\" width=\"9\" height=\"18\" align=\"baseline\"></span> <span class=\"black\">=</span> <img src=\"../images/symbol.xBar.png\" width=\"10\" height=\"10\" align=\"baseline\">");
		
//***	Hcontext
		enableSvgImage(contents, "feedbackProcess");
		enableSvgImage(contents, "pdca");
		enableSvgImage(contents, "simpleProcess");
		replacePattern_all(contents, "<img[^>]*images/purpose.gif\"[^>]*>", "<p><font size=\"+3\"><strong><font color=\"#CC0000\">What is the purpose of Statistics?</font></strong></font></p>");

//***	HcontrolChart
		enableSvgImage(contents, "bands");
		enableSvgImage(contents, "pooledSDEqn");
		enableSvgImage(contents, "pooledSDEqn2");
		enableSvgImage(contents, "sdConstantEqn");
		enableSvgImage(contents, "sdConstantEqn2");
		replacePattern_all(contents, "<img[^>]*images/plusMinus3SD.gif\"[^>]*>", "<img src=\"../images/symbol.xBar.png\" width=\"10\" height=\"10\" align=\"baseline\"> <span class=\"black\">&nbsp;&plusmn;&nbsp; 3<em>s</em></span>");
		replacePattern_all(contents, "<img[^>]*images/plusMinus3SDMean.gif\"[^>]*>", "<span style=\"position:relative; top:-12px\"><img src=\"../images/symbol.xBar.png\" width=\"10\" height=\"10\" align=\"baseline\"> <span class=\"black\">&nbsp;&plusmn;&nbsp; 3</span></span><img src=\"../images/symbol.sOverRootN.png\" width=\"26\" height=\"31\" align=\"baseline\">");
		
//***	Hcorrelation
		enableSvgImage(contents, "corrDefn");
		enableSvgImage(contents, "corrDefn2");
		enableSvgImage(contents, "property1");
		enableSvgImage(contents, "property2");
		enableSvgImage(contents, "property3");
		enableSvgImage(contents, "property4");
		enableSvgImage(contents, "property5");
		replacePattern_all(contents, "<img[^>]*images/property6.gif\"[^>]*>", "<span class=\"black\">&minus;1 &nbsp;&le;&nbsp; <em>r</em> &nbsp;&le;&nbsp; +1</span>");
		replacePattern_all(contents, "<img[^>]*images/rLimits.gif\"[^>]*>", "<span class=\"black\">&minus;1 &nbsp;&le;&nbsp; <em>r</em> &nbsp;&le;&nbsp; +1</span>");
		enableSvgImage(contents, "standardiseX");
		enableSvgImage(contents, "standardMeanSD");
		enableSvgImage(contents, "zXEqn");
		enableSvgImage(contents, "zYEqn");

//***	Hcounts
		enableSvgImage(contents, "spreadsheet");
		enableSvgImage(contents, "discreteHisto");
		enableSvgImage(contents, "discreteHisto2");
		enableSvgImage(contents, "meanCalc");
		enableSvgImage(contents, "meanCountEqn");
		enableSvgImage(contents, "ssqEqn");

//***	Hcurvature
		enableSvgImage(contents, "extrapolate");
		enableSvgImage(contents, "residSsqEqn");
		replacePattern_all(contents, "<img[^>]*images/quadraticEqn.gif\"[^>]*>", "<span class=\"black\"><em>y</em>&nbsp;&nbsp;=&nbsp;&nbsp;<em>b</em><sub>0</sub>&nbsp;&nbsp;+&nbsp;&nbsp;<em>b</em><sub>1 </sub><em>x</em>&nbsp;&nbsp;+&nbsp;&nbsp;<em>b</em><sub>2 </sub><em>x</em><sup>2</sup></span>");
		replacePattern_all(contents, "<img[^>]*images/quadPredictEqn.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:4\"><img src=\"../images/symbol.yiHat.png\" width=\"11\" height=\"18\" align=\"baseline\"></span>&nbsp;&nbsp;=&nbsp;&nbsp;<em>b</em><sub>0</sub>&nbsp;&nbsp;+&nbsp;&nbsp;<em>b</em><sub>1 </sub><em>x<sub>i</sub></em>&nbsp;&nbsp;+&nbsp;&nbsp;<em>b</em><sub>1 </sub><em>x<sub>i</sub></em><sup>2</sup></span>");
		replacePattern_all(contents, "<img[^>]*images/pressureEstEqn3.gif\"[^>]*>", "<strong>estimate of pressure</strong>&nbsp;&nbsp;=&nbsp;&nbsp;116.59 &minus; 1.4165 <em>x</em> + 0.004752 <em>x</em><sup>2</sup>");
		replacePattern_all(contents, "<img[^>]*images/pressureEstEqn.gif\"[^>]*>", "<strong>estimate of </strong>ln<strong> (pressure)</strong>&nbsp;&nbsp;=&nbsp;&nbsp;&minus;0.9518 + 0.02052 <em>x</em>");
		replacePattern_all(contents, "<img[^>]*images/pressureEstEqn2.gif\"[^>]*>", "<strong>estimate of pressure</strong>&nbsp;&nbsp;=&nbsp;&nbsp;exp( &minus;0.9518 + 0.02052 <em>x</em> )");
		replacePattern_all(contents, "<img[^>]*images/gdpQuadEqn.gif\"[^>]*>", "<strong>estimate of GDP</strong>&nbsp;&nbsp;=&nbsp;&nbsp;2562.6 &minus; 74.913 <em>t</em> + 2.0851 <em>t</em><sup>2</sup>");
		replacePattern_all(contents, "<img[^>]*images/logGdpEqn.gif\"[^>]*>", "<strong>estimate of </strong>ln<strong> (GDP)</strong>&nbsp;&nbsp;=&nbsp;&nbsp;7.851 + 0.03166 <em>t</em>");
		replacePattern_all(contents, "<img[^>]*images/logGdpEqn2.gif\"[^>]*>", "<strong>estimate of GDP</strong>&nbsp;&nbsp;=&nbsp;&nbsp;exp( 7.851 + 0.03166 <em>t</em> )");
		
//***	Hcyclic
		enableSvgImage(contents, "arForecastPlot");
		enableSvgImage(contents, "yAgainstPrevious");
		replacePattern_all(contents, "<img[^>]*images/arModel.gif\"[^>]*>", "<span class=\"black\"><em>y<sub>t</sub></em>&nbsp;&nbsp;=&nbsp;&nbsp;<em>b</em><sub>0</sub>&nbsp;&nbsp;+&nbsp;&nbsp;<em>b</em><sub>1 </sub><em>y<sub>t</sub></em><sub>&minus;1</sub></span>");
		replacePattern_all(contents, "<img[^>]*images/arPredictEqn.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:4px\"><img src=\"../images/symbol.yHat.png\" width=\"10\" height=\"17\"></span><em><sub>t</sub></em>&nbsp;&nbsp;=&nbsp;&nbsp;<em>b</em><sub>0</sub>&nbsp;&nbsp;+&nbsp;&nbsp;<em>b</em><sub>1 </sub><em>y<sub>t</sub></em><sub> &minus; 1</sub></span>");
		replacePattern_all(contents, "<img[^>]*images/arPredict2Eqn.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:4px\"><img src=\"../images/symbol.yHat.png\" width=\"10\" height=\"17\"></span><em><sub>t</sub></em><sub> + </sub><em><sub>k</sub></em>&nbsp;&nbsp;=&nbsp;&nbsp;<em>b</em><sub>0</sub>&nbsp;&nbsp;+&nbsp;&nbsp;<em>b</em><sub>1 </sub><em><span style=\"position:relative; top:4px\"><img src=\"../images/symbol.yHat.png\" width=\"10\" height=\"17\"></span><sub>t</sub></em><sub> + <em>k </em>&minus;1</sub></span>");
		
//***	Hdecision
		enableSvgImage(contents, "pErrorAlt");
		enableSvgImage(contents, "pErrorNull");
		replacePattern_all(contents, "<img[^>]*images/hypothC.gif\"[^>]*>", "<strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> = 15%</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &gt; 15%</strong>");
		replacePattern_all(contents, "<img[^>]*images/hypothMean10.gif\"[^>]*>", "<strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> = 10</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &gt; 10</strong>");
		replacePattern_all(contents, "<img[^>]*images/hypothOneTail.gif\"[^>]*>", "<strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong>&nbsp;&nbsp;=&nbsp;&nbsp;</strong>&mu;<sub>0</sub><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong>&nbsp;&nbsp;&gt;&nbsp;&nbsp;</strong>&mu;<sub>0</sub>");
		replacePattern_all(contents, "<img[^>]*images/meanDistn.gif\"[^>]*>", "<span style=\"position:relative; top:-12px\"><img src=\"../images/symbol.xBarCap.png\" width=\"11\" height=\"15\" align=\"baseline\">&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">normal</font> (&mu;, &nbsp;</span><img src=\"../images/symbol.sigmaOverRootN.png\" width=\"26\" height=\"31\" align=\"baseline\"><span style=\"position:relative; top:-12px\"> = 1)</span>");
		
//***	Hdensity
		enableSvgImage(contents, "ageHistoCanopy");
		enableSvgImage(contents, "ageHistoRelFreq");
		enableSvgImage(contents, "freqTable");
		enableSvgImage(contents, "freqTable2");
		enableSvgImage(contents, "freqTable3");
		enableSvgImage(contents, "freqTable4");
		enableSvgImage(contents, "freqTable5");
		enableSvgImage(contents, "kernels");
		enableSvgImage(contents, "oneValArea");
		enableSvgImage(contents, "popnPyramids");
		enableSvgImage(contents, "densityDefn");

//***	HdesignIntro
		enableSvgImage(contents, "confounded_c3");
		enableSvgImage(contents, "confounded_c4");
		enableSvgImage(contents, "confounded1");
		enableSvgImage(contents, "confounded2");
		enableSvgImage(contents, "confounded3");
		enableSvgImage(contents, "confounded4");
		enableSvgImage(contents, "lurking2");
		enableSvgImage(contents, "lurking3");
		enableSvgImage(contents, "replication1");

//***	HdisplayInterp
		enableSvgImage(contents, "aircraftCO2");
		enableSvgImage(contents, "coffeeStemLeaf");
		enableSvgImage(contents, "friesianVigour");
		enableSvgImage(contents, "mvisDotPlot");
		enableSvgImage(contents, "rainDaysStemLeaf");
		enableSvgImage(contents, "rentalDotPlot");
		enableSvgImage(contents, "slagStemLeaf");
		enableSvgImage(contents, "stormDuration");
		enableSvgImage(contents, "stormDurationStemLeaf");
		enableSvgImage(contents, "techSupStemLeaf");
		enableSvgImage(contents, "windSpeed");
		enableSvgImage(contents, "yamStemLeaf");
		enableSvgImage(contents, "outlierSkew");
		enableSvgImage(contents, "outlierPossible");
		enableSvgImage(contents, "outlierSymmetric");

//***	HestIntro
		enableSvgImage(contents, "assetLiabilitiesMean");
		enableSvgImage(contents, "confidence");
		enableSvgImage(contents, "equalMeans");
		enableSvgImage(contents, "errorDistnDiagram");
		enableSvgImage(contents, "estExample");
		enableSvgImage(contents, "estimatorComparison1");
		enableSvgImage(contents, "estimatorComparison2");
		enableSvgImage(contents, "samaruMean");
		enableSvgImage(contents, "silkwormMean");
		replacePattern_all(contents, "<img[^>]*images/pHivEst.gif\"[^>]*>", "<img src=\"../images/symbol.piHat.png\" width=\"9\" height=\"15\" align=\"baseline\">&nbsp;&nbsp;=&nbsp;&nbsp;<em>p</em>&nbsp;&nbsp;=&nbsp;&nbsp;<sup>1883</sup>/<sub>4955</sub>&nbsp;&nbsp;=&nbsp;&nbsp;0.380");
		replacePattern_all(contents, "<img[^>]*images/pSorghumEst.gif\"[^>]*>", "<img src=\"../images/symbol.piHat.png\" width=\"9\" height=\"15\" align=\"baseline\">&nbsp;&nbsp;=&nbsp;&nbsp;<em>p</em>&nbsp;&nbsp;=&nbsp;&nbsp;<sup>37</sup>/<sub>100</sub>&nbsp;&nbsp;=&nbsp;&nbsp;0.37");
		replacePattern_all(contents, "<img[^>]*images/meanEwesEst.gif\"[^>]*>", "<span style=\"position:relative; top:4px\"><img src=\"../images/symbol.muHat.png\" width=\"9\" height=\"18\" align=\"baseline\"></span>&nbsp;&nbsp;=&nbsp;&nbsp;<img src=\"../images/symbol.xBar.png\" width=\"10\" height=\"10\" align=\"baseline\">&nbsp;&nbsp;=&nbsp;&nbsp;263.6 <em>ewes</em>");
		replacePattern_all(contents, "<img[^>]*images/meanSorghumEst.gif\"[^>]*>", "<span style=\"position:relative; top:4px\"><img src=\"../images/symbol.muHat.png\" width=\"9\" height=\"18\" align=\"baseline\"></span>&nbsp;&nbsp;=&nbsp;&nbsp;<img src=\"../images/symbol.xBar.png\" width=\"10\" height=\"10\" align=\"baseline\">&nbsp;&nbsp;=&nbsp;&nbsp;0.68 <em>hectares</em>");
		
//***	HestOther
		enableSvgImage(contents, "assetLiabilitiesLQ");
		enableSvgImage(contents, "assetLiabilitiesModel");
		enableSvgImage(contents, "samaruLQ");
		enableSvgImage(contents, "samaruModel");
		enableSvgImage(contents, "samaruOctUQ");
		enableSvgImage(contents, "silkwormModel");
		enableSvgImage(contents, "silkwormUQ");
		replacePattern_all(contents, "<img[^>]*images/propnSe.gif\"[^>]*>", "<span style=\"position:relative; top:-14px\"><span class=\"black\"><em>standard error</em>&nbsp;&nbsp;=&nbsp;&nbsp;</span></span><img src=\"../images/symbol.seP.png\" width=\"80\" height=\"44\" align=\"baseline\">");
		replacePattern_all(contents, "<img[^>]*images/riceBiasSe.gif\"[^>]*>", "<span style=\"position:relative; top:-14px\"><em>bias</em>&nbsp; =&nbsp; 0<br><em>standard error</em>&nbsp; =&nbsp; </span><img src=\"../images/symbol.seP.png\" width=\"80\" height=\"44\" align=\"baseline\"><span style=\"position:relative; top:-14px\">&nbsp; =&nbsp; 0.0832</span>");
		replacePattern_all(contents, "<img[^>]*images/riceBinom.gif\"[^>]*>", "<span class=\"black\"><em>X</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">binomial</font> (<em>n</em> = 36, &nbsp;&pi;)</span>");
																													//	also changes same gifs in HestPropn
		
//***	HestPropn
		enableSvgImage(contents, "errorInPBounds_b");
		enableSvgImage(contents, "errorInPBounds_c");
		enableSvgImage(contents, "errorInPBounds");
		enableSvgImage(contents, "ciP");
		enableSvgImage(contents, "estErrorSD");
		enableSvgImage(contents, "pEqn");
		replacePattern_all(contents, "<img[^>]*images/propnSe.gif\"[^>]*>", "<span style=\"position:relative; top:-14px\"><span class=\"black\"><em>standard error</em>&nbsp;&nbsp;=&nbsp;&nbsp;</span></span><img src=\"../images/symbol.seP.png\" width=\"80\" height=\"44\" align=\"baseline\">");
		replacePattern_all(contents, "<img[^>]*images/binom.gif\"[^>]*>", "<span class=\"black\"><em>X</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">binomial</font> (<em>n</em>, &nbsp;&pi;)</span>");
		replacePattern_all(contents, "images/propnErrorNormal.gif", "images/errorApproxDistn.gif");			//		then replaced by HTML code below
		replacePattern_all(contents, "<img[^>]*images/errorApproxDistn.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-14px\"><em>error</em> &nbsp;=&nbsp; <em>p</em> &minus; &pi; &nbsp;~&nbsp; <font face=\"Arial, Helvetica, sans-serif\">normal</font> (0, </span><img src=\"../images/symbol.sePi.png\" width=\"89\" height=\"46\"><span style=\"position:relative; top:-14px\"> )</span></span>");
		replacePattern_all(contents, "<img[^>]*images/estSeP.gif\"[^>]*>", "<span style=\"position:relative; top:14px\"><img src=\"../images/symbol.seP.png\" width=\"80\" height=\"44\" align=\"baseline\"></span>");
		replacePattern_all(contents, "<img[^>]*images/estSeP2.gif\"[^>]*>", "2 &times; <span style=\"position:relative; top:14px\"><img src=\"../images/symbol.seP.png\" width=\"80\" height=\"44\" align=\"baseline\"></span>");
		replacePattern_all(contents, "<img[^>]*images/fruitFlyBinom.gif\"[^>]*>", "<span class=\"black\"><em>X</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">binomial</font> (<em>n</em> = 5903, &nbsp;&pi;)</span>");
		replacePattern_all(contents, "<img[^>]*images/meanPEqn.gif\"[^>]*>", "<span class=\"black\">&mu;<sub><em>p</em></sub> &nbsp;=&nbsp; &pi;</span>");
		replacePattern_all(contents, "<img[^>]*images/propnBiasSe.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-14px\"><em>bias</em>&nbsp; =&nbsp; &mu;<sub>error</sub>&nbsp; =&nbsp; 0<br><em>standard error</em>&nbsp; =&nbsp; &sigma;<sub>error</sub>&nbsp; =&nbsp; </span><img src=\"../images/symbol.sePi.png\" width=\"89\" height=\"46\"></span>");
		replacePattern_all(contents, "<img[^>]*images/propnBiasSe2.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-14px\"><em>bias</em>&nbsp; =&nbsp; &mu;<sub>error</sub>&nbsp; =&nbsp; 0<br><em>standard error</em>&nbsp; =&nbsp; &sigma;<sub>error</sub>&nbsp; =&nbsp; </span><img src=\"../images/symbol.seP.png\" width=\"80\" height=\"44\"></span>");
		replacePattern_all(contents, "<img[^>]*images/sdPEqn.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-14px\">&sigma;<sub><em>p</em></sub>&nbsp; =&nbsp; </span><img src=\"../images/symbol.sePi.png\" width=\"89\" height=\"46\"></span>");
		replacePattern_all(contents, "<img[^>]*images/successionBinom.gif\"[^>]*>", "<span class=\"black\"><em>X</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">binomial</font> (<em>n</em> = 210, &nbsp;&pi;)</span>");
		
//***	HfreqTable
		enableSvgImage(contents, "europeResponseRate");
		enableSvgImage(contents, "unRecipients");

//***	Hindep
		enableSvgImage(contents, "assocVenn");
		enableSvgImage(contents, "assocVenn2");
		enableSvgImage(contents, "chi2PValue");
		enableSvgImage(contents, "expCount");
		enableSvgImage(contents, "expCount2");
		enableSvgImage(contents, "indepVenn");
		enableSvgImage(contents, "expCountEqn");
		enableSvgImage(contents, "expCountEx");
		enableSvgImage(contents, "expCountEx2");
		enableSvgImage(contents, "chiSquaredEqn");
		replacePattern_all(contents, "<img[^>]*images/indepHypoth.gif\"[^>]*>", "<span class=\"black\"><strong><font size=\"+1\">H</font><sub>0</sub> :</strong>&nbsp; <em>X and Y are independent</em><br><strong><font size=\"+1\">H</font><sub>A</sub> :</strong>&nbsp; <em>X and Y are dependent</em>&nbsp;&nbsp;</span>");
		replacePattern_all(contents, "<img[^>]*images/jointIndepProbs.gif\"[^>]*>", "<span class=\"black\"><em>p<sub>xy</sub></em>&nbsp; = &nbsp;<em>p<sub>x</sub></em> &times; <em>p<sub>y</sub></em>");
		enableSvgImage(contents, "ssqErrorForm");
		
//***	HindexNos
		enableSvgImage(contents, "brent_graph");
		enableSvgImage(contents, "fish_graph");
		enableSvgImage(contents, "gdp_graph");
		enableSvgImage(contents, "brent_2001");
		enableSvgImage(contents, "brent_2002");
		enableSvgImage(contents, "brent_newBase");
		enableSvgImage(contents, "changeBase");
		enableSvgImage(contents, "deflateEqn");
		enableSvgImage(contents, "fish_2006");
		enableSvgImage(contents, "fish_deflate");
		enableSvgImage(contents, "fish_laspeyres");
		enableSvgImage(contents, "fish_laspeyres2");
		enableSvgImage(contents, "gdp_2003");
		enableSvgImage(contents, "laspeyresIndex");
		enableSvgImage(contents, "simpleIndex");
		enableSvgImage(contents, "unweightedIndex");

//***	HleastSqrs
		enableSvgImage(contents, "predictAndInverse");
		replacePattern_all(contents, "<img[^>]*images/exampleEqn.gif\"[^>]*>", "<span class=\"black\"><em>y</em> &nbsp;=&nbsp; 2.0 + 0.4 <em>x</em></span>");
		replacePattern_all(contents, "<img[^>]*images/fittedValEqn.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:4px\"><img src=\"../images/symbol.yiHat.png\" width=\"11\" height=\"18\" align=\"baseline\"></span>&nbsp;=&nbsp; <em>b</em><sub>0</sub> + <em>b</em><sub>1 </sub><em>x<sub>i</sub></em></span>");
		replacePattern_all(contents, "<img[^>]*images/linearEqn.gif\"[^>]*>", "<span class=\"black\"><em>y</em> &nbsp;=&nbsp; <em>b</em><sub>0</sub> + <em>b</em><sub>1 </sub><em>x</em></span>");
		enableSvgImage(contents, "lsFormulae");
		replacePattern_all(contents, "<img[^>]*images/predictionEqn.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:4px\"><img src=\"../images/symbol.yHat.png\" width=\"10\" height=\"17\" align=\"baseline\"></span>&nbsp; =&nbsp; <em>b</em><sub>0</sub> + <em>b</em><sub>1 </sub><em>x</em></span>");
		replacePattern_all(contents, "<img[^>]*images/residEqn.gif\"[^>]*>", "<span class=\"black\"><em>e<sub>i</sub></em>&nbsp;&nbsp;=&nbsp;&nbsp;<em>y<sub>i</sub></em> &minus; <span style=\"position:relative; top:4\"><img src=\"../images/symbol.yiHat.png\" width=\"11\" height=\"18\" align=\"baseline\"></span></span>");
		enableSvgImage(contents, "residSsq");
		replacePattern_all(contents, "<img[^>]*images/xyEquation.gif\"[^>]*>", "<span class=\"black\"><em>x</em> &nbsp;=&nbsp; <em>c</em><sub>0</sub> + <em>c</em><sub>1 </sub><em>y</em></span>");
		enableSvgImage(contents, "xyLsLine");
		replacePattern_all(contents, "<img[^>]*images/yEqualsFX.gif\"[^>]*>", "<span class=\"black\"><em>y</em> &nbsp;=&nbsp; <em>&fnof;</em> (<em> x</em> )</span>");
		replacePattern_all(contents, "<img[^>]*images/yxEquation.gif\"[^>]*>", "<span class=\"black\"><em>y</em> &nbsp;=&nbsp; <em>b</em><sub>0</sub> + <em>b</em><sub>1 </sub><em>x</em></span>");
		enableSvgImage(contents, "yxLsLine");
		
//***	Hlogistic
		enableSvgImage(contents, "incomeBarBox");
		enableSvgImage(contents, "incomeEducBox");
		enableSvgImage(contents, "incomePensionStacked");
		enableSvgImage(contents, "lizardBox");
		enableSvgImage(contents, "lizardStacked");
		enableSvgImage(contents, "ratBarBox");
		enableSvgImage(contents, "linearPredictEqn");
		enableSvgImage(contents, "logisticPredEqn");

//***	HmoreVariation
		enableSvgImage(contents, "bias");
		enableSvgImage(contents, "errorOne");
		enableSvgImage(contents, "meanError");
		enableSvgImage(contents, "meanSqrError");
		enableSvgImage(contents, "rootMeanSqrError");
		enableSvgImage(contents, "sampleSD");
		enableSvgImage(contents, "sd2");
		enableSvgImage(contents, "stDevn");
		enableSvgImage(contents, "variance");
		enableSvgImage(contents, "zeroBias");

//***	HmultiGroup
		enableSvgImage(contents, "anovaTable2");
		enableSvgImage(contents, "devnInGroup");
		enableSvgImage(contents, "diffMeanSD");
		enableSvgImage(contents, "diffMeanSDEst");
		enableSvgImage(contents, "fRatio");
		enableSvgImage(contents, "generalCI");
		enableSvgImage(contents, "groupMeanEst");
		enableSvgImage(contents, "meanCI");
		enableSvgImage(contents, "meanCIPooled");
		enableSvgImage(contents, "regnComponent");
		enableSvgImage(contents, "regnMss");
		enableSvgImage(contents, "regnSsq");
		enableSvgImage(contents, "regnSsq2");
		enableSvgImage(contents, "residComponent");
		enableSvgImage(contents, "residMss");
		enableSvgImage(contents, "residSsq");
		enableSvgImage(contents, "residSsq2");
		replacePattern_all(contents, "<img[^>]*images/rSqrInequality.gif\"[^>]*>", "<span class=\"black\">0 &nbsp;&le;&nbsp; <em>R</em><sup>2</sup> &nbsp;&le;&nbsp; 1</span>");
		enableSvgImage(contents, "rSquaredDefn");
		enableSvgImage(contents, "sdEst");
		enableSvgImage(contents, "sdEst1");
		replacePattern_all(contents, "<img[^>]*images/sdUnequal.gif\"[^>]*>", "<span class=\"black\">&sigma;<sub>1</sub> &ne; &sigma;<sub>2</sub></span>");
		replacePattern_all(contents, "<img[^>]*images/sigmaEqual.gif\"[^>]*>", "<span class=\"black\">&sigma;<sub>1</sub> = &sigma;<sub>2</sub> = &sigma;</span>");
		enableSvgImage(contents, "ssqEqn");
		enableSvgImage(contents, "testStat");
		enableSvgImage(contents, "totalComponent");
		enableSvgImage(contents, "totalMss");
		enableSvgImage(contents, "totalSsq");
		enableSvgImage(contents, "totalSsq2");
		enableSvgImage(contents, "varIEst");
		enableSvgImage(contents, "varPooledEst");
		enableSvgImage(contents, "varPooledEst2");
		enableSvgImage(contents, "varPooledEst3");
		
//***	Hmultiplicative
		replacePattern_all(contents, "<img[^>]*images/multModel.gif\"[^>]*>", "<span class=\"darkblue\"><strong>Data &nbsp; = &nbsp; (Seasonal effect) &nbsp; &times; &nbsp; Trend &nbsp; &times; &nbsp; Cyclical &nbsp; &times; &nbsp; Residual</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/addLogModel.gif\"[^>]*>", "<span class=\"darkblue\"><strong>log(Data) &nbsp; = &nbsp; Seasonal* &nbsp; + &nbsp; Trend* &nbsp; + &nbsp; Cyclical* &nbsp; + &nbsp; Residual</strong>*</span>");
		replacePattern_all(contents, "<img[^>]*images/forecastLog.gif\"[^>]*>", "<span class=\"darkblue\"><strong>forecast of log(Data) &nbsp; = &nbsp; (forecast Seasonal*) &nbsp;+ &nbsp;(forecast Trend*) &nbsp;+ &nbsp;(forecast Cyclical*)</strong></span>");
		enableSvgImage(contents, "constDollars");
		
//***	HmultiRegn
		enableSvgImage(contents, "fatHat");
		replacePattern_all(contents, "<img[^>]*images/yxEqn.gif\"[^>]*>", "<span class=\"black\"><em>y</em> &nbsp;=&nbsp; <em>b</em><sub>0</sub> + <em>b</em><sub>1 </sub><em>x</em></span>");
		replacePattern_all(contents, "<img[^>]*images/yxzEqn.gif\"[^>]*>", "<span class=\"black\"><em>y</em> &nbsp;=&nbsp; <em>b</em><sub>0</sub> + <em>b</em><sub>1 </sub><em>x</em> + <em>b</em><sub>2 </sub><em>z</em></span>");
		replacePattern_all(contents, "<img[^>]*images/yxzwEqn.gif\"[^>]*>", "<span class=\"black\"><em>y</em> &nbsp;=&nbsp; <em>b</em><sub>0</sub> + <em>b</em><sub>1 </sub><em>x</em> + <em>b</em><sub>2 </sub><em>z</em> + <em>b</em><sub>3 </sub><em>w</em> + ...</span>");
		replacePattern_all(contents, "<img[^>]*images/fatAbHtEqn.gif\"[^>]*>", "<em>Body fat</em> &nbsp; = &nbsp; 22 &nbsp;+&nbsp; 0.53 <em>Abdomen</em> &nbsp;&minus;&nbsp; 0.80 <em>Height</em>");
		replacePattern_all(contents, "<img[^>]*images/fatAbHtLsEqn.gif\"[^>]*>", "<em>body fat</em> &nbsp; = &nbsp; 30.0 &nbsp;+&nbsp; 0.53 <em>abdomen</em> &nbsp;&minus;&nbsp; 0.80 <em>height</em>");
		replacePattern_all(contents, "<img[^>]*images/fitVal2.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:4px\"><img src=\"../images/symbol.yiHatBlue.png\" width=\"11\" height=\"18\" align=\"baseline\"></span> &nbsp;=&nbsp; <em>b</em><sub>0</sub> + <em>b</em><sub>1 </sub><em>x<sub>i</sub></em> + <em>b</em><sub>2 </sub><em>z<sub>i</sub></em></span>");
		replacePattern_all(contents, "<img[^>]*images/resid2.gif\"[^>]*>", "<span class=\"red\"><em>e</em><sub><em>i</em></sub></span> <span class=\"black\">&nbsp;=&nbsp; <em>y<sub>i</sub></em> &minus;</span> <span style=\"position:relative; top:4px\"><img src=\"../images/symbol.yiHatBlue.png\" width=\"11\" height=\"18\" align=\"baseline\"></span>");
		enableSvgImage(contents, "residSsqMulti");
		enableSvgImage(contents, "residSsq2Multi");
		
//***	Hnormal
		enableSvgImage(contents, "arrow");
		enableSvgImage(contents, "normalDensity");
		enableSvgImage(contents, "standardiseEqn2");
		enableSvgImage(contents, "zScoreExEqn");
		replacePattern_all(contents, "<img[^>]*images/xFromZ.gif\"[^>]*>", "<span class=\"black\"><em>x</em> &nbsp;=&nbsp; &mu; &nbsp;+&nbsp; <em>z</em> &sigma;</span>");
		replacePattern_all(contents, "<img[^>]*images/zToXEqn.gif\"[^>]*>", "<span class=\"black\"><em>x</em> &nbsp;=&nbsp; &mu; &nbsp;+&nbsp; <em>z</em> &times; &sigma;</span>");
		
//***	HpopSamp
		enableSvgImage(contents, "samplingHistos");
		enableSvgImage(contents, "samplingHistos2");

//***	Hprob
		enableSvgImage(contents, "conditCalc");
		enableSvgImage(contents, "glaucoma");
		enableSvgImage(contents, "jointCondit");
		enableSvgImage(contents, "propnVenn");
		enableSvgImage(contents, "seekers");
		swapGifForPng(contents, "cardAndCoin");
		replacePattern_all(contents, "<img[^>]*images/cardCoinProb.gif\"[^>]*>", "<span class=\"black\"><em>p</em><span class=\"textSub\">head, heart</span> &nbsp;= &nbsp;<em>p</em><span class=\"textSub\">head, club</span> &nbsp;= &nbsp;... &nbsp;= &nbsp;<em>p</em><span class=\"textSub\">tail, spade</span> &nbsp;= &nbsp;<sup>1</sup>/<sub>8</sub> &nbsp;= &nbsp;0.125</span>");
		enableSvgImage(contents, "conditProbDefn");
		enableSvgImage(contents, "conditProb2Defn");
		replacePattern_all(contents, "<img[^>]*images/margCountEx.gif\"[^>]*>", "<span class=\"black\"><em>n</em><span class=\"textSub\">blue eyes</span> &nbsp;= &nbsp;<em>n</em><span class=\"textSub\">blue, blonde</span> &nbsp;+ &nbsp;<em>n</em><span class=\"textSub\">blue, brunette</span> &nbsp;+ &nbsp;...</span>");
		replacePattern_all(contents, "<img[^>]*images/margProbEx.gif\"[^>]*>", "<span class=\"black\"><em>p</em><span class=\"textSub\">blue eyes</span> &nbsp;= &nbsp;<em>p</em><span class=\"textSub\">blue, blonde</span> &nbsp;+ &nbsp;<em>p</em><span class=\"textSub\">blue, brunette</span> &nbsp;+ &nbsp;...</span>");
		enableSvgImage(contents, "margProbEqn");
		enableSvgImage(contents, "margProb2Eqn");
		enableSvgImage(contents, "conditProbEx");
		enableSvgImage(contents, "conditProbEx2");
		enableSvgImage(contents, "conditProbEx3");
		replacePattern_all(contents, "<img[^>]*images/diseaseProbEx.gif\"[^>]*>", "<span class=\"black\"><em>p</em><span class=\"textSub\">negative | disease</span> = &nbsp;0.05&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<em>p</em><span class=\"textSub\">positive | no disease</span> = &nbsp;0.10</span>");
		replacePattern_all(contents, "<img[^>]*images/diseaseProb2Ex.gif\"[^>]*>", "<span class=\"black\"><em>p</em><span class=\"textSub\">positive | disease</span> = &nbsp;0.95&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<em>p</em><span class=\"textSub\">negative | no disease</span> = &nbsp;0.90</span>");
		replacePattern_all(contents, "<img[^>]*images/diseaseProb3Ex.gif\"[^>]*>", "<span class=\"black\"><em>p</em><span class=\"textSub\">positive | disease</span> &nbsp;= &nbsp;0.95</span>");
		replacePattern_all(contents, "<img[^>]*images/diseaseProb4Ex.gif\"[^>]*>", "<span class=\"black\"><em>p</em><span class=\"textSub\">disease | positive</span> &nbsp;= &nbsp;0.514</span>");
		replacePattern_all(contents, "<img[^>]*images/diseaseProb5Ex.gif\"[^>]*>", "<span class=\"black\"><em>p</em><span class=\"textSub\">disease | positive</span></span>");
		enableSvgImage(contents, "jointProbEqn");
		replacePattern_all(contents, "<img[^>]*images/taxProbEx.gif\"[^>]*>", "<span class=\"black\"><em>p</em><span class=\"textSub\">investigated | good claim</span> = &nbsp;0.1&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<em>p</em><span class=\"textSub\">not investigated | bad claim</span> = &nbsp;0.2</span>");
		replacePattern_all(contents, "<img[^>]*images/taxProb2Ex.gif\"[^>]*>", "<span class=\"black\"><em>p</em><span class=\"textSub\">not investigated | good claim</span> = &nbsp;0.9&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<em>p</em><span class=\"textSub\">investigated | bad claim</span> = &nbsp;0.8</span>");
		replacePattern_all(contents, "<img[^>]*images/taxProb3Ex.gif\"[^>]*>", "<span class=\"black\"><em>p</em><span class=\"textSub\">bad claim | investigated</span> &nbsp;= &nbsp;0.471</span>");
		replacePattern_all(contents, "<img[^>]*images/taxProb4Ex.gif\"[^>]*>", "<span class=\"black\"><em>p</em><span class=\"textSub\">investigated | bad claim</span> &nbsp;= &nbsp;0.8</span>");
		replacePattern_all(contents, "<img[^>]*images/taxProb5Ex.gif\"[^>]*>", "<span class=\"black\"><em>p</em><span class=\"textSub\">bad claim | investigated</span></span>");
		
//***	HprobDensity
		enableSvgImage(contents, "barChart");
		enableSvgImage(contents, "histogram");
		enableSvgImage(contents, "pdf");
		enableSvgImage(contents, "unknownPopn");
		enableSvgImage(contents, "tickCross");

//***	HprobSim
		enableSvgImage(contents, "envelopeGenerator");
		enableSvgImage(contents, "uniform01");

//***	HprobSim
		enableSvgImage(contents, "randBlockAnovaTable");
		enableSvgImage(contents, "ssqEqn");
		
//***	HrandBlock
		replacePattern_all(contents, "<img[^>]*images/blockMean.gif\"[^>]*>", "<span style=\"position:relative; top:3px\"><img src=\"../images/symbol.yBar.png\" width=\"9\" height=\"15\" align=\"baseline\"></span><sub><em><span class=\"black\">b</span></em></sub>");
		replacePattern_all(contents, "<img[^>]*images/groupMean.gif\"[^>]*>", "<span style=\"position:relative; top:3px\"><img src=\"../images/symbol.yBar.png\" width=\"9\" height=\"15\" align=\"baseline\"></span><sub><em><span class=\"black\">g</span></em></sub>");
		enableSvgImage(contents, "blockSsq");
		enableSvgImage(contents, "fittedValEqn");
		replacePattern_all(contents, "<img[^>]*images/overallMean.gif\"[^>]*>", "<span style=\"position:relative; top:3px\"><img src=\"../images/symbol.yBar.png\" width=\"9\" height=\"15\" align=\"baseline\"></span>");
		enableSvgImage(contents, "residSsq");
		enableSvgImage(contents, "ssqEqn");
		enableSvgImage(contents, "totalSsq");
		enableSvgImage(contents, "treatSsq");
		
//***	HrandomMean
		enableSvgImage(contents, "sampToPopn");
		enableSvgImage(contents, "sampToPopn2");
		enableSvgImage(contents, "sdMeanSWOR");
		enableSvgImage(contents, "popnSdFinite");
		enableSvgImage(contents, "estSDMean");
		replacePattern_all(contents, "<img[^>]*images/meanMeanEqn.gif\"[^>]*>", "<span style=\"position:relative; top:6px\"><img src=\"../images/symbol.muXbar.png\" width=\"19\" height=\"16\" align=\"baseline\"></span> &nbsp;<span class=\"black\">=&nbsp; &mu;</span>");
		replacePattern_all(contents, "<img[^>]*images/meanNormalDistn.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-12px\"><img src=\"../images/symbol.xBar.png\" width=\"10\" height=\"10\" align=\"baseline\">&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">normal</font> (0, &nbsp;</span><img src=\"../images/symbol.sigmaOverRootN.png\" width=\"26\" height=\"31\" align=\"baseline\"><span style=\"position:relative; top:-12px\">)</span></span>");
		replacePattern_all(contents, "<img[^>]*images/sdMeanEqn.gif\"[^>]*>", "<span style=\"position:relative; top:5px\"><img src=\"../images/symbol.sigmaXbar.png\" width=\"19\" height=\"16\" align=\"baseline\"></span> &nbsp;<span class=\"black\">=</span>&nbsp; <span style=\"position:relative; top:12px\"><img src=\"../images/symbol.sigmaOverRootN.png\" width=\"26\" height=\"31\" align=\"baseline\"></span>");
		
//***	HrandomPropn
		enableSvgImage(contents, "normalApprox");
		enableSvgImage(contents, "propnAsMeanEqn");
		replacePattern_all(contents, "<img[^>]*images/probSum.gif\"[^>]*>", "<span class=\"black\">P(<em> a</em> &le; <em>X</em> &le; <em>b </em>) &nbsp;&nbsp;=&nbsp;&nbsp; P(<em> X</em> = <em>a </em>) &nbsp;+&nbsp; P( <em>X</em> = <em>a</em> + 1 ) &nbsp;+&nbsp; ... &nbsp;+&nbsp; P(<em> X</em> = <em>b </em>)</span>");
		
//***	HregnEst
		enableSvgImage(contents, "nonlinearity");
		enableSvgImage(contents, "nonlinearity2");
		enableSvgImage(contents, "residDiagram");
		enableSvgImage(contents, "hawaii2");
		enableSvgImage(contents, "radiationCancer2");
		replacePattern_all(contents, "<img[^>]*images/errorSdDefn.gif\"[^>]*>", "<span class=\"black\">&sigma; &nbsp;=&nbsp;<strong>st devn</strong>( &epsilon; )</span>");
		enableSvgImage(contents, "errorSdEst");
		enableSvgImage(contents, "errorSdEst2");
		enableSvgImage(contents, "errorSdEst3");
		enableSvgImage(contents, "hawaiiCI");
		enableSvgImage(contents, "meanSlope");
		enableSvgImage(contents, "radiationCI");
		enableSvgImage(contents, "sdSlopeEqn");
		enableSvgImage(contents, "sdSlopeEqn2");
		enableSvgImage(contents, "slopeCIFormula");
		enableSvgImage(contents, "slopeCIFormula2");
		replacePattern_all(contents, "<img[^>]*images/slopeErrorDistn.gif\"[^>]*>", "<span class=\"black\"><strong>error in estimate of &beta;<sub>1</sub></strong> &nbsp; = &nbsp; (<em>b</em><sub>1</sub> &minus; &beta;<sub>1</sub>) &nbsp; ~ &nbsp; <font face=\"Arial, Helvetica, sans-serif\">normal</font> ( 0, &nbsp;&sigma;<sub><em>b</em><sub>1</sub></sub> )</span>");
		
//***	HregnGroups
		enableSvgImage(contents, "parallelLines");
		enableSvgImage(contents, "twoLines");

//***	HregnModel
		enableSvgImage(contents, "band95");
		enableSvgImage(contents, "errorDiagram");
		replacePattern_all(contents, "<img[^>]*images/errorEqn.gif\"[^>]*>", "<span class=\"black\">&epsilon;&nbsp; = &nbsp;<em>y</em> &nbsp;&minus;&nbsp; ( &beta;<sub>0</sub> &nbsp;+&nbsp; &beta;<sub>1</sub><em>x</em> )</span>");
		replacePattern_all(contents, "<img[^>]*images/regnDistn.gif\"[^>]*>", "<span class=\"black\"><em>Y</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">normal</font> (&mu;<sub>y</sub>&nbsp;, &sigma;<sub>y</sub>)</span>");
		replacePattern_all(contents, "<img[^>]*images/regnEqn2.gif\"[^>]*>", "<span class=\"black\"><em>y<sub></sub></em> &nbsp;=&nbsp; &beta;<sub>0</sub> &nbsp;+&nbsp; &beta;<sub>1</sub><em>x</em> &nbsp;+&nbsp; &epsilon;</span>");
		replacePattern_all(contents, "<img[^>]*images/regnEqnEx.gif\"[^>]*>", "<span class=\"black\">&mu;<sub><em>y</em></sub> &nbsp;=&nbsp; 2.5<sub></sub> &nbsp;+&nbsp; 1.5<sub></sub><em>x</em></span>");
		replacePattern_all(contents, "<img[^>]*images/regnErrorDistn.gif\"[^>]*>", "<span class=\"black\">&epsilon;&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">normal</font> (0<sub></sub>&nbsp;, &sigma;)</span>");
		replacePattern_all(contents, "<img[^>]*images/regnErrorSDEx.gif\"[^>]*>", "<span class=\"black\">&sigma;<sub><em>y</em></sub> &nbsp;=&nbsp; 0.8</span>");
		replacePattern_all(contents, "<img[^>]*images/regnMeanEqn.gif\"[^>]*>", "<span class=\"black\">&mu;<sub><em>y</em></sub> &nbsp;=&nbsp; &beta;<sub>0</sub> &nbsp;+&nbsp; &beta;<sub>1</sub><em>x</em></span>");
		replacePattern_all(contents, "<img[^>]*images/regnSDEqn.gif\"[^>]*>", "<span class=\"black\">&sigma;<sub><em>y</em></sub> &nbsp;=&nbsp; &sigma;</span>");
		
//***	HregnPred
		enableSvgImage(contents, "ciForMeanY");
		enableSvgImage(contents, "predInterval");
		
//***	HregnProblem
		enableSvgImage(contents, "autocorr");
		enableSvgImage(contents, "errorsDiagram");
		enableSvgImage(contents, "housePrice");
		enableSvgImage(contents, "housePriceL");
		enableSvgImage(contents, "housePriceLL");
		enableSvgImage(contents, "outlierError");
		enableSvgImage(contents, "residPull");
		enableSvgImage(contents, "residualsDiagram");
		enableSvgImage(contents, "logShareVol");
		enableSvgImage(contents, "minResidSsq");
		enableSvgImage(contents, "nonlinear");
		enableSvgImage(contents, "hetroskedastic");
		replacePattern_all(contents, "<img[^>]*images/nonlinearEqn.gif\"[^>]*>", "<span class=\"black\"><em>y<sup>2</sup><sub></sub></em> &nbsp; = &nbsp; &beta;<sub>0 &nbsp;</sub>+ &nbsp; &beta;<sub>1 </sub><strong>log</strong><em> x</em> &nbsp;+ &nbsp;&epsilon;</span>");
		enableSvgImage(contents, "skewError");
		replacePattern_all(contents, "../java/javaImages/xEquals/durbWatson.gif", "../HregnProblem/durbWatson.gif");		//	always use copy in HregnProblem
		enableSvgImage(contents, "durbWatson");
		
//***	HregnTest
		enableSvgImage(contents, "rnEffect");
		enableSvgImage(contents, "slopeTestP");
		enableSvgImage(contents, "zForTestingSlope");
		replacePattern_all(contents, "<img[^>]*images/regnDistn.gif\"[^>]*>", "<span class=\"black\"><em>Y</em> &nbsp; ~ &nbsp; <font face=\"Arial, Helvetica, sans-serif\">normal</font> (&beta;<sub>0</sub> + &beta;<sub>1</sub><em>x</em> , &nbsp;&sigma;)</span>");
		replacePattern_all(contents, "<img[^>]*images/zeroSlopeDistn.gif\"[^>]*>", "<span class=\"black\"><em>Y</em> &nbsp; ~ &nbsp; <font face=\"Arial, Helvetica, sans-serif\">normal</font> (&beta;<sub>0</sub> , &nbsp;&sigma;)</span>");
		enableSvgImage(contents, "s_tFromR");
		enableSvgImage(contents, "sdSlopeEqn");
		replacePattern_all(contents, "<img[^>]*images/slopeHypoth.gif\"[^>]*>", "<span class=\"blue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&beta;<sub>1</sub> &nbsp;=&nbsp; 0<br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&beta;<sub>1</sub>  &nbsp;&ne;&nbsp;  0</span>");
		enableSvgImage(contents, "standardisedSlope");
		enableSvgImage(contents, "standardisedSlope2");
		enableSvgImage(contents, "tFromR");
		
//***	Hrelationship
		enableSvgImage(contents, "outliers");
		enableSvgImage(contents, "outliers2");

//***	HsampPractice
		enableSvgImage(contents, "estMean");
		replacePattern_all(contents, "<img[^>]*images/strataMeans.gif\"[^>]*>", "<img src=\"../images/symbol.xBar.png\" width=\"10\" height=\"10\" align=\"baseline\"><span class=\"black\"><sub>1</sub></span>, <img src=\"../images/symbol.xBar.png\" width=\"10\" height=\"10\" align=\"baseline\"><span class=\"black\"><sub>2</sub></span>, ..., <img src=\"../images/symbol.xBar.png\" width=\"10\" height=\"10\" align=\"baseline\"><span class=\"black\"><sub><em>k</em></sub></span>");
		
//***	Hscatterplot
		enableSvgImage(contents, "scatterValues");

//***	Hseasonal
		enableSvgImage(contents, "weeklyMean");
		replacePattern_all(contents, "<img[^>]*images/dataComponents.gif\"[^>]*>", "<span class=\"darkblue\" style=\"font-weight:bold\">Deseasonalised&nbsp; = &nbsp; Trend &nbsp; + &nbsp; Cyclical &nbsp; + &nbsp; Residual</span>");
		replacePattern_all(contents, "<img[^>]*images/deseaComponents.gif\"[^>]*>", "<span class=\"darkblue\" style=\"font-weight:bold\">Deseasonalised&nbsp; = &nbsp; Trend &nbsp; + &nbsp; Cyclical &nbsp; + &nbsp; Residual</span>");
		replacePattern_all(contents, "<img[^>]*images/sea_desea_eqn.gif\"[^>]*>", "<span class=\"darkblue\" style=\"font-weight:bold\">Data &nbsp; = &nbsp; Seasonal effect &nbsp; + &nbsp; Deseasonalised</span>");
		replacePattern_all(contents, "<img[^>]*images/forecastEqn.gif\"[^>]*>", "<span class=\"darkblue\" style=\"font-weight:bold\">Forecast&nbsp; = &nbsp; Seasonal effect &nbsp; + &nbsp; Trend forecast&nbsp; + &nbsp; Cyclical forecast</span>");
		enableSvgImage(contents, "quarterlySmoothEqn");
		
//***	HseMean
		enableSvgImage(contents, "muHatEqualsXBar");
		replacePattern_all(contents, "<img[^>]*images/errorDistn.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-11px\"><em>error</em>&nbsp; ~ &nbsp;normal (0, &nbsp;</span><img src=\"../images/symbol.sigmaOverRootN.png\" width=\"26\" height=\"31\" align=\"baseline\"><span style=\"position:relative; top:-11px\">)</span></span>");
		replacePattern_all(contents, "<img[^>]*images/meanBias.gif\"[^>]*>", "<span class=\"black\"><em>error</em> &nbsp;=&nbsp; &mu;<sub>error</sub> &nbsp;=&nbsp; 0</span>");
		replacePattern_all(contents, "<img[^>]*images/meanSe.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-11px\"><em>standard error</em> &nbsp;=&nbsp; &sigma;<sub>error</sub> &nbsp;=&nbsp; </span><img src=\"../images/symbol.sigmaOverRootN.png\" width=\"26\" height=\"31\" align=\"baseline\"></span>");
		replacePattern_all(contents, "<img[^>]*images/meanSe2.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-11px\"><em>standard error</em> &nbsp;=&nbsp; &sigma;<sub>error</sub> &nbsp;=&nbsp; </span><img src=\"../images/symbol.sOverRootN.png\" width=\"26\" height=\"31\" align=\"baseline\"></span>");
		replacePattern_all(contents, "<img[^>]*images/xxx.gif\"[^>]*>", "");
		replacePattern_all(contents, "<img[^>]*images/concDistn.gif\"[^>]*>", "<em>X</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">normal</font> (&mu;&nbsp;, &sigma; = 0.0068)");
		replacePattern_all(contents, "<img[^>]*images/concErrorDistn.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-13px\"><em>error</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">normal</font> (0, &nbsp;&sigma; = </span><img src=\"images/symbol.concError.png\" width=\"50\" height=\"36\" align=\"baseline\"><span style=\"position:relative; top:-13px\">)</span></span>");
		replacePattern_all(contents, "<img[^>]*images/concSe.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-11px\"><em>standard error</em> &nbsp;=&nbsp; SE &nbsp;=&nbsp; </span><img src=\"images/symbol.concSe.png\" width=\"176\" height=\"34\" align=\"baseline\"></span>");
		replacePattern_all(contents, "<img[^>]*images/sugarDistn.gif\"[^>]*>", "<em>X</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">normal</font> (&mu;&nbsp;, &sigma; = 3)");
		replacePattern_all(contents, "<img[^>]*images/sugarErrorDistn.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-13px\"><em>error</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">normal</font> (0, &nbsp;&sigma; = </span><img src=\"images/symbol.sugarError.png\" width=\"25\" height=\"36\" align=\"baseline\"><span style=\"position:relative; top:-13px\">)</span></span>");
		replacePattern_all(contents, "<img[^>]*images/sugarSe.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:-11px\"><em>standard error</em> &nbsp;=&nbsp; SE &nbsp;=&nbsp; </span><img src=\"images/symbol.sugarSe.png\" width=\"146\" height=\"34\" align=\"baseline\"></span>");
		
//***	Hsmoothing
		enableSvgImage(contents, "expPredict");
		enableSvgImage(contents, "expPredict2");
		enableSvgImage(contents, "expSmoothEqn");
		enableSvgImage(contents, "expSmoothEqn2");
		enableSvgImage(contents, "expSmoothHalf");
		enableSvgImage(contents, "expSmoothInitial");
		enableSvgImage(contents, "lowessEqn");
		enableSvgImage(contents, "movingAverage4");
		enableSvgImage(contents, "movingAverage5");
		enableSvgImage(contents, "movingAverageDouble");
		enableSvgImage(contents, "quarterlySmoothEqn");
		enableSvgImage(contents, "runningMeanEqn");
		enableSvgImage(contents, "runningMedianEqn");
		replacePattern_all(contents, "<img[^>]*images/smoothingEqn.gif\"[^>]*>", "<span class=\"black\"><strong>smoothed value</strong> &nbsp; = &nbsp;<em><strong>centre</strong></em> ( <strong>original value</strong> <em>and</em> <strong>adjacent values</strong> )</span>");
		
//***	Hstructures
		enableSvgImage(contents, "dataMatrix");
		enableSvgImage(contents, "multilevelMatrices");

//***	HsumDiff
		enableSvgImage(contents, "diffExample");
		enableSvgImage(contents, "sumDiffSD");
		enableSvgImage(contents, "sumExample");
		enableSvgImage(contents, "totalExample");
		enableSvgImage(contents, "diff2Distn2");
		enableSvgImage(contents, "meanSDDiff");
		enableSvgImage(contents, "sampleSum");
		enableSvgImage(contents, "sdDiffExample");
		enableSvgImage(contents, "sdInequality");
		enableSvgImage(contents, "standardiseEqn");
		enableSvgImage(contents, "sum2Distn");
		enableSvgImage(contents, "sum2Distn2");
		enableSvgImage(contents, "sum2Distn3");
		enableSvgImage(contents, "sum2Variance");
		enableSvgImage(contents, "sumMean");
		enableSvgImage(contents, "sumSD");

//***	HtableDisplay
		enableSvgImage(contents, "gridlines");
		
//***	HtestMean
		enableSvgImage(contents, "normalOneTailArea");
		enableSvgImage(contents, "normalTwoTailArea");
		enableSvgImage(contents, "oneTailedT");
		enableSvgImage(contents, "pValueFromZ");
		enableSvgImage(contents, "twoTailedT");
		enableSvgImage(contents, "twoTailedZ");
		replacePattern_all(contents, "<img[^>]*images/hypothA.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;=&nbsp; 128</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;&ne;&nbsp; 128</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypothB.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;=&nbsp; 0.86</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;&ne;&nbsp; 0.86</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypothC.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;=&nbsp; 15%</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;&gt;&nbsp; 15%</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypothD.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;=&nbsp; 2.012</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;&ne;&nbsp; 2.012</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypothD.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;=&nbsp; 5.64</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;&gt;&nbsp; 5.64</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypothLowTailed.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;=&nbsp; </strong>&mu;<strong><sub>0</sub></strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;&lt;&nbsp; </strong>&mu;<strong><sub>0</sub></strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypothOneTailed.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;=&nbsp; </strong>&mu;<strong><sub>0</sub></strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;&gt;&nbsp; </strong>&mu;<strong><sub>0</sub></strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypothTwoTailed.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;=&nbsp; </strong>&mu;<strong><sub>0</sub></strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &nbsp;&ne;&nbsp; </strong>&mu;<strong><sub>0</sub></strong></span>");
		enableSvgImage(contents, "tStatDefn");
		enableSvgImage(contents, "zForMean");
		enableSvgImage(contents, "zStatDefn");
		enableSvgImage(contents, "zGeneral");
		enableSvgImage(contents, "cornflakeMeanMean");
		enableSvgImage(contents, "cornflakeMeanSd");
		enableSvgImage(contents, "cornflakeZ");
		
//***	HtestPaired
		replacePattern_all(contents, "<img[^>]*images/nullGarage.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub>garage A</sub> = </strong>&mu;<strong><sub>garage B</sub></strong></span>");
		replacePattern_all(contents, "<img[^>]*images/nullIQ.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub>good</sub> = </strong>&mu;<strong><sub>poor</sub></strong></span>");
		replacePattern_all(contents, "<img[^>]*images/nullPill.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub>before</sub> = </strong>&mu;<strong><sub>after</sub></strong></span>");
		replacePattern_all(contents, "<img[^>]*images/nullSnail.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub>yellow</sub> = </strong>&mu;<strong><sub>brown</sub></strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypothesesA.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub><em>X</em></sub> = </strong>&mu;<strong><sub><em>Y</em></sub></strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub><em>X</em></sub> &ne; </strong>&mu;<strong><sub><em>Y</sub></em></strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypothesesB.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub><em>X</em></sub> = </strong>&mu;<strong><sub><em>Y</em></sub></strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub><em>X</em></sub> &gt; </strong>&mu;<strong><sub><em>Y</sub></em></strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypothesesC.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub><em>D</em></sub> = 0</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub><em>D</em></sub> &ne; 0</strong></span>");
		enableSvgImage(contents, "testStat");
		
//***	HtestPropn
		enableSvgImage(contents, "normalPValue");
		enableSvgImage(contents, "testProcessA");
		enableSvgImage(contents, "testProcessB");
		replacePattern_all(contents, "<img[^>]*images/binomialDistn.gif\"[^>]*>", "<em>X</em>&nbsp; ~ &nbsp;<font face=\"Arial, Helvetica, sans-serif\">binomial</font> (<em>n</em>&nbsp;, &pi;)");
		replacePattern_all(contents, "<img[^>]*images/exampleHypoth.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;=&nbsp; 0.574</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;&ne;&nbsp; 0.574</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/halfHypoth.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;=&nbsp; 0.5</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;&lt;&nbsp; 0.5</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypothPropnA.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;=&nbsp; 0.01</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;&gt;&nbsp; 0.01</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypothPropnB.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;=&nbsp; 0.72</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;&ne;&nbsp; 0.72</strong></span>");
		enableSvgImage(contents, "normalPValue2");
		replacePattern_all(contents, "<img[^>]*images/oneTailedHyp.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;=&nbsp; </strong>&pi;<strong><sub>0</sub></strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;&lt;&nbsp; </strong>&pi;<strong><sub>0</sub></strong></span>");
		replacePattern_all(contents, "<img[^>]*images/oneTailedHyp2.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;=&nbsp; </strong>&pi;<strong><sub>0</sub></strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;&gt;&nbsp; </strong>&pi;<strong><sub>0</sub></strong></span>");
		replacePattern_all(contents, "<img[^>]*images/twoTailedHyp.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;=&nbsp; </strong>&pi;<strong><sub>0</sub></strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong> &nbsp;&ne;&nbsp; </strong>&pi;<strong><sub>0</sub></strong></span>");
		replacePattern_all(contents, "<img[^>]*images/pValueSum.gif\"[^>]*>", "<strong>p-value</strong> &nbsp; = &nbsp; P(<em>X</em> &le; 369) &nbsp; = &nbsp; P(0) &nbsp;+ &nbsp;P(1) &nbsp;+ &nbsp;...  + &nbsp;P(368) &nbsp;+ &nbsp;P(369)");
		replacePattern_all(contents, "<img[^>]*images/pValueSum2.gif\"[^>]*>", "<strong>p-value</strong> &nbsp; = &nbsp; P(<em>X</em> &ge; 37) &nbsp; = &nbsp; P(37) &nbsp;+ &nbsp;P(38) &nbsp;+ &nbsp;...  + &nbsp;P(2499) &nbsp;+ &nbsp;P(2500)");
		enableSvgImage(contents, "z-formula");
		enableSvgImage(contents, "zForP");
		
//***	HtestPValue
		enableSvgImage(contents, "mutualHistoNormal");
		enableSvgImage(contents, "newcombHisto");
		enableSvgImage(contents, "nullAltHypoth");
		enableSvgImage(contents, "psaHisto");
		enableSvgImage(contents, "pValueDistn");
		enableSvgImage(contents, "pValueProbs");
		replacePattern_all(contents, "<img[^>]*images/hypothTestB.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &le; 1.1</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &gt; 1.1</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypothZero.gif\"[^>]*>", "<span class=\"darkblue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &le; 0</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong> &gt; 0</strong></span>");
		
//***	HtimePlot
		enableSvgImage(contents, "timePlotDisplay");

//***	Htransform
		enableSvgImage(contents, "boxCoxEqn");
		enableSvgImage(contents, "powerTable");
		enableSvgImage(contents, "negativePower");
		replacePattern_all(contents, "<img[^>]*images/linear.gif\"[^>]*>", "<span class=\"black\"><strong>new value</strong> &nbsp; = &nbsp; <em>a</em> &nbsp;+ &nbsp;<em>b</em> &nbsp;&times &nbsp;<strong>old value</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/kmToMileEqn.gif\"[^>]*>", "<span class=\"black\"><strong>miles</strong> &nbsp; = &nbsp;0.6214&nbsp;&times &nbsp;<strong>kilometers</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/linearMean.gif\"[^>]*>", "<span class=\"black\"><strong>new mean</strong> &nbsp; = &nbsp; <em>a</em> &nbsp;+ &nbsp;<em>b</em> &nbsp;&times &nbsp;<strong>old mean</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/linearSD.gif\"[^>]*>", "<span class=\"black\"><strong>new sd</strong> &nbsp; = &nbsp;<em><font size=\"+1\">|</font>b<font size=\"+1\">|</font></em> &nbsp;&times &nbsp;<strong>old sd</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/ozToGramEqn.gif\"[^>]*>", "<span class=\"black\"><strong>grams</strong> &nbsp; = &nbsp;28.3494&nbsp;&times &nbsp;<strong>ounces</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/logTransform.gif\"[^>]*>", "<span class=\"black\"><strong>new value</strong> &nbsp; = log<sub>10</sub> <font size=\"+1\">(</font><strong>old value</strong><font size=\"+1\">)</font></span>");
		
//***	Htrend
		replacePattern_all(contents, "<img[^>]*images/residual.gif\"[^>]*>", "<span class=\"black\"><em>e<sub>i</sub></em>&nbsp;&nbsp;=&nbsp;&nbsp;<em>y<sub>i</sub></em>&nbsp;&minus;&nbsp;<em>trend<sub>i</sub></em></span>");
		replacePattern_all(contents, "<img[^>]*images/trendFunction.gif\"[^>]*>", "<span class=\"black\"><em>trend</em> &nbsp; = &nbsp; function (<em> time</em> )</span>");
		replacePattern_all(contents, "<img[^>]*images/trendLinear.gif\"[^>]*>", "<span class=\"black\"><em>trend</em> &nbsp; = &nbsp; <em>b</em><sub>0</sub> &nbsp;+ &nbsp;<em>b</em><sub>1</sub><em> time</em></span>");
		replacePattern_all(contents, "<img[^>]*images/trendRecoded.gif\"[^>]*>", "<span class=\"black\"><em>trend</em> &nbsp; = &nbsp; <em>b</em><sub>0</sub> &nbsp;+ &nbsp;<em>b</em><sub>1</sub> <font size=\"+1\">(</font><em>time</em> &minus; 1960<font size=\"+1\">)</font></span>");
		enableSvgImage(contents, "rss");
		replacePattern_all(contents, "<img[^>]*images/trendQuadratic.gif\"[^>]*>", "<span class=\"black\"><em>trend</em> &nbsp; = &nbsp; <em>b</em><sub>0</sub> &nbsp;+ &nbsp;<em>b</em><sub>1</sub><em> time</em> &nbsp;+ &nbsp;<em>b</em><sub>2</sub><em> time</em><sup>2</sup></span>");
		replacePattern_all(contents, "<img[^>]*images/rss2.gif\"[^>]*>", "<span class=\"black\"><font size=\"+1\">&Sigma;</font> <em>e<sub>i</sub></em><sup>2</sup></span>");
		replacePattern_all(contents, "<img[^>]*images/trendPolynomial.gif\"[^>]*>", "<span class=\"black\"><em>trend</em> &nbsp; = &nbsp; <em>b</em><sub>0</sub> &nbsp;+ &nbsp;<em>b</em><sub>1</sub><em> time</em> &nbsp;+ &nbsp;<em>b</em><sub>2</sub><em> time</em><sup>2</sup> &nbsp;+ &nbsp;<em>b</em><sub>3</sub><em> time</em><sup>3</sup> &nbsp;+ &nbsp;...</span>");
		
//***	HtwoGroupInf
		enableSvgImage(contents, "oneTailedP");
		enableSvgImage(contents, "pValue");
		replacePattern_all(contents, "<img[^>]*images/confLevel.gif\"[^>]*>", "<span class=\"black\"><strong>Prob</strong> ( <span style=\"position:relative; top:6px\"><img src=\"../images/symbol.xBarDiffRed.png\" width=\"42\" height=\"21\" align=\"baseline\"></span> &nbsp;<strong>is within</strong> &nbsp; &plusmn; &nbsp;1.96 &nbsp;<span style=\"position:relative; top:7px\"><img src=\"../images/symbol.sdDiffGreen.png\" width=\"41\" height=\"26\" align=\"baseline\"></span> &nbsp; of &nbsp; <span style=\"color:#00F; font-weight:bold\">&mu;<sub>2</sub>&nbsp;-&nbsp;&mu;<sub>1</sub></span>) &nbsp; = &nbsp; 0.95</span>");
		replacePattern_all(contents, "<img[^>]*images/diffCIWithZ.gif\"[^>]*>", "<span class=\"black\"><span style=\"position:relative; top:6px\"><img src=\"../images/symbol.xBarDiffRed.png\" width=\"42\" height=\"21\" align=\"baseline\"></span>&nbsp; &plusmn; &nbsp; 1.96 &nbsp;<span style=\"position:relative; top:7px\"><img src=\"../images/symbol.sdDiffGreen.png\" width=\"41\" height=\"26\" align=\"baseline\"></span></span>");
		replacePattern_all(contents, "<img[^>]*images/dfEqn.gif\"[^>]*>", "<span class=\"black\">&nu; &nbsp; = &nbsp; min (<em>n</em><sub>1</sub>&minus;1, &nbsp;<em>n</em><sub>2</sub>&minus;1)</span>");
		enableSvgImage(contents, "diffCIWithT");
		enableSvgImage(contents, "diffCIWithZ");
		enableSvgImage(contents, "dfEqn2");
		enableSvgImage(contents, "errorTwoDistn");
		enableSvgImage(contents, "meanSDDiff2");
		enableSvgImage(contents, "hypotheses");
		enableSvgImage(contents, "diffMeanSD");
		enableSvgImage(contents, "estErrorDistn");
		enableSvgImage(contents, "estErrorDistn2");
		enableSvgImage(contents, "normalDistn");
		replacePattern_all(contents, "<img[^>]*images/twoMeanHypoth.gif\"[^>]*>", "<span class=\"blue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub>2</sub> &minus; </strong>&mu;<strong><sub>1</sub> &nbsp;=&nbsp; 0</strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub>2</sub> &minus; </strong>&mu;<strong><sub>1</sub> &nbsp;&ne;&nbsp; 0</strong></span>");
		replacePattern_all(contents, "<img[^>]*images/hypotheses2.gif\"[^>]*>", "<span class=\"blue\"><strong><font size=\"+1\">H</font><sub>0</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub>1</sub> &nbsp;=&nbsp; </strong>&mu;<strong><sub>2</sub></strong><br><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&mu;<strong><sub>1</sub> &nbsp;&gt;&nbsp; </strong>&mu;<strong><sub>2</sub></strong></span>");
		
//***	HtwoGroupModel
		enableSvgImage(contents, "model");
		enableSvgImage(contents, "birthWt2");
		enableSvgImage(contents, "piecework2");
		replacePattern_all(contents, "<img[^>]*images/diffMean.gif\"[^>]*>", "<span class=\"black\">&delta; &nbsp;=&nbsp; &mu;<sub>2</sub> &nbsp;&minus;&nbsp; &mu;<sub>1</sub></span>");
		
//***	HtwoGroupPropn
		enableSvgImage(contents, "propnModel");
		enableSvgImage(contents, "pValCalc");
		enableSvgImage(contents, "diffCIEqn");
		enableSvgImage(contents, "diffCIEqn2");
		replacePattern_all(contents, "<img[^>]*images/diffConfLevel.gif\"[^>]*>", "<span class=\"black\"><strong>Prob</strong> &nbsp;( <span class=\"red\"><em>p</em><sub>2</sub>&minus;<em>p</em><sub>1</sub></span> &nbsp; <strong>is within</strong> &nbsp;<span class=\"blue\">&pi;<sub>2</sub>&minus;&pi;<sub>1</sub></span> &nbsp;&plusmn;&nbsp; 1.96  <span class=\"green\">&sigma;<sub><em>p</em><sub>2</sub>&minus;<em>p</em><sub>1</sub></sub></span> ) &nbsp; = &nbsp; 0.95</span>");
		enableSvgImage(contents, "diffPDistn");
		enableSvgImage(contents, "diffSDEstEqn");
		replacePattern_all(contents, "<img[^>]*images/oneTailAlt.gif\"[^>]*>", "<span class=\"blue\"><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong><sub>1</sub> &nbsp;&minus;&nbsp; </strong>&pi;<strong><sub>2</sub> &nbsp;&gt;&nbsp; 0</strong></span> &nbsp;&nbsp; <span class=\"red\"><strong>or</strong></span> &nbsp;&nbsp; <span class=\"blue\"><strong><font size=\"+1\">H</font><sub>A</sub>&nbsp;:&nbsp;&nbsp;&nbsp;</strong>&pi;<strong><sub>1</sub> &nbsp;&minus;&nbsp; </strong>&pi;<strong><sub>2</sub> &nbsp;&lt;&nbsp; 0</strong></span>");
		enableSvgImage(contents, "meanSD");
		enableSvgImage(contents, "meanSDDiffEqn");
		enableSvgImage(contents, "meanSDOfP");
		enableSvgImage(contents, "sdDiffEqn");
		enableSvgImage(contents, "sdDiffEqn2");
		
//***	HunivarCat
		enableSvgImage(contents, "bad_pie");
		enableSvgImage(contents, "barAndPie");
		enableSvgImage(contents, "carrotBarChart");
		enableSvgImage(contents, "chickenPredExploded");
		enableSvgImage(contents, "chickenPredPie");
		enableSvgImage(contents, "hawaii3DBars");
		enableSvgImage(contents, "hawaiiPerspBars");
		enableSvgImage(contents, "hospital3DPie");
		enableSvgImage(contents, "kestrel3DBars");
		enableSvgImage(contents, "kestrel3DPie");
		enableSvgImage(contents, "kestrelPerspBars");
		enableSvgImage(contents, "maritalStatusExploded");
		enableSvgImage(contents, "maritalStatusPie");
		enableSvgImage(contents, "simpleBarchart");
		enableSvgImage(contents, "tanzania3DBars");
		enableSvgImage(contents, "tanzaniaPerspBars");
		enableSvgImage(contents, "vacationExploded");
		enableSvgImage(contents, "vacationPie");

//***	HvalueDisplay
		enableSvgImage(contents, "examStemLeaf1");
		enableSvgImage(contents, "examStemLeaf2");
		enableSvgImage(contents, "markStemAndLeaf");
		enableSvgImage(contents, "mvisStemLeaf1");
		enableSvgImage(contents, "mvisStemLeaf2");
		enableSvgImage(contents, "rentalStemLeaf1");
		enableSvgImage(contents, "rentalStemLeaf2");
	}
/* */
	
	
//**************
//**************    changes relative paths for books and sections dropping down a level in the folder hierarchy
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_bookLevel(FileChanges contents, boolean isEnglish) {
		replacePattern_once(contents, "href=['\"]\\.\\./pageStyles.css['\"]", "href=\"\\.\\./\\.\\./pageStyles.css\"");
		replacePattern_once(contents, "src=['\"]\\.\\./structure/pageSetup.js['\"]", "src=\"\\.\\./\\.\\./structure/pageSetup.js\"");
		
		replacePattern_all(contents, "codebase=['\"]\\.\\./java['\"]", "codebase=\"\\.\\./\\.\\./java\"");
		replacePattern_all(contents, "src=\"\\.\\./images", "src=\"\\.\\./\\.\\./images");
		if (isEnglish)
			replacePattern_all(contents, "src=\"\\.\\./H(\\w*)/images", "src=\"\\.\\./$1/images");
		else
			replacePattern_all(contents, "src=\"\\.\\./H(\\w*)/images", "src=\"\\.\\./\\.\\./en/$1/images");
		
		replacePattern_all(contents, "src='\\.\\./exercises/", "src='\\.\\./\\.\\./exercises/");			//	for exercises
		replacePattern_all(contents, "src=\"\\.\\./exercises/", "src=\"\\.\\./\\.\\./exercises/");
		
		replacePattern_all(contents, "src='\\.\\./structure/", "src='\\.\\./\\.\\./structure/");			//	for video controller
		replacePattern_all(contents, "href='\\.\\./structure/", "href='\\.\\./\\.\\./structure/");
		
		replacePattern_all(contents, "writeVideo\\('(\\w*)', 'H(\\w*)'", "writeVideo\\('bk/$1', '$2'");
		
	}
	
	
//**************
//**************    adds release information to each page
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_releaseInfo(FileChanges contents) {
		replacePattern_once(contents, "<link rel=['\"]stylesheet['\"] href=['\"]\\.\\./\\.\\./pageStyles.css['\"] type=['\"]text/css['\"]>\\s*<script src=['\"]\\.\\./\\.\\./structure/pageSetup.js['\"]></script>",
									"<link rel=\"stylesheet\" href=\"\\.\\./\\.\\./pageStyles.css\" type=\"text/css\">\n"
									+ "\t<script src=\"\\.\\./\\.\\./releaseInfo.js\"></script>\n"
									+ "\t<script src=\"\\.\\./\\.\\./structure/pageSetup.js\"></script>");
	}
	
	
//**************
//**************    changes <span> for italic and bold to <em> and <strong>
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_emphasis(FileChanges contents) {
		replacePattern_all(contents, "<span\\s*style=\"font-style: italic;\">([^<]*)</span>", "<em>$1</em>");
		replacePattern_all(contents, "<span\\s*style=\"font-weight: bold;\">([^<]*)</span>", "<strong>$1</strong>");
		replacePattern_all(contents, "<strong></strong>", "");
		replacePattern_all(contents, "<em></em>", "");
	}
	
	
//**************
//**************    changes <span> for italic and bold to <em> and <strong>
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_appletSize(FileChanges contents) {
		replacePattern_all(contents, "writeAppletParams\\([^\\)]*\\)", "writeAppletParams\\(\\)");
	}
	
	
//**************
//**************    changes mathJax output format
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_mathJaxFormat(FileChanges contents) {
		replacePattern_once(contents, "TeX-AMS-MML_HTMLorMML", "TeX-AMS-MML_SVG");
	}
	
	
//**************
//**************    changes removes scripting to change gif to svg images
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_gifToSvg(FileChanges contents) {
		String sourcePattern = "(<img.*?src=.*?.gif\").*?(width=\"[^\"]*\").*?(height=\"[^\"]*\").*?<iframe.*?</iframe>.*?<script.*?</script>";
		String destPattern = "$1 $2 $3>";
		replacePattern_all(contents, sourcePattern, destPattern);
	}
	
	
//**************
//**************    Moves applet name from CODE attribute to PARAM
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_appletName(FileChanges contents) {
		replacePattern_all(contents, "(<applet.*?code=['\"])(.*?)(.class.*?>)(\\s*)(<script.*?</script>)",
															"$1dataView.CastApplet$3$4$5$4<param name=\"appletName\" value=\"$2\">");
	}
	
	
//**************
//**************    Moves applet name from CODE attribute to PARAM
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_appletName2(FileChanges contents) {
		replacePattern_all(contents, "(<!?)(applet.*?code=['\"])(.*?)(.class.*?>)(\\s*)",
															"$1$2dataView.CastApplet$4$5$1param name=\"appletName\" value=\"$3\">$5", false);
	}
	
	
//**************
//**************    changes top, bottom, left and right classes to table borders
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_tableBorders(FileChanges contents) {
		replacePattern_all(contents, "class=\"top\"",
															"style=\"border-top:1px solid #999999;\"", false);
		replacePattern_all(contents, "class=\"left\"",
															"style=\"border-left:1px solid #999999;\"", false);
		replacePattern_all(contents, "class=\"bottom\"",
															"style=\"border-bottom:1px solid #999999;\"", false);
		replacePattern_all(contents, "class=\"right\"",
															"style=\"border-right:1px solid #999999;\"", false);
															
		replacePattern_all(contents, "class=\"top left\"",
															"style=\"border-top:1px solid #999999; border-left:1px solid #999999;\"", false);
		replacePattern_all(contents, "class=\"top right\"",
															"style=\"border-top:1px solid #999999; border-right:1px solid #999999;\"", false);
		replacePattern_all(contents, "class=\"bottom left\"",
															"style=\"border-bottom:1px solid #999999; border-left:1px solid #999999;\"", false);
		replacePattern_all(contents, "class=\"bottom right\"",
															"style=\"border-bottom:1px solid #999999; border-right:1px solid #999999;\"", false);
															
		replacePattern_all(contents, "class=\"top bottom left\"",
															"style=\"border:1px solid #999999; border-right:0px;\"", false);
		replacePattern_all(contents, "class=\"top left bottom\"",
															"style=\"border:1px solid #999999; border-right:0px;\"", false);
		replacePattern_all(contents, "class=\"top bottom right\"",
															"style=\"border:1px solid #999999; border-left:0px;\"", false);
		replacePattern_all(contents, "class=\"top right bottom\"",
															"style=\"border:1px solid #999999; border-left:0px;\"", false);
		
		replacePattern_all(contents, "class=\"top bottom left right\"",
															"style=\"border:1px solid #999999;\"", false);
		replacePattern_all(contents, "999999\";", "999999;\"", false);
		replacePattern_all(contents, "0px\";", "0px;\"", false);
	}
	
	
//**************
//**************    changes text subscript
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_subscripts(FileChanges contents) {
		replacePattern_all(contents, "<span class=\"textSub\">(.*?)</span>",
															"<sub style=\"font-family:sans-serif; font-size:small;\">$1</sub>", false);
	}
	
	
//**************
//**************    removes <span class="heading">
//**************
	
	@SuppressWarnings("unused")
	static private void makeChanges_span(FileChanges contents) {
		replacePattern_all(contents, "<span class=\"heading\">(.*?)</span>", "$1", false);
		replacePattern_all(contents, "\\Q{align*}\\E", "{align}", false);
	}
	
	
//**************
//**************    removes <span class="heading">
//**************
	
	static private void makeChanges_remove_semicolon(FileChanges contents) {
		replacePattern_all(contents, "backgroundColor\"; value=", "backgroundColor\" value=", false);
	}
	
	
//**************

  static private void tidyHtmlFile(File inFile) {
    try {
			String s = HtmlHelper.getFileAsString(inFile);
			FileChanges contents = new FileChanges(s);
			
//			makeChanges_writeAppletParams(contents);
//			makeChanges_index_and_dataset(contents);
//			makeChanges_symbols(contents);
//			makeChanges_gifs(contents);
//			makeChanges_reformat(contents);
//			makeChanges_foreign(contents);
//			makeChanges_video2(contents);
//			makeChanges_docType(contents);
			
//			if (inFile.getName().startsWith("v_"))
//				makeChanges_video3(contents);
			
//			boolean isEnglish = inFile.getParentFile().getParentFile().getName().equals("en");
//			makeChanges_bookLevel(contents, isEnglish);
			
//			makeChanges_releaseInfo(contents);
			
//			makeChanges_emphasis(contents);
			
//			if (inFile.getName().startsWith("v_"))
//				makeChanges_video4(contents);
			
//			makeChanges_appletSize(contents);
			
//			makeChanges_mathJaxFormat(contents);
//			if (inFile.getName().startsWith("v_"))
//				makeChanges_videoBody(contents);
			
//			if (!inFile.getName().startsWith("mixture"))		//	files in this folder have so many images that Matcher gets into loop
//				makeChanges_gifToSvg(contents);
			
//			makeChanges_appletName(contents);
//			makeChanges_appletName2(contents);
			
//			makeChanges_tableBorders(contents);
			
//			makeChanges_subscripts(contents);
//			makeChanges_span(contents);
			makeChanges_remove_semicolon(contents);
			
			if (!contents.changed)
				return;
			
			System.out.println("Changed file: " + inFile.getParentFile().getName() + ", " + inFile.getName());
			
			File dir = inFile.getParentFile();
			String fileName = inFile.getName();
			File outFile = new File(dir, fileName + "__X");
			
			OutputStream out = new FileOutputStream(outFile);
			Writer w = new OutputStreamWriter(out, "UTF-8");
			w.write(contents.s);
			w.flush();
			w.close();
			
			inFile.delete();
			outFile.renameTo(inFile);
		} catch (Exception e) {
			System.out.println("Error in tidyHtmlFile() for: " + inFile.toString());
			e.printStackTrace();
		}
  }

  @SuppressWarnings("unused")
static private void tidyXmlFile(File inFile) {
    try {
			String s = XmlHelper.getFileAsString(inFile);
			FileChanges contents = new FileChanges(s);
			

			replacePattern_all(contents, "dir=\"B_", "dir=\"bk/");
			
			replacePattern_all(contents, "dir=\"H", "dir=\"en/");
			replacePattern_all(contents, "dir=\"F", "dir=\"fr/");
			replacePattern_all(contents, "dir=\"D", "dir=\"de/");
			replacePattern_all(contents, "dir=\"S", "dir=\"es/");
			replacePattern_all(contents, "dir=\"C", "dir=\"ch/");
			replacePattern_all(contents, "dir=\"K", "dir=\"gd/");
			
			replacePattern_all(contents, "dir='B_", "dir='bk/");
			replacePattern_all(contents, "dir='H", "dir='en/");
			replacePattern_all(contents, "dir='F", "dir='fr/");
			replacePattern_all(contents, "dir='D", "dir='de/");
			replacePattern_all(contents, "dir='S", "dir='es/");
			replacePattern_all(contents, "dir='C", "dir='ch/");
			replacePattern_all(contents, "dir='K", "dir='gd/");
			
			replacePattern_all(contents, "Dir=\"B_", "Dir=\"bk/");
			replacePattern_all(contents, "Dir=\"H", "Dir=\"en/");
			replacePattern_all(contents, "Dir=\"F", "Dir=\"fr/");
			replacePattern_all(contents, "Dir=\"D", "Dir=\"de/");
			replacePattern_all(contents, "Dir=\"S", "Dir=\"es/");
			replacePattern_all(contents, "Dir=\"C", "Dir=\"ch/");
			replacePattern_all(contents, "Dir=\"K", "Dir=\"gd/");
			
			replacePattern_all(contents, "Dir='B_", "Dir='bk/");
			replacePattern_all(contents, "Dir='H", "Dir='en/");
			replacePattern_all(contents, "Dir='F", "Dir='fr/");
			replacePattern_all(contents, "Dir='D", "Dir='de/");
			replacePattern_all(contents, "Dir='S", "Dir='es/");
			replacePattern_all(contents, "Dir='C", "Dir='ch/");
			replacePattern_all(contents, "Dir='K", "Dir='gd/");
			
			replacePattern_once(contents, "\"../../structure/bookXmlDefn.dtd\"", "\"../../../structure/bookXmlDefn.dtd\"");
			replacePattern_once(contents, "\"../../structure/sectionXmlDefn.dtd\"", "\"../../../structure/sectionXmlDefn.dtd\"");
			replacePattern_all(contents, "encoding=['\"]ISO-8859-1['\"]", "encoding=\"UTF-8\"");

			replacePattern_once(contents, "'../../structure/videoXmlDefn.dtd'", "'../../../structure/videoXmlDefn.dtd'");
			replacePattern_all(contents, "dir name='H", "dir name='");
			replacePattern_all(contents, "book name='", "book name='bk/");
			
			if (!contents.changed)
				return;
			
			System.out.println("Changed file: " + inFile.getParentFile().getName() + ", " + inFile.getName());
			
			File dir = inFile.getParentFile();
			String fileName = inFile.getName();
			File outFile = new File(dir, fileName + "__X");
			
			OutputStream out = new FileOutputStream(outFile);
			Writer w = new OutputStreamWriter(out, "UTF-8");
			w.write(contents.s);
			w.flush();
			w.close();
			
			inFile.delete();
			outFile.renameTo(inFile);
		} catch (Exception e) {
			System.out.println("Error in tidyXmlFile() for: " + inFile.toString());
			e.printStackTrace();
		}
  }
	
	private JButton encodeButton;
	private JLabel finishedLabel;
//	private File coreDir;
	
	public TidyHtml(File coreDir) {
		super("Tidy HTML files");
//		this.coreDir = coreDir;
		
		setLayout(new BorderLayout(0, 10));
		setBackground(kBackgroundColor);
		
			Panel buttonPanel = new Panel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				encodeButton = new JButton("Choose the \"core\" folder or another with HTML...");
				encodeButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										File dir = chooseFolder();
										if (dir != null) {
											finishedLabel.setText("Starting");
											tidy(dir);
											finishedLabel.setText("Finished");
										}
									}
								});
			buttonPanel.add(encodeButton);
		add("North", buttonPanel);
		
			Panel messagePanel = new Panel();
			messagePanel.setLayout(new FixedSizeLayout(200, 40));
				finishedLabel = new JLabel("", Label.LEFT);
			messagePanel.add(finishedLabel);
		add("Center", messagePanel);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private File chooseFolder() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setDialogTitle("Select folder to tidy");
		fc.setFileHidingEnabled(true);

		int result = fc.showOpenDialog(this);
	
		switch (result) {
			case JFileChooser.APPROVE_OPTION:
				return fc.getSelectedFile();
			case JFileChooser.CANCEL_OPTION:
			case JFileChooser.ERROR_OPTION:
				System.exit(0);
		}
		return null;
	}
	
	private boolean directoryOK(String fileName) {
		String badDirName[] = {"exercises", "images", "java", "releaseInfo", "structure", "terms", "text"};
		for (int i=0 ; i<badDirName.length ; i++)
			if (fileName.equals(badDirName[i]))
				return false;
		
		return true;
	}
	
	private void tidy(File file) {
		if (file.isDirectory() && directoryOK(file.getName())) {
			System.out.println("looking in directory: " + file.getName());
			File contents[] = file.listFiles();
			for (int i=0 ; i<contents.length ; i++)
				tidy(contents[i]);
		}
		else if (file.isFile() && file.getName().endsWith(".html")) {
//			System.out.println("Tidying " + file.getName());
			tidyHtmlFile(file);
		}
//		else if (file.isFile() && file.getName().endsWith(".xml")) {
//			System.out.println("Tidying " + file.getName());
//			tidyXmlFile(file);
//		}
	}
	
}
