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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observable;

public class Channel extends Observable {
    public static final int TOPIC_MASK = 0x01;

    private String tag;
    private String topic;
    private String url;
    private int modes_mask;

    private Hashtable users;

    public Channel(String tag) {
	this.tag = tag;
	this.users = new Hashtable();
    }

    public String getTag() {
	return tag;
    }

    /* Having a setTag method would be useless since a channel's tag doesn't change through time, and one has to be passed along in the constructor. */

    public String getTopic() {
	return topic;
    }

    public void setTopic(String topic) {
	this.topic = topic;
	setChanged();
	notifyObservers("topic");
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
	setChanged();
	notifyObservers("url");
    }


    /*
     * User related methods.
     */

    public void add(String raw_nick) {
	/* Obtain tag and modes out of the uncanonicalized nick. */
	String nick = Modes.canonicalizeNick(raw_nick);
	int mask = Modes.symbolicToMask(raw_nick.charAt(0));

	User user = new User(nick);
	user.setModes(mask);

	synchronized (users) {
	    users.put(nick, user);
	}

	setChanged();
	notifyObservers("add ".concat(nick));
    }

    /**
     * Removes a <code>User</code> from the <code>Channel</code>. This method does nothing if <code>nick</code> isn't found in the channel.
     *
     * @param nick whom to remove.
     */
    public void remove(String nick) {
	boolean existed;

	synchronized (users) {
	    existed = null != users.remove(nick);
	}

	if (existed) {
	    setChanged();
	    notifyObservers("remove ".concat(nick));
	}
    }

    public void rename(String nick, String new_nick) {
	synchronized (users) {
	    User user = (User) users.remove(nick);
	    user.setTag(new_nick);
	    users.put(new_nick, user);
	}

	setChanged();
	notifyObservers("rename ".concat(nick).concat(" ").concat(new_nick));
    }

    public User get(String nick) {
  	return (User) users.get(nick);
    }

    public boolean contains(String nick) {
	return users.containsKey(nick);
    }

    public User [] elements() {
	User [] a;
	Enumeration e;

	synchronized (users) {
	    a = new User [users.size()];
	    e = users.elements();
	}

	for (int i = 0; e.hasMoreElements(); i++) {
	    a[i] = (User) e.nextElement();
	}

	return a;
    }


    /*
     * Mode related methods.
     */

    /**
     * Set channel modes.
     */
    public void setModes(String modes, String [] params) {
	char [] mode_ary = modes.toCharArray();
	boolean sign = false;

	int j = 0;
	for (int i = 0; i < mode_ary.length; i++) {
	    switch (mode_ary[i]) {
	    case '+':
		sign = true;
		break;
	    case '-':
		sign = false;
		break;
	    case 'v':
		setUserModes(params[j++], sign, User.VOICE_MASK);
		break;
	    case 'o':
		setUserModes(params[j++], sign, User.OP_MASK);
		break;
	    case 'h':
		setUserModes(params[j++], sign, User.HALFOP_MASK);
		break;
	    case 't':
		setChannelModes(sign, Channel.TOPIC_MASK);
		break;
	    case 'k':
	    case 'l':
	    case 'b':
	    case 'e':
	    case 'I':
		// These modes aren't handled, but are known to carry a parameter which must be skipped.
		j++;
		break;
	    }
	}

	setChanged();
	notifyObservers("mode");
    }

    private void setUserModes(String nick, boolean sign, int mask) {
	User user = get(nick);

	if (sign) {
	    user.setModes(mask);
	} else {
	    user.unsetModes(mask);
	}
    }

    private void setChannelModes(boolean sign, int mask) {
	if (sign) {
	    modes_mask |= mask;
	} else {
	    modes_mask &= ~mask;
	}
    }

    public boolean isTopicSettable() {
	return (0 == (modes_mask & TOPIC_MASK));
    }


    public static boolean isChannel(String s) {
	char ch = s.charAt(0);

	return (ch == '&' || ch == '#' || ch == '+' || ch == '!');
    }
}
