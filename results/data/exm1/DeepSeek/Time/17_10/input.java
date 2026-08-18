// buggy code
    public long adjustOffset(long instant, boolean earlierOrLater) {
        // a bit messy, but will work in all non-pathological cases
        
        // evaluate 3 hours before and after to work out if anything is happening
        long instantBefore = convertUTCToLocal(instant - 3 * DateTimeConstants.MILLIS_PER_HOUR);
        long instantAfter = convertUTCToLocal(instant + 3 * DateTimeConstants.MILLIS_PER_HOUR);
        if (instantBefore == instantAfter) {
            return instant;  // not an overlap (less than is a gap, equal is normal case)
        }
        
        // work out range of instants that have duplicate local times
        long local = convertUTCToLocal(instant);
        return convertLocalToUTC(local, false, earlierOrLater ? instantAfter : instantBefore);
        
        // calculate result
          // currently in later offset
          // currently in earlier offset
    }

// relevant test
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

// org.joda.time.convert.TestDateConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = DateConverter.class;
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

// org.joda.time.convert.TestDateConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(Date.class, DateConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestDateConverter::testGetInstantMillis_Object_Chronology
    public void testGetInstantMillis_Object_Chronology() throws Exception {
        Date date = new Date(123L);
        long millis = DateConverter.INSTANCE.getInstantMillis(date, JULIAN);
        assertEquals(123L, millis);
        assertEquals(123L, DateConverter.INSTANCE.getInstantMillis(date, (Chronology) null));
    }

// org.joda.time.convert.TestDateConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        assertEquals(ISO_PARIS, DateConverter.INSTANCE.getChronology(new Date(123L), PARIS));
        assertEquals(ISO, DateConverter.INSTANCE.getChronology(new Date(123L), (DateTimeZone) null));
    }

// org.joda.time.convert.TestDateConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        assertEquals(JULIAN, DateConverter.INSTANCE.getChronology(new Date(123L), JULIAN));
        assertEquals(ISO, DateConverter.INSTANCE.getChronology(new Date(123L), (Chronology) null));
    }

// org.joda.time.convert.TestDateConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        TimeOfDay tod = new TimeOfDay();
        int[] expected = COPTIC.get(tod, 12345678L);
        int[] actual = DateConverter.INSTANCE.getPartialValues(tod, new Date(12345678L), COPTIC);
        assertEquals(true, Arrays.equals(expected, actual));
    }

// org.joda.time.convert.TestDateConverter::testToString
    public void testToString() {
        assertEquals("Converter[java.util.Date]", DateConverter.INSTANCE.toString());
    }

// org.joda.time.convert.TestLongConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = LongConverter.class;
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

// org.joda.time.convert.TestLongConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(Long.class, LongConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestLongConverter::testGetInstantMillis_Object_Chronology
    public void testGetInstantMillis_Object_Chronology() throws Exception {
        assertEquals(123L, LongConverter.INSTANCE.getInstantMillis(new Long(123L), JULIAN));
        assertEquals(123L, LongConverter.INSTANCE.getInstantMillis(new Long(123L), (Chronology) null));
    }

// org.joda.time.convert.TestLongConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        assertEquals(ISO_PARIS, LongConverter.INSTANCE.getChronology(new Long(123L), PARIS));
        assertEquals(ISO, LongConverter.INSTANCE.getChronology(new Long(123L), (DateTimeZone) null));
    }

// org.joda.time.convert.TestLongConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        assertEquals(JULIAN, LongConverter.INSTANCE.getChronology(new Long(123L), JULIAN));
        assertEquals(ISO, LongConverter.INSTANCE.getChronology(new Long(123L), (Chronology) null));
    }

// org.joda.time.convert.TestLongConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        TimeOfDay tod = new TimeOfDay();
        int[] expected = ISOChronology.getInstance().get(tod, 12345678L);
        int[] actual = LongConverter.INSTANCE.getPartialValues(tod, new Long(12345678L), ISOChronology.getInstance());
        assertEquals(true, Arrays.equals(expected, actual));
    }

// org.joda.time.convert.TestLongConverter::testGetDurationMillis_Object
    public void testGetDurationMillis_Object() throws Exception {
        assertEquals(123L, LongConverter.INSTANCE.getDurationMillis(new Long(123L)));
    }

// org.joda.time.convert.TestLongConverter::testToString
    public void testToString() {
        assertEquals("Converter[java.lang.Long]", LongConverter.INSTANCE.toString());
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

// org.joda.time.convert.TestReadableDurationConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = ReadableDurationConverter.class;
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

// org.joda.time.convert.TestReadableDurationConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(ReadableDuration.class, ReadableDurationConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestReadableDurationConverter::testGetDurationMillis_Object
    public void testGetDurationMillis_Object() throws Exception {
        assertEquals(123L, ReadableDurationConverter.INSTANCE.getDurationMillis(new Duration(123L)));
    }

// org.joda.time.convert.TestReadableDurationConverter::testGetPeriodType_Object
    public void testGetPeriodType_Object() throws Exception {
        assertEquals(PeriodType.standard(),
            ReadableDurationConverter.INSTANCE.getPeriodType(new Duration(123L)));
    }

// org.joda.time.convert.TestReadableDurationConverter::testSetInto_Object
    public void testSetInto_Object() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearMonthDayTime());
        ReadableDurationConverter.INSTANCE.setInto(m, new Duration(
            3L * DateTimeConstants.MILLIS_PER_DAY +
            4L * DateTimeConstants.MILLIS_PER_MINUTE + 5L
        ), null);
        assertEquals(0, m.getYears());
        assertEquals(0, m.getMonths());
        assertEquals(0, m.getWeeks());
        assertEquals(0, m.getDays());
        assertEquals(3 * 24, m.getHours());
        assertEquals(4, m.getMinutes());
        assertEquals(0, m.getSeconds());
        assertEquals(5, m.getMillis());
    }

// org.joda.time.convert.TestReadableDurationConverter::testToString
    public void testToString() {
        assertEquals("Converter[org.joda.time.ReadableDuration]", ReadableDurationConverter.INSTANCE.toString());
    }

// org.joda.time.convert.TestReadableInstantConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = ReadableInstantConverter.class;
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

// org.joda.time.convert.TestReadableInstantConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(ReadableInstant.class, ReadableInstantConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestReadableInstantConverter::testGetInstantMillis_Object_Chronology
    public void testGetInstantMillis_Object_Chronology() throws Exception {
        assertEquals(123L, ReadableInstantConverter.INSTANCE.getInstantMillis(new Instant(123L), JULIAN));
        assertEquals(123L, ReadableInstantConverter.INSTANCE.getInstantMillis(new DateTime(123L), JULIAN));
        assertEquals(123L, ReadableInstantConverter.INSTANCE.getInstantMillis(new Instant(123L), (Chronology) null));
        assertEquals(123L, ReadableInstantConverter.INSTANCE.getInstantMillis(new DateTime(123L), (Chronology) null));
    }

