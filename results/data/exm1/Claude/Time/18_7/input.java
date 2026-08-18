// buggy code
    public long getDateTimeMillis(int year, int monthOfYear, int dayOfMonth,
                                  int hourOfDay, int minuteOfHour,
                                  int secondOfMinute, int millisOfSecond)
        throws IllegalArgumentException
    {
        Chronology base;
        if ((base = getBase()) != null) {
            return base.getDateTimeMillis
                (year, monthOfYear, dayOfMonth,
                 hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
        }

        // Assume date is Gregorian.
        long instant;
            instant = iGregorianChronology.getDateTimeMillis
                (year, monthOfYear, dayOfMonth,
                 hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
        if (instant < iCutoverMillis) {
            // Maybe it's Julian.
            instant = iJulianChronology.getDateTimeMillis
                (year, monthOfYear, dayOfMonth,
                 hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond);
            if (instant >= iCutoverMillis) {
                // Okay, it's in the illegal cutover gap.
                throw new IllegalArgumentException("Specified date does not exist");
            }
        }
        return instant;
    }

// relevant test
// org.joda.time.chrono.TestISOChronology::testAddMonths
    public void testAddMonths() {
        testAdd("1582-01-01", DurationFieldType.months(), 1, "1582-02-01");
        testAdd("1582-01-01", DurationFieldType.months(), 6, "1582-07-01");
        testAdd("1582-01-01", DurationFieldType.months(), 12, "1583-01-01");
        testAdd("1582-11-15", DurationFieldType.months(), 1, "1582-12-15");
        testAdd("1582-09-04", DurationFieldType.months(), 2, "1582-11-04");
        testAdd("1582-09-05", DurationFieldType.months(), 2, "1582-11-05");
        testAdd("1582-09-10", DurationFieldType.months(), 2, "1582-11-10");
        testAdd("1582-09-15", DurationFieldType.months(), 2, "1582-11-15");
        testAdd("1580-01-01", DurationFieldType.months(), 48, "1584-01-01");
        testAdd("1580-02-29", DurationFieldType.months(), 48, "1584-02-29");
        testAdd("1580-10-01", DurationFieldType.months(), 48, "1584-10-01");
        testAdd("1580-10-10", DurationFieldType.months(), 48, "1584-10-10");
        testAdd("1580-10-15", DurationFieldType.months(), 48, "1584-10-15");
        testAdd("1580-12-31", DurationFieldType.months(), 48, "1584-12-31");
    }

// org.joda.time.chrono.TestISOChronology::testTimeOfDayAdd
    public void testTimeOfDayAdd() {
        TimeOfDay start = new TimeOfDay(12, 30);
        TimeOfDay end = new TimeOfDay(10, 30);
        assertEquals(end, start.plusHours(22));
        assertEquals(start, end.minusHours(22));
        assertEquals(end, start.plusMinutes(22 * 60));
        assertEquals(start, end.minusMinutes(22 * 60));
    }

// org.joda.time.chrono.TestISOChronology::testPartialDayOfYearAdd
    public void testPartialDayOfYearAdd() {
        Partial start = new Partial().with(DateTimeFieldType.year(), 2000).with(DateTimeFieldType.dayOfYear(), 366);
        Partial end = new Partial().with(DateTimeFieldType.year(), 2004).with(DateTimeFieldType.dayOfYear(), 366);
        assertEquals(end, start.withFieldAdded(DurationFieldType.days(), 365 + 365 + 365 + 366));
        assertEquals(start, end.withFieldAdded(DurationFieldType.days(), -(365 + 365 + 365 + 366)));
    }

// org.joda.time.chrono.TestISOChronology::testMaximumValue
    public void testMaximumValue() {
        DateMidnight dt = new DateMidnight(1570, 1, 1);
        while (dt.getYear() < 1590) {
            dt = dt.plusDays(1);
            YearMonthDay ymd = dt.toYearMonthDay();
            assertEquals(dt.year().getMaximumValue(), ymd.year().getMaximumValue());
            assertEquals(dt.monthOfYear().getMaximumValue(), ymd.monthOfYear().getMaximumValue());
            assertEquals(dt.dayOfMonth().getMaximumValue(), ymd.dayOfMonth().getMaximumValue());
        }
    }

// org.joda.time.convert.TestCalendarConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = CalendarConverter.class;
        assertEquals(false, Modifier.isPublic(cls.getModifiers()));
        assertEquals(false, Modifier.isProtected(cls.getModifiers()));
        assertEquals(false, Modifier.isPrivate(cls.getModifiers()));
        
        Constructor con = cls.getDeclaredConstructor((Class[]) null);
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(con.getModifiers()));
        
        Field fld = cls.getDeclaredField("INSTANCE");
        assertEquals(false, Modifier.isPublic(fld.getModifiers()));
        assertEquals(false, Modifier.isProtected(fld.getModifiers()));
        assertEquals(false, Modifier.isPrivate(fld.getModifiers()));
    }

// org.joda.time.convert.TestCalendarConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(Calendar.class, CalendarConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestCalendarConverter::testGetInstantMillis_Object_Chronology
    public void testGetInstantMillis_Object_Chronology() throws Exception {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(123L));
        assertEquals(123L, CalendarConverter.INSTANCE.getInstantMillis(cal, JULIAN));
        assertEquals(123L, cal.getTime().getTime());
    }

// org.joda.time.convert.TestCalendarConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Paris"));
        assertEquals(GJChronology.getInstance(MOSCOW), CalendarConverter.INSTANCE.getChronology(cal, MOSCOW));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        assertEquals(GJChronology.getInstance(), CalendarConverter.INSTANCE.getChronology(cal, (DateTimeZone) null));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        cal.setGregorianChange(new Date(0L));
        assertEquals(GJChronology.getInstance(MOSCOW, 0L, 4), CalendarConverter.INSTANCE.getChronology(cal, MOSCOW));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        cal.setGregorianChange(new Date(Long.MAX_VALUE));
        assertEquals(JulianChronology.getInstance(PARIS), CalendarConverter.INSTANCE.getChronology(cal, PARIS));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        cal.setGregorianChange(new Date(Long.MIN_VALUE));
        assertEquals(GregorianChronology.getInstance(PARIS), CalendarConverter.INSTANCE.getChronology(cal, PARIS));
        
        Calendar uc = new MockUnknownCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        assertEquals(ISOChronology.getInstance(PARIS), CalendarConverter.INSTANCE.getChronology(uc, PARIS));
        
        try {
            Calendar bc = (Calendar) Class.forName("sun.util.BuddhistCalendar").newInstance();
            bc.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
            assertEquals(BuddhistChronology.getInstance(PARIS), CalendarConverter.INSTANCE.getChronology(bc, PARIS));
        } catch (ClassNotFoundException ex) {
            
        }
    }

// org.joda.time.convert.TestCalendarConverter::testGetChronology_Object_nullChronology
    public void testGetChronology_Object_nullChronology() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Paris"));
        assertEquals(GJChronology.getInstance(PARIS), CalendarConverter.INSTANCE.getChronology(cal, (Chronology) null));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        cal.setGregorianChange(new Date(0L));
        assertEquals(GJChronology.getInstance(MOSCOW, 0L, 4), CalendarConverter.INSTANCE.getChronology(cal, (Chronology) null));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        cal.setGregorianChange(new Date(Long.MAX_VALUE));
        assertEquals(JulianChronology.getInstance(MOSCOW), CalendarConverter.INSTANCE.getChronology(cal, (Chronology) null));
        
        cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        cal.setGregorianChange(new Date(Long.MIN_VALUE));
        assertEquals(GregorianChronology.getInstance(MOSCOW), CalendarConverter.INSTANCE.getChronology(cal, (Chronology) null));
        
        cal = new GregorianCalendar(new MockUnknownTimeZone());
        assertEquals(GJChronology.getInstance(), CalendarConverter.INSTANCE.getChronology(cal, (Chronology) null));
        
        Calendar uc = new MockUnknownCalendar(TimeZone.getTimeZone("Europe/Moscow"));
        assertEquals(ISOChronology.getInstance(MOSCOW), CalendarConverter.INSTANCE.getChronology(uc, (Chronology) null));
        
        try {
            Calendar bc = (Calendar) Class.forName("sun.util.BuddhistCalendar").newInstance();
            bc.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
            assertEquals(BuddhistChronology.getInstance(MOSCOW), CalendarConverter.INSTANCE.getChronology(bc, (Chronology) null));
        } catch (ClassNotFoundException ex) {
            
        }
    }

// org.joda.time.convert.TestCalendarConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Paris"));
        assertEquals(JULIAN, CalendarConverter.INSTANCE.getChronology(cal, JULIAN));
    }

// org.joda.time.convert.TestCalendarConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(12345678L));
        TimeOfDay tod = new TimeOfDay();
        int[] expected = ISO.get(tod, 12345678L);
        int[] actual = CalendarConverter.INSTANCE.getPartialValues(tod, cal, ISO);
        assertEquals(true, Arrays.equals(expected, actual));
    }

// org.joda.time.convert.TestCalendarConverter::testToString
    public void testToString() {
        assertEquals("Converter[java.util.Calendar]", CalendarConverter.INSTANCE.toString());
    }

// org.joda.time.convert.TestConverterManager::testSingleton
    public void testSingleton() throws Exception {
        Class cls = ConverterManager.class;
        assertEquals(true, Modifier.isPublic(cls.getModifiers()));
        
        Constructor con = cls.getDeclaredConstructor((Class[]) null);
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(con.getModifiers()));
        
        Field fld = cls.getDeclaredField("INSTANCE");
        assertEquals(true, Modifier.isPrivate(fld.getModifiers()));
    }

