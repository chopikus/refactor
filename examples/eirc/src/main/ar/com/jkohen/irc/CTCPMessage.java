/*
Eteria IRC Client, an RFC 1459 compliant client program written in Java.
Copyright (C) 2000  Javier Kohen <jkohen at tough.com>

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

import java.util.Hashtable;

public class CTCPMessage {
    public static final int UNKNOWN = 0;
    public static final int ACTION = 1;
    public static final int PING = 2;
    public static final int DCC = 3;
    public static final int VERSION = 4;

    private int command;
    private String command_string;
    private String parameter;
    private boolean hasParameter;
    private static Hashtable command_list;

    static {
	command_list = new Hashtable();
	command_list.put("action", new Integer(ACTION));
	command_list.put("ping", new Integer(PING));
	command_list.put("dcc", new Integer(DCC));
	command_list.put("version", new Integer(VERSION));
    }

    public CTCPMessage(String msg) {
	int end = msg.length();
	if (msg.endsWith("\001")) { // No ending \001.
	    end--;
	}

	String ctcp = msg.substring(1, end);
	end = ctcp.indexOf(' ');

	if (-1 == end) {
	    this.command_string = ctcp;
	} else {
	    this.command_string = ctcp.substring(0, end);

	    // Extract the parameter.
	    int len = ctcp.length();
	    int begin = end + 1;
	    if (begin < len) {
		this.hasParameter = true;
		this.parameter = ctcp.substring(begin, len);
	    }
	}

	Integer cmd = (Integer) command_list.get(command_string.toLowerCase());
	this.command = null != cmd ? cmd.intValue() : UNKNOWN;
    }

    public CTCPMessage(String command_string, String parameter) {
	this.command_string = command_string;
	this.parameter = parameter;
	this.hasParameter = true;

	Integer cmd = (Integer) command_list.get(command_string.toLowerCase());
	this.command = null != cmd ? cmd.intValue() : UNKNOWN;
    }

    public String toString() {
	String t = "\001".concat(command_string);

	if (hasParameter) {
	    t = t.concat(" ").concat(parameter);
	}

	return t.concat("\001");
    }

    public int getCommand() {
	return command;
    }

    public String getCommandString() {
	return command_string;
    }

    public String getParameter() {
	return parameter;
    }

    public boolean hasParameter() {
	return hasParameter;
    }

    public static boolean isCTCPMessage(String s) {
	// We don't check for the ending delimiter.
	return ('\001' == s.charAt(0));
    }
}
