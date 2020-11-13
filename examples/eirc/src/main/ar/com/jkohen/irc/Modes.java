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

import java.awt.Color;

public class Modes {
    /**
     * Returns a canonical representation of <code>nick</code>, that is, without the mode character.
     *
     * @param nick the nick name to canonicalize.
     */
    static public String canonicalizeNick(String nick) {
	char ch = nick.charAt(0);

	if (User.NORMAL_MASK == symbolicToMask(ch)) {
	    return nick;
	} else {
	    return nick.substring(1);
	}
    }

    /**
     * Prepends a symbolic char to <code>nick</code> depending on the <code>mask</code> provided.
     *
     * @param nick the nick name to uncanonicalize.
     * @param mask the user mode mask corresponding to nick.
     */
    static public String uncanonicalizeNick(String nick, int mask) {
	return maskToSymbolic(mask).concat(nick);
    }

    static public int symbolicToMask(char symbol) {
	switch (symbol) {
	case '@':
	    return User.OP_MASK;
	case '%':
	    return User.HALFOP_MASK;
	case '+':
	    return User.VOICE_MASK;
	default:
	    return User.NORMAL_MASK;
	}
    }

    static public String maskToSymbolic(int mask) {
	if (0 != (User.OP_MASK & mask)) {
	    return "@";
	} else if (0 != (User.HALFOP_MASK & mask)) {
	    return "%";
	} else if (0 != (User.VOICE_MASK & mask)) {
	    return "+";
	}

	return "";
    }

    static public int alphaToMask(char ch) {
	switch (ch) {
	case 'o':
	    return User.OP_MASK;
	case 'h':
	    return User.HALFOP_MASK;
	case 'v':
	    return User.VOICE_MASK;
	default:
	    return User.NORMAL_MASK;
	}
    }

    static public String maskToAlpha(int mask) {
	if (0 != (User.OP_MASK & mask)) {
	    return "o";
	} else if (0 != (User.HALFOP_MASK & mask)) {
	    return "h";
	} else if (0 != (User.VOICE_MASK & mask)) {
	    return "v";
	}

	return "";
    }
}
