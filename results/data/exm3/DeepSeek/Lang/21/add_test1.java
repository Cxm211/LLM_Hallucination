// org/apache/commons/lang3/time/DateUtilsTest.java
public void testIsSameLocalTime_DifferentClass() {
        class MyCalendar extends GregorianCalendar {}
        Calendar cal1 = new GregorianCalendar();
        Calendar cal2 = new MyCalendar();
        cal1.set(2004, 6, 9, 13, 45, 0);
        cal2.set(2004, 6, 9, 13, 45, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        assertTrue("Different Calendar subclasses with same local time should be equal", DateUtils.isSameLocalTime(cal1, cal2));
    }
