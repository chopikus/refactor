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

package ar.com.jkohen.irc;

public class User {
    // NORMAL_MASK is an algorithmic hack to help sorting by priority.
    public static final int NORMAL_MASK = 0x01;
    public static final int OP_MASK     = 0x02;
    public static final int HALFOP_MASK = 0x04;
    public static final int VOICE_MASK  = 0x08;

    private String tag;
    private int modes;

    public User(String tag) {
	this.tag = tag;
	// Everybody is normal all the time.
	this.modes = NORMAL_MASK;
    }

    public String getTag() {
	return tag;
    }

    public void setTag(String tag) {
	this.tag = tag;
    }

    public int getModes() {
	return modes;
    }

    public void setModes(int mask) {
	this.modes |= mask;
    }

    public void unsetModes(int mask) {
	this.modes &= ~mask;
    }

    public boolean isOp() {
	return (0 != (modes & OP_MASK));
    }

    public boolean isHalfOp() {
	return (0 != (modes & HALFOP_MASK));
    }

    public boolean isVoiced() {
	return (0 != (modes & VOICE_MASK));
    }

    public boolean isNormal() {
	return (0 != (modes & NORMAL_MASK));
    }
}
