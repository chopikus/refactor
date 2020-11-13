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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import ar.com.jkohen.awt.*;
import ar.com.jkohen.irc.Channel;
import ar.com.jkohen.util.Resource;

public class ChatPanelContainer extends Panel
    implements ActionListener, MouseListener, TextListener {

    private EIRC eirc;

    private ResourceBundle images;

    private Hashtable panels;
    private String status_tag;
    private String tab_under_menu;
    private ChatPanel visible_panel;

    private CardLayout container_layout;
    private Panel container;
    private ChatTabs tabs;
    private PopupMenu tabs_popup;
    private MenuItem close_item;
    private ImageCanvas label;
    private Panel bottom_panel;
    private ImageButton shift_left;
    private ImageButton shift_right;

    public ChatPanelContainer(EIRC eirc) {
	this(eirc, Locale.getDefault());
    }

    public ChatPanelContainer(EIRC eirc, Locale locale) {
	this.eirc = eirc;
	this.images = ResourceBundle.getBundle("Images", locale);

	ResourceBundle lang = ResourceBundle.getBundle("eirc");

	this.panels = new Hashtable();

	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();

	setLayout(gb);

	this.container = new Panel();
	gbc.weighty = 1.0;
	gbc.weightx = 1.0;
	gbc.fill = GridBagConstraints.BOTH;
	gb.setConstraints(container, gbc);
	add(container);
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;

	gbc.gridy = 1;

	bottom_panel = new Panel();
	GridBagLayout bottom_gb = new GridBagLayout();
	GridBagConstraints bottom_gbc = new GridBagConstraints();
	bottom_panel.setLayout(bottom_gb);

	/* Bottom Panel begins here. */
	bottom_gbc.anchor = GridBagConstraints.NORTH;

	this.label = new ImageCanvas(getResourceImage("tabs.label"));
	bottom_gbc.gridheight = GridBagConstraints.REMAINDER;
	bottom_gb.setConstraints(label, bottom_gbc);
	bottom_panel.add(label);
	bottom_gbc.gridheight = 1;

  	this.tabs = new ChatTabs();
	bottom_gbc.weightx = 1.0;
	bottom_gbc.fill = GridBagConstraints.HORIZONTAL;
	bottom_gbc.gridheight = GridBagConstraints.REMAINDER;
	bottom_gb.setConstraints(tabs, bottom_gbc);
	bottom_panel.add(tabs);
	bottom_gbc.gridheight = 1;
	bottom_gbc.fill = GridBagConstraints.NONE;
	bottom_gbc.weightx = 0.0;

	this.shift_left = new ImageButton();
	shift_left.setEnabled(false);
	shift_left.setImage(ImageButton.DISABLED,
			    getResourceImage("tabs.shift_left.disabled"));
	shift_left.setImage(ImageButton.ENTERED,
			    getResourceImage("tabs.shift_left.entered"));
	shift_left.setImage(ImageButton.PRESSED,
			    getResourceImage("tabs.shift_left.pressed"));
	shift_left.setImage(ImageButton.EXITED,
			    getResourceImage("tabs.shift_left.exited"));
	bottom_gb.setConstraints(shift_left, bottom_gbc);
	bottom_panel.add(shift_left);

	bottom_gbc.gridy = 1;

	this.shift_right = new ImageButton();
	shift_right.setEnabled(false);
	shift_right.setImage(ImageButton.DISABLED,
			     getResourceImage("tabs.shift_right.disabled"));
	shift_right.setImage(ImageButton.ENTERED,
			     getResourceImage("tabs.shift_right.entered"));
	shift_right.setImage(ImageButton.PRESSED,
			     getResourceImage("tabs.shift_right.pressed"));
	shift_right.setImage(ImageButton.EXITED,
			     getResourceImage("tabs.shift_right.exited"));
	bottom_gb.setConstraints(shift_right, bottom_gbc);
	bottom_panel.add(shift_right);
	
	/* Bottom Panel ends here. */

	gbc.weightx = 1.0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gb.setConstraints(bottom_panel, gbc);
	add(bottom_panel);

	tabs.setImage(ChatTabs.NORMAL, ChatTabs.LEFT,
		      getResourceImage("tabs.normal.left"));
	tabs.setImage(ChatTabs.NORMAL, ChatTabs.CENTER,
		      getResourceImage("tabs.normal.center"));
	tabs.setImage(ChatTabs.NORMAL, ChatTabs.RIGHT,
		      getResourceImage("tabs.normal.right"));
	tabs.setImage(ChatTabs.CURRENT, ChatTabs.LEFT,
		      getResourceImage("tabs.current.left"));
	tabs.setImage(ChatTabs.CURRENT, ChatTabs.CENTER,
		      getResourceImage("tabs.current.center"));
	tabs.setImage(ChatTabs.CURRENT, ChatTabs.RIGHT,
		      getResourceImage("tabs.current.right"));
	tabs.setImage(ChatTabs.ALARM, ChatTabs.LEFT,
		      getResourceImage("tabs.alarm.left"));
	tabs.setImage(ChatTabs.ALARM, ChatTabs.CENTER,
		      getResourceImage("tabs.alarm.center"));
	tabs.setImage(ChatTabs.ALARM, ChatTabs.RIGHT,
		      getResourceImage("tabs.alarm.right"));

	this.container_layout = new CardLayout();
	container.setLayout(container_layout);

	this.tabs_popup = new PopupMenu();
	MenuItem mi = new MenuItem(lang.getString("open"));
	mi.setActionCommand("open");
	tabs_popup.add(mi);
	this.close_item = new MenuItem(lang.getString("close"));
	close_item.setActionCommand("close");
	tabs_popup.add(close_item);
	add(tabs_popup);

	tabs.addActionListener(this);
	tabs.addMouseListener(this);
	shift_left.addActionListener(this);
	shift_right.addActionListener(this);
	tabs_popup.addActionListener(this);
    }

    public void add(ChatPanel cp, String tag) {
	add(cp, tag, false);
    }

    public void add(ChatPanel cp, String tag, boolean is_status) {
	if (is_status) {
	    if (null != status_tag) {
		throw new IllegalArgumentException("already have a status panel");
	    }
	    this.status_tag = tag;
	}

	synchronized (this) {
	    panels.put(tag, cp);
	    container.add(cp, tag);
	    tabs.add(tag);
	}

	updateShiftButtons();

	cp.addTextListener(this);
    }

    public void remove(String tag) {
	if (tag.equals(visible_panel)) {
	    this.visible_panel = null;
	}

	synchronized (this) {
	    tabs.remove(tag);
	    Component c = (Component) panels.remove(tag);
	    container.remove(c);

	    if (tag.equals(status_tag)) {
		this.status_tag = null;
	    }

	    String new_tab = tabs.getCurrent();
	    if (null != new_tab) {
		show(new_tab);
	    } else if (panels.containsKey(status_tag)) {
		show(status_tag);
	    }
	}

	updateShiftButtons();
    }

    public synchronized void rename(String old_tag, String tag) {
	ChatPanel cp = (ChatPanel) panels.get(old_tag);

	// Removing the old panel isn't necessary: adding it under a different tag has the same effect.
	container.add(cp, tag);
	if (old_tag.equals(tabs.getVisible())) {
	    container_layout.show(container, tag);
	}

	cp.setPanelTag(tag);
	panels.put(tag, cp);
	panels.remove(old_tag);

	tabs.rename(old_tag, tag);
    }


    public synchronized void show(String tag) {
	ChatPanel cp = this.visible_panel = (ChatPanel) panels.get(tag);

	container_layout.show(container, tag);
	cp.requestFocus();

	tabs.setAlarm(tag, false);
	tabs.makeVisible(tag);
    }

    private void updateShiftButtons() {
	int tabs_width = tabs.getSize().width;
	boolean enabled = tabs_width > 0
	    && tabs_width < tabs.getPreferredSize().width;

	shift_left.setEnabled(enabled);
	shift_right.setEnabled(enabled);
    }

    private Image getResourceImage(String tag) {
	String resource_tag = images.getString(tag);

	try {
	    return Resource.createImage(resource_tag, this);
	} catch (IOException e) {
	    System.err.println(e);
	}

	return null;
    }

    public ChatPanel getVisible() {
	return visible_panel;
    }

    public void setForeground(Color c) {
	super.setForeground(c);
	container.setForeground(c);
	bottom_panel.setForeground(c);
  	label.setForeground(c);
  	tabs.setForeground(c);
    }

    public void setBackground(Color c) {
	super.setBackground(c);
	container.setBackground(c);
	bottom_panel.setBackground(c);
  	label.setBackground(c);
  	tabs.setBackground(c);
    }

    public void setBounds(int x, int y, int w, int h) {
	super.setBounds(x, y, w, h);
	updateShiftButtons();
    }

    public void actionPerformed(ActionEvent ev) {
	Object source = ev.getSource();

	if (source.equals(tabs)) {
	    show(ev.getActionCommand());
	} else if (source.equals(shift_left)) {
	    tabs.shiftLeft();
	} else if (source.equals(shift_right)) {
	    tabs.shiftRight();
	} else if (source instanceof MenuItem) {
	    String cmd = ev.getActionCommand();

	    if (cmd.equals("open")) {
		show(tab_under_menu);
	    } else if (cmd.equals("close")) {
		ChatPanel cp = (ChatPanel) panels.get(tab_under_menu);
		if (null != cp) {
		    if (Channel.isChannel(tab_under_menu)) {
			String p[] = { tab_under_menu };
			eirc.sendMessage("part", p);
		    } else {
			cp.dispose();
		    }
		} 
	    }
	}
    }

    public void mouseClicked(MouseEvent ev) {
    }

    public void mousePressed(MouseEvent ev) {
        Object source = ev.getSource();

	if (source.equals(tabs)) {
	    if (ev.isPopupTrigger() ||
		0 != (ev.getModifiers() & MouseEvent.BUTTON3_MASK)) { // WORKAROUND: #27 MSIE doesn't set popupTrigger.
		String tab = tabs.getTabAt(ev.getPoint());
  		if (null != tab) {
  		    tab_under_menu = tab;

		    // Disable "close" item for status panel.
		    close_item.setEnabled(!tab.equals(status_tag));

		    tabs_popup.show(tabs, ev.getX(), ev.getY());
  		}
	    }
	}
    }

    public void mouseReleased(MouseEvent ev) {
    }

    public void mouseEntered(MouseEvent ev) {
    }

    public void mouseExited(MouseEvent ev) {
    }

    public void textValueChanged(TextEvent e) {
	ChatPanel cp = (ChatPanel) e.getSource();
	String tag = cp.getPanelTag();

	// Don't try to set an alarm on a recently closed panel.
	synchronized (this) {
	    if (panels.containsKey(tag)) {
		tabs.setAlarm(tag, true);
	    }
	}
    }
}
