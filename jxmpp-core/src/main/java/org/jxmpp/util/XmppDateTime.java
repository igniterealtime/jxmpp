/**
 *
 * Copyright © 2014-2018 Florian Schmaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jxmpp.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for date and time handling in XMPP.
 *
 * @see <a href="http://xmpp.org/extensions/xep-0082.html">XEP-82: XMPP Date and Time Profiles</a>
 */
public class XmppDateTime {

	private static final DateFormatType dateFormatter = DateFormatType.XEP_0082_DATE_PROFILE;
	private static final Pattern datePattern = Pattern.compile("^\\d+-\\d+-\\d+$");

	private static final DateFormatType timeFormatter = DateFormatType.XEP_0082_TIME_MILLIS_ZONE_PROFILE;
	private static final Pattern timePattern = Pattern.compile("^(\\d+:){2}\\d+.\\d+(Z|([+-](\\d+:?\\d+)))$");
	private static final DateFormatType timeNoZoneFormatter = DateFormatType.XEP_0082_TIME_MILLIS_PROFILE;
	private static final Pattern timeNoZonePattern = Pattern.compile("^(\\d+:){2}\\d+.\\d+$");

	private static final DateFormatType timeNoMillisFormatter = DateFormatType.XEP_0082_TIME_ZONE_PROFILE;
	private static final Pattern timeNoMillisPattern = Pattern.compile("^(\\d+:){2}\\d+(Z|([+-](\\d+:?\\d+)))$");
	private static final DateFormatType timeNoMillisNoZoneFormatter = DateFormatType.XEP_0082_TIME_PROFILE;
	private static final Pattern timeNoMillisNoZonePattern = Pattern.compile("^(\\d+:){2}\\d+$");

	private static final DateFormatType dateTimeFormatter = DateFormatType.XEP_0082_DATETIME_MILLIS_PROFILE;
	private static final Pattern dateTimePattern = Pattern
			.compile("^\\d+(-\\d+){2}+T(\\d+:){2}\\d+.\\d+(Z|([+-](\\d+:?\\d+)))$");
	private static final DateFormatType dateTimeNoMillisFormatter = DateFormatType.XEP_0082_DATETIME_PROFILE;
	private static final Pattern dateTimeNoMillisPattern = Pattern
			.compile("^\\d+(-\\d+){2}+T(\\d+:){2}\\d+(Z|([+-](\\d+:?\\d+)))$");

	private static final TimeZone TIME_ZONE_UTC = TimeZone.getTimeZone("UTC");

