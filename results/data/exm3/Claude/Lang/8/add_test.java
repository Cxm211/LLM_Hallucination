// org/apache/commons/lang3/time/FastDatePrinterTest.java
@Test
    public void testCalendarTimezoneRespectedWithDST() {
        TimeZone dstZone = TimeZone.getTimeZone("America/New_York");
        final String pattern = "h:mma z";
        final Calendar cal = Calendar.getInstance(dstZone);
        cal.set(Calendar.MONTH, Calendar.JULY);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(dstZone);
        String expectedValue = sdf.format(cal.getTime());
        String actualValue = FastDateFormat.getInstance(pattern).format(cal);
        assertEquals(expectedValue, actualValue);
    }