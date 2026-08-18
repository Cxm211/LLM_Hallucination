// org/apache/commons/lang/time/DurationFormatUtilsTest.java
public void testFormatPeriodFebruaryLeapYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.set(Calendar.YEAR, 2008);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.MONTH, Calendar.MARCH);
        cal2.set(Calendar.DAY_OF_MONTH, 1);
        cal2.set(Calendar.YEAR, 2008);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        String result = DurationFormatUtils.formatPeriod(cal.getTime().getTime(), cal2.getTime().getTime(), "MM");
        assertEquals("01", result);
    }