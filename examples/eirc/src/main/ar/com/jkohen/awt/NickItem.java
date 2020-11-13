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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.SystemColor;
import ar.com.jkohen.irc.User;

public abstract class NickItem extends Component {
    public static final int SYMBOL_RENDERER = 0;
    public static final int BULLET_RENDERER = 1;

    protected boolean selected;
    protected User user;

    private Color textbg = SystemColor.text;
    private Color textfg = SystemColor.textText;
    private Color selbg = SystemColor.textHighlight;
    private Color selfg = SystemColor.textHighlightText;

    public NickItem(User user) {
	this.user = user;
    }

    public User getUser() {
	return user;
    }

    public static NickItem newInstance(User user, int renderer) {
    	NickItem ni;

  	switch (renderer) {
	case BULLET_RENDERER:
	    ni = new NickItemBullet(user);
	    break;
	case SYMBOL_RENDERER:
	    ni = new NickItemSymbol(user);
	    break;
	default:
	    throw new IllegalArgumentException("invalid NickItem renderer");
	}

	return ni;
    }

    public boolean isSelected() {
	return selected;
    }

    public void setSelected(boolean selected) {
	this.selected = selected;
    }

    public Color getTextBackground() {
	return textbg;
    }

    public void setTextBackground(Color c) {
	this.textbg = c;
    }

    public Color getTextForeground() {
	return textfg;
    }

    public void setTextForeground(Color c) {
	this.textfg = c;
    }

    public Color getSelectedBackground() {
	return selbg;
    }

    public void setSelectedBackground(Color c) {
	this.selbg = c;
    }

    public Color getSelectedForeground() {
	return selfg;
    }

    public void setSelectedForeground(Color c) {
	this.selfg = c;
    }
}
