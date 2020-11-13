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

public class ChannelItem {
    private String tag;
    private int vu;
    private String t;

    public ChannelItem(String tag, int vu, String t) {
	this.tag = tag;
	this.vu = vu;
	this.t = t;
    }

    public String getTag() {
	return tag;
    }

    public int getUsersCount() {
	return vu;
    }

    public String getTopic() {
	return t;
    }

    public static boolean isChannel(String s) {
	char ch = s.charAt(0);

	return (ch == '&' || ch == '#' || ch == '+' || ch == '!');
    }
}
