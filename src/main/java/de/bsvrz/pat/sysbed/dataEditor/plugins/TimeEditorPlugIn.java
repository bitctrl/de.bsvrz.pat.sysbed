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
import de.bsvrz.dav.daf.main.config.StringAttributeType;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Locale;

/**
 * GTM-Editor-Plugin, dass für Zeitwerte (ohen datum) zuständig ist (att.uhrzeit)
 *
 * @author Kappich Systemberatung
 */
public class TimeEditorPlugIn extends ComboBoxEditorPlugIn {

	public static final DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder()
			.parseLenient()
			.appendValue(ChronoField.HOUR_OF_DAY, 2)
			.optionalStart()
			.appendLiteral(':')
			.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
			.optionalStart()
			.appendLiteral(':')
			.appendValue(ChronoField.SECOND_OF_MINUTE, 2)
			.optionalEnd()
			.optionalEnd()
			.toFormatter(Locale.GERMAN);


	@Override
	protected void addComboItems(final Data data, final JComboBox<String> comboBox) {
		comboBox.addItem("Jetzt");
	}

	@Override
	protected void comboBoxAttributeModified(final JComboBox<String> comboBox, final JLabel suffixBox, final Data data, final boolean update) {
		if(update){
			String value= comboBox.getSelectedItem().toString();
			LocalTime date = parseDate(value);
			if(date != null) {
				data.asTextValue().setText(TIME_FORMAT.format(date));
				comboBox.setSelectedItem(data.asTextValue().getValueText());
			}
		}
		super.comboBoxAttributeModified(comboBox, suffixBox, data, false);
	}

	private LocalTime parseDate(String value) {
		if(value.toLowerCase().equals("jetzt")) return LocalTime.now();
		try {
			return LocalTime.parse(value, TIME_FORMAT);
		}
		catch(DateTimeParseException ignored){
			return null;
		}
	}

	@Override
	protected int getPriority() {
		return 10;
	}

	@Override
	public boolean supportsData(final Data data) {
		if(!data.isPlain()) return false;
		if(data.getAttributeType() instanceof StringAttributeType) {
			return data.getAttributeType().getPid().equals("att.uhrzeit");
		}
		return false;
	}

	@Override
	public String toString() {
		return "Zeit";
	}
}
