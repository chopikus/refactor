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

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.text.ParseException;
import ar.com.jkohen.irc.Message;
import ar.com.jkohen.irc.MircMessage;
import ar.com.jkohen.net.*;
import ar.com.jkohen.util.ConfigurationProperties;

public class ServerThread extends Thread implements ServerProcess, Observer {
    private Socket sock;
    private ClientProcess client;
    private BufferedReader br;
    private BufferedWriter bw;

    private boolean disconnected;

    /* Configuration properties. */
    private boolean filter_mirc_attribs;

    public ServerThread(ClientProcess client, Socket s) throws IOException {
	this.client = client;
	this.sock = s;

	InputStream is = sock.getInputStream();
	this.br = new BufferedReader(new InputStreamReader(is));

	OutputStream os = sock.getOutputStream();
	this.bw = new BufferedWriter(new OutputStreamWriter(os));

	try {
	    // FIXME: this doesn't seem to be working.
	    // Prevents the lock-up that occurs when the route to the peer is dropped.
	    sock.setSoLinger(true, 30);
	} catch (SocketException ex) {
	    System.err.println(ex);
	}
    }
    
    public void run() {
	while (!disconnected) {
	    try {
		processNextLine();
	    } catch (IOException e) {
		System.err.println(e);
		disconnect();
	    }
	}

	client.disconnect(this);
    }

    private void processNextLine() throws IOException {
	String str = br.readLine();

	if (null == str) {
	    disconnect();
	    return;
	}

	try {
	    Message m;
	    if (filter_mirc_attribs) {
		m = new MircMessage(str);
	    } else {
		m = new Message(str);
	    }

	    client.processMessage(this, m);
	} catch (ParseException e) {
	    System.err.println(e);
	}
    }

    public void enqueueMessage(Message m) throws IOException {
	String [] slices = m.slices();

	for (int i = 0; i < slices.length; i++) {
	    bw.write(slices[i], 0, slices[i].length());
	}

	bw.flush();
    }

    public void disconnect() {
	if (disconnected) {
	    return;
	}

	disconnected = true;

	try {
	    br.close();
	} catch (IOException e) {
		// Who cares?
	}

	try {
	    bw.close();
	} catch (IOException e) {
		// Who cares?
	}
    }

    public Socket getSocket() {
	return sock;
    }

    public void update(Observable o, Object arg) {
	ConfigurationProperties props = (ConfigurationProperties) o;

	if (null == arg || arg.equals("filter_mirc_attribs")) {
	    this.filter_mirc_attribs = props.getBoolean("filter_mirc_attribs");
	}
    }
}
