package uk.ac.rhul.cs.dice.vacuumworld.utils;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class VacuumWorldLogFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		StringBuilder builder = new StringBuilder(formatMessage(record));
		builder.append("\n");
		
		return builder.toString();		
	}
}