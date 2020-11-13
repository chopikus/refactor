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

package ar.com.jkohen.awt;

import java.awt.*;
import ar.com.jkohen.irc.Modes;
import ar.com.jkohen.irc.User;

public class NickItemSymbol extends NickItem {
    public NickItemSymbol(User user) {
	super(user);
    }

    public void paint(Graphics g) {
	Dimension size = getSize();
	Color fg = getTextForeground();
	String nick = Modes.uncanonicalizeNick(user.getTag(), user.getModes());

	if (isSelected()) {
	    // Fill background.
  	    g.setColor(getSelectedBackground());
	    g.fillRect(0, 0, size.width, size.height);
  	    fg = getSelectedForeground();
	} else if (user.isOp()) {
	    fg = Color.green;
	} else if (user.isHalfOp()) {
	    fg = Color.red;
	} else if (user.isVoiced()) {
	    fg = Color.yellow;
	}

	g.setColor(fg);

	FontMetrics fm = g.getFontMetrics();
	int baseline = fm.getLeading() + fm.getMaxAscent();

	g.drawString(nick, 0, baseline);
    }

    public Dimension getPreferredSize() {
	Graphics g = getGraphics();
	FontMetrics fm = g.getFontMetrics();
	String nick = Modes.uncanonicalizeNick(user.getTag(), user.getModes());

	Dimension size = new Dimension(fm.stringWidth(nick),
				       fm.getLeading() + fm.getMaxAscent()
				       + fm.getMaxDescent());

	return size;
    }
}
