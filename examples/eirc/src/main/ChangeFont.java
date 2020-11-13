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
import com.splendid.awtchat.SmileyTextArea;

public class ChangeFont extends Frame
    implements ItemListener, ActionListener, WindowListener {

    private Choice fonts;
    private TextField textSize;
    private Component caller;

    public ChangeFont(Component caller) {
	this(caller, Locale.getDefault());
    }

    public ChangeFont(Component caller, Locale locale) {
	super();

	this.caller = caller;

	ResourceBundle lang = ResourceBundle.getBundle("eirc", locale);

	setTitle(lang.getString("change_font.title"));

	Font current_font = caller.getFont();

	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	
	setLayout(gb);

	gbc.insets = new Insets(2, 2, 2, 2);

	Button b;
	Label l;

	l = new Label(lang.getString("change_font.font"));
	gbc.anchor = GridBagConstraints.EAST;
	gb.setConstraints(l, gbc);
	add(l);
	gbc.anchor = GridBagConstraints.CENTER;

	this.fonts = new Choice();
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gb.setConstraints(fonts, gbc);
	add(fonts);
	gbc.fill = GridBagConstraints.NONE;

	gbc.gridy = 1;

	l = new Label(lang.getString("change_font.size"));
	gbc.anchor = GridBagConstraints.EAST;
	gb.setConstraints(l, gbc);
	add(l);
	gbc.anchor = GridBagConstraints.CENTER;

	this.textSize = new TextField(String.valueOf(current_font.getSize()), 2);
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gb.setConstraints(textSize, gbc);
	add(textSize);
	gbc.fill = GridBagConstraints.NONE;

	gbc.gridy = 2;

	gbc.weightx = 1.0;

	b = new Button(lang.getString("ok"));
	b.setActionCommand("ok");
	b.addActionListener(this);
	gb.setConstraints(b, gbc);
	add(b);

	b = new Button(lang.getString("cancel"));
	b.setActionCommand("cancel");
	b.addActionListener(this);
	gb.setConstraints(b, gbc);
	add(b);

	// Deprecation warning expected on JDK 1.2+.
	String[] fontNames = getToolkit().getFontList();
	for (int i = 0; i < fontNames.length; i++) {
	    fonts.addItem(fontNames[i]);
	}
	fonts.select(current_font.getName());

	fonts.addItemListener(this);
	textSize.addActionListener(this);
	addWindowListener(this);
    }

    private void change_font(Component target) {
	int font_size = target.getFont().getSize();

	try {
	    font_size = Integer.parseInt(textSize.getText());
	} catch (NumberFormatException e) {
	    // The field contained bad input, set it to something sane.
	    textSize.setText(String.valueOf(font_size));
	}

	Font newFont = new Font(fonts.getSelectedItem(), Font.PLAIN, font_size);

	// Don't try to change font if the new equals the current, it will make nasty things, at least with Sun's JDK 1.2.2+ on Linux and XFree 4.0.1.
	if (newFont.equals(target.getFont())) {
	    return;
	}

	target.setFont(newFont);
	target.validate();
	if (target instanceof Frame) {
	    ((Frame) target).pack();
	}
    }

    public void setTextForeground(Color c) {
	textSize.setForeground(c);
    }

    public void setTextBackground(Color c) {
	textSize.setBackground(c);
    }

    public void itemStateChanged(ItemEvent ev) {
	Object source = ev.getSource();

	if (source.equals(fonts)) {
	    // This dialog's font is set with the user's selection, so it can be previewed.
	    change_font(this);
	}
    }

    public void actionPerformed(ActionEvent ev) {
	Object source = ev.getSource();
	String command = ev.getActionCommand();

	if (source.equals(textSize) || command.equals("ok")) {
	    change_font(caller);
	    dispose();
	} else if (command.equals("cancel")) {
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