// org.joda.time.convert.TestReadableInstantConverter::testGetChronology_Object_Zone
    public void testGetChronology_Object_Zone() throws Exception {
        assertEquals(ISO_PARIS, ReadableInstantConverter.INSTANCE.getChronology(new Instant(123L), PARIS));
        assertEquals(ISO_PARIS, ReadableInstantConverter.INSTANCE.getChronology(new DateTime(123L), PARIS));
        assertEquals(ISO, ReadableInstantConverter.INSTANCE.getChronology(new Instant(123L), DateTimeZone.getDefault()));
        assertEquals(ISO, ReadableInstantConverter.INSTANCE.getChronology(new DateTime(123L), DateTimeZone.getDefault()));
        assertEquals(ISO, ReadableInstantConverter.INSTANCE.getChronology(new Instant(123L), (DateTimeZone) null));
        assertEquals(ISO, ReadableInstantConverter.INSTANCE.getChronology(new DateTime(123L), (DateTimeZone) null));
        
        assertEquals(ISO_PARIS, ReadableInstantConverter.INSTANCE.getChronology(new DateTime(123L, new MockBadChronology()), PARIS));
        
        MutableDateTime mdt = new MutableDateTime() {
            public Chronology getChronology() {
                return null; 
            }
        };
        assertEquals(ISO_PARIS, ReadableInstantConverter.INSTANCE.getChronology(mdt, PARIS));
    }

// org.joda.time.convert.TestReadableInstantConverter::testGetChronology_Object_nullChronology
    public void testGetChronology_Object_nullChronology() throws Exception {
        assertEquals(ISO.withUTC(), ReadableInstantConverter.INSTANCE.getChronology(new Instant(123L), (Chronology) null));
        assertEquals(ISO, ReadableInstantConverter.INSTANCE.getChronology(new DateTime(123L), (Chronology) null));
        
        MutableDateTime mdt = new MutableDateTime() {
            public Chronology getChronology() {
                return null; 
            }
        };
        assertEquals(ISO, ReadableInstantConverter.INSTANCE.getChronology(mdt, (Chronology) null));
    }

// org.joda.time.convert.TestReadableInstantConverter::testGetChronology_Object_Chronology
    public void testGetChronology_Object_Chronology() throws Exception {
        assertEquals(JULIAN, ReadableInstantConverter.INSTANCE.getChronology(new Instant(123L), JULIAN));
        assertEquals(JULIAN, ReadableInstantConverter.INSTANCE.getChronology(new DateTime(123L), JULIAN));
    }

// org.joda.time.convert.TestReadableInstantConverter::testGetPartialValues
    public void testGetPartialValues() throws Exception {
        TimeOfDay tod = new TimeOfDay();
        int[] expected = ISOChronology.getInstance().get(tod, 12345678L);
        int[] actual = ReadableInstantConverter.INSTANCE.getPartialValues(tod, new Instant(12345678L), ISOChronology.getInstance());
        assertEquals(true, Arrays.equals(expected, actual));
    }

