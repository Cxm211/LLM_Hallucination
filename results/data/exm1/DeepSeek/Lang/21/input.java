// buggy code
    public static boolean isSameLocalTime(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return (cal1.get(Calendar.MILLISECOND) == cal2.get(Calendar.MILLISECOND) &&
                cal1.get(Calendar.SECOND) == cal2.get(Calendar.SECOND) &&
                cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) &&
                cal1.get(Calendar.HOUR) == cal2.get(Calendar.HOUR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.getClass() == cal2.getClass());
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

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testNullDate
    public void testNullDate() {
        try {
            DateUtils.getFragmentInMilliseconds((Date) null, Calendar.MILLISECOND);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInSeconds((Date) null, Calendar.MILLISECOND);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInMinutes((Date) null, Calendar.MILLISECOND);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInHours((Date) null, Calendar.MILLISECOND);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInDays((Date) null, Calendar.MILLISECOND);
            fail();
        } catch(IllegalArgumentException iae) {}
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testNullCalendar
    public void testNullCalendar() {
        try {
            DateUtils.getFragmentInMilliseconds((Calendar) null, Calendar.MILLISECOND);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInSeconds((Calendar) null, Calendar.MILLISECOND);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInMinutes((Calendar) null, Calendar.MILLISECOND);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInHours((Calendar) null, Calendar.MILLISECOND);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInDays((Calendar) null, Calendar.MILLISECOND);
            fail();
        } catch(IllegalArgumentException iae) {}
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testInvalidFragmentWithDate
    public void testInvalidFragmentWithDate() {
        try {
            DateUtils.getFragmentInMilliseconds(aDate, 0);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInSeconds(aDate, 0);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInMinutes(aDate, 0);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInHours(aDate, 0);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInDays(aDate, 0);
            fail();
        } catch(IllegalArgumentException iae) {}
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testInvalidFragmentWithCalendar
    public void testInvalidFragmentWithCalendar() {
        try {
            DateUtils.getFragmentInMilliseconds(aCalendar, 0);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInSeconds(aCalendar, 0);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInMinutes(aCalendar, 0);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInHours(aCalendar, 0);
            fail();
        } catch(IllegalArgumentException iae) {}

        try {
            DateUtils.getFragmentInDays(aCalendar, 0);
            fail();
        } catch(IllegalArgumentException iae) {}
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondFragmentInLargerUnitWithDate
    public void testMillisecondFragmentInLargerUnitWithDate() {
        assertEquals(0, DateUtils.getFragmentInMilliseconds(aDate, Calendar.MILLISECOND));
        assertEquals(0, DateUtils.getFragmentInSeconds(aDate, Calendar.MILLISECOND));
        assertEquals(0, DateUtils.getFragmentInMinutes(aDate, Calendar.MILLISECOND));
        assertEquals(0, DateUtils.getFragmentInHours(aDate, Calendar.MILLISECOND));
        assertEquals(0, DateUtils.getFragmentInDays(aDate, Calendar.MILLISECOND));
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondFragmentInLargerUnitWithCalendar
    public void testMillisecondFragmentInLargerUnitWithCalendar() {
        assertEquals(0, DateUtils.getFragmentInMilliseconds(aCalendar, Calendar.MILLISECOND));
        assertEquals(0, DateUtils.getFragmentInSeconds(aCalendar, Calendar.MILLISECOND));
        assertEquals(0, DateUtils.getFragmentInMinutes(aCalendar, Calendar.MILLISECOND));
        assertEquals(0, DateUtils.getFragmentInHours(aCalendar, Calendar.MILLISECOND));
        assertEquals(0, DateUtils.getFragmentInDays(aCalendar, Calendar.MILLISECOND));
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testSecondFragmentInLargerUnitWithDate
    public void testSecondFragmentInLargerUnitWithDate() {
        assertEquals(0, DateUtils.getFragmentInSeconds(aDate, Calendar.SECOND));
        assertEquals(0, DateUtils.getFragmentInMinutes(aDate, Calendar.SECOND));
        assertEquals(0, DateUtils.getFragmentInHours(aDate, Calendar.SECOND));
        assertEquals(0, DateUtils.getFragmentInDays(aDate, Calendar.SECOND));
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testSecondFragmentInLargerUnitWithCalendar
    public void testSecondFragmentInLargerUnitWithCalendar() {
        assertEquals(0, DateUtils.getFragmentInSeconds(aCalendar, Calendar.SECOND));
        assertEquals(0, DateUtils.getFragmentInMinutes(aCalendar, Calendar.SECOND));
        assertEquals(0, DateUtils.getFragmentInHours(aCalendar, Calendar.SECOND));
        assertEquals(0, DateUtils.getFragmentInDays(aCalendar, Calendar.SECOND));
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMinuteFragmentInLargerUnitWithDate
    public void testMinuteFragmentInLargerUnitWithDate() {
        assertEquals(0, DateUtils.getFragmentInMinutes(aDate, Calendar.MINUTE));
        assertEquals(0, DateUtils.getFragmentInHours(aDate, Calendar.MINUTE));
        assertEquals(0, DateUtils.getFragmentInDays(aDate, Calendar.MINUTE));
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMinuteFragmentInLargerUnitWithCalendar
    public void testMinuteFragmentInLargerUnitWithCalendar() {
        assertEquals(0, DateUtils.getFragmentInMinutes(aCalendar, Calendar.MINUTE));
        assertEquals(0, DateUtils.getFragmentInHours(aCalendar, Calendar.MINUTE));
        assertEquals(0, DateUtils.getFragmentInDays(aCalendar, Calendar.MINUTE));
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testHourOfDayFragmentInLargerUnitWithDate
    public void testHourOfDayFragmentInLargerUnitWithDate() {
        assertEquals(0, DateUtils.getFragmentInHours(aDate, Calendar.HOUR_OF_DAY));
        assertEquals(0, DateUtils.getFragmentInDays(aDate, Calendar.HOUR_OF_DAY));
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testHourOfDayFragmentInLargerUnitWithCalendar
    public void testHourOfDayFragmentInLargerUnitWithCalendar() {
        assertEquals(0, DateUtils.getFragmentInHours(aCalendar, Calendar.HOUR_OF_DAY));
        assertEquals(0, DateUtils.getFragmentInDays(aCalendar, Calendar.HOUR_OF_DAY));
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testDayOfYearFragmentInLargerUnitWithDate
    public void testDayOfYearFragmentInLargerUnitWithDate() {
        assertEquals(0, DateUtils.getFragmentInDays(aDate, Calendar.DAY_OF_YEAR));
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testDayOfYearFragmentInLargerUnitWithCalendar
    public void testDayOfYearFragmentInLargerUnitWithCalendar() {
        assertEquals(0, DateUtils.getFragmentInDays(aCalendar, Calendar.DAY_OF_YEAR));
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testDateFragmentInLargerUnitWithDate
    public void testDateFragmentInLargerUnitWithDate() {
        assertEquals(0, DateUtils.getFragmentInDays(aDate, Calendar.DATE));
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testDateFragmentInLargerUnitWithCalendar
    public void testDateFragmentInLargerUnitWithCalendar() {
        assertEquals(0, DateUtils.getFragmentInDays(aCalendar, Calendar.DATE));
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondsOfSecondWithDate
    public void testMillisecondsOfSecondWithDate() {
        long testResult = DateUtils.getFragmentInMilliseconds(aDate, Calendar.SECOND);
        assertEquals(millis, testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondsOfSecondWithCalendar
    public void testMillisecondsOfSecondWithCalendar() {
        long testResult = DateUtils.getFragmentInMilliseconds(aCalendar, Calendar.SECOND);
        assertEquals(millis, testResult);
        assertEquals(aCalendar.get(Calendar.MILLISECOND), testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondsOfMinuteWithDate
    public void testMillisecondsOfMinuteWithDate() {
        long testResult = DateUtils.getFragmentInMilliseconds(aDate, Calendar.MINUTE);
        assertEquals(millis + (seconds * DateUtils.MILLIS_PER_SECOND), testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondsOfMinuteWithCalender
    public void testMillisecondsOfMinuteWithCalender() {
        long testResult = DateUtils.getFragmentInMilliseconds(aCalendar, Calendar.MINUTE);
        assertEquals(millis + (seconds * DateUtils.MILLIS_PER_SECOND), testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testSecondsofMinuteWithDate
    public void testSecondsofMinuteWithDate() {
        long testResult = DateUtils.getFragmentInSeconds(aDate, Calendar.MINUTE);
        assertEquals(seconds, testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testSecondsofMinuteWithCalendar
    public void testSecondsofMinuteWithCalendar() {
        long testResult = DateUtils.getFragmentInSeconds(aCalendar, Calendar.MINUTE);
        assertEquals(seconds, testResult);
        assertEquals(aCalendar.get(Calendar.SECOND), testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondsOfHourWithDate
    public void testMillisecondsOfHourWithDate() {
        long testResult = DateUtils.getFragmentInMilliseconds(aDate, Calendar.HOUR_OF_DAY);
        assertEquals(millis + (seconds * DateUtils.MILLIS_PER_SECOND) + (minutes * DateUtils.MILLIS_PER_MINUTE), testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondsOfHourWithCalendar
    public void testMillisecondsOfHourWithCalendar() {
        long testResult = DateUtils.getFragmentInMilliseconds(aCalendar, Calendar.HOUR_OF_DAY);
        assertEquals(millis + (seconds * DateUtils.MILLIS_PER_SECOND) + (minutes * DateUtils.MILLIS_PER_MINUTE), testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testSecondsofHourWithDate
    public void testSecondsofHourWithDate() {
        long testResult = DateUtils.getFragmentInSeconds(aDate, Calendar.HOUR_OF_DAY);
        assertEquals(
                seconds
                        + (minutes
                                * DateUtils.MILLIS_PER_MINUTE / DateUtils.MILLIS_PER_SECOND),
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testSecondsofHourWithCalendar
    public void testSecondsofHourWithCalendar() {
        long testResult = DateUtils.getFragmentInSeconds(aCalendar, Calendar.HOUR_OF_DAY);
        assertEquals(
                seconds
                        + (minutes
                                * DateUtils.MILLIS_PER_MINUTE / DateUtils.MILLIS_PER_SECOND),
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMinutesOfHourWithDate
    public void testMinutesOfHourWithDate() {
        long testResult = DateUtils.getFragmentInMinutes(aDate, Calendar.HOUR_OF_DAY);
        assertEquals(minutes, testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMinutesOfHourWithCalendar
    public void testMinutesOfHourWithCalendar() {
        long testResult = DateUtils.getFragmentInMinutes(aCalendar, Calendar.HOUR_OF_DAY);
        assertEquals(minutes, testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondsOfDayWithDate
    public void testMillisecondsOfDayWithDate() {
        long testresult = DateUtils.getFragmentInMilliseconds(aDate, Calendar.DATE);
        long expectedValue = millis + (seconds * DateUtils.MILLIS_PER_SECOND) + (minutes * DateUtils.MILLIS_PER_MINUTE) + (hours * DateUtils.MILLIS_PER_HOUR); 
        assertEquals(expectedValue, testresult);
        testresult = DateUtils.getFragmentInMilliseconds(aDate, Calendar.DAY_OF_YEAR);
        assertEquals(expectedValue, testresult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondsOfDayWithCalendar
    public void testMillisecondsOfDayWithCalendar() {
        long testresult = DateUtils.getFragmentInMilliseconds(aCalendar, Calendar.DATE);
        long expectedValue = millis + (seconds * DateUtils.MILLIS_PER_SECOND) + (minutes * DateUtils.MILLIS_PER_MINUTE) + (hours * DateUtils.MILLIS_PER_HOUR); 
        assertEquals(expectedValue, testresult);
        testresult = DateUtils.getFragmentInMilliseconds(aCalendar, Calendar.DAY_OF_YEAR);
        assertEquals(expectedValue, testresult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testSecondsOfDayWithDate
    public void testSecondsOfDayWithDate() {
        long testresult = DateUtils.getFragmentInSeconds(aDate, Calendar.DATE);
        long expectedValue = seconds + ((minutes * DateUtils.MILLIS_PER_MINUTE) + (hours * DateUtils.MILLIS_PER_HOUR))/ DateUtils.MILLIS_PER_SECOND;
        assertEquals(expectedValue, testresult);
        testresult = DateUtils.getFragmentInSeconds(aDate, Calendar.DAY_OF_YEAR);
        assertEquals(expectedValue, testresult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testSecondsOfDayWithCalendar
    public void testSecondsOfDayWithCalendar() {
        long testresult = DateUtils.getFragmentInSeconds(aCalendar, Calendar.DATE);
        long expectedValue = seconds + ((minutes * DateUtils.MILLIS_PER_MINUTE) + (hours * DateUtils.MILLIS_PER_HOUR))/ DateUtils.MILLIS_PER_SECOND;
        assertEquals(expectedValue, testresult);
        testresult = DateUtils.getFragmentInSeconds(aCalendar, Calendar.DAY_OF_YEAR);
        assertEquals(expectedValue, testresult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMinutesOfDayWithDate
    public void testMinutesOfDayWithDate() {
        long testResult = DateUtils.getFragmentInMinutes(aDate, Calendar.DATE);
        long expectedValue = minutes + ((hours * DateUtils.MILLIS_PER_HOUR))/ DateUtils.MILLIS_PER_MINUTE; 
        assertEquals(expectedValue,testResult);
        testResult = DateUtils.getFragmentInMinutes(aDate, Calendar.DAY_OF_YEAR);
        assertEquals(expectedValue,testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMinutesOfDayWithCalendar
    public void testMinutesOfDayWithCalendar() {
        long testResult = DateUtils.getFragmentInMinutes(aCalendar, Calendar.DATE);
        long expectedValue = minutes + ((hours * DateUtils.MILLIS_PER_HOUR))/ DateUtils.MILLIS_PER_MINUTE; 
        assertEquals(expectedValue, testResult);
        testResult = DateUtils.getFragmentInMinutes(aCalendar, Calendar.DAY_OF_YEAR);
        assertEquals(expectedValue, testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testHoursOfDayWithDate
    public void testHoursOfDayWithDate() {
        long testResult = DateUtils.getFragmentInHours(aDate, Calendar.DATE);
        long expectedValue = hours; 
        assertEquals(expectedValue,testResult);
        testResult = DateUtils.getFragmentInHours(aDate, Calendar.DAY_OF_YEAR);
        assertEquals(expectedValue,testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testHoursOfDayWithCalendar
    public void testHoursOfDayWithCalendar() {
        long testResult = DateUtils.getFragmentInHours(aCalendar, Calendar.DATE);
        long expectedValue = hours; 
        assertEquals(expectedValue, testResult);
        testResult = DateUtils.getFragmentInHours(aCalendar, Calendar.DAY_OF_YEAR);
        assertEquals(expectedValue, testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondsOfMonthWithDate
    public void testMillisecondsOfMonthWithDate() {
        long testResult = DateUtils.getFragmentInMilliseconds(aDate, Calendar.MONTH);
        assertEquals(millis + (seconds * DateUtils.MILLIS_PER_SECOND) + (minutes * DateUtils.MILLIS_PER_MINUTE)
                                + (hours * DateUtils.MILLIS_PER_HOUR) + (days * DateUtils.MILLIS_PER_DAY),
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondsOfMonthWithCalendar
    public void testMillisecondsOfMonthWithCalendar() {
        long testResult = DateUtils.getFragmentInMilliseconds(aCalendar, Calendar.MONTH);
        assertEquals(millis + (seconds * DateUtils.MILLIS_PER_SECOND) + (minutes * DateUtils.MILLIS_PER_MINUTE)
                + (hours * DateUtils.MILLIS_PER_HOUR) + (days * DateUtils.MILLIS_PER_DAY),
testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testSecondsOfMonthWithDate
    public void testSecondsOfMonthWithDate() {
        long testResult = DateUtils.getFragmentInSeconds(aDate, Calendar.MONTH);
        assertEquals(
                seconds
                        + ((minutes * DateUtils.MILLIS_PER_MINUTE)
                                + (hours * DateUtils.MILLIS_PER_HOUR) + (days * DateUtils.MILLIS_PER_DAY))
                        / DateUtils.MILLIS_PER_SECOND,
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testSecondsOfMonthWithCalendar
    public void testSecondsOfMonthWithCalendar() {
        long testResult = DateUtils.getFragmentInSeconds(aCalendar, Calendar.MONTH);
        assertEquals(
                seconds
                        + ((minutes * DateUtils.MILLIS_PER_MINUTE)
                                + (hours * DateUtils.MILLIS_PER_HOUR) + (days * DateUtils.MILLIS_PER_DAY))
                        / DateUtils.MILLIS_PER_SECOND,
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMinutesOfMonthWithDate
    public void testMinutesOfMonthWithDate() {
        long testResult = DateUtils.getFragmentInMinutes(aDate, Calendar.MONTH);
        assertEquals(minutes
                                + ((hours * DateUtils.MILLIS_PER_HOUR) + (days * DateUtils.MILLIS_PER_DAY))
                        / DateUtils.MILLIS_PER_MINUTE,
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMinutesOfMonthWithCalendar
    public void testMinutesOfMonthWithCalendar() {
        long testResult = DateUtils.getFragmentInMinutes(aCalendar, Calendar.MONTH);
        assertEquals( minutes  +((hours * DateUtils.MILLIS_PER_HOUR) + (days * DateUtils.MILLIS_PER_DAY))
                        / DateUtils.MILLIS_PER_MINUTE,
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testHoursOfMonthWithDate
    public void testHoursOfMonthWithDate() {
        long testResult = DateUtils.getFragmentInHours(aDate, Calendar.MONTH);
        assertEquals(hours + ((days * DateUtils.MILLIS_PER_DAY))
                        / DateUtils.MILLIS_PER_HOUR,
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testHoursOfMonthWithCalendar
    public void testHoursOfMonthWithCalendar() {
        long testResult = DateUtils.getFragmentInHours(aCalendar, Calendar.MONTH);
        assertEquals( hours +((days * DateUtils.MILLIS_PER_DAY))
                        / DateUtils.MILLIS_PER_HOUR,
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondsOfYearWithDate
    public void testMillisecondsOfYearWithDate() {
        long testResult = DateUtils.getFragmentInMilliseconds(aDate, Calendar.YEAR);
        Calendar cal = Calendar.getInstance();
        cal.setTime(aDate);
        assertEquals(millis + (seconds * DateUtils.MILLIS_PER_SECOND) + (minutes * DateUtils.MILLIS_PER_MINUTE)
                                + (hours * DateUtils.MILLIS_PER_HOUR) + (cal.get(Calendar.DAY_OF_YEAR) * DateUtils.MILLIS_PER_DAY),
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMillisecondsOfYearWithCalendar
    public void testMillisecondsOfYearWithCalendar() {
        long testResult = DateUtils.getFragmentInMilliseconds(aCalendar, Calendar.YEAR);
        assertEquals(millis + (seconds * DateUtils.MILLIS_PER_SECOND) + (minutes * DateUtils.MILLIS_PER_MINUTE)
                + (hours * DateUtils.MILLIS_PER_HOUR) + (aCalendar.get(Calendar.DAY_OF_YEAR) * DateUtils.MILLIS_PER_DAY),
testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testSecondsOfYearWithDate
    public void testSecondsOfYearWithDate() {
        long testResult = DateUtils.getFragmentInSeconds(aDate, Calendar.YEAR);
        Calendar cal = Calendar.getInstance();
        cal.setTime(aDate);
        assertEquals(
                seconds
                        + ((minutes * DateUtils.MILLIS_PER_MINUTE)
                                + (hours * DateUtils.MILLIS_PER_HOUR) + (cal.get(Calendar.DAY_OF_YEAR) * DateUtils.MILLIS_PER_DAY))
                        / DateUtils.MILLIS_PER_SECOND,
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testSecondsOfYearWithCalendar
    public void testSecondsOfYearWithCalendar() {
        long testResult = DateUtils.getFragmentInSeconds(aCalendar, Calendar.YEAR);
        assertEquals(
                seconds
                        + ((minutes * DateUtils.MILLIS_PER_MINUTE)
                                + (hours * DateUtils.MILLIS_PER_HOUR) + (aCalendar.get(Calendar.DAY_OF_YEAR) * DateUtils.MILLIS_PER_DAY))
                        / DateUtils.MILLIS_PER_SECOND,
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMinutesOfYearWithDate
    public void testMinutesOfYearWithDate() {
        long testResult = DateUtils.getFragmentInMinutes(aDate, Calendar.YEAR);
        Calendar cal = Calendar.getInstance();
        cal.setTime(aDate);
        assertEquals(minutes
                                + ((hours * DateUtils.MILLIS_PER_HOUR) + (cal.get(Calendar.DAY_OF_YEAR) * DateUtils.MILLIS_PER_DAY))
                        / DateUtils.MILLIS_PER_MINUTE,
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testMinutesOfYearWithCalendar
    public void testMinutesOfYearWithCalendar() {
        long testResult = DateUtils.getFragmentInMinutes(aCalendar, Calendar.YEAR);
        assertEquals( minutes  +((hours * DateUtils.MILLIS_PER_HOUR) + (aCalendar.get(Calendar.DAY_OF_YEAR) * DateUtils.MILLIS_PER_DAY))
                        / DateUtils.MILLIS_PER_MINUTE,
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testHoursOfYearWithDate
    public void testHoursOfYearWithDate() {
        long testResult = DateUtils.getFragmentInHours(aDate, Calendar.YEAR);
        Calendar cal = Calendar.getInstance();
        cal.setTime(aDate);
        assertEquals(hours + ((cal.get(Calendar.DAY_OF_YEAR) * DateUtils.MILLIS_PER_DAY))
                        / DateUtils.MILLIS_PER_HOUR,
                testResult);
    }

// org.apache.commons.lang3.time.DateUtilsFragmentTest::testHoursOfYearWithCalendar
    public void testHoursOfYearWithCalendar() {
        long testResult = DateUtils.getFragmentInHours(aCalendar, Calendar.YEAR);
        assertEquals( hours +((aCalendar.get(Calendar.DAY_OF_YEAR) * DateUtils.MILLIS_PER_DAY))
                        / DateUtils.MILLIS_PER_HOUR,
                testResult);
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

        Calendar cal3 = Calendar.getInstance();
        Calendar cal4 = Calendar.getInstance();
        cal3.set(2004, 6, 9, 4,  0, 0);
        cal4.set(2004, 6, 9, 16, 0, 0);
        cal3.set(Calendar.MILLISECOND, 0);
        cal4.set(Calendar.MILLISECOND, 0);
        assertFalse("LANG-677", DateUtils.isSameLocalTime(cal3, cal4));
        
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
        if (SystemUtils.isJavaVersionAtLeast(JAVA_1_4)) {
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
            this.warn("WARNING: Some date rounding tests not run since the current version is " + SystemUtils.JAVA_SPECIFICATION_VERSION);
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
        if (!SystemUtils.isJavaVersionAtLeast(JAVA_1_4)) {
            this.warn("WARNING: Test for LANG-59 not run since the current version is " + SystemUtils.JAVA_SPECIFICATION_VERSION);
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
        
        assertEquals("ceiling javadoc-1 failed",
                dateTimeParser.parse("March 28, 2002 14:00:00.000"),
                DateUtils.ceiling(
                    dateTimeParser.parse("March 28, 2002 13:45:01.231"),
                Calendar.HOUR));
        assertEquals("ceiling javadoc-2 failed",
                dateTimeParser.parse("April 1, 2002 00:00:00.000"),
                DateUtils.ceiling(
                    dateTimeParser.parse("March 28, 2002 13:45:01.231"),
                Calendar.MONTH));

        
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
        if (SystemUtils.isJavaVersionAtLeast(JAVA_1_4)) {
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
            this.warn("WARNING: Some date ceiling tests not run since the current version is " + SystemUtils.JAVA_SPECIFICATION_VERSION);
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
