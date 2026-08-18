// buggy code
    public static synchronized FastDateFormat getDateInstance(int style, TimeZone timeZone, Locale locale) {
        Object key = new Integer(style);
        if (timeZone != null) {
            key = new Pair(key, timeZone);
        }

        if (locale != null) {
            key = new Pair(key, locale);
        }


        FastDateFormat format = (FastDateFormat) cDateInstanceCache.get(key);
        if (format == null) {
            if (locale == null) {
                locale = Locale.getDefault();
            }
            try {
                SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateInstance(style, locale);
                String pattern = formatter.toPattern();
                format = getInstance(pattern, timeZone, locale);
                cDateInstanceCache.put(key, format);
                
            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("No date pattern for locale: " + locale);
            }
        }
        return format;
    }

    public static synchronized FastDateFormat getDateTimeInstance(int dateStyle, int timeStyle, TimeZone timeZone,
            Locale locale) {

        Object key = new Pair(new Integer(dateStyle), new Integer(timeStyle));
        if (timeZone != null) {
            key = new Pair(key, timeZone);
        }
        if (locale != null) {
            key = new Pair(key, locale);
        }

        FastDateFormat format = (FastDateFormat) cDateTimeInstanceCache.get(key);
        if (format == null) {
            if (locale == null) {
                locale = Locale.getDefault();
            }
            try {
                SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateTimeInstance(dateStyle, timeStyle,
                        locale);
                String pattern = formatter.toPattern();
                format = getInstance(pattern, timeZone, locale);
                cDateTimeInstanceCache.put(key, format);

            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("No date time pattern for locale: " + locale);
            }
        }
        return format;
    }

// relevant test
// org.apache.commons.lang.time.DateFormatUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new DateFormatUtils());
        Constructor[] cons = DateFormatUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(DateFormatUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(DateFormatUtils.class.getModifiers()));
    }

// org.apache.commons.lang.time.DateFormatUtilsTest::testFormat
    public void testFormat() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(2005,0,1,12,0,0);
        c.setTimeZone(TimeZone.getDefault());
        StringBuffer buffer = new StringBuffer ();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        buffer.append (year);
        buffer.append(month);
        buffer.append(day);
        buffer.append(hour);
        assertEquals(buffer.toString(), DateFormatUtils.format(c.getTime(), "yyyyMdH"));
        
        assertEquals(buffer.toString(), DateFormatUtils.format(c.getTime().getTime(), "yyyyMdH"));
        
        assertEquals(buffer.toString(), DateFormatUtils.format(c.getTime(), "yyyyMdH", Locale.US));
        
        assertEquals(buffer.toString(), DateFormatUtils.format(c.getTime().getTime(), "yyyyMdH", Locale.US));
    }

// org.apache.commons.lang.time.DateFormatUtilsTest::testFormatUTC
    public void testFormatUTC() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(2005,0,1,12,0,0);
        assertEquals ("2005-01-01T12:00:00", DateFormatUtils.formatUTC(c.getTime(), DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()));
        
        assertEquals ("2005-01-01T12:00:00", DateFormatUtils.formatUTC(c.getTime().getTime(), DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()));
        
        assertEquals ("2005-01-01T12:00:00", DateFormatUtils.formatUTC(c.getTime(), DateFormatUtils.ISO_DATETIME_FORMAT.getPattern(), Locale.US));
        
        assertEquals ("2005-01-01T12:00:00", DateFormatUtils.formatUTC(c.getTime().getTime(), DateFormatUtils.ISO_DATETIME_FORMAT.getPattern(), Locale.US));
    }

// org.apache.commons.lang.time.DateFormatUtilsTest::testDateTimeISO
    public void testDateTimeISO(){
        TimeZone timeZone = TimeZone.getTimeZone("GMT-3");
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2002,1,23,9,11,12);
        String text = DateFormatUtils.format(cal.getTime(), 
                        DateFormatUtils.ISO_DATETIME_FORMAT.getPattern(), timeZone);
        assertEquals("2002-02-23T09:11:12", text);
        text = DateFormatUtils.format(cal.getTime().getTime(), 
                      DateFormatUtils.ISO_DATETIME_FORMAT.getPattern(), timeZone);
        assertEquals("2002-02-23T09:11:12", text);
        text = DateFormatUtils.ISO_DATETIME_FORMAT.format(cal);
        assertEquals("2002-02-23T09:11:12", text);
        
        text = DateFormatUtils.format(cal.getTime(), 
                      DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern(), timeZone);
        assertEquals("2002-02-23T09:11:12-03:00", text);
        text = DateFormatUtils.format(cal.getTime().getTime(), 
                      DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern(), timeZone);
        assertEquals("2002-02-23T09:11:12-03:00", text);
        text = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(cal);
        assertEquals("2002-02-23T09:11:12-03:00", text);
    }