	private static final ThreadLocal<DateFormat> xep0091Formatter = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
			dateFormat.setTimeZone(TIME_ZONE_UTC);
			return dateFormat;
		}
	};
	private static final ThreadLocal<DateFormat> xep0091Date6DigitFormatter = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			DateFormat dateFormat = new SimpleDateFormat("yyyyMd'T'HH:mm:ss");
			dateFormat.setTimeZone(TIME_ZONE_UTC);
			return dateFormat;
		}
	};
	private static final ThreadLocal<DateFormat> xep0091Date7Digit1MonthFormatter = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			DateFormat dateFormat = new SimpleDateFormat("yyyyMdd'T'HH:mm:ss");
			dateFormat.setTimeZone(TIME_ZONE_UTC);
			dateFormat.setLenient(false);
			return dateFormat;
		}
	};
	private static final ThreadLocal<DateFormat> xep0091Date7Digit2MonthFormatter = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMd'T'HH:mm:ss");
			dateFormat.setTimeZone(TIME_ZONE_UTC);
			dateFormat.setLenient(false);
			return dateFormat;
		}
	};
	private static final Pattern xep0091Pattern = Pattern.compile("^\\d+T\\d+:\\d+:\\d+$");

	@SuppressWarnings("ImmutableEnumChecker")
	private enum DateFormatType {
		// @formatter:off
		XEP_0082_DATE_PROFILE("yyyy-MM-dd"),
		XEP_0082_DATETIME_PROFILE("yyyy-MM-dd'T'HH:mm:ssZ"),
		XEP_0082_DATETIME_MILLIS_PROFILE("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
		XEP_0082_TIME_PROFILE("hh:mm:ss"),
		XEP_0082_TIME_ZONE_PROFILE("hh:mm:ssZ"),
		XEP_0082_TIME_MILLIS_PROFILE("hh:mm:ss.SSS"),
		XEP_0082_TIME_MILLIS_ZONE_PROFILE("hh:mm:ss.SSSZ"),
		XEP_0091_DATETIME("yyyyMMdd'T'HH:mm:ss");
		// @formatter:on

		private final String FORMAT_STRING;
		private final ThreadLocal<DateFormat> FORMATTER;
		private final boolean CONVERT_TIMEZONE;

		/**
		 * XEP-0082 allows the fractional second addendum to contain ANY number
		 * of digits. Implementations are therefore free to send as much digits
		 * after the dot as they want, therefore we need to truncate or fill up
		 * milliseconds. Certain platforms are only able to parse up to milliseconds,
		 * so truncate to 3 digits after the dot or fill zeros until 3 digits.
		 */
		private final boolean HANDLE_MILLIS;

		DateFormatType(String dateFormat) {
			FORMAT_STRING = dateFormat;
			FORMATTER = new ThreadLocal<DateFormat>() {
				@Override
				protected DateFormat initialValue() {
					DateFormat dateFormat = new SimpleDateFormat(FORMAT_STRING);
					dateFormat.setTimeZone(TIME_ZONE_UTC);
					return dateFormat;
				}
			};
			CONVERT_TIMEZONE = dateFormat.charAt(dateFormat.length() - 1) == 'Z';
			HANDLE_MILLIS = dateFormat.contains("SSS");
		}

		private String format(Date date) {
			String res = FORMATTER.get().format(date);
			if (CONVERT_TIMEZONE) {
				res = convertRfc822TimezoneToXep82(res);
			}
			return res;
		}

		private Date parse(String dateString) throws ParseException {
			if (CONVERT_TIMEZONE) {
				dateString = convertXep82TimezoneToRfc822(dateString);
			}
			if (HANDLE_MILLIS) {
				dateString = handleMilliseconds(dateString);
			}
			return FORMATTER.get().parse(dateString);
		}
	}

	private static final List<PatternCouplings> couplings = new ArrayList<PatternCouplings>();

	static {
		couplings.add(new PatternCouplings(datePattern, dateFormatter));
		couplings.add(new PatternCouplings(dateTimePattern, dateTimeFormatter));
		couplings.add(new PatternCouplings(dateTimeNoMillisPattern, dateTimeNoMillisFormatter));
		couplings.add(new PatternCouplings(timePattern, timeFormatter));
		couplings.add(new PatternCouplings(timeNoZonePattern, timeNoZoneFormatter));
		couplings.add(new PatternCouplings(timeNoMillisPattern, timeNoMillisFormatter));
		couplings.add(new PatternCouplings(timeNoMillisNoZonePattern, timeNoMillisNoZoneFormatter));
	}

	/**
	 * Parses the given date string in the <a
	 * href="http://xmpp.org/extensions/xep-0082.html">XEP-0082 - XMPP Date and
	 * Time Profiles</a>.
	 * 
	 * @param dateString
	 *            the date string to parse
	 * @return the parsed Date
	 * @throws ParseException
	 *             if the specified string cannot be parsed
	 */
	public static Date parseXEP0082Date(String dateString) throws ParseException {
		for (PatternCouplings coupling : couplings) {
			Matcher matcher = coupling.pattern.matcher(dateString);

			if (matcher.matches()) {
				return coupling.formatter.parse(dateString);
			}
		}
		/*
		 * We assume it is the XEP-0082 DateTime profile with no milliseconds at
		 * this point. If it isn't, is is just not parseable, then we attempt to
		 * parse it regardless and let it throw the ParseException.
		 */
		return dateTimeNoMillisFormatter.parse(dateString);
	}

	/**
	 * Parses the given date string in either of the three profiles of <a
	 * href="http://xmpp.org/extensions/xep-0082.html">XEP-0082 - XMPP Date and
	 * Time Profiles</a> or <a
	 * href="http://xmpp.org/extensions/xep-0091.html">XEP-0091 - Legacy Delayed
	 * Delivery</a> format.
	 * <p>
	 * This method uses internal date formatters and is thus threadsafe.
	 * 
	 * @param dateString
	 *            the date string to parse
	 * @return the parsed Date
	 * @throws ParseException
	 *             if the specified string cannot be parsed
	 */
	public static Date parseDate(String dateString) throws ParseException {
		Matcher matcher = xep0091Pattern.matcher(dateString);

		/*
		 * if date is in XEP-0091 format handle ambiguous dates missing the
		 * leading zero in month and day
		 */
		if (matcher.matches()) {
			int length = dateString.split("T")[0].length();

			if (length < 8) {
				Date date = handleDateWithMissingLeadingZeros(dateString, length);

				if (date != null)
					return date;
			} else {
				return xep0091Formatter.get().parse(dateString);
			}
		}
		// Assume XEP-82 date if Matcher does not match
		return parseXEP0082Date(dateString);
	}

	/**
	 * Formats a Date into a XEP-0082 - XMPP Date and Time Profiles string.
	 * 
	 * @param date
	 *            the time value to be formatted into a time string
	 * @return the formatted time string in XEP-0082 format
	 */
	public static String formatXEP0082Date(Date date) {
		return dateTimeFormatter.format(date);
	}

	/**
	 * Converts a XEP-0082 date String's time zone definition into a RFC822 time
	 * zone definition. The major difference is that XEP-0082 uses a smicolon
	 * between hours and minutes and RFC822 does not.
	 * 
	 * @param dateString the date String.
	 * @return the String with converted timezone
	 */
	public static String convertXep82TimezoneToRfc822(String dateString) {
		if (dateString.charAt(dateString.length() - 1) == 'Z') {
			return dateString.replace("Z", "+0000");
		} else {
			// If the time zone wasn't specified with 'Z', then it's in
			// ISO8601 format (i.e. '(+|-)HH:mm')
			// RFC822 needs a similar format just without the colon (i.e.
			// '(+|-)HHmm)'), so remove it
			return dateString.replaceAll("([\\+\\-]\\d\\d):(\\d\\d)", "$1$2");
		}
	}

	/**
	 * Convert a RFC 822 Timezone to the Timezone format used in XEP-82.
	 *
	 * @param dateString the input date String.
	 * @return the input String with the timezone converted to XEP-82.
	 */
	public static String convertRfc822TimezoneToXep82(String dateString) {
		int length = dateString.length();
		String res = dateString.substring(0, length - 2);
		res += ':';
		res += dateString.substring(length - 2, length);
		return res;
	}

	/**
	 * Converts a time zone to the String format as specified in XEP-0082.
	 * 
	 * @param timeZone the time zone to convert.
	 * @return the String representation of the TimeZone
	 */
	public static String asString(TimeZone timeZone) {
		int rawOffset = timeZone.getRawOffset();
		int hours = rawOffset / (1000 * 60 * 60);
		int minutes = Math.abs((rawOffset / (1000 * 60)) - (hours * 60));
		return String.format("%+d:%02d", hours, minutes);
	}

	/**
	 * Parses the given date string in different ways and returns the date that
	 * lies in the past and/or is nearest to the current date-time.
	 * 
	 * @param stampString
	 *            date in string representation
	 * @param dateLength the length of the date prefix of stampString
	 * @return the parsed date
	 * @throws ParseException
	 *             The date string was of an unknown format
	 */
	private static Date handleDateWithMissingLeadingZeros(String stampString, int dateLength) throws ParseException {
		if (dateLength == 6) {
			return xep0091Date6DigitFormatter.get().parse(stampString);
		}
		Calendar now = Calendar.getInstance();

		Calendar oneDigitMonth = parseXEP91Date(stampString, xep0091Date7Digit1MonthFormatter.get());
		Calendar twoDigitMonth = parseXEP91Date(stampString, xep0091Date7Digit2MonthFormatter.get());

		List<Calendar> dates = filterDatesBefore(now, oneDigitMonth, twoDigitMonth);

		if (!dates.isEmpty()) {
			return determineNearestDate(now, dates).getTime();
		}
		return null;
	}

	private static Calendar parseXEP91Date(String stampString, DateFormat dateFormat) {
		try {
			dateFormat.parse(stampString);
			return dateFormat.getCalendar();
		} catch (ParseException e) {
			return null;
		}
	}

	private static List<Calendar> filterDatesBefore(Calendar now, Calendar... dates) {
		List<Calendar> result = new ArrayList<Calendar>();

		for (Calendar calendar : dates) {
			if (calendar != null && calendar.before(now)) {
				result.add(calendar);
			}
		}

		return result;
	}


	/**
	 * A pattern with 3 capturing groups, the second one are at least 1 digits
	 * after the 'dot'. The last one is the timezone definition, either 'Z',
	 * '+1234' or '-1234'.
	 */
	private static final Pattern SECOND_FRACTION = Pattern.compile(".*\\.(\\d{1,})(Z|((\\+|-)\\d{4}))");

	/**
	 * Handle the milliseconds. This means either fill up with zeros or
	 * truncate the date String so that the fractional second addendum only
	 * contains 3 digits. Returns the given string unmodified if it doesn't
	 * match {@link #SECOND_FRACTION}.
	 * 
	 * @param dateString the date string
	 * @return the date String where the fractional second addendum is a most 3
	 *         digits
	 */
	private static String handleMilliseconds(String dateString) {
		Matcher matcher = SECOND_FRACTION.matcher(dateString);
		if (!matcher.matches()) {
			// The date string does not contain any milliseconds
			return dateString;
		}

		int fractionalSecondsDigitCount = matcher.group(1).length();
		if (fractionalSecondsDigitCount == 3) {
			// The date string has exactly 3 fractional second digits
			return dateString;
		}

		// Gather information about the date string
		int posDecimal = dateString.indexOf(".");
		StringBuilder sb = new StringBuilder(dateString.length() - fractionalSecondsDigitCount + 3);
		if (fractionalSecondsDigitCount > 3) {
			// Append only 3 fractional digits after posDecimal
			sb.append(dateString.substring(0, posDecimal + 4));
		} else {
			// The date string has less then 3 fractional second digits
			sb.append(dateString.substring(0, posDecimal + fractionalSecondsDigitCount + 1));
			// Fill up the "missing" fractional second digits with zeros
			for (int i = fractionalSecondsDigitCount; i < 3; i++) {
				sb.append('0');
			}
		}
		// Append the timezone definition
		sb.append(dateString.substring(posDecimal + fractionalSecondsDigitCount + 1));
		return sb.toString();
	}

	private static Calendar determineNearestDate(final Calendar now, List<Calendar> dates) {

		Collections.sort(dates, new Comparator<Calendar>() {

			@Override
			public int compare(Calendar o1, Calendar o2) {
				Long diff1 = now.getTimeInMillis() - o1.getTimeInMillis();
				Long diff2 = now.getTimeInMillis() - o2.getTimeInMillis();
				return diff1.compareTo(diff2);
			}

		});

		return dates.get(0);
	}

	private static class PatternCouplings {
		final Pattern pattern;
		final DateFormatType formatter;

		PatternCouplings(Pattern datePattern, DateFormatType dateFormat) {
			pattern = datePattern;
			formatter = dateFormat;
		}
	}
}
