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

package ar.com.jkohen.awt;

import java.awt.TextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import ar.com.jkohen.irc.RFC1459;

public class RFC1459TextField extends TextField implements KeyListener {
    public RFC1459TextField() {
	this(0);
    }

    public RFC1459TextField(int columns) {
	this("", columns);
    }

    public RFC1459TextField(String text) {
	this(text, 0);
    }

    public RFC1459TextField(String text, int columns) {
	super(text, columns);

	addKeyListener(this);
    }

    public void keyPressed(KeyEvent e) {
	char ch = e.getKeyChar();
	int code = e.getKeyCode();

	// Consume key if it isn't valid for a nick name
	if (!e.isActionKey() && !RFC1459.isDeclaredChar(ch)
	    && code != KeyEvent.VK_DELETE
	    && code != KeyEvent.VK_BACK_SPACE
	    && code != KeyEvent.VK_SHIFT
	    && code != KeyEvent.VK_ENTER) {
	    e.consume();
	}
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
}
