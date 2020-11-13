package com.splendid.awtchat;

/*
Subcomponent (main part) of SmileyTextArea,
  which is intended to be part of Eteria IRC Client from Javier Kohen
2001 written by Frank Bartels for Splendid Internet GmbH, Kiel, Germany
Maintained by Javier Kohen <jkohen@users.sourceforge.net>.
*/

  //                       ===O=V=E=R=V=I=E=W===
  //                          (over this file)
  //
  //                       Constants & Variables
  //     Class Run  (a part of a line within attributes not changing)
  //  Class ContentLine  (a line of runs, is one or more lines on display)
  //                            Constructor
  //                           Private Utils
  //                           Update & Paint
  //                            Mouse Events
  //                     Component's User Interface

import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import ar.com.jkohen.irc.MircMessage;

public class SmileyTextAreaArea extends java.awt.Canvas
    implements MouseListener, MouseMotionListener
{
    //=======================================================================
    //                       Constants & Variables
    //=======================================================================

    private int bufferlen=1000; // number of lines saved by default
    // (virtual lines, i.e. lines independent of display line length)

    private int dimx=300; // reflects actual component width (initalized in constructor)
    private int dimy=200; // reflects actual component height (initalized in constructor)
    private int borderx=4;
    private int bordery=3;
    private int dimxv=294; // view area
    private int dimyv=196;
    int subsequentindent=12; // indent for subsequent lines (inside one append with long string)

    int SMILEY_DEFAULT_SIZE=14;

    int SMOOTH=0; int FAST=1; int SAFE=2;  // mode's possible values
    int mode=0; // SMOOTH is default
    int oldSbValue=0;
    boolean SB_WORKAROUND_ENABLED=false;
    Scrollbar sb=null;
    int sbValue=0;
    int sbLength=0;
    boolean inupdate=false;
    boolean inappend=false;
    boolean last_action_scrolling=false;

    HyperlinkReceiver hyperlinkReceiver=null;
    Vector lines=null;
    SmileyTextArea sta=null;
    Dimension preferreddim; // reflects preferred size

  // Color stuff
  // Workaround IBM's jdk-1.1.8 problem with final keyword.
//    static final Color [] fixedColors; // Init. in static block.
    static Color [] fixedColors; // Init. in static block.
    private int urlColorIndex=12;  // Standard url color

    private static final int bgColorIndex=16;  // background color index
    private static final int fgColorIndex=17;  // foreground color index

    Color backgroundColor;
    Color foregroundColor;

  // the default values are overwritten on construction
    private Font usedFonts[]={null,null,null,null};
    private FontMetrics fmFonts[]={null,null,null,null};
    private int fmSpaceFonts=1;
    private int fmDescentFonts[]={1,1,1,1};
    private int fmAscentFonts[]={1,1,1,1};
    private int fmDY=2;
    private int fmDescent=1;
    private int fmAscent=1;
    private int fmLeading=0;

    // current settings of Graphics
    private int currentCombinedBoldItalic=0;
    private boolean currentUnderlined=false;
    private int currentColorIndex=0;

    static {
	fixedColors = new Color[18]; // 16 colors + 2 (fg/bg) internal.

	ResourceBundle resources
	    // EIRC WORKAROUND #35.
	    //  	  = ResourceBundle.getBundle("com.splendid.awtchat.colors");
	    = ResourceBundle.getBundle("colors");

	for (int i = 0; i < fixedColors.length; i++) {
	    try {
		fixedColors[i] = Color.decode(resources.getString("n" + i));
	    } catch (Exception ex) {
		fixedColors[i] = Color.black;
		System.err.println(ex);
	    }
	}
    }

    //=======================================================================
    //     Class Run  (a part of a line within attributes not changing)
    //=======================================================================

    private class Run // === class Run represents a text fragment including attributes ===
    { public String text=null;
	public String url=null;
	public int pixX0=0; // Start(0)
	public int pixY0=0;
	public int pixX1=0; // End(1)
	public int pixY1=0;
	public int nbreaks=0;
	public Vector breaks=null; // if the Run splits at lineends the text indices are saved here
	public int combinedBoldItalic=0;
	public boolean underlined=false;
	public int fgColorIndexMine=fgColorIndex;
	public int bgColorIndexMine=bgColorIndex;
	public Image smiley=null;

	// Constructor (text)
	public Run(String atext,
		   boolean bold, boolean italic, boolean underlined,
		   int fgColorIndex, int bgColorIndex) {
	    char c;
	    int len = atext.length();
	    StringBuffer filtered_text = new StringBuffer(len);
	    for (int i = 0; i < len; i++) {
		c = atext.charAt(i);
		if (Character.isISOControl(c) || !Character.isDefined(c)) {
		    continue; // skip control and undefined characters
		}

		filtered_text.append(c);
	    }
	    this.text = filtered_text.toString();
	    this.combinedBoldItalic=(bold ? 1 : 0) | (italic ? 2 : 0);
	    this.underlined=underlined;
	    this.fgColorIndexMine=fgColorIndex;
	    this.bgColorIndexMine=bgColorIndex;
	}
	public Run(String text, Image smiley, int bgColorIndex) // Constructor (smiley)
	{ this.text=text;
	this.smiley=smiley;
	this.bgColorIndexMine=bgColorIndex;
	}
	public void setUrl(String url)        // if URL, you need this second construction step
	{ this.url=url;
	// preserve bold and italic and bgColorIndexMine
	underlined=true;
	fgColorIndexMine=urlColorIndex;
	}

	private int mySpaceIndex(String s, int i)
	{ int len=s.length();
	for (int j=i; j<len; j++)
	    if (Character.isSpaceChar(s.charAt(j))) return(j);
	return(-1);
	}

	public int setXY(int newPixX, int newPixY, int line) // prepare the Run for display
	{ String s=text;
	int loopcount,lasti,i,x,snipped=0;
	if (newPixX>=dimxv-fmSpaceFonts*2) // break Run, needed for speed and smileys
	    { newPixX=borderx+subsequentindent;
	    newPixY+=fmDY;
	    line++;
	    }
	breaks=null; nbreaks=0;
	pixX0=newPixX;
	pixY0=newPixY;
	if (smiley!=null)
	    { pixX1=newPixX+smiley.getWidth(null);
	    pixY1=newPixY;
	    if (pixX1>dimx-borderx)
		{ pixX0=borderx+subsequentindent;
		pixY0+=fmDY;
		pixX1=borderx+subsequentindent+smiley.getWidth(null);
		pixY1+=fmDY;
		line++;
		}
	    return(line); // smiley case ready
	    }
	pixX1=newPixX+fmFonts[combinedBoldItalic].stringWidth(s);
	pixY1=newPixY;
	if (smiley!=null) return(line); // do not break inside a smiley
	loopcount=0;
	while (pixX1>dimx-borderx) // then must break line
	    { if (loopcount++>100) break; // avoid deadlocks (althogh there are none ;-)
	    for (lasti=0, i=0; (i=mySpaceIndex(s,i))>=0; )
		{ x=newPixX+fmFonts[combinedBoldItalic].stringWidth(s.substring(0,i));
		if (x>dimx-borderx) break; // found proper break (the last one on actual line)
		i++; // pos of next char after space
		while (s.length()>i && Character.isSpaceChar(s.charAt(i))) i++; // hop over spaces
		lasti=i;
		}
	    if (lasti==0) // break complete s
		{ if (newPixX<=borderx+subsequentindent) // already gone to the next line?
		    { // the (spaceless) s does not fit on a line! break by force somewhere
			for (lasti=1, i=1; i<s.length()-1; i++)
			    { x=newPixX+fmFonts[combinedBoldItalic].stringWidth(s.substring(0,i+1));
			    if (x>dimx-borderx)
				{ lasti=i;
				break;
				}
			    }
		    }
		else
		    { newPixX=borderx+subsequentindent;
		    if (pixY0==pixY1 && breaks==null) // if break at beginning of run...
			{ pixX0=borderx+subsequentindent; // ...the beginning also must go on next line
			pixY0+=fmDY;
			}
		    pixX1=newPixX+fmFonts[combinedBoldItalic].stringWidth(s);
		    pixY1+=fmDY;
		    line++;
		    continue; // retry with linebreak (almost nothing to do for this) before
		    }
		}
	    if (lasti>0 && lasti<s.length()) // break line (can not use else!)
		{ if (breaks==null) breaks=new Vector();
		breaks.addElement(new Integer(lasti+snipped));
		nbreaks++;
		s=s.substring(lasti);
		snipped+=lasti;
		}
	    newPixX=borderx+subsequentindent;
	    pixX1=newPixX+fmFonts[combinedBoldItalic].stringWidth(s);
	    pixY1+=fmDY;
	    line++;
	    }
	return(line);
	}

	private void print(Graphics g, String s, int x, int y) {
	    if (smiley != null) {
		int dy = smiley.getHeight(null);
		if (dy <= 0) {
		    dy = SMILEY_DEFAULT_SIZE;
		}

		int ascent = fmAscentFonts[combinedBoldItalic];
		int descent = fmDescentFonts[combinedBoldItalic];
		int yoff = -(ascent - descent + dy) / 2; // put in center between ascent and descent
		if (yoff > descent - dy + 1) { // but not below descent
		    yoff = descent - dy + 1;
		}

		// Not the normal background color->paint bg first
		if (bgColorIndexMine != bgColorIndex) {
		    int len = smiley.getWidth(null);
		    if (len <= 0) {
			len = SMILEY_DEFAULT_SIZE;
		    }
		    g.setColor(fixedColors[bgColorIndexMine]);
		    g.fillRect(x, y - ascent, len, ascent + descent);
		    g.setColor(fixedColors[fgColorIndexMine]);
		}

		g.drawImage(smiley, x, y + yoff, null);
	    } else {
		// Not the normal background color->paint bg first
		if (bgColorIndexMine != bgColorIndex) {
		    int len = fmFonts[combinedBoldItalic].stringWidth(s);
		    int ascent = fmAscentFonts[combinedBoldItalic];

		    g.setColor(fixedColors[bgColorIndexMine]);
		    g.fillRect(x, y - ascent, len, ascent + fmDescentFonts[combinedBoldItalic]);
		    g.setColor(fixedColors[fgColorIndexMine]);
		}

		g.drawString(s, x, y);

		if (underlined) { // underlined->add the line
		    int len = fmFonts[combinedBoldItalic].stringWidth(s);
		    int below = fmDescentFonts[combinedBoldItalic] / 4;
		    if (below < 1) { // otherwise the underline line sticks to the font
			below = 1;
		    }
		    // if (getItalic())
		    //   would like to subtract something from len, but I don't know how much
		    // -1 makes it comaptible with fillRect len
		    g.drawLine(x, y + below, x + len - 1, y + below);
		    if (getBold()) {
			g.drawLine(x, y + below + 1, x + len - 1, y + below + 1);
		    }
		}
	    }
	}

	public void paint(Graphics g, int y0, boolean force)
	{ int x=pixX0;
	int y=y0+pixY0;
	setMyGraphicsAttributes(g,force);
	if (nbreaks==0) // makes simple case fast
	    { print(g,text,x,y);
	    }
	else
	    { int oldj=0;
	    int j=0;
	    for (int i=0; i<=nbreaks; i++)
		{ if (i==nbreaks) j=text.length();
		else            j=((Integer)(breaks.elementAt(i))).intValue();
		print(g,text.substring(oldj,j),x,y);
		x=borderx+subsequentindent;
		y+=fmDY;
		oldj=j;
		}
	    }
	}

	public void setMyGraphicsAttributes(Graphics g, boolean force) {
	    if (force || currentCombinedBoldItalic != combinedBoldItalic) {
		g.setFont(usedFonts[combinedBoldItalic]);
		currentCombinedBoldItalic = combinedBoldItalic;
	    }
	    if (force || currentColorIndex != fgColorIndexMine) {
		g.setColor(fixedColors[fgColorIndexMine]);
		currentColorIndex = fgColorIndexMine;
	    }
	    // currentUnderlined!=underlined does not matter for g
	}

	public String getUrl()
	{ return(url==null?"":url);
	}
	public boolean getBold()
	{ return((combinedBoldItalic&1)==1);
	}
	public boolean getItalic()
	{ return((combinedBoldItalic&2)==2);
	}
	public boolean getUnderlined()
	{ return(underlined);
	}
	public Color getColor()
	{ return(fixedColors[fgColorIndexMine]);
	}
	public Font getFont()
	{ return(usedFonts[combinedBoldItalic]);
	}
	public Image getSmiley() // may return null!
	{ return(smiley);
	}
    } // ============ End of class Run ============

    //=======================================================================
    //  Class ContentLine  (a line of runs, is one or more lines on display)
    //=======================================================================

    private class ContentLine {
	private String text;
	private Vector runs;
	private int lineStart;
	private int lineEnd;
	private int lineDY = 1;

	// Constructor (parses string and produces runs)
	public ContentLine(String s, boolean interpretUrl) {
	    boolean boldCurrent = false;
	    boolean italicCurrent = false;
	    boolean underlineCurrent = false;
	    int bgColorIndexCurrent = bgColorIndex;
	    int fgColorIndexCurrent = fgColorIndex;

	    int len = s.length();

	    // Empty line, no runs.
	    if (0 == len) {
		return;
	    }

	    this.text = s;
	    this.runs = new Vector();

	    int hrefend = -1;
	    int start = 0;

	    for (int i = 0; i < len; i++) {
		char ch = s.charAt(i);

		// Find e-mail addresses.
  		if (i > hrefend && interpretUrl) {
		    hrefend = findEndOfHref(s, i);

		    if (i < hrefend && hrefend < len
			&& s.charAt(hrefend) == '@') {
			if (start < i) {
			    runs.addElement(new Run(s.substring(start, i),
						    boldCurrent,
						    italicCurrent,
						    underlineCurrent,
						    fgColorIndexCurrent,
						    bgColorIndexCurrent));
			}

			int emailend = findEndOfHref(s, hrefend + 1);
			String emailstr = s.substring(i, emailend);
			Run run = new Run(emailstr, boldCurrent, italicCurrent,
					  underlineCurrent,
					  fgColorIndexCurrent,
					  bgColorIndexCurrent);

			// Prepend "mailto:" to URL if it's missing.
			if (!emailstr.toUpperCase().startsWith("MAILTO:")) {
			    emailstr = "mailto:" + emailstr;
			}

			run.setUrl(emailstr);
			runs.addElement(run);

			i = emailend - 1;
			start = emailend;
			continue;
		    }
		}

		// Find hrefs (including #channels).
		if (interpretUrl && isHref(s.substring(i))) {
		    if (start < i) {
			runs.addElement(new Run(s.substring(start, i),
						boldCurrent, italicCurrent,
						underlineCurrent,
						fgColorIndexCurrent,
						bgColorIndexCurrent));
		    }

		    String urlstr = s.substring(i, hrefend);
		    Run run = new Run(urlstr, boldCurrent, italicCurrent,
				      underlineCurrent,
				      fgColorIndexCurrent,
				      bgColorIndexCurrent);

		    // Prepend protocol to URL if it's missing.
		    if (urlstr.toUpperCase().startsWith("WWW.")) {
			urlstr = "http://" + urlstr;
		    } else if (urlstr.toUpperCase().startsWith("FTP.")) {
			urlstr = "ftp://" + urlstr;
		    }

		    run.setUrl(urlstr);
		    runs.addElement(run);

		    i = hrefend - 1;
		    start = hrefend;
		    continue;
		}

		// check for smileys
		if (sta.smileys != null) {
		    int imax = Math.min(i + sta.maxCodeLength,
					len);

		    String sbuf = s.substring(i, imax);

		    Image image;
		    for (int j = sbuf.length(); j > 0; j--) {
			String smax = sbuf.substring(0, j);

			// Found (longest) smiley.
			if ((image = (Image) sta.smileys.get(smax)) != null) {
			    if (start < i) {
				runs.addElement(new Run(s.substring(start, i),
							boldCurrent,
							italicCurrent,
							underlineCurrent,
							fgColorIndexCurrent,
							bgColorIndexCurrent));
			    }

			    runs.addElement(new Run(smax, image,
						    bgColorIndexCurrent));
			    i += j - 1;
			    start = i + 1;
			    break;
			}
		    }
		}

		// check for control codes
		switch (ch) {
		case MircMessage.BELL:
		    Toolkit.getDefaultToolkit().beep();
		    break;
		case MircMessage.BOLD:
		case MircMessage.ITALIC:
		case MircMessage.UNDERLINE:
		case MircMessage.COLOR:
		case MircMessage.REVERSE:
		case MircMessage.RESET: // Is control code.
		    if (start < i) {
			runs.addElement(new Run(s.substring(start, i),
						boldCurrent, italicCurrent,
						underlineCurrent,
						fgColorIndexCurrent,
						bgColorIndexCurrent));
		    }
			
		    start = i + 1;

		    switch (ch) {
		    case MircMessage.BOLD:
			boldCurrent = !boldCurrent;
			break;
		    case MircMessage.ITALIC:
			italicCurrent = !italicCurrent;
			break;
		    case MircMessage.UNDERLINE:
			underlineCurrent = !underlineCurrent;
			break;
		    case MircMessage.COLOR: {
			// Parse color(s) 0,0 ... 15,15 (>15 mapped modulo 16).
			int j, k;
			char c;
			int fgv = -1;
			int bgv = -1;
			for (j = i + 1; j < len && j <= i + 2; j++) {
			    c = s.charAt(j);
			    if (!Character.isDigit(c)) break;
			    if (fgv < 0) fgv = 0;
			    fgv = fgv * 10 + Character.digit(c, 10);
			}
			if (j < len && s.charAt(j) == ',') {
			    k=j;
			    for (j = k + 1; j < len && j <= k + 2; j++) {
				c=s.charAt(j);
				if (!Character.isDigit(c)) break;
				if (bgv < 0) bgv = 0;
				bgv = bgv * 10 + Character.digit(c, 10);
			    }
			}
			if (fgv >= 0) fgColorIndexCurrent = fgv % 16;
			if (bgv >= 0) bgColorIndexCurrent = bgv % 16;
			if (-1 == fgv && -1 == bgv) {
			    fgColorIndexCurrent = fgColorIndex;
			    bgColorIndexCurrent = bgColorIndex;
			}
			i = j - 1;
			start = i + 1;
			break;
		    }
		    case MircMessage.REVERSE:
			int saveci = bgColorIndexCurrent;
			bgColorIndexCurrent = fgColorIndexCurrent;
			fgColorIndexCurrent = saveci;
			break;
		    case MircMessage.RESET:
			boldCurrent = false;
			italicCurrent = false;
			underlineCurrent = false;
			bgColorIndexCurrent = bgColorIndex;
			fgColorIndexCurrent = fgColorIndex;
			break;
		    }
		    break;
		}
	    } // end of: for (int i = 0; i < len; i++)

	    // Add what's left.
	    if (start < len) {
		runs.addElement(new Run(s.substring(start, len),
					boldCurrent, italicCurrent,
					underlineCurrent, fgColorIndexCurrent,
					bgColorIndexCurrent));
	    }
	}

	public int findEndOfHref(String s, int start) {
	    int len = s.length();

	    for (int i = start; i < len; i++) {
		char c = s.charAt(i);
		if (Character.isLetterOrDigit(c)) {
		    continue;
		}

		switch (c) {
		case '.':
		case '?':
		case '&':
		case '=':
		case '-':
		case '_':
		case '/':
		case '#':
		case ':':
		case '~':
		case '%':
		case '+':
		    continue;
		default:
		    return i;
		}
	    }

	    return len;
	}

	private /* static */ final String [] href_prefix = {
	    "WWW.", "HTTP://", "HTTPS://", "FTP.", "FTP://"
	};

	public boolean isHref(String s) {
	    /* First pass: first char only and minimum length.
	       Filters most things out and is fast. */

	    char c = s.charAt(0);
	    if ('#' == c) {
		return true; // IRC Channel.
	    }

	    // Require at least 8 chars for URIs (ie. www.x.ar).
	    if (s.length() < 8) {
		return false;
	    }

	    c = Character.toUpperCase(c);
	    boolean first_pass_failed = true;
	    for (int i = 0; i < href_prefix.length; i++) {
		if (c == href_prefix[i].charAt(0)) {
		    first_pass_failed = false;
		    break;
		}
	    }

	    if (first_pass_failed) {
		return false;
	    }

	    /* Second pass: exact match. */

	    String up = s.toUpperCase();
	    for (int i = 0; i < href_prefix.length; i++) {
		if (up.startsWith(href_prefix[i])) {
		    return true;
		}
	    }

	    return false;
	}

	public void reconstruct() // reconstruct ContentLine (prepare for display)
	{ int x=borderx;
	int y=0; // inside a line think from top left as (0,0)
	int line=0;
	for (Enumeration e=runs.elements(); e.hasMoreElements(); )
	    { Run run=(Run)e.nextElement();
	    line=run.setXY(x, y, line);
	    x=run.pixX1; //+fmSpaceFonts;
	    y=run.pixY1;
	    }
	lineDY=line+1;
	}

	int paint(Graphics g, int lineRevY)
	{ if (lineRevY>=-lineDY+1) // clipping of other edge in SmileyTextAreaArea.paint()
	    { //int y1=dimy-bordery-1-fmDescent-lineRevY*fmDY-(lineDY-1)*fmDY;
		int y=dimy-bordery-(lineRevY+lineDY)*fmDY; // top
		g.setColor(backgroundColor);
		g.fillRect(2,y,dimx-3,lineDY*fmDY);
		y+=fmDY-fmDescent; // baseline of top text line
		int n=0;
		for (Enumeration e=runs.elements(); e.hasMoreElements(); )
		    { Run run=(Run)e.nextElement();
		    run.paint(g, y, (n++)==0);
		    }
	    }
	return(lineRevY+lineDY);
	}

	public void add(Run run) {
	    runs.addElement(run);
	}
    }
    // ============ End of class ContentLine ============

    private Vector contentLines; // instance variable for the list of ContentLines

  //=======================================================================
  //                            Constructor
  //=======================================================================

    public SmileyTextAreaArea(SmileyTextArea sta, Scrollbar sb, HyperlinkReceiver hr)
    { super();
    this.sta=sta;
    this.sb=sb;
    hyperlinkReceiver=hr;
    fmDY=1;

    SB_WORKAROUND_ENABLED=((new Scrollbar(Scrollbar.HORIZONTAL,20,10,0,20)).getValue()==20);

    backgroundColor=fixedColors[bgColorIndex];
    super.setBackground(backgroundColor); // for consistency and against flickering
    foregroundColor=fixedColors[fgColorIndex];
    super.setForeground(foregroundColor); // for consistency only

    //prepareFont(getFont());  getFont()==null at this point of time
    prepareFont(sta.DEFAULT_FONT.getName(),sta.DEFAULT_FONT.getSize());

    contentLines=new Vector();

    sbLength=0;
    sbValue=0;
    correctSb();
    sb.addAdjustmentListener
	( new AdjustmentListener()
	    { public void adjustmentValueChanged(AdjustmentEvent e)
		{ setRawSbValue(e.getValue());
		}
	    }
	  );

    /* Does not work since this does not get the keyboard focus... see focus_frame
       addFocusListener
       ( new FocusAdapter()
       { public void focusGained(FocusEvent e)
       { last_action_scrolling=false;
       //System.out.println("focusGained");
       repaint();
       }
       }
       );*/

    addComponentListener
	( new ComponentAdapter()
	    { public void componentResized(ComponentEvent e)
		{ dimensionChange();
		}
		public void componentShown(ComponentEvent e)
		{ last_action_scrolling=false;
		repaint();
		}
	    }
	  );

    dimensionChange(); // initialize dimension by same means

    addMouseListener(this);
    addMouseMotionListener(this);

    //had no effect... see paint()
    //if (sta.staticinitialized)
    //{ for (Enumeration e=sta.smileys.elements(); e.hasMoreElements(); )
    //  { Image image=(Image)e.nextElement();
    //    prepareImage(image,null);
    //  }
    //}
    }

    // Sort of passive construction step...
    private java.awt.Container focus_element=null;
    public void addNotify()
    { super.addNotify();
    if (focus_element==null)
	{ focus_element=getContainer();
	if (focus_element!=null)
	    { focus_element.addFocusListener
		  ( new FocusAdapter()
		      { public void focusGained(FocusEvent e) // if above container gets the focus...
			  { last_action_scrolling=false;        // ...redisplay, because hidden areas...
			  repaint();                          // ...trash the view partially.
			  }
		      }
		    );
	    }
	}
    }
    java.awt.Container getContainer()
    { Container c = getParent();
    do
	{ if (c!=sta &&
	      c instanceof java.awt.Container) return ((java.awt.Container)c);
	else                                 c = c.getParent();
	} while (c != null);
    return null;
    }

    //=======================================================================
    //                            Private Utils
    //=======================================================================

    private void correctSb()
    { int len=dimyv/fmDY;
    int scrollmax=sbLength-len; if (scrollmax<0) scrollmax=0;
    if (sbValue>scrollmax) sbValue=scrollmax; // needed for dimensionChange
    if (sbValue<0) sbValue=0;
    int value=scrollmax-sbValue; if (value<0) value=0;
    sb.setValues(value,len,0,(SB_WORKAROUND_ENABLED?scrollmax:scrollmax+len));
    }
    private void setRawSbValue(int value) // scrolling called from outside
    { boolean dorepaint=false;
    // this synchonization is not very critical and it costs too much time:
    //synchronized(this) // to be exact, it should be synchronized, because called from outside
    //{
    int len=dimyv/fmDY;
    int scrollmax=sbLength-len; if (scrollmax<0) scrollmax=0;
    int sbValueOld=sbValue;
    sbValue=scrollmax-value; if (sbValue<0) sbValue=0;
    dorepaint=(sbValueOld!=sbValue);
    //}
    if (dorepaint)
	{ //last_action_scrolling=true;  (at end of paint())
	    repaint();
	}
    }

    private void prepareFont(String name, int size)
    { usedFonts[0]=new Font(name,Font.PLAIN,           size);
    usedFonts[1]=new Font(name,Font.BOLD,            size);
    usedFonts[2]=new Font(name,Font.ITALIC,          size);
    usedFonts[3]=new Font(name,Font.BOLD|Font.ITALIC,size);
    fmDY=0;
    fmDescent=0;
    fmAscent=0;
    fmLeading=9999;
    for (int i=0; i<4; i++)
	{ FontMetrics fm=getFontMetrics(usedFonts[i]);
	fmFonts[i]=fm;
	fmSpaceFonts+=fm.stringWidth(" ");
	fmDescentFonts[i]=fm.getMaxDescent();
	fmAscentFonts[i]=fm.getMaxAscent();
	//if (fmDY<fm.getHeight()) fmDY=fm.getHeight();                   // fmDY is max.(height)
	if (fmDescent<fm.getMaxDescent()) fmDescent=fm.getMaxDescent(); // fmDescent is max.
	if (fmAscent<fm.getMaxAscent()) fmAscent=fm.getMaxAscent();   // fmAscent is max.
	if (fmLeading>fm.getLeading()) fmLeading=fm.getLeading();     // fmLeading is min.
	}
    if (fmLeading<0) fmLeading=0;
    fmDY=fmDescent+fmAscent+fmLeading; // construction of abstract fm.getHeight() for all fonts
    fmSpaceFonts=(fmSpaceFonts+2)/4; // +2 in order to round; now fmSpaceFonts is mean value
    if (fmDY<fmDescent+fmAscent+fmLeading) // fmDY could be too small!
	fmDY=fmDescent+fmAscent+fmLeading;
    correctSb();
    }

    private void dimensionChange()
    { synchronized(this) // must be synchronized, because called from outside
	{ Dimension d=getSize();
	dimx=d.width;
	dimy=d.height;
	dimxv=dimx-borderx*2;
	dimyv=dimy-bordery*2;

	sbLength=0;
	for (int i=contentLines.size()-1; i>=0; i--)
	    { ContentLine cl=(ContentLine)contentLines.elementAt(i);
	    cl.reconstruct();
	    sbLength+=cl.lineDY;
	    }
	if (sbValue>sbLength) sbValue=sbLength;
	correctSb();
	}
    last_action_scrolling=false;
    invalidate();
    repaint();
    }

    private Run getRun(Point p) // searching the run is analogous paint
    { if (usedFonts[3]==null) return(null);
    int y0=dimy-bordery+sbValue*fmDY;
    if (p.y>dimy-bordery) return(null);
    if (p.y<bordery) return(null);
    for (int i=contentLines.size()-1; i>=0; i--)
	{ ContentLine cl=(ContentLine)contentLines.elementAt(i);
	y0-=cl.lineDY*fmDY;
	if (p.y>y0)
	    { // cl found, now search the run
		y0+=fmLeading;
		int y=p.y-y0;
		int dyline=fmAscent+fmDescent;
		for (Enumeration e=cl.runs.elements(); e.hasMoreElements(); )
		    { Run run=(Run)e.nextElement();
		    if (run.pixY1==run.pixY0) // only one line, check X0 and X1
			{ if (p.x>run.pixX0 && p.x<run.pixX1) return(run);
			else continue;
			}
		    else if (y<run.pixY0+dyline) // first line, check X0
			{ if (p.x>run.pixX0) return(run);
			else continue;
			}
		    else if (y<run.pixY1) // between first and last line
			{ return(run);
			}
		    else if (y<run.pixY1) // last line, check X1
			{ if (p.x<run.pixX1) return(run);
			else continue;
			}
		    }
		break; // return null
	    }
	}
    return(null);
    }

    int paint_i=0;

    private final void deleteFirstLine()
    { if (contentLines.isEmpty()) return;
    sbLength-=((ContentLine)(contentLines.firstElement())).lineDY;
    contentLines.removeElementAt(0);
    if (paint_i>0) paint_i--; // undo the effect of removeElementAt for paint if paint runs
    if (sbValue>sbLength) sbValue=sbLength; // not really nescessary...
    correctSb();
    }
    //=======================================================================
    //                           Update & Paint
    //=======================================================================

    public void update(Graphics g)
    { inupdate=true;
    paint(g); // everything in paint, no clear, but no need to call from here (no effect)
    inupdate=false;
    }

    boolean smileys_myinit=true;

    public void paint(Graphics g)
    // not synchronized (should be to be 100% correct, but this costs too much time and only
    // small display errors can occur, which should be repaired at least after some scrolling)
    { if (usedFonts[3]==null)
	{ Font f=getFont();
	if (f==null) return; // must have a font...
	prepareFont(f.getName(),f.getSize());
	}

    // Is it safe to comment this out?

    //      if (smileys_myinit && sta.staticinitialized) // smileys did not show up at first drawImage
    //      { for (Enumeration e=sta.smileys.elements(); e.hasMoreElements(); )
    //        { Image image=(Image)e.nextElement();
    //          g.drawImage(image,1,1,null);
    //        }
    //        smileys_myinit=false;
    //      }

    int dyLines=sbValue-oldSbValue;
    int dy=dyLines*fmDY;
    oldSbValue=sbValue;
    boolean scrolling=(java.lang.Math.abs(dy)<(dimy*2)/3);
    if (!inupdate && !inappend) scrolling=false; // scrolling only in update() or append()
    if (!last_action_scrolling) scrolling=false; // scrolling only if scrolling or append
    if (mode==SAFE) scrolling=false; // scrolling is possibly not safe
    if (!isShowing())scrolling=false; // may be overlapped by another window
    if (scrolling && dy!=0)
	{ if (mode==SMOOTH)
	    { int step=dy/fmDY; // so far exact divison (rest==0)
	    for (int yy=0; java.lang.Math.abs(yy)<java.lang.Math.abs(dy); yy+=step) {
		if (yy != 0) {
		    try {
  			Thread.sleep(5);
		    } catch (InterruptedException e) {
			// Do nothing.
		    }
		}
		if (dy<0)
		    g.copyArea(2,-step+2+(yy-dy+step),dimx-4,dimy-4+step+(dy-step),0,step); // up
		else
		    g.copyArea(2,2+yy,dimx-4,dimy-4-step-(dy-step),0,step); // down
		//area a littlebit too large (OK, but funny optics):
		//if (dy<0)
		//  g.copyArea(2,-step+2,dimx-4,dimy-4+step,0,step); // up
		//else
		//  g.copyArea(2,2,dimx-4,dimy-4-step,0,step); // down
		}
	    }
	else
	    { if (dy<0)
		g.copyArea(2,-dy+2,dimx-4,dimy-4+dy,0,dy); // up
	    else
		g.copyArea(2,2,dimx-4,dimy-4-dy,0,dy); // down
	    }
	}

    if (bordery>2)
	{ g.setColor(backgroundColor);
	g.fillRect(2,dimy-bordery,dimx-4,bordery-2);
	}
    int lineRevY=-sbValue;
    int maxLineRevY=(dimy-4)/fmDY; // top
    int minLineRevY=0; // bottom
    if (scrolling && dyLines>=0) minLineRevY=(dimy-2*bordery-2-dy)/fmDY;
    if (scrolling && dyLines<0) maxLineRevY=-dyLines-1;
    for (paint_i=contentLines.size()-1; paint_i>=0; paint_i--)
	{ ContentLine cl=(ContentLine)contentLines.elementAt(paint_i);
	if (lineRevY+cl.lineDY-1<minLineRevY) lineRevY+=cl.lineDY; // below (skip)
	else                                  lineRevY=cl.paint(g,lineRevY);
	if (lineRevY>maxLineRevY) break; // above (we are ready)
	}
    paint_i=0;
    if (!(scrolling && dyLines<=0)) // avoid clearing the top if scrolling up
	{ int topdrawn=dimy-bordery-(lineRevY)*fmDY;
	if (topdrawn>2) // clear rest above textlines, if  there is a rest
	    { g.setColor(backgroundColor);
	    g.fillRect(2,2,dimx-4,topdrawn);
	    }
	}
    g.setColor(new java.awt.Color(128,128,128));
    int tempx1[]={0,0,dimx-1};
    int tempy1[]={dimy-1,0,0};
    g.drawPolyline(tempx1,tempy1,3);
    g.setColor(new java.awt.Color(0,0,0));
    int tempx2[]={1,1,dimx-2};
    int tempy2[]={dimy-2,1,1};
    g.drawPolyline(tempx2,tempy2,3);
    g.setColor(new java.awt.Color(255,255,255));
    int tempx3[]={0,dimx-1,dimx-1};
    int tempy3[]={dimy-1,dimy-1,0};
    g.drawPolyline(tempx3,tempy3,3);
    int tempx4[]={1,dimx-2,dimx-2};
    int tempy4[]={dimy-2,dimy-2,1};
    g.drawPolyline(tempx4,tempy4,3);

    last_action_scrolling=true;
    }

    //=======================================================================
    //                            Mouse Events
    //=======================================================================

    public void mousePressed(MouseEvent ev) { }
    public void mouseReleased(MouseEvent ev) { }
    public void mouseEntered(MouseEvent ev) { }
    public void mouseExited(MouseEvent ev) { }

    public void mouseClicked(MouseEvent ev)
    { Run run=getRun(ev.getPoint());
    if (run!=null)
	{ if (!run.getUrl().equals("") && hyperlinkReceiver!=null)
	    hyperlinkReceiver.handleHyperlink(run.url);
	}
    }

    public void mouseMoved(MouseEvent ev)
    { Run run=getRun(ev.getPoint());
    if (run!=null)
	{ if (!run.getUrl().equals("") && hyperlinkReceiver!=null)
	    { setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    return;
	    }
	}
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void mouseDragged(MouseEvent ev) { }

    //=======================================================================
    //                        Component's User Interface
    //=======================================================================

    public void append(String s, boolean interpretUrl)
    { synchronized(this)
	{ ContentLine cl=new ContentLine(s,interpretUrl);
	cl.reconstruct();
	contentLines.addElement(cl);
	sbLength+=cl.lineDY;
	sbValue=0;
	if (oldSbValue==0) oldSbValue+=cl.lineDY;
	else               oldSbValue=-999;
	correctSb();
	if (bufferlen>=0 && contentLines.size()>bufferlen) deleteFirstLine();
	}

    //last_action_scrolling=true;  (at end of paint())
    repaint();
    /* if many messages are coming in, it is more efficient to call repaint everytime
       if (mode==SAFE) repaint();
       else
       { Graphics g=getGraphics();
       inappend=true;
       if (g==null) repaint();
       else         paint(g);
       inappend=false;
       }*/
    }
    public void setMode(int mode)
    { synchronized(this)
	{ this.mode=mode;
	last_action_scrolling=false;
	}
    repaint();
    }

    public void setSubsequentIndent(int indent)
    { synchronized(this)
	{ subsequentindent=indent;
	last_action_scrolling=false;
	}
    dimensionChange();
    // repaint(); (included in dimensionChange())
    }
    public int getSubsequentIndent()
    { return(subsequentindent);
    }

    public void setBufferlen(int bufferlen)
    { synchronized(this)
	{ this.bufferlen=bufferlen;
	while (bufferlen>=0 && contentLines.size()>bufferlen) deleteFirstLine();
	last_action_scrolling=false;
	}
    repaint();
    }
    public int getBufferlen()
    { return(bufferlen);
    }

    public void setBackground(Color c)
    { synchronized(this)
	{ fixedColors[bgColorIndex]=c;
	backgroundColor=c;
	super.setBackground(backgroundColor); // for consistency and against flickering
	last_action_scrolling=false;
	}
    repaint();
    }
    public Color getBackground()
    { return(fixedColors[bgColorIndex]);
    }
    public void setForeground(Color c)
    { synchronized(this)
	{ fixedColors[fgColorIndex]=c;
	foregroundColor=c;
	super.setForeground(foregroundColor); // for consistency only
	last_action_scrolling=false;
	}
    repaint();
    }
    public Color getForeground()
    { return(fixedColors[fgColorIndex]);
    }
    public void setColorPaletteEntry(int n, Color c)
    { synchronized(this)
	{ fixedColors[(n % 16)]=c;
	last_action_scrolling=false;
	}
    repaint();
    }
    public Color getColorPaletteEntry(int n)
    { return(fixedColors[(n % 16)]);
    }

    public void setFont(String name, int size)
    { synchronized(this)
	{ prepareFont(name, size);
	last_action_scrolling=false;
	}
    dimensionChange();
    // repaint(); (included in dimensionChange())
    }
    public Font getFont()
    { return(usedFonts[0]);
    }
}
