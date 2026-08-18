// buggy code
    public static Date parse(String date, ParsePosition pos) throws ParseException {
        Exception fail = null;
        try {
            int offset = pos.getIndex();

            // extract year
            int year = parseInt(date, offset, offset += 4);
            if (checkOffset(date, offset, '-')) {
                offset += 1;
            }

            // extract month
            int month = parseInt(date, offset, offset += 2);
            if (checkOffset(date, offset, '-')) {
                offset += 1;
            }

            // extract day
            int day = parseInt(date, offset, offset += 2);
            // default time value
            int hour = 0;
            int minutes = 0;
            int seconds = 0;
            int milliseconds = 0; // always use 0 otherwise returned date will include millis of current time

            // if the value has no time component (and no time zone), we are done
            boolean hasT = checkOffset(date, offset, 'T');
            
            if (!hasT && (date.length() <= offset)) {
                Calendar calendar = new GregorianCalendar(year, month - 1, day);

                pos.setIndex(offset);
                return calendar.getTime();
            }

            if (hasT) {

                // extract hours, minutes, seconds and milliseconds
                hour = parseInt(date, offset += 1, offset += 2);
                if (checkOffset(date, offset, ':')) {
                    offset += 1;
                }

                minutes = parseInt(date, offset, offset += 2);
                if (checkOffset(date, offset, ':')) {
                    offset += 1;
                }
                // second and milliseconds can be optional
                if (date.length() > offset) {
                    char c = date.charAt(offset);
                    if (c != 'Z' && c != '+' && c != '-') {
                        seconds = parseInt(date, offset, offset += 2);
                        if (seconds > 59 && seconds < 63) seconds = 59; // truncate up to 3 leap seconds
                        // milliseconds can be optional in the format
                        if (checkOffset(date, offset, '.')) {
                            offset += 1;
                            int endOffset = indexOfNonDigit(date, offset + 1); // assume at least one digit
                            int parseEndOffset = Math.min(endOffset, offset + 3); // parse up to 3 digits
                            int fraction = parseInt(date, offset, parseEndOffset);
                            // compensate for "missing" digits
                            switch (parseEndOffset - offset) { // number of digits parsed
                            case 2:
                                milliseconds = fraction * 10;
                                break;
                            case 1:
                                milliseconds = fraction * 100;
                                break;
                            default:
                                milliseconds = fraction;
                            }
                            offset = endOffset;
                        }
                    }
                }
            }

            // extract timezone
            if (date.length() <= offset) {
                throw new IllegalArgumentException("No time zone indicator");
            }

            TimeZone timezone = null;
            char timezoneIndicator = date.charAt(offset);

            if (timezoneIndicator == 'Z') {
                timezone = TIMEZONE_UTC;
                offset += 1;
            } else if (timezoneIndicator == '+' || timezoneIndicator == '-') {
                String timezoneOffset = date.substring(offset);

                // When timezone has no minutes, we should append it, valid timezones are, for example: +00:00, +0000 and +00

                offset += timezoneOffset.length();
                // 18-Jun-2015, tatu: Minor simplification, skip offset of "+0000"/"+00:00"
                if ("+0000".equals(timezoneOffset) || "+00:00".equals(timezoneOffset)) {
                    timezone = TIMEZONE_UTC;
                } else {
                    // 18-Jun-2015, tatu: Looks like offsets only work from GMT, not UTC...
                    //    not sure why, but that's the way it looks. Further, Javadocs for
                    //    `java.util.TimeZone` specifically instruct use of GMT as base for
                    //    custom timezones... odd.
                    String timezoneId = "GMT" + timezoneOffset;
//                    String timezoneId = "UTC" + timezoneOffset;

                    timezone = TimeZone.getTimeZone(timezoneId);

                    String act = timezone.getID();
                    if (!act.equals(timezoneId)) {
                        /* 22-Jan-2015, tatu: Looks like canonical version has colons, but we may be given
                         *    one without. If so, don't sweat.
                         *   Yes, very inefficient. Hopefully not hit often.
                         *   If it becomes a perf problem, add 'loose' comparison instead.
                         */
                        String cleaned = act.replace(":", "");
                        if (!cleaned.equals(timezoneId)) {
                            throw new IndexOutOfBoundsException("Mismatching time zone indicator: "+timezoneId+" given, resolves to "
                                    +timezone.getID());
                        }
                    }
                }
            } else {
                throw new IndexOutOfBoundsException("Invalid time zone indicator '" + timezoneIndicator+"'");
            }

            Calendar calendar = new GregorianCalendar(timezone);
            calendar.setLenient(false);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minutes);
            calendar.set(Calendar.SECOND, seconds);
            calendar.set(Calendar.MILLISECOND, milliseconds);

            pos.setIndex(offset);
            return calendar.getTime();
            // If we get a ParseException it'll already have the right message/offset.
            // Other exception types can convert here.
        } catch (IndexOutOfBoundsException e) {
            fail = e;
        } catch (NumberFormatException e) {
            fail = e;
        } catch (IllegalArgumentException e) {
            fail = e;
        }
        String input = (date == null) ? null : ('"' + date + "'");
        String msg = fail.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = "("+fail.getClass().getName()+")";
        }
        ParseException ex = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
        ex.initCause(fail);
        throw ex;
    }

