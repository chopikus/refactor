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

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import ar.com.jkohen.awt.NickItem;
import ar.com.jkohen.awt.NickList;
import ar.com.jkohen.util.ConfigurationProperties;

public class Configurator extends Frame
    implements ItemListener, ActionListener, WindowListener {

    private java.awt.List tabs;
    private Panel page;
    private CardLayout page_layout;

    private ConfigurationProperties properties;

    private Vector items;

    public Configurator(ConfigurationProperties properties) {
	this(properties, Locale.getDefault());
    }

    public Configurator(ConfigurationProperties properties, Locale locale) {
	super();

	this.properties = properties;

	ResourceBundle lang = ResourceBundle.getBundle("configurator", locale);

	this.items = new Vector();

	setTitle(lang.getString("title"));

	/* Build GUI.
	 */
	this.tabs = new java.awt.List();

	page = new Panel();

	Panel action = new Panel();
	Button b;

	b = new Button(lang.getString("ok"));
	b.setActionCommand("ok");
	b.addActionListener(this);
	action.add(b, BorderLayout.WEST);

	b = new Button(lang.getString("cancel"));
	b.setActionCommand("cancel");
	b.addActionListener(this);
	action.add(b, BorderLayout.EAST);

	add(tabs, BorderLayout.WEST);
	add(page, BorderLayout.EAST);
	add(action, BorderLayout.SOUTH);

	page_layout = new CardLayout();
	page.setLayout(page_layout);

	Panel p, p2;
	GridBagLayout gb;
	GridBagConstraints gbc;

	Checkbox cb;
	CheckboxGroup cbg;
	TextField tf;
	Label l;


	/* Message settings Tab.
	 */
	p = new Panel();
	p.setLayout(new GridLayout(0, 1, 2, 2));

	cb = new Checkbox(lang.getString("prefix_own"),
			  properties.getBoolean("prefix_own"));
	p.add(cb);
	items.addElement(new ConfCheckbox("prefix_own", cb));

	cb = new Checkbox(lang.getString("see_everything_from_server"),
			  properties.getBoolean("see_everything_from_server"));
	p.add(cb);
	items.addElement(new ConfCheckbox("see_everything_from_server", cb));

	cb = new Checkbox(lang.getString("see_join"),
			  properties.getBoolean("see_join"));
	p.add(cb);
	items.addElement(new ConfCheckbox("see_join", cb));

	cb = new Checkbox(lang.getString("filter_mirc_attribs"),
			  properties.getBoolean("filter_mirc_attribs"));
	p.add(cb);
	items.addElement(new ConfCheckbox("filter_mirc_attribs", cb));

	cb = new Checkbox(lang.getString("on_dcc_notify_peer"),
			  properties.getBoolean("on_dcc_notify_peer"));
	p.add(cb);
	items.addElement(new ConfCheckbox("on_dcc_notify_peer", cb));

//  	cb = new Checkbox(lang.getString("text_attr_memory"),
//  			  properties.getBoolean("text_attr_memory"));
//  	p.add(cb);
//  	items.addElement(new ConfCheckbox("text_attr_memory", cb));

	/* Quit message option. */
	p2 = new Panel();
	gb = new GridBagLayout();
	gbc = new GridBagConstraints();
	p2.setLayout(gb);

	l = new Label(lang.getString("quit_message"), Label.RIGHT);
	gb.setConstraints(l, gbc);
	p2.add(l);

	tf = new TextField(properties.getString("quit_message"), 20);
	gbc.weightx = 1.0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gb.setConstraints(tf, gbc);
	p2.add(tf);

	p.add(p2);
	items.addElement(new ConfTextField("quit_message", tf));

	/* Add tab to list. */
	String tab_tag = lang.getString("tab.message");
	page.add(p, tab_tag);
	tabs.add(tab_tag);


	/* Miscellaneous settings Tab.
	 */
	p = new Panel();
	p.setLayout(new GridLayout(0, 1, 2, 2));

	cb = new Checkbox(lang.getString("focus_opening_privates"),
			  properties.getBoolean("focus_opening_privates"));
	p.add(cb);
	items.addElement(new ConfCheckbox("focus_opening_privates", cb));

	cbg = new CheckboxGroup();
	int renderer = properties.getInt("nick_item_renderer");
	cb = new Checkbox(lang.getString("nick_item_bullet"), cbg,
			  NickItem.BULLET_RENDERER == renderer);
	cb.setName(String.valueOf(NickItem.BULLET_RENDERER));
	p.add(cb);
	cb = new Checkbox(lang.getString("nick_item_symbol"), cbg,
			  NickItem.SYMBOL_RENDERER == renderer);
	cb.setName(String.valueOf(NickItem.SYMBOL_RENDERER));
	p.add(cb);
	items.addElement(new ConfCheckboxGroup("nick_item_renderer", cbg));

	cbg = new CheckboxGroup();
	int method = properties.getInt("nick_item_sort_method");
	cb = new Checkbox(lang.getString("nick_item_alpha"), cbg,
			  NickList.SORT_ALPHA == method);
	cb.setName(String.valueOf(NickList.SORT_ALPHA));
	p.add(cb);
	cb = new Checkbox(lang.getString("nick_item_alpha_favor_mode"), cbg,
			  NickList.SORT_ALPHA_FAVOR_MODE == method);
	cb.setName(String.valueOf(NickList.SORT_ALPHA_FAVOR_MODE));
	p.add(cb);
	items.addElement(new ConfCheckboxGroup("nick_item_sort_method", cbg));

	/* Add tab to list. */
	tab_tag = lang.getString("tab.misc");
	page.add(p, tab_tag);
	tabs.add(tab_tag);


	/* Pre-connect settings Tab.
	 */
	p = new Panel();
	p.setLayout(new GridLayout(0, 1, 2, 2));

	/* Request MOTD option. */
	cb = new Checkbox(lang.getString("request_motd"),
			  properties.getBoolean("request_motd"));
	p.add(cb);
	items.addElement(new ConfCheckbox("request_motd", cb));

	/* Password option. */
	p2 = new Panel();
	gb = new GridBagLayout();
	gbc = new GridBagConstraints();
	p2.setLayout(gb);

	l = new Label(lang.getString("password"), Label.RIGHT);
	gb.setConstraints(l, gbc);
	p2.add(l);

	tf = new TextField(properties.getString("password"), 10);
	tf.setEchoChar('*');
	gbc.weightx = 1.0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gb.setConstraints(tf, gbc);
	p2.add(tf);

	p.add(p2);
	items.addElement(new ConfTextField("password", tf));

	/* Add tab to list. */
	tab_tag = lang.getString("tab.pre_connect");
	page.add(p, tab_tag);
	tabs.add(tab_tag);


	tabs.addItemListener(this);
	addWindowListener(this);
    }

    private void updateConfiguration() {
	for (Enumeration e = items.elements(); e.hasMoreElements(); ) {
	    ConfComponent c = (ConfComponent) e.nextElement();
	    String name = c.getName();
	    Object o = c.getValue();

	    if (o instanceof Boolean) {
		properties.setBoolean(name, ((Boolean) o).booleanValue());
	    } else if (o instanceof Integer) {
		properties.setInt(name, ((Integer) o).intValue());
	    } else if (o instanceof String) {
		properties.setString(name, (String) o);
	    }
	}
    }

    public void setBackground(Color c) {
	super.setBackground(c);
	tabs.setBackground(c);
    }

    public void setForeground(Color c) {
	super.setForeground(c);
	tabs.setForeground(c);
    }

    public void itemStateChanged(ItemEvent e) {
	page_layout.show(page, tabs.getSelectedItem());
    }

    public void actionPerformed(ActionEvent ev) {
	String action = ev.getActionCommand();

	if (action.equals("ok")) {
	    updateConfiguration();
	    dispose();
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

abstract class ConfComponent {
    private String name;

    public ConfComponent(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public abstract Object getValue();
}

class ConfTextField extends ConfComponent {
    private TextField component;

    public ConfTextField(String name, TextField tf) {
	super(name);

	this.component = tf;
    }

    public Object getValue() {
	return component.getText();
    }
}

class ConfCheckbox extends ConfComponent {
    private Checkbox component;

    public ConfCheckbox(String name, Checkbox cb) {
	super(name);

	this.component = cb;
    }

    public Object getValue() {
	return new Boolean(component.getState());
    }
}

class ConfCheckboxGroup extends ConfComponent {
    private CheckboxGroup component;

    public ConfCheckboxGroup(String name, CheckboxGroup cbg) {
	super(name);

	this.component = cbg;
    }

    public Object getValue() {
	Checkbox cb = component.getSelectedCheckbox();

	try {
	    return Integer.decode(cb.getName());
	} catch (NumberFormatException ex) {
	    return null;
	}
    }
}
