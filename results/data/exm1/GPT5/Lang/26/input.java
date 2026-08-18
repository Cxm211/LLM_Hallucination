// buggy code
    public String format(Date date) {
        Calendar c = new GregorianCalendar(mTimeZone);
        c.setTime(date);
        return applyRules(c, new StringBuffer(mMaxLengthEstimate)).toString();
    }

// relevant test
// org.apache.commons.lang3.time.DateFormatUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new DateFormatUtils());
        Constructor<?>[] cons = DateFormatUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(DateFormatUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(DateFormatUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.time.DateFormatUtilsTest::testFormat
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

// org.apache.commons.lang3.time.DateFormatUtilsTest::testFormatCalendar
    public void testFormatCalendar() {
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
        assertEquals(buffer.toString(), DateFormatUtils.format(c, "yyyyMdH"));
        
        assertEquals(buffer.toString(), DateFormatUtils.format(c.getTime(), "yyyyMdH"));
        
        assertEquals(buffer.toString(), DateFormatUtils.format(c, "yyyyMdH", Locale.US));
        
        assertEquals(buffer.toString(), DateFormatUtils.format(c.getTime(), "yyyyMdH", Locale.US));
    }

// org.apache.commons.lang3.time.DateFormatUtilsTest::testFormatUTC
    public void testFormatUTC() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(2005,0,1,12,0,0);
        assertEquals ("2005-01-01T12:00:00", DateFormatUtils.formatUTC(c.getTime(), DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()));
        
        assertEquals ("2005-01-01T12:00:00", DateFormatUtils.formatUTC(c.getTime().getTime(), DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()));
        
        assertEquals ("2005-01-01T12:00:00", DateFormatUtils.formatUTC(c.getTime(), DateFormatUtils.ISO_DATETIME_FORMAT.getPattern(), Locale.US));
        
        assertEquals ("2005-01-01T12:00:00", DateFormatUtils.formatUTC(c.getTime().getTime(), DateFormatUtils.ISO_DATETIME_FORMAT.getPattern(), Locale.US));
    }

// org.apache.commons.lang3.time.DateFormatUtilsTest::testDateTimeISO
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

// org.apache.commons.lang3.time.DateFormatUtilsTest::testDateISO
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

// org.apache.commons.lang3.time.DateFormatUtilsTest::testTimeISO
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

// org.apache.commons.lang3.time.DateFormatUtilsTest::testTimeNoTISO
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

// org.apache.commons.lang3.time.DateFormatUtilsTest::testSMTP
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

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testRoundYear
    public void testRoundYear() throws Exception {
        final int calendarField = Calendar.YEAR;
        Date roundedUpDate = dateTimeParser.parse("January 1, 2008 0:00:00.000");
        Date roundedDownDate = targetYearDate;
        Date lastRoundedDownDate = dateTimeParser.parse("June 30, 2007 23:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testRoundMonth
    public void testRoundMonth() throws Exception {
        final int calendarField = Calendar.MONTH;
        Date roundedUpDate, roundedDownDate, lastRoundedDownDate;
        Date minDate, maxDate;
        
        
        roundedUpDate = dateTimeParser.parse("March 1, 2007 0:00:00.000");
        roundedDownDate = dateTimeParser.parse("February 1, 2007 0:00:00.000");
        lastRoundedDownDate = dateTimeParser.parse("February 14, 2007 23:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);

        
        roundedUpDate = dateTimeParser.parse("March 1, 2008 0:00:00.000");
        roundedDownDate = dateTimeParser.parse("February 1, 2008 0:00:00.000");
        lastRoundedDownDate = dateTimeParser.parse("February 15, 2008 23:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
        
        
        roundedUpDate = dateTimeParser.parse("May 1, 2008 0:00:00.000");
        roundedDownDate = dateTimeParser.parse("April 1, 2008 0:00:00.000");
        lastRoundedDownDate = dateTimeParser.parse("April 15, 2008 23:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
        
        
        roundedUpDate = dateTimeParser.parse("June 1, 2008 0:00:00.000");
        roundedDownDate = dateTimeParser.parse("May 1, 2008 0:00:00.000");
        lastRoundedDownDate = dateTimeParser.parse("May 16, 2008 23:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
        
        
        minDate = dateTimeParser.parse("December 17, 2007 00:00:00.000");
        maxDate = dateTimeParser.parse("January 16, 2008 23:59:59.999");
        roundToJanuaryFirst(minDate, maxDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testRoundSemiMonth
    public void testRoundSemiMonth() throws Exception {
        final int calendarField = DateUtils.SEMI_MONTH;
        Date roundedUpDate, roundedDownDate, lastRoundedDownDate;
        Date minDate, maxDate;
        
        
        roundedUpDate = dateTimeParser.parse("February 16, 2007 0:00:00.000");
        roundedDownDate = dateTimeParser.parse("February 1, 2007 0:00:00.000");
        lastRoundedDownDate = dateTimeParser.parse("February 8, 2007 23:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);

        
        roundedUpDate = dateTimeParser.parse("March 1, 2007 0:00:00.000");
        roundedDownDate = dateTimeParser.parse("February 16, 2007 0:00:00.000");
        lastRoundedDownDate = dateTimeParser.parse("February 23, 2007 23:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);

        
        roundedUpDate = dateTimeParser.parse("February 16, 2008 0:00:00.000");
        roundedDownDate = dateTimeParser.parse("February 1, 2008 0:00:00.000");
        lastRoundedDownDate = dateTimeParser.parse("February 8, 2008 23:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
        
        
        roundedUpDate = dateTimeParser.parse("March 1, 2008 0:00:00.000");
        roundedDownDate = dateTimeParser.parse("February 16, 2008 0:00:00.000");
        lastRoundedDownDate = dateTimeParser.parse("February 23, 2008 23:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);

        
        roundedUpDate = dateTimeParser.parse("April 16, 2008 0:00:00.000");
        roundedDownDate = dateTimeParser.parse("April 1, 2008 0:00:00.000");
        lastRoundedDownDate = dateTimeParser.parse("April 8, 2008 23:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);

        
        roundedUpDate = dateTimeParser.parse("May 1, 2008 0:00:00.000");
        roundedDownDate = dateTimeParser.parse("April 16, 2008 0:00:00.000");
        lastRoundedDownDate = dateTimeParser.parse("April 23, 2008 23:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
        
        
        roundedUpDate = dateTimeParser.parse("May 16, 2008 0:00:00.000");
        roundedDownDate = dateTimeParser.parse("May 1, 2008 0:00:00.000");
        lastRoundedDownDate = dateTimeParser.parse("May 8, 2008 23:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);

        
        roundedUpDate = dateTimeParser.parse("June 1, 2008 0:00:00.000");
        roundedDownDate = dateTimeParser.parse("May 16, 2008 0:00:00.000");
        lastRoundedDownDate = dateTimeParser.parse("May 23, 2008 23:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
        
        
        minDate = dateTimeParser.parse("December 24, 2007 00:00:00.000");
        maxDate = dateTimeParser.parse("January 8, 2008 23:59:59.999");
        roundToJanuaryFirst(minDate, maxDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testRoundDate
    public void testRoundDate() throws Exception {
        final int calendarField = Calendar.DATE;
        Date roundedUpDate, roundedDownDate, lastRoundedDownDate;
        Date minDate, maxDate;

        roundedUpDate = dateTimeParser.parse("June 2, 2008 0:00:00.000");
        roundedDownDate = targetDateDate;
        lastRoundedDownDate = dateTimeParser.parse("June 1, 2008 11:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
        
        
        minDate = dateTimeParser.parse("December 31, 2007 12:00:00.000");
        maxDate = dateTimeParser.parse("January 1, 2008 11:59:59.999");
        roundToJanuaryFirst(minDate, maxDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testRoundDayOfMonth
    public void testRoundDayOfMonth() throws Exception {
        final int calendarField = Calendar.DAY_OF_MONTH;
        Date roundedUpDate, roundedDownDate, lastRoundedDownDate;
        Date minDate, maxDate;

        roundedUpDate = dateTimeParser.parse("June 2, 2008 0:00:00.000");
        roundedDownDate = targetDayOfMonthDate;
        lastRoundedDownDate = dateTimeParser.parse("June 1, 2008 11:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
        
        
        minDate = dateTimeParser.parse("December 31, 2007 12:00:00.000");
        maxDate = dateTimeParser.parse("January 1, 2008 11:59:59.999");
        roundToJanuaryFirst(minDate, maxDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testRoundAmPm
    public void testRoundAmPm() throws Exception {
        final int calendarField = Calendar.AM_PM;
        Date roundedUpDate, roundedDownDate, lastRoundedDownDate;
        Date minDate, maxDate;

        
        roundedUpDate = dateTimeParser.parse("June 1, 2008 12:00:00.000");
        roundedDownDate = targetAmDate;
        lastRoundedDownDate = dateTimeParser.parse("June 1, 2008 5:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);

        
        roundedUpDate = dateTimeParser.parse("June 2, 2008 0:00:00.000");
        roundedDownDate = targetPmDate;
        lastRoundedDownDate = dateTimeParser.parse("June 1, 2008 17:59:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);

        
        minDate = dateTimeParser.parse("December 31, 2007 18:00:00.000");
        maxDate = dateTimeParser.parse("January 1, 2008 5:59:59.999");
        roundToJanuaryFirst(minDate, maxDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testRoundHourOfDay
    public void testRoundHourOfDay() throws Exception {
        final int calendarField = Calendar.HOUR_OF_DAY;
        Date roundedUpDate, roundedDownDate, lastRoundedDownDate;
        Date minDate, maxDate;

        roundedUpDate = dateTimeParser.parse("June 1, 2008 9:00:00.000");
        roundedDownDate = targetHourOfDayDate;
        lastRoundedDownDate = dateTimeParser.parse("June 1, 2008 8:29:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
        
        
        minDate = dateTimeParser.parse("December 31, 2007 23:30:00.000");
        maxDate = dateTimeParser.parse("January 1, 2008 0:29:59.999");
        roundToJanuaryFirst(minDate, maxDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testRoundHour
    public void testRoundHour() throws Exception {
        final int calendarField = Calendar.HOUR;
        Date roundedUpDate, roundedDownDate, lastRoundedDownDate;
        Date minDate, maxDate;

        roundedUpDate = dateTimeParser.parse("June 1, 2008 9:00:00.000");
        roundedDownDate = targetHourDate;
        lastRoundedDownDate = dateTimeParser.parse("June 1, 2008 8:29:59.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
        
        
        minDate = dateTimeParser.parse("December 31, 2007 23:30:00.000");
        maxDate = dateTimeParser.parse("January 1, 2008 0:29:59.999");
        roundToJanuaryFirst(minDate, maxDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testRoundMinute
    public void testRoundMinute() throws Exception {
        final int calendarField = Calendar.MINUTE;
        Date roundedUpDate, roundedDownDate, lastRoundedDownDate;
        Date minDate, maxDate;

        roundedUpDate = dateTimeParser.parse("June 1, 2008 8:16:00.000");
        roundedDownDate = targetMinuteDate;
        lastRoundedDownDate = dateTimeParser.parse("June 1, 2008 8:15:29.999");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
        
        
        minDate = dateTimeParser.parse("December 31, 2007 23:59:30.000");
        maxDate = dateTimeParser.parse("January 1, 2008 0:00:29.999");
        roundToJanuaryFirst(minDate, maxDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testRoundSecond
    public void testRoundSecond() throws Exception {
        final int calendarField = Calendar.SECOND;
        Date roundedUpDate, roundedDownDate, lastRoundedDownDate;
        Date minDate, maxDate;

        roundedUpDate = dateTimeParser.parse("June 1, 2008 8:15:15.000");
        roundedDownDate = targetSecondDate;
        lastRoundedDownDate = dateTimeParser.parse("June 1, 2008 8:15:14.499");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
        
        
        minDate = dateTimeParser.parse("December 31, 2007 23:59:59.500");
        maxDate = dateTimeParser.parse("January 1, 2008 0:00:00.499");
        roundToJanuaryFirst(minDate, maxDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testRoundMilliSecond
    public void testRoundMilliSecond() throws Exception {
        final int calendarField = Calendar.MILLISECOND;
        Date roundedUpDate, roundedDownDate, lastRoundedDownDate;
        Date minDate, maxDate;

        roundedDownDate = lastRoundedDownDate = targetMilliSecondDate;
        roundedUpDate = dateTimeParser.parse("June 1, 2008 8:15:14.232");
        baseRoundTest(roundedUpDate, roundedDownDate, lastRoundedDownDate,  calendarField);
        
        
        minDate = maxDate = januaryOneDate;
        roundToJanuaryFirst(minDate, maxDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testTruncateYear
    public void testTruncateYear() throws Exception {
        final int calendarField = Calendar.YEAR;
        Date lastTruncateDate = dateTimeParser.parse("December 31, 2007 23:59:59.999");
        baseTruncateTest(targetYearDate, lastTruncateDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testTruncateMonth
    public void testTruncateMonth() throws Exception {
        final int calendarField = Calendar.MONTH;
        Date truncatedDate = dateTimeParser.parse("March 1, 2008 0:00:00.000");
        Date lastTruncateDate = dateTimeParser.parse("March 31, 2008 23:59:59.999");
        baseTruncateTest(truncatedDate, lastTruncateDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testTruncateSemiMonth
    public void testTruncateSemiMonth() throws Exception {
        final int calendarField = DateUtils.SEMI_MONTH;
        Date truncatedDate, lastTruncateDate;
        
        
        truncatedDate = dateTimeParser.parse("February 1, 2007 0:00:00.000");
        lastTruncateDate = dateTimeParser.parse("February 15, 2007 23:59:59.999");
        baseTruncateTest(truncatedDate, lastTruncateDate, calendarField);

        
        truncatedDate = dateTimeParser.parse("February 16, 2007 0:00:00.000");
        lastTruncateDate = dateTimeParser.parse("February 28, 2007 23:59:59.999");
        baseTruncateTest(truncatedDate, lastTruncateDate, calendarField);

        
        truncatedDate = dateTimeParser.parse("February 1, 2008 0:00:00.000");
        lastTruncateDate = dateTimeParser.parse("February 15, 2008 23:59:59.999");
        baseTruncateTest(truncatedDate, lastTruncateDate, calendarField);

        
        truncatedDate = dateTimeParser.parse("February 16, 2008 0:00:00.000");
        lastTruncateDate = dateTimeParser.parse("February 29, 2008 23:59:59.999");
        baseTruncateTest(truncatedDate, lastTruncateDate, calendarField);

        
        truncatedDate = dateTimeParser.parse("April 1, 2008 0:00:00.000");
        lastTruncateDate = dateTimeParser.parse("April 15, 2008 23:59:59.999");
        baseTruncateTest(truncatedDate, lastTruncateDate, calendarField);

        
        truncatedDate = dateTimeParser.parse("April 16, 2008 0:00:00.000");
        lastTruncateDate = dateTimeParser.parse("April 30, 2008 23:59:59.999");
        baseTruncateTest(truncatedDate, lastTruncateDate, calendarField);
        
        
        truncatedDate = dateTimeParser.parse("March 1, 2008 0:00:00.000");
        lastTruncateDate = dateTimeParser.parse("March 15, 2008 23:59:59.999");
        baseTruncateTest(truncatedDate, lastTruncateDate, calendarField);

        
        truncatedDate = dateTimeParser.parse("March 16, 2008 0:00:00.000");
        lastTruncateDate = dateTimeParser.parse("March 31, 2008 23:59:59.999");
        baseTruncateTest(truncatedDate, lastTruncateDate, calendarField);

    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testTruncateDate
    public void testTruncateDate() throws Exception {
        final int calendarField = Calendar.DATE;
        Date lastTruncateDate = dateTimeParser.parse("June 1, 2008 23:59:59.999");
        baseTruncateTest(targetDateDate, lastTruncateDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testTruncateDayOfMonth
    public void testTruncateDayOfMonth() throws Exception {
        final int calendarField = Calendar.DAY_OF_MONTH;
        Date lastTruncateDate = dateTimeParser.parse("June 1, 2008 23:59:59.999");
        baseTruncateTest(targetDayOfMonthDate, lastTruncateDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testTruncateAmPm
    public void testTruncateAmPm() throws Exception {
        final int calendarField = Calendar.AM_PM;
        
        
        Date lastTruncateDate = dateTimeParser.parse("June 1, 2008 11:59:59.999");
        baseTruncateTest(targetAmDate, lastTruncateDate, calendarField);

        
        lastTruncateDate = dateTimeParser.parse("June 1, 2008 23:59:59.999");
        baseTruncateTest(targetPmDate, lastTruncateDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testTruncateHour
    public void testTruncateHour() throws Exception {
        final int calendarField = Calendar.HOUR;
        Date lastTruncateDate = dateTimeParser.parse("June 1, 2008 8:59:59.999");
        baseTruncateTest(targetHourDate, lastTruncateDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testTruncateHourOfDay
    public void testTruncateHourOfDay() throws Exception {
        final int calendarField = Calendar.HOUR_OF_DAY;
        Date lastTruncateDate = dateTimeParser.parse("June 1, 2008 8:59:59.999");
        baseTruncateTest(targetHourOfDayDate, lastTruncateDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testTruncateMinute
    public void testTruncateMinute() throws Exception {
        final int calendarField = Calendar.MINUTE;
        Date lastTruncateDate = dateTimeParser.parse("June 1, 2008 8:15:59.999");
        baseTruncateTest(targetMinuteDate, lastTruncateDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testTruncateSecond
    public void testTruncateSecond() throws Exception {
        final int calendarField = Calendar.SECOND;
        Date lastTruncateDate = dateTimeParser.parse("June 1, 2008 8:15:14.999");
        baseTruncateTest(targetSecondDate, lastTruncateDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsRoundingTest::testTruncateMilliSecond
    public void testTruncateMilliSecond() throws Exception {
        final int calendarField = Calendar.MILLISECOND;
        baseTruncateTest(targetMilliSecondDate, targetMilliSecondDate, calendarField);
    }

// org.apache.commons.lang3.time.DateUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new DateUtils());
        Constructor<?>[] cons = DateUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(DateUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(DateUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.time.DateUtilsTest::testIsSameDay_Date
    public void testIsSameDay_Date() {
        Date date1 = new GregorianCalendar(2004, 6, 9, 13, 45).getTime();
        Date date2 = new GregorianCalendar(2004, 6, 9, 13, 45).getTime();
        assertEquals(true, DateUtils.isSameDay(date1, date2));
        date2 = new GregorianCalendar(2004, 6, 10, 13, 45).getTime();
        assertEquals(false, DateUtils.isSameDay(date1, date2));
        date1 = new GregorianCalendar(2004, 6, 10, 13, 45).getTime();
        assertEquals(true, DateUtils.isSameDay(date1, date2));
        date2 = new GregorianCalendar(2005, 6, 10, 13, 45).getTime();
        assertEquals(false, DateUtils.isSameDay(date1, date2));
        try {
            DateUtils.isSameDay((Date) null, (Date) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.time.DateUtilsTest::testIsSameDay_Cal
    public void testIsSameDay_Cal() {
        GregorianCalendar cal1 = new GregorianCalendar(2004, 6, 9, 13, 45);
        GregorianCalendar cal2 = new GregorianCalendar(2004, 6, 9, 13, 45);
        assertEquals(true, DateUtils.isSameDay(cal1, cal2));
        cal2.add(Calendar.DAY_OF_YEAR, 1);
        assertEquals(false, DateUtils.isSameDay(cal1, cal2));
        cal1.add(Calendar.DAY_OF_YEAR, 1);
        assertEquals(true, DateUtils.isSameDay(cal1, cal2));
        cal2.add(Calendar.YEAR, 1);
        assertEquals(false, DateUtils.isSameDay(cal1, cal2));
        try {
            DateUtils.isSameDay((Calendar) null, (Calendar) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.time.DateUtilsTest::testIsSameInstant_Date
    public void testIsSameInstant_Date() {
        Date date1 = new GregorianCalendar(2004, 6, 9, 13, 45).getTime();
        Date date2 = new GregorianCalendar(2004, 6, 9, 13, 45).getTime();
        assertEquals(true, DateUtils.isSameInstant(date1, date2));
        date2 = new GregorianCalendar(2004, 6, 10, 13, 45).getTime();
        assertEquals(false, DateUtils.isSameInstant(date1, date2));
        date1 = new GregorianCalendar(2004, 6, 10, 13, 45).getTime();
        assertEquals(true, DateUtils.isSameInstant(date1, date2));
        date2 = new GregorianCalendar(2005, 6, 10, 13, 45).getTime();
        assertEquals(false, DateUtils.isSameInstant(date1, date2));
        try {
            DateUtils.isSameInstant((Date) null, (Date) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.time.DateUtilsTest::testIsSameInstant_Cal
    public void testIsSameInstant_Cal() {
        GregorianCalendar cal1 = new GregorianCalendar(TimeZone.getTimeZone("GMT+1"));
        GregorianCalendar cal2 = new GregorianCalendar(TimeZone.getTimeZone("GMT-1"));
        cal1.set(2004, 6, 9, 13, 45, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(2004, 6, 9, 13, 45, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        assertEquals(false, DateUtils.isSameInstant(cal1, cal2));
        
        cal2.set(2004, 6, 9, 11, 45, 0);
        assertEquals(true, DateUtils.isSameInstant(cal1, cal2));
        try {
            DateUtils.isSameInstant((Calendar) null, (Calendar) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.time.DateUtilsTest::testIsSameLocalTime_Cal
    public void testIsSameLocalTime_Cal() {
        GregorianCalendar cal1 = new GregorianCalendar(TimeZone.getTimeZone("GMT+1"));
        GregorianCalendar cal2 = new GregorianCalendar(TimeZone.getTimeZone("GMT-1"));
        cal1.set(2004, 6, 9, 13, 45, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(2004, 6, 9, 13, 45, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        assertEquals(true, DateUtils.isSameLocalTime(cal1, cal2));
        
        cal2.set(2004, 6, 9, 11, 45, 0);
        assertEquals(false, DateUtils.isSameLocalTime(cal1, cal2));
        try {
            DateUtils.isSameLocalTime((Calendar) null, (Calendar) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.time.DateUtilsTest::testParseDate
    public void testParseDate() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1972, 11, 3);
        String dateStr = "1972-12-03";
        String[] parsers = new String[] {"yyyy'-'DDD", "yyyy'-'MM'-'dd", "yyyyMMdd"};
        Date date = DateUtils.parseDate(dateStr, parsers);
        assertEquals(cal.getTime(), date);
        
        dateStr = "1972-338";
        date = DateUtils.parseDate(dateStr, parsers);
        assertEquals(cal.getTime(), date);
        
        dateStr = "19721203";
        date = DateUtils.parseDate(dateStr, parsers);
        assertEquals(cal.getTime(), date);
        
        try {
            DateUtils.parseDate("PURPLE", parsers);
            fail();
        } catch (ParseException ex) {}
        try {
            DateUtils.parseDate("197212AB", parsers);
            fail();
        } catch (ParseException ex) {}
        try {
            DateUtils.parseDate(null, parsers);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.parseDate(dateStr, (String[]) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.parseDate(dateStr, new String[0]);
            fail();
        } catch (ParseException ex) {}
    }

// org.apache.commons.lang3.time.DateUtilsTest::testParseDateWithLeniency
    public void testParseDateWithLeniency() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1998, 6, 30);
        String dateStr = "02 942, 1996";
        String[] parsers = new String[] {"MM DDD, yyyy"};
        
        Date date = DateUtils.parseDate(dateStr, parsers);
        assertEquals(cal.getTime(), date);
        
        try {
            date = DateUtils.parseDateStrictly(dateStr, parsers);
            fail();
        } catch (ParseException ex) {}
    }

// org.apache.commons.lang3.time.DateUtilsTest::testAddYears
    public void testAddYears() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.addYears(base, 0);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 2, 1);
        
        result = DateUtils.addYears(base, 1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2001, 6, 5, 4, 3, 2, 1);
        
        result = DateUtils.addYears(base, -1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 1999, 6, 5, 4, 3, 2, 1);
    }

// org.apache.commons.lang3.time.DateUtilsTest::testAddMonths
    public void testAddMonths() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.addMonths(base, 0);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 2, 1);
        
        result = DateUtils.addMonths(base, 1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 7, 5, 4, 3, 2, 1);
        
        result = DateUtils.addMonths(base, -1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 5, 5, 4, 3, 2, 1);
    }

// org.apache.commons.lang3.time.DateUtilsTest::testAddWeeks
    public void testAddWeeks() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.addWeeks(base, 0);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 2, 1);
        
        result = DateUtils.addWeeks(base, 1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 12, 4, 3, 2, 1);
        
        result = DateUtils.addWeeks(base, -1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);      
        assertDate(result, 2000, 5, 28, 4, 3, 2, 1);   
    }

// org.apache.commons.lang3.time.DateUtilsTest::testAddDays
    public void testAddDays() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.addDays(base, 0);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 2, 1);
        
        result = DateUtils.addDays(base, 1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 6, 4, 3, 2, 1);
        
        result = DateUtils.addDays(base, -1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 4, 4, 3, 2, 1);
    }

// org.apache.commons.lang3.time.DateUtilsTest::testAddHours
    public void testAddHours() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.addHours(base, 0);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 2, 1);
        
        result = DateUtils.addHours(base, 1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 5, 3, 2, 1);
        
        result = DateUtils.addHours(base, -1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 3, 3, 2, 1);
    }

// org.apache.commons.lang3.time.DateUtilsTest::testAddMinutes
    public void testAddMinutes() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.addMinutes(base, 0);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 2, 1);
        
        result = DateUtils.addMinutes(base, 1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 4, 2, 1);
        
        result = DateUtils.addMinutes(base, -1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 2, 2, 1);
    }

// org.apache.commons.lang3.time.DateUtilsTest::testAddSeconds
    public void testAddSeconds() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.addSeconds(base, 0);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 2, 1);
        
        result = DateUtils.addSeconds(base, 1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 3, 1);
        
        result = DateUtils.addSeconds(base, -1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 1, 1);
    }

// org.apache.commons.lang3.time.DateUtilsTest::testAddMilliseconds
    public void testAddMilliseconds() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.addMilliseconds(base, 0);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 2, 1);
        
        result = DateUtils.addMilliseconds(base, 1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 2, 2);
        
        result = DateUtils.addMilliseconds(base, -1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 2, 0);
    }

// org.apache.commons.lang3.time.DateUtilsTest::testSetYears
    public void testSetYears() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.setYears(base, 2000);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 2, 1);

        result = DateUtils.setYears(base, 2008);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2008, 6, 5, 4, 3, 2, 1);

        result = DateUtils.setYears(base, 2005);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2005, 6, 5, 4, 3, 2, 1);
    }

// org.apache.commons.lang3.time.DateUtilsTest::testSetMonths
    public void testSetMonths() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.setMonths(base, 5);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 5, 5, 4, 3, 2, 1);

        result = DateUtils.setMonths(base, 1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 1, 5, 4, 3, 2, 1);

        try {
            result = DateUtils.setMonths(base, 12);
            fail("DateUtils.setMonths did not throw an expected IllegalArguementException.");
        } catch (IllegalArgumentException e) {

        }
    }

// org.apache.commons.lang3.time.DateUtilsTest::testSetDays
    public void testSetDays() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.setDays(base, 1);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 1, 4, 3, 2, 1);

        result = DateUtils.setDays(base, 29);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 29, 4, 3, 2, 1);

        try {
            result = DateUtils.setDays(base, 32);
            fail("DateUtils.setDays did not throw an expected IllegalArguementException.");
        } catch (IllegalArgumentException e) {

        }
    }

// org.apache.commons.lang3.time.DateUtilsTest::testSetHours
    public void testSetHours() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.setHours(base, 0);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 0, 3, 2, 1);

        result = DateUtils.setHours(base, 23);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 23, 3, 2, 1);

        try {
            result = DateUtils.setHours(base, 24);
            fail("DateUtils.setHours did not throw an expected IllegalArguementException.");
        } catch (IllegalArgumentException e) {

        }
    }

// org.apache.commons.lang3.time.DateUtilsTest::testSetMinutes
    public void testSetMinutes() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.setMinutes(base, 0);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 0, 2, 1);

        result = DateUtils.setMinutes(base, 59);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 59, 2, 1);

        try {
            result = DateUtils.setMinutes(base, 60);
            fail("DateUtils.setMinutes did not throw an expected IllegalArguementException.");
        } catch (IllegalArgumentException e) {

        }
    }

// org.apache.commons.lang3.time.DateUtilsTest::testSetSeconds
    public void testSetSeconds() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.setSeconds(base, 0);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 0, 1);

        result = DateUtils.setSeconds(base, 59);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 59, 1);

        try {
            result = DateUtils.setSeconds(base, 60);
            fail("DateUtils.setSeconds did not throw an expected IllegalArguementException.");
        } catch (IllegalArgumentException e) {

        }
    }

// org.apache.commons.lang3.time.DateUtilsTest::testSetMilliseconds
    public void testSetMilliseconds() throws Exception {
        Date base = new Date(MILLIS_TEST);
        Date result = DateUtils.setMilliseconds(base, 0);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 2, 0);

        result = DateUtils.setMilliseconds(base, 999);
        assertNotSame(base, result);
        assertDate(base, 2000, 6, 5, 4, 3, 2, 1);
        assertDate(result, 2000, 6, 5, 4, 3, 2, 999);

        try {
            result = DateUtils.setMilliseconds(base, 1000);
            fail("DateUtils.setMilliseconds did not throw an expected IllegalArguementException.");
        } catch (IllegalArgumentException e) {

        }
    }

// org.apache.commons.lang3.time.DateUtilsTest::testToCalendar
    public void testToCalendar() {
        assertEquals("Failed to convert to a Calendar and back", date1, DateUtils.toCalendar(date1).getTime());
        try {
            DateUtils.toCalendar(null);
            fail("Expected NullPointerException to be thrown");
        } catch(NullPointerException npe) {
            
        }
    }

// org.apache.commons.lang3.time.DateUtilsTest::testRound
    public void testRound() throws Exception {
        
        assertEquals("round year-1 failed",
                dateParser.parse("January 1, 2002"),
                DateUtils.round(date1, Calendar.YEAR));
        assertEquals("round year-2 failed",
                dateParser.parse("January 1, 2002"),
                DateUtils.round(date2, Calendar.YEAR));
        assertEquals("round month-1 failed",
                dateParser.parse("February 1, 2002"),
                DateUtils.round(date1, Calendar.MONTH));
        assertEquals("round month-2 failed",
                dateParser.parse("December 1, 2001"),
                DateUtils.round(date2, Calendar.MONTH));
        assertEquals("round semimonth-0 failed",
                dateParser.parse("February 1, 2002"),
                DateUtils.round(date0, DateUtils.SEMI_MONTH));
        assertEquals("round semimonth-1 failed",
                dateParser.parse("February 16, 2002"),
                DateUtils.round(date1, DateUtils.SEMI_MONTH));
        assertEquals("round semimonth-2 failed",
                dateParser.parse("November 16, 2001"),
                DateUtils.round(date2, DateUtils.SEMI_MONTH));
        
        
        assertEquals("round date-1 failed",
                dateParser.parse("February 13, 2002"),
                DateUtils.round(date1, Calendar.DATE));
        assertEquals("round date-2 failed",
                dateParser.parse("November 18, 2001"),
                DateUtils.round(date2, Calendar.DATE));
        assertEquals("round hour-1 failed",
                dateTimeParser.parse("February 12, 2002 13:00:00.000"),
                DateUtils.round(date1, Calendar.HOUR));
        assertEquals("round hour-2 failed",
                dateTimeParser.parse("November 18, 2001 1:00:00.000"),
                DateUtils.round(date2, Calendar.HOUR));
        assertEquals("round minute-1 failed",
                dateTimeParser.parse("February 12, 2002 12:35:00.000"),
                DateUtils.round(date1, Calendar.MINUTE));
        assertEquals("round minute-2 failed",
                dateTimeParser.parse("November 18, 2001 1:23:00.000"),
                DateUtils.round(date2, Calendar.MINUTE));
        assertEquals("round second-1 failed",
                dateTimeParser.parse("February 12, 2002 12:34:57.000"),
                DateUtils.round(date1, Calendar.SECOND));
        assertEquals("round second-2 failed",
                dateTimeParser.parse("November 18, 2001 1:23:11.000"),
                DateUtils.round(date2, Calendar.SECOND));
        assertEquals("round ampm-1 failed",
                dateTimeParser.parse("February 3, 2002 00:00:00.000"),
                DateUtils.round(dateAmPm1, Calendar.AM_PM));
        assertEquals("round ampm-2 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.round(dateAmPm2, Calendar.AM_PM));
        assertEquals("round ampm-3 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.round(dateAmPm3, Calendar.AM_PM));
        assertEquals("round ampm-4 failed",
                dateTimeParser.parse("February 4, 2002 00:00:00.000"),
                DateUtils.round(dateAmPm4, Calendar.AM_PM));

        
        assertEquals("round year-1 failed",
                dateParser.parse("January 1, 2002"),
                DateUtils.round((Object) date1, Calendar.YEAR));
        assertEquals("round year-2 failed",
                dateParser.parse("January 1, 2002"),
                DateUtils.round((Object) date2, Calendar.YEAR));
        assertEquals("round month-1 failed",
                dateParser.parse("February 1, 2002"),
                DateUtils.round((Object) date1, Calendar.MONTH));
        assertEquals("round month-2 failed",
                dateParser.parse("December 1, 2001"),
                DateUtils.round((Object) date2, Calendar.MONTH));
        assertEquals("round semimonth-1 failed",
                dateParser.parse("February 16, 2002"),
                DateUtils.round((Object) date1, DateUtils.SEMI_MONTH));
        assertEquals("round semimonth-2 failed",
                dateParser.parse("November 16, 2001"),
                DateUtils.round((Object) date2, DateUtils.SEMI_MONTH));
        assertEquals("round date-1 failed",
                dateParser.parse("February 13, 2002"),
                DateUtils.round((Object) date1, Calendar.DATE));
        assertEquals("round date-2 failed",
                dateParser.parse("November 18, 2001"),
                DateUtils.round((Object) date2, Calendar.DATE));
        assertEquals("round hour-1 failed",
                dateTimeParser.parse("February 12, 2002 13:00:00.000"),
                DateUtils.round((Object) date1, Calendar.HOUR));
        assertEquals("round hour-2 failed",
                dateTimeParser.parse("November 18, 2001 1:00:00.000"),
                DateUtils.round((Object) date2, Calendar.HOUR));
        assertEquals("round minute-1 failed",
                dateTimeParser.parse("February 12, 2002 12:35:00.000"),
                DateUtils.round((Object) date1, Calendar.MINUTE));
        assertEquals("round minute-2 failed",
                dateTimeParser.parse("November 18, 2001 1:23:00.000"),
                DateUtils.round((Object) date2, Calendar.MINUTE));
        assertEquals("round second-1 failed",
                dateTimeParser.parse("February 12, 2002 12:34:57.000"),
                DateUtils.round((Object) date1, Calendar.SECOND));
        assertEquals("round second-2 failed",
                dateTimeParser.parse("November 18, 2001 1:23:11.000"),
                DateUtils.round((Object) date2, Calendar.SECOND));
        assertEquals("round calendar second-1 failed",
                dateTimeParser.parse("February 12, 2002 12:34:57.000"),
                DateUtils.round((Object) cal1, Calendar.SECOND));
        assertEquals("round calendar second-2 failed",
                dateTimeParser.parse("November 18, 2001 1:23:11.000"),
                DateUtils.round((Object) cal2, Calendar.SECOND));
        assertEquals("round ampm-1 failed",
                dateTimeParser.parse("February 3, 2002 00:00:00.000"),
                DateUtils.round((Object) dateAmPm1, Calendar.AM_PM));
        assertEquals("round ampm-2 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.round((Object) dateAmPm2, Calendar.AM_PM));
        assertEquals("round ampm-3 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.round((Object) dateAmPm3, Calendar.AM_PM));
        assertEquals("round ampm-4 failed",
                dateTimeParser.parse("February 4, 2002 00:00:00.000"),
                DateUtils.round((Object) dateAmPm4, Calendar.AM_PM));

        try {
            DateUtils.round((Date) null, Calendar.SECOND);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.round((Calendar) null, Calendar.SECOND);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.round((Object) null, Calendar.SECOND);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.round("", Calendar.SECOND);
            fail();
        } catch (ClassCastException ex) {}
        try {
            DateUtils.round(date1, -9999);
            fail();
        } catch(IllegalArgumentException ex) {}

        assertEquals("round ampm-1 failed",
                dateTimeParser.parse("February 3, 2002 00:00:00.000"),
                DateUtils.round((Object) calAmPm1, Calendar.AM_PM));
        assertEquals("round ampm-2 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.round((Object) calAmPm2, Calendar.AM_PM));
        assertEquals("round ampm-3 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.round((Object) calAmPm3, Calendar.AM_PM));
        assertEquals("round ampm-4 failed",
                dateTimeParser.parse("February 4, 2002 00:00:00.000"),
                DateUtils.round((Object) calAmPm4, Calendar.AM_PM));
        
        
        
        TimeZone.setDefault(zone);
        dateTimeParser.setTimeZone(zone);
        assertEquals("round MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 00:00:00.000"),
                DateUtils.round(date4, Calendar.DATE));
        assertEquals("round MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 00:00:00.000"),
                DateUtils.round((Object) cal4, Calendar.DATE));
        assertEquals("round MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 00:00:00.000"),
                DateUtils.round(date5, Calendar.DATE));
        assertEquals("round MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 00:00:00.000"),
                DateUtils.round((Object) cal5, Calendar.DATE));
        assertEquals("round MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 00:00:00.000"),
                DateUtils.round(date6, Calendar.DATE));
        assertEquals("round MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 00:00:00.000"),
                DateUtils.round((Object) cal6, Calendar.DATE));
        assertEquals("round MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 00:00:00.000"),
                DateUtils.round(date7, Calendar.DATE));
        assertEquals("round MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 00:00:00.000"),
                DateUtils.round((Object) cal7, Calendar.DATE));
        
        assertEquals("round MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 01:00:00.000"),
                DateUtils.round(date4, Calendar.HOUR_OF_DAY));
        assertEquals("round MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 01:00:00.000"),
                DateUtils.round((Object) cal4, Calendar.HOUR_OF_DAY));
        if (SystemUtils.isJavaVersionAtLeast(1.4f)) {
            assertEquals("round MET date across DST change-over",
                    dateTimeParser.parse("March 30, 2003 03:00:00.000"),
                    DateUtils.round(date5, Calendar.HOUR_OF_DAY));
            assertEquals("round MET date across DST change-over",
                    dateTimeParser.parse("March 30, 2003 03:00:00.000"),
                    DateUtils.round((Object) cal5, Calendar.HOUR_OF_DAY));
            assertEquals("round MET date across DST change-over",
                    dateTimeParser.parse("March 30, 2003 03:00:00.000"),
                    DateUtils.round(date6, Calendar.HOUR_OF_DAY));
            assertEquals("round MET date across DST change-over",
                    dateTimeParser.parse("March 30, 2003 03:00:00.000"),
                    DateUtils.round((Object) cal6, Calendar.HOUR_OF_DAY));
            assertEquals("round MET date across DST change-over",
                    dateTimeParser.parse("March 30, 2003 04:00:00.000"),
                    DateUtils.round(date7, Calendar.HOUR_OF_DAY));
            assertEquals("round MET date across DST change-over",
                    dateTimeParser.parse("March 30, 2003 04:00:00.000"),
                    DateUtils.round((Object) cal7, Calendar.HOUR_OF_DAY));
        } else {
            this.warn("WARNING: Some date rounding tests not run since the current version is " + SystemUtils.JAVA_VERSION);
        }
        TimeZone.setDefault(defaultZone);
        dateTimeParser.setTimeZone(defaultZone);
    }

// org.apache.commons.lang3.time.DateUtilsTest::testRoundLang346
    public void testRoundLang346() throws Exception
    {
        TimeZone.setDefault(defaultZone);
        dateTimeParser.setTimeZone(defaultZone);
        Calendar testCalendar = Calendar.getInstance();
        testCalendar.set(2007, 6, 2, 8, 8, 50);
        Date date = testCalendar.getTime();
        assertEquals("Minute Round Up Failed",
                     dateTimeParser.parse("July 2, 2007 08:09:00.000"),
                     DateUtils.round(date, Calendar.MINUTE));

        testCalendar.set(2007, 6, 2, 8, 8, 20);
        date = testCalendar.getTime();
        assertEquals("Minute No Round Failed",
                     dateTimeParser.parse("July 2, 2007 08:08:00.000"),
                     DateUtils.round(date, Calendar.MINUTE));

        testCalendar.set(2007, 6, 2, 8, 8, 50);
        testCalendar.set(Calendar.MILLISECOND, 600);
        date = testCalendar.getTime();

        assertEquals("Second Round Up with 600 Milli Seconds Failed",
                     dateTimeParser.parse("July 2, 2007 08:08:51.000"),
                     DateUtils.round(date, Calendar.SECOND));

        testCalendar.set(2007, 6, 2, 8, 8, 50);
        testCalendar.set(Calendar.MILLISECOND, 200);
        date = testCalendar.getTime();
        assertEquals("Second Round Down with 200 Milli Seconds Failed",
                     dateTimeParser.parse("July 2, 2007 08:08:50.000"),
                     DateUtils.round(date, Calendar.SECOND));

        testCalendar.set(2007, 6, 2, 8, 8, 20);
        testCalendar.set(Calendar.MILLISECOND, 600);
        date = testCalendar.getTime();
        assertEquals("Second Round Up with 200 Milli Seconds Failed",
                     dateTimeParser.parse("July 2, 2007 08:08:21.000"),
                     DateUtils.round(date, Calendar.SECOND));

        testCalendar.set(2007, 6, 2, 8, 8, 20);
        testCalendar.set(Calendar.MILLISECOND, 200);
        date = testCalendar.getTime();
        assertEquals("Second Round Down with 200 Milli Seconds Failed",
                     dateTimeParser.parse("July 2, 2007 08:08:20.000"),
                     DateUtils.round(date, Calendar.SECOND));

        testCalendar.set(2007, 6, 2, 8, 8, 50);
        date = testCalendar.getTime();
        assertEquals("Hour Round Down Failed",
                     dateTimeParser.parse("July 2, 2007 08:00:00.000"),
                     DateUtils.round(date, Calendar.HOUR));

        testCalendar.set(2007, 6, 2, 8, 31, 50);
        date = testCalendar.getTime();
        assertEquals("Hour Round Up Failed",
                     dateTimeParser.parse("July 2, 2007 09:00:00.000"),
                     DateUtils.round(date, Calendar.HOUR));
    }

// org.apache.commons.lang3.time.DateUtilsTest::testTruncate
    public void testTruncate() throws Exception {
        
        assertEquals("truncate year-1 failed",
                dateParser.parse("January 1, 2002"),
                DateUtils.truncate(date1, Calendar.YEAR));
        assertEquals("truncate year-2 failed",
                dateParser.parse("January 1, 2001"),
                DateUtils.truncate(date2, Calendar.YEAR));
        assertEquals("truncate month-1 failed",
                dateParser.parse("February 1, 2002"),
                DateUtils.truncate(date1, Calendar.MONTH));
        assertEquals("truncate month-2 failed",
                dateParser.parse("November 1, 2001"),
                DateUtils.truncate(date2, Calendar.MONTH));
        assertEquals("truncate semimonth-1 failed",
                dateParser.parse("February 1, 2002"),
                DateUtils.truncate(date1, DateUtils.SEMI_MONTH));
        assertEquals("truncate semimonth-2 failed",
                dateParser.parse("November 16, 2001"),
                DateUtils.truncate(date2, DateUtils.SEMI_MONTH));
        assertEquals("truncate date-1 failed",
                dateParser.parse("February 12, 2002"),
                DateUtils.truncate(date1, Calendar.DATE));
        assertEquals("truncate date-2 failed",
                dateParser.parse("November 18, 2001"),
                DateUtils.truncate(date2, Calendar.DATE));
        assertEquals("truncate hour-1 failed",
                dateTimeParser.parse("February 12, 2002 12:00:00.000"),
                DateUtils.truncate(date1, Calendar.HOUR));
        assertEquals("truncate hour-2 failed",
                dateTimeParser.parse("November 18, 2001 1:00:00.000"),
                DateUtils.truncate(date2, Calendar.HOUR));
        assertEquals("truncate minute-1 failed",
                dateTimeParser.parse("February 12, 2002 12:34:00.000"),
                DateUtils.truncate(date1, Calendar.MINUTE));
        assertEquals("truncate minute-2 failed",
                dateTimeParser.parse("November 18, 2001 1:23:00.000"),
                DateUtils.truncate(date2, Calendar.MINUTE));
        assertEquals("truncate second-1 failed",
                dateTimeParser.parse("February 12, 2002 12:34:56.000"),
                DateUtils.truncate(date1, Calendar.SECOND));
        assertEquals("truncate second-2 failed",
                dateTimeParser.parse("November 18, 2001 1:23:11.000"),
                DateUtils.truncate(date2, Calendar.SECOND));
        assertEquals("truncate ampm-1 failed",
                dateTimeParser.parse("February 3, 2002 00:00:00.000"),
                DateUtils.truncate(dateAmPm1, Calendar.AM_PM));
        assertEquals("truncate ampm-2 failed",
                dateTimeParser.parse("February 3, 2002 00:00:00.000"),
                DateUtils.truncate(dateAmPm2, Calendar.AM_PM));
        assertEquals("truncate ampm-3 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.truncate(dateAmPm3, Calendar.AM_PM));
        assertEquals("truncate ampm-4 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.truncate(dateAmPm4, Calendar.AM_PM));

        
        assertEquals("truncate year-1 failed",
                dateParser.parse("January 1, 2002"),
                DateUtils.truncate((Object) date1, Calendar.YEAR));
        assertEquals("truncate year-2 failed",
                dateParser.parse("January 1, 2001"),
                DateUtils.truncate((Object) date2, Calendar.YEAR));
        assertEquals("truncate month-1 failed",
                dateParser.parse("February 1, 2002"),
                DateUtils.truncate((Object) date1, Calendar.MONTH));
        assertEquals("truncate month-2 failed",
                dateParser.parse("November 1, 2001"),
                DateUtils.truncate((Object) date2, Calendar.MONTH));
        assertEquals("truncate semimonth-1 failed",
                dateParser.parse("February 1, 2002"),
                DateUtils.truncate((Object) date1, DateUtils.SEMI_MONTH));
        assertEquals("truncate semimonth-2 failed",
                dateParser.parse("November 16, 2001"),
                DateUtils.truncate((Object) date2, DateUtils.SEMI_MONTH));
        assertEquals("truncate date-1 failed",
                dateParser.parse("February 12, 2002"),
                DateUtils.truncate((Object) date1, Calendar.DATE));
        assertEquals("truncate date-2 failed",
                dateParser.parse("November 18, 2001"),
                DateUtils.truncate((Object) date2, Calendar.DATE));
        assertEquals("truncate hour-1 failed",
                dateTimeParser.parse("February 12, 2002 12:00:00.000"),
                DateUtils.truncate((Object) date1, Calendar.HOUR));
        assertEquals("truncate hour-2 failed",
                dateTimeParser.parse("November 18, 2001 1:00:00.000"),
                DateUtils.truncate((Object) date2, Calendar.HOUR));
        assertEquals("truncate minute-1 failed",
                dateTimeParser.parse("February 12, 2002 12:34:00.000"),
                DateUtils.truncate((Object) date1, Calendar.MINUTE));
        assertEquals("truncate minute-2 failed",
                dateTimeParser.parse("November 18, 2001 1:23:00.000"),
                DateUtils.truncate((Object) date2, Calendar.MINUTE));
        assertEquals("truncate second-1 failed",
                dateTimeParser.parse("February 12, 2002 12:34:56.000"),
                DateUtils.truncate((Object) date1, Calendar.SECOND));
        assertEquals("truncate second-2 failed",
                dateTimeParser.parse("November 18, 2001 1:23:11.000"),
                DateUtils.truncate((Object) date2, Calendar.SECOND));
        assertEquals("truncate ampm-1 failed",
                dateTimeParser.parse("February 3, 2002 00:00:00.000"),
                DateUtils.truncate((Object) dateAmPm1, Calendar.AM_PM));
        assertEquals("truncate ampm-2 failed",
                dateTimeParser.parse("February 3, 2002 00:00:00.000"),
                DateUtils.truncate((Object) dateAmPm2, Calendar.AM_PM));
        assertEquals("truncate ampm-3 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.truncate((Object) dateAmPm3, Calendar.AM_PM));
        assertEquals("truncate ampm-4 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.truncate((Object) dateAmPm4, Calendar.AM_PM));
        
        assertEquals("truncate calendar second-1 failed",
                dateTimeParser.parse("February 12, 2002 12:34:56.000"),
                DateUtils.truncate((Object) cal1, Calendar.SECOND));
        assertEquals("truncate calendar second-2 failed",
                dateTimeParser.parse("November 18, 2001 1:23:11.000"),
                DateUtils.truncate((Object) cal2, Calendar.SECOND));
        
        assertEquals("truncate ampm-1 failed",
                dateTimeParser.parse("February 3, 2002 00:00:00.000"),
                DateUtils.truncate((Object) calAmPm1, Calendar.AM_PM));
        assertEquals("truncate ampm-2 failed",
                dateTimeParser.parse("February 3, 2002 00:00:00.000"),
                DateUtils.truncate((Object) calAmPm2, Calendar.AM_PM));
        assertEquals("truncate ampm-3 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.truncate((Object) calAmPm3, Calendar.AM_PM));
        assertEquals("truncate ampm-4 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.truncate((Object) calAmPm4, Calendar.AM_PM));
        
        try {
            DateUtils.truncate((Date) null, Calendar.SECOND);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.truncate((Calendar) null, Calendar.SECOND);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.truncate((Object) null, Calendar.SECOND);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.truncate("", Calendar.SECOND);
            fail();
        } catch (ClassCastException ex) {}

        
        
        TimeZone.setDefault(zone);
        dateTimeParser.setTimeZone(zone);
        assertEquals("truncate MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 00:00:00.000"),
                DateUtils.truncate(date3, Calendar.DATE));
        assertEquals("truncate MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 00:00:00.000"),
                DateUtils.truncate((Object) cal3, Calendar.DATE));
        
        assertEquals("truncate MET date across DST change-over",
                dateTimeParser.parse("October 26, 2003 00:00:00.000"),
                DateUtils.truncate(date8, Calendar.DATE));
        assertEquals("truncate MET date across DST change-over",
                dateTimeParser.parse("October 26, 2003 00:00:00.000"),
                DateUtils.truncate((Object) cal8, Calendar.DATE));
        TimeZone.setDefault(defaultZone);
        dateTimeParser.setTimeZone(defaultZone);
        
        
        Date endOfTime = new Date(Long.MAX_VALUE); 
        GregorianCalendar endCal = new GregorianCalendar();
        endCal.setTime(endOfTime);
        try {
            DateUtils.truncate(endCal, Calendar.DATE);
            fail();
        } catch (ArithmeticException ex) {}
        endCal.set(Calendar.YEAR, 280000001);
        try {
            DateUtils.truncate(endCal, Calendar.DATE);
            fail();
        } catch (ArithmeticException ex) {}
        endCal.set(Calendar.YEAR, 280000000);
        Calendar cal = DateUtils.truncate(endCal, Calendar.DATE);
        assertEquals(0, cal.get(Calendar.HOUR));
    }

// org.apache.commons.lang3.time.DateUtilsTest::testTruncateLang59
    public void testTruncateLang59() throws Exception {
        if (!SystemUtils.isJavaVersionAtLeast(1.4f)) {
            this.warn("WARNING: Test for LANG-59 not run since the current version is " + SystemUtils.JAVA_VERSION);
            return;
        }

        
        TimeZone MST_MDT = TimeZone.getTimeZone("MST7MDT");
        TimeZone.setDefault(MST_MDT);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
        format.setTimeZone(MST_MDT);

        Date oct31_01MDT = new Date(1099206000000L); 

        Date oct31MDT             = new Date(oct31_01MDT.getTime()       - 3600000L); 
        Date oct31_01_02MDT       = new Date(oct31_01MDT.getTime()       + 120000L);  
        Date oct31_01_02_03MDT    = new Date(oct31_01_02MDT.getTime()    + 3000L);    
        Date oct31_01_02_03_04MDT = new Date(oct31_01_02_03MDT.getTime() + 4L);       

        assertEquals("Check 00:00:00.000", "2004-10-31 00:00:00.000 MDT", format.format(oct31MDT));
        assertEquals("Check 01:00:00.000", "2004-10-31 01:00:00.000 MDT", format.format(oct31_01MDT));
        assertEquals("Check 01:02:00.000", "2004-10-31 01:02:00.000 MDT", format.format(oct31_01_02MDT));
        assertEquals("Check 01:02:03.000", "2004-10-31 01:02:03.000 MDT", format.format(oct31_01_02_03MDT));
        assertEquals("Check 01:02:03.004", "2004-10-31 01:02:03.004 MDT", format.format(oct31_01_02_03_04MDT));

        
        Calendar gval = Calendar.getInstance();
        gval.setTime(new Date(oct31_01MDT.getTime()));
        gval.set(Calendar.MINUTE, gval.get(Calendar.MINUTE)); 
        assertEquals("Demonstrate Problem", gval.getTime().getTime(), oct31_01MDT.getTime() + 3600000L);

        
        assertEquals("Truncate Calendar.MILLISECOND",
                oct31_01_02_03_04MDT, DateUtils.truncate(oct31_01_02_03_04MDT, Calendar.MILLISECOND));

        assertEquals("Truncate Calendar.SECOND",
                   oct31_01_02_03MDT, DateUtils.truncate(oct31_01_02_03_04MDT, Calendar.SECOND));

        assertEquals("Truncate Calendar.MINUTE",
                      oct31_01_02MDT, DateUtils.truncate(oct31_01_02_03_04MDT, Calendar.MINUTE));

        assertEquals("Truncate Calendar.HOUR_OF_DAY",
                         oct31_01MDT, DateUtils.truncate(oct31_01_02_03_04MDT, Calendar.HOUR_OF_DAY));

        assertEquals("Truncate Calendar.HOUR",
                         oct31_01MDT, DateUtils.truncate(oct31_01_02_03_04MDT, Calendar.HOUR));

        assertEquals("Truncate Calendar.DATE",
                            oct31MDT, DateUtils.truncate(oct31_01_02_03_04MDT, Calendar.DATE));

        
        assertEquals("Round Calendar.MILLISECOND",
                oct31_01_02_03_04MDT, DateUtils.round(oct31_01_02_03_04MDT, Calendar.MILLISECOND));

        assertEquals("Round Calendar.SECOND",
                   oct31_01_02_03MDT, DateUtils.round(oct31_01_02_03_04MDT, Calendar.SECOND));

        assertEquals("Round Calendar.MINUTE",
                      oct31_01_02MDT, DateUtils.round(oct31_01_02_03_04MDT, Calendar.MINUTE));

        assertEquals("Round Calendar.HOUR_OF_DAY",
                         oct31_01MDT, DateUtils.round(oct31_01_02_03_04MDT, Calendar.HOUR_OF_DAY));

        assertEquals("Round Calendar.HOUR",
                         oct31_01MDT, DateUtils.round(oct31_01_02_03_04MDT, Calendar.HOUR));

        assertEquals("Round Calendar.DATE",
                            oct31MDT, DateUtils.round(oct31_01_02_03_04MDT, Calendar.DATE));

        
        TimeZone.setDefault(defaultZone);
    }

// org.apache.commons.lang3.time.DateUtilsTest::testLang530
    public void testLang530() throws ParseException {
        Date d = new Date();
        String isoDateStr = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(d);
        Date d2 = DateUtils.parseDate(isoDateStr, new String[] { DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern() });
        
        assertEquals("Date not equal to itself ISO formatted and parsed", d.getTime(), d2.getTime() + d.getTime() % 1000); 
    }

// org.apache.commons.lang3.time.DateUtilsTest::testCeil
    public void testCeil() throws Exception {
        
        assertEquals("ceiling year-1 failed",
                dateParser.parse("January 1, 2003"),
                DateUtils.ceiling(date1, Calendar.YEAR));
        assertEquals("ceiling year-2 failed",
                dateParser.parse("January 1, 2002"),
                DateUtils.ceiling(date2, Calendar.YEAR));
        assertEquals("ceiling month-1 failed",
                dateParser.parse("March 1, 2002"),
                DateUtils.ceiling(date1, Calendar.MONTH));
        assertEquals("ceiling month-2 failed",
                dateParser.parse("December 1, 2001"),
                DateUtils.ceiling(date2, Calendar.MONTH));
        assertEquals("ceiling semimonth-1 failed",
                dateParser.parse("February 16, 2002"),
                DateUtils.ceiling(date1, DateUtils.SEMI_MONTH));
        assertEquals("ceiling semimonth-2 failed",
                dateParser.parse("December 1, 2001"),
                DateUtils.ceiling(date2, DateUtils.SEMI_MONTH));
        assertEquals("ceiling date-1 failed",
                dateParser.parse("February 13, 2002"),
                DateUtils.ceiling(date1, Calendar.DATE));
        assertEquals("ceiling date-2 failed",
                dateParser.parse("November 19, 2001"),
                DateUtils.ceiling(date2, Calendar.DATE));
        assertEquals("ceiling hour-1 failed",
                dateTimeParser.parse("February 12, 2002 13:00:00.000"),
                DateUtils.ceiling(date1, Calendar.HOUR));
        assertEquals("ceiling hour-2 failed",
                dateTimeParser.parse("November 18, 2001 2:00:00.000"),
                DateUtils.ceiling(date2, Calendar.HOUR));
        assertEquals("ceiling minute-1 failed",
                dateTimeParser.parse("February 12, 2002 12:35:00.000"),
                DateUtils.ceiling(date1, Calendar.MINUTE));
        assertEquals("ceiling minute-2 failed",
                dateTimeParser.parse("November 18, 2001 1:24:00.000"),
                DateUtils.ceiling(date2, Calendar.MINUTE));
        assertEquals("ceiling second-1 failed",
                dateTimeParser.parse("February 12, 2002 12:34:57.000"),
                DateUtils.ceiling(date1, Calendar.SECOND));
        assertEquals("ceiling second-2 failed",
                dateTimeParser.parse("November 18, 2001 1:23:12.000"),
                DateUtils.ceiling(date2, Calendar.SECOND));
        assertEquals("ceiling ampm-1 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.ceiling(dateAmPm1, Calendar.AM_PM));
        assertEquals("ceiling ampm-2 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.ceiling(dateAmPm2, Calendar.AM_PM));
        assertEquals("ceiling ampm-3 failed",
                dateTimeParser.parse("February 4, 2002 00:00:00.000"),
                DateUtils.ceiling(dateAmPm3, Calendar.AM_PM));
        assertEquals("ceiling ampm-4 failed",
                dateTimeParser.parse("February 4, 2002 00:00:00.000"),
                DateUtils.ceiling(dateAmPm4, Calendar.AM_PM));
        
     
        assertEquals("ceiling year-1 failed",
                dateParser.parse("January 1, 2003"),
                DateUtils.ceiling((Object) date1, Calendar.YEAR));
        assertEquals("ceiling year-2 failed",
                dateParser.parse("January 1, 2002"),
                DateUtils.ceiling((Object) date2, Calendar.YEAR));
        assertEquals("ceiling month-1 failed",
                dateParser.parse("March 1, 2002"),
                DateUtils.ceiling((Object) date1, Calendar.MONTH));
        assertEquals("ceiling month-2 failed",
                dateParser.parse("December 1, 2001"),
                DateUtils.ceiling((Object) date2, Calendar.MONTH));
        assertEquals("ceiling semimonth-1 failed",
                dateParser.parse("February 16, 2002"),
                DateUtils.ceiling((Object) date1, DateUtils.SEMI_MONTH));
        assertEquals("ceiling semimonth-2 failed",
                dateParser.parse("December 1, 2001"),
                DateUtils.ceiling((Object) date2, DateUtils.SEMI_MONTH));
        assertEquals("ceiling date-1 failed",
                dateParser.parse("February 13, 2002"),
                DateUtils.ceiling((Object) date1, Calendar.DATE));
        assertEquals("ceiling date-2 failed",
                dateParser.parse("November 19, 2001"),
                DateUtils.ceiling((Object) date2, Calendar.DATE));
        assertEquals("ceiling hour-1 failed",
                dateTimeParser.parse("February 12, 2002 13:00:00.000"),
                DateUtils.ceiling((Object) date1, Calendar.HOUR));
        assertEquals("ceiling hour-2 failed",
                dateTimeParser.parse("November 18, 2001 2:00:00.000"),
                DateUtils.ceiling((Object) date2, Calendar.HOUR));
        assertEquals("ceiling minute-1 failed",
                dateTimeParser.parse("February 12, 2002 12:35:00.000"),
                DateUtils.ceiling((Object) date1, Calendar.MINUTE));
        assertEquals("ceiling minute-2 failed",
                dateTimeParser.parse("November 18, 2001 1:24:00.000"),
                DateUtils.ceiling((Object) date2, Calendar.MINUTE));
        assertEquals("ceiling second-1 failed",
                dateTimeParser.parse("February 12, 2002 12:34:57.000"),
                DateUtils.ceiling((Object) date1, Calendar.SECOND));
        assertEquals("ceiling second-2 failed",
                dateTimeParser.parse("November 18, 2001 1:23:12.000"),
                DateUtils.ceiling((Object) date2, Calendar.SECOND));
        assertEquals("ceiling ampm-1 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.ceiling((Object) dateAmPm1, Calendar.AM_PM));
        assertEquals("ceiling ampm-2 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.ceiling((Object) dateAmPm2, Calendar.AM_PM));
        assertEquals("ceiling ampm-3 failed",
                dateTimeParser.parse("February 4, 2002 00:00:00.000"),
                DateUtils.ceiling((Object) dateAmPm3, Calendar.AM_PM));
        assertEquals("ceiling ampm-4 failed",
                dateTimeParser.parse("February 4, 2002 00:00:00.000"),
                DateUtils.ceiling((Object) dateAmPm4, Calendar.AM_PM));
        
        assertEquals("ceiling calendar second-1 failed",
                dateTimeParser.parse("February 12, 2002 12:34:57.000"),
                DateUtils.ceiling((Object) cal1, Calendar.SECOND));
        assertEquals("ceiling calendar second-2 failed",
                dateTimeParser.parse("November 18, 2001 1:23:12.000"),
                DateUtils.ceiling((Object) cal2, Calendar.SECOND));
        
        assertEquals("ceiling ampm-1 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.ceiling((Object) calAmPm1, Calendar.AM_PM));
        assertEquals("ceiling ampm-2 failed",
                dateTimeParser.parse("February 3, 2002 12:00:00.000"),
                DateUtils.ceiling((Object) calAmPm2, Calendar.AM_PM));
        assertEquals("ceiling ampm-3 failed",
                dateTimeParser.parse("February 4, 2002 00:00:00.000"),
                DateUtils.ceiling((Object) calAmPm3, Calendar.AM_PM));
        assertEquals("ceiling ampm-4 failed",
                dateTimeParser.parse("February 4, 2002 00:00:00.000"),
                DateUtils.ceiling((Object) calAmPm4, Calendar.AM_PM));

        try {
            DateUtils.ceiling((Date) null, Calendar.SECOND);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.ceiling((Calendar) null, Calendar.SECOND);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.ceiling((Object) null, Calendar.SECOND);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.ceiling("", Calendar.SECOND);
            fail();
        } catch (ClassCastException ex) {}
        try {
            DateUtils.ceiling(date1, -9999);
            fail();
        } catch(IllegalArgumentException ex) {}

        
        
        
        TimeZone.setDefault(zone);
        dateTimeParser.setTimeZone(zone);

        assertEquals("ceiling MET date across DST change-over",
                dateTimeParser.parse("March 31, 2003 00:00:00.000"),
                DateUtils.ceiling(date4, Calendar.DATE));
        assertEquals("ceiling MET date across DST change-over",
                dateTimeParser.parse("March 31, 2003 00:00:00.000"),
                DateUtils.ceiling((Object) cal4, Calendar.DATE));
        assertEquals("ceiling MET date across DST change-over",
                dateTimeParser.parse("March 31, 2003 00:00:00.000"),
                DateUtils.ceiling(date5, Calendar.DATE));
        assertEquals("ceiling MET date across DST change-over",
                dateTimeParser.parse("March 31, 2003 00:00:00.000"),
                DateUtils.ceiling((Object) cal5, Calendar.DATE));
        assertEquals("ceiling MET date across DST change-over",
                dateTimeParser.parse("March 31, 2003 00:00:00.000"),
                DateUtils.ceiling(date6, Calendar.DATE));
        assertEquals("ceiling MET date across DST change-over",
                dateTimeParser.parse("March 31, 2003 00:00:00.000"),
                DateUtils.ceiling((Object) cal6, Calendar.DATE));
        assertEquals("ceiling MET date across DST change-over",
                dateTimeParser.parse("March 31, 2003 00:00:00.000"),
                DateUtils.ceiling(date7, Calendar.DATE));
        assertEquals("ceiling MET date across DST change-over",
                dateTimeParser.parse("March 31, 2003 00:00:00.000"),
                DateUtils.ceiling((Object) cal7, Calendar.DATE));
        
        assertEquals("ceiling MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 03:00:00.000"),
                DateUtils.ceiling(date4, Calendar.HOUR_OF_DAY));
        assertEquals("ceiling MET date across DST change-over",
                dateTimeParser.parse("March 30, 2003 03:00:00.000"),
                DateUtils.ceiling((Object) cal4, Calendar.HOUR_OF_DAY));
        if (SystemUtils.isJavaVersionAtLeast(1.4f)) {
            assertEquals("ceiling MET date across DST change-over",
                    dateTimeParser.parse("March 30, 2003 03:00:00.000"),
                    DateUtils.ceiling(date5, Calendar.HOUR_OF_DAY));
            assertEquals("ceiling MET date across DST change-over",
                    dateTimeParser.parse("March 30, 2003 03:00:00.000"),
                    DateUtils.ceiling((Object) cal5, Calendar.HOUR_OF_DAY));
            assertEquals("ceiling MET date across DST change-over",
                    dateTimeParser.parse("March 30, 2003 04:00:00.000"),
                    DateUtils.ceiling(date6, Calendar.HOUR_OF_DAY));
            assertEquals("ceiling MET date across DST change-over",
                    dateTimeParser.parse("March 30, 2003 04:00:00.000"),
                    DateUtils.ceiling((Object) cal6, Calendar.HOUR_OF_DAY));
            assertEquals("ceiling MET date across DST change-over",
                    dateTimeParser.parse("March 30, 2003 04:00:00.000"),
                    DateUtils.ceiling(date7, Calendar.HOUR_OF_DAY));
            assertEquals("ceiling MET date across DST change-over",
                    dateTimeParser.parse("March 30, 2003 04:00:00.000"),
                    DateUtils.ceiling((Object) cal7, Calendar.HOUR_OF_DAY));
        } else {
            this.warn("WARNING: Some date ceiling tests not run since the current version is " + SystemUtils.JAVA_VERSION);
        }
        TimeZone.setDefault(defaultZone);
        dateTimeParser.setTimeZone(defaultZone);
        
     
        Date endOfTime = new Date(Long.MAX_VALUE); 
        GregorianCalendar endCal = new GregorianCalendar();
        endCal.setTime(endOfTime);
        try {
            DateUtils.ceiling(endCal, Calendar.DATE);
            fail();
        } catch (ArithmeticException ex) {}
        endCal.set(Calendar.YEAR, 280000001);
        try {
            DateUtils.ceiling(endCal, Calendar.DATE);
            fail();
        } catch (ArithmeticException ex) {}
        endCal.set(Calendar.YEAR, 280000000);
        Calendar cal = DateUtils.ceiling(endCal, Calendar.DATE);
        assertEquals(0, cal.get(Calendar.HOUR));
    }

// org.apache.commons.lang3.time.DateUtilsTest::testIteratorEx
    public void testIteratorEx() throws Exception {
        try {
            DateUtils.iterator(Calendar.getInstance(), -9999);
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.iterator((Date) null, DateUtils.RANGE_WEEK_CENTER);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.iterator((Calendar) null, DateUtils.RANGE_WEEK_CENTER);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.iterator((Object) null, DateUtils.RANGE_WEEK_CENTER);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateUtils.iterator("", DateUtils.RANGE_WEEK_CENTER);
            fail();
        } catch (ClassCastException ex) {}
    }

// org.apache.commons.lang3.time.DateUtilsTest::testWeekIterator
    public void testWeekIterator() throws Exception {
        Calendar now = Calendar.getInstance();
        for (int i = 0; i< 7; i++) {
            Calendar today = DateUtils.truncate(now, Calendar.DATE);
            Calendar sunday = DateUtils.truncate(now, Calendar.DATE);
            sunday.add(Calendar.DATE, 1 - sunday.get(Calendar.DAY_OF_WEEK));
            Calendar monday = DateUtils.truncate(now, Calendar.DATE);
            if (monday.get(Calendar.DAY_OF_WEEK) == 1) {
                
                monday.add(Calendar.DATE, -6);
            } else {
                monday.add(Calendar.DATE, 2 - monday.get(Calendar.DAY_OF_WEEK));
            }
            Calendar centered = DateUtils.truncate(now, Calendar.DATE);
            centered.add(Calendar.DATE, -3);
            
            Iterator<?> it = DateUtils.iterator(now, DateUtils.RANGE_WEEK_SUNDAY);
            assertWeekIterator(it, sunday);
            it = DateUtils.iterator(now, DateUtils.RANGE_WEEK_MONDAY);
            assertWeekIterator(it, monday);
            it = DateUtils.iterator(now, DateUtils.RANGE_WEEK_RELATIVE);
            assertWeekIterator(it, today);
            it = DateUtils.iterator(now, DateUtils.RANGE_WEEK_CENTER);
            assertWeekIterator(it, centered);
            
            it = DateUtils.iterator((Object) now, DateUtils.RANGE_WEEK_CENTER);
            assertWeekIterator(it, centered);
            it = DateUtils.iterator((Object) now.getTime(), DateUtils.RANGE_WEEK_CENTER);
            assertWeekIterator(it, centered);
            try {
                it.next();
                fail();
            } catch (NoSuchElementException ex) {}
            it = DateUtils.iterator(now, DateUtils.RANGE_WEEK_CENTER);
            it.next();
            try {
                it.remove();
            } catch( UnsupportedOperationException ex) {}
            
            now.add(Calendar.DATE,1);
        }
    }

// org.apache.commons.lang3.time.DateUtilsTest::testMonthIterator
    public void testMonthIterator() throws Exception {
        Iterator<?> it = DateUtils.iterator(date1, DateUtils.RANGE_MONTH_SUNDAY);
        assertWeekIterator(it,
                dateParser.parse("January 27, 2002"),
                dateParser.parse("March 2, 2002"));

        it = DateUtils.iterator(date1, DateUtils.RANGE_MONTH_MONDAY);
        assertWeekIterator(it,
                dateParser.parse("January 28, 2002"),
                dateParser.parse("March 3, 2002"));

        it = DateUtils.iterator(date2, DateUtils.RANGE_MONTH_SUNDAY);
        assertWeekIterator(it,
                dateParser.parse("October 28, 2001"),
                dateParser.parse("December 1, 2001"));

        it = DateUtils.iterator(date2, DateUtils.RANGE_MONTH_MONDAY);
        assertWeekIterator(it,
                dateParser.parse("October 29, 2001"),
                dateParser.parse("December 2, 2001"));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new DurationFormatUtils());
        Constructor<?>[] cons = DurationFormatUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(DurationFormatUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(DurationFormatUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDurationWords
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
            text = DurationFormatUtils.formatDurationWords(i * 24 * 60 * 60 * 1000L, false, false);
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
        }
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDurationPluralWords
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

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDurationHMS
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

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDurationISO
    public void testFormatDurationISO() {
        assertEquals("P0Y0M0DT0H0M0.000S", DurationFormatUtils.formatDurationISO(0L));
        assertEquals("P0Y0M0DT0H0M0.001S", DurationFormatUtils.formatDurationISO(1L));
        assertEquals("P0Y0M0DT0H0M0.010S", DurationFormatUtils.formatDurationISO(10L));
        assertEquals("P0Y0M0DT0H0M0.100S", DurationFormatUtils.formatDurationISO(100L));
        assertEquals("P0Y0M0DT0H1M15.321S", DurationFormatUtils.formatDurationISO(75321L));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDuration
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

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatPeriodISO
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

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatPeriod
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

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testLexx
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

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testBugzilla38401
    public void testBugzilla38401() {
        assertEqualDuration( "0000/00/30 16:00:00 000", new int[] { 2006, 0, 26, 18, 47, 34 }, 
                             new int[] { 2006, 1, 26, 10, 47, 34 }, "yyyy/MM/dd HH:mm:ss SSS");
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testJiraLang281
    public void testJiraLang281() {
        assertEqualDuration( "09", new int[] { 2005, 11, 31, 0, 0, 0 }, 
                             new int[] { 2006, 9, 6, 0, 0, 0 }, "MM");
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testLowDurations
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

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testEdgeDurations
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

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testDurationsByBruteForce
    public void testDurationsByBruteForce() {
        bruteForce(2006, 0, 1, "d", Calendar.DAY_OF_MONTH);
        bruteForce(2006, 0, 2, "d", Calendar.DAY_OF_MONTH);
        bruteForce(2007, 1, 2, "d", Calendar.DAY_OF_MONTH);
        bruteForce(2004, 1, 29, "d", Calendar.DAY_OF_MONTH);
        bruteForce(1996, 1, 29, "d", Calendar.DAY_OF_MONTH);

        bruteForce(1969, 1, 28, "M", Calendar.MONTH);  
        
    }

// org.apache.commons.lang3.time.FastDateFormatTest::test_getInstance
    public void test_getInstance() {
        FastDateFormat format1 = FastDateFormat.getInstance();
        FastDateFormat format2 = FastDateFormat.getInstance();
        assertSame(format1, format2);
        assertEquals(new SimpleDateFormat().toPattern(), format1.getPattern());
    }

// org.apache.commons.lang3.time.FastDateFormatTest::test_getInstance_String
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

// org.apache.commons.lang3.time.FastDateFormatTest::test_getInstance_String_TimeZone
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

// org.apache.commons.lang3.time.FastDateFormatTest::test_getInstance_String_Locale
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

// org.apache.commons.lang3.time.FastDateFormatTest::test_changeDefault_Locale_DateInstance
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

// org.apache.commons.lang3.time.FastDateFormatTest::test_changeDefault_Locale_DateTimeInstance
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

// org.apache.commons.lang3.time.FastDateFormatTest::test_getInstance_String_TimeZone_Locale
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

// org.apache.commons.lang3.time.FastDateFormatTest::testFormat
    public void testFormat() {}

// org.apache.commons.lang3.time.FastDateFormatTest::testShortDateStyleWithLocales
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

// org.apache.commons.lang3.time.FastDateFormatTest::testLowYearPadding
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

// org.apache.commons.lang3.time.FastDateFormatTest::testMilleniumBug
    public void testMilleniumBug() {
        Calendar cal = Calendar.getInstance();
        FastDateFormat format = FastDateFormat.getInstance("dd.MM.yyyy");

        cal.set(1000,0,1);
        assertEquals("01.01.1000", format.format(cal));
    }

// org.apache.commons.lang3.time.FastDateFormatTest::testSimpleDate
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

// org.apache.commons.lang3.time.FastDateFormatTest::testLang303
    public void testLang303() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004,11,31);

        FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/dd");
        String output = format.format(cal);

        format = (FastDateFormat) SerializationUtils.deserialize( SerializationUtils.serialize( format ) );
        assertEquals(output, format.format(cal));
    }

// org.apache.commons.lang3.time.FastDateFormatTest::testLang538
    public void testLang538() {
        final String dateTime = "2009-10-16T16:42:16.000Z";

        
        
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT-8"));
        cal.clear();
        cal.set(2009, 9, 16, 8, 42, 16);

        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("GMT"));
        assertEquals("dateTime", dateTime, format.format(cal));
    }

// org.apache.commons.lang3.time.FastDateFormatTest::testLang645
    public void testLang645() {
        Locale locale = new Locale("sv", "SE");

        Calendar cal = Calendar.getInstance();
        cal.set(2010, 0, 1, 12, 0, 0);
        Date d = cal.getTime();

        FastDateFormat fdf = FastDateFormat.getInstance("EEEE', week 'ww", locale);

        assertEquals("fredag, week 53", fdf.format(d));
    }
