/*
Javier Kohen's Java Framework Classes.
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

package ar.com.jkohen.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;

public class Resource {
    /**
     * BUGS:
     *  * Netscape Navigator (tested with 4.7x) won't load files from outside a JAR.
     *  * Life would be easier if Netscape Navigator supported Class.getResource(). Their not supporting it is actually documented on Netscape's Developers website.
     */
    public static byte [] getResource(String name, Object target)
	throws IOException {
	// Resolve object to a Class, if needed.
	Class cl = (target instanceof Class)
	    ? (Class) target : target.getClass();
	InputStream is = cl.getResourceAsStream(name);
	BufferedInputStream bis = new BufferedInputStream(is);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	int pos = 0;
	int readed;
	final int chunk_size = 4096;
  	byte [] buf = new byte [chunk_size];

	while (-1 != (readed = bis.read(buf, 0, chunk_size))) {
	    baos.write(buf, pos, readed);

	    pos += readed;
	}

	return baos.toByteArray();
    }

    public static Image createImage(String resource_name, Object target)
	throws IOException {

	byte [] image = getResource(resource_name, target);

	return Toolkit.getDefaultToolkit().createImage(image);
    }
}
