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
import java.awt.event.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import ar.com.jkohen.awt.TextFieldHistory;
import ar.com.jkohen.irc.Channel;

public class StatusWindow extends OutputWindow implements ActionListener {

    private EIRC eirc;
    private String title;

    private TextFieldHistory entry;

    private static final ResourceBundle msg
	= ResourceBundle.getBundle("message");

    protected static final MessageFormat UNMANGLED
	= new MessageFormat(msg.getString("unmangled"));

    public StatusWindow(EIRC eirc, String title) {
	this(eirc, title, Locale.getDefault());
    }

    public StatusWindow(EIRC eirc, String title, Locale locale) {
	super(title, locale);

	this.eirc = eirc;
	this.title = title;

	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();

	setLayout(gb);

  	gbc.weightx = 1.0;
	gbc.gridwidth = GridBagConstraints.REMAINDER;

	gbc.fill = GridBagConstraints.BOTH;
	gbc.weighty = 1.0;
	gb.setConstraints(text_canvas, gbc);
	add(text_canvas/*, "text_canvas"*/);
	gbc.weighty = 0.0;

	gbc.gridy = 1;

	entry = new TextFieldHistory();
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gb.setConstraints(entry, gbc);
	add(entry);

	entry.addActionListener(this);
    }

    public void printUnmangled(String s) {
	Object [] o = { s };
	text_canvas.append(UNMANGLED.format(o), true);

	postTextEvent();
    }

    public void requestFocus() {
	entry.requestFocus();
    }

    public void setTextBackground(Color c) {
	text_canvas.setBackground(c);
	entry.setBackground(c);
    }

    public void setTextForeground(Color c) {
	text_canvas.setForeground(c);
	entry.setForeground(c);
    }

    protected void visitURL(URL url) {
	eirc.visitURL(url);
    }

    protected void joinChannel(String name) {
	if (null == eirc.getChannel(name)) {
	    String p[] = { name };
	    eirc.sendMessage("join", p);
	} else {
	    eirc.showPanel(name);
	}
    }

    /**
     * This method does nothing. The status window is not an actual peer,
     * so sending it text wouldn't make a lot of sense.
     *
     * @param text the text to not send.
     */
    public void sendText(String text) {
	// Do nothing.
    }

    public void actionPerformed(ActionEvent ev) {
	String text = entry.getText();
	if (text.length() <= 0) {
	    return;
	}

	/* Process command. */

	// See if it has a slash.
	if (text.charAt(0) == '/') {
	    // Remove it.
	    text = text.substring(1);
	}

	// Avoid empty (or pure white space) input.
	if (text.trim().length() > 0) {
	    eirc.sendCommand(text, this);
	}

	entry.setText("");
    }
}