// org.joda.time.convert.TestReadableInstantConverter::testToString
    public void testToString() {
        assertEquals("Converter[org.joda.time.ReadableInstant]", ReadableInstantConverter.INSTANCE.toString());
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

// org.joda.time.convert.TestReadablePeriodConverter::testSingleton
    public void testSingleton() throws Exception {
        Class cls = ReadablePeriodConverter.class;
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

// org.joda.time.convert.TestReadablePeriodConverter::testSupportedType
    public void testSupportedType() throws Exception {
        assertEquals(ReadablePeriod.class, ReadablePeriodConverter.INSTANCE.getSupportedType());
    }

// org.joda.time.convert.TestReadablePeriodConverter::testGetPeriodType_Object
    public void testGetPeriodType_Object() throws Exception {
        assertEquals(PeriodType.standard(),
            ReadablePeriodConverter.INSTANCE.getPeriodType(new Period(123L, PeriodType.standard())));
        assertEquals(PeriodType.yearMonthDayTime(),
            ReadablePeriodConverter.INSTANCE.getPeriodType(new Period(123L, PeriodType.yearMonthDayTime())));
    }

// org.joda.time.convert.TestReadablePeriodConverter::testSetInto_Object
    public void testSetInto_Object() throws Exception {
        MutablePeriod m = new MutablePeriod(PeriodType.yearMonthDayTime());
        ReadablePeriodConverter.INSTANCE.setInto(m, new Period(0, 0, 0, 3, 0, 4, 0, 5), null);
        assertEquals(0, m.getYears());
        assertEquals(0, m.getMonths());
        assertEquals(0, m.getWeeks());
        assertEquals(3, m.getDays());
        assertEquals(0, m.getHours());
        assertEquals(4, m.getMinutes());
        assertEquals(0, m.getSeconds());
        assertEquals(5, m.getMillis());
    }

// org.joda.time.convert.TestReadablePeriodConverter::testToString
    public void testToString() {
        assertEquals("Converter[org.joda.time.ReadablePeriod]", ReadablePeriodConverter.INSTANCE.toString());
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

// org.joda.time.field.TestBaseDateTimeField::test_constructor
    public void test_constructor() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(DateTimeFieldType.secondOfMinute(), field.getType());
        try {
            field = new MockBaseDateTimeField(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestBaseDateTimeField::test_getType
    public void test_getType() {
        BaseDateTimeField field = new MockBaseDateTimeField(DateTimeFieldType.secondOfDay());
        assertEquals(DateTimeFieldType.secondOfDay(), field.getType());
    }

// org.joda.time.field.TestBaseDateTimeField::test_getName
    public void test_getName() {
        BaseDateTimeField field = new MockBaseDateTimeField(DateTimeFieldType.secondOfDay());
        assertEquals("secondOfDay", field.getName());
    }

// org.joda.time.field.TestBaseDateTimeField::test_toString
    public void test_toString() {
        BaseDateTimeField field = new MockBaseDateTimeField(DateTimeFieldType.secondOfDay());
        assertEquals("DateTimeField[secondOfDay]", field.toString());
    }

// org.joda.time.field.TestBaseDateTimeField::test_isSupported
    public void test_isSupported() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(true, field.isSupported());
    }

// org.joda.time.field.TestBaseDateTimeField::test_get
    public void test_get() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.get(0));
        assertEquals(1, field.get(60));
        assertEquals(2, field.get(123));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsText_long_Locale
    public void test_getAsText_long_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("29", field.getAsText(60L * 29, Locale.ENGLISH));
        assertEquals("29", field.getAsText(60L * 29, null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsText_long
    public void test_getAsText_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("29", field.getAsText(60L * 29));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsText_RP_int_Locale
    public void test_getAsText_RP_int_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("20", field.getAsText(new TimeOfDay(12, 30, 40, 50), 20, Locale.ENGLISH));
        assertEquals("20", field.getAsText(new TimeOfDay(12, 30, 40, 50), 20, null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsText_RP_Locale
    public void test_getAsText_RP_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("40", field.getAsText(new TimeOfDay(12, 30, 40, 50), Locale.ENGLISH));
        assertEquals("40", field.getAsText(new TimeOfDay(12, 30, 40, 50), null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsText_int_Locale
    public void test_getAsText_int_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("80", field.getAsText(80, Locale.ENGLISH));
        assertEquals("80", field.getAsText(80, null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsShortText_long_Locale
    public void test_getAsShortText_long_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("29", field.getAsShortText(60L * 29, Locale.ENGLISH));
        assertEquals("29", field.getAsShortText(60L * 29, null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsShortText_long
    public void test_getAsShortText_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("29", field.getAsShortText(60L * 29));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsShortText_RP_int_Locale
    public void test_getAsShortText_RP_int_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("20", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), 20, Locale.ENGLISH));
        assertEquals("20", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), 20, null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsShortText_RP_Locale
    public void test_getAsShortText_RP_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("40", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), Locale.ENGLISH));
        assertEquals("40", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getAsShortText_int_Locale
    public void test_getAsShortText_int_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals("80", field.getAsShortText(80, Locale.ENGLISH));
        assertEquals("80", field.getAsShortText(80, null));
    }

// org.joda.time.field.TestBaseDateTimeField::test_add_long_int
    public void test_add_long_int() {
        MockCountingDurationField.add_int = 0;
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(61, field.add(1L, 1));
        assertEquals(1, MockCountingDurationField.add_int);
    }

// org.joda.time.field.TestBaseDateTimeField::test_add_long_long
    public void test_add_long_long() {
        MockCountingDurationField.add_long = 0;
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(61, field.add(1L, 1L));
        assertEquals(1, MockCountingDurationField.add_long);
    }

// org.joda.time.field.TestBaseDateTimeField::test_add_RP_int_intarray_int
    public void test_add_RP_int_intarray_int() {
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        BaseDateTimeField field = new MockStandardBaseDateTimeField();
        int[] result = field.add(new TimeOfDay(), 2, values, 0);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 31, 40};
        result = field.add(new TimeOfDay(), 2, values, 1);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 21, 0, 40};
        result = field.add(new TimeOfDay(), 2, values, 30);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {23, 59, 30, 40};
        try {
            field.add(new TimeOfDay(), 2, values, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 29, 40};
        result = field.add(new TimeOfDay(), 2, values, -1);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 19, 59, 40};
        result = field.add(new TimeOfDay(), 2, values, -31);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {0, 0, 30, 40};
        try {
            field.add(new TimeOfDay(), 2, values, -31);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        values = new int[] {0, 0};
        try {
            field.add(new MockPartial(), 0, values, 1000);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        values = new int[] {1, 0};
        try {
            field.add(new MockPartial(), 0, values, -1000);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestBaseDateTimeField::test_addWrapField_long_int
    public void test_addWrapField_long_int() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(1029, field.addWrapField(60L * 29, 0));
        assertEquals(1059, field.addWrapField(60L * 29, 30));
        assertEquals(1000, field.addWrapField(60L * 29, 31));
    }

// org.joda.time.field.TestBaseDateTimeField::test_addWrapField_RP_int_intarray_int
    public void test_addWrapField_RP_int_intarray_int() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        int[] result = field.addWrapField(new TimeOfDay(), 2, values, 0);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 59, 40};
        result = field.addWrapField(new TimeOfDay(), 2, values, 29);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 0, 40};
        result = field.addWrapField(new TimeOfDay(), 2, values, 30);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 1, 40};
        result = field.addWrapField(new TimeOfDay(), 2, values, 31);
        assertEquals(true, Arrays.equals(result, expected));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getDifference_long_long
    public void test_getDifference_long_long() {
        MockCountingDurationField.difference_long = 0;
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(30, field.getDifference(0L, 0L));
        assertEquals(1, MockCountingDurationField.difference_long);
    }

// org.joda.time.field.TestBaseDateTimeField::test_getDifferenceAsLong_long_long
    public void test_getDifferenceAsLong_long_long() {
        MockCountingDurationField.difference_long = 0;
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(30, field.getDifferenceAsLong(0L, 0L));
        assertEquals(1, MockCountingDurationField.difference_long);
    }

// org.joda.time.field.TestBaseDateTimeField::test_set_long_int
    public void test_set_long_int() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(1000, field.set(0L, 0));
        assertEquals(1029, field.set(0L, 29));
    }

// org.joda.time.field.TestBaseDateTimeField::test_set_RP_int_intarray_int
    public void test_set_RP_int_intarray_int() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        int[] result = field.set(new TimeOfDay(), 2, values, 30);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 29, 40};
        result = field.set(new TimeOfDay(), 2, values, 29);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
    }

// org.joda.time.field.TestBaseDateTimeField::test_set_long_String_Locale
    public void test_set_long_String_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(1000, field.set(0L, "0", null));
        assertEquals(1029, field.set(0L, "29", Locale.ENGLISH));
    }

// org.joda.time.field.TestBaseDateTimeField::test_set_long_String
    public void test_set_long_String() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(1000, field.set(0L, "0"));
        assertEquals(1029, field.set(0L, "29"));
    }

// org.joda.time.field.TestBaseDateTimeField::test_set_RP_int_intarray_String_Locale
    public void test_set_RP_int_intarray_String_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        int[] result = field.set(new TimeOfDay(), 2, values, "30", null);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 29, 40};
        result = field.set(new TimeOfDay(), 2, values, "29", Locale.ENGLISH);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, "60", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, "-1", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
    }

// org.joda.time.field.TestBaseDateTimeField::test_convertText
    public void test_convertText() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.convertText("0", null));
        assertEquals(29, field.convertText("29", null));
        try {
            field.convertText("2A", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            field.convertText(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestBaseDateTimeField::test_isLeap_long
    public void test_isLeap_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(false, field.isLeap(0L));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getLeapAmount_long
    public void test_getLeapAmount_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.getLeapAmount(0L));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getLeapDurationField
    public void test_getLeapDurationField() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(null, field.getLeapDurationField());
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMinimumValue
    public void test_getMinimumValue() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.getMinimumValue());
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMinimumValue_long
    public void test_getMinimumValue_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.getMinimumValue(0L));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMinimumValue_RP
    public void test_getMinimumValue_RP() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.getMinimumValue(new TimeOfDay()));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMinimumValue_RP_intarray
    public void test_getMinimumValue_RP_intarray() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0, field.getMinimumValue(new TimeOfDay(), new int[4]));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMaximumValue
    public void test_getMaximumValue() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(59, field.getMaximumValue());
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMaximumValue_long
    public void test_getMaximumValue_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(59, field.getMaximumValue(0L));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMaximumValue_RP
    public void test_getMaximumValue_RP() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(59, field.getMaximumValue(new TimeOfDay()));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMaximumValue_RP_intarray
    public void test_getMaximumValue_RP_intarray() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(59, field.getMaximumValue(new TimeOfDay(), new int[4]));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMaximumTextLength_Locale
    public void test_getMaximumTextLength_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(2, field.getMaximumTextLength(Locale.ENGLISH));

        field = new MockBaseDateTimeField() {
            public int getMaximumValue() {
                return 5;
            }
        };
        assertEquals(1, field.getMaximumTextLength(Locale.ENGLISH));
        
        field = new MockBaseDateTimeField() {
            public int getMaximumValue() {
                return 555;
            }
        };
        assertEquals(3, field.getMaximumTextLength(Locale.ENGLISH));
        
        field = new MockBaseDateTimeField() {
            public int getMaximumValue() {
                return 5555;
            }
        };
        assertEquals(4, field.getMaximumTextLength(Locale.ENGLISH));
        
        field = new MockBaseDateTimeField() {
            public int getMaximumValue() {
                return -1;
            }
        };
        assertEquals(2, field.getMaximumTextLength(Locale.ENGLISH));
    }

