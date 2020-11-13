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

package ar.com.jkohen.awt.event;

import java.awt.AWTEvent;
import java.awt.event.ComponentEvent;
import ar.com.jkohen.awt.ChatPanel;

public class ChatPanelEvent extends ComponentEvent {
    public final static int CHATPANEL_FIRST = AWTEvent.RESERVED_ID_MAX + 1;
    public final static int CHATPANEL_CLOSING = CHATPANEL_FIRST + 1;
    public final static int CHATPANEL_LAST = CHATPANEL_FIRST + 2;

    public ChatPanelEvent(ChatPanel source, int id) {
	super(source, id);
    }

    public ChatPanel getChatPanel() {
	return (ChatPanel) getSource();
    }
}
