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
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import ar.com.jkohen.util.ConfigurationProperties;

public class NickServCommander extends Frame
    implements Observer, ActionListener, WindowListener {

    private EIRC eirc;

    private TextField pass;
    private TextField email;

    private ConfigurationProperties properties;

    public NickServCommander(EIRC eirc, ConfigurationProperties properties) {
	this(eirc, properties, Locale.getDefault());
    }

    public NickServCommander(EIRC eirc, ConfigurationProperties properties,
			     Locale locale) {
	super();

	this.eirc = eirc;
	this.properties = properties;

	ResourceBundle lang = ResourceBundle.getBundle("eirc", locale);

	setTitle(lang.getString("nickserv.title"));

	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	
	setLayout(gb);

	gbc.insets = new Insets(2, 2, 2, 2);

	Button b;
	Label l;

	l = new Label(lang.getString("nickserv.password"));
	gbc.anchor = GridBagConstraints.EAST;
	gb.setConstraints(l, gbc);
	gbc.anchor = GridBagConstraints.CENTER;
	add(l);

	pass = new TextField(16);
	pass.setEchoChar('*');
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gb.setConstraints(pass, gbc);
	gbc.fill = GridBagConstraints.NONE;
	add(pass);

	gbc.gridy = 1;

	l = new Label(lang.getString("nickserv.email"));
	gbc.anchor = GridBagConstraints.EAST;
	gb.setConstraints(l, gbc);
	gbc.anchor = GridBagConstraints.CENTER;
	add(l);

	email = new TextField(32);
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gb.setConstraints(email, gbc);
	gbc.fill = GridBagConstraints.NONE;
	add(email);

	gbc.gridy = 2;

  	GridBagLayout gb2 = new GridBagLayout();
  	GridBagConstraints gbc2 = new GridBagConstraints();
  	Panel button_panel = new Panel(gb2);

	gbc2.insets = new Insets(0, 2, 0, 2);

	b = new Button(lang.getString("nickserv.register"));
	b.setActionCommand("register");
	b.addActionListener(this);
	gb2.setConstraints(b, gbc2);
	button_panel.add(b);

	b = new Button(lang.getString("nickserv.identify"));
	b.setActionCommand("identify");
	b.addActionListener(this);
	gb2.setConstraints(b, gbc2);
	button_panel.add(b);

	b = new Button(lang.getString("ok"));
	b.setActionCommand("ok");
	b.addActionListener(this);
	gb2.setConstraints(b, gbc2);
	button_panel.add(b);

    	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gb.setConstraints(button_panel, gbc);
	gbc.gridwidth = 1;
	add(button_panel);

	addWindowListener(this);
    }

    public void update(Observable o, Object arg) {
	ConfigurationProperties props = (ConfigurationProperties) o;

	if (null == arg || arg.equals("services_password")) {
	    pass.setText(props.getString("services_password"));
	}
	if (null == arg || arg.equals("services_email")) {
	    email.setText(props.getString("services_email"));
	}
    }

    public void requestFocus() {
	pass.requestFocus();
    }

    public void setBackground(Color c) {
	super.setBackground(c);
    }

    public void setForeground(Color c) {
	super.setForeground(c);
    }

    public void setTextBackground(Color c) {
	pass.setBackground(c);
	email.setBackground(c);
    }

    public void setTextForeground(Color c) {
	pass.setForeground(c);
        email.setForeground(c);
    }

    public void actionPerformed(ActionEvent ev) {
	String action = ev.getActionCommand();
	String password = pass.getText();
	String email = this.email.getText();

	if (action.equals("register")) {
	    if (0 != password.length()) {
		String p[] = { "nickserv",
			       "register " + password + " " + email };
		eirc.sendMessage("privmsg", p);
	    }
	} else if (action.equals("identify")) {
	    if (0 != password.length()) {
		String p[] = { "nickserv", "identify " + password };
		eirc.sendMessage("privmsg", p);
	    }
	} else if (action.equals("ok")) {
	    properties.setString("services_password", password);
	    properties.setString("services_email", email);
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
