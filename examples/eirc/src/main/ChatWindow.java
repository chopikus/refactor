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

import java.awt.*;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import ar.com.jkohen.irc.MircMessage;
import ar.com.jkohen.util.ConfigurationProperties;

public abstract class ChatWindow extends OutputWindow implements Observer {
    /* Configuration properties. */
    private boolean prefix_own;

    private static final ResourceBundle msg
	= ResourceBundle.getBundle("message");

    protected static final MessageFormat ACTION
	= new MessageFormat(msg.getString("action"));
    protected static final MessageFormat PRIVMSG
	= new MessageFormat(msg.getString("privmsg"));
    protected static final MessageFormat MY_ACTION
	= new MessageFormat(msg.getString("my_action"));
    protected static final MessageFormat MY_NOTICE
	= new MessageFormat(msg.getString("my_notice"));
    protected static final MessageFormat MY_PRIVMSG
	= new MessageFormat(msg.getString("my_privmsg"));

    public ChatWindow(String title) {
	this(title, Locale.getDefault());
    }

    public ChatWindow(String title, Locale locale) {
	super(title, locale);
    }

    public void printAction(String s, String whom) {
	printAction(s, whom, true);
    }

    public void printAction(String s, String whom, boolean url) {
	Object [] o = { whom, s };
	text_canvas.append(ACTION.format(o), url);

	postTextEvent();
    }

    public void printMyAction(String s) {
	printMyAction(s, true);
    }

    public void printMyAction(String s, boolean url) {
	Object [] o = { getNick(), s };
	text_canvas.append(MY_ACTION.format(o), url);

	postTextEvent();
    }

    public void printPrivmsg(String s, String whom) {
	printPrivmsg(s, whom, true);
    }

    public void printPrivmsg(String s, String whom, boolean url) {
	Object [] o = { whom, s };
	text_canvas.append(PRIVMSG.format(o), url);

	postTextEvent();
    }

    public void printMyPrivmsg(String s) {
	printMyPrivmsg(s, true);
    }

    public void printMyPrivmsg(String s, boolean url) {
	Object [] o = { prefix_own ? getNick() : "", s };
	text_canvas.append(MY_PRIVMSG.format(o), url);

	postTextEvent();
    }

    public void printMyNotice(String s) {
	printMyNotice(s, true);
    }

    public void printMyNotice(String s, boolean url) {
	Object [] o = { prefix_own ? getNick() : "", s };
	text_canvas.append(MY_NOTICE.format(o), url);

	postTextEvent();
    }

    public void update(Observable o, Object arg) {
	ConfigurationProperties props = (ConfigurationProperties) o;

	if (null == arg || arg.equals("prefix_own")) {
	    this.prefix_own = props.getBoolean("prefix_own");
	}
    }

    /* Returns the user's current nick */
    protected abstract String getNick();
}
