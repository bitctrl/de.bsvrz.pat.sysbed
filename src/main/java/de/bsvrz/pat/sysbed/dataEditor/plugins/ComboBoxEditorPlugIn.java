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
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueRange;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.pat.sysbed.dataEditor.ExtendedEditorPanel;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Collection;
import java.util.List;

/**
 * GTM-Editor-Plugin, dass für Integer-Attribute (ggf. mit Zustandswerten) zuständig ist und als Editor eine ComboBox darstellt.
 * Für bestimmte Anwendungsfälle ist diese Klasse ableitbar
 *
 * @author Kappich Systemberatung
 */
public class ComboBoxEditorPlugIn extends PlainEditorPlugIn {
	
	@Override
	public JComponent createValueBox(final Data data, final boolean editable, final Collection<JButton> additionalButtons, final JLabel suffixBox) {
		final JComponent valueBox;
		if(editable) {
			final JComboBox<String> comboBox = new JComboBox<String>();
			comboBox.setEditable(true);

			addComboItems(data, comboBox);
			
			comboBoxAttributeModified(comboBox, suffixBox, data, false);

			FocusAdapter l = new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					comboBoxAttributeModified(comboBox, suffixBox, data, true);
				}
			};
			comboBox.addFocusListener(l);
			comboBox.getEditor().getEditorComponent().addFocusListener(l);
			
			comboBox.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							comboBoxAttributeModified(comboBox, suffixBox, data, true);
						}
					}
			);

			valueBox = comboBox;
		}
		else {			// ist false -> nicht editierbares Textfeld
			final JTextField textField = new JTextField();
			textField.setText(data.asTextValue().getValueText());
			textField.setEditable(false);
			valueBox = textField;
		}
		return valueBox;
	}

	protected void addComboItems(final Data data, final JComboBox<String> comboBox) {
		IntegerAttributeType att = (IntegerAttributeType)data.getAttributeType();
		List<IntegerValueState> states = att.getStates();
		for(IntegerValueState state : states) {
			comboBox.addItem(state.getName());
		}
		IntegerValueRange range = att.getRange();
		if(range != null) {
			final long unscaledMinimum = range.getMinimum();
			final long unscaledMaximum = range.getMaximum();
			final double conversionFactor = range.getConversionFactor();
			comboBox.addItem(ExtendedEditorPanel.getScaledValueText(unscaledMinimum, conversionFactor));
			comboBox.addItem(ExtendedEditorPanel.getScaledValueText(unscaledMaximum, conversionFactor));
		}
	}

	protected void comboBoxAttributeModified(final JComboBox<String> comboBox, final JLabel suffixBox, final Data data, final boolean update) {
		JTextComponent textComponent = (JTextComponent) comboBox.getEditor().getEditorComponent();
		TextEditorPlugIn.textBoxAttributeModified(textComponent, suffixBox, data, update);
		if(update && data.isDefined()) {
			comboBox.setSelectedItem(data.asTextValue().getText());
		}
	}

	@Override
	public boolean supportsData(final Data data) {
		return super.supportsData(data) && data.getAttributeType() instanceof IntegerAttributeType;
	}


	@Override
	protected int getPriority() {
		return 10;
	}

	@Override
	public String toString() {
		return "Auswahlliste";
	}
}