// org.apache.commons.lang.time.DateFormatUtilsTest::testDateISO
    public void testDateISO(){
        TimeZone timeZone = TimeZone.getTimeZone("GMT-3");
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2002,1,23,10,11,12);
        String text = DateFormatUtils.format(cal.getTime(), 
                        DateFormatUtils.ISO_DATE_FORMAT.getPattern(), timeZone);
        assertEquals("2002-02-23", text);
        text = DateFormatUtils.format(cal.getTime().getTime(), 
                        DateFormatUtils.ISO_DATE_FORMAT.getPattern(), timeZone);
        assertEquals("2002-02-23", text);
        text = DateFormatUtils.ISO_DATE_FORMAT.format(cal);
        assertEquals("2002-02-23", text);
        
        text = DateFormatUtils.format(cal.getTime(), 
                      DateFormatUtils.ISO_DATE_TIME_ZONE_FORMAT.getPattern(), timeZone);
        assertEquals("2002-02-23-03:00", text);
        text = DateFormatUtils.format(cal.getTime().getTime(), 
                      DateFormatUtils.ISO_DATE_TIME_ZONE_FORMAT.getPattern(), timeZone);
        assertEquals("2002-02-23-03:00", text);
        text = DateFormatUtils.ISO_DATE_TIME_ZONE_FORMAT.format(cal);
        assertEquals("2002-02-23-03:00", text);
    }

// org.apache.commons.lang.time.DateFormatUtilsTest::testTimeISO
    public void testTimeISO(){
        TimeZone timeZone = TimeZone.getTimeZone("GMT-3");
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2002,1,23,10,11,12);
        String text = DateFormatUtils.format(cal.getTime(), 
                        DateFormatUtils.ISO_TIME_FORMAT.getPattern(), timeZone);
        assertEquals("T10:11:12", text);
        text = DateFormatUtils.format(cal.getTime().getTime(), 
                        DateFormatUtils.ISO_TIME_FORMAT.getPattern(), timeZone);
        assertEquals("T10:11:12", text);
        text = DateFormatUtils.ISO_TIME_FORMAT.format(cal);
        assertEquals("T10:11:12", text);
        
        text = DateFormatUtils.format(cal.getTime(), 
                      DateFormatUtils.ISO_TIME_TIME_ZONE_FORMAT.getPattern(), timeZone);
        assertEquals("T10:11:12-03:00", text);
        text = DateFormatUtils.format(cal.getTime().getTime(), 
                      DateFormatUtils.ISO_TIME_TIME_ZONE_FORMAT.getPattern(), timeZone);
        assertEquals("T10:11:12-03:00", text);
        text = DateFormatUtils.ISO_TIME_TIME_ZONE_FORMAT.format(cal);
        assertEquals("T10:11:12-03:00", text);
    }

// org.apache.commons.lang.time.DateFormatUtilsTest::testTimeNoTISO
    public void testTimeNoTISO(){
        TimeZone timeZone = TimeZone.getTimeZone("GMT-3");
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2002,1,23,10,11,12);
        String text = DateFormatUtils.format(cal.getTime(), 
                        DateFormatUtils.ISO_TIME_NO_T_FORMAT.getPattern(), timeZone);
        assertEquals("10:11:12", text);
        text = DateFormatUtils.format(cal.getTime().getTime(), 
                        DateFormatUtils.ISO_TIME_NO_T_FORMAT.getPattern(), timeZone);
        assertEquals("10:11:12", text);
        text = DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(cal);
        assertEquals("10:11:12", text);
        
        text = DateFormatUtils.format(cal.getTime(), 
                      DateFormatUtils.ISO_TIME_NO_T_TIME_ZONE_FORMAT.getPattern(), timeZone);
        assertEquals("10:11:12-03:00", text);
        text = DateFormatUtils.format(cal.getTime().getTime(), 
                      DateFormatUtils.ISO_TIME_NO_T_TIME_ZONE_FORMAT.getPattern(), timeZone);
        assertEquals("10:11:12-03:00", text);
        text = DateFormatUtils.ISO_TIME_NO_T_TIME_ZONE_FORMAT.format(cal);
        assertEquals("10:11:12-03:00", text);
    }

// org.apache.commons.lang.time.DateFormatUtilsTest::testSMTP
    public void testSMTP(){
        TimeZone timeZone = TimeZone.getTimeZone("GMT-3");
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2003,5,8,10,11,12);
        String text = DateFormatUtils.format(cal.getTime(), 
                        DateFormatUtils.SMTP_DATETIME_FORMAT.getPattern(), timeZone,
                        DateFormatUtils.SMTP_DATETIME_FORMAT.getLocale());
        assertEquals("Sun, 08 Jun 2003 10:11:12 -0300", text);
        text = DateFormatUtils.format(cal.getTime().getTime(), 
                        DateFormatUtils.SMTP_DATETIME_FORMAT.getPattern(), timeZone,
                        DateFormatUtils.SMTP_DATETIME_FORMAT.getLocale());
        assertEquals("Sun, 08 Jun 2003 10:11:12 -0300", text);
        text = DateFormatUtils.SMTP_DATETIME_FORMAT.format(cal);
        assertEquals("Sun, 08 Jun 2003 10:11:12 -0300", text);
        
        
        text = DateFormatUtils.formatUTC(cal.getTime().getTime(), 
                        DateFormatUtils.SMTP_DATETIME_FORMAT.getPattern(),
                        DateFormatUtils.SMTP_DATETIME_FORMAT.getLocale());
        assertEquals("Sun, 08 Jun 2003 13:11:12 +0000", text);
    }

