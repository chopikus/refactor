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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import ar.com.jkohen.awt.ChatPanel;
import ar.com.jkohen.awt.MircSmileyTextArea;
import ar.com.jkohen.irc.Channel;
import ar.com.jkohen.irc.MircMessage;
import com.splendid.awtchat.HyperlinkReceiver;

public abstract class OutputWindow extends ChatPanel
    implements HyperlinkReceiver {
    protected MircSmileyTextArea text_canvas;

    private ResourceBundle lang;

    private static final ResourceBundle msg
	= ResourceBundle.getBundle("message");

    protected static final MessageFormat ERROR
	= new MessageFormat(msg.getString("error"));
    protected static final MessageFormat INFO
	= new MessageFormat(msg.getString("info"));
    protected static final MessageFormat NOTICE
	= new MessageFormat(msg.getString("notice"));
    protected static final MessageFormat WARNING
	= new MessageFormat(msg.getString("warning"));

    public OutputWindow(String title) {
	this(title, Locale.getDefault());
    }

    public OutputWindow(String title, Locale locale) {
	super(title);

	this.lang = ResourceBundle.getBundle("eirc", locale);
	this.text_canvas = new MircSmileyTextArea(this);
//  	this.text_canvas.setMode(MircSmileyTextArea.FAST);
    }

    /* High level methods.
     */
    public void printError(String s) {
	printError(s, true);
    }

    public void printError(String s, boolean url) {
	Object [] o = { s };
	text_canvas.append(ERROR.format(o), url);

	postTextEvent();
    }

    public void printInfo(String s) {
	printInfo(s, true);
    }

    public void printInfo(String s, boolean url) {
	Object [] o = { s };
	text_canvas.append(INFO.format(o), url);

	postTextEvent();
    }

    public void printWarning(String s) {
	printWarning(s, true);
    }

    public void printWarning(String s, boolean url) {
	Object [] o = { s };
	text_canvas.append(WARNING.format(o), url);

	postTextEvent();
    }

    public void printNotice(String s, String whom) {
	printNotice(s, whom, true);
    }

    public void printNotice(String s, String whom, boolean url) {
	Object [] o = { whom, s };
	text_canvas.append(NOTICE.format(o), url);

	postTextEvent();
    }

    /**
     * Sends the specified text to the peer. The notion of peer
     * varies with the implementation.
     *
     * @param text the text to send the peer.
     */
    public abstract void sendText(String text);

    /* Low level methods.
     */
    public void output(String s) {
	text_canvas.append(s);

	postTextEvent();
    }

    public void setFont(Font f) {
	super.setFont(f);

	text_canvas.setFont(f);
    }

    public void handleHyperlink(String link) {
	if (Channel.isChannel(link)) {
	    joinChannel(link);
 	} else {
	    try {
		visitURL(new URL(link));
	    } catch (MalformedURLException e) {
		printError(lang.getString("eirc.not_an_url"));
	    }
	}
    }

    /* Makes the client join the specified Channel. */
    protected abstract void joinChannel(String name);

    /* Makes the browser visit the specified URL. */
    protected abstract void visitURL(URL url);
}
