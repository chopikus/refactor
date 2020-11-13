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

import java.awt.AWTEventMulticaster;
import java.awt.Panel;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import ar.com.jkohen.awt.event.ChatPanelEvent;
import ar.com.jkohen.awt.event.ChatPanelListener;

public class ChatPanel extends Panel {
    private String tag;
    private boolean disposed;

    protected ChatPanelListener chatPanelListener;
    protected TextListener textListener;

    public ChatPanel(String tag) {
	this.tag = tag;
    }

    public String getPanelTag() {
	return tag;
    }

    public void setPanelTag(String tag) {
	this.tag = tag;
    }

//      public void show() {
//  	this.disposed = false;
//  	super.show();
//      }

    public void setVisible(boolean state) {
	if (state) {
	    this.disposed = false;
	}
	super.setVisible(state);
    }

    public void dispose() {
	if (disposed) {
	    return;
	}

	this.disposed = true;
	postChatPanelClosingEvent();
    }

    public void addChatPanelListener(ChatPanelListener l) {
	if (null != l) {
	    chatPanelListener = MyAWTEventMulticaster.add(chatPanelListener, l);
	}
    }

    public void removeChatPanelListener(ChatPanelListener l) {
	if (null != l) {
	    chatPanelListener = MyAWTEventMulticaster.remove(chatPanelListener, l);
	}
    }

    private synchronized void postChatPanelClosingEvent() {
	if (chatPanelListener != null) {
	    chatPanelListener.chatPanelClosing(new ChatPanelEvent(this, ChatPanelEvent.CHATPANEL_CLOSING));
	}
    }

    public void addTextListener(TextListener l) {
	if (null != l) {
	    textListener = AWTEventMulticaster.add(textListener, l);
	}
    }

    public void removeTextListener(TextListener l) {
	if (null != l) {
	    textListener = AWTEventMulticaster.remove(textListener, l);
	}
    }

    protected synchronized void postTextEvent() {
	if (textListener != null) {
	    textListener.textValueChanged(new TextEvent(this, TextEvent.TEXT_VALUE_CHANGED));
	}
    }
}
