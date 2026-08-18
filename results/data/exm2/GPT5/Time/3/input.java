    public void add(DurationFieldType type, int amount) {
        if (type == null) {
            throw new IllegalArgumentException("Field must not be null");
        }
            setMillis(type.getField(getChronology()).add(getMillis(), amount));
    }

    public void addYears(final int years) {
            setMillis(getChronology().years().add(getMillis(), years));
    }

    public void addWeekyears(final int weekyears) {
            setMillis(getChronology().weekyears().add(getMillis(), weekyears));
    }

    public void addMonths(final int months) {
            setMillis(getChronology().months().add(getMillis(), months));
    }

    public void addWeeks(final int weeks) {
            setMillis(getChronology().weeks().add(getMillis(), weeks));
    }

    public void addDays(final int days) {
            setMillis(getChronology().days().add(getMillis(), days));
    }

    public void addHours(final int hours) {
            setMillis(getChronology().hours().add(getMillis(), hours));
    }

    public void addMinutes(final int minutes) {
            setMillis(getChronology().minutes().add(getMillis(), minutes));
    }

    public void addSeconds(final int seconds) {
            setMillis(getChronology().seconds().add(getMillis(), seconds));
    }

    public void addMillis(final int millis) {
            setMillis(getChronology().millis().add(getMillis(), millis));
    }

// trigger testcase
public void testAddDays_int_dstOverlapWinter_addZero() {
        MutableDateTime test = new MutableDateTime(2011, 10, 30, 2, 30, 0, 0, DateTimeZone.forID("Europe/Berlin"));
        test.addHours(1);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
        test.addDays(0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
    }

public void testAddMonths_int_dstOverlapWinter_addZero() {
        MutableDateTime test = new MutableDateTime(2011, 10, 30, 2, 30, 0, 0, DateTimeZone.forID("Europe/Berlin"));
        test.addHours(1);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
        test.addMonths(0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
    }

public void testAddWeeks_int_dstOverlapWinter_addZero() {
        MutableDateTime test = new MutableDateTime(2011, 10, 30, 2, 30, 0, 0, DateTimeZone.forID("Europe/Berlin"));
        test.addHours(1);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
        test.addWeeks(0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
    }

public void testAddYears_int_dstOverlapWinter_addZero() {
        MutableDateTime test = new MutableDateTime(2011, 10, 30, 2, 30, 0, 0, DateTimeZone.forID("Europe/Berlin"));
        test.addHours(1);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
        test.addYears(0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
    }

public void testAdd_DurationFieldType_int_dstOverlapWinter_addZero() {
        MutableDateTime test = new MutableDateTime(2011, 10, 30, 2, 30, 0, 0, DateTimeZone.forID("Europe/Berlin"));
        test.addHours(1);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
        test.add(DurationFieldType.years(), 0);
        assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
    }
