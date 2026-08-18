// buggy code
    public static String formatPeriod(long startMillis, long endMillis, String format, boolean padWithZeros, 
            TimeZone timezone) {

        long millis = endMillis - startMillis;
        if (millis < 28 * DateUtils.MILLIS_PER_DAY) {
            return formatDuration(millis, format, padWithZeros);
        }

        Token[] tokens = lexx(format);

        // timezones get funky around 0, so normalizing everything to GMT 
        // stops the hours being off
        Calendar start = Calendar.getInstance(timezone);
        start.setTime(new Date(startMillis));
        Calendar end = Calendar.getInstance(timezone);
        end.setTime(new Date(endMillis));

        // initial estimates
        int milliseconds = end.get(Calendar.MILLISECOND) - start.get(Calendar.MILLISECOND);
        int seconds = end.get(Calendar.SECOND) - start.get(Calendar.SECOND);
        int minutes = end.get(Calendar.MINUTE) - start.get(Calendar.MINUTE);
        int hours = end.get(Calendar.HOUR_OF_DAY) - start.get(Calendar.HOUR_OF_DAY);
        int days = end.get(Calendar.DAY_OF_MONTH) - start.get(Calendar.DAY_OF_MONTH);
        int months = end.get(Calendar.MONTH) - start.get(Calendar.MONTH);
        int years = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);

        // each initial estimate is adjusted in case it is under 0
        while (milliseconds < 0) {
            milliseconds += 1000;
            seconds -= 1;
        }
        while (seconds < 0) {
            seconds += 60;
            minutes -= 1;
        }
        while (minutes < 0) {
            minutes += 60;
            hours -= 1;
        }
        while (hours < 0) {
            hours += 24;
            days -= 1;
        }
        while (days < 0) {
            days += 31;
//days += 31; // TODO: Need tests to show this is bad and the new code is good.
// HEN: It's a tricky subject. Jan 15th to March 10th. If I count days-first it is 
// 1 month and 26 days, but if I count month-first then it is 1 month and 23 days.
// Also it's contextual - if asked for no M in the format then I should probably 
// be doing no calculating here.
            months -= 1;
        }
        while (months < 0) {
            months += 12;
            years -= 1;
        }
        milliseconds -= reduceAndCorrect(start, end, Calendar.MILLISECOND, milliseconds);
        seconds -= reduceAndCorrect(start, end, Calendar.SECOND, seconds);
        minutes -= reduceAndCorrect(start, end, Calendar.MINUTE, minutes);
        hours -= reduceAndCorrect(start, end, Calendar.HOUR_OF_DAY, hours);
        days -= reduceAndCorrect(start, end, Calendar.DAY_OF_MONTH, days);
        months -= reduceAndCorrect(start, end, Calendar.MONTH, months);
        years -= reduceAndCorrect(start, end, Calendar.YEAR, years);

        // This next block of code adds in values that 
        // aren't requested. This allows the user to ask for the 
        // number of months and get the real count and not just 0->11.
        if (!Token.containsTokenWithValue(tokens, y)) {
            if (Token.containsTokenWithValue(tokens, M)) {
                months += 12 * years;
                years = 0;
            } else {
                // TODO: this is a bit weak, needs work to know about leap years
                days += 365 * years;
                years = 0;
            }
        }
        if (!Token.containsTokenWithValue(tokens, M)) {
            days += end.get(Calendar.DAY_OF_YEAR) - start.get(Calendar.DAY_OF_YEAR);
            months = 0;
        }
        if (!Token.containsTokenWithValue(tokens, d)) {
            hours += 24 * days;
            days = 0;
        }
        if (!Token.containsTokenWithValue(tokens, H)) {
            minutes += 60 * hours;
            hours = 0;
        }
        if (!Token.containsTokenWithValue(tokens, m)) {
            seconds += 60 * minutes;
            minutes = 0;
        }
        if (!Token.containsTokenWithValue(tokens, s)) {
            milliseconds += 1000 * seconds;
            seconds = 0;
        }

        return format(tokens, years, months, days, hours, minutes, seconds, milliseconds, padWithZeros);
    }

    static int reduceAndCorrect(Calendar start, Calendar end, int field, int difference) {
        end.add( field, -1 * difference );
        int endValue = end.get(field);
        int startValue = start.get(field);
        if (endValue < startValue) {
            int newdiff = startValue - endValue;
            end.add( field, newdiff );
            return newdiff;
        } else {
            return 0;
        }
    }

// relevant test
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
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.set(Calendar.YEAR, 2005);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.MONTH, Calendar.OCTOBER);
        cal2.set(Calendar.DAY_OF_MONTH, 6);
        cal2.set(Calendar.YEAR, 2006);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        String result = DurationFormatUtils.formatPeriod(cal.getTime().getTime(), cal2.getTime().getTime(), "MM");
        assertEquals("09", result);
    }

// org.apache.commons.lang.time.StopWatchTest::testStopWatchSimple
    public void testStopWatchSimple(){
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.stop();
        long time = watch.getTime();
        assertEquals(time, watch.getTime());
        
        assertTrue(time >= 500);
        assertTrue(time < 700);
        
        watch.reset();
        assertEquals(0, watch.getTime());
    }

// org.apache.commons.lang.time.StopWatchTest::testStopWatchSimpleGet
    public void testStopWatchSimpleGet(){
        StopWatch watch = new StopWatch();
        assertEquals(0, watch.getTime());
        assertEquals("0:00:00.000", watch.toString());
        
        watch.start();
            try {Thread.sleep(500);} catch (InterruptedException ex) {}
        assertTrue(watch.getTime() < 2000);
    }

// org.apache.commons.lang.time.StopWatchTest::testStopWatchSplit
    public void testStopWatchSplit(){
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.split();
        long splitTime = watch.getSplitTime();
        String splitStr = watch.toSplitString();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.unsplit();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.stop();
        long totalTime = watch.getTime();

        assertEquals("Formatted split string not the correct length", 
                     splitStr.length(), 11);
        assertTrue(splitTime >= 500);
        assertTrue(splitTime < 700);
        assertTrue(totalTime >= 1500);
        assertTrue(totalTime < 1900);
    }

// org.apache.commons.lang.time.StopWatchTest::testStopWatchSuspend
    public void testStopWatchSuspend(){
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.suspend();
        long suspendTime = watch.getTime();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.resume();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.stop();
        long totalTime = watch.getTime();
        
        assertTrue(suspendTime >= 500);
        assertTrue(suspendTime < 700);
        assertTrue(totalTime >= 1000);
        assertTrue(totalTime < 1300);
    }

// org.apache.commons.lang.time.StopWatchTest::testBadStates
    public void testBadStates() {
        StopWatch watch = new StopWatch();
        try {
            watch.stop();
            fail("Calling stop on an unstarted StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.stop();
            fail("Calling stop on an unstarted StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.suspend();
            fail("Calling suspend on an unstarted StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.split();
            fail("Calling split on a non-running StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.unsplit();
            fail("Calling unsplit on an unsplit StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.resume();
            fail("Calling resume on an unsuspended StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        watch.start();

        try {
            watch.start();
            fail("Calling start on a started StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.unsplit();
            fail("Calling unsplit on an unsplit StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.getSplitTime();
            fail("Calling getSplitTime on an unsplit StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.resume();
            fail("Calling resume on an unsuspended StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        watch.stop();

        try {
            watch.start();
            fail("Calling start on a stopped StopWatch should throw an exception as it needs to be reset. ");
        } catch(IllegalStateException ise) {
            
        }

    }
