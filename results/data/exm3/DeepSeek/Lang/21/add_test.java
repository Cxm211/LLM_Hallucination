// org/apache/commons/lang3/time/DateUtilsTest.java
public void testIsSameLocalTime_HourBug() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2004, 6, 9, 0, 45, 0);
        cal2.set(2004, 6, 9, 12, 45, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        assertFalse("Midnight and noon should not be same local time", DateUtils.isSameLocalTime(cal1, cal2));
    }