// org.joda.time.field.TestBaseDateTimeField::test_getMaximumShortTextLength_Locale
    public void test_getMaximumShortTextLength_Locale() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(2, field.getMaximumShortTextLength(Locale.ENGLISH));
    }

// org.joda.time.field.TestBaseDateTimeField::test_roundFloor_long
    public void test_roundFloor_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0L, field.roundFloor(0L));
        assertEquals(0L, field.roundFloor(29L));
        assertEquals(0L, field.roundFloor(30L));
        assertEquals(0L, field.roundFloor(31L));
        assertEquals(60L, field.roundFloor(60L));
    }

// org.joda.time.field.TestBaseDateTimeField::test_roundCeiling_long
    public void test_roundCeiling_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0L, field.roundCeiling(0L));
        assertEquals(60L, field.roundCeiling(29L));
        assertEquals(60L, field.roundCeiling(30L));
        assertEquals(60L, field.roundCeiling(31L));
        assertEquals(60L, field.roundCeiling(60L));
    }

// org.joda.time.field.TestBaseDateTimeField::test_roundHalfFloor_long
    public void test_roundHalfFloor_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0L, field.roundHalfFloor(0L));
        assertEquals(0L, field.roundHalfFloor(29L));
        assertEquals(0L, field.roundHalfFloor(30L));
        assertEquals(60L, field.roundHalfFloor(31L));
        assertEquals(60L, field.roundHalfFloor(60L));
    }

// org.joda.time.field.TestBaseDateTimeField::test_roundHalfCeiling_long
    public void test_roundHalfCeiling_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0L, field.roundHalfCeiling(0L));
        assertEquals(0L, field.roundHalfCeiling(29L));
        assertEquals(60L, field.roundHalfCeiling(30L));
        assertEquals(60L, field.roundHalfCeiling(31L));
        assertEquals(60L, field.roundHalfCeiling(60L));
    }

// org.joda.time.field.TestBaseDateTimeField::test_roundHalfEven_long
    public void test_roundHalfEven_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0L, field.roundHalfEven(0L));
        assertEquals(0L, field.roundHalfEven(29L));
        assertEquals(0L, field.roundHalfEven(30L));
        assertEquals(60L, field.roundHalfEven(31L));
        assertEquals(60L, field.roundHalfEven(60L));
        assertEquals(60L, field.roundHalfEven(89L));
        assertEquals(120L, field.roundHalfEven(90L));
        assertEquals(120L, field.roundHalfEven(91L));
    }

// org.joda.time.field.TestBaseDateTimeField::test_remainder_long
    public void test_remainder_long() {
        BaseDateTimeField field = new MockBaseDateTimeField();
        assertEquals(0L, field.remainder(0L));
        assertEquals(29L, field.remainder(29L));
        assertEquals(30L, field.remainder(30L));
        assertEquals(31L, field.remainder(31L));
        assertEquals(0L, field.remainder(60L));
    }

// org.joda.time.field.TestMillisDurationField::test_getType
    public void test_getType() {
        assertEquals(DurationFieldType.millis(), MillisDurationField.INSTANCE.getType());
    }

// org.joda.time.field.TestMillisDurationField::test_getName
    public void test_getName() {
        assertEquals("millis", MillisDurationField.INSTANCE.getName());
    }

// org.joda.time.field.TestMillisDurationField::test_isSupported
    public void test_isSupported() {
        assertEquals(true, MillisDurationField.INSTANCE.isSupported());
    }

// org.joda.time.field.TestMillisDurationField::test_isPrecise
    public void test_isPrecise() {
        assertEquals(true, MillisDurationField.INSTANCE.isPrecise());
    }

// org.joda.time.field.TestMillisDurationField::test_getUnitMillis
    public void test_getUnitMillis() {
        assertEquals(1, MillisDurationField.INSTANCE.getUnitMillis());
    }

// org.joda.time.field.TestMillisDurationField::test_toString
    public void test_toString() {
        assertEquals("DurationField[millis]", MillisDurationField.INSTANCE.toString());
    }

