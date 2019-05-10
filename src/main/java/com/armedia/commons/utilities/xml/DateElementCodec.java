package com.armedia.commons.utilities.xml;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Function;

import org.apache.commons.lang3.time.FastDateFormat;

import com.armedia.commons.utilities.Tools;

public class DateElementCodec implements AnyElementCodec<Date> {

	private final FastDateFormat encoder;
	private final Collection<FastDateFormat> decoders;

	public DateElementCodec(String encoder, String... extraDecoders) {
		this(encoder, null, null, extraDecoders);
	}

	public DateElementCodec(String encoder, Collection<String> extraDecoders) {
		this(encoder, null, null, extraDecoders);
	}

	public DateElementCodec(String encoder, TimeZone timeZone, String... extraDecoders) {
		this(encoder, timeZone, null, extraDecoders);
	}

	public DateElementCodec(String encoder, TimeZone timeZone, Collection<String> extraDecoders) {
		this(encoder, timeZone, null, extraDecoders);
	}

	public DateElementCodec(String encoder, Locale locale, String... extraDecoders) {
		this(encoder, null, locale, extraDecoders);
	}

	public DateElementCodec(String encoder, Locale locale, Collection<String> extraDecoders) {
		this(encoder, null, locale, extraDecoders);
	}

	public DateElementCodec(String encoder, TimeZone timeZone, Locale locale, String... extraDecoders) {
		this(encoder, timeZone, locale,
			(extraDecoders != null ? Arrays.asList(extraDecoders) : Collections.emptyList()));
	}

	public DateElementCodec(String encoder, TimeZone timeZone, Locale locale, Collection<String> extraDecoders) {
		Objects.requireNonNull(encoder, "Must provide a non-null date encoder");
		if (locale == null) {
			locale = Locale.getDefault();
		}
		if (timeZone == null) {
			timeZone = TimeZone.getDefault();
		}
		this.encoder = FastDateFormat.getInstance(encoder, timeZone, locale);
		Collection<FastDateFormat> decoders = new LinkedList<>();
		decoders.add(this.encoder); // Always start with this one
		for (String d : extraDecoders) {
			decoders.add(FastDateFormat.getInstance(d, timeZone, locale));
		}
		this.decoders = Tools.freezeCollection(decoders);
	}

	@Override
	public Function<Date, String> getEncoder() {
		return this.encoder::format;
	}

	protected Date parse(String s) {
		for (FastDateFormat f : this.decoders) {
			try {
				return f.parse(s);
			} catch (ParseException e) {
				// Do nothing...
			}
		}
		throw new RuntimeException(
			String.format("Could not parse the date string [%s] with any of these formats: %s", s, this.decoders));
	}

	@Override
	public Function<String, Date> getDecoder() {
		return this::parse;
	}
}