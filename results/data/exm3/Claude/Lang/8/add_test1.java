// org/apache/commons/lang3/time/FastDatePrinterTest.java
@Test
    public void testCalendarTimezoneRespectedWithoutDST() {
        TimeZone noDstZone = TimeZone.getTimeZone("UTC");
        final String pattern = "h:mma z";
        final Calendar cal = Calendar.getInstance(noDstZone);
        
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(noDstZone);
        String expectedValue = sdf.format(cal.getTime());
        String actualValue = FastDateFormat.getInstance(pattern).format(cal);
        assertEquals(expectedValue, actualValue);
    }