// org.joda.time.field.TestMillisDurationField::test_getValue_long
    public void test_getValue_long() {
        assertEquals(0, MillisDurationField.INSTANCE.getValue(0L));
        assertEquals(1234, MillisDurationField.INSTANCE.getValue(1234L));
        assertEquals(-1234, MillisDurationField.INSTANCE.getValue(-1234L));
        try {
            MillisDurationField.INSTANCE.getValue(((long) (Integer.MAX_VALUE)) + 1L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestMillisDurationField::test_getValueAsLong_long
    public void test_getValueAsLong_long() {
        assertEquals(0L, MillisDurationField.INSTANCE.getValueAsLong(0L));
        assertEquals(1234L, MillisDurationField.INSTANCE.getValueAsLong(1234L));
        assertEquals(-1234L, MillisDurationField.INSTANCE.getValueAsLong(-1234L));
        assertEquals(((long) (Integer.MAX_VALUE)) + 1L, MillisDurationField.INSTANCE.getValueAsLong(((long) (Integer.MAX_VALUE)) + 1L));
    }

// org.joda.time.field.TestMillisDurationField::test_getValue_long_long
    public void test_getValue_long_long() {
        assertEquals(0, MillisDurationField.INSTANCE.getValue(0L, 567L));
        assertEquals(1234, MillisDurationField.INSTANCE.getValue(1234L, 567L));
        assertEquals(-1234, MillisDurationField.INSTANCE.getValue(-1234L, 567L));
        try {
            MillisDurationField.INSTANCE.getValue(((long) (Integer.MAX_VALUE)) + 1L, 567L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestMillisDurationField::test_getValueAsLong_long_long
    public void test_getValueAsLong_long_long() {
        assertEquals(0L, MillisDurationField.INSTANCE.getValueAsLong(0L, 567L));
        assertEquals(1234L, MillisDurationField.INSTANCE.getValueAsLong(1234L, 567L));
        assertEquals(-1234L, MillisDurationField.INSTANCE.getValueAsLong(-1234L, 567L));
        assertEquals(((long) (Integer.MAX_VALUE)) + 1L, MillisDurationField.INSTANCE.getValueAsLong(((long) (Integer.MAX_VALUE)) + 1L, 567L));
    }

// org.joda.time.field.TestMillisDurationField::test_getMillis_int
    public void test_getMillis_int() {
        assertEquals(0, MillisDurationField.INSTANCE.getMillis(0));
        assertEquals(1234, MillisDurationField.INSTANCE.getMillis(1234));
        assertEquals(-1234, MillisDurationField.INSTANCE.getMillis(-1234));
    }

// org.joda.time.field.TestMillisDurationField::test_getMillis_long
    public void test_getMillis_long() {
        assertEquals(0L, MillisDurationField.INSTANCE.getMillis(0L));
        assertEquals(1234L, MillisDurationField.INSTANCE.getMillis(1234L));
        assertEquals(-1234L, MillisDurationField.INSTANCE.getMillis(-1234L));
    }

// org.joda.time.field.TestMillisDurationField::test_getMillis_int_long
    public void test_getMillis_int_long() {
        assertEquals(0, MillisDurationField.INSTANCE.getMillis(0, 567L));
        assertEquals(1234, MillisDurationField.INSTANCE.getMillis(1234, 567L));
        assertEquals(-1234, MillisDurationField.INSTANCE.getMillis(-1234, 567L));
    }

// org.joda.time.field.TestMillisDurationField::test_getMillis_long_long
    public void test_getMillis_long_long() {
        assertEquals(0L, MillisDurationField.INSTANCE.getMillis(0L, 567L));
        assertEquals(1234L, MillisDurationField.INSTANCE.getMillis(1234L, 567L));
        assertEquals(-1234L, MillisDurationField.INSTANCE.getMillis(-1234L, 567L));
    }

// org.joda.time.field.TestMillisDurationField::test_add_long_int
    public void test_add_long_int() {
        assertEquals(567L, MillisDurationField.INSTANCE.add(567L, 0));
        assertEquals(567L + 1234L, MillisDurationField.INSTANCE.add(567L, 1234));
        assertEquals(567L - 1234L, MillisDurationField.INSTANCE.add(567L, -1234));
        try {
            MillisDurationField.INSTANCE.add(Long.MAX_VALUE, 1);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestMillisDurationField::test_add_long_long
    public void test_add_long_long() {
        assertEquals(567L, MillisDurationField.INSTANCE.add(567L, 0L));
        assertEquals(567L + 1234L, MillisDurationField.INSTANCE.add(567L, 1234L));
        assertEquals(567L - 1234L, MillisDurationField.INSTANCE.add(567L, -1234L));
        try {
            MillisDurationField.INSTANCE.add(Long.MAX_VALUE, 1L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestMillisDurationField::test_getDifference_long_int
    public void test_getDifference_long_int() {
        assertEquals(567, MillisDurationField.INSTANCE.getDifference(567L, 0L));
        assertEquals(567 - 1234, MillisDurationField.INSTANCE.getDifference(567L, 1234L));
        assertEquals(567 + 1234, MillisDurationField.INSTANCE.getDifference(567L, -1234L));
        try {
            MillisDurationField.INSTANCE.getDifference(Long.MAX_VALUE, 1L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestMillisDurationField::test_getDifferenceAsLong_long_long
    public void test_getDifferenceAsLong_long_long() {
        assertEquals(567L, MillisDurationField.INSTANCE.getDifferenceAsLong(567L, 0L));
        assertEquals(567L - 1234L, MillisDurationField.INSTANCE.getDifferenceAsLong(567L, 1234L));
        assertEquals(567L + 1234L, MillisDurationField.INSTANCE.getDifferenceAsLong(567L, -1234L));
        try {
            MillisDurationField.INSTANCE.getDifferenceAsLong(Long.MAX_VALUE, -1L);
            fail();
        } catch (ArithmeticException ex) {}
    }

// org.joda.time.field.TestMillisDurationField::test_compareTo
    public void test_compareTo() {
        assertEquals(0, MillisDurationField.INSTANCE.compareTo(MillisDurationField.INSTANCE));
        assertEquals(-1, MillisDurationField.INSTANCE.compareTo(ISOChronology.getInstance().seconds()));
        DurationField dummy = new PreciseDurationField(DurationFieldType.seconds(), 0);
        assertEquals(1, MillisDurationField.INSTANCE.compareTo(dummy));

        try {
            MillisDurationField.INSTANCE.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}
    }

// org.joda.time.field.TestMillisDurationField::testSerialization
    public void testSerialization() throws Exception {
        DurationField test = MillisDurationField.INSTANCE;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DurationField result = (DurationField) ois.readObject();
        ois.close();
        
        assertSame(test, result);
    }

// org.joda.time.field.TestOffsetDateTimeField::test_constructor1
    public void test_constructor1() {
        OffsetDateTimeField field = new OffsetDateTimeField(
            ISOChronology.getInstance().secondOfMinute(), 3
        );
        assertEquals(DateTimeFieldType.secondOfMinute(), field.getType());
        assertEquals(3, field.getOffset());
        
        try {
            field = new OffsetDateTimeField(null, 3);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        try {
            field = new OffsetDateTimeField(ISOChronology.getInstance().secondOfMinute(), 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        try {
            field = new OffsetDateTimeField(UnsupportedDateTimeField.getInstance(
                DateTimeFieldType.secondOfMinute(), UnsupportedDurationField.getInstance(DurationFieldType.seconds())), 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestOffsetDateTimeField::test_constructor2
    public void test_constructor2() {
        OffsetDateTimeField field = new OffsetDateTimeField(
            ISOChronology.getInstance().secondOfMinute(), DateTimeFieldType.secondOfDay(), 3
        );
        assertEquals(DateTimeFieldType.secondOfDay(), field.getType());
        assertEquals(3, field.getOffset());
        
        try {
            field = new OffsetDateTimeField(null, DateTimeFieldType.secondOfDay(), 3);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        try {
            field = new OffsetDateTimeField(ISOChronology.getInstance().secondOfMinute(), null, 3);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        try {
            field = new OffsetDateTimeField(ISOChronology.getInstance().secondOfMinute(), DateTimeFieldType.secondOfDay(), 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getType
    public void test_getType() {
        OffsetDateTimeField field = new OffsetDateTimeField(
            ISOChronology.getInstance().secondOfMinute(), 3
        );
        assertEquals(DateTimeFieldType.secondOfMinute(), field.getType());
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getName
    public void test_getName() {
        OffsetDateTimeField field = new OffsetDateTimeField(
            ISOChronology.getInstance().secondOfMinute(), 3
        );
        assertEquals("secondOfMinute", field.getName());
    }

// org.joda.time.field.TestOffsetDateTimeField::test_toString
    public void test_toString() {
        OffsetDateTimeField field = new OffsetDateTimeField(
            ISOChronology.getInstance().secondOfMinute(), 3
        );
        assertEquals("DateTimeField[secondOfMinute]", field.toString());
    }

// org.joda.time.field.TestOffsetDateTimeField::test_isSupported
    public void test_isSupported() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(true, field.isSupported());
    }

// org.joda.time.field.TestOffsetDateTimeField::test_isLenient
    public void test_isLenient() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(false, field.isLenient());
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getOffset
    public void test_getOffset() {
        OffsetDateTimeField field = new OffsetDateTimeField(
            ISOChronology.getInstance().secondOfMinute(), 5
        );
        assertEquals(5, field.getOffset());
    }

// org.joda.time.field.TestOffsetDateTimeField::test_get
    public void test_get() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(0 + 3, field.get(0));
        assertEquals(6 + 3, field.get(6000));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getAsText_long_Locale
    public void test_getAsText_long_Locale() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals("32", field.getAsText(1000L * 29, Locale.ENGLISH));
        assertEquals("32", field.getAsText(1000L * 29, null));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getAsText_long
    public void test_getAsText_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals("32", field.getAsText(1000L * 29));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getAsText_RP_int_Locale
    public void test_getAsText_RP_int_Locale() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals("20", field.getAsText(new TimeOfDay(12, 30, 40, 50), 20, Locale.ENGLISH));
        assertEquals("20", field.getAsText(new TimeOfDay(12, 30, 40, 50), 20, null));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getAsText_RP_Locale
    public void test_getAsText_RP_Locale() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals("40", field.getAsText(new TimeOfDay(12, 30, 40, 50), Locale.ENGLISH));
        assertEquals("40", field.getAsText(new TimeOfDay(12, 30, 40, 50), null));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getAsText_int_Locale
    public void test_getAsText_int_Locale() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals("80", field.getAsText(80, Locale.ENGLISH));
        assertEquals("80", field.getAsText(80, null));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getAsShortText_long_Locale
    public void test_getAsShortText_long_Locale() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals("32", field.getAsShortText(1000L * 29, Locale.ENGLISH));
        assertEquals("32", field.getAsShortText(1000L * 29, null));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getAsShortText_long
    public void test_getAsShortText_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals("32", field.getAsShortText(1000L * 29));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getAsShortText_RP_int_Locale
    public void test_getAsShortText_RP_int_Locale() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals("20", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), 20, Locale.ENGLISH));
        assertEquals("20", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), 20, null));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getAsShortText_RP_Locale
    public void test_getAsShortText_RP_Locale() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals("40", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), Locale.ENGLISH));
        assertEquals("40", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), null));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getAsShortText_int_Locale
    public void test_getAsShortText_int_Locale() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals("80", field.getAsShortText(80, Locale.ENGLISH));
        assertEquals("80", field.getAsShortText(80, null));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_add_long_int
    public void test_add_long_int() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(1001, field.add(1L, 1));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_add_long_long
    public void test_add_long_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(1001, field.add(1L, 1L));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_add_RP_int_intarray_int
    public void test_add_RP_int_intarray_int() {
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        OffsetDateTimeField field = new MockStandardDateTimeField();
        int[] result = field.add(new TimeOfDay(), 2, values, 0);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 31, 40};
        result = field.add(new TimeOfDay(), 2, values, 1);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 62, 40};
        result = field.add(new TimeOfDay(), 2, values, 32);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 21, 3, 40};
        result = field.add(new TimeOfDay(), 2, values, 33);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {23, 59, 30, 40};
        try {
            field.add(new TimeOfDay(), 2, values, 33);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 29, 40};
        result = field.add(new TimeOfDay(), 2, values, -1);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 19, 59, 40};
        result = field.add(new TimeOfDay(), 2, values, -31);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {0, 0, 30, 40};
        try {
            field.add(new TimeOfDay(), 2, values, -31);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestOffsetDateTimeField::test_addWrapField_long_int
    public void test_addWrapField_long_int() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(29 * 1000L, field.addWrapField(1000L * 29, 0));
        assertEquals(59 * 1000L, field.addWrapField(1000L * 29, 30));
        assertEquals(0L, field.addWrapField(1000L * 29, 31));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_addWrapField_RP_int_intarray_int
    public void test_addWrapField_RP_int_intarray_int() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        int[] result = field.addWrapField(new TimeOfDay(), 2, values, 0);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 59, 40};
        result = field.addWrapField(new TimeOfDay(), 2, values, 29);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 3, 40};
        result = field.addWrapField(new TimeOfDay(), 2, values, 33);
        assertEquals(true, Arrays.equals(result, expected));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getDifference_long_long
    public void test_getDifference_long_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(-21, field.getDifference(20000L, 41000L));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getDifferenceAsLong_long_long
    public void test_getDifferenceAsLong_long_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(-21L, field.getDifferenceAsLong(20000L, 41000L));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_set_long_int
    public void test_set_long_int() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(3120L, field.set(2120L, 6));
        assertEquals(26120L, field.set(120L, 29));
        assertEquals(57120L, field.set(2120L, 60));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_set_RP_int_intarray_int
    public void test_set_RP_int_intarray_int() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        int[] result = field.set(new TimeOfDay(), 2, values, 30);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 29, 40};
        result = field.set(new TimeOfDay(), 2, values, 29);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, 63);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, 2);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_set_long_String_Locale
    public void test_set_long_String_Locale() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(3050L, field.set(50L, "6", null));
        assertEquals(26050L, field.set(50L, "29", Locale.ENGLISH));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_set_long_String
    public void test_set_long_String() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(3050L, field.set(50L, "6"));
        assertEquals(26050L, field.set(50L, "29"));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_set_RP_int_intarray_String_Locale
    public void test_set_RP_int_intarray_String_Locale() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        int[] result = field.set(new TimeOfDay(), 2, values, "30", null);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 29, 40};
        result = field.set(new TimeOfDay(), 2, values, "29", Locale.ENGLISH);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, "63", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, "2", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_convertText
    public void test_convertText() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(0, field.convertText("0", null));
        assertEquals(29, field.convertText("29", null));
        try {
            field.convertText("2A", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            field.convertText(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestOffsetDateTimeField::test_isLeap_long
    public void test_isLeap_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(false, field.isLeap(0L));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getLeapAmount_long
    public void test_getLeapAmount_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(0, field.getLeapAmount(0L));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getLeapDurationField
    public void test_getLeapDurationField() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(null, field.getLeapDurationField());
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getMinimumValue
    public void test_getMinimumValue() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(3, field.getMinimumValue());
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getMinimumValue_long
    public void test_getMinimumValue_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(3, field.getMinimumValue(0L));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getMinimumValue_RP
    public void test_getMinimumValue_RP() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(3, field.getMinimumValue(new TimeOfDay()));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getMinimumValue_RP_intarray
    public void test_getMinimumValue_RP_intarray() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(3, field.getMinimumValue(new TimeOfDay(), new int[4]));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getMaximumValue
    public void test_getMaximumValue() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(62, field.getMaximumValue());
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getMaximumValue_long
    public void test_getMaximumValue_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(62, field.getMaximumValue(0L));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getMaximumValue_RP
    public void test_getMaximumValue_RP() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(62, field.getMaximumValue(new TimeOfDay()));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getMaximumValue_RP_intarray
    public void test_getMaximumValue_RP_intarray() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(62, field.getMaximumValue(new TimeOfDay(), new int[4]));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getMaximumTextLength_Locale
    public void test_getMaximumTextLength_Locale() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(2, field.getMaximumTextLength(Locale.ENGLISH));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_getMaximumShortTextLength_Locale
    public void test_getMaximumShortTextLength_Locale() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(2, field.getMaximumShortTextLength(Locale.ENGLISH));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_roundFloor_long
    public void test_roundFloor_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(-2000L, field.roundFloor(-1001L));
        assertEquals(-1000L, field.roundFloor(-1000L));
        assertEquals(-1000L, field.roundFloor(-999L));
        assertEquals(-1000L, field.roundFloor(-1L));
        assertEquals(0L, field.roundFloor(0L));
        assertEquals(0L, field.roundFloor(1L));
        assertEquals(0L, field.roundFloor(499L));
        assertEquals(0L, field.roundFloor(500L));
        assertEquals(0L, field.roundFloor(501L));
        assertEquals(1000L, field.roundFloor(1000L));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_roundCeiling_long
    public void test_roundCeiling_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(-1000L, field.roundCeiling(-1001L));
        assertEquals(-1000L, field.roundCeiling(-1000L));
        assertEquals(0L, field.roundCeiling(-999L));
        assertEquals(0L, field.roundCeiling(-1L));
        assertEquals(0L, field.roundCeiling(0L));
        assertEquals(1000L, field.roundCeiling(1L));
        assertEquals(1000L, field.roundCeiling(499L));
        assertEquals(1000L, field.roundCeiling(500L));
        assertEquals(1000L, field.roundCeiling(501L));
        assertEquals(1000L, field.roundCeiling(1000L));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_roundHalfFloor_long
    public void test_roundHalfFloor_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(0L, field.roundHalfFloor(0L));
        assertEquals(0L, field.roundHalfFloor(499L));
        assertEquals(0L, field.roundHalfFloor(500L));
        assertEquals(1000L, field.roundHalfFloor(501L));
        assertEquals(1000L, field.roundHalfFloor(1000L));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_roundHalfCeiling_long
    public void test_roundHalfCeiling_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(0L, field.roundHalfCeiling(0L));
        assertEquals(0L, field.roundHalfCeiling(499L));
        assertEquals(1000L, field.roundHalfCeiling(500L));
        assertEquals(1000L, field.roundHalfCeiling(501L));
        assertEquals(1000L, field.roundHalfCeiling(1000L));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_roundHalfEven_long
    public void test_roundHalfEven_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(0L, field.roundHalfEven(0L));
        assertEquals(0L, field.roundHalfEven(499L));
        assertEquals(0L, field.roundHalfEven(500L));
        assertEquals(1000L, field.roundHalfEven(501L));
        assertEquals(1000L, field.roundHalfEven(1000L));
        assertEquals(1000L, field.roundHalfEven(1499L));
        assertEquals(2000L, field.roundHalfEven(1500L));
        assertEquals(2000L, field.roundHalfEven(1501L));
    }

// org.joda.time.field.TestOffsetDateTimeField::test_remainder_long
    public void test_remainder_long() {
        OffsetDateTimeField field = new MockOffsetDateTimeField();
        assertEquals(0L, field.remainder(0L));
        assertEquals(499L, field.remainder(499L));
        assertEquals(500L, field.remainder(500L));
        assertEquals(501L, field.remainder(501L));
        assertEquals(0L, field.remainder(1000L));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_constructor
    public void test_constructor() {
        BaseDateTimeField field = new PreciseDateTimeField(
            DateTimeFieldType.secondOfMinute(),
            ISOChronology.getInstanceUTC().millis(),
            ISOChronology.getInstanceUTC().hours()
        );
        assertEquals(DateTimeFieldType.secondOfMinute(), field.getType());
        try {
            field = new PreciseDateTimeField(null, null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            field = new PreciseDateTimeField(
                DateTimeFieldType.minuteOfHour(),
                new MockImpreciseDurationField(DurationFieldType.minutes()),
                ISOChronology.getInstanceUTC().hours());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            field = new PreciseDateTimeField(
                DateTimeFieldType.minuteOfHour(),
                ISOChronology.getInstanceUTC().hours(),
                new MockImpreciseDurationField(DurationFieldType.minutes()));
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            field = new PreciseDateTimeField(
                DateTimeFieldType.minuteOfHour(),
                ISOChronology.getInstanceUTC().hours(),
                ISOChronology.getInstanceUTC().hours());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            field = new PreciseDateTimeField(
                DateTimeFieldType.minuteOfHour(),
                new MockZeroDurationField(DurationFieldType.minutes()),
                ISOChronology.getInstanceUTC().hours());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getType
    public void test_getType() {
        BaseDateTimeField field = new PreciseDateTimeField(
            DateTimeFieldType.secondOfDay(),
            ISOChronology.getInstanceUTC().millis(),
            ISOChronology.getInstanceUTC().hours()
        );
        assertEquals(DateTimeFieldType.secondOfDay(), field.getType());
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getName
    public void test_getName() {
        BaseDateTimeField field = new PreciseDateTimeField(
            DateTimeFieldType.secondOfDay(),
            ISOChronology.getInstanceUTC().millis(),
            ISOChronology.getInstanceUTC().hours()
        );
        assertEquals("secondOfDay", field.getName());
    }

// org.joda.time.field.TestPreciseDateTimeField::test_toString
    public void test_toString() {
        BaseDateTimeField field = new PreciseDateTimeField(
            DateTimeFieldType.secondOfDay(),
            ISOChronology.getInstanceUTC().millis(),
            ISOChronology.getInstanceUTC().hours()
        );
        assertEquals("DateTimeField[secondOfDay]", field.toString());
    }

// org.joda.time.field.TestPreciseDateTimeField::test_isSupported
    public void test_isSupported() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(true, field.isSupported());
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getRange
    public void test_getRange() {
        PreciseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(60, field.getRange());
    }

// org.joda.time.field.TestPreciseDateTimeField::test_get
    public void test_get() {
        PreciseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(0, field.get(0));
        assertEquals(1, field.get(60));
        assertEquals(2, field.get(123));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getAsText_long_Locale
    public void test_getAsText_long_Locale() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals("29", field.getAsText(60L * 29, Locale.ENGLISH));
        assertEquals("29", field.getAsText(60L * 29, null));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getAsText_long
    public void test_getAsText_long() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals("29", field.getAsText(60L * 29));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getAsText_RP_int_Locale
    public void test_getAsText_RP_int_Locale() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals("20", field.getAsText(new TimeOfDay(12, 30, 40, 50), 20, Locale.ENGLISH));
        assertEquals("20", field.getAsText(new TimeOfDay(12, 30, 40, 50), 20, null));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getAsText_RP_Locale
    public void test_getAsText_RP_Locale() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals("40", field.getAsText(new TimeOfDay(12, 30, 40, 50), Locale.ENGLISH));
        assertEquals("40", field.getAsText(new TimeOfDay(12, 30, 40, 50), null));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getAsText_int_Locale
    public void test_getAsText_int_Locale() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals("80", field.getAsText(80, Locale.ENGLISH));
        assertEquals("80", field.getAsText(80, null));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getAsShortText_long_Locale
    public void test_getAsShortText_long_Locale() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals("29", field.getAsShortText(60L * 29, Locale.ENGLISH));
        assertEquals("29", field.getAsShortText(60L * 29, null));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getAsShortText_long
    public void test_getAsShortText_long() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals("29", field.getAsShortText(60L * 29));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getAsShortText_RP_int_Locale
    public void test_getAsShortText_RP_int_Locale() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals("20", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), 20, Locale.ENGLISH));
        assertEquals("20", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), 20, null));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getAsShortText_RP_Locale
    public void test_getAsShortText_RP_Locale() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals("40", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), Locale.ENGLISH));
        assertEquals("40", field.getAsShortText(new TimeOfDay(12, 30, 40, 50), null));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getAsShortText_int_Locale
    public void test_getAsShortText_int_Locale() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals("80", field.getAsShortText(80, Locale.ENGLISH));
        assertEquals("80", field.getAsShortText(80, null));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_add_long_int
    public void test_add_long_int() {
        MockCountingDurationField.add_int = 0;
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(61, field.add(1L, 1));
        assertEquals(1, MockCountingDurationField.add_int);
    }

// org.joda.time.field.TestPreciseDateTimeField::test_add_long_long
    public void test_add_long_long() {
        MockCountingDurationField.add_long = 0;
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(61, field.add(1L, 1L));
        assertEquals(1, MockCountingDurationField.add_long);
    }

// org.joda.time.field.TestPreciseDateTimeField::test_add_RP_int_intarray_int
    public void test_add_RP_int_intarray_int() {
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        BaseDateTimeField field = new MockStandardDateTimeField();
        int[] result = field.add(new TimeOfDay(), 2, values, 0);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 31, 40};
        result = field.add(new TimeOfDay(), 2, values, 1);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 21, 0, 40};
        result = field.add(new TimeOfDay(), 2, values, 30);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {23, 59, 30, 40};
        try {
            field.add(new TimeOfDay(), 2, values, 30);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 29, 40};
        result = field.add(new TimeOfDay(), 2, values, -1);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 19, 59, 40};
        result = field.add(new TimeOfDay(), 2, values, -31);
        assertEquals(true, Arrays.equals(expected, result));
        
        values = new int[] {0, 0, 30, 40};
        try {
            field.add(new TimeOfDay(), 2, values, -31);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestPreciseDateTimeField::test_addWrapField_long_int
    public void test_addWrapField_long_int() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(29 * 60L, field.addWrapField(60L * 29, 0));
        assertEquals(59 * 60L, field.addWrapField(60L * 29, 30));
        assertEquals(0 * 60L, field.addWrapField(60L * 29, 31));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_addWrapField_RP_int_intarray_int
    public void test_addWrapField_RP_int_intarray_int() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        int[] result = field.addWrapField(new TimeOfDay(), 2, values, 0);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 59, 40};
        result = field.addWrapField(new TimeOfDay(), 2, values, 29);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 0, 40};
        result = field.addWrapField(new TimeOfDay(), 2, values, 30);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 1, 40};
        result = field.addWrapField(new TimeOfDay(), 2, values, 31);
        assertEquals(true, Arrays.equals(result, expected));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getDifference_long_long
    public void test_getDifference_long_long() {
        MockCountingDurationField.difference_long = 0;
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(30, field.getDifference(0L, 0L));
        assertEquals(1, MockCountingDurationField.difference_long);
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getDifferenceAsLong_long_long
    public void test_getDifferenceAsLong_long_long() {
        MockCountingDurationField.difference_long = 0;
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(30, field.getDifferenceAsLong(0L, 0L));
        assertEquals(1, MockCountingDurationField.difference_long);
    }

// org.joda.time.field.TestPreciseDateTimeField::test_set_long_int
    public void test_set_long_int() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(0, field.set(120L, 0));
        assertEquals(29 * 60, field.set(120L, 29));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_set_RP_int_intarray_int
    public void test_set_RP_int_intarray_int() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        int[] result = field.set(new TimeOfDay(), 2, values, 30);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 29, 40};
        result = field.set(new TimeOfDay(), 2, values, 29);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_set_long_String_Locale
    public void test_set_long_String_Locale() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(0, field.set(0L, "0", null));
        assertEquals(29 * 60, field.set(0L, "29", Locale.ENGLISH));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_set_long_String
    public void test_set_long_String() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(0, field.set(0L, "0"));
        assertEquals(29 * 60, field.set(0L, "29"));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_set_RP_int_intarray_String_Locale
    public void test_set_RP_int_intarray_String_Locale() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        int[] values = new int[] {10, 20, 30, 40};
        int[] expected = new int[] {10, 20, 30, 40};
        int[] result = field.set(new TimeOfDay(), 2, values, "30", null);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 29, 40};
        result = field.set(new TimeOfDay(), 2, values, "29", Locale.ENGLISH);
        assertEquals(true, Arrays.equals(result, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, "60", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
        
        values = new int[] {10, 20, 30, 40};
        expected = new int[] {10, 20, 30, 40};
        try {
            field.set(new TimeOfDay(), 2, values, "-1", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        assertEquals(true, Arrays.equals(values, expected));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_convertText
    public void test_convertText() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(0, field.convertText("0", null));
        assertEquals(29, field.convertText("29", null));
        try {
            field.convertText("2A", null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            field.convertText(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.field.TestPreciseDateTimeField::test_isLeap_long
    public void test_isLeap_long() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(false, field.isLeap(0L));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getLeapAmount_long
    public void test_getLeapAmount_long() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(0, field.getLeapAmount(0L));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getLeapDurationField
    public void test_getLeapDurationField() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(null, field.getLeapDurationField());
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getMinimumValue
    public void test_getMinimumValue() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(0, field.getMinimumValue());
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getMinimumValue_long
    public void test_getMinimumValue_long() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(0, field.getMinimumValue(0L));
    }

// org.joda.time.field.TestPreciseDateTimeField::test_getMinimumValue_RP
    public void test_getMinimumValue_RP() {
        BaseDateTimeField field = new MockPreciseDateTimeField();
        assertEquals(0, field.getMinimumValue(new TimeOfDay()));
    }
