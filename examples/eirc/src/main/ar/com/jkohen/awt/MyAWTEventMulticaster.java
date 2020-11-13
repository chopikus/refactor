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

import java.awt.AWTEventMulticaster;
import java.util.EventListener;
import ar.com.jkohen.awt.event.ChatPanelEvent;
import ar.com.jkohen.awt.event.ChatPanelListener;

public class MyAWTEventMulticaster extends AWTEventMulticaster implements ChatPanelListener {
    protected MyAWTEventMulticaster(EventListener a, EventListener b) {
	super(a, b);
    }

    public static ChatPanelListener add(ChatPanelListener a,
					ChatPanelListener b) {
	return (ChatPanelListener) addInternal(a, b);
    }

    public static ChatPanelListener remove(ChatPanelListener a,
					   ChatPanelListener b) {
	return (ChatPanelListener) removeInternal(a, b);
    }

    public void chatPanelClosing(ChatPanelEvent e) {
        ((ChatPanelListener) a).chatPanelClosing(e);
	((ChatPanelListener) b).chatPanelClosing(e);
    }
}