// relevant test
// com.google.gson.DefaultDateTypeAdapterTest::testFormattingInEnUs
  public void testFormattingInEnUs() {
    assertFormattingAlwaysEmitsUsLocale(Locale.US);
  }

// com.google.gson.DefaultDateTypeAdapterTest::testFormattingInFr
  public void testFormattingInFr() {
    assertFormattingAlwaysEmitsUsLocale(Locale.FRANCE);
  }

// com.google.gson.DefaultDateTypeAdapterTest::testParsingDatesFormattedWithSystemLocale
  public void testParsingDatesFormattedWithSystemLocale() {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.FRANCE);
    try {
      assertParsed("1 janv. 1970 à 00:00:00", new DefaultDateTypeAdapter());
      assertParsed("01/01/70", new DefaultDateTypeAdapter(DateFormat.SHORT));
      assertParsed("1 janv. 1970", new DefaultDateTypeAdapter(DateFormat.MEDIUM));
      assertParsed("1 janvier 1970", new DefaultDateTypeAdapter(DateFormat.LONG));
      assertParsed("01/01/70 00:00",
          new DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.SHORT));
      assertParsed("1 janv. 1970 à 00:00:00",
          new DefaultDateTypeAdapter(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertParsed("1 janvier 1970 à 00:00:00 UTC",
          new DefaultDateTypeAdapter(DateFormat.LONG, DateFormat.LONG));
      assertParsed("jeudi 1 janvier 1970 à 00:00:00 Temps universel coordonné",
          new DefaultDateTypeAdapter(DateFormat.FULL, DateFormat.FULL));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

// com.google.gson.DefaultDateTypeAdapterTest::testParsingDatesFormattedWithUsLocale
  public void testParsingDatesFormattedWithUsLocale() {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      assertParsed("Jan 1, 1970, 0:00:00 AM", new DefaultDateTypeAdapter());
      assertParsed("1/1/70", new DefaultDateTypeAdapter(DateFormat.SHORT));
      assertParsed("Jan 1, 1970", new DefaultDateTypeAdapter(DateFormat.MEDIUM));
      assertParsed("January 1, 1970", new DefaultDateTypeAdapter(DateFormat.LONG));
      assertParsed("1/1/70, 0:00 AM",
          new DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.SHORT));
      assertParsed("Jan 1, 1970, 0:00:00 AM",
          new DefaultDateTypeAdapter(DateFormat.MEDIUM, DateFormat.MEDIUM));
      assertParsed("January 1, 1970 at 0:00:00 AM UTC",
          new DefaultDateTypeAdapter(DateFormat.LONG, DateFormat.LONG));
      assertParsed("Thursday, January 1, 1970 at 0:00:00 AM UTC",
          new DefaultDateTypeAdapter(DateFormat.FULL, DateFormat.FULL));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

// com.google.gson.DefaultDateTypeAdapterTest::testFormatUsesDefaultTimezone
  public void testFormatUsesDefaultTimezone() {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      assertFormatted("Dec 31, 1969, 4:00:00 PM", new DefaultDateTypeAdapter());
      assertParsed("Dec 31, 1969, 4:00:00 PM", new DefaultDateTypeAdapter());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

// com.google.gson.DefaultDateTypeAdapterTest::testDateDeserializationISO8601
  public void testDateDeserializationISO8601() throws Exception {
  	DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter();
    assertParsed("1970-01-01T00:00:00.000Z", adapter);
    assertParsed("1970-01-01T00:00Z", adapter);
    assertParsed("1970-01-01T00:00:00+00:00", adapter);
    assertParsed("1970-01-01T01:00:00+01:00", adapter);
    assertParsed("1970-01-01T01:00:00+01", adapter);
  }

// com.google.gson.DefaultDateTypeAdapterTest::testDateSerialization
  public void testDateSerialization() throws Exception {
    int dateStyle = DateFormat.LONG;
    DefaultDateTypeAdapter dateTypeAdapter = new DefaultDateTypeAdapter(dateStyle);
    DateFormat formatter = DateFormat.getDateInstance(dateStyle, Locale.US);
    Date currentDate = new Date();

    String dateString = dateTypeAdapter.serialize(currentDate, Date.class, null).getAsString();
    assertEquals(formatter.format(currentDate), dateString);
  }

// com.google.gson.DefaultDateTypeAdapterTest::testDatePattern
  public void testDatePattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    DefaultDateTypeAdapter dateTypeAdapter = new DefaultDateTypeAdapter(pattern);
    DateFormat formatter = new SimpleDateFormat(pattern);
    Date currentDate = new Date();

    String dateString = dateTypeAdapter.serialize(currentDate, Date.class, null).getAsString();
    assertEquals(formatter.format(currentDate), dateString);
  }

// com.google.gson.DefaultDateTypeAdapterTest::testInvalidDatePattern
  public void testInvalidDatePattern() throws Exception {
    try {
      new DefaultDateTypeAdapter("I am a bad Date pattern....");
      fail("Invalid date pattern should fail.");
    } catch (IllegalArgumentException expected) { }
  }
