/*
Javier Kohen's Java Framework Classes.
Copyright (C) 2001  Javier Kohen <jkohen at tough.com>

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

package ar.com.jkohen.text;

import java.text.Collator;

/**
 * This class implements a text completer. Given a list of words it will iterate through all the items in the list returning those that begin with the supplied pattern, one at a time. The iteration will be restarted from the beginning of the list once all the items have been run through.
 *
 * @author <a href="mailto:jkohen@tough.com">Javier Kohen</a>
 */
public class Completer {
    private String [] list;
    private String pattern;
    private int index;

    /**
     * Creates a new <code>Completer</code> instance.
     *
     * @param list a <code>String[]</code> containing the list of completion words
     */
    public Completer(String [] list) {
	this(list, "");
    }

    /**
     * Creates a new <code>Completer</code> instance.
     *
     * @param list a <code>String[]</code> containing the list of completion words
     * @param pattern the <code>String</code> to complete
     */
    public Completer(String [] list, String pattern) {
	loadList(list);
	setPattern(pattern);
	reset();
    }

    /**
     * Loads a new list of completion words.
     *
     * @param list a <code>String[]</code> containing the new list of words
     */
    public void loadList(String [] list) {
	this.list = list;
	this.index = 0;
    }

    /**
     * Sets a new <code>String</code> thas specifies the pattern that has to be completed.
     *
     * @param pattern the <code>String</code> to complete
     */
    public void setPattern(String pattern) {
	this.pattern = pattern;
    }

    /**
     * Iterates through the list, returning the next match from where the last completion was left. It will start over from the beginning once all the items have been run through.
     *
     * @return the completed <code>String</code>
     */
    public String nextCompletion() {
	return nextCompletion(pattern);
    }

    /**
     * Iterates through the list, returning the next match from where the last completion was left. It will start over from the beginning once all the items have been run through.
     * This method allows the caller to specify a different pattern to complete. This new pattern is set as the default.
     *
     * @param pattern the <code>String</code> to complete
     * @return the completed <code>String</code>. Null if no completion could be found
     */
    public String nextCompletion(String pattern) {
	return nextCompletion(pattern, Collator.getInstance());
    }

    /**
     * Iterates through the list, returning the next match from where the last completion was left. It will start over from the beginning once all the items have been run through.
     * This method allows the caller to specify a Collator different from the default. This Collator is used to match the completions.
     *
     * @param collator the <code>Collator</code> used to match the completions
     * @return the completed <code>String</code>. Null if no completion could be found
     */
    public String nextCompletion(Collator collator) {
	return nextCompletion(pattern, collator);
    }

    /**
     * Iterates through the list, returning the next match from where the last completion was left. It will start over from the beginning once all the items have been run through.
     * This method allows the caller to specify a different pattern to complete. This new pattern is set as the default.
     * This method also allows the caller to specify a Collator different from the default. This Collator is used to match the completions.
     *
     * @param pattern the <code>String</code> to complete
     * @param collator the <code>Collator</code> used to match the completions
     * @return the completed <code>String</code>. Null if no completion could be found
     */
    public String nextCompletion(String pattern, Collator collator) {
	setPattern(pattern);

	int last_index = index;
	int ptn_len = pattern.length();
	boolean found = false;
	String completion = null;

	/* Find a completion. */

	do {
	    // It can't match if it's shorter.
	    if (list[index].length() < ptn_len) {
		continue;
	    }

	    String partial = list[index].substring(0, ptn_len);
	    if (collator.equals(pattern, partial)) {
		// A match was found.
		found = true;
		completion = list[index];
	    }

	    // Increment index, staying into list bounds.
	    this.index = (index + 1) % list.length;

	    // If index == last_index, it means all the list has been run through (except previous match). No completions will be found by going through it all over again.
	} while (!found && index != last_index);

	return found ? completion : null;
    }

    /**
     * Resets the internal state of the searcher. This method allows an instance of this class to be reused.
     *
     */
    public void reset() {
	this.index = 0;
    }
}
