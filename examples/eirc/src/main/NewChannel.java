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
import java.util.Locale;
import java.util.ResourceBundle;
import ar.com.jkohen.irc.Channel;

public class NewChannel extends Frame
    implements ActionListener, WindowListener {

    private EIRC eirc;

    private TextField name;
    private TextField key;

    public NewChannel(EIRC eirc) {
	this(eirc, Locale.getDefault());
    }

    public NewChannel(EIRC eirc, Locale locale) {
	super();

	this.eirc = eirc;

	ResourceBundle lang = ResourceBundle.getBundle("eirc", locale);

	setTitle(lang.getString("new_channel.title"));

	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	setLayout(gb);

	gbc.insets = new Insets(2, 2, 2, 2);
	gbc.fill = GridBagConstraints.HORIZONTAL;

	Button b;
	Label l;

	l = new Label(lang.getString("new_channel.name"), Label.RIGHT);
	gb.setConstraints(l, gbc);
	add(l);

	this.name = new TextField(16);
	gbc.weightx = 1.0;
	gb.setConstraints(name, gbc);
	add(name);
	gbc.weightx = 0.0;

	gbc.gridy = 1;

	l = new Label(lang.getString("new_channel.key"), Label.RIGHT);
	gb.setConstraints(l, gbc);
	add(l);

	this.key = new TextField(16);
	gbc.weightx = 1.0;
	gb.setConstraints(key, gbc);
	add(key);
	gbc.weightx = 0.0;

	gbc.fill = GridBagConstraints.NONE;

	gbc.gridy = 2;

	Panel bottom_panel = new Panel();
	GridBagLayout gb2 = new GridBagLayout();
	GridBagConstraints gbc2 = new GridBagConstraints();
	bottom_panel.setLayout(gb2);

	gbc2.weightx = 1.0;
        gbc2.insets = new Insets(0, 2, 0, 2);

       	b = new Button(lang.getString("ok"));
	b.setActionCommand("ok");
        gb2.setConstraints(b, gbc2);
	bottom_panel.add(b);
    	b.addActionListener(this);

       	b = new Button(lang.getString("cancel"));
	b.setActionCommand("cancel");
        gb2.setConstraints(b, gbc2);
	bottom_panel.add(b);
    	b.addActionListener(this);

  	gbc.gridwidth = GridBagConstraints.REMAINDER;
        gb.setConstraints(bottom_panel, gbc);
	add(bottom_panel);

	name.addActionListener(this);
	key.addActionListener(this);
	addWindowListener(this);
    }

    private boolean create_channel() {
	String channel = name.getText();
	if (channel.length() <= 0) {
	    return false;
	}

	if (!Channel.isChannel(channel)) {
	    channel = "#" + channel;
	}

	String p[] = { channel, key.getText() };
	eirc.sendMessage("join", p);

	return true;
    }

    public void requestFocus() {
	name.requestFocus();
    }

    public void setTextForeground(Color c) {
	name.setForeground(c);
	key.setForeground(c);
    }

    public void setTextBackground(Color c) {
	name.setBackground(c);
	key.setBackground(c);
    }

    public void actionPerformed(ActionEvent ev) {
	Component source = (Component) ev.getSource();
	String action = ev.getActionCommand();

	if (source.equals(name) || source.equals(key) || action.equals("ok")) {
	    if (create_channel()) {
		dispose();
	    }
	} else if (action.equals("cancel")) {
	    dispose();
	}
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
	dispose();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }
}
