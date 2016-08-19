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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Collection;

/**
 * GTM-Editor-Plugin, dass für einfache Textwerte zuständig ist
 *
 * @author Kappich Systemberatung
 */
public class TextEditorPlugIn extends PlainEditorPlugIn {

	private static Color _originalTextboxBackground;

	@Override
	public JTextField createValueBox(final Data data, final boolean editable, final Collection<JButton> additionalButtons, final JLabel suffixBox) {
		final JTextField textBox;
		
		textBox = createComponent(data);
		_originalTextboxBackground = textBox.getBackground();
		textBoxAttributeModified(textBox, suffixBox, data, false);

		textBox.setEditable(editable);
		textBox.addFocusListener(
				new FocusAdapter() {
					public void focusLost(FocusEvent e) {
						textBoxAttributeModified(textBox, suffixBox, data, true);
					}
				}
		);
		textBox.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						textBoxAttributeModified(textBox, suffixBox, data, true);
					}
				}
		);
		return textBox;
	}

	protected JTextField createComponent(final Data data) {
		return new JTextField();
	}

	/**
	 * Überprüft, ob der eingegebene Wert auch ein zulässiger Wert ist.
	 * @param textBox   Komponente, wo der neue Wert eingegeben wurde
	 * @param suffixBox das Feld mit dem Suffix
	 * @param data      die modifizierten Daten
	 * @param update
	 */
	protected static void textBoxAttributeModified(final JTextComponent textBox, final JLabel suffixBox, final Data data, final boolean update) {
		String text = textBox.getText();
		try {
			if(update) {
				if(text.equals(_defaultValueString) || text.equals(_undefinedString)) {
					data.setToDefault();
				}
				else {
					data.asTextValue().setText(text);
				}
			}

			if(!data.isDefined()) {
				textBox.setBackground(_backgroundUndefinedValue);
				textBox.setText(_undefinedString);
				if(suffixBox != null) suffixBox.setText("");
			}
			else {
				textBox.setBackground(_originalTextboxBackground);
				textBox.setText(data.asTextValue().getValueText());
				if(suffixBox != null) suffixBox.setText(data.asTextValue().getSuffixText());
			}
		}
		catch(Exception ex) {
			_debug.error("Fehler beim Setzen eines Werts", ex);
			// Wenn beim setzen des Textes ein Fehler auftritt, dann wird der Wert auf "undefiniert" gesetzt
			// und der Benutzer muss sich Gedanken um den Wert machen
			data.setToDefault();
			textBox.setBackground(_backgroundUndefinedValue);
			textBox.setText(_undefinedString);
			if(suffixBox != null) suffixBox.setText("");
		}
		if(suffixBox != null) suffixBox.setVisible(!suffixBox.getText().isEmpty());
	}

	@Override
	protected int getPriority() {
		return 0;
	}

	@Override
	public String toString() {
		return "Textwert";
	}
}