// org.apache.commons.lang.time.DurationFormatUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new DurationFormatUtils());
        Constructor[] cons = DurationFormatUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(DurationFormatUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(DurationFormatUtils.class.getModifiers()));
    }

// org.apache.commons.lang.time.DurationFormatUtilsTest::testFormatDurationWords
    public void testFormatDurationWords() {
        String text = null;

        text = DurationFormatUtils.formatDurationWords(50 * 1000, true, false);
        assertEquals("50 seconds", text);
        text = DurationFormatUtils.formatDurationWords(65 * 1000, true, false);
        assertEquals("1 minute 5 seconds", text);
        text = DurationFormatUtils.formatDurationWords(120 * 1000, true, false);
        assertEquals("2 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(121 * 1000, true, false);
        assertEquals("2 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(72 * 60 * 1000, true, false);
        assertEquals("1 hour 12 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(24 * 60 * 60 * 1000, true, false);
        assertEquals("1 day 0 hours 0 minutes 0 seconds", text);

        text = DurationFormatUtils.formatDurationWords(50 * 1000, true, true);
        assertEquals("50 seconds", text);
        text = DurationFormatUtils.formatDurationWords(65 * 1000, true, true);
        assertEquals("1 minute 5 seconds", text);
        text = DurationFormatUtils.formatDurationWords(120 * 1000, true, true);
        assertEquals("2 minutes", text);
        text = DurationFormatUtils.formatDurationWords(121 * 1000, true, true);
        assertEquals("2 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(72 * 60 * 1000, true, true);
        assertEquals("1 hour 12 minutes", text);
        text = DurationFormatUtils.formatDurationWords(24 * 60 * 60 * 1000, true, true);
        assertEquals("1 day", text);

        text = DurationFormatUtils.formatDurationWords(50 * 1000, false, true);
        assertEquals("0 days 0 hours 0 minutes 50 seconds", text);
        text = DurationFormatUtils.formatDurationWords(65 * 1000, false, true);
        assertEquals("0 days 0 hours 1 minute 5 seconds", text);
        text = DurationFormatUtils.formatDurationWords(120 * 1000, false, true);
        assertEquals("0 days 0 hours 2 minutes", text);
        text = DurationFormatUtils.formatDurationWords(121 * 1000, false, true);
        assertEquals("0 days 0 hours 2 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(72 * 60 * 1000, false, true);
        assertEquals("0 days 1 hour 12 minutes", text);
        text = DurationFormatUtils.formatDurationWords(24 * 60 * 60 * 1000, false, true);
        assertEquals("1 day", text);

        text = DurationFormatUtils.formatDurationWords(50 * 1000, false, false);
        assertEquals("0 days 0 hours 0 minutes 50 seconds", text);
        text = DurationFormatUtils.formatDurationWords(65 * 1000, false, false);
        assertEquals("0 days 0 hours 1 minute 5 seconds", text);
        text = DurationFormatUtils.formatDurationWords(120 * 1000, false, false);
        assertEquals("0 days 0 hours 2 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(121 * 1000, false, false);
        assertEquals("0 days 0 hours 2 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(72 * 60 * 1000, false, false);
        assertEquals("0 days 1 hour 12 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(24 * 60 * 60 * 1000 + 72 * 60 * 1000, false, false);
        assertEquals("1 day 1 hour 12 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(2 * 24 * 60 * 60 * 1000 + 72 * 60 * 1000, false, false);
        assertEquals("2 days 1 hour 12 minutes 0 seconds", text);
        for (int i = 2; i < 31; i++) {
            text = DurationFormatUtils.formatDurationWords(i * 24 * 60 * 60 * 1000, false, false);
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
        }
    }

// org.apache.commons.lang.time.DurationFormatUtilsTest::testFormatDurationPluralWords
    public void testFormatDurationPluralWords() {
        long oneSecond = 1000;
        long oneMinute = oneSecond * 60;
        long oneHour = oneMinute * 60;
        long oneDay = oneHour * 24;
        String text = null;

        text = DurationFormatUtils.formatDurationWords(oneSecond, false, false);
        assertEquals("0 days 0 hours 0 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(oneSecond * 2, false, false);
        assertEquals("0 days 0 hours 0 minutes 2 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneSecond * 11, false, false);
        assertEquals("0 days 0 hours 0 minutes 11 seconds", text);

        text = DurationFormatUtils.formatDurationWords(oneMinute, false, false);
        assertEquals("0 days 0 hours 1 minute 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneMinute * 2, false, false);
        assertEquals("0 days 0 hours 2 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneMinute * 11, false, false);
        assertEquals("0 days 0 hours 11 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneMinute + oneSecond, false, false);
        assertEquals("0 days 0 hours 1 minute 1 second", text);

        text = DurationFormatUtils.formatDurationWords(oneHour, false, false);
        assertEquals("0 days 1 hour 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneHour * 2, false, false);
        assertEquals("0 days 2 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneHour * 11, false, false);
        assertEquals("0 days 11 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneHour + oneMinute + oneSecond, false, false);
        assertEquals("0 days 1 hour 1 minute 1 second", text);

        text = DurationFormatUtils.formatDurationWords(oneDay, false, false);
        assertEquals("1 day 0 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneDay * 2, false, false);
        assertEquals("2 days 0 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneDay * 11, false, false);
        assertEquals("11 days 0 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneDay + oneHour + oneMinute + oneSecond, false, false);
        assertEquals("1 day 1 hour 1 minute 1 second", text);
    }

// org.apache.commons.lang.time.DurationFormatUtilsTest::testFormatDurationHMS
    public void testFormatDurationHMS() {
        long time = 0;
        assertEquals("0:00:00.000", DurationFormatUtils.formatDurationHMS(time));

        time = 1;
        assertEquals("0:00:00.001", DurationFormatUtils.formatDurationHMS(time));

        time = 15;
        assertEquals("0:00:00.015", DurationFormatUtils.formatDurationHMS(time));

        time = 165;
        assertEquals("0:00:00.165", DurationFormatUtils.formatDurationHMS(time));

        time = 1675;
        assertEquals("0:00:01.675", DurationFormatUtils.formatDurationHMS(time));

        time = 13465;
        assertEquals("0:00:13.465", DurationFormatUtils.formatDurationHMS(time));

        time = 72789;
        assertEquals("0:01:12.789", DurationFormatUtils.formatDurationHMS(time));

        time = 12789 + 32 * 60000;
        assertEquals("0:32:12.789", DurationFormatUtils.formatDurationHMS(time));

        time = 12789 + 62 * 60000;
        assertEquals("1:02:12.789", DurationFormatUtils.formatDurationHMS(time));
    }

// org.apache.commons.lang.time.DurationFormatUtilsTest::testFormatDurationISO
    public void testFormatDurationISO() {
        assertEquals("P0Y0M0DT0H0M0.000S", DurationFormatUtils.formatDurationISO(0L));
        assertEquals("P0Y0M0DT0H0M0.001S", DurationFormatUtils.formatDurationISO(1L));
        assertEquals("P0Y0M0DT0H0M0.010S", DurationFormatUtils.formatDurationISO(10L));
        assertEquals("P0Y0M0DT0H0M0.100S", DurationFormatUtils.formatDurationISO(100L));
        assertEquals("P0Y0M0DT0H1M15.321S", DurationFormatUtils.formatDurationISO(75321L));
    }

// org.apache.commons.lang.time.DurationFormatUtilsTest::testFormatDuration
    public void testFormatDuration() {
        long duration = 0;
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "y"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "M"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "d"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "H"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "m"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "s"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "S"));
        assertEquals("0000", DurationFormatUtils.formatDuration(duration, "SSSS"));
        assertEquals("0000", DurationFormatUtils.formatDuration(duration, "yyyy"));
        assertEquals("0000", DurationFormatUtils.formatDuration(duration, "yyMM"));

        duration = 60 * 1000;
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "y"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "M"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "d"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "H"));
        assertEquals("1", DurationFormatUtils.formatDuration(duration, "m"));
        assertEquals("60", DurationFormatUtils.formatDuration(duration, "s"));
        assertEquals("60000", DurationFormatUtils.formatDuration(duration, "S"));
        assertEquals("01:00", DurationFormatUtils.formatDuration(duration, "mm:ss"));

        Calendar base = Calendar.getInstance();
        base.set(2000, 0, 1, 0, 0, 0);
        base.set(Calendar.MILLISECOND, 0);

        Calendar cal = Calendar.getInstance();
        cal.set(2003, 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        duration = cal.getTime().getTime() - base.getTime().getTime(); 
        
        
        int days = 366 + 365 + 365 + 31;
        assertEquals("0 0 " + days, DurationFormatUtils.formatDuration(duration, "y M d"));
    }

// org.apache.commons.lang.time.DurationFormatUtilsTest::testFormatPeriodISO
    public void testFormatPeriodISO() {
        TimeZone timeZone = TimeZone.getTimeZone("GMT-3");
        Calendar base = Calendar.getInstance(timeZone);
        base.set(1970, 0, 1, 0, 0, 0);
        base.set(Calendar.MILLISECOND, 0);

        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2002, 1, 23, 9, 11, 12);
        cal.set(Calendar.MILLISECOND, 1);
        String text;
        
        text = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(cal);
        assertEquals("2002-02-23T09:11:12-03:00", text);
        
        text = DurationFormatUtils.formatPeriod(base.getTime().getTime(), cal.getTime().getTime(),
                DurationFormatUtils.ISO_EXTENDED_FORMAT_PATTERN, false, timeZone);
        assertEquals("P32Y1M22DT9H11M12.001S", text);
        
        cal.set(1971, 1, 3, 10, 30, 0);
        cal.set(Calendar.MILLISECOND, 0);
        text = DurationFormatUtils.formatPeriod(base.getTime().getTime(), cal.getTime().getTime(),
                DurationFormatUtils.ISO_EXTENDED_FORMAT_PATTERN, false, timeZone);
        assertEquals("P1Y1M2DT10H30M0.000S", text);
        
        
    }

// org.apache.commons.lang.time.DurationFormatUtilsTest::testFormatPeriod
    public void testFormatPeriod() {
        Calendar cal1970 = Calendar.getInstance();
        cal1970.set(1970, 0, 1, 0, 0, 0);
        cal1970.set(Calendar.MILLISECOND, 0);
        long time1970 = cal1970.getTime().getTime();

        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "y"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "M"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "d"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "H"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "m"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "s"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "S"));
        assertEquals("0000", DurationFormatUtils.formatPeriod(time1970, time1970, "SSSS"));
        assertEquals("0000", DurationFormatUtils.formatPeriod(time1970, time1970, "yyyy"));
        assertEquals("0000", DurationFormatUtils.formatPeriod(time1970, time1970, "yyMM"));

        long time = time1970 + 60 * 1000;
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time, "y"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time, "M"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time, "d"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time, "H"));
        assertEquals("1", DurationFormatUtils.formatPeriod(time1970, time, "m"));
        assertEquals("60", DurationFormatUtils.formatPeriod(time1970, time, "s"));
        assertEquals("60000", DurationFormatUtils.formatPeriod(time1970, time, "S"));
        assertEquals("01:00", DurationFormatUtils.formatPeriod(time1970, time, "mm:ss"));

        Calendar cal = Calendar.getInstance();
        cal.set(1973, 6, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        time = cal.getTime().getTime();
        assertEquals("36", DurationFormatUtils.formatPeriod(time1970, time, "yM"));
        assertEquals("3 years 6 months", DurationFormatUtils.formatPeriod(time1970, time, "y' years 'M' months'"));
        assertEquals("03/06", DurationFormatUtils.formatPeriod(time1970, time, "yy/MM"));

        cal.set(1973, 10, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        time = cal.getTime().getTime();
        assertEquals("310", DurationFormatUtils.formatPeriod(time1970, time, "yM"));
        assertEquals("3 years 10 months", DurationFormatUtils.formatPeriod(time1970, time, "y' years 'M' months'"));
        assertEquals("03/10", DurationFormatUtils.formatPeriod(time1970, time, "yy/MM"));

        cal.set(1974, 0, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        time = cal.getTime().getTime();
        assertEquals("40", DurationFormatUtils.formatPeriod(time1970, time, "yM"));
        assertEquals("4 years 0 months", DurationFormatUtils.formatPeriod(time1970, time, "y' years 'M' months'"));
        assertEquals("04/00", DurationFormatUtils.formatPeriod(time1970, time, "yy/MM"));
        assertEquals("48", DurationFormatUtils.formatPeriod(time1970, time, "M"));
        assertEquals("48", DurationFormatUtils.formatPeriod(time1970, time, "MM"));
        assertEquals("048", DurationFormatUtils.formatPeriod(time1970, time, "MMM"));
    }

// org.apache.commons.lang.time.DurationFormatUtilsTest::testLexx
    public void testLexx() {
        
        assertArrayEquals(new DurationFormatUtils.Token[]{
            new DurationFormatUtils.Token(DurationFormatUtils.y, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.M, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.d, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.H, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.m, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.s, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.S, 1)}, DurationFormatUtils.lexx("yMdHmsS"));

        
        assertArrayEquals(new DurationFormatUtils.Token[]{
            new DurationFormatUtils.Token(DurationFormatUtils.H, 1),
            new DurationFormatUtils.Token(new StringBuffer(":"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.m, 2),
            new DurationFormatUtils.Token(new StringBuffer(":"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.s, 2),
            new DurationFormatUtils.Token(new StringBuffer("."), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.S, 3)}, DurationFormatUtils.lexx("H:mm:ss.SSS"));

        
        assertArrayEquals(new DurationFormatUtils.Token[]{
            new DurationFormatUtils.Token(new StringBuffer("P"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.y, 4),
            new DurationFormatUtils.Token(new StringBuffer("Y"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.M, 1),
            new DurationFormatUtils.Token(new StringBuffer("M"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.d, 1),
            new DurationFormatUtils.Token(new StringBuffer("DT"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.H, 1),
            new DurationFormatUtils.Token(new StringBuffer("H"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.m, 1),
            new DurationFormatUtils.Token(new StringBuffer("M"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.s, 1),
            new DurationFormatUtils.Token(new StringBuffer("."), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.S, 1),
            new DurationFormatUtils.Token(new StringBuffer("S"), 1)}, DurationFormatUtils
                .lexx(DurationFormatUtils.ISO_EXTENDED_FORMAT_PATTERN));

        
        DurationFormatUtils.Token token = new DurationFormatUtils.Token(DurationFormatUtils.y, 4);
        assertFalse("Token equal to non-Token class. ", token.equals(new Object()));
        assertFalse("Token equal to Token with wrong value class. ", token.equals(new DurationFormatUtils.Token(
                new Object())));
        assertFalse("Token equal to Token with different count. ", token.equals(new DurationFormatUtils.Token(
                DurationFormatUtils.y, 1)));
        DurationFormatUtils.Token numToken = new DurationFormatUtils.Token(new Integer(1), 4);
        assertTrue("Token with Number value not equal to itself. ", numToken.equals(numToken));
    }

// org.apache.commons.lang.time.DurationFormatUtilsTest::testBugzilla38401
    public void testBugzilla38401() {}

// org.apache.commons.lang.time.DurationFormatUtilsTest::testJiraLang281
    public void testJiraLang281() {
        assertEqualDuration( "09", new int[] { 2005, 11, 31, 0, 0, 0 }, 
                             new int[] { 2006, 9, 6, 0, 0, 0 }, "MM");
    }

// org.apache.commons.lang.time.DurationFormatUtilsTest::testLowDurations
    public void testLowDurations() {
        for(int hr=0; hr < 24; hr++) {
            for(int min=0; min < 60; min++) {
                for(int sec=0; sec < 60; sec++) {
                    assertEqualDuration( hr + ":" + min + ":" + sec, 
                                         new int[] { 2000, 0, 1, 0, 0, 0, 0 },
                                         new int[] { 2000, 0, 1, hr, min, sec },
                                         "H:m:s"
                                       );
                }
            }
        }
    }

// org.apache.commons.lang.time.DurationFormatUtilsTest::testEdgeDurations
    public void testEdgeDurations() {
        assertEqualDuration( "01", new int[] { 2006, 0, 15, 0, 0, 0 }, 
                             new int[] { 2006, 2, 10, 0, 0, 0 }, "MM");
        assertEqualDuration( "12", new int[] { 2005, 0, 15, 0, 0, 0 }, 
                             new int[] { 2006, 0, 15, 0, 0, 0 }, "MM");
        assertEqualDuration( "12", new int[] { 2005, 0, 15, 0, 0, 0 }, 
                             new int[] { 2006, 0, 16, 0, 0, 0 }, "MM");
        assertEqualDuration( "11", new int[] { 2005, 0, 15, 0, 0, 0 }, 
                             new int[] { 2006, 0, 14, 0, 0, 0 }, "MM");
        
        assertEqualDuration( "01 26", new int[] { 2006, 0, 15, 0, 0, 0 },
                             new int[] { 2006, 2, 10, 0, 0, 0 }, "MM dd");
        assertEqualDuration( "54", new int[] { 2006, 0, 15, 0, 0, 0 },
                             new int[] { 2006, 2, 10, 0, 0, 0 }, "dd"); 
        
        assertEqualDuration( "09 12", new int[] { 2006, 1, 20, 0, 0, 0 },
                             new int[] { 2006, 11, 4, 0, 0, 0 }, "MM dd");
        assertEqualDuration( "287", new int[] { 2006, 1, 20, 0, 0, 0 },
                             new int[] { 2006, 11, 4, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "11 30", new int[] { 2006, 0, 2, 0, 0, 0 },
                             new int[] { 2007, 0, 1, 0, 0, 0 }, "MM dd"); 
        assertEqualDuration( "364", new int[] { 2006, 0, 2, 0, 0, 0 },
                             new int[] { 2007, 0, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "12 00", new int[] { 2006, 0, 1, 0, 0, 0 },
                             new int[] { 2007, 0, 1, 0, 0, 0 }, "MM dd"); 
        assertEqualDuration( "365", new int[] { 2006, 0, 1, 0, 0, 0 },
                             new int[] { 2007, 0, 1, 0, 0, 0 }, "dd"); 
    
        assertEqualDuration( "31", new int[] { 2006, 0, 1, 0, 0, 0 },
                new int[] { 2006, 1, 1, 0, 0, 0 }, "dd"); 
        
        assertEqualDuration( "92", new int[] { 2005, 9, 1, 0, 0, 0 },
                new int[] { 2006, 0, 1, 0, 0, 0 }, "dd"); 
        assertEqualDuration( "77", new int[] { 2005, 9, 16, 0, 0, 0 },
                new int[] { 2006, 0, 1, 0, 0, 0 }, "dd"); 

        
        assertEqualDuration( "136", new int[] { 2005, 9, 16, 0, 0, 0 },
                new int[] { 2006, 2, 1, 0, 0, 0 }, "dd"); 
        
        assertEqualDuration( "136", new int[] { 2004, 9, 16, 0, 0, 0 },
                new int[] { 2005, 2, 1, 0, 0, 0 }, "dd"); 
        
        assertEqualDuration( "137", new int[] { 2003, 9, 16, 0, 0, 0 },
                new int[] { 2004, 2, 1, 0, 0, 0 }, "dd");         
        
        assertEqualDuration( "135", new int[] { 2003, 9, 16, 0, 0, 0 },
                new int[] { 2004, 1, 28, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "364", new int[] { 2007, 0, 2, 0, 0, 0 },
                new int[] { 2008, 0, 1, 0, 0, 0 }, "dd"); 
        assertEqualDuration( "729", new int[] { 2006, 0, 2, 0, 0, 0 },
                new int[] { 2008, 0, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "365", new int[] { 2007, 2, 2, 0, 0, 0 },
                new int[] { 2008, 2, 1, 0, 0, 0 }, "dd"); 
        assertEqualDuration( "333", new int[] { 2007, 1, 2, 0, 0, 0 },
                new int[] { 2008, 0, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "28", new int[] { 2008, 1, 2, 0, 0, 0 },
                new int[] { 2008, 2, 1, 0, 0, 0 }, "dd"); 
        assertEqualDuration( "393", new int[] { 2007, 1, 2, 0, 0, 0 },
                new int[] { 2008, 2, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "369", new int[] { 2004, 0, 29, 0, 0, 0 },
                new int[] { 2005, 1, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "338", new int[] { 2004, 1, 29, 0, 0, 0 },
                new int[] { 2005, 1, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "28", new int[] { 2004, 2, 8, 0, 0, 0 },
                new int[] { 2004, 3, 5, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "48", new int[] { 1992, 1, 29, 0, 0, 0 },
                new int[] { 1996, 1, 29, 0, 0, 0 }, "M"); 
        
        
        
        
        assertEqualDuration( "11", new int[] { 1996, 1, 29, 0, 0, 0 },
                new int[] { 1997, 1, 28, 0, 0, 0 }, "M"); 
        
        assertEqualDuration( "11 28", new int[] { 1996, 1, 29, 0, 0, 0 },
                new int[] { 1997, 1, 28, 0, 0, 0 }, "M d"); 
        
    }

// org.apache.commons.lang.time.DurationFormatUtilsTest::testDurationsByBruteForce
    public void testDurationsByBruteForce() {
        bruteForce(2006, 0, 1, "d", Calendar.DAY_OF_MONTH);
        bruteForce(2006, 0, 2, "d", Calendar.DAY_OF_MONTH);
        bruteForce(2007, 1, 2, "d", Calendar.DAY_OF_MONTH);
        bruteForce(2004, 1, 29, "d", Calendar.DAY_OF_MONTH);
        bruteForce(1996, 1, 29, "d", Calendar.DAY_OF_MONTH);

        bruteForce(1969, 1, 28, "M", Calendar.MONTH);  
        
    }

// org.apache.commons.lang.time.FastDateFormatTest::test_getInstance
    public void test_getInstance() {
        FastDateFormat format1 = FastDateFormat.getInstance();
        FastDateFormat format2 = FastDateFormat.getInstance();
        assertSame(format1, format2);
        assertEquals(new SimpleDateFormat().toPattern(), format1.getPattern());
    }

// org.apache.commons.lang.time.FastDateFormatTest::test_getInstance_String
    public void test_getInstance_String() {
        FastDateFormat format1 = FastDateFormat.getInstance("MM/DD/yyyy");
        FastDateFormat format2 = FastDateFormat.getInstance("MM-DD-yyyy");
        FastDateFormat format3 = FastDateFormat.getInstance("MM-DD-yyyy");

        assertTrue(format1 != format2); 
        assertSame(format2, format3);
        assertEquals("MM/DD/yyyy", format1.getPattern());
        assertEquals(TimeZone.getDefault(), format1.getTimeZone());
        assertEquals(TimeZone.getDefault(), format2.getTimeZone());
        assertEquals(false, format1.getTimeZoneOverridesCalendar());
        assertEquals(false, format2.getTimeZoneOverridesCalendar());
    }

// org.apache.commons.lang.time.FastDateFormatTest::test_getInstance_String_TimeZone
    public void test_getInstance_String_TimeZone() {
        Locale realDefaultLocale = Locale.getDefault();
        TimeZone realDefaultZone = TimeZone.getDefault();
        try {
            Locale.setDefault(Locale.US);
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));

            FastDateFormat format1 = FastDateFormat.getInstance("MM/DD/yyyy",
                    TimeZone.getTimeZone("Atlantic/Reykjavik"));
            FastDateFormat format2 = FastDateFormat.getInstance("MM/DD/yyyy");
            FastDateFormat format3 = FastDateFormat.getInstance("MM/DD/yyyy", TimeZone.getDefault());
            FastDateFormat format4 = FastDateFormat.getInstance("MM/DD/yyyy", TimeZone.getDefault());
            FastDateFormat format5 = FastDateFormat.getInstance("MM-DD-yyyy", TimeZone.getDefault());
            FastDateFormat format6 = FastDateFormat.getInstance("MM-DD-yyyy");

            assertTrue(format1 != format2); 
            assertEquals(TimeZone.getTimeZone("Atlantic/Reykjavik"), format1.getTimeZone());
            assertEquals(true, format1.getTimeZoneOverridesCalendar());
            assertEquals(TimeZone.getDefault(), format2.getTimeZone());
            assertEquals(false, format2.getTimeZoneOverridesCalendar());
            assertSame(format3, format4);
            assertTrue(format3 != format5); 
            assertTrue(format4 != format6); 

        } finally {
            Locale.setDefault(realDefaultLocale);
            TimeZone.setDefault(realDefaultZone);
        }
    }

// org.apache.commons.lang.time.FastDateFormatTest::test_getInstance_String_Locale
    public void test_getInstance_String_Locale() {
        Locale realDefaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            FastDateFormat format1 = FastDateFormat.getInstance("MM/DD/yyyy", Locale.GERMANY);
            FastDateFormat format2 = FastDateFormat.getInstance("MM/DD/yyyy");
            FastDateFormat format3 = FastDateFormat.getInstance("MM/DD/yyyy", Locale.GERMANY);

            assertTrue(format1 != format2); 
            assertSame(format1, format3);
            assertSame(Locale.GERMANY, format1.getLocale());

        } finally {
            Locale.setDefault(realDefaultLocale);
        }
    }

// org.apache.commons.lang.time.FastDateFormatTest::test_changeDefault_Locale_DateInstance
    public void test_changeDefault_Locale_DateInstance() {
        Locale realDefaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            FastDateFormat format1 = FastDateFormat.getDateInstance(FastDateFormat.FULL, Locale.GERMANY);
            FastDateFormat format2 = FastDateFormat.getDateInstance(FastDateFormat.FULL);
            Locale.setDefault(Locale.GERMANY);
            FastDateFormat format3 = FastDateFormat.getDateInstance(FastDateFormat.FULL);

            assertSame(Locale.GERMANY, format1.getLocale());
            assertSame(Locale.US, format2.getLocale());
            assertSame(Locale.GERMANY, format3.getLocale());
            assertTrue(format1 != format2); 
            assertTrue(format2 != format3);

        } finally {
            Locale.setDefault(realDefaultLocale);
        }
    }

// org.apache.commons.lang.time.FastDateFormatTest::test_changeDefault_Locale_DateTimeInstance
    public void test_changeDefault_Locale_DateTimeInstance() {
        Locale realDefaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            FastDateFormat format1 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, Locale.GERMANY);
            FastDateFormat format2 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL);
            Locale.setDefault(Locale.GERMANY);
            FastDateFormat format3 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL);

            assertSame(Locale.GERMANY, format1.getLocale());
            assertSame(Locale.US, format2.getLocale());
            assertSame(Locale.GERMANY, format3.getLocale());
            assertTrue(format1 != format2); 
            assertTrue(format2 != format3);

        } finally {
            Locale.setDefault(realDefaultLocale);
        }
    }

// org.apache.commons.lang.time.FastDateFormatTest::test_getInstance_String_TimeZone_Locale
    public void test_getInstance_String_TimeZone_Locale() {
        Locale realDefaultLocale = Locale.getDefault();
        TimeZone realDefaultZone = TimeZone.getDefault();
        try {
            Locale.setDefault(Locale.US);
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));

            FastDateFormat format1 = FastDateFormat.getInstance("MM/DD/yyyy",
                    TimeZone.getTimeZone("Atlantic/Reykjavik"), Locale.GERMANY);
            FastDateFormat format2 = FastDateFormat.getInstance("MM/DD/yyyy", Locale.GERMANY);
            FastDateFormat format3 = FastDateFormat.getInstance("MM/DD/yyyy",
                    TimeZone.getDefault(), Locale.GERMANY);

            assertTrue(format1 != format2); 
            assertEquals(TimeZone.getTimeZone("Atlantic/Reykjavik"), format1.getTimeZone());
            assertEquals(TimeZone.getDefault(), format2.getTimeZone());
            assertEquals(TimeZone.getDefault(), format3.getTimeZone());
            assertEquals(true, format1.getTimeZoneOverridesCalendar());
            assertEquals(false, format2.getTimeZoneOverridesCalendar());
            assertEquals(true, format3.getTimeZoneOverridesCalendar());
            assertEquals(Locale.GERMANY, format1.getLocale());
            assertEquals(Locale.GERMANY, format2.getLocale());
            assertEquals(Locale.GERMANY, format3.getLocale());

        } finally {
            Locale.setDefault(realDefaultLocale);
            TimeZone.setDefault(realDefaultZone);
        }
    }

// org.apache.commons.lang.time.FastDateFormatTest::testFormat
    public void testFormat() {}

// org.apache.commons.lang.time.FastDateFormatTest::testShortDateStyleWithLocales
    public void testShortDateStyleWithLocales() {
        Locale usLocale = Locale.US;
        Locale swedishLocale = new Locale("sv", "SE");
        Calendar cal = Calendar.getInstance();
        cal.set(2004, 1, 3);
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.SHORT, usLocale);
        assertEquals("2/3/04", fdf.format(cal));

        fdf = FastDateFormat.getDateInstance(FastDateFormat.SHORT, swedishLocale);
        assertEquals("04-02-03", fdf.format(cal));

    }

// org.apache.commons.lang.time.FastDateFormatTest::testLowYearPadding
    public void testLowYearPadding() {
        Calendar cal = Calendar.getInstance();
        FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/DD");

        cal.set(1,0,1);
        assertEquals("0001/01/01", format.format(cal));
        cal.set(10,0,1);
        assertEquals("0010/01/01", format.format(cal));
        cal.set(100,0,1);
        assertEquals("0100/01/01", format.format(cal));
        cal.set(999,0,1);
        assertEquals("0999/01/01", format.format(cal));
    }

// org.apache.commons.lang.time.FastDateFormatTest::testMilleniumBug
    public void testMilleniumBug() {
        Calendar cal = Calendar.getInstance();
        FastDateFormat format = FastDateFormat.getInstance("dd.MM.yyyy");

        cal.set(1000,0,1);
        assertEquals("01.01.1000", format.format(cal));
    }

// org.apache.commons.lang.time.FastDateFormatTest::testSimpleDate
    public void testSimpleDate() {
        Calendar cal = Calendar.getInstance();
        FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/dd");

        cal.set(2004,11,31);
        assertEquals("2004/12/31", format.format(cal));
        cal.set(999,11,31);
        assertEquals("0999/12/31", format.format(cal));
        cal.set(1,2,2);
        assertEquals("0001/03/02", format.format(cal));
    }

// org.apache.commons.lang.time.FastDateFormatTest::testLang303
    public void testLang303() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004,11,31);

        FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/dd");
        String output = format.format(cal);

        format = (FastDateFormat) SerializationUtils.deserialize( SerializationUtils.serialize( format ) );
        assertEquals(output, format.format(cal));
    }
