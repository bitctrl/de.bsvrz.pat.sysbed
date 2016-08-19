/*
 * Copyright 2016 by Kappich Systemberatung Aachen
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
import de.bsvrz.dav.daf.main.config.AttributeType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Darstellung eines JaNein-Attributs als Checkbox
 *
 * @author Kappich Systemberatung
 */
public class CheckBoxEditorPlugIn extends DataEditorPlugIn{

	@Override
	public Box createComponent(final Data data, final boolean editable, final List<JButton> additionalButtons) {
		final Box box;
		box = Box.createHorizontalBox();
		box.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		box.add(Box.createHorizontalStrut(5));
		final JLabel suffixBox = new JLabel(data.isPlain() ? data.asTextValue().getSuffixText() : "");

		Collection<JButton> optionalButtons = new LinkedList<JButton>();

		JComponent valueBox = createValueBox(data, editable, optionalButtons, suffixBox);

		valueBox.setInheritsPopupMenu(true);
		
		box.add(valueBox);
		box.add(Box.createHorizontalStrut(1));
		box.add(suffixBox);
		box.add(Box.createHorizontalGlue());
		Box buttonBox;
		buttonBox = box;
		for(JButton button : optionalButtons) {
			buttonBox.add(Box.createHorizontalStrut(5));
			buttonBox.add(button);
		}	
		for(JButton button : additionalButtons) {
			buttonBox.add(Box.createHorizontalStrut(5));
			buttonBox.add(button);
		}
		return box;
	}

	@Override
	protected int getPriority() {
		return 20;
	}

	@Override
	public boolean supportsData(final Data data) {
		if(!data.isPlain()) return false;
		AttributeType attributeType = data.getAttributeType();
		return attributeType != null && attributeType.getPid().equals("att.jaNein");
	}

	@Override
	public JComponent createValueBox(final Data data, final boolean editable, final Collection<JButton> additionalButtons, final JLabel suffixBox) {
		JCheckBox checkBox = new JCheckBox(data.getName(), data.asTextValue().getValueText().equals("Ja"));
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				data.asTextValue().setText(checkBox.isSelected() ? "Ja" : "Nein");
			}
		});
		checkBox.setEnabled(editable);
		return checkBox;
	}
}
