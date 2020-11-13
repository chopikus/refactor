package com.splendid.awtchat;

/*
Component SmileyTextArea,
  which is intended to be part of Eteria IRC Client from Javier Kohen
2001 written by Frank Bartels for Splendid Internet GmbH, Kiel, Germany
*/

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import ar.com.jkohen.util.Resource;

public class SmileyTextArea extends java.awt.Panel
{
  public static Font DEFAULT_FONT=
    new Font("Dialog", Font.PLAIN, 12); // ("Dialog"/10->wrong width-sizes on my compi (?))

  // mode's possible values
  public final static int SMOOTH=0;
  public final static int FAST=1;
  public final static int SAFE=2;

  private SmileyTextAreaArea smileytext; // the text area
  private Scrollbar scrollbar;           // the scrollbar

  // stuff for saving the smileys (prepared on init())
  static Hashtable smileys=null;
  static int maxCodeLength=1;
  static boolean staticinitialized=false;

  // ====== Constructor, init() ======

  /** Constructor. supply HyperlinkReceiver for getting the clicked URLs
    */
  public SmileyTextArea(HyperlinkReceiver hr)
  { super();
    setLayout(new BorderLayout());
    scrollbar=new Scrollbar(Scrollbar.VERTICAL,5,5,0,10); //val,vis,min,max 0,5,0,10->0..5
    smileytext=new SmileyTextAreaArea(this,scrollbar,hr);
    add(smileytext,"Center");
    add(scrollbar,"East");
    init();
  }

  //static
  //{ init();  Now called in constructor (only once); advantage: nicer error messages if any
  //}

  // Is called above
  public static void init()
  { //this.sp=sp;
    if (staticinitialized) return;

    smileys=new Hashtable();

    // Read smileys from resource file
    // EIRC WORKAROUND #35.
//      ResourceBundle images=ResourceBundle.getBundle("com.splendid.awtchat.Smileys");
    ResourceBundle images=ResourceBundle.getBundle("Smileys");
    maxCodeLength=1;
    for (Enumeration e=images.getKeys(); e.hasMoreElements() ;)
    { String key=(String)e.nextElement();
      // this version needs entries like (you could use 123 or thisnthat instead of sad):
      // smiley.sad.emoticons=:-( :( :-<
      // smiley.sad.image=smileys/sm_sad.gif
      int imagepos=key.indexOf(".image");
      if (key.indexOf("smiley.")==0 && imagepos==key.length()-6)
      { try
        { String filename=images.getString(key);
          String smiley=images.getString(key.substring(0,imagepos)+".emoticons");
          StringTokenizer st = new StringTokenizer(smiley);
          while (st.hasMoreTokens())
          { String code=(String)st.nextToken();   // the other are the ascii codes
            addSmiley(filename,code);
            if (code.length()>maxCodeLength) maxCodeLength=code.length();
          }
        }
        catch(MissingResourceException mre){} // ignore errors
      }
      // this version commented out: smiley.sad=smileys/sm_sad.gif :-( :( :-<
      /*if (key.indexOf("smiley.")==0)
      { String smiley=images.getString(key);
        StringTokenizer st = new StringTokenizer(smiley);
        String filename=(String)st.nextToken(); // first token is filename
        while (st.hasMoreTokens())
        { String code=(String)st.nextToken();   // the other are the ascii codes
          addSmiley(filename,code,sp);
          if (code.length()>maxCodeLength) maxCodeLength=code.length();
        }
      }*/
    }
    staticinitialized=true;
  }

  private static void addSmiley(String filename, String code)
  { try
    { Image image=
          Toolkit.getDefaultToolkit().createImage(Resource.getResource(filename, Class.forName("com.splendid.awtchat.SmileyTextArea")));
      smileys.put(code,image);
    }
    catch (ClassNotFoundException e) { System.err.println(e); }
    catch (IOException e) { System.err.println(e); }
  }

  //public synchronized void addTextListener(TextListener l)
  //{
  //}

  // ====== SubsequentIndent ======

  /** set the indent of subsequent lines (>=0) if a line must be split (default is 12)
    */
  public void setSubsequentIndent(int indent) // allowed values: 0 and greater
  { if (indent<0) indent=0;
    smileytext.setSubsequentIndent(indent);
  }
  /** get the indent of subsequent lines if a line must be split (default is 12)
    */
  public int getSubsequentIndent()
  { return(smileytext.getSubsequentIndent());
  }

  // ====== Colors ======

  /** change the background color of the display area, initially it is white
    */
  public void setBackground(Color c)
  { smileytext.setBackground(c);
  }

  /** get the background color of the display area
    */
  public Color getBackground()
  { return(smileytext.getBackground());
  }

  /** change the foreground color of the text in the display area, initially it is black
    */
  public void setForeground(Color c)
  { smileytext.setForeground(c);
  }

  /** get the foreground color of the text in the display area, initially it is black
    */
  public Color getForeground()
  { return(smileytext.getForeground());
  }

  /** change one of the color paette entries (n may be a number from 0 to 15,
    * it corresponds to the number associated with MircMessage.COLOR IRC attribute)
    */
  public void setColorPaletteEntry(int n, Color c)
  { smileytext.setColorPaletteEntry(n, c);
  }

  /** get one of the color paette entries (n may be a number from 0 to 15,
    * it corresponds to the number associated with MircMessage.COLOR IRC attribute)
    */
  public Color getColorPaletteEntry(int n)
  { return(smileytext.getColorPaletteEntry(n));
  }

  /** get the static color palette.
    */
  public static Color [] getColorPalette()
  { return(SmileyTextAreaArea.fixedColors);
  }

  // ====== Font ======

  /** change the font (only name and size are used)
    */
  public void setFont(Font f)
  { smileytext.setFont(f.getName(),f.getSize());
  }

  /** change the font
    */
  public void setFont(String name, int size)
  { smileytext.setFont(name,size);
  }

  /** get the actual base font (not bold and not italic)
    */
  public Font getFont()
  { return(smileytext.getFont());
  }

  // ====== Append ======

  /** set the length of the text buffer (number of appends after which old text is deleted)
      (set -1 for infinite buffer)
    */
  public void setBufferlen(int bufferlen)
  { smileytext.setBufferlen(bufferlen);
  }

  /** get the length of the text buffer (number of appends after which old text is deleted)
      (-1 means infinite buffer)
    */
  public int getBufferlen()
  { return(smileytext.getBufferlen());
  }

  // ====== Append ======

  /** insert a line of text (smileys, URLs and IRC attributes interpreted)
    * (URLs == http://* | https://* | ftp://* | www.* | #*  (case is ignored))
    * (IRC attributes are (from MircMessage) BELL, BOLD, ITALIC,
    *  UNDERLINE, COLOR, REVERSE, RESET)
    */
  public void append(String s)
  { smileytext.append(s,true);
  }

  /** same as append, except, optionally URL interpretation is suppressed (channels, email, web-adresses)
    */
  public void append(String s, boolean url)
  { smileytext.append(s,url);
  }

  /** Scrolling display mode, may be SMOOTH, FAST, SAFE (initially it is SMOOTH)
    * Set mode to FAST if problems arise with smooth scrolling
    * Set mode to SAFE if problems arise with any scrolling (always pains the whole)
    */
  public void setMode(int mode)
  { smileytext.setMode(mode);
  }
}
