package com.colinalworth.gwt.viola.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

	private final DateFormat iso8601Format;
	private final DateFormat rxfFormat;

	public DateTypeAdapter() {
		this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		this.iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));

		this.rxfFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	}

	// These methods need to be synchronized since JDK DateFormat classes are not thread-safe
	// See issue 162
	public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
		synchronized (iso8601Format) {
			String dateFormatAsString = iso8601Format.format(src);
			return new JsonPrimitive(dateFormatAsString);
		}
	}

	public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		if (!(json instanceof JsonPrimitive)) {
			throw new JsonParseException("The date should be a string value");
		}
		Date date = deserializeToDate(json);
		if (typeOfT == Date.class) {
			return date;
		} else if (typeOfT == Timestamp.class) {
			return new Timestamp(date.getTime());
		} else if (typeOfT == java.sql.Date.class) {
			return new java.sql.Date(date.getTime());
		} else {
			throw new IllegalArgumentException(getClass() + " cannot deserialize to " + typeOfT);
		}
	}

	private Date deserializeToDate(JsonElement json) {
		synchronized (iso8601Format) {
			try {
				return iso8601Format.parse(json.getAsString());
			} catch (ParseException e) {
				//fall back to old format
			}
			try {
				return rxfFormat.parse(json.getAsString());
			} catch (ParseException e) {
				throw new JsonSyntaxException(json.getAsString(), e);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(DateTypeAdapter.class.getSimpleName());
		sb.append('(').append(iso8601Format.getClass().getSimpleName()).append(')');
		return sb.toString();
	}
}
