// buggy function
    public static LocalDate fromCalendarFields(Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("The calendar must not be null");
        }
        int yearOfEra = calendar.get(Calendar.YEAR);
        return new LocalDate(
            yearOfEra,
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    public static LocalDate fromDateFields(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
            // handle years in era BC
        return new LocalDate(
            date.getYear() + 1900,
            date.getMonth() + 1,
            date.getDate()
        );
    }

    public static LocalDateTime fromCalendarFields(Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("The calendar must not be null");
        }
        int yearOfEra = calendar.get(Calendar.YEAR);
        return new LocalDateTime(
            yearOfEra,
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND),
            calendar.get(Calendar.MILLISECOND)
        );
    }

    public static LocalDateTime fromDateFields(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
            // handle years in era BC
        return new LocalDateTime(
            date.getYear() + 1900,
            date.getMonth() + 1,
            date.getDate(),
            date.getHours(),
            date.getMinutes(),
            date.getSeconds(),
            (((int) (date.getTime() % 1000)) + 1000) % 1000
        );
    }

// trigger testcase
// org/joda/time/TestLocalDateTime_Constructors.java::testFactory_fromCalendarFields_beforeYearZero1
public void testFactory_fromCalendarFields_beforeYearZero1() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime expected = new LocalDateTime(0, 2, 3, 4, 5, 6, 7);
        assertEquals(expected, LocalDateTime.fromCalendarFields(cal));
    }

// org/joda/time/TestLocalDateTime_Constructors.java::testFactory_fromCalendarFields_beforeYearZero3
public void testFactory_fromCalendarFields_beforeYearZero3() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(3, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime expected = new LocalDateTime(-2, 2, 3, 4, 5, 6, 7);
        assertEquals(expected, LocalDateTime.fromCalendarFields(cal));
    }

// org/joda/time/TestLocalDateTime_Constructors.java::testFactory_fromDateFields_beforeYearZero1
public void testFactory_fromDateFields_beforeYearZero1() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime expected = new LocalDateTime(0, 2, 3, 4, 5, 6, 7);
        assertEquals(expected, LocalDateTime.fromDateFields(cal.getTime()));
    }

// org/joda/time/TestLocalDateTime_Constructors.java::testFactory_fromDateFields_beforeYearZero3
public void testFactory_fromDateFields_beforeYearZero3() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(3, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDateTime expected = new LocalDateTime(-2, 2, 3, 4, 5, 6, 7);
        assertEquals(expected, LocalDateTime.fromDateFields(cal.getTime()));
    }

// org/joda/time/TestLocalDate_Constructors.java::testFactory_fromCalendarFields_beforeYearZero1
public void testFactory_fromCalendarFields_beforeYearZero1() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDate expected = new LocalDate(0, 2, 3);
        assertEquals(expected, LocalDate.fromCalendarFields(cal));
    }

// org/joda/time/TestLocalDate_Constructors.java::testFactory_fromCalendarFields_beforeYearZero3
public void testFactory_fromCalendarFields_beforeYearZero3() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(3, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDate expected = new LocalDate(-2, 2, 3);
        assertEquals(expected, LocalDate.fromCalendarFields(cal));
    }

// org/joda/time/TestLocalDate_Constructors.java::testFactory_fromDateFields_beforeYearZero1
public void testFactory_fromDateFields_beforeYearZero1() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDate expected = new LocalDate(0, 2, 3);
        assertEquals(expected, LocalDate.fromDateFields(cal.getTime()));
    }

// org/joda/time/TestLocalDate_Constructors.java::testFactory_fromDateFields_beforeYearZero3
public void testFactory_fromDateFields_beforeYearZero3() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(3, 1, 3, 4, 5, 6);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.MILLISECOND, 7);
        LocalDate expected = new LocalDate(-2, 2, 3);
        assertEquals(expected, LocalDate.fromDateFields(cal.getTime()));
    }
