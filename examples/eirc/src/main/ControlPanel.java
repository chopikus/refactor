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
import java.io.IOException;
import java.util.*;
import ar.com.jkohen.awt.ImageButton;
import ar.com.jkohen.util.ConfigurationProperties;
import ar.com.jkohen.util.Resource;

public class ControlPanel extends Panel implements ActionListener, Observer {
    private static final int CONNECT = 0;
    private static final int DISCONNECT = 0;
    private static final int SETUP = 1;
    private static final int CHANNELS = 2;
    private static final int JOIN = 3;
    private static final int NICKSERV = 4;
//      private static final int FONT = 5;
    private static final int COMPONENTS = 5;

    private EIRC eirc;

    private ResourceBundle images;
    private Component [] components;

    /* Configuration properties. */
    private String list_parameter;
    private boolean special_services;

    private boolean connected;

    public ControlPanel(EIRC eirc) {
	this(eirc, Locale.getDefault());
    }

    public ControlPanel(EIRC eirc, Locale locale) {
	this.eirc = eirc;
	this.images = ResourceBundle.getBundle("Images", locale);
	this.components = new Component [COMPONENTS];

	FlowLayout layout = (FlowLayout) getLayout();
	layout.setHgap(0);
	layout.setVgap(0);
	layout.setAlignment(FlowLayout.RIGHT);

	ImageButton ib;

	// This component's images are loaded later from setConnect.
	components[CONNECT] = new ImageButton();

	components[SETUP] = ib = new ImageButton();
	ib.setImage(ImageButton.ENTERED,
		    getResourceImage("status.configuration.entered"));
	ib.setImage(ImageButton.EXITED,
		    getResourceImage("status.configuration.exited"));
	ib.setImage(ImageButton.PRESSED,
		    getResourceImage("status.configuration.pressed"));

	components[CHANNELS] = ib = new ImageButton();
	ib.setImage(ImageButton.DISABLED,
		    getResourceImage("status.channel-list.disabled"));
	ib.setImage(ImageButton.ENTERED,
		    getResourceImage("status.channel-list.entered"));
	ib.setImage(ImageButton.EXITED,
		    getResourceImage("status.channel-list.exited"));
	ib.setImage(ImageButton.PRESSED,
		    getResourceImage("status.channel-list.pressed"));

	components[JOIN] = ib = new ImageButton();
	ib.setImage(ImageButton.DISABLED,
		    getResourceImage("status.new-channel.disabled"));
	ib.setImage(ImageButton.ENTERED,
		    getResourceImage("status.new-channel.entered"));
	ib.setImage(ImageButton.EXITED,
		    getResourceImage("status.new-channel.exited"));
	ib.setImage(ImageButton.PRESSED,
		    getResourceImage("status.new-channel.pressed"));

	components[NICKSERV] = ib = new ImageButton();
	ib.setImage(ImageButton.DISABLED,
		    getResourceImage("status.nickserv.disabled"));
	ib.setImage(ImageButton.ENTERED,
		    getResourceImage("status.nickserv.entered"));
	ib.setImage(ImageButton.EXITED,
		    getResourceImage("status.nickserv.exited"));
	ib.setImage(ImageButton.PRESSED,
		    getResourceImage("status.nickserv.pressed"));

//  	components[FONT] = ib = new ImageButton();
//  	ib.setImage(ImageButton.DISABLED,
//  		    getResourceImage("status.change-font.disabled"));
//  	ib.setImage(ImageButton.ENTERED,
//  		    getResourceImage("status.change-font.entered"));
//  	ib.setImage(ImageButton.EXITED,
//  		    getResourceImage("status.change-font.exited"));
//  	ib.setImage(ImageButton.PRESSED,
//  		    getResourceImage("status.change-font.pressed"));

	for (int i = 0; i < COMPONENTS; i++) {
	    add(components[i]);

	    if (components[i] instanceof ImageButton) {
		ib = (ImageButton) components[i];
		ib.addActionListener(this);
	    }
	}

	// Set default button states.
	setConnected(false);
    }

    public void setConnected(boolean connected) {
	this.connected = connected;

	ImageButton ib = (ImageButton) components[CONNECT];
	String base = "status." + (connected ? "disconnect" : "connect");
	ib.setImage(ImageButton.ENTERED,
		    getResourceImage(base + ".entered"));
	ib.setImage(ImageButton.EXITED,
		    getResourceImage(base + ".exited"));
	ib.setImage(ImageButton.PRESSED,
		    getResourceImage(base + ".pressed"));

	components[CHANNELS].setEnabled(connected);
	components[JOIN].setEnabled(connected);
	if (special_services) {
	    components[NICKSERV].setEnabled(connected);
	} else {
	    components[NICKSERV].setEnabled(false);
	}

	// This must be here, or the "connect" button would disappear in MSIE if connection failed.
	validate();

	ib.repaint();
    }

    private Image getResourceImage(String name) {
	String resource_name = images.getString(name);

	try {
	    return Resource.createImage(resource_name, this);
	} catch (IOException e) {
	    System.err.println(e);
	}

	return null;
    }

    public void update(Observable o, Object arg) {
	ConfigurationProperties props = (ConfigurationProperties) o;

 	if (null == arg || arg.equals("list_parameter")) {
	    this.list_parameter = props.getString("list_parameter");
	}
 	if (null == arg || arg.equals("special_services")) {
	    this.special_services = props.getBoolean("special_services");
	    if (special_services) {
		components[NICKSERV].setEnabled(connected);
	    } else {
		components[NICKSERV].setEnabled(false);
	    }
	}
    }

    public void setBackground(Color c) {
	super.setBackground(c);
	for (int i = 0; i < COMPONENTS; i++) {
	    components[i].setBackground(c);
	}
    }

    public void setForeground(Color c) {
	super.setForeground(c);
	for (int i = 0; i < COMPONENTS; i++) {
	    components[i].setForeground(c);
	}
    }

    public void actionPerformed(ActionEvent ev) {
	Object source = ev.getSource();

	if (source.equals(components[CONNECT])) {
	    if (connected) {
		eirc.disconnect();
	    } else {
		eirc.connect();
	    }
	} else if (source.equals(components[CHANNELS])) {
	    String [] p = { list_parameter };
	    eirc.sendMessage("list", p);
	} else if (source.equals(components[JOIN])) {
	    eirc.openNewChannel();
	} else if (source.equals(components[SETUP])) {
	    eirc.openConfigurator();
//  	} else if (source.equals(components[FONT])) {
//  	    eirc.openChangeFont();
	} else if (source.equals(components[NICKSERV])) {
	    eirc.openNickServ();
	}
    }
}