// org.joda.time.convert.TestConverterManager::testGetInstantConverter
    public void testGetInstantConverter() {
        InstantConverter c = ConverterManager.getInstance().getInstantConverter(new Long(0L));
        assertEquals(Long.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getInstantConverter(new DateTime());
        assertEquals(ReadableInstant.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getInstantConverter("");
        assertEquals(String.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getInstantConverter(new Date());
        assertEquals(Date.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getInstantConverter(new GregorianCalendar());
        assertEquals(Calendar.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getInstantConverter(null);
        assertEquals(null, c.getSupportedType());
        
        try {
            ConverterManager.getInstance().getInstantConverter(Boolean.TRUE);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestConverterManager::testGetInstantConverterRemovedNull
    public void testGetInstantConverterRemovedNull() {
        try {
            ConverterManager.getInstance().removeInstantConverter(NullConverter.INSTANCE);
            try {
                ConverterManager.getInstance().getInstantConverter(null);
                fail();
            } catch (IllegalArgumentException ex) {}
        } finally {
            ConverterManager.getInstance().addInstantConverter(NullConverter.INSTANCE);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetInstantConverterOKMultipleMatches
    public void testGetInstantConverterOKMultipleMatches() {
        InstantConverter c = new InstantConverter() {
            public long getInstantMillis(Object object, Chronology chrono) {return 0;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return ReadableDateTime.class;}
        };
        try {
            ConverterManager.getInstance().addInstantConverter(c);
            InstantConverter ok = ConverterManager.getInstance().getInstantConverter(new DateTime());
            
            assertEquals(ReadableDateTime.class, ok.getSupportedType());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(c);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetInstantConverterBadMultipleMatches
    public void testGetInstantConverterBadMultipleMatches() {
        InstantConverter c = new InstantConverter() {
            public long getInstantMillis(Object object, Chronology chrono) {return 0;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return Serializable.class;}
        };
        try {
            ConverterManager.getInstance().addInstantConverter(c);
            try {
                ConverterManager.getInstance().getInstantConverter(new DateTime());
                fail();
            } catch (IllegalStateException ex) {
                
            }
        } finally {
            ConverterManager.getInstance().removeInstantConverter(c);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetInstantConverters
    public void testGetInstantConverters() {
        InstantConverter[] array = ConverterManager.getInstance().getInstantConverters();
        assertEquals(6, array.length);
    }

// org.joda.time.convert.TestConverterManager::testAddInstantConverter1
    public void testAddInstantConverter1() {
        InstantConverter c = new InstantConverter() {
            public long getInstantMillis(Object object, Chronology chrono) {return 0;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return Boolean.class;}
        };
        try {
            InstantConverter removed = ConverterManager.getInstance().addInstantConverter(c);
            assertEquals(null, removed);
            assertEquals(Boolean.class, ConverterManager.getInstance().getInstantConverter(Boolean.TRUE).getSupportedType());
            assertEquals(7, ConverterManager.getInstance().getInstantConverters().length);
        } finally {
            ConverterManager.getInstance().removeInstantConverter(c);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddInstantConverter2
    public void testAddInstantConverter2() {
        InstantConverter c = new InstantConverter() {
            public long getInstantMillis(Object object, Chronology chrono) {return 0;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return String.class;}
        };
        try {
            InstantConverter removed = ConverterManager.getInstance().addInstantConverter(c);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(String.class, ConverterManager.getInstance().getInstantConverter("").getSupportedType());
            assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
        } finally {
            ConverterManager.getInstance().addInstantConverter(StringConverter.INSTANCE);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddInstantConverter3
    public void testAddInstantConverter3() {
        InstantConverter removed = ConverterManager.getInstance().addInstantConverter(StringConverter.INSTANCE);
        assertEquals(null, removed);
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddInstantConverter4
    public void testAddInstantConverter4() {
        InstantConverter removed = ConverterManager.getInstance().addInstantConverter(null);
        assertEquals(null, removed);
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddInstantConverterSecurity
    public void testAddInstantConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().addInstantConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveInstantConverter1
    public void testRemoveInstantConverter1() {
        try {
            InstantConverter removed = ConverterManager.getInstance().removeInstantConverter(StringConverter.INSTANCE);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(5, ConverterManager.getInstance().getInstantConverters().length);
        } finally {
            ConverterManager.getInstance().addInstantConverter(StringConverter.INSTANCE);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveInstantConverter2
    public void testRemoveInstantConverter2() {
        InstantConverter c = new InstantConverter() {
            public long getInstantMillis(Object object, Chronology chrono) {return 0;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return Boolean.class;}
        };
        InstantConverter removed = ConverterManager.getInstance().removeInstantConverter(c);
        assertEquals(null, removed);
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveInstantConverter3
    public void testRemoveInstantConverter3() {
        InstantConverter removed = ConverterManager.getInstance().removeInstantConverter(null);
        assertEquals(null, removed);
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveInstantConverterSecurity
    public void testRemoveInstantConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().removeInstantConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetPartialConverter
    public void testGetPartialConverter() {
        PartialConverter c = ConverterManager.getInstance().getPartialConverter(new Long(0L));
        assertEquals(Long.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPartialConverter(new TimeOfDay());
        assertEquals(ReadablePartial.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPartialConverter(new DateTime());
        assertEquals(ReadableInstant.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPartialConverter("");
        assertEquals(String.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPartialConverter(new Date());
        assertEquals(Date.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPartialConverter(new GregorianCalendar());
        assertEquals(Calendar.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPartialConverter(null);
        assertEquals(null, c.getSupportedType());
        
        try {
            ConverterManager.getInstance().getPartialConverter(Boolean.TRUE);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestConverterManager::testGetPartialConverterRemovedNull
    public void testGetPartialConverterRemovedNull() {
        try {
            ConverterManager.getInstance().removePartialConverter(NullConverter.INSTANCE);
            try {
                ConverterManager.getInstance().getPartialConverter(null);
                fail();
            } catch (IllegalArgumentException ex) {}
        } finally {
            ConverterManager.getInstance().addPartialConverter(NullConverter.INSTANCE);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetPartialConverterOKMultipleMatches
    public void testGetPartialConverterOKMultipleMatches() {
        PartialConverter c = new PartialConverter() {
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {return null;}
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {return null;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return ReadableDateTime.class;}
        };
        try {
            ConverterManager.getInstance().addPartialConverter(c);
            PartialConverter ok = ConverterManager.getInstance().getPartialConverter(new DateTime());
            
            assertEquals(ReadableDateTime.class, ok.getSupportedType());
        } finally {
            ConverterManager.getInstance().removePartialConverter(c);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetPartialConverterBadMultipleMatches
    public void testGetPartialConverterBadMultipleMatches() {
        PartialConverter c = new PartialConverter() {
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {return null;}
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {return null;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return Serializable.class;}
        };
        try {
            ConverterManager.getInstance().addPartialConverter(c);
            try {
                ConverterManager.getInstance().getPartialConverter(new DateTime());
                fail();
            } catch (IllegalStateException ex) {
                
            }
        } finally {
            ConverterManager.getInstance().removePartialConverter(c);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetPartialConverters
    public void testGetPartialConverters() {
        PartialConverter[] array = ConverterManager.getInstance().getPartialConverters();
        assertEquals(PARTIAL_SIZE, array.length);
    }

// org.joda.time.convert.TestConverterManager::testAddPartialConverter1
    public void testAddPartialConverter1() {
        PartialConverter c = new PartialConverter() {
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {return null;}
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {return null;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return Boolean.class;}
        };
        try {
            PartialConverter removed = ConverterManager.getInstance().addPartialConverter(c);
            assertEquals(null, removed);
            assertEquals(Boolean.class, ConverterManager.getInstance().getPartialConverter(Boolean.TRUE).getSupportedType());
            assertEquals(PARTIAL_SIZE + 1, ConverterManager.getInstance().getPartialConverters().length);
        } finally {
            ConverterManager.getInstance().removePartialConverter(c);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPartialConverter2
    public void testAddPartialConverter2() {
        PartialConverter c = new PartialConverter() {
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {return null;}
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {return null;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return String.class;}
        };
        try {
            PartialConverter removed = ConverterManager.getInstance().addPartialConverter(c);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(String.class, ConverterManager.getInstance().getPartialConverter("").getSupportedType());
            assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
        } finally {
            ConverterManager.getInstance().addPartialConverter(StringConverter.INSTANCE);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPartialConverter3
    public void testAddPartialConverter3() {
        PartialConverter removed = ConverterManager.getInstance().addPartialConverter(StringConverter.INSTANCE);
        assertEquals(null, removed);
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPartialConverter4
    public void testAddPartialConverter4() {
        PartialConverter removed = ConverterManager.getInstance().addPartialConverter(null);
        assertEquals(null, removed);
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPartialConverterSecurity
    public void testAddPartialConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().addPartialConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePartialConverter1
    public void testRemovePartialConverter1() {
        try {
            PartialConverter removed = ConverterManager.getInstance().removePartialConverter(StringConverter.INSTANCE);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(PARTIAL_SIZE - 1, ConverterManager.getInstance().getPartialConverters().length);
        } finally {
            ConverterManager.getInstance().addPartialConverter(StringConverter.INSTANCE);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePartialConverter2
    public void testRemovePartialConverter2() {
        PartialConverter c = new PartialConverter() {
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {return null;}
            public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {return null;}
            public Chronology getChronology(Object object, DateTimeZone zone) {return null;}
            public Chronology getChronology(Object object, Chronology chrono) {return null;}
            public Class getSupportedType() {return Boolean.class;}
        };
        PartialConverter removed = ConverterManager.getInstance().removePartialConverter(c);
        assertEquals(null, removed);
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePartialConverter3
    public void testRemovePartialConverter3() {
        PartialConverter removed = ConverterManager.getInstance().removePartialConverter(null);
        assertEquals(null, removed);
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePartialConverterSecurity
    public void testRemovePartialConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().removeInstantConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetDurationConverter
    public void testGetDurationConverter() {
        DurationConverter c = ConverterManager.getInstance().getDurationConverter(new Long(0L));
        assertEquals(Long.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getDurationConverter(new Duration(123L));
        assertEquals(ReadableDuration.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getDurationConverter(new Interval(0L, 1000L));
        assertEquals(ReadableInterval.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getDurationConverter("");
        assertEquals(String.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getDurationConverter(null);
        assertEquals(null, c.getSupportedType());
        
        try {
            ConverterManager.getInstance().getDurationConverter(Boolean.TRUE);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestConverterManager::testGetDurationConverterRemovedNull
    public void testGetDurationConverterRemovedNull() {
        try {
            ConverterManager.getInstance().removeDurationConverter(NullConverter.INSTANCE);
            try {
                ConverterManager.getInstance().getDurationConverter(null);
                fail();
            } catch (IllegalArgumentException ex) {}
        } finally {
            ConverterManager.getInstance().addDurationConverter(NullConverter.INSTANCE);
        }
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetDurationConverters
    public void testGetDurationConverters() {
        DurationConverter[] array = ConverterManager.getInstance().getDurationConverters();
        assertEquals(DURATION_SIZE, array.length);
    }

// org.joda.time.convert.TestConverterManager::testAddDurationConverter1
    public void testAddDurationConverter1() {
        DurationConverter c = new DurationConverter() {
            public long getDurationMillis(Object object) {return 0;}
            public Class getSupportedType() {return Boolean.class;}
        };
        try {
            DurationConverter removed = ConverterManager.getInstance().addDurationConverter(c);
            assertEquals(null, removed);
            assertEquals(Boolean.class, ConverterManager.getInstance().getDurationConverter(Boolean.TRUE).getSupportedType());
            assertEquals(DURATION_SIZE + 1, ConverterManager.getInstance().getDurationConverters().length);
        } finally {
            ConverterManager.getInstance().removeDurationConverter(c);
        }
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddDurationConverter2
    public void testAddDurationConverter2() {
        DurationConverter c = new DurationConverter() {
            public long getDurationMillis(Object object) {return 0;}
            public Class getSupportedType() {return String.class;}
        };
        try {
            DurationConverter removed = ConverterManager.getInstance().addDurationConverter(c);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(String.class, ConverterManager.getInstance().getDurationConverter("").getSupportedType());
            assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
        } finally {
            ConverterManager.getInstance().addDurationConverter(StringConverter.INSTANCE);
        }
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddDurationConverter3
    public void testAddDurationConverter3() {
        DurationConverter removed = ConverterManager.getInstance().addDurationConverter(null);
        assertEquals(null, removed);
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddDurationConverterSecurity
    public void testAddDurationConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().addDurationConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveDurationConverter1
    public void testRemoveDurationConverter1() {
        try {
            DurationConverter removed = ConverterManager.getInstance().removeDurationConverter(StringConverter.INSTANCE);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(DURATION_SIZE - 1, ConverterManager.getInstance().getDurationConverters().length);
        } finally {
            ConverterManager.getInstance().addDurationConverter(StringConverter.INSTANCE);
        }
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveDurationConverter2
    public void testRemoveDurationConverter2() {
        DurationConverter c = new DurationConverter() {
            public long getDurationMillis(Object object) {return 0;}
            public Class getSupportedType() {return Boolean.class;}
        };
        DurationConverter removed = ConverterManager.getInstance().removeDurationConverter(c);
        assertEquals(null, removed);
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveDurationConverter3
    public void testRemoveDurationConverter3() {
        DurationConverter removed = ConverterManager.getInstance().removeDurationConverter(null);
        assertEquals(null, removed);
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveDurationConverterSecurity
    public void testRemoveDurationConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().removeDurationConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetPeriodConverter
    public void testGetPeriodConverter() {
        PeriodConverter c = ConverterManager.getInstance().getPeriodConverter(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        assertEquals(ReadablePeriod.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPeriodConverter(new Duration(123L));
        assertEquals(ReadableDuration.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPeriodConverter(new Interval(0L, 1000L));
        assertEquals(ReadableInterval.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPeriodConverter("");
        assertEquals(String.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getPeriodConverter(null);
        assertEquals(null, c.getSupportedType());
        
        try {
            ConverterManager.getInstance().getPeriodConverter(Boolean.TRUE);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestConverterManager::testGetPeriodConverterRemovedNull
    public void testGetPeriodConverterRemovedNull() {
        try {
            ConverterManager.getInstance().removePeriodConverter(NullConverter.INSTANCE);
            try {
                ConverterManager.getInstance().getPeriodConverter(null);
                fail();
            } catch (IllegalArgumentException ex) {}
        } finally {
            ConverterManager.getInstance().addPeriodConverter(NullConverter.INSTANCE);
        }
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetPeriodConverters
    public void testGetPeriodConverters() {
        PeriodConverter[] array = ConverterManager.getInstance().getPeriodConverters();
        assertEquals(PERIOD_SIZE, array.length);
    }

// org.joda.time.convert.TestConverterManager::testAddPeriodConverter1
    public void testAddPeriodConverter1() {
        PeriodConverter c = new PeriodConverter() {
            public void setInto(ReadWritablePeriod duration, Object object, Chronology c) {}
            public PeriodType getPeriodType(Object object) {return null;}
            public Class getSupportedType() {return Boolean.class;}
        };
        try {
            PeriodConverter removed = ConverterManager.getInstance().addPeriodConverter(c);
            assertEquals(null, removed);
            assertEquals(Boolean.class, ConverterManager.getInstance().getPeriodConverter(Boolean.TRUE).getSupportedType());
            assertEquals(PERIOD_SIZE + 1, ConverterManager.getInstance().getPeriodConverters().length);
        } finally {
            ConverterManager.getInstance().removePeriodConverter(c);
        }
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPeriodConverter2
    public void testAddPeriodConverter2() {
        PeriodConverter c = new PeriodConverter() {
            public void setInto(ReadWritablePeriod duration, Object object, Chronology c) {}
            public PeriodType getPeriodType(Object object) {return null;}
            public Class getSupportedType() {return String.class;}
        };
        try {
            PeriodConverter removed = ConverterManager.getInstance().addPeriodConverter(c);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(String.class, ConverterManager.getInstance().getPeriodConverter("").getSupportedType());
            assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
        } finally {
            ConverterManager.getInstance().addPeriodConverter(StringConverter.INSTANCE);
        }
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPeriodConverter3
    public void testAddPeriodConverter3() {
        PeriodConverter removed = ConverterManager.getInstance().addPeriodConverter(null);
        assertEquals(null, removed);
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddPeriodConverterSecurity
    public void testAddPeriodConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().addPeriodConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePeriodConverter1
    public void testRemovePeriodConverter1() {
        try {
            PeriodConverter removed = ConverterManager.getInstance().removePeriodConverter(StringConverter.INSTANCE);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(PERIOD_SIZE - 1, ConverterManager.getInstance().getPeriodConverters().length);
        } finally {
            ConverterManager.getInstance().addPeriodConverter(StringConverter.INSTANCE);
        }
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePeriodConverter2
    public void testRemovePeriodConverter2() {
        PeriodConverter c = new PeriodConverter() {
            public void setInto(ReadWritablePeriod duration, Object object, Chronology c) {}
            public PeriodType getPeriodType(Object object) {return null;}
            public Class getSupportedType() {return Boolean.class;}
        };
        PeriodConverter removed = ConverterManager.getInstance().removePeriodConverter(c);
        assertEquals(null, removed);
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePeriodConverter3
    public void testRemovePeriodConverter3() {
        PeriodConverter removed = ConverterManager.getInstance().removePeriodConverter(null);
        assertEquals(null, removed);
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemovePeriodConverterSecurity
    public void testRemovePeriodConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().removePeriodConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetIntervalConverter
    public void testGetIntervalConverter() {
        IntervalConverter c = ConverterManager.getInstance().getIntervalConverter(new Interval(0L, 1000L));
        assertEquals(ReadableInterval.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getIntervalConverter("");
        assertEquals(String.class, c.getSupportedType());
        
        c = ConverterManager.getInstance().getIntervalConverter(null);
        assertEquals(null, c.getSupportedType());
        
        try {
            ConverterManager.getInstance().getIntervalConverter(Boolean.TRUE);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ConverterManager.getInstance().getIntervalConverter(new Long(0));
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestConverterManager::testGetIntervalConverterRemovedNull
    public void testGetIntervalConverterRemovedNull() {
        try {
            ConverterManager.getInstance().removeIntervalConverter(NullConverter.INSTANCE);
            try {
                ConverterManager.getInstance().getIntervalConverter(null);
                fail();
            } catch (IllegalArgumentException ex) {}
        } finally {
            ConverterManager.getInstance().addIntervalConverter(NullConverter.INSTANCE);
        }
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testGetIntervalConverters
    public void testGetIntervalConverters() {
        IntervalConverter[] array = ConverterManager.getInstance().getIntervalConverters();
        assertEquals(INTERVAL_SIZE, array.length);
    }

// org.joda.time.convert.TestConverterManager::testAddIntervalConverter1
    public void testAddIntervalConverter1() {
        IntervalConverter c = new IntervalConverter() {
            public boolean isReadableInterval(Object object, Chronology chrono) {return false;}
            public void setInto(ReadWritableInterval interval, Object object, Chronology chrono) {}
            public Class getSupportedType() {return Boolean.class;}
        };
        try {
            IntervalConverter removed = ConverterManager.getInstance().addIntervalConverter(c);
            assertEquals(null, removed);
            assertEquals(Boolean.class, ConverterManager.getInstance().getIntervalConverter(Boolean.TRUE).getSupportedType());
            assertEquals(INTERVAL_SIZE + 1, ConverterManager.getInstance().getIntervalConverters().length);
        } finally {
            ConverterManager.getInstance().removeIntervalConverter(c);
        }
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddIntervalConverter2
    public void testAddIntervalConverter2() {
        IntervalConverter c = new IntervalConverter() {
            public boolean isReadableInterval(Object object, Chronology chrono) {return false;}
            public void setInto(ReadWritableInterval interval, Object object, Chronology chrono) {}
            public Class getSupportedType() {return String.class;}
        };
        try {
            IntervalConverter removed = ConverterManager.getInstance().addIntervalConverter(c);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(String.class, ConverterManager.getInstance().getIntervalConverter("").getSupportedType());
            assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
        } finally {
            ConverterManager.getInstance().addIntervalConverter(StringConverter.INSTANCE);
        }
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddIntervalConverter3
    public void testAddIntervalConverter3() {
        IntervalConverter removed = ConverterManager.getInstance().addIntervalConverter(null);
        assertEquals(null, removed);
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testAddIntervalConverterSecurity
    public void testAddIntervalConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().addIntervalConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveIntervalConverter1
    public void testRemoveIntervalConverter1() {
        try {
            IntervalConverter removed = ConverterManager.getInstance().removeIntervalConverter(StringConverter.INSTANCE);
            assertEquals(StringConverter.INSTANCE, removed);
            assertEquals(INTERVAL_SIZE - 1, ConverterManager.getInstance().getIntervalConverters().length);
        } finally {
            ConverterManager.getInstance().addIntervalConverter(StringConverter.INSTANCE);
        }
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveIntervalConverter2
    public void testRemoveIntervalConverter2() {
        IntervalConverter c = new IntervalConverter() {
            public boolean isReadableInterval(Object object, Chronology chrono) {return false;}
            public void setInto(ReadWritableInterval interval, Object object, Chronology chrono) {}
            public Class getSupportedType() {return Boolean.class;}
        };
        IntervalConverter removed = ConverterManager.getInstance().removeIntervalConverter(c);
        assertEquals(null, removed);
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveIntervalConverter3
    public void testRemoveIntervalConverter3() {
        IntervalConverter removed = ConverterManager.getInstance().removeIntervalConverter(null);
        assertEquals(null, removed);
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testRemoveIntervalConverterSecurity
    public void testRemoveIntervalConverterSecurity() {
        if (OLD_JDK) {
            return;
        }
        try {
            Policy.setPolicy(RESTRICT);
            System.setSecurityManager(new SecurityManager());
            ConverterManager.getInstance().removeIntervalConverter(StringConverter.INSTANCE);
            fail();
        } catch (SecurityException ex) {
            
        } finally {
            System.setSecurityManager(null);
            Policy.setPolicy(ALLOW);
        }
        assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    }

// org.joda.time.convert.TestConverterManager::testToString
    public void testToString() {
        assertEquals("ConverterManager[6 instant,7 partial,5 duration,5 period,3 interval]", ConverterManager.getInstance().toString());
    }

// org.joda.time.convert.TestNullConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = NullConverter.class;
        assertEquals(false, Modifier.isPublic(cls.getModifiers()));
        assertEquals(false, Modifier.isProtected(cls.getModifiers()));
        assertEquals(false, Modifier.isPrivate(cls.getModifiers()));
        
        Constructor con = cls.getDeclaredConstructor((Class[]) null);
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(con.getModifiers()));
        
        Field fld = cls.getDeclaredField("INSTANCE");
        assertEquals(false, Modifier.isPublic(fld.getModifiers()));
        assertEquals(false, Modifier.isProtected(fld.getModifiers()));
        assertEquals(false, Modifier.isPrivate(fld.getModifiers()));
    }

// org.joda.time.convert.TestNullConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(null, NullConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestNullConverter::testGetInstantMillis_Object_Chronology
    public void testGetInstantMillis_Object_Chronology() throws Exception {
        assertEquals(TEST_TIME_NOW, NullConverter.INSTANCE.getInstantMillis(null, JULIAN));
        assertEquals(TEST_TIME_NOW, NullConverter.INSTANCE.getInstantMillis(null, (Chronology) null));
    }

// org.joda.time.convert.TestNullConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        assertEquals(ISO_PARIS, NullConverter.INSTANCE.getChronology(null, PARIS));
        assertEquals(ISO, NullConverter.INSTANCE.getChronology(null, (DateTimeZone) null));
    }

// org.joda.time.convert.TestNullConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        assertEquals(JULIAN, NullConverter.INSTANCE.getChronology(null, JULIAN));
        assertEquals(ISO, NullConverter.INSTANCE.getChronology(null, (Chronology) null));
    }

// org.joda.time.convert.TestNullConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        TimeOfDay tod = new TimeOfDay();
        int[] expected = new int[] {10 + 1, 20, 30, 40}; 
        int[] actual = NullConverter.INSTANCE.getPartialValues(tod, null, ISOChronology.getInstance());
        assertEquals(true, Arrays.equals(expected, actual));
    }

// org.joda.time.convert.TestNullConverter::testGetDurationMillis_Object
    public void testGetDurationMillis_Object() throws Exception {
        assertEquals(0L, NullConverter.INSTANCE.getDurationMillis(null));
    }

// org.joda.time.convert.TestNullConverter::testGetPeriodType_Object
    public void testGetPeriodType_Object() throws Exception {
        assertEquals(PeriodType.standard(),
            NullConverter.INSTANCE.getPeriodType(null));
    }

// org.joda.time.convert.TestNullConverter::testSetInto_Object
    public void testSetInto_Object() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.millis());
        NullConverter.INSTANCE.setInto(m, null, null);
        assertEquals(0L, m.getMillis());
    }

// org.joda.time.convert.TestNullConverter::testIsReadableInterval_Object_Chronology
    public void testIsReadableInterval_Object_Chronology() throws Exception {
        assertEquals(false, NullConverter.INSTANCE.isReadableInterval(null, null));
    }

// org.joda.time.convert.TestNullConverter::testSetInto_Object_Chronology1
    public void testSetInto_Object_Chronology1() throws Exception {
        MutableInterval m = new MutableInterval(1000L, 2000L, GJChronology.getInstance());
        NullConverter.INSTANCE.setInto(m, null, null);
        assertEquals(TEST_TIME_NOW, m.getStartMillis());
        assertEquals(TEST_TIME_NOW, m.getEndMillis());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestNullConverter::testSetInto_Object_Chronology2
    public void testSetInto_Object_Chronology2() throws Exception {
        MutableInterval m = new MutableInterval(1000L, 2000L, GJChronology.getInstance());
        NullConverter.INSTANCE.setInto(m, null, CopticChronology.getInstance());
        assertEquals(TEST_TIME_NOW, m.getStartMillis());
        assertEquals(TEST_TIME_NOW, m.getEndMillis());
        assertEquals(CopticChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestNullConverter::testToString
    public void testToString() {
        assertEquals("Converter[null]", NullConverter.INSTANCE.toString());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = ReadableIntervalConverter.class;
        assertEquals(false, Modifier.isPublic(cls.getModifiers()));
        assertEquals(false, Modifier.isProtected(cls.getModifiers()));
        assertEquals(false, Modifier.isPrivate(cls.getModifiers()));
        
        Constructor con = cls.getDeclaredConstructor((Class[]) null);
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(con.getModifiers()));
        
        Field fld = cls.getDeclaredField("INSTANCE");
        assertEquals(false, Modifier.isPublic(fld.getModifiers()));
        assertEquals(false, Modifier.isProtected(fld.getModifiers()));
        assertEquals(false, Modifier.isPrivate(fld.getModifiers()));
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(ReadableInterval.class, ReadableIntervalConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testGetDurationMillis_Object
    public void testGetDurationMillis_Object() throws Exception {
        Interval i = new Interval(100L, 223L);
        assertEquals(123L, ReadableIntervalConverter.INSTANCE.getDurationMillis(i));
    }

// org.joda.time.convert.TestReadableIntervalConverter::testGetPeriodType_Object
    public void testGetPeriodType_Object() throws Exception {
        Interval i = new Interval(100L, 223L);
        assertEquals(PeriodType.standard(),
            ReadableIntervalConverter.INSTANCE.getPeriodType(i));
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSetIntoPeriod_Object1
    public void testSetIntoPeriod_Object1() throws Exception {
        Interval i = new Interval(100L, 223L);
        MutablePeriod m = new MutablePeriod(PeriodType.millis());
        ReadableIntervalConverter.INSTANCE.setInto(m, i, null);
        assertEquals(0, m.getYears());
        assertEquals(0, m.getMonths());
        assertEquals(0, m.getWeeks());
        assertEquals(0, m.getDays());
        assertEquals(0, m.getHours());
        assertEquals(0, m.getMinutes());
        assertEquals(0, m.getSeconds());
        assertEquals(123, m.getMillis());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSetIntoPeriod_Object2
    public void testSetIntoPeriod_Object2() throws Exception {
        Interval i = new Interval(100L, 223L);
        MutablePeriod m = new MutablePeriod(PeriodType.millis());
        ReadableIntervalConverter.INSTANCE.setInto(m, i, CopticChronology.getInstance());
        assertEquals(0, m.getYears());
        assertEquals(0, m.getMonths());
        assertEquals(0, m.getWeeks());
        assertEquals(0, m.getDays());
        assertEquals(0, m.getHours());
        assertEquals(0, m.getMinutes());
        assertEquals(0, m.getSeconds());
        assertEquals(123, m.getMillis());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testIsReadableInterval_Object_Chronology
    public void testIsReadableInterval_Object_Chronology() throws Exception {
        Interval i = new Interval(1234L, 5678L);
        assertEquals(true, ReadableIntervalConverter.INSTANCE.isReadableInterval(i, null));
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSetIntoInterval_Object1
    public void testSetIntoInterval_Object1() throws Exception {
        Interval i = new Interval(0L, 123L, CopticChronology.getInstance());
        MutableInterval m = new MutableInterval(-1000L, 1000L, BuddhistChronology.getInstance());
        ReadableIntervalConverter.INSTANCE.setInto(m, i, null);
        assertEquals(0L, m.getStartMillis());
        assertEquals(123L, m.getEndMillis());
        assertEquals(CopticChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSetIntoInterval_Object2
    public void testSetIntoInterval_Object2() throws Exception {
        Interval i = new Interval(0L, 123L, CopticChronology.getInstance());
        MutableInterval m = new MutableInterval(-1000L, 1000L, BuddhistChronology.getInstance());
        ReadableIntervalConverter.INSTANCE.setInto(m, i, GJChronology.getInstance());
        assertEquals(0L, m.getStartMillis());
        assertEquals(123L, m.getEndMillis());
        assertEquals(GJChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSetIntoInterval_Object3
    public void testSetIntoInterval_Object3() throws Exception {
        MutableInterval i = new MutableInterval(0L, 123L) {
            public Chronology getChronology() {
                return null; 
            }
        };
        MutableInterval m = new MutableInterval(-1000L, 1000L, BuddhistChronology.getInstance());
        ReadableIntervalConverter.INSTANCE.setInto(m, i, GJChronology.getInstance());
        assertEquals(0L, m.getStartMillis());
        assertEquals(123L, m.getEndMillis());
        assertEquals(GJChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testSetIntoInterval_Object4
    public void testSetIntoInterval_Object4() throws Exception {
        MutableInterval i = new MutableInterval(0L, 123L) {
            public Chronology getChronology() {
                return null; 
            }
        };
        MutableInterval m = new MutableInterval(-1000L, 1000L, BuddhistChronology.getInstance());
        ReadableIntervalConverter.INSTANCE.setInto(m, i, null);
        assertEquals(0L, m.getStartMillis());
        assertEquals(123L, m.getEndMillis());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestReadableIntervalConverter::testToString
    public void testToString() {
        assertEquals("Converter[org.joda.time.ReadableInterval]", ReadableIntervalConverter.INSTANCE.toString());
    }

// org.joda.time.convert.TestReadablePartialConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = ReadablePartialConverter.class;
        assertEquals(false, Modifier.isPublic(cls.getModifiers()));
        assertEquals(false, Modifier.isProtected(cls.getModifiers()));
        assertEquals(false, Modifier.isPrivate(cls.getModifiers()));
        
        Constructor con = cls.getDeclaredConstructor((Class[]) null);
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(con.getModifiers()));
        
        Field fld = cls.getDeclaredField("INSTANCE");
        assertEquals(false, Modifier.isPublic(fld.getModifiers()));
        assertEquals(false, Modifier.isProtected(fld.getModifiers()));
        assertEquals(false, Modifier.isPrivate(fld.getModifiers()));
    }

// org.joda.time.convert.TestReadablePartialConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(ReadablePartial.class, ReadablePartialConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestReadablePartialConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        assertEquals(ISO_PARIS, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L), PARIS));
        assertEquals(ISO, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L), DateTimeZone.getDefault()));
        assertEquals(ISO, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L), (DateTimeZone) null));
    }

// org.joda.time.convert.TestReadablePartialConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        assertEquals(JULIAN, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L, BUDDHIST), JULIAN));
        assertEquals(JULIAN, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L), JULIAN));
        assertEquals(BUDDHIST.withUTC(), ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L, BUDDHIST), (Chronology) null));
    }

// org.joda.time.convert.TestReadablePartialConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        TimeOfDay tod = new TimeOfDay();
        int[] expected = new int[] {1, 2, 3, 4};
        int[] actual = ReadablePartialConverter.INSTANCE.getPartialValues(tod, new TimeOfDay(1, 2, 3, 4), ISOChronology.getInstance(PARIS));
        assertEquals(true, Arrays.equals(expected, actual));
        
        try {
            ReadablePartialConverter.INSTANCE.getPartialValues(tod, new YearMonthDay(2005, 6, 9), JULIAN);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ReadablePartialConverter.INSTANCE.getPartialValues(tod, new MockTOD(), JULIAN);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestReadablePartialConverter::testToString
    public void testToString() {
        assertEquals("Converter[org.joda.time.ReadablePartial]", ReadablePartialConverter.INSTANCE.toString());
    }

// org.joda.time.convert.TestStringConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = StringConverter.class;
        assertEquals(false, Modifier.isPublic(cls.getModifiers()));
        assertEquals(false, Modifier.isProtected(cls.getModifiers()));
        assertEquals(false, Modifier.isPrivate(cls.getModifiers()));
        
        Constructor con = cls.getDeclaredConstructor((Class[]) null);
        assertEquals(1, cls.getDeclaredConstructors().length);
        assertEquals(true, Modifier.isProtected(con.getModifiers()));
        
        Field fld = cls.getDeclaredField("INSTANCE");
        assertEquals(false, Modifier.isPublic(fld.getModifiers()));
        assertEquals(false, Modifier.isProtected(fld.getModifiers()));
        assertEquals(false, Modifier.isPrivate(fld.getModifiers()));
    }

// org.joda.time.convert.TestStringConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(String.class, StringConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestStringConverter::testGetInstantMillis_Object
    public void testGetInstantMillis_Object() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 12, 24, 48, 501, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 1, 1, 0, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004T+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 1, 0, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06T+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-161T+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-W24-3T+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 7, 0, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-W24T+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 0, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 48, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 30, 0, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12.5+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 30, 0, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24.5+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 48, 500, EIGHT);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.5+08:00", ISO_EIGHT));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 48, 501);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501", ISO));
    }

// org.joda.time.convert.TestStringConverter::testGetInstantMillis_Object_Zone
    public void testGetInstantMillis_Object_Zone() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 12, 24, 48, 501, PARIS);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501+02:00", ISO_PARIS));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 48, 501, PARIS);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501", ISO_PARIS));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 48, 501, LONDON);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501+01:00", ISO_LONDON));
        
        dt = new DateTime(2004, 6, 9, 12, 24, 48, 501, LONDON);
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501", ISO_LONDON));
    }

// org.joda.time.convert.TestStringConverter::testGetInstantMillis_Object_Chronology
    public void testGetInstantMillis_Object_Chronology() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 12, 24, 48, 501, JulianChronology.getInstance(LONDON));
        assertEquals(dt.getMillis(), StringConverter.INSTANCE.getInstantMillis("2004-06-09T12:24:48.501+01:00", JULIAN));
    }

// org.joda.time.convert.TestStringConverter::testGetInstantMillisInvalid
    public void testGetInstantMillisInvalid() {
        try {
            StringConverter.INSTANCE.getInstantMillis("", (Chronology) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getInstantMillis("X", (Chronology) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        assertEquals(ISOChronology.getInstance(PARIS), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501+01:00", PARIS));
        assertEquals(ISOChronology.getInstance(PARIS), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501", PARIS));
        assertEquals(ISOChronology.getInstance(LONDON), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501+01:00", (DateTimeZone) null));
        assertEquals(ISOChronology.getInstance(LONDON), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501", (DateTimeZone) null));
    }

// org.joda.time.convert.TestStringConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        assertEquals(JulianChronology.getInstance(LONDON), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501+01:00", JULIAN));
        assertEquals(JulianChronology.getInstance(LONDON), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501", JULIAN));
        assertEquals(ISOChronology.getInstance(LONDON), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501+01:00", (Chronology) null));
        assertEquals(ISOChronology.getInstance(LONDON), StringConverter.INSTANCE.getChronology("2004-06-09T12:24:48.501", (Chronology) null));
    }

// org.joda.time.convert.TestStringConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        TimeOfDay tod = new TimeOfDay();
        int[] expected = new int[] {3, 4, 5, 6};
        int[] actual = StringConverter.INSTANCE.getPartialValues(tod, "T03:04:05.006", ISOChronology.getInstance());
        assertEquals(true, Arrays.equals(expected, actual));
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime
    public void testGetDateTime() throws Exception {
        DateTime base = new DateTime(2004, 6, 9, 12, 24, 48, 501, PARIS);
        DateTime test = new DateTime(base.toString(), PARIS);
        assertEquals(base, test);
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime1
    public void testGetDateTime1() throws Exception {
        DateTime test = new DateTime("2004-06-09T12:24:48.501+01:00");
        assertEquals(2004, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
        assertEquals(LONDON, test.getZone());
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime2
    public void testGetDateTime2() throws Exception {
        DateTime test = new DateTime("2004-06-09T12:24:48.501");
        assertEquals(2004, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
        assertEquals(LONDON, test.getZone());
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime3
    public void testGetDateTime3() throws Exception {
        DateTime test = new DateTime("2004-06-09T12:24:48.501+02:00", PARIS);
        assertEquals(2004, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
        assertEquals(PARIS, test.getZone());
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime4
    public void testGetDateTime4() throws Exception {
        DateTime test = new DateTime("2004-06-09T12:24:48.501", PARIS);
        assertEquals(2004, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
        assertEquals(PARIS, test.getZone());
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime5
    public void testGetDateTime5() throws Exception {
        DateTime test = new DateTime("2004-06-09T12:24:48.501+02:00", JulianChronology.getInstance(PARIS));
        assertEquals(2004, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
        assertEquals(PARIS, test.getZone());
    }

// org.joda.time.convert.TestStringConverter::testGetDateTime6
    public void testGetDateTime6() throws Exception {
        DateTime test = new DateTime("2004-06-09T12:24:48.501", JulianChronology.getInstance(PARIS));
        assertEquals(2004, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(12, test.getHourOfDay());
        assertEquals(24, test.getMinuteOfHour());
        assertEquals(48, test.getSecondOfMinute());
        assertEquals(501, test.getMillisOfSecond());
        assertEquals(PARIS, test.getZone());
    }

// org.joda.time.convert.TestStringConverter::testGetDurationMillis_Object1
    public void testGetDurationMillis_Object1() throws Exception {
        long millis = StringConverter.INSTANCE.getDurationMillis("PT12.345S");
        assertEquals(12345, millis);
        
        millis = StringConverter.INSTANCE.getDurationMillis("pt12.345s");
        assertEquals(12345, millis);
        
        millis = StringConverter.INSTANCE.getDurationMillis("pt12s");
        assertEquals(12000, millis);
        
        millis = StringConverter.INSTANCE.getDurationMillis("pt12.s");
        assertEquals(12000, millis);
        
        millis = StringConverter.INSTANCE.getDurationMillis("pt-12.32s");
        assertEquals(-12320, millis);
        
        millis = StringConverter.INSTANCE.getDurationMillis("pt12.3456s");
        assertEquals(12345, millis);
    }

// org.joda.time.convert.TestStringConverter::testGetDurationMillis_Object2
    public void testGetDurationMillis_Object2() throws Exception {
        try {
            StringConverter.INSTANCE.getDurationMillis("P2Y6M9DXYZ");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("PTS");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("XT0S");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("PX0S");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("PT0X");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("PTXS");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("PT0.0.0S");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.getDurationMillis("PT0-00S");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testGetPeriodType_Object
    public void testGetPeriodType_Object() throws Exception {
        assertEquals(PeriodType.standard(),
            StringConverter.INSTANCE.getPeriodType("P2Y6M9D"));
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object1
    public void testSetIntoPeriod_Object1() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearMonthDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y6M9DT12H24M48S", null);
        assertEquals(2, m.getYears());
        assertEquals(6, m.getMonths());
        assertEquals(9, m.getDays());
        assertEquals(12, m.getHours());
        assertEquals(24, m.getMinutes());
        assertEquals(48, m.getSeconds());
        assertEquals(0, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object2
    public void testSetIntoPeriod_Object2() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearWeekDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y4W3DT12H24M48S", null);
        assertEquals(2, m.getYears());
        assertEquals(4, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(12, m.getHours());
        assertEquals(24, m.getMinutes());
        assertEquals(48, m.getSeconds());
        assertEquals(0, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object3
    public void testSetIntoPeriod_Object3() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearWeekDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y4W3DT12H24M48.034S", null);
        assertEquals(2, m.getYears());
        assertEquals(4, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(12, m.getHours());
        assertEquals(24, m.getMinutes());
        assertEquals(48, m.getSeconds());
        assertEquals(34, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object4
    public void testSetIntoPeriod_Object4() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearWeekDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y4W3DT12H24M.056S", null);
        assertEquals(2, m.getYears());
        assertEquals(4, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(12, m.getHours());
        assertEquals(24, m.getMinutes());
        assertEquals(0, m.getSeconds());
        assertEquals(56, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object5
    public void testSetIntoPeriod_Object5() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearWeekDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y4W3DT12H24M56.S", null);
        assertEquals(2, m.getYears());
        assertEquals(4, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(12, m.getHours());
        assertEquals(24, m.getMinutes());
        assertEquals(56, m.getSeconds());
        assertEquals(0, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object6
    public void testSetIntoPeriod_Object6() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearWeekDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y4W3DT12H24M56.1234567S", null);
        assertEquals(2, m.getYears());
        assertEquals(4, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(12, m.getHours());
        assertEquals(24, m.getMinutes());
        assertEquals(56, m.getSeconds());
        assertEquals(123, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object7
    public void testSetIntoPeriod_Object7() throws Exception {
        MutablePeriod m = new MutablePeriod(1, 0, 1, 1, 1, 1, 1, 1, PeriodType.yearWeekDayTime());
        StringConverter.INSTANCE.setInto(m, "P2Y4W3D", null);
        assertEquals(2, m.getYears());
        assertEquals(4, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(0, m.getHours());
        assertEquals(0, m.getMinutes());
        assertEquals(0, m.getSeconds());
        assertEquals(0, m.getMillis());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoPeriod_Object8
    public void testSetIntoPeriod_Object8() throws Exception {
        MutablePeriod m = new MutablePeriod();
        try {
            StringConverter.INSTANCE.setInto(m, "", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        try {
            StringConverter.INSTANCE.setInto(m, "PXY", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        try {
            StringConverter.INSTANCE.setInto(m, "PT0SXY", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            StringConverter.INSTANCE.setInto(m, "P2Y4W3DT12H24M48SX", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testIsReadableInterval_Object_Chronology
    public void testIsReadableInterval_Object_Chronology() throws Exception {
        assertEquals(false, StringConverter.INSTANCE.isReadableInterval("", null));
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology1
    public void testSetIntoInterval_Object_Chronology1() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2004-06-09/P1Y2M", null);
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), m.getStart());
        assertEquals(new DateTime(2005, 8, 9, 0, 0, 0, 0), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology2
    public void testSetIntoInterval_Object_Chronology2() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "P1Y2M/2004-06-09", null);
        assertEquals(new DateTime(2003, 4, 9, 0, 0, 0, 0), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology3
    public void testSetIntoInterval_Object_Chronology3() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2003-08-09/2004-06-09", null);
        assertEquals(new DateTime(2003, 8, 9, 0, 0, 0, 0), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology4
    public void testSetIntoInterval_Object_Chronology4() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2004-06-09T+06:00/P1Y2M", null);
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getStart());
        assertEquals(new DateTime(2005, 8, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology5
    public void testSetIntoInterval_Object_Chronology5() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "P1Y2M/2004-06-09T+06:00", null);
        assertEquals(new DateTime(2003, 4, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology6
    public void testSetIntoInterval_Object_Chronology6() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2003-08-09T+06:00/2004-06-09T+07:00", null);
        assertEquals(new DateTime(2003, 8, 9, 0, 0, 0, 0, SIX).withChronology(null), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, SEVEN).withChronology(null), m.getEnd());
        assertEquals(ISOChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology7
    public void testSetIntoInterval_Object_Chronology7() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2003-08-09/2004-06-09", BuddhistChronology.getInstance());
        assertEquals(new DateTime(2003, 8, 9, 0, 0, 0, 0, BuddhistChronology.getInstance()), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, BuddhistChronology.getInstance()), m.getEnd());
        assertEquals(BuddhistChronology.getInstance(), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoInterval_Object_Chronology8
    public void testSetIntoInterval_Object_Chronology8() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        StringConverter.INSTANCE.setInto(m, "2003-08-09T+06:00/2004-06-09T+07:00", BuddhistChronology.getInstance(EIGHT));
        assertEquals(new DateTime(2003, 8, 9, 0, 0, 0, 0, BuddhistChronology.getInstance(SIX)).withZone(EIGHT), m.getStart());
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0, BuddhistChronology.getInstance(SEVEN)).withZone(EIGHT), m.getEnd());
        assertEquals(BuddhistChronology.getInstance(EIGHT), m.getChronology());
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology1
    public void testSetIntoIntervalEx_Object_Chronology1() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology2
    public void testSetIntoIntervalEx_Object_Chronology2() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "/", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology3
    public void testSetIntoIntervalEx_Object_Chronology3() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "P1Y/", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology4
    public void testSetIntoIntervalEx_Object_Chronology4() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "/P1Y", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testSetIntoIntervalEx_Object_Chronology5
    public void testSetIntoIntervalEx_Object_Chronology5() throws Exception {
        MutableInterval m = new MutableInterval(-1000L, 1000L);
        try {
            StringConverter.INSTANCE.setInto(m, "P1Y/P2Y", null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.convert.TestStringConverter::testToString
    public void testToString() {
        assertEquals("Converter[java.lang.String]", StringConverter.INSTANCE.toString());
    }

// org.joda.time.format.TestDateTimeFormat::testSubclassableConstructor
    public void testSubclassableConstructor() {
        DateTimeFormat f = new DateTimeFormat() {
            
        };
        assertNotNull(f);
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_era
    public void testFormat_era() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("G").withLocale(Locale.UK);
        assertEquals(dt.toString(), "AD", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "AD", f.print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals(dt.toString(), "AD", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_centuryOfEra
    public void testFormat_centuryOfEra() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("C").withLocale(Locale.UK);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "1", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_yearOfEra
    public void testFormat_yearOfEra() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("Y").withLocale(Locale.UK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "124", f.print(dt));  
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_yearOfEra_twoDigit
    public void testFormat_yearOfEra_twoDigit() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("YY").withLocale(Locale.UK);
        assertEquals(dt.toString(), "04", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "23", f.print(dt));
        
        
        f = f.withZoneUTC();
        DateTime expect = null;
        expect = new DateTime(2004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));
        
        expect = new DateTime(1922, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("22"));
        
        expect = new DateTime(2021, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("21"));

        
        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = f.withPivotYear(new Integer(2050));
        expect = new DateTime(2000, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("00"));

        expect = new DateTime(2099, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("99"));

        
        f = DateTimeFormat.forPattern("YY").withLocale(Locale.UK);
        f = f.withZoneUTC();
        f.parseDateTime("5");
        f.parseDateTime("005");
        f.parseDateTime("+50");
        f.parseDateTime("-50");
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_yearOfEraParse
    public void testFormat_yearOfEraParse() {
        Chronology chrono = GJChronology.getInstanceUTC();

        DateTimeFormatter f = DateTimeFormat
            .forPattern("YYYY-MM GG")
            .withChronology(chrono)
            .withLocale(Locale.UK);

        DateTime dt = new DateTime(2005, 10, 1, 0, 0, 0, 0, chrono);
        assertEquals(dt, f.parseDateTime("2005-10 AD"));
        assertEquals(dt, f.parseDateTime("2005-10 CE"));

        dt = new DateTime(-2005, 10, 1, 0, 0, 0, 0, chrono);
        assertEquals(dt, f.parseDateTime("2005-10 BC"));
        assertEquals(dt, f.parseDateTime("2005-10 BCE"));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_year
    public void testFormat_year() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("y").withLocale(Locale.UK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "-123", f.print(dt));

        
        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_year_twoDigit
    public void testFormat_year_twoDigit() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("yy").withLocale(Locale.UK);
        assertEquals(dt.toString(), "04", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "23", f.print(dt));
        
        
        f = f.withZoneUTC();
        DateTime expect = null;
        expect = new DateTime(2004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));
        
        expect = new DateTime(1922, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("22"));
        
        expect = new DateTime(2021, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("21"));

        
        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = f.withPivotYear(new Integer(2050));
        expect = new DateTime(2000, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("00"));

        expect = new DateTime(2099, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("99"));

        
        
        f = new DateTimeFormatterBuilder().appendTwoDigitYear(2000).toFormatter();
        f = f.withZoneUTC();
        try {
            f.parseDateTime("5");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("005");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("+50");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("-50");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = DateTimeFormat.forPattern("yy").withLocale(Locale.UK);
        f = f.withZoneUTC();
        f.parseDateTime("5");
        f.parseDateTime("005");
        f.parseDateTime("+50");
        f.parseDateTime("-50");

        
        f = new DateTimeFormatterBuilder().appendTwoDigitYear(2000, true).toFormatter();
        f = f.withZoneUTC();
        expect = new DateTime(2004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));

        expect = new DateTime(4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+04"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-04"));

        expect = new DateTime(4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("4"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-4"));

        expect = new DateTime(4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("004"));

        expect = new DateTime(4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+004"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-004"));

        expect = new DateTime(3004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("3004"));

        expect = new DateTime(3004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+3004"));

        expect = new DateTime(-3004, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-3004"));

        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_year_long
    public void testFormat_year_long() {
        DateTime dt = new DateTime(278004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy");
        assertEquals(dt.toString(), "278004", f.print(dt));
        
        
        f = DateTimeFormat.forPattern("yyyyMMdd");
        assertEquals(dt.toString(), "2780040609", f.print(dt));
        
        
        f = DateTimeFormat.forPattern("yyyyddMM");
        assertEquals(dt.toString(), "2780040906", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_weekyear
    public void testFormat_weekyear() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("x").withLocale(Locale.UK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "2004", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "-123", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_weekyearOfEra_twoDigit
    public void testFormat_weekyearOfEra_twoDigit() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("xx").withLocale(Locale.UK);
        assertEquals(dt.toString(), "04", f.print(dt));
        
        dt = new DateTime(-123, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals(dt.toString(), "23", f.print(dt));
        
        
        f = f.withZoneUTC();
        DateTime expect = null;
        expect = new DateTime(2003, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));
        
        expect = new DateTime(1922, 1, 2, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("22"));
        
        expect = new DateTime(2021, 1, 4, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("21"));

        
        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = f.withPivotYear(new Integer(2050));
        expect = new DateTime(2000, 1, 3, 0, 0, 0, 0, DateTimeZone.UTC);
        assertEquals(expect, f.parseDateTime("00"));

        expect = new DateTime(2098, 12, 29, 0, 0, 0, 0, DateTimeZone.UTC);
        assertEquals(expect, f.parseDateTime("99"));

        
        
        f = new DateTimeFormatterBuilder().appendTwoDigitWeekyear(2000).toFormatter();
        f = f.withZoneUTC();
        try {
            f.parseDateTime("5");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("005");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("+50");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            f.parseDateTime("-50");
            fail();
        } catch (IllegalArgumentException ex) {}

        
        f = DateTimeFormat.forPattern("xx").withLocale(Locale.UK);
        f = f.withZoneUTC();
        f.parseDateTime("5");
        f.parseDateTime("005");
        f.parseDateTime("+50");
        f.parseDateTime("-50");

        
        f = new DateTimeFormatterBuilder().appendTwoDigitWeekyear(2000, true).toFormatter();
        f = f.withZoneUTC();
        expect = new DateTime(2003, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("04"));

        expect = new DateTime(3, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+04"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-04"));

        expect = new DateTime(3, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("4"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-4"));

        expect = new DateTime(3, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("004"));

        expect = new DateTime(3, 12, 29, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+004"));

        expect = new DateTime(-4, 1, 1, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-004"));

        expect = new DateTime(3004, 1, 2, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("3004"));

        expect = new DateTime(3004, 1, 2, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("+3004"));

        expect = new DateTime(-3004, 1, 4, 0, 0, 0, 0, UTC);
        assertEquals(expect, f.parseDateTime("-3004"));

        try {
            f.parseDateTime("-");
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            f.parseDateTime("+");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_weekOfWeekyear
    public void testFormat_weekOfWeekyear() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("w").withLocale(Locale.UK);
        assertEquals(dt.toString(), "24", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "24", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "24", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfWeek
    public void testFormat_dayOfWeek() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("e").withLocale(Locale.UK);
        assertEquals(dt.toString(), "3", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "3", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "3", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfWeekShortText
    public void testFormat_dayOfWeekShortText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("E").withLocale(Locale.UK);
        assertEquals(dt.toString(), "Wed", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "Wed", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "Wed", f.print(dt));
        
        f = f.withLocale(Locale.FRENCH);
        assertEquals(dt.toString(), "mer.", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfWeekText
    public void testFormat_dayOfWeekText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("EEEE").withLocale(Locale.UK);
        assertEquals(dt.toString(), "Wednesday", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "Wednesday", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "Wednesday", f.print(dt));
        
        f = f.withLocale(Locale.FRENCH);
        assertEquals(dt.toString(), "mercredi", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfYearText
    public void testFormat_dayOfYearText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("D").withLocale(Locale.UK);
        assertEquals(dt.toString(), "161", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "161", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "161", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_monthOfYear
    public void testFormat_monthOfYear() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("M").withLocale(Locale.UK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "6", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_monthOfYearShortText
    public void testFormat_monthOfYearShortText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("MMM").withLocale(Locale.UK);
        assertEquals(dt.toString(), "Jun", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "Jun", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "Jun", f.print(dt));
        
        f = f.withLocale(Locale.FRENCH);
        assertEquals(dt.toString(), "juin", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_monthOfYearText
    public void testFormat_monthOfYearText() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("MMMM").withLocale(Locale.UK);
        assertEquals(dt.toString(), "June", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "June", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "June", f.print(dt));
        
        f = f.withLocale(Locale.FRENCH);
        assertEquals(dt.toString(), "juin", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_dayOfMonth
    public void testFormat_dayOfMonth() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("d").withLocale(Locale.UK);
        assertEquals(dt.toString(), "9", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "9", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "9", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_halfdayOfDay
    public void testFormat_halfdayOfDay() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("a").withLocale(Locale.UK);
        assertEquals(dt.toString(), "am", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "am", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "pm", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_hourOfHalfday
    public void testFormat_hourOfHalfday() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("K").withLocale(Locale.UK);
        assertEquals(dt.toString(), "10", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "7", f.print(dt));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, UTC);
        assertEquals(dt.toString(), "0", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_clockhourOfHalfday
    public void testFormat_clockhourOfHalfday() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("h").withLocale(Locale.UK);
        assertEquals(dt.toString(), "10", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "7", f.print(dt));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, UTC);
        assertEquals(dt.toString(), "12", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_hourOfDay
    public void testFormat_hourOfDay() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("H").withLocale(Locale.UK);
        assertEquals(dt.toString(), "10", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "19", f.print(dt));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, UTC);
        assertEquals(dt.toString(), "0", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_clockhourOfDay
    public void testFormat_clockhourOfDay() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("k").withLocale(Locale.UK);
        assertEquals(dt.toString(), "10", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "6", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "19", f.print(dt));
        
        dt = new DateTime(2004, 6, 9, 0, 0, 0, 0, UTC);
        assertEquals(dt.toString(), "24", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_minute
    public void testFormat_minute() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("m").withLocale(Locale.UK);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "20", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "20", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_second
    public void testFormat_second() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("s").withLocale(Locale.UK);
        assertEquals(dt.toString(), "30", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "30", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "30", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_fractionOfSecond
    public void testFormat_fractionOfSecond() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("SSS").withLocale(Locale.UK);
        assertEquals(dt.toString(), "040", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "040", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "040", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_fractionOfSecondLong
    public void testFormat_fractionOfSecondLong() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("SSSSSS").withLocale(Locale.UK);
        assertEquals(dt.toString(), "040000", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "040000", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "040000", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneText
    public void testFormat_zoneText() {}

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneLongText
    public void testFormat_zoneLongText() {}

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneAmount
    public void testFormat_zoneAmount() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("Z").withLocale(Locale.UK);
        assertEquals(dt.toString(), "+0000", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "-0400", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "+0900", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneAmountColon
    public void testFormat_zoneAmountColon() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("ZZ").withLocale(Locale.UK);
        assertEquals(dt.toString(), "+00:00", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "-04:00", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "+09:00", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_zoneAmountID
    public void testFormat_zoneAmountID() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("ZZZ").withLocale(Locale.UK);
        assertEquals(dt.toString(), "UTC", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals(dt.toString(), "America/New_York", f.print(dt));
        
        dt = dt.withZone(TOKYO);
        assertEquals(dt.toString(), "Asia/Tokyo", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_other
    public void testFormat_other() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("'Hello' ''");
        assertEquals("Hello '", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_invalid
    public void testFormat_invalid() {
        try {
            DateTimeFormat.forPattern(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forPattern("");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forPattern("A");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forPattern("dd/mm/AA");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_samples
    public void testFormat_samples() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH.mm.ss");
        assertEquals("2004-06-09 10.20.30", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormat::testFormat_shortBasicParse
    public void testFormat_shortBasicParse() {
        
        

        DateTime dt = new DateTime(2004, 3, 9, 0, 0, 0, 0);

        DateTimeFormatter f = DateTimeFormat.forPattern("yyMMdd");
        assertEquals(dt, f.parseDateTime("040309"));
        try {
            assertEquals(dt, f.parseDateTime("20040309"));
            fail();
        } catch (IllegalArgumentException ex) {}

        f = DateTimeFormat.forPattern("yy/MM/dd");
        assertEquals(dt, f.parseDateTime("04/03/09"));
        assertEquals(dt, f.parseDateTime("2004/03/09"));
    }

// org.joda.time.format.TestDateTimeFormat::testParse_pivotYear
    public void testParse_pivotYear() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd.MM.yy").withPivotYear(2050).withZoneUTC();
        
        DateTime date = dateFormatter.parseDateTime("25.12.15");
        assertEquals(date.getYear(), 2015);
        
        date = dateFormatter.parseDateTime("25.12.00");
        assertEquals(date.getYear(), 2000);
        
        date = dateFormatter.parseDateTime("25.12.99");
        assertEquals(date.getYear(), 2099);
    }

// org.joda.time.format.TestDateTimeFormat::testParse_pivotYear_ignored4DigitYear
    public void testParse_pivotYear_ignored4DigitYear() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd.MM.yyyy").withPivotYear(2050).withZoneUTC();
        
        DateTime date = dateFormatter.parseDateTime("25.12.15");
        assertEquals(date.getYear(), 15);
        
        date = dateFormatter.parseDateTime("25.12.00");
        assertEquals(date.getYear(), 0);
        
        date = dateFormatter.parseDateTime("25.12.99");
        assertEquals(date.getYear(), 99);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanShort_UK
    public void testFormatParse_textMonthJanShort_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 1, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals(str, "23 Jan 2007");
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanShortLowerCase_UK
    public void testFormatParse_textMonthJanShortLowerCase_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        DateTime date = dateFormatter.parseDateTime("23 jan 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanShortUpperCase_UK
    public void testFormatParse_textMonthJanShortUpperCase_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        DateTime date = dateFormatter.parseDateTime("23 JAN 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testParse_textMonthJanLong_UK
    public void testParse_textMonthJanLong_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        
        DateTime date = dateFormatter.parseDateTime("23 January 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanLongLowerCase_UK
    public void testFormatParse_textMonthJanLongLowerCase_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        DateTime date = dateFormatter.parseDateTime("23 january 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanLongUpperCase_UK
    public void testFormatParse_textMonthJanLongUpperCase_UK() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.UK).withZoneUTC();
        DateTime date = dateFormatter.parseDateTime("23 JANUARY 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanShort_France
    public void testFormatParse_textMonthJanShort_France() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 1, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("23 janv. 2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthJanLong_France
    public void testFormatParse_textMonthJanLong_France() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        DateTime date = dateFormatter.parseDateTime("23 janvier 2007");
        check(date, 2007, 1, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthApr_France
    public void testFormatParse_textMonthApr_France() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM yyyy")
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 2, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("23 f\u00E9vr. 2007", str);  
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 2, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthAtEnd_France
    public void testFormatParse_textMonthAtEnd_France() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM")
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("23 juin", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2000, 6, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthAtEnd_France_withSpecifiedDefault
    public void testFormatParse_textMonthAtEnd_France_withSpecifiedDefault() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd MMM")
            .withLocale(Locale.FRANCE).withZoneUTC().withDefaultYear(1980);
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("23 juin", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 1980, 6, 23);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textMonthApr_Korean
    public void testFormatParse_textMonthApr_Korean() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("EEEE, d MMMM yyyy HH:mm")
            .withLocale(Locale.KOREAN).withZoneUTC();
        
        String str = new DateTime(2007, 3, 8, 22, 0, 0, 0, UTC).toString(dateFormatter);
        DateTime date = dateFormatter.parseDateTime(str);
        assertEquals(new DateTime(2007, 3, 8, 22, 0, 0, 0, UTC), date);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textHalfdayAM_UK
    public void testFormatParse_textHalfdayAM_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendClockhourOfHalfday(2)
            .appendLiteral('-')
            .appendHalfdayOfDayText()
            .appendLiteral('-')
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 18, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$06-pm-2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textHalfdayAM_France
    public void testFormatParse_textHalfdayAM_France() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendClockhourOfHalfday(2)
            .appendLiteral('-')
            .appendHalfdayOfDayText()
            .appendLiteral('-')
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 18, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$06-PM-2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textEraAD_UK
    public void testFormatParse_textEraAD_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendEraText()
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$AD2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textEraAD_France
    public void testFormatParse_textEraAD_France() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendEraText()
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$ap. J.-C.2007", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, 2007, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textEraBC_France
    public void testFormatParse_textEraBC_France() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendEraText()
            .appendYear(4, 4)
            .toFormatter()
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(-1, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$av. J.-C.-0001", str);
        DateTime date = dateFormatter.parseDateTime(str);
        check(date, -1, 1, 1);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textYear_UK
    public void testFormatParse_textYear_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendText(DateTimeFieldType.year())
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$2007", str);
        try {
            dateFormatter.parseDateTime(str);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textYear_France
    public void testFormatParse_textYear_France() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendText(DateTimeFieldType.year())
            .toFormatter()
            .withLocale(Locale.FRANCE).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$2007", str);
        try {
            dateFormatter.parseDateTime(str);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textAdjoiningHelloWorld_UK
    public void testFormatParse_textAdjoiningHelloWorld_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendDayOfMonth(2)
            .appendMonthOfYearShortText()
            .appendLiteral("HelloWorld")
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$23JunHelloWorld", str);
        dateFormatter.parseDateTime(str);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_textAdjoiningMonthDOW_UK
    public void testFormatParse_textAdjoiningMonthDOW_UK() {
        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .appendLiteral('$')
            .appendDayOfMonth(2)
            .appendMonthOfYearShortText()
            .appendDayOfWeekShortText()
            .toFormatter()
            .withLocale(Locale.UK).withZoneUTC();
        
        String str = new DateTime(2007, 6, 23, 0, 0, 0, 0, UTC).toString(dateFormatter);
        assertEquals("$23JunSat", str);
        dateFormatter.parseDateTime(str);
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_zoneId_noColon
    public void testFormatParse_zoneId_noColon() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm Z").withZoneUTC();
        String str = new DateTime(2007, 6, 23, 1, 2, 0, 0, UTC).toString(dateFormatter);
        assertEquals("01:02 +0000", str);
        DateTime parsed = dateFormatter.parseDateTime(str);
        assertEquals(1, parsed.getHourOfDay());
        assertEquals(2, parsed.getMinuteOfHour());
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_zoneId_noColon_parseZ
    public void testFormatParse_zoneId_noColon_parseZ() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm Z").withZoneUTC();
        DateTime parsed = dateFormatter.parseDateTime("01:02 Z");
        assertEquals(1, parsed.getHourOfDay());
        assertEquals(2, parsed.getMinuteOfHour());
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_zoneId_colon
    public void testFormatParse_zoneId_colon() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm ZZ").withZoneUTC();
        String str = new DateTime(2007, 6, 23, 1, 2, 0, 0, UTC).toString(dateFormatter);
        assertEquals("01:02 +00:00", str);
        DateTime parsed = dateFormatter.parseDateTime(str);
        assertEquals(1, parsed.getHourOfDay());
        assertEquals(2, parsed.getMinuteOfHour());
    }

// org.joda.time.format.TestDateTimeFormat::testFormatParse_zoneId_colon_parseZ
    public void testFormatParse_zoneId_colon_parseZ() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("HH:mm ZZ").withZoneUTC();
        DateTime parsed = dateFormatter.parseDateTime("01:02 Z");
        assertEquals(1, parsed.getHourOfDay());
        assertEquals(2, parsed.getMinuteOfHour());
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_stringLengths
    public void testForStyle_stringLengths() {
        try {
            DateTimeFormat.forStyle(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("S");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("SSS");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_invalidStrings
    public void testForStyle_invalidStrings() {
        try {
            DateTimeFormat.forStyle("AA");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("--");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeFormat.forStyle("ss");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortDate
    public void testForStyle_shortDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.shortDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("S-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
        
        DateTime date = new DateTime(
                DateFormat.getDateInstance(DateFormat.SHORT, FRANCE).parse(expect));
        assertEquals(date, f.withLocale(FRANCE).parseDateTime(expect));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortTime
    public void testForStyle_shortTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.shortTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("-S");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getTimeInstance(DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
        
        if (TimeZone.getDefault() instanceof SimpleTimeZone) {
            
        } else {
            DateTime date = new DateTime(
                DateFormat.getTimeInstance(DateFormat.SHORT, FRANCE).parse(expect));
            assertEquals(date, f.withLocale(FRANCE).parseDateTime(expect));
        }
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortDateTime
    public void testForStyle_shortDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.shortDateTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("SS");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
        
        DateTime date = new DateTime(
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, FRANCE).parse(expect));
        assertEquals(date, f.withLocale(FRANCE).parseDateTime(expect));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumDate
    public void testForStyle_mediumDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.mediumDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("M-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumTime
    public void testForStyle_mediumTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.mediumTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("-M");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getTimeInstance(DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getTimeInstance(DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumDateTime
    public void testForStyle_mediumDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.mediumDateTime();
        DateTimeFormatter g = DateTimeFormat.forStyle("MM");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_longDate
    public void testForStyle_longDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.longDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("L-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.LONG, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.LONG, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.LONG, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_longTime
    public void testForStyle_longTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_longDateTime
    public void testForStyle_longDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_fullDate
    public void testForStyle_fullDate() throws Exception {
        DateTimeFormatter f = DateTimeFormat.fullDate();
        DateTimeFormatter g = DateTimeFormat.forStyle("F-");
        assertSame(g, f);
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateInstance(DateFormat.FULL, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateInstance(DateFormat.FULL, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateInstance(DateFormat.FULL, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_fullTime
    public void testForStyle_fullTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_fullDateTime
    public void testForStyle_fullDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortMediumDateTime
    public void testForStyle_shortMediumDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.forStyle("SM");
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortLongDateTime
    public void testForStyle_shortLongDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_shortFullDateTime
    public void testForStyle_shortFullDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumShortDateTime
    public void testForStyle_mediumShortDateTime() throws Exception {
        DateTimeFormatter f = DateTimeFormat.forStyle("MS");
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 0);
        String expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, UK).format(dt.toDate());
        assertEquals(expect, f.print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, US).format(dt.toDate());
        assertEquals(expect, f.withLocale(US).print(dt));
        expect = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, FRANCE).format(dt.toDate());
        assertEquals(expect, f.withLocale(FRANCE).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumLongDateTime
    public void testForStyle_mediumLongDateTime() {}

// org.joda.time.format.TestDateTimeFormatStyle::testForStyle_mediumFullDateTime
    public void testForStyle_mediumFullDateTime() {}

// org.joda.time.format.TestDateTimeFormatter::testPrint_simple
    public void testPrint_simple() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T10:20:30Z", f.print(dt));
        
        dt = dt.withZone(PARIS);
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.print(dt));
        
        dt = dt.withChronology(BUDDHIST_PARIS);
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_locale
    public void testPrint_locale() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("mer. 2004-06-09T10:20:30Z", f.withLocale(Locale.FRENCH).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withLocale(null).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_zone
    public void testPrint_zone() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withZone(null).print(dt));
        
        dt = dt.withZone(NEWYORK);
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withZoneUTC().print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00", f.withZone(null).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_chrono
    public void testPrint_chrono() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withChronology(ISO_PARIS).print(dt));
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.withChronology(BUDDHIST_PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withChronology(null).print(dt));
        
        dt = dt.withChronology(BUDDHIST_PARIS);
        assertEquals("Wed 2004-06-09T12:20:30+02:00", f.withChronology(ISO_PARIS).print(dt));
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.withChronology(BUDDHIST_PARIS).print(dt));
        assertEquals("Wed 2004-06-09T10:20:30Z", f.withChronology(ISO_UTC).print(dt));
        assertEquals("Wed 2547-06-09T12:20:30+02:00", f.withChronology(null).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_bufferMethods
    public void testPrint_bufferMethods() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        StringBuffer buf = new StringBuffer();
        f.printTo(buf, dt);
        assertEquals("Wed 2004-06-09T10:20:30Z", buf.toString());
        
        buf = new StringBuffer();
        f.printTo(buf, dt.getMillis());
        assertEquals("Wed 2004-06-09T11:20:30+01:00", buf.toString());
        
        buf = new StringBuffer();
        ISODateTimeFormat.yearMonthDay().printTo(buf, dt.toYearMonthDay());
        assertEquals("2004-06-09", buf.toString());
        
        buf = new StringBuffer();
        try {
            ISODateTimeFormat.yearMonthDay().printTo(buf, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_writerMethods
    public void testPrint_writerMethods() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        CharArrayWriter out = new CharArrayWriter();
        f.printTo(out, dt);
        assertEquals("Wed 2004-06-09T10:20:30Z", out.toString());
        
        out = new CharArrayWriter();
        f.printTo(out, dt.getMillis());
        assertEquals("Wed 2004-06-09T11:20:30+01:00", out.toString());
        
        out = new CharArrayWriter();
        ISODateTimeFormat.yearMonthDay().printTo(out, dt.toYearMonthDay());
        assertEquals("2004-06-09", out.toString());
        
        out = new CharArrayWriter();
        try {
            ISODateTimeFormat.yearMonthDay().printTo(out, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_appendableMethods
    public void testPrint_appendableMethods() throws Exception {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        StringBuilder buf = new StringBuilder();
        f.printTo(buf, dt);
        assertEquals("Wed 2004-06-09T10:20:30Z", buf.toString());
        
        buf = new StringBuilder();
        f.printTo(buf, dt.getMillis());
        assertEquals("Wed 2004-06-09T11:20:30+01:00", buf.toString());
        
        buf = new StringBuilder();
        ISODateTimeFormat.yearMonthDay().printTo(buf, dt.toLocalDate());
        assertEquals("2004-06-09", buf.toString());
        
        buf = new StringBuilder();
        try {
            ISODateTimeFormat.yearMonthDay().printTo(buf, (ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testPrint_chrono_and_zone
    public void testPrint_chrono_and_zone() {
        DateTime dt = new DateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
        assertEquals("Wed 2004-06-09T10:20:30Z",
                f.withChronology(null).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(ISO_PARIS).withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(null).withZone(NEWYORK).print(dt));
        
        dt = dt.withChronology(ISO_PARIS);
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(null).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(ISO_PARIS).withZone(NEWYORK).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(null).withZone(NEWYORK).print(dt));
        
        dt = dt.withChronology(BUDDHIST_PARIS);
        assertEquals("Wed 2547-06-09T12:20:30+02:00",
                f.withChronology(null).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(null).print(dt));
        assertEquals("Wed 2004-06-09T12:20:30+02:00",
                f.withChronology(ISO_PARIS).withZone(PARIS).print(dt));
        assertEquals("Wed 2004-06-09T06:20:30-04:00",
                f.withChronology(ISO_PARIS).withZone(NEWYORK).print(dt));
        assertEquals("Wed 2547-06-09T06:20:30-04:00",
                f.withChronology(null).withZone(NEWYORK).print(dt));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetLocale
    public void testWithGetLocale() {
        DateTimeFormatter f2 = f.withLocale(Locale.FRENCH);
        assertEquals(Locale.FRENCH, f2.getLocale());
        assertSame(f2, f2.withLocale(Locale.FRENCH));
        
        f2 = f.withLocale(null);
        assertEquals(null, f2.getLocale());
        assertSame(f2, f2.withLocale(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetZone
    public void testWithGetZone() {
        DateTimeFormatter f2 = f.withZone(PARIS);
        assertEquals(PARIS, f2.getZone());
        assertSame(f2, f2.withZone(PARIS));
        
        f2 = f.withZone(null);
        assertEquals(null, f2.getZone());
        assertSame(f2, f2.withZone(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetChronology
    public void testWithGetChronology() {
        DateTimeFormatter f2 = f.withChronology(BUDDHIST_PARIS);
        assertEquals(BUDDHIST_PARIS, f2.getChronology());
        assertSame(f2, f2.withChronology(BUDDHIST_PARIS));
        
        f2 = f.withChronology(null);
        assertEquals(null, f2.getChronology());
        assertSame(f2, f2.withChronology(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetPivotYear
    public void testWithGetPivotYear() {
        DateTimeFormatter f2 = f.withPivotYear(13);
        assertEquals(new Integer(13), f2.getPivotYear());
        assertSame(f2, f2.withPivotYear(13));
        
        f2 = f.withPivotYear(new Integer(14));
        assertEquals(new Integer(14), f2.getPivotYear());
        assertSame(f2, f2.withPivotYear(new Integer(14)));
        
        f2 = f.withPivotYear(null);
        assertEquals(null, f2.getPivotYear());
        assertSame(f2, f2.withPivotYear(null));
    }

// org.joda.time.format.TestDateTimeFormatter::testWithGetOffsetParsedMethods
    public void testWithGetOffsetParsedMethods() {
        DateTimeFormatter f2 = f;
        assertEquals(false, f2.isOffsetParsed());
        assertEquals(null, f2.getZone());
        
        f2 = f.withOffsetParsed();
        assertEquals(true, f2.isOffsetParsed());
        assertEquals(null, f2.getZone());
        
        f2 = f2.withZone(PARIS);
        assertEquals(false, f2.isOffsetParsed());
        assertEquals(PARIS, f2.getZone());
        
        f2 = f2.withOffsetParsed();
        assertEquals(true, f2.isOffsetParsed());
        assertEquals(null, f2.getZone());
        
        f2 = f.withOffsetParsed();
        assertNotSame(f, f2);
        DateTimeFormatter f3 = f2.withOffsetParsed();
        assertSame(f2, f3);
    }

// org.joda.time.format.TestDateTimeFormatter::testPrinterParserMethods
    public void testPrinterParserMethods() {
        DateTimeFormatter f2 = new DateTimeFormatter(f.getPrinter(), f.getParser());
        assertEquals(f.getPrinter(), f2.getPrinter());
        assertEquals(f.getParser(), f2.getParser());
        assertEquals(true, f2.isPrinter());
        assertEquals(true, f2.isParser());
        assertNotNull(f2.print(0L));
        assertNotNull(f2.parseDateTime("Thu 1970-01-01T00:00:00Z"));
        
        f2 = new DateTimeFormatter(f.getPrinter(), null);
        assertEquals(f.getPrinter(), f2.getPrinter());
        assertEquals(null, f2.getParser());
        assertEquals(true, f2.isPrinter());
        assertEquals(false, f2.isParser());
        assertNotNull(f2.print(0L));
        try {
            f2.parseDateTime("Thu 1970-01-01T00:00:00Z");
            fail();
        } catch (UnsupportedOperationException ex) {}
        
        f2 = new DateTimeFormatter(null, f.getParser());
        assertEquals(null, f2.getPrinter());
        assertEquals(f.getParser(), f2.getParser());
        assertEquals(false, f2.isPrinter());
        assertEquals(true, f2.isParser());
        try {
            f2.print(0L);
            fail();
        } catch (UnsupportedOperationException ex) {}
        assertNotNull(f2.parseDateTime("Thu 1970-01-01T00:00:00Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_simple
    public void testParseLocalDate_simple() {
        assertEquals(new LocalDate(2004, 6, 9), g.parseLocalDate("2004-06-09T10:20:30Z"));
        assertEquals(new LocalDate(2004, 6, 9), g.parseLocalDate("2004-06-09T10:20:30+18:00"));
        assertEquals(new LocalDate(2004, 6, 9), g.parseLocalDate("2004-06-09T10:20:30-18:00"));
        assertEquals(new LocalDate(2004, 6, 9, BUDDHIST_PARIS),
                g.withChronology(BUDDHIST_PARIS).parseLocalDate("2004-06-09T10:20:30Z"));
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_yearOfEra
    public void testParseLocalDate_yearOfEra() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("YYYY-MM GG")
            .withChronology(chrono)
            .withLocale(Locale.UK);
        
        LocalDate date = new LocalDate(2005, 10, 1, chrono);
        assertEquals(date, f.parseLocalDate("2005-10 AD"));
        assertEquals(date, f.parseLocalDate("2005-10 CE"));
        
        date = new LocalDate(-2005, 10, 1, chrono);
        assertEquals(date, f.parseLocalDate("2005-10 BC"));
        assertEquals(date, f.parseLocalDate("2005-10 BCE"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_yearOfCentury
    public void testParseLocalDate_yearOfCentury() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("yy M d")
            .withChronology(chrono)
            .withLocale(Locale.UK)
            .withPivotYear(2050);
        
        LocalDate date = new LocalDate(2050, 8, 4, chrono);
        assertEquals(date, f.parseLocalDate("50 8 4"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_monthDay_feb29
    public void testParseLocalDate_monthDay_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d")
            .withChronology(chrono)
            .withLocale(Locale.UK);
        
        assertEquals(new LocalDate(2000, 2, 29, chrono), f.parseLocalDate("2 29"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_monthDay_withDefaultYear_feb29
    public void testParseLocalDate_monthDay_withDefaultYear_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d")
            .withChronology(chrono)
            .withLocale(Locale.UK)
            .withDefaultYear(2012);
        
        assertEquals(new LocalDate(2012, 2, 29, chrono), f.parseLocalDate("2 29"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_weekyear_month_week_2010
    public void testParseLocalDate_weekyear_month_week_2010() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("xxxx-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2010, 1, 4, chrono), f.parseLocalDate("2010-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_weekyear_month_week_2011
    public void testParseLocalDate_weekyear_month_week_2011() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("xxxx-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2011, 1, 3, chrono), f.parseLocalDate("2011-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_weekyear_month_week_2012
    public void testParseLocalDate_weekyear_month_week_2012() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("xxxx-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2012, 1, 2, chrono), f.parseLocalDate("2012-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2010
    public void testParseLocalDate_year_month_week_2010() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2010, 1, 4, chrono), f.parseLocalDate("2010-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2011
    public void testParseLocalDate_year_month_week_2011() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2011, 1, 3, chrono), f.parseLocalDate("2011-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2012
    public void testParseLocalDate_year_month_week_2012() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2012, 1, 2, chrono), f.parseLocalDate("2012-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2013
    public void testParseLocalDate_year_month_week_2013() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2012, 12, 31, chrono), f.parseLocalDate("2013-01-01"));  
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2014
    public void testParseLocalDate_year_month_week_2014() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2013, 12, 30, chrono), f.parseLocalDate("2014-01-01"));  
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2015
    public void testParseLocalDate_year_month_week_2015() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2014, 12, 29, chrono), f.parseLocalDate("2015-01-01"));  
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDate_year_month_week_2016
    public void testParseLocalDate_year_month_week_2016() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
        assertEquals(new LocalDate(2016, 1, 4, chrono), f.parseLocalDate("2016-01-01"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalTime_simple
    public void testParseLocalTime_simple() {
        assertEquals(new LocalTime(10, 20, 30), g.parseLocalTime("2004-06-09T10:20:30Z"));
        assertEquals(new LocalTime(10, 20, 30), g.parseLocalTime("2004-06-09T10:20:30+18:00"));
        assertEquals(new LocalTime(10, 20, 30), g.parseLocalTime("2004-06-09T10:20:30-18:00"));
        assertEquals(new LocalTime(10, 20, 30, 0, BUDDHIST_PARIS),
                g.withChronology(BUDDHIST_PARIS).parseLocalTime("2004-06-09T10:20:30Z"));
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDateTime_simple
    public void testParseLocalDateTime_simple() {
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30), g.parseLocalDateTime("2004-06-09T10:20:30Z"));
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30), g.parseLocalDateTime("2004-06-09T10:20:30+18:00"));
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30), g.parseLocalDateTime("2004-06-09T10:20:30-18:00"));
        assertEquals(new LocalDateTime(2004, 6, 9, 10, 20, 30, 0, BUDDHIST_PARIS),
                g.withChronology(BUDDHIST_PARIS).parseLocalDateTime("2004-06-09T10:20:30Z"));
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDateTime_monthDay_feb29
    public void testParseLocalDateTime_monthDay_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d H m")
            .withChronology(chrono)
            .withLocale(Locale.UK);
        
        assertEquals(new LocalDateTime(2000, 2, 29, 13, 40, 0, 0, chrono), f.parseLocalDateTime("2 29 13 40"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseLocalDateTime_monthDay_withDefaultYear_feb29
    public void testParseLocalDateTime_monthDay_withDefaultYear_feb29() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat
            .forPattern("M d H m")
            .withChronology(chrono)
            .withLocale(Locale.UK)
            .withDefaultYear(2012);
        
        assertEquals(new LocalDateTime(2012, 2, 29, 13, 40, 0, 0, chrono), f.parseLocalDateTime("2 29 13 40"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_simple
    public void testParseDateTime_simple() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.parseDateTime("2004-06-09T10:20:30Z"));
        
        try {
            g.parseDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_zone
    public void testParseDateTime_zone() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_zone2
    public void testParseDateTime_zone2() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseDateTime("2004-06-09T06:20:30-04:00"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_zone3
    public void testParseDateTime_zone3() {
        DateTimeFormatter h = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.date())
        .appendLiteral('T')
        .append(ISODateTimeFormat.timeElementParser())
        .toFormatter();
        
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(LONDON).parseDateTime("2004-06-09T10:20:30"));
        
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(null).parseDateTime("2004-06-09T10:20:30"));
        
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, PARIS);
        assertEquals(expect, h.withZone(PARIS).parseDateTime("2004-06-09T10:20:30"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_simple_precedence
    public void testParseDateTime_simple_precedence() {
        DateTime expect = null;
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, f.parseDateTime("Wed 2004-06-09T10:20:30Z"));
        
        
        expect = new DateTime(2004, 6, 7, 11, 20, 30, 0, LONDON);
        
        assertEquals(expect, f.parseDateTime("Mon 2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_offsetParsed
    public void testParseDateTime_offsetParsed() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withOffsetParsed().parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 6, 20, 30, 0, DateTimeZone.forOffsetHours(-4));
        assertEquals(expect, g.withOffsetParsed().parseDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new DateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withZone(PARIS).withOffsetParsed().parseDateTime("2004-06-09T10:20:30Z"));
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withOffsetParsed().withZone(PARIS).parseDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseDateTime_chrono
    public void testParseDateTime_chrono() {
        DateTime expect = null;
        expect = new DateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withChronology(ISO_PARIS).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 11, 20, 30, 0,LONDON);
        assertEquals(expect, g.withChronology(null).parseDateTime("2004-06-09T10:20:30Z"));
        
        expect = new DateTime(2547, 6, 9, 12, 20, 30, 0, BUDDHIST_PARIS);
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseDateTime("2547-06-09T10:20:30Z"));
        
        expect = new DateTime(2004, 6, 9, 10, 29, 51, 0, BUDDHIST_PARIS); 
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_simple
    public void testParseMutableDateTime_simple() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        try {
            g.parseMutableDateTime("ABC");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_zone
    public void testParseMutableDateTime_zone() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_zone2
    public void testParseMutableDateTime_zone2() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(LONDON).parseMutableDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, g.withZone(null).parseMutableDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withZone(PARIS).parseMutableDateTime("2004-06-09T06:20:30-04:00"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_zone3
    public void testParseMutableDateTime_zone3() {
        DateTimeFormatter h = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.date())
        .appendLiteral('T')
        .append(ISODateTimeFormat.timeElementParser())
        .toFormatter();
        
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(LONDON).parseMutableDateTime("2004-06-09T10:20:30"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        assertEquals(expect, h.withZone(null).parseMutableDateTime("2004-06-09T10:20:30"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, PARIS);
        assertEquals(expect, h.withZone(PARIS).parseMutableDateTime("2004-06-09T10:20:30"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_simple_precedence
    public void testParseMutableDateTime_simple_precedence() {
        MutableDateTime expect = null;
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(expect, f.parseDateTime("Wed 2004-06-09T10:20:30Z"));
        
        
        expect = new MutableDateTime(2004, 6, 7, 11, 20, 30, 0, LONDON);
        
        assertEquals(expect, f.parseDateTime("Mon 2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_offsetParsed
    public void testParseMutableDateTime_offsetParsed() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withOffsetParsed().parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 6, 20, 30, 0, DateTimeZone.forOffsetHours(-4));
        assertEquals(expect, g.withOffsetParsed().parseMutableDateTime("2004-06-09T06:20:30-04:00"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        assertEquals(expect, g.withZone(PARIS).withOffsetParsed().parseMutableDateTime("2004-06-09T10:20:30Z"));
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withOffsetParsed().withZone(PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMutableDateTime_chrono
    public void testParseMutableDateTime_chrono() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(expect, g.withChronology(ISO_PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0,LONDON);
        assertEquals(expect, g.withChronology(null).parseMutableDateTime("2004-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2547, 6, 9, 12, 20, 30, 0, BUDDHIST_PARIS);
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseMutableDateTime("2547-06-09T10:20:30Z"));
        
        expect = new MutableDateTime(2004, 6, 9, 10, 29, 51, 0, BUDDHIST_PARIS); 
        assertEquals(expect, g.withChronology(BUDDHIST_PARIS).parseMutableDateTime("2004-06-09T10:20:30Z"));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_simple
    public void testParseInto_simple() {
        MutableDateTime expect = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        MutableDateTime result = new MutableDateTime(0L);
        assertEquals(20, g.parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        try {
            g.parseInto(null, "2004-06-09T10:20:30Z", 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        assertEquals(~0, g.parseInto(result, "ABC", 0));
        assertEquals(~10, g.parseInto(result, "2004-06-09", 0));
        assertEquals(~13, g.parseInto(result, "XX2004-06-09T", 2));
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_zone
    public void testParseInto_zone() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withZone(LONDON).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withZone(null).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withZone(PARIS).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_zone2
    public void testParseInto_zone2() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(25, g.withZone(LONDON).parseInto(result, "2004-06-09T06:20:30-04:00", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        assertEquals(25, g.withZone(null).parseInto(result, "2004-06-09T06:20:30-04:00", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        assertEquals(25, g.withZone(PARIS).parseInto(result, "2004-06-09T06:20:30-04:00", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_zone3
    public void testParseInto_zone3() {
        DateTimeFormatter h = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.date())
        .appendLiteral('T')
        .append(ISODateTimeFormat.timeElementParser())
        .toFormatter();
        
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(19, h.withZone(LONDON).parseInto(result, "2004-06-09T10:20:30", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(19, h.withZone(null).parseInto(result, "2004-06-09T10:20:30", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, PARIS);
        result = new MutableDateTime(0L);
        assertEquals(19, h.withZone(PARIS).parseInto(result, "2004-06-09T10:20:30", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_simple_precedence
    public void testParseInto_simple_precedence() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 7, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        
        assertEquals(24, f.parseInto(result, "Mon 2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_offsetParsed
    public void testParseInto_offsetParsed() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withOffsetParsed().parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 6, 20, 30, 0, DateTimeZone.forOffsetHours(-4));
        result = new MutableDateTime(0L);
        assertEquals(25, g.withOffsetParsed().parseInto(result, "2004-06-09T06:20:30-04:00", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 10, 20, 30, 0, UTC);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withZone(PARIS).withOffsetParsed().parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withOffsetParsed().withZone(PARIS).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseInto_chrono
    public void testParseInto_chrono() {
        MutableDateTime expect = null;
        MutableDateTime result = null;
        expect = new MutableDateTime(2004, 6, 9, 12, 20, 30, 0, PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withChronology(ISO_PARIS).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 11, 20, 30, 0, LONDON);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withChronology(null).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2547, 6, 9, 12, 20, 30, 0, BUDDHIST_PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withChronology(BUDDHIST_PARIS).parseInto(result, "2547-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
        
        expect = new MutableDateTime(2004, 6, 9, 10, 29, 51, 0, BUDDHIST_PARIS);
        result = new MutableDateTime(0L);
        assertEquals(20, g.withChronology(BUDDHIST_PARIS).parseInto(result, "2004-06-09T10:20:30Z", 0));
        assertEquals(expect, result);
    }

// org.joda.time.format.TestDateTimeFormatter::testParseMillis_fractionOfSecondLong
    public void testParseMillis_fractionOfSecondLong() {
        DateTimeFormatter f = new DateTimeFormatterBuilder()
            .appendSecondOfDay(2).appendLiteral('.').appendFractionOfSecond(1, 9)
                .toFormatter().withZoneUTC();
        assertEquals(10512, f.parseMillis("10.5123456"));
        assertEquals(10512, f.parseMillis("10.512999"));
    }

// org.joda.time.format.TestDateTimeFormatter::testZoneNameNearTransition
    public void testZoneNameNearTransition() {}

// org.joda.time.format.TestDateTimeFormatter::testZoneShortNameNearTransition
    public void testZoneShortNameNearTransition() {}

// org.joda.time.format.TestDateTimeFormatterBuilder::test_toFormatter
    public void test_toFormatter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        try {
            bld.toFormatter();
            fail();
        } catch (UnsupportedOperationException ex) {}
        bld.appendLiteral('X');
        assertNotNull(bld.toFormatter());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_toPrinter
    public void test_toPrinter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        try {
            bld.toPrinter();
            fail();
        } catch (UnsupportedOperationException ex) {}
        bld.appendLiteral('X');
        assertNotNull(bld.toPrinter());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_toParser
    public void test_toParser() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        try {
            bld.toParser();
            fail();
        } catch (UnsupportedOperationException ex) {}
        bld.appendLiteral('X');
        assertNotNull(bld.toParser());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_canBuildFormatter
    public void test_canBuildFormatter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        assertEquals(false, bld.canBuildFormatter());
        bld.appendLiteral('X');
        assertEquals(true, bld.canBuildFormatter());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_canBuildPrinter
    public void test_canBuildPrinter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        assertEquals(false, bld.canBuildPrinter());
        bld.appendLiteral('X');
        assertEquals(true, bld.canBuildPrinter());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_canBuildParser
    public void test_canBuildParser() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        assertEquals(false, bld.canBuildParser());
        bld.appendLiteral('X');
        assertEquals(true, bld.canBuildParser());
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_append_Formatter
    public void test_append_Formatter() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendLiteral('Y');
        DateTimeFormatter f = bld.toFormatter();
        
        DateTimeFormatterBuilder bld2 = new DateTimeFormatterBuilder();
        bld2.appendLiteral('X');
        bld2.append(f);
        bld2.appendLiteral('Z');
        assertEquals("XYZ", bld2.toFormatter().print(0L));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_append_Printer
    public void test_append_Printer() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendLiteral('Y');
        DateTimePrinter p = bld.toPrinter();
        
        DateTimeFormatterBuilder bld2 = new DateTimeFormatterBuilder();
        bld2.appendLiteral('X');
        bld2.append(p);
        bld2.appendLiteral('Z');
        assertEquals("XYZ", bld2.toFormatter().print(0L));
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_appendFixedDecimal
    public void test_appendFixedDecimal() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendFixedDecimal(DateTimeFieldType.year(), 4);
        DateTimeFormatter f = bld.toFormatter();

        assertEquals("2007", f.print(new DateTime("2007-01-01")));
        assertEquals("0123", f.print(new DateTime("123-01-01")));
        assertEquals("0001", f.print(new DateTime("1-2-3")));
        assertEquals("99999", f.print(new DateTime("99999-2-3")));
        assertEquals("-0099", f.print(new DateTime("-99-2-3")));
        assertEquals("0000", f.print(new DateTime("0-2-3")));

        assertEquals(2001, f.parseDateTime("2001").getYear());
        try {
            f.parseDateTime("-2001");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            f.parseDateTime("200");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            f.parseDateTime("20016");
            fail();
        } catch (IllegalArgumentException e) {
        }

        bld = new DateTimeFormatterBuilder();
        bld.appendFixedDecimal(DateTimeFieldType.hourOfDay(), 2);
        bld.appendLiteral(':');
        bld.appendFixedDecimal(DateTimeFieldType.minuteOfHour(), 2);
        bld.appendLiteral(':');
        bld.appendFixedDecimal(DateTimeFieldType.secondOfMinute(), 2);
        f = bld.toFormatter();

        assertEquals("01:02:34", f.print(new DateTime("T1:2:34")));

        DateTime dt = f.parseDateTime("01:02:34");
        assertEquals(1, dt.getHourOfDay());
        assertEquals(2, dt.getMinuteOfHour());
        assertEquals(34, dt.getSecondOfMinute());

        try {
            f.parseDateTime("0145:02:34");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            f.parseDateTime("01:0:34");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

// org.joda.time.format.TestDateTimeFormatterBuilder::test_appendFixedSignedDecimal
    public void test_appendFixedSignedDecimal() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder();
        bld.appendFixedSignedDecimal(DateTimeFieldType.year(), 4);
        DateTimeFormatter f = bld.toFormatter();

        assertEquals("2007", f.print(new DateTime("2007-01-01")));
        assertEquals("0123", f.print(new DateTime("123-01-01")));
        assertEquals("0001", f.print(new DateTime("1-2-3")));
        assertEquals("99999", f.print(new DateTime("99999-2-3")));
        assertEquals("-0099", f.print(new DateTime("-99-2-3")));
        assertEquals("0000", f.print(new DateTime("0-2-3")));

        assertEquals(2001, f.parseDateTime("2001").getYear());
        assertEquals(-2001, f.parseDateTime("-2001").getYear());
        assertEquals(2001, f.parseDateTime("+2001").getYear());
        try {
            f.parseDateTime("20016");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
