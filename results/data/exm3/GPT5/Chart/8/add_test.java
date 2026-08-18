// org/jfree/data/time/junit/WeekTests.java
public void testConstructor_TimeZoneRespected() {
        Locale savedLocale = Locale.getDefault();
        TimeZone savedZone = TimeZone.getDefault();
        try {
            Locale.setDefault(Locale.US);
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance(
                    TimeZone.getDefault(), Locale.getDefault());
            // Sunday in UTC
            cal.set(2007, Calendar.AUGUST, 26, 0, 30, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date t = cal.getTime();
            // In Honolulu, this instant is Saturday, so the week should be the previous one
            Week w = new Week(t, TimeZone.getTimeZone("Pacific/Honolulu"));
            assertEquals(34, w.getWeek());
        } finally {
            Locale.setDefault(savedLocale);
            TimeZone.setDefault(savedZone);
        }
    }