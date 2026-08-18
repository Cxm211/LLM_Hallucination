// org/apache/commons/lang3/time/FastDatePrinterTest.java
@Test
    public void testDSTTimezoneInDST() {
        TimeZone dstZone = TimeZone.getTimeZone("America/New_York");
        Calendar cal = Calendar.getInstance(dstZone);
        cal.set(2020, Calendar.JULY, 1, 12, 0, 0);
        assertTrue("Should be in DST", cal.get(Calendar.DST_OFFSET) != 0);
        
        final String pattern = "h:mma z";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(dstZone);
        String expectedValue = sdf.format(cal.getTime());
        String actualValue = FastDateFormat.getInstance(pattern).format(cal);
        assertEquals(expectedValue, actualValue);
    }
