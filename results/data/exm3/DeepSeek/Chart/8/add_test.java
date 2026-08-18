// org/jfree/data/time/junit/WeekTests.java
public void testConstructorWithExplicitTimeZone() {
        // Use a date near week boundary with timezone shift
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2007, Calendar.DECEMBER, 31, 23, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        TimeZone zone = TimeZone.getTimeZone("Asia/Tokyo");
        Week weekTwoArg = new Week(date, zone);
        Week weekThreeArg = new Week(date, zone, Locale.getDefault());
        assertEquals(weekThreeArg.getWeek(), weekTwoArg.getWeek());
    }
