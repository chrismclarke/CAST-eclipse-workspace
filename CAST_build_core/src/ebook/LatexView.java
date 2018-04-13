package ebook;

import java.awt.*;
import javax.swing.*;

import org.scilab.forge.jlatexmath.*;


public class LatexView extends JComponent {
	static final private int kTexFontSize = 16;		//	should really read this from castStyles.css
	
	static final String kLatexPrelude = "\\newcommand{\\Var}{\\operatorname{Var}} \n"
																		+ "\\newcommand{\\Covar}{\\operatorname{Covar}} \n"
																		+ "\\newcommand{\\Corr}{\\operatorname{Corr}} \n"
																		+ "\\newcommand{\\se}{\\operatorname{se}} \n"
																		+ "\\newcommand{\\Bias}{\\operatorname{Bias}} \n"
																		+ "\\newcommand{\\MSE}{\\operatorname{MSE}} \n"
																		
																		+ "\\newcommand{\\UniformDistn}{\\mathcal{Uniform}} \n"
																		+ "\\newcommand{\\BernoulliDistn}{\\mathcal{Bernoulli}} \n"
																		+ "\\newcommand{\\BinomDistn}{\\mathcal{Binomial}} \n"
																		+ "\\newcommand{\\GeomDistn}{\\mathcal{Geometric}} \n"
																		+ "\\newcommand{\\NegBinDistn}{\\mathcal{NegBinom}} \n"
																		+ "\\newcommand{\\RectDistn}{\\mathcal{Rectangular}} \n"
																		+ "\\newcommand{\\PoissonDistn}{\\mathcal{Poisson}} \n"
																		+ "\\newcommand{\\ExponDistn}{\\mathcal{Exponential}} \n"
																		+ "\\newcommand{\\ErlangDistn}{\\mathcal{Erlang}} \n"
																		+ "\\newcommand{\\GammaDistn}{\\mathcal{Gamma}} \n"
																		+ "\\newcommand{\\WeibullDistn}{\\mathcal{Weibull}} \n"
																		+ "\\newcommand{\\NormalDistn}{\\mathcal{Normal}} \n"
																		+ "\\newcommand{\\BetaBinomDistn}{\\mathcal{BetaBinomial}} \n"
																		+ "\\newcommand{\\BetaDistn}{\\mathcal{Beta}} \n"
																		+ "\\newcommand{\\ChiSqrDistn}{\\mathcal{\\chi^2}} \n"
																		+ "\\newcommand{\\TDistn}{\\mathcal{t}} \n"
																		+ "\\newcommand{\\FDistn}{\\mathcal{F}} \n"
																		+ "\\newcommand{\\MultinomDistn}{\\mathcal{Multinomial}} \n"
																		
																		+ "\\newcommand{\\spaced}[1]{{\\qquad}\\text{#1}{\\qquad}} \n"
																		+ "\\newcommand{\\diagfrac}[2]{\\small{\\frac {#1} {#2}}} \n";
//																		+ "\\newcommand{\\diagfrac}[3][-2]{\\raise{0.1em}{#2} \\kern#1pt/ \\raise{-0.3em}{#3}} \n\n";
	
	static final private String kTestLatex = "X \\;\\sim\\; \\BinomDistn \\left(n, \\pi = \\frac {\\lambda} n\\right)";
	
	static final private int kVerticalOffset = 8;
		
	private TeXFormula formula;
	private TeXIcon icon;
	
	public LatexView() {
		this(kTestLatex);
	}
	
	public LatexView(String theLatex) {
		formula = new TeXFormula(kLatexPrelude + theLatex);
		icon = formula.new TeXIconBuilder().setStyle(TeXConstants.STYLE_DISPLAY).setSize(kTexFontSize).build();
		icon.setInsets(new Insets(0, 0, 0, 0));
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		icon.paintIcon(this, g2, 0, 0);
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(icon.getIconWidth(), icon.getIconHeight() + kVerticalOffset);
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
}