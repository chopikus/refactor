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
import java.net.URL;
import java.util.Locale;
import java.util.Observable;
import java.util.ResourceBundle;
import ar.com.jkohen.awt.*;
import ar.com.jkohen.irc.Channel;
import ar.com.jkohen.irc.MircMessage;
import ar.com.jkohen.util.ConfigurationProperties;
import ar.com.jkohen.util.Resource;
import com.splendid.awtchat.SmileyTextArea;
//  import htmllayout.HtmlLayout;

class PrivateWindow extends ChatWindow
    implements ActionListener, MouseListener {

    private TextFieldHistory entry;
    private ImageButton whois;
    private ImageButton close;
    private TextAttributePicker text_attr_picker;

    /* Configuration properties. */
    private boolean text_attr_memory;

    private EIRC eirc;
    private String user;
    private ResourceBundle images;

    public PrivateWindow(EIRC eirc, String u, String title) {
	this(eirc, u, title, Locale.getDefault());
    }

    public PrivateWindow(EIRC eirc, String u, String title, Locale locale) {
	super(title, locale);

  	this.eirc = eirc;
	this.user = u;

	this.images = ResourceBundle.getBundle("Images", locale);

//  	String html_layout;

//  	try {
//  	    html_layout = new String(Resource.getResource("PrivateWindow.html", this));
//  	} catch (java.io.IOException e) {
//  	    System.err.println(e);
//  	    return;
//  	}

//  	setLayout(new HtmlLayout(html_layout));

	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();

	setLayout(gb);

	gbc.gridy = 0;

	/* Component's initialization
	 */

	whois = new ImageButton();
	whois.setImage(ImageButton.ENTERED,
		       getResourceImage("private.whois.entered"));
	whois.setImage(ImageButton.EXITED,
		       getResourceImage("private.whois.exited"));
	whois.setImage(ImageButton.PRESSED,
		       getResourceImage("private.whois.pressed"));
	gb.setConstraints(whois, gbc);
  	add(whois/*, "whois"*/);

	close = new ImageButton();
	close.setImage(ImageButton.ENTERED,
		       getResourceImage("private.close.entered"));
	close.setImage(ImageButton.EXITED,
		       getResourceImage("private.close.exited"));
	close.setImage(ImageButton.PRESSED,
		       getResourceImage("private.close.pressed"));
	gbc.anchor = GridBagConstraints.EAST;
	gb.setConstraints(close, gbc);
  	add(close/*, "close"*/);
	gbc.anchor = GridBagConstraints.CENTER;


	// From here below, everything fills the space widely.
	gbc.gridwidth = GridBagConstraints.REMAINDER;
  	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 1.0;

	/* Next Row */
	gbc.gridy++;

	gbc.fill = GridBagConstraints.BOTH;
	gbc.weighty = 1.0;
	gb.setConstraints(text_canvas, gbc);
	add(text_canvas/*, "text_canvas"*/);
	gbc.weighty = 0.0;

	/* Next Row */
	gbc.gridy++;

	// Build picker to use indexed colors.
	this.text_attr_picker
	    = new TextAttributePicker(SmileyTextArea.getColorPalette(), 16);
	gb.setConstraints(text_attr_picker, gbc);
	add(text_attr_picker);

	IndexedColorPicker color_picker
	    = (IndexedColorPicker) text_attr_picker.getColorPicker();

	color_picker.setDisposition(0, 1);
	color_picker.setItemSize(16, 12);
	color_picker.setGap(2, 2);

	/* Next Row */
	gbc.gridy++;

	this.entry = new TextFieldHistory();
	gb.setConstraints(entry, gbc);
	add(entry/*, "entry"*/);


	/* Event Listeners
	 */

  	whois.addMouseListener(this);
  	close.addMouseListener(this);
	entry.addActionListener(this);
	text_attr_picker.addActionListener(this);
    }

    public void requestFocus() {
	entry.requestFocus();
    }

    protected String getNick() {
	return eirc.getNick();
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

    public void update(Observable o, Object arg) {
	super.update(o, arg);

	ConfigurationProperties props = (ConfigurationProperties) o;

	if (null == arg || arg.equals("text_attr_memory")) {
	    this.text_attr_memory = props.getBoolean("text_attr_memory");
	}
    }

    /**
     * Sends the specified text to the peer.
     *
     * @param text the text to send to the other user.
     */
    public void sendText(String text) {
	String [] p = { user, text };
	eirc.sendMessage("privmsg", p);
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

    public void setTextBackground(Color c) {
	text_canvas.setBackground(c);
	entry.setBackground(c);
    }

    public void setTextForeground(Color c) {
	text_canvas.setForeground(c);
	entry.setForeground(c);
    }

    public String getUser() {
	return user;
    }

    public void setUser(String s) {
	this.user = s;
    }

    public void actionPerformed(ActionEvent ev) {
	Object comp = ev.getSource();

	if (comp.equals(entry)) {
	    String text = entry.getText();
	    if (text.length() <= 0) {
		return;
	    }

	    // See if it's a command.
	    if (text.charAt(0) == '/') {
		// Remove slash.
		text = text.substring(1);

		// Avoid empty (or pure white space) input.
		if (text.trim().length() > 0) {
		    eirc.sendCommand(text, this);
		}
	    } else {
		printMyPrivmsg(entry.getText());
		sendText(entry.getText());
	    }

//  	    if (text_attr_memory) {
//  	    } else {
		entry.setText("");
//  	    }
	} else if (comp.equals(text_attr_picker)) {
	    String text = entry.getText();
	    String new_text = ev.getActionCommand();

	    // Insert attributes at caret position.
	    int pos = entry.getCaretPosition();

	    text = text.substring(0, pos)
		.concat(new_text)
		.concat(text.substring(pos));

	    entry.setText(text);
	    entry.setCaretPosition(pos + new_text.length());
	}
    }

    public void mouseClicked(MouseEvent ev) {
	Component comp = ev.getComponent();

	if (comp.equals(whois)) {
	    String p[] = { user };
	    if (null != p[0]) {
		eirc.sendMessage("whois", p);
	    }
	} else if (comp.equals(close)) {
	    dispose();
	}
    }

    public void mousePressed(MouseEvent ev) {
    }

    public void mouseReleased(MouseEvent ev) {
    }

    public void mouseEntered(MouseEvent ev) {
    }

    public void mouseExited(MouseEvent ev) {
    }
}
