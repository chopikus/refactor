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

import java.text.ParseException;
import java.util.*;

public class Message {
    protected String prefix;
    protected String command;
    protected String [] parameters;

    public Message(String msg) throws ParseException {
	if (0 == msg.trim().length()) {
	    throw new ParseException("Empty message", 0);
	}

	StringTokenizer st = new StringTokenizer(msg, " ");
	String t;

	if (!st.hasMoreTokens()) {
	    throw new ParseException("empty message", 0);
	}
	t = st.nextToken();

	// Let's see if there's a prefix
	if (t.charAt(0) == ':') {
	    if (t.length() == 1) {
		throw new ParseException("empty prefix", 1);
	    }
	    this.prefix = t.substring(1);
	    if (!st.hasMoreTokens()) {
		throw new ParseException("command expected", 0);
	    }
	    t = st.nextToken();
	} else {
	    this.prefix = "";
	}

	// Then we should have received the command
	this.command = t;
	if (!st.hasMoreTokens()) {
	    throw new ParseException("parameters expected", 0);
	}

	Vector tmp_params = new Vector();
	st = new StringTokenizer(st.nextToken(""), " ");
	while (st.hasMoreTokens()) {
	    t = st.nextToken();
	    if (t.charAt(0) == ':') {
		// We found the last parameter, it needs special treatment.
		break;
	    }
	    tmp_params.addElement(t);
	}

	// We want to pass the last parameter unparsed, therefore, we can't use the output of StringTokenizer; we have to work with the original message.
	// Start looking one char past the beginning to avoid finding the prefix marker.
	int colon_index = msg.indexOf(':', 1);
	if (-1 != colon_index) {
	    tmp_params.addElement(msg.substring(colon_index + 1));
	}

	this.parameters = new String [tmp_params.size()];
	tmp_params.copyInto(parameters);
    }

    public Message(String command, String [] parameters) {
	this("", command, parameters);
    }

    public Message(String prefix, String command, String [] parameters) {
	this.prefix = prefix;
	this.command = command;

	this.parameters = new String [parameters.length];
	for (int i = 0; i < parameters.length; i++) {
	    this.parameters[i] = parameters[i];
	}
    }

    public String toString() {
	String pre = "";

	if (0 != prefix.length()) {
	    pre += ":" + prefix + " ";
	}

	return (pre + command + getParametersString() + "\r\n");
    }

    /* Produce as many 512-byte strings as needed to represent the message.
     * PREFIX and COMMAND are included in every fragment.
     */
    public String [] slices() {
	String pre = "";

	if (0 != prefix.length()) {
	    pre += ":" + prefix + " ";
	}
	pre += command;

	String params = getParametersString();

	Vector v = new Vector();
	// Length is 510 instead of 512 because of the "\r\n" appended later.
	final int max_slice_len = 510 - pre.length();
	int i = 0;
	do {
	    int slice_len = Math.min(max_slice_len, params.length() - i);
	    String slice = params.substring(i, i + slice_len);
	    v.addElement(pre + slice + "\r\n");
	    i += slice_len;
	} while (i < params.length());

	String [] slices = new String [v.size()];
	v.copyInto(slices);

	return slices;
    }

    public String getPrefix() {
	return prefix;
    }

    public String getCommand() {
	return command;
    }

    public String [] getParameters() {
	return parameters;
    }

    protected String getParametersString() {
	StringBuffer buf = new StringBuffer();

	if (parameters.length > 0) {
	    for (int i = 0; i < parameters.length - 1; i++) {
		buf.append(' ').append(parameters[i]);
	    }

	    buf.append(" :").append(parameters[parameters.length - 1]);
	}

	return buf.toString();
    }
}
