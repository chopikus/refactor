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

package ar.com.jkohen.awt;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import ar.com.jkohen.irc.MircMessage;
import ar.com.jkohen.util.Resource;

public class TextAttributePicker extends Panel implements ActionListener {
    private ActionListener actionListener;

    private ColorPicker color_picker;
    private ImageButton reset;
    private ImageButton bold;
    private ImageButton under;
    private ImageButton italic;

    private Color deffg;
    private Color defbg;

    public TextAttributePicker() {
	throw new IllegalArgumentException("can't have a RGBColorPicker, yet");
//  	this.color_picker = new RGBColorPicker();
    }

    public TextAttributePicker(Color [] colors, int size) {
	this.deffg = colors[0];
	this.defbg = colors[1];

	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();

	setLayout(gb);

	this.reset = new ImageButton();
	reset.setImage(ImageButton.ENTERED, getResourceImage("regular.gif"));
	reset.setImage(ImageButton.EXITED, getResourceImage("regular.gif"));
	reset.setImage(ImageButton.PRESSED, getResourceImage("regular-p.gif"));
  	add(reset);

	this.bold = new ImageButton();
	bold.setImage(ImageButton.ENTERED, getResourceImage("bold.gif"));
	bold.setImage(ImageButton.EXITED, getResourceImage("bold.gif"));
	bold.setImage(ImageButton.PRESSED, getResourceImage("bold-p.gif"));
	add(bold);

	this.under = new ImageButton();
	under.setImage(ImageButton.ENTERED, getResourceImage("under.gif"));
	under.setImage(ImageButton.EXITED, getResourceImage("under.gif"));
	under.setImage(ImageButton.PRESSED, getResourceImage("under-p.gif"));
	add(under);

	this.italic = new ImageButton();
	italic.setImage(ImageButton.ENTERED, getResourceImage("italic.gif"));
	italic.setImage(ImageButton.EXITED, getResourceImage("italic.gif"));
	italic.setImage(ImageButton.PRESSED, getResourceImage("italic-p.gif"));
	add(italic);

	this.color_picker = new IndexedColorPicker(colors, size);
	gbc.anchor = GridBagConstraints.EAST;
	gbc.weightx = 1.0;
	add((Component) color_picker);
	gb.setConstraints((Component) color_picker, gbc);
	gbc.weightx = 0.0;
	gbc.anchor = GridBagConstraints.CENTER;

	/* Event listeners.
	 */
	reset.addActionListener(this);
	bold.addActionListener(this);
	under.addActionListener(this);
	italic.addActionListener(this);
	color_picker.addActionListener(this);
    }

    public ColorPicker getColorPicker() {
	return color_picker;
    }

    private Image getResourceImage(String name) {
	Toolkit tk = Toolkit.getDefaultToolkit();

	try {
	    return tk.createImage(Resource.getResource(name, this));
	} catch (IOException e) {
	    System.err.println(e);
	}

	return null;
    }

    public void actionPerformed(ActionEvent ev) {
	Object source = ev.getSource();
	String text_attr = "";

	if (source.equals(color_picker)) {
	    Color c = color_picker.getSelected();
	    int ci;
	    int modifiers = ev.getModifiers();

	    if (color_picker instanceof IndexedColorPicker) {
		ci = ((IndexedColorPicker) color_picker).getSelectedIndex();
	    } else {
		ci = c.getRGB();
	    }

	    if (0 != (MouseEvent.BUTTON1_MASK & modifiers)
		|| 0 == modifiers) { // WORKAROUND: #32 Netscape Navigator doesn't set it right for BUTTON1.
		text_attr = MircMessage.attrEncode(MircMessage.COLOR) + ci;
	    } else if (0 != (MouseEvent.BUTTON3_MASK & modifiers)) {
		text_attr = MircMessage.attrEncode(MircMessage.COLOR)
		    + "," + ci;
	    }
	} else if (source.equals(bold)) {
	    text_attr = MircMessage.attrEncode(MircMessage.BOLD);
	} else if (source.equals(under)) {
	    text_attr = MircMessage.attrEncode(MircMessage.UNDERLINE);
	} else if (source.equals(italic)) {
	    text_attr = MircMessage.attrEncode(MircMessage.ITALIC);
	} else if (source.equals(reset)) {
	    text_attr = MircMessage.attrEncode(MircMessage.RESET);
	}

	processActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
					   text_attr, ev.getModifiers()));
    }

    public void addActionListener(ActionListener l) {
	if (null != l) {
	    actionListener = AWTEventMulticaster.add(actionListener, l);
	}
    }

    public void removeActionListener(ActionListener l) {
	if (null != l) {
	    actionListener = AWTEventMulticaster.remove(actionListener, l);
	}
    }

    protected void processActionEvent(ActionEvent e) {
	if (actionListener != null) {
	    actionListener.actionPerformed(e);
	}
    }
}
