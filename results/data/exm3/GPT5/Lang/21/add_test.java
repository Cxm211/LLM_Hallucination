// org/apache/commons/lang3/time/DateUtilsTest.java::testIsSameLocalTime_Cal
        // Additional check: 00:10 vs 12:10 should not be the same local time
        Calendar cal5 = Calendar.getInstance();
        Calendar cal6 = Calendar.getInstance();
        cal5.set(2020, 0, 1, 0, 10, 0);
        cal6.set(2020, 0, 1, 12, 10, 0);
        cal5.set(Calendar.MILLISECOND, 0);
        cal6.set(Calendar.MILLISECOND, 0);
        assertFalse(DateUtils.isSameLocalTime(cal5, cal6));
