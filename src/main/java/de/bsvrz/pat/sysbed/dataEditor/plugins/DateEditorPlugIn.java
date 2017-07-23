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
import de.bsvrz.dav.daf.main.config.TimeAttributeType;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

/**
 * GTM-Editor-Plugin, dass für absolute Datumswerte zuständig ist
 *
 * @author Kappich Systemberatung
 */
public class DateEditorPlugIn extends ComboBoxEditorPlugIn {

	private static final DateTimeFormatter[] _parseDateFormats = {
			new DateTimeFormatterBuilder()
					.parseLenient()
					.optionalStart()
					.appendValue(ChronoField.DAY_OF_MONTH)
					.appendLiteral('.')
					.optionalStart()
					.appendValue(ChronoField.MONTH_OF_YEAR)
					.optionalStart()
					.appendLiteral('.')
					.optionalStart()
					.appendValueReduced(ChronoField.YEAR, 2, 10, LocalDate.now())
					.optionalEnd()
					.optionalEnd()
					.optionalEnd()
					.optionalEnd()
					.optionalStart()
					.appendLiteral(' ')
					.optionalEnd()
					.optionalStart()
					.appendValue(ChronoField.HOUR_OF_DAY)
					.optionalStart()
					.appendLiteral(':')
					.appendValue(ChronoField.MINUTE_OF_HOUR)
					.optionalStart()
					.appendLiteral(':')
					.appendValue(ChronoField.SECOND_OF_MINUTE)
					.optionalStart()
					.appendLiteral(',')
					.appendValue(ChronoField.MILLI_OF_SECOND)
					.optionalEnd()
					.optionalEnd()
					.optionalEnd()
					.optionalEnd()
					.parseDefaulting(ChronoField.MONTH_OF_YEAR, LocalDate.now().getMonthValue())
					.parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear())
					.toFormatter(Locale.GERMAN)

	};


	@Override
	protected void addComboItems(final Data data, final JComboBox<String> comboBox) {
		comboBox.addItem("Jetzt");
	}

	@Override
	protected void comboBoxAttributeModified(final JComboBox<String> comboBox, final JLabel suffixBox, final Data data, final boolean update) {
		if(update){
			String value= comboBox.getSelectedItem().toString();
			long date = parseDate(value);
			data.asTimeValue().setMillis(date);
			if(date != 0) {
				comboBox.setSelectedItem(data.asTextValue().getValueText());
			}
		}
		super.comboBoxAttributeModified(comboBox, suffixBox, data, false);
	}

	private long parseDate(String value) {
		if(value.toLowerCase().equals("jetzt")) return System.currentTimeMillis();
		value = value.replace('-',' ');
		for(final DateTimeFormatter format : _parseDateFormats) {
			try {
				TemporalAccessor temporalAccessor = format.parseBest(value, LocalDateTime::from, LocalDate::from, LocalTime::from);
				LocalDateTime dateTime;
				if(temporalAccessor instanceof LocalDateTime) {
					dateTime = (LocalDateTime) temporalAccessor;
				}
				else if(temporalAccessor instanceof LocalDate) {
					LocalDate localDate = (LocalDate) temporalAccessor;
					dateTime = LocalDateTime.of(localDate, LocalTime.now());
				}
				else {
					LocalTime accessor = (LocalTime) temporalAccessor;
					dateTime = LocalDateTime.of(LocalDate.now(), accessor);
				}
				return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			}
			catch(DateTimeParseException e) {
				//continue with next Format
			}
		}
		return 0;
	}

	@Override
	protected int getPriority() {
		return 10;
	}

	@Override
	public boolean supportsData(final Data data) {
		if(!data.isPlain()) return false;
		if(data.getAttributeType() instanceof TimeAttributeType) {
			TimeAttributeType timeAttributeType = (TimeAttributeType) data.getAttributeType();
			return !timeAttributeType.isRelative();
		}
		return false;
	}

	@Override
	public String toString() {
		return "Datum";
	}
}
