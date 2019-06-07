package com.armedia.commons.utilities.codec;

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

public class DateCodec extends StringCodec<Date> {

	public static DateCodec from(String encoder, String... extraDecoders) {
		return DateCodec.from(encoder, null, null, extraDecoders);
	}

	public static DateCodec from(String encoder, Collection<String> extraDecoders) {
		return DateCodec.from(encoder, null, null, extraDecoders);
	}

	public static DateCodec from(String encoder, TimeZone timeZone, String... extraDecoders) {
		return DateCodec.from(encoder, timeZone, null, extraDecoders);
	}

	public static DateCodec from(String encoder, TimeZone timeZone, Collection<String> extraDecoders) {
		return DateCodec.from(encoder, timeZone, null, extraDecoders);
	}

	public static DateCodec from(String encoder, Locale locale, String... extraDecoders) {
		return DateCodec.from(encoder, null, locale, extraDecoders);
	}

	public static DateCodec from(String encoder, Locale locale, Collection<String> extraDecoders) {
		return DateCodec.from(encoder, null, locale, extraDecoders);
	}

	public static DateCodec from(String encoder, TimeZone timeZone, Locale locale, String... extraDecoders) {
		return DateCodec.from(encoder, timeZone, locale,
			(extraDecoders != null ? Arrays.asList(extraDecoders) : Collections.emptyList()));
	}

	public static DateCodec from(String encoderString, TimeZone timeZone, Locale locale,
		Collection<String> extraDecoderStrings) {
		Objects.requireNonNull(encoderString, "Must provide a non-null date encoder");
		if (locale == null) {
			locale = Locale.getDefault();
		}
		if (timeZone == null) {
			timeZone = TimeZone.getDefault();
		}
		final FastDateFormat encoder = FastDateFormat.getInstance(encoderString, timeZone, locale);
		final Collection<FastDateFormat> decoders = new LinkedList<>();
		decoders.add(encoder); // Always start with this one
		for (String d : extraDecoderStrings) {
			decoders.add(FastDateFormat.getInstance(d, timeZone, locale));
		}
		return DateCodec.from(encoder, decoders);
	}

	public static DateCodec from(FastDateFormat encoder, FastDateFormat... extraDecoders) {
		return DateCodec.from(encoder, extraDecoders != null ? Arrays.asList(extraDecoders) : null);
	}

	public static DateCodec from(FastDateFormat encoder, Collection<FastDateFormat> extraDecoders) {
		Objects.requireNonNull(encoder, "Must provide a non-null date encoder");
		final Collection<FastDateFormat> decoders = new LinkedList<>();
		decoders.add(encoder);
		if (extraDecoders != null) {
			extraDecoders.stream().filter(Objects::nonNull).forEach(decoders::add);
		}
		Function<String, Date> parse = (s) -> {
			for (FastDateFormat f : decoders) {
				try {
					return f.parse(s);
				} catch (ParseException e) {
					// Do nothing...
				}
			}
			throw new RuntimeException(
				String.format("Could not parse the date string [%s] with any of these formats: %s", s, decoders));
		};
		return new DateCodec(encoder::format, parse);
	}

	private DateCodec(Function<Date, String> encoder, Function<String, Date> decoder) {
		super(encoder, decoder);
	}
}