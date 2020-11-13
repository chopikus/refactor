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

import java.util.*;

public class Commands extends ListResourceBundle {

    // Always use lower case for command tags! (See getCommand method below.)
    private static final Object [][] contents = {
	// Internally handled commands.
	{ "help", new Command("HELP", 0, 1) },
	{ "quote", new Command("QUOTE", 1) },
	{ "join", new Command("JOIN", 1, 2) },
	{ "j", "join" },
	{ "part", new Command("PART", 0, 2) },
	{ "ping", new Command("PING", 1) },
	{ "quit", new Command("QUIT", 0, 1) },
	{ "nick", new Command("NICK", 1) },
	{ "me", new Command("ME", 1) },
	{ "ctcp", new Command("CTCP", 2) },
	{ "eirc", new Command("EIRC", 0) },
	{ "kban", new Command("KBAN", 1, 2) },
	{ "ignore", new Command("IGNORE", 0, 1)  },
	{ "unignore", new Command("UNIGNORE", 1) },
	{ "server", new Command("SERVER", 1, 2) },
	{ "query", new Command("QUERY", 1, 2) },
	{ "q", "query" },

	// Standard IRC commands.
	{ "oper", new Command("OPER", 2) },
	{ "mode", new Command("MODE", 1, 4) },
	{ "squit", new Command("SQUIT", 2) },
	{ "topic", new Command("TOPIC", 1, 2) },
	{ "names", new Command("NAMES", 0, 2) },
	{ "list", new Command("LIST", 0, 2) },
	{ "invite", new Command("INVITE", 2) },
	{ "kick", new Command("KICK", 2, 3) },
	{ "privmsg", new Command("PRIVMSG", 2) },
	{ "msg", "privmsg" },
	{ "notice", new Command("NOTICE", 2) },
	{ "motd", new Command("MOTD", 0, 1) },
	{ "lusers", new Command("LUSERS", 0, 2) },
	{ "version", new Command("VERSION", 0, 1) },
	{ "stats", new Command("STATS", 0, 2) },
	{ "links", new Command("LINKS", 0, 2) },
	{ "time", new Command("TIME", 0, 1) },
	{ "connect", new Command("CONNECT", 2, 3) },
	{ "trace", new Command("TRACE", 0, 1) },
	{ "admin", new Command("ADMIN", 0, 1) },
	{ "info", new Command("INFO", 0, 1) },
	{ "who", new Command("WHO", 0, 2) },
	{ "whois", new Command("WHOIS", 1, 2) },
	{ "whowas", new Command("WHOWAS", 1, 2) },
	{ "kill", new Command("KILL", 0, 2) },
	{ "away", new Command("AWAY", 0, 1) },
    };

    public Object [][] getContents() {
	return contents;
    }

    /**
     * Parses command-line style input.
     *
     * @param text the string to parse.
     * @return a <code>String</code> array filled with the command name, followed by its parameters.
     * @throws MissingResourceException if the command can't be found by Commands.getCommand().
     * @throws IllegalArgumentException if there are missing parameters.
     */
    public static String [] parseCommand(ResourceBundle cmds, String text)
	throws MissingResourceException, IllegalArgumentException {
	StringTokenizer st = new StringTokenizer(text);

	/* Find command description. */

	// The next line may throw a MissingResourceException.
	Command c = Commands.getCommand(cmds, st.nextToken());
	String action = c.getTag();
	int required = c.getRequiredParameters();
	int accepted = c.getAcceptedParameters();

	Vector result = new Vector();

	result.addElement(action);

	/* Parse parameters. */

	final int tokens = st.countTokens();
	if (tokens < required) {
	    throw new IllegalArgumentException("insuficient parameters");
	}

	// Add parameters, omit last one because it needs special parsing.
	for (int i = 0; i < Math.min(accepted, tokens) - 1; i++) {
	    result.addElement(st.nextToken());
	}

	if (tokens > 0) {
	    // Add the last parameter, which is allowed to contain spaces.
	    String tok = st.nextToken("");
	    result.addElement(tok.trim());
	}

	String [] ret = new String [result.size()];
	result.copyInto(ret);

	return ret;
    }

    /**
     * This method resolves Command aliases.
     */
    public static Command getCommand(ResourceBundle cmds, String key)
	throws MissingResourceException {
	Object o = key.toLowerCase();

	while ((o = cmds.getObject((String) o)) instanceof String) {
	}

	return (Command) o;
    }
}

class Command {
    private int required;
    private int accepted;
    private String tag;

    public Command(String tag, int number) {
	this(tag, number, number);
    }

    public Command(String tag, int required, int accepted) {
	this.tag = tag;
	this.required = required;
	this.accepted = accepted;
    }

    public String getTag() {
	return tag;
    }

    public int getRequiredParameters() {
	return required;
    }

    public int getAcceptedParameters() {
	return accepted;
    }
}
