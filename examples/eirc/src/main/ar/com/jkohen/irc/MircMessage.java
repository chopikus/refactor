/*
Eteria IRC Client, an RFC 1459 compliant client program written in Java.
Copyright (C) 2000-2001  Javier Kohen <jkohen at tough.com>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package ar.com.jkohen.irc;

import java.awt.Color;
import java.text.ParseException;
import java.util.ResourceBundle;

public class MircMessage extends Message {
    public static final char BOLD = '\002'; // ^B
    public static final char COLOR = '\003'; // ^C
    public static final char BELL = '\007'; // ^G
    public static final char RESET = '\017'; // ^O
    public static final char ITALIC = '\024'; // ^T
    public static final char REVERSE = '\026'; // ^V
    public static final char UNDERLINE = '\037'; // ^_

    public MircMessage(String msg) throws ParseException {
	super(msg);
	attrDecodeParameters();
    }

    public MircMessage(String command, String [] parameters) {
	this("", command, parameters);
    }

    public MircMessage(String prefix, String command, String [] parameters) {
	super(prefix, command, parameters);
	attrDecodeParameters();
    }

    public String [] getParameters() {
	String [] a = new String [parameters.length];

	for (int i = 0; i < parameters.length; i++) {
	    a[i] = filterMircAttributes(parameters[i]);
	}

	return a;
    }

//      public static int getColorIndex(Color c) {
//  	for (int i = 0; i < COLOR_TABLE.length; i++) {
//  	    if (c.equals(COLOR_TABLE[i])) {
//  		return i;
//  	    }
//  	}

//  	return -1;
//      }

    /* Blamelessly cut-and-pasted from XChat, then ported to Java.
     */
    public static String filterMircAttributes(String text) {
        int nc = 0;
        int i = 0, j = 0;
	boolean color = false;
	int len = text.length();
	char new_str[] = new char [len];

	while (len > 0) {
	    char ch = text.charAt(i);

	    if ((color && Character.isDigit(ch) && nc < 2)
		|| (color && ch == ',' && nc < 3)) {
		nc++;
		if (ch == ',') {
		    nc = 0;
		}
	    } else {
		if (color) {
		    color = false;
		}
		switch (ch) {
		case MircMessage.COLOR:
		    color = true;
		    nc = 0;
		    break;
		case MircMessage.BOLD:
		case MircMessage.RESET:
		case MircMessage.UNDERLINE:
		case MircMessage.ITALIC:
		case MircMessage.REVERSE:
		case MircMessage.BELL:
		    break;
		default:
		    new_str[j] = ch;
		    j++;
		}
	    }
	    i++;
	    len--;
	}

	return String.valueOf(new_str, 0, j);
    }

    /**
     * Replaces all control codes found in <code>src</code> by their corresponding escaped codes.
     */
    public static String attrEncode(String src) {
	StringBuffer buf = new StringBuffer();
	char [] ref = src.toCharArray();

	for (int i = 0; i < ref.length; i++) {
	    buf.append(attrEncode(ref[i]));
	}

	return buf.toString();
    }

    public static String attrEncode(char c) {
	char subst;

	switch (c) {
	case BOLD:
	    subst = 'B';
	    break;
	case COLOR:
	    subst = 'C';
	    break;
	case BELL:
	    subst = 'A';
	    break;
	case RESET:
	    subst = 'O';
	    break;
	case ITALIC:
	    subst = 'I';
	    break;
	case REVERSE:
	    subst = 'R';
	    break;
	case UNDERLINE:
	    subst = 'U';
	    break;
	default:
	    return String.valueOf(c);
	}

	return ("%" + subst);
    }

    /**
     * Replaces all escaped codes found in <code>src</code> by their corresponding control codes.
     */
    public static String attrDecode(String src) {
	StringBuffer buf = new StringBuffer();
	char [] ref = src.toCharArray();

	for (int i = 0; i < ref.length; i++) {
	    // If it's not an escape character or it's the last character, append it as it is.
	    if ('%' != ref[i] || i == ref.length - 1) {
		buf.append(ref[i]);
		continue;
	    }

	    char subst;

	    i++; // Advance to the next character and try to decode it.

	    switch (ref[i]) {
	    case 'B':
		subst = BOLD;
		break;
	    case 'C':
		subst = COLOR;
		break;
	    case 'A':
		subst = BELL;
		break;
	    case 'O':
		subst = RESET;
		break;
	    case 'I':
		subst = ITALIC;
		break;
	    case 'R':
		subst = REVERSE;
		break;
	    case 'U':
		subst = UNDERLINE;
		break;
	    case '%':
		subst = '%'; // If double % appears, only one must be sent.
		break;
	    default:
		buf.append('%'); // The escape character must remain if it isn't part of an escaped code.
		subst = ref[i];
		break;
	    }

	    buf.append(subst);
	}

	return buf.toString();
    }

    /**
     * Helper method that passes all parameters through attrDecode.
     */
    private void attrDecodeParameters() {
	for (int i = 0; i < parameters.length; i++) {
	    parameters[i] = attrDecode(parameters[i]);
	}
    }
}
