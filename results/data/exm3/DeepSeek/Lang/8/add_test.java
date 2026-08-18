// org/apache/commons/lang3/time/FastDatePrinterTest.java
@Test
    public void testNonDSTTimezone() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        assertFalse("UTC should not use DST", utc.useDaylightTime());
        
        final String pattern = "h:mma z";
        final Calendar cal = Calendar.getInstance(utc);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(utc);
        String expectedValue = sdf.format(cal.getTime());
        String actualValue = FastDateFormat.getInstance(pattern).format(cal);
        assertEquals(expectedValue, actualValue);
    }
