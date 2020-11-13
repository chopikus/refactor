/*
Javier Kohen's Java Framework Classes.
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

package ar.com.jkohen.awt;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class TextFieldHistory extends TextField
    implements ActionListener, KeyListener {

    // Purge entries past this number (actual entry is part of the count.) Twenty seems a sane number for a human being's short-term memory of what he could've written =)
    private int history_max_size = 20;

    private Vector history;
    private int pos;

    public TextFieldHistory() {
	this(0);
    }

    public TextFieldHistory(int columns) {
	this("", columns);
    }

    public TextFieldHistory(String text) {
	this(text, 0);
    }

    public TextFieldHistory(String text, int columns) {
	super(text, columns);

	// First place is for the current entry's content.
	history = new Vector(1);
	history.addElement("");
	pos = 0;

	addActionListener(this);
	addKeyListener(this);
    }

    public int getMaximumHistorySize() {
	return history_max_size;
    }

    public void setMaximumHistorySize(int new_size) {
	history_max_size = new_size; 
	history.setSize(history_max_size);
    }

    private void loadItem(int n) {
	String item = (String) history.elementAt(pos);
	setText(item);
	setCaretPosition(item.length());
    }

    public void actionPerformed(ActionEvent e) {
	// Record entered lines.
	String entry = e.getActionCommand();

	if (0 == entry.length()) {
	    return;
	}

	// Reset history position.
	pos = 0;

	history.insertElementAt(e.getActionCommand(), 1);

	if (history.size() > history_max_size) {
	    history.setSize(history_max_size);
	}
    }

    public void keyPressed(KeyEvent ev) {
	switch (ev.getKeyCode()) {
	case KeyEvent.VK_UP:
	    if (0 == pos) {
		// Store not yet entered entry.
		history.setElementAt(getText(), 0);
	    }
	    pos = (pos + 1) % history.size();
	    loadItem(pos);
	    break;
	case KeyEvent.VK_DOWN:
	    if (0 == pos) {
		// Store not yet entered entry.
		history.setElementAt(getText(), 0);
		pos = history.size();
	    }
	    pos--;
	    loadItem(pos);
	    break;
	default:
	    pos = 0;
	    break;
	}
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
}
