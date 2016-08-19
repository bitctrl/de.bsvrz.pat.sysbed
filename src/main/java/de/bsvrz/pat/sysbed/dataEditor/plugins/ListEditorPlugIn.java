/*
 * Copyright 2015 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.sysbed.dataEditor.plugins;

import de.bsvrz.dav.daf.main.Data;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.util.Collection;
import java.util.List;

/**
 * GTM-Editor-Plugin, dass für Listen zuständig ist
 *
 * @author Kappich Systemberatung
 */
public class ListEditorPlugIn extends DataEditorPlugIn {

	@Override
	public Box createComponent(final Data data, final boolean editable, final List<JButton> additionalButtons) {
		final Box box;
		box = createValueBox(data, editable, additionalButtons, null);
		box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(data.getName() + ": "), new EmptyBorder(5, 5, 5, 5)));
		return box;
	}

	@Override
	public Box createValueBox(final Data data, final boolean editable, final Collection<JButton> additionalButtons, final JLabel suffixBox) {
		final Box box;
		box = Box.createVerticalBox();
		createListComponent(data, box, editable);
		if(!additionalButtons.isEmpty()) {
			Box horizontalBox = Box.createHorizontalBox();
			horizontalBox.add(Box.createHorizontalGlue());
			for(JButton additionalButton : additionalButtons) {
				horizontalBox.add(Box.createHorizontalStrut(5));
				horizontalBox.add(additionalButton);
			}
			box.add(horizontalBox);
		}
		return box;
	}

	private void createListComponent(final Data data, final Box box, final boolean editable) {
		for(final Data subData : data) {
			box.add(_registry.createBox(subData, editable));
		}
	}

	@Override
	public boolean supportsData(final Data data) {
		return data.isList();
	}

	@Override
	public String toString() {
		return "Liste";
	}

	@Override
	protected int getPriority() {
		return 0;
	}
}
