// buggy code
    public int getOffsetFromLocal(long instantLocal) {
        // get the offset at instantLocal (first estimate)
        final int offsetLocal = getOffset(instantLocal);
        // adjust instantLocal using the estimate and recalc the offset
        final long instantAdjusted = instantLocal - offsetLocal;
        final int offsetAdjusted = getOffset(instantAdjusted);
        // if the offsets differ, we must be near a DST boundary
        if (offsetLocal != offsetAdjusted) {
            // we need to ensure that time is always after the DST gap
            // this happens naturally for positive offsets, but not for negative
            if ((offsetLocal - offsetAdjusted) < 0) {
                // if we just return offsetAdjusted then the time is pushed
                // back before the transition, whereas it should be
                // on or after the transition
                long nextLocal = nextTransition(instantAdjusted);
                long nextAdjusted = nextTransition(instantLocal - offsetAdjusted);
                if (nextLocal != nextAdjusted) {
                    return offsetLocal;
                }
            }
        }
        return offsetAdjusted;
    }

// relevant test
// org.joda.time.TestDateTimeZoneCutover::test_LocalDate_toDateMidnight_Gaza
    public void test_LocalDate_toDateMidnight_Gaza() {
        LocalDate date = new LocalDate(2007, 4, 1);
        try {
            date.toDateMidnight(MOCK_GAZA);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(true, ex.getMessage().startsWith("Illegal instant due to time zone offset transition"));
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_new_Gaza
    public void test_DateTime_new_Gaza() {
        try {
            new DateTime(2007, 4, 1, 0, 0, 0, 0, MOCK_GAZA);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(true, ex.getMessage().indexOf("Illegal instant due to time zone offset transition") >= 0);
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_newValid_Gaza
    public void test_DateTime_newValid_Gaza() {
        new DateTime(2007, 3, 31, 19, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 3, 31, 20, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 3, 31, 21, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 3, 31, 22, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 3, 31, 23, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 4, 1, 1, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 4, 1, 2, 0, 0, 0, MOCK_GAZA);
        new DateTime(2007, 4, 1, 3, 0, 0, 0, MOCK_GAZA);
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_parse_Gaza
    public void test_DateTime_parse_Gaza() {
        try {
            new DateTime("2007-04-01T00:00", MOCK_GAZA);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(true, ex.getMessage().indexOf("Illegal instant due to time zone offset transition") >= 0);
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_MockTurkIsCorrect
    public void test_MockTurkIsCorrect() {
        DateTime pre = new DateTime(CUTOVER_TURK - 1L, MOCK_TURK);
        assertEquals("2007-03-31T23:59:59.999-05:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_TURK, MOCK_TURK);
        assertEquals("2007-04-01T01:00:00.000-04:00", at.toString());
        DateTime post = new DateTime(CUTOVER_TURK + 1L, MOCK_TURK);
        assertEquals("2007-04-01T01:00:00.001-04:00", post.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_Turk
    public void test_getOffsetFromLocal_Turk() {
        doTest_getOffsetFromLocal_Turk(-1, 23, 0, "2007-03-31T23:00:00.000-05:00");
        doTest_getOffsetFromLocal_Turk(-1, 23, 30, "2007-03-31T23:30:00.000-05:00");
        doTest_getOffsetFromLocal_Turk(0, 0, 0, "2007-04-01T01:00:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 0, 30, "2007-04-01T01:30:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 1, 0, "2007-04-01T01:00:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 1, 30, "2007-04-01T01:30:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 2, 0, "2007-04-01T02:00:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 3, 0, "2007-04-01T03:00:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 4, 0, "2007-04-01T04:00:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 5, 0, "2007-04-01T05:00:00.000-04:00");
        doTest_getOffsetFromLocal_Turk(0, 6, 0, "2007-04-01T06:00:00.000-04:00");
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_Turk
    public void test_DateTime_roundFloor_Turk() {
        DateTime dt = new DateTime(2007, 4, 1, 8, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-01T08:00:00.000-04:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundFloorCopy();
        assertEquals("2007-04-01T01:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloorNotDST_Turk
    public void test_DateTime_roundFloorNotDST_Turk() {
        DateTime dt = new DateTime(2007, 4, 2, 8, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-02T08:00:00.000-04:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundFloorCopy();
        assertEquals("2007-04-02T00:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_Turk
    public void test_DateTime_roundCeiling_Turk() {
        DateTime dt = new DateTime(2007, 3, 31, 20, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-03-31T20:00:00.000-05:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundCeilingCopy();
        assertEquals("2007-04-01T01:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourZero_Turk
    public void test_DateTime_setHourZero_Turk() {
        DateTime dt = new DateTime(2007, 4, 1, 8, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-01T08:00:00.000-04:00", dt.toString());
        try {
            dt.hourOfDay().setCopy(0);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_withHourZero_Turk
    public void test_DateTime_withHourZero_Turk() {
        DateTime dt = new DateTime(2007, 4, 1, 8, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-01T08:00:00.000-04:00", dt.toString());
        try {
            dt.withHourOfDay(0);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_withDay_Turk
    public void test_DateTime_withDay_Turk() {
        DateTime dt = new DateTime(2007, 4, 2, 0, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-02T00:00:00.000-04:00", dt.toString());
        DateTime res = dt.withDayOfMonth(1);
        assertEquals("2007-04-01T01:00:00.000-04:00", res.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_minusHour_Turk
    public void test_DateTime_minusHour_Turk() {
        DateTime dt = new DateTime(2007, 4, 1, 8, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-01T08:00:00.000-04:00", dt.toString());
        
        DateTime minus7 = dt.minusHours(7);
        assertEquals("2007-04-01T01:00:00.000-04:00", minus7.toString());
        DateTime minus8 = dt.minusHours(8);
        assertEquals("2007-03-31T23:00:00.000-05:00", minus8.toString());
        DateTime minus9 = dt.minusHours(9);
        assertEquals("2007-03-31T22:00:00.000-05:00", minus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusHour_Turk
    public void test_DateTime_plusHour_Turk() {
        DateTime dt = new DateTime(2007, 3, 31, 16, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-03-31T16:00:00.000-05:00", dt.toString());
        
        DateTime plus7 = dt.plusHours(7);
        assertEquals("2007-03-31T23:00:00.000-05:00", plus7.toString());
        DateTime plus8 = dt.plusHours(8);
        assertEquals("2007-04-01T01:00:00.000-04:00", plus8.toString());
        DateTime plus9 = dt.plusHours(9);
        assertEquals("2007-04-01T02:00:00.000-04:00", plus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_minusDay_Turk
    public void test_DateTime_minusDay_Turk() {
        DateTime dt = new DateTime(2007, 4, 2, 0, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-02T00:00:00.000-04:00", dt.toString());
        
        DateTime minus1 = dt.minusDays(1);
        assertEquals("2007-04-01T01:00:00.000-04:00", minus1.toString());
        DateTime minus2 = dt.minusDays(2);
        assertEquals("2007-03-31T00:00:00.000-05:00", minus2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusDay_Turk
    public void test_DateTime_plusDay_Turk() {
        DateTime dt = new DateTime(2007, 3, 31, 0, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-03-31T00:00:00.000-05:00", dt.toString());
        
        DateTime plus1 = dt.plusDays(1);
        assertEquals("2007-04-01T01:00:00.000-04:00", plus1.toString());
        DateTime plus2 = dt.plusDays(2);
        assertEquals("2007-04-02T00:00:00.000-04:00", plus2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusDayMidGap_Turk
    public void test_DateTime_plusDayMidGap_Turk() {
        DateTime dt = new DateTime(2007, 3, 31, 0, 30, 0, 0, MOCK_TURK);
        assertEquals("2007-03-31T00:30:00.000-05:00", dt.toString());
        
        DateTime plus1 = dt.plusDays(1);
        assertEquals("2007-04-01T01:30:00.000-04:00", plus1.toString());
        DateTime plus2 = dt.plusDays(2);
        assertEquals("2007-04-02T00:30:00.000-04:00", plus2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_addWrapFieldDay_Turk
    public void test_DateTime_addWrapFieldDay_Turk() {
        DateTime dt = new DateTime(2007, 4, 30, 0, 0, 0, 0, MOCK_TURK);
        assertEquals("2007-04-30T00:00:00.000-04:00", dt.toString());
        
        DateTime plus1 = dt.dayOfMonth().addWrapFieldToCopy(1);
        assertEquals("2007-04-01T01:00:00.000-04:00", plus1.toString());
        DateTime plus2 = dt.dayOfMonth().addWrapFieldToCopy(2);
        assertEquals("2007-04-02T00:00:00.000-04:00", plus2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_withZoneRetainFields_Turk
    public void test_DateTime_withZoneRetainFields_Turk() {
        DateTime dt = new DateTime(2007, 4, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        assertEquals("2007-04-01T00:00:00.000Z", dt.toString());
        
        DateTime res = dt.withZoneRetainFields(MOCK_TURK);
        assertEquals("2007-04-01T01:00:00.000-04:00", res.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_MutableDateTime_setZoneRetainFields_Turk
    public void test_MutableDateTime_setZoneRetainFields_Turk() {
        MutableDateTime dt = new MutableDateTime(2007, 4, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        assertEquals("2007-04-01T00:00:00.000Z", dt.toString());
        
        dt.setZoneRetainFields(MOCK_TURK);
        assertEquals("2007-04-01T01:00:00.000-04:00", dt.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_LocalDate_new_Turk
    public void test_LocalDate_new_Turk() {
        LocalDate date1 = new LocalDate(CUTOVER_TURK, MOCK_TURK);
        assertEquals("2007-04-01", date1.toString());
        
        LocalDate date2 = new LocalDate(CUTOVER_TURK - 1, MOCK_TURK);
        assertEquals("2007-03-31", date2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_LocalDate_toDateMidnight_Turk
    public void test_LocalDate_toDateMidnight_Turk() {
        LocalDate date = new LocalDate(2007, 4, 1);
        try {
            date.toDateMidnight(MOCK_TURK);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(true, ex.getMessage().startsWith("Illegal instant due to time zone offset transition"));
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_new_Turk
    public void test_DateTime_new_Turk() {
        try {
            new DateTime(2007, 4, 1, 0, 0, 0, 0, MOCK_TURK);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(true, ex.getMessage().indexOf("Illegal instant due to time zone offset transition") >= 0);
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_newValid_Turk
    public void test_DateTime_newValid_Turk() {
        new DateTime(2007, 3, 31, 23, 0, 0, 0, MOCK_TURK);
        new DateTime(2007, 4, 1, 1, 0, 0, 0, MOCK_TURK);
        new DateTime(2007, 4, 1, 2, 0, 0, 0, MOCK_TURK);
        new DateTime(2007, 4, 1, 3, 0, 0, 0, MOCK_TURK);
        new DateTime(2007, 4, 1, 4, 0, 0, 0, MOCK_TURK);
        new DateTime(2007, 4, 1, 5, 0, 0, 0, MOCK_TURK);
        new DateTime(2007, 4, 1, 6, 0, 0, 0, MOCK_TURK);
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_parse_Turk
    public void test_DateTime_parse_Turk() {
        try {
            new DateTime("2007-04-01T00:00", MOCK_TURK);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(true, ex.getMessage().indexOf("Illegal instant due to time zone offset transition") >= 0);
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_NewYorkIsCorrect_Spring
    public void test_NewYorkIsCorrect_Spring() {
        DateTime pre = new DateTime(CUTOVER_NEW_YORK_SPRING - 1L, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:59:59.999-05:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_NEW_YORK_SPRING, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:00:00.000-04:00", at.toString());
        DateTime post = new DateTime(CUTOVER_NEW_YORK_SPRING + 1L, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:00:00.001-04:00", post.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_NewYork_Spring
    public void test_getOffsetFromLocal_NewYork_Spring() {
        doTest_getOffsetFromLocal(3, 11, 1, 0, "2007-03-11T01:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 1,30, "2007-03-11T01:30:00.000-05:00", ZONE_NEW_YORK);
        
        doTest_getOffsetFromLocal(3, 11, 2, 0, "2007-03-11T03:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 2,30, "2007-03-11T03:30:00.000-04:00", ZONE_NEW_YORK);
        
        doTest_getOffsetFromLocal(3, 11, 3, 0, "2007-03-11T03:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 3,30, "2007-03-11T03:30:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 4, 0, "2007-03-11T04:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 5, 0, "2007-03-11T05:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 6, 0, "2007-03-11T06:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 7, 0, "2007-03-11T07:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(3, 11, 8, 0, "2007-03-11T08:00:00.000-04:00", ZONE_NEW_YORK);
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourAcross_NewYork_Spring
    public void test_DateTime_setHourAcross_NewYork_Spring() {
        DateTime dt = new DateTime(2007, 3, 11, 0, 0, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T00:00:00.000-05:00", dt.toString());
        DateTime res = dt.hourOfDay().setCopy(4);
        assertEquals("2007-03-11T04:00:00.000-04:00", res.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourForward_NewYork_Spring
    public void test_DateTime_setHourForward_NewYork_Spring() {
        DateTime dt = new DateTime(2007, 3, 11, 0, 0, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T00:00:00.000-05:00", dt.toString());
        
        try {
            dt.hourOfDay().setCopy(2);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourBack_NewYork_Spring
    public void test_DateTime_setHourBack_NewYork_Spring() {
        DateTime dt = new DateTime(2007, 3, 11, 8, 0, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T08:00:00.000-04:00", dt.toString());
        
        try {
            dt.hourOfDay().setCopy(2);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_day_NewYork_Spring_preCutover
    public void test_DateTime_roundFloor_day_NewYork_Spring_preCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundFloorCopy();
        assertEquals("2007-03-11T00:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_day_NewYork_Spring_postCutover
    public void test_DateTime_roundFloor_day_NewYork_Spring_postCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 3, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundFloorCopy();
        assertEquals("2007-03-11T00:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_hour_NewYork_Spring_preCutover
    public void test_DateTime_roundFloor_hour_NewYork_Spring_preCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundFloorCopy();
        assertEquals("2007-03-11T01:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_hour_NewYork_Spring_postCutover
    public void test_DateTime_roundFloor_hour_NewYork_Spring_postCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 3, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundFloorCopy();
        assertEquals("2007-03-11T03:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_minute_NewYork_Spring_preCutover
    public void test_DateTime_roundFloor_minute_NewYork_Spring_preCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 1, 30, 40, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:30:40.000-05:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundFloorCopy();
        assertEquals("2007-03-11T01:30:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_minute_NewYork_Spring_postCutover
    public void test_DateTime_roundFloor_minute_NewYork_Spring_postCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 3, 30, 40, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:30:40.000-04:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundFloorCopy();
        assertEquals("2007-03-11T03:30:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_day_NewYork_Spring_preCutover
    public void test_DateTime_roundCeiling_day_NewYork_Spring_preCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundCeilingCopy();
        assertEquals("2007-03-12T00:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_day_NewYork_Spring_postCutover
    public void test_DateTime_roundCeiling_day_NewYork_Spring_postCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 3, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundCeilingCopy();
        assertEquals("2007-03-12T00:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_hour_NewYork_Spring_preCutover
    public void test_DateTime_roundCeiling_hour_NewYork_Spring_preCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundCeilingCopy();
        assertEquals("2007-03-11T03:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_hour_NewYork_Spring_postCutover
    public void test_DateTime_roundCeiling_hour_NewYork_Spring_postCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 3, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundCeilingCopy();
        assertEquals("2007-03-11T04:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_minute_NewYork_Spring_preCutover
    public void test_DateTime_roundCeiling_minute_NewYork_Spring_preCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 1, 30, 40, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T01:30:40.000-05:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundCeilingCopy();
        assertEquals("2007-03-11T01:31:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_minute_NewYork_Spring_postCutover
    public void test_DateTime_roundCeiling_minute_NewYork_Spring_postCutover() {
        DateTime dt = new DateTime(2007, 3, 11, 3, 30, 40, 0, ZONE_NEW_YORK);
        assertEquals("2007-03-11T03:30:40.000-04:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundCeilingCopy();
        assertEquals("2007-03-11T03:31:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_NewYorkIsCorrect_Autumn
    public void test_NewYorkIsCorrect_Autumn() {
        DateTime pre = new DateTime(CUTOVER_NEW_YORK_AUTUMN - 1L, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:59:59.999-04:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_NEW_YORK_AUTUMN, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:00:00.000-05:00", at.toString());
        DateTime post = new DateTime(CUTOVER_NEW_YORK_AUTUMN + 1L, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:00:00.001-05:00", post.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_NewYork_Autumn
    public void test_getOffsetFromLocal_NewYork_Autumn() {
        doTest_getOffsetFromLocal(11, 4, 0, 0, "2007-11-04T00:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 0,30, "2007-11-04T00:30:00.000-04:00", ZONE_NEW_YORK);
        
        doTest_getOffsetFromLocal(11, 4, 1, 0, "2007-11-04T01:00:00.000-04:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 1,30, "2007-11-04T01:30:00.000-04:00", ZONE_NEW_YORK);
        
        doTest_getOffsetFromLocal(11, 4, 2, 0, "2007-11-04T02:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 2,30, "2007-11-04T02:30:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 3, 0, "2007-11-04T03:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 3,30, "2007-11-04T03:30:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 4, 0, "2007-11-04T04:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 5, 0, "2007-11-04T05:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 6, 0, "2007-11-04T06:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 7, 0, "2007-11-04T07:00:00.000-05:00", ZONE_NEW_YORK);
        doTest_getOffsetFromLocal(11, 4, 8, 0, "2007-11-04T08:00:00.000-05:00", ZONE_NEW_YORK);
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_constructor_NewYork_Autumn
    public void test_DateTime_constructor_NewYork_Autumn() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.000-04:00", dt.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusHour_NewYork_Autumn
    public void test_DateTime_plusHour_NewYork_Autumn() {
        DateTime dt = new DateTime(2007, 11, 3, 18, 0, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-03T18:00:00.000-04:00", dt.toString());
        
        DateTime plus6 = dt.plusHours(6);
        assertEquals("2007-11-04T00:00:00.000-04:00", plus6.toString());
        DateTime plus7 = dt.plusHours(7);
        assertEquals("2007-11-04T01:00:00.000-04:00", plus7.toString());
        DateTime plus8 = dt.plusHours(8);
        assertEquals("2007-11-04T01:00:00.000-05:00", plus8.toString());
        DateTime plus9 = dt.plusHours(9);
        assertEquals("2007-11-04T02:00:00.000-05:00", plus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_minusHour_NewYork_Autumn
    public void test_DateTime_minusHour_NewYork_Autumn() {
        DateTime dt = new DateTime(2007, 11, 4, 8, 0, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T08:00:00.000-05:00", dt.toString());
        
        DateTime minus6 = dt.minusHours(6);
        assertEquals("2007-11-04T02:00:00.000-05:00", minus6.toString());
        DateTime minus7 = dt.minusHours(7);
        assertEquals("2007-11-04T01:00:00.000-05:00", minus7.toString());
        DateTime minus8 = dt.minusHours(8);
        assertEquals("2007-11-04T01:00:00.000-04:00", minus8.toString());
        DateTime minus9 = dt.minusHours(9);
        assertEquals("2007-11-04T00:00:00.000-04:00", minus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_day_NewYork_Autumn_preCutover
    public void test_DateTime_roundFloor_day_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundFloorCopy();
        assertEquals("2007-11-04T00:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_day_NewYork_Autumn_postCutover
    public void test_DateTime_roundFloor_day_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundFloorCopy();
        assertEquals("2007-11-04T00:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_hourOfDay_NewYork_Autumn_preCutover
    public void test_DateTime_roundFloor_hourOfDay_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundFloorCopy();
        assertEquals("2007-11-04T01:00:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_hourOfDay_NewYork_Autumn_postCutover
    public void test_DateTime_roundFloor_hourOfDay_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundFloorCopy();
        assertEquals("2007-11-04T01:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_minuteOfHour_NewYork_Autumn_preCutover
    public void test_DateTime_roundFloor_minuteOfHour_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:40.000-04:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundFloorCopy();
        assertEquals("2007-11-04T01:30:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_minuteOfHour_NewYork_Autumn_postCutover
    public void test_DateTime_roundFloor_minuteOfHour_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 0, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:40.000-05:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundFloorCopy();
        assertEquals("2007-11-04T01:30:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_secondOfMinute_NewYork_Autumn_preCutover
    public void test_DateTime_roundFloor_secondOfMinute_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 500, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:40.500-04:00", dt.toString());
        DateTime rounded = dt.secondOfMinute().roundFloorCopy();
        assertEquals("2007-11-04T01:30:40.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundFloor_secondOfMinute_NewYork_Autumn_postCutover
    public void test_DateTime_roundFloor_secondOfMinute_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 500, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:40.500-05:00", dt.toString());
        DateTime rounded = dt.secondOfMinute().roundFloorCopy();
        assertEquals("2007-11-04T01:30:40.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_day_NewYork_Autumn_preCutover
    public void test_DateTime_roundCeiling_day_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundCeilingCopy();
        assertEquals("2007-11-05T00:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_day_NewYork_Autumn_postCutover
    public void test_DateTime_roundCeiling_day_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.dayOfMonth().roundCeilingCopy();
        assertEquals("2007-11-05T00:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_hourOfDay_NewYork_Autumn_preCutover
    public void test_DateTime_roundCeiling_hourOfDay_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.000-04:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundCeilingCopy();
        assertEquals("2007-11-04T01:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_hourOfDay_NewYork_Autumn_postCutover
    public void test_DateTime_roundCeiling_hourOfDay_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 0, 0, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:00.000-05:00", dt.toString());
        DateTime rounded = dt.hourOfDay().roundCeilingCopy();
        assertEquals("2007-11-04T02:00:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_minuteOfHour_NewYork_Autumn_preCutover
    public void test_DateTime_roundCeiling_minuteOfHour_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 0, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:40.000-04:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundCeilingCopy();
        assertEquals("2007-11-04T01:31:00.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_minuteOfHour_NewYork_Autumn_postCutover
    public void test_DateTime_roundCeiling_minuteOfHour_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 0, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:40.000-05:00", dt.toString());
        DateTime rounded = dt.minuteOfHour().roundCeilingCopy();
        assertEquals("2007-11-04T01:31:00.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_secondOfMinute_NewYork_Autumn_preCutover
    public void test_DateTime_roundCeiling_secondOfMinute_NewYork_Autumn_preCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 500, ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:40.500-04:00", dt.toString());
        DateTime rounded = dt.secondOfMinute().roundCeilingCopy();
        assertEquals("2007-11-04T01:30:41.000-04:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_roundCeiling_secondOfMinute_NewYork_Autumn_postCutover
    public void test_DateTime_roundCeiling_secondOfMinute_NewYork_Autumn_postCutover() {
        DateTime dt = new DateTime(2007, 11, 4, 1, 30, 40, 500, ZONE_NEW_YORK).plusHours(1);
        assertEquals("2007-11-04T01:30:40.500-05:00", dt.toString());
        DateTime rounded = dt.secondOfMinute().roundCeilingCopy();
        assertEquals("2007-11-04T01:30:41.000-05:00", rounded.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_MoscowIsCorrect_Spring
    public void test_MoscowIsCorrect_Spring() {

        DateTime pre = new DateTime(CUTOVER_MOSCOW_SPRING - 1L, ZONE_MOSCOW);
        assertEquals("2007-03-25T01:59:59.999+03:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_MOSCOW_SPRING, ZONE_MOSCOW);
        assertEquals("2007-03-25T03:00:00.000+04:00", at.toString());
        DateTime post = new DateTime(CUTOVER_MOSCOW_SPRING + 1L, ZONE_MOSCOW);
        assertEquals("2007-03-25T03:00:00.001+04:00", post.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_Moscow_Spring
    public void test_getOffsetFromLocal_Moscow_Spring() {
        doTest_getOffsetFromLocal(3, 25, 1, 0, "2007-03-25T01:00:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 1,30, "2007-03-25T01:30:00.000+03:00", ZONE_MOSCOW);
        
        doTest_getOffsetFromLocal(3, 25, 2, 0, "2007-03-25T03:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 2,30, "2007-03-25T03:30:00.000+04:00", ZONE_MOSCOW);
        
        doTest_getOffsetFromLocal(3, 25, 3, 0, "2007-03-25T03:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 3,30, "2007-03-25T03:30:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 4, 0, "2007-03-25T04:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 5, 0, "2007-03-25T05:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 6, 0, "2007-03-25T06:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 7, 0, "2007-03-25T07:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(3, 25, 8, 0, "2007-03-25T08:00:00.000+04:00", ZONE_MOSCOW);
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourAcross_Moscow_Spring
    public void test_DateTime_setHourAcross_Moscow_Spring() {
        DateTime dt = new DateTime(2007, 3, 25, 0, 0, 0, 0, ZONE_MOSCOW);
        assertEquals("2007-03-25T00:00:00.000+03:00", dt.toString());
        DateTime res = dt.hourOfDay().setCopy(4);
        assertEquals("2007-03-25T04:00:00.000+04:00", res.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourForward_Moscow_Spring
    public void test_DateTime_setHourForward_Moscow_Spring() {
        DateTime dt = new DateTime(2007, 3, 25, 0, 0, 0, 0, ZONE_MOSCOW);
        assertEquals("2007-03-25T00:00:00.000+03:00", dt.toString());
        
        try {
            dt.hourOfDay().setCopy(2);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_setHourBack_Moscow_Spring
    public void test_DateTime_setHourBack_Moscow_Spring() {
        DateTime dt = new DateTime(2007, 3, 25, 8, 0, 0, 0, ZONE_MOSCOW);
        assertEquals("2007-03-25T08:00:00.000+04:00", dt.toString());
        
        try {
            dt.hourOfDay().setCopy(2);
            fail();
        } catch (IllegalFieldValueException ex) {
            
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_MoscowIsCorrect_Autumn
    public void test_MoscowIsCorrect_Autumn() {
        DateTime pre = new DateTime(CUTOVER_MOSCOW_AUTUMN - 1L, ZONE_MOSCOW);
        assertEquals("2007-10-28T02:59:59.999+04:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_MOSCOW_AUTUMN, ZONE_MOSCOW);
        assertEquals("2007-10-28T02:00:00.000+03:00", at.toString());
        DateTime post = new DateTime(CUTOVER_MOSCOW_AUTUMN + 1L, ZONE_MOSCOW);
        assertEquals("2007-10-28T02:00:00.001+03:00", post.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_Moscow_Autumn
    public void test_getOffsetFromLocal_Moscow_Autumn() {
        doTest_getOffsetFromLocal(10, 28, 0, 0, "2007-10-28T00:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 0,30, "2007-10-28T00:30:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 1, 0, "2007-10-28T01:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 1,30, "2007-10-28T01:30:00.000+04:00", ZONE_MOSCOW);
        
        doTest_getOffsetFromLocal(10, 28, 2, 0, "2007-10-28T02:00:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 2,30, "2007-10-28T02:30:00.000+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 2,30,59,999, "2007-10-28T02:30:59.999+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 2,59,59,998, "2007-10-28T02:59:59.998+04:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 2,59,59,999, "2007-10-28T02:59:59.999+04:00", ZONE_MOSCOW);
        
        doTest_getOffsetFromLocal(10, 28, 3, 0, "2007-10-28T03:00:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 3,30, "2007-10-28T03:30:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 4, 0, "2007-10-28T04:00:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 5, 0, "2007-10-28T05:00:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 6, 0, "2007-10-28T06:00:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 7, 0, "2007-10-28T07:00:00.000+03:00", ZONE_MOSCOW);
        doTest_getOffsetFromLocal(10, 28, 8, 0, "2007-10-28T08:00:00.000+03:00", ZONE_MOSCOW);
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_Moscow_Autumn_overlap_mins
    public void test_getOffsetFromLocal_Moscow_Autumn_overlap_mins() {
        for (int min = 0; min < 60; min++) {
            if (min < 10) {
                doTest_getOffsetFromLocal(10, 28, 2, min, "2007-10-28T02:0" + min + ":00.000+04:00", ZONE_MOSCOW);
            } else {
                doTest_getOffsetFromLocal(10, 28, 2, min, "2007-10-28T02:" + min + ":00.000+04:00", ZONE_MOSCOW);
            }
        }
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_constructor_Moscow_Autumn
    public void test_DateTime_constructor_Moscow_Autumn() {
        DateTime dt = new DateTime(2007, 10, 28, 2, 30, ZONE_MOSCOW);
        assertEquals("2007-10-28T02:30:00.000+04:00", dt.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusHour_Moscow_Autumn
    public void test_DateTime_plusHour_Moscow_Autumn() {
        DateTime dt = new DateTime(2007, 10, 27, 19, 0, 0, 0, ZONE_MOSCOW);
        assertEquals("2007-10-27T19:00:00.000+04:00", dt.toString());
        
        DateTime plus6 = dt.plusHours(6);
        assertEquals("2007-10-28T01:00:00.000+04:00", plus6.toString());
        DateTime plus7 = dt.plusHours(7);
        assertEquals("2007-10-28T02:00:00.000+04:00", plus7.toString());
        DateTime plus8 = dt.plusHours(8);
        assertEquals("2007-10-28T02:00:00.000+03:00", plus8.toString());
        DateTime plus9 = dt.plusHours(9);
        assertEquals("2007-10-28T03:00:00.000+03:00", plus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_minusHour_Moscow_Autumn
    public void test_DateTime_minusHour_Moscow_Autumn() {
        DateTime dt = new DateTime(2007, 10, 28, 9, 0, 0, 0, ZONE_MOSCOW);
        assertEquals("2007-10-28T09:00:00.000+03:00", dt.toString());
        
        DateTime minus6 = dt.minusHours(6);
        assertEquals("2007-10-28T03:00:00.000+03:00", minus6.toString());
        DateTime minus7 = dt.minusHours(7);
        assertEquals("2007-10-28T02:00:00.000+03:00", minus7.toString());
        DateTime minus8 = dt.minusHours(8);
        assertEquals("2007-10-28T02:00:00.000+04:00", minus8.toString());
        DateTime minus9 = dt.minusHours(9);
        assertEquals("2007-10-28T01:00:00.000+04:00", minus9.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_GuatemataIsCorrect_Autumn
    public void test_GuatemataIsCorrect_Autumn() {
        DateTime pre = new DateTime(CUTOVER_GUATEMALA_AUTUMN - 1L, ZONE_GUATEMALA);
        assertEquals("2006-09-30T23:59:59.999-05:00", pre.toString());
        DateTime at = new DateTime(CUTOVER_GUATEMALA_AUTUMN, ZONE_GUATEMALA);
        assertEquals("2006-09-30T23:00:00.000-06:00", at.toString());
        DateTime post = new DateTime(CUTOVER_GUATEMALA_AUTUMN + 1L, ZONE_GUATEMALA);
        assertEquals("2006-09-30T23:00:00.001-06:00", post.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_getOffsetFromLocal_Guatemata_Autumn
    public void test_getOffsetFromLocal_Guatemata_Autumn() {
        doTest_getOffsetFromLocal( 2006, 9,30,23, 0,
                                  "2006-09-30T23:00:00.000-05:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006, 9,30,23,30,
                                  "2006-09-30T23:30:00.000-05:00", ZONE_GUATEMALA);
        
        doTest_getOffsetFromLocal( 2006, 9,30,23, 0,
                                  "2006-09-30T23:00:00.000-05:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006, 9,30,23,30,
                                  "2006-09-30T23:30:00.000-05:00", ZONE_GUATEMALA);
        
        doTest_getOffsetFromLocal( 2006,10, 1, 0, 0,
                                  "2006-10-01T00:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 0,30,
                                  "2006-10-01T00:30:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 1, 0,
                                  "2006-10-01T01:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 1,30,
                                  "2006-10-01T01:30:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 2, 0,
                                  "2006-10-01T02:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 2,30,
                                  "2006-10-01T02:30:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 3, 0,
                                  "2006-10-01T03:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 3,30,
                                  "2006-10-01T03:30:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 4, 0,
                                  "2006-10-01T04:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 4,30,
                                  "2006-10-01T04:30:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 5, 0,
                                  "2006-10-01T05:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 5,30,
                                  "2006-10-01T05:30:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 6, 0,
                                  "2006-10-01T06:00:00.000-06:00", ZONE_GUATEMALA);
        doTest_getOffsetFromLocal( 2006,10, 1, 6,30,
                                  "2006-10-01T06:30:00.000-06:00", ZONE_GUATEMALA);
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_plusHour_Guatemata_Autumn
    public void test_DateTime_plusHour_Guatemata_Autumn() {
        DateTime dt = new DateTime(2006, 9, 30, 20, 0, 0, 0, ZONE_GUATEMALA);
        assertEquals("2006-09-30T20:00:00.000-05:00", dt.toString());
        
        DateTime plus1 = dt.plusHours(1);
        assertEquals("2006-09-30T21:00:00.000-05:00", plus1.toString());
        DateTime plus2 = dt.plusHours(2);
        assertEquals("2006-09-30T22:00:00.000-05:00", plus2.toString());
        DateTime plus3 = dt.plusHours(3);
        assertEquals("2006-09-30T23:00:00.000-05:00", plus3.toString());
        DateTime plus4 = dt.plusHours(4);
        assertEquals("2006-09-30T23:00:00.000-06:00", plus4.toString());
        DateTime plus5 = dt.plusHours(5);
        assertEquals("2006-10-01T00:00:00.000-06:00", plus5.toString());
        DateTime plus6 = dt.plusHours(6);
        assertEquals("2006-10-01T01:00:00.000-06:00", plus6.toString());
        DateTime plus7 = dt.plusHours(7);
        assertEquals("2006-10-01T02:00:00.000-06:00", plus7.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_minusHour_Guatemata_Autumn
    public void test_DateTime_minusHour_Guatemata_Autumn() {
        DateTime dt = new DateTime(2006, 10, 1, 2, 0, 0, 0, ZONE_GUATEMALA);
        assertEquals("2006-10-01T02:00:00.000-06:00", dt.toString());
        
        DateTime minus1 = dt.minusHours(1);
        assertEquals("2006-10-01T01:00:00.000-06:00", minus1.toString());
        DateTime minus2 = dt.minusHours(2);
        assertEquals("2006-10-01T00:00:00.000-06:00", minus2.toString());
        DateTime minus3 = dt.minusHours(3);
        assertEquals("2006-09-30T23:00:00.000-06:00", minus3.toString());
        DateTime minus4 = dt.minusHours(4);
        assertEquals("2006-09-30T23:00:00.000-05:00", minus4.toString());
        DateTime minus5 = dt.minusHours(5);
        assertEquals("2006-09-30T22:00:00.000-05:00", minus5.toString());
        DateTime minus6 = dt.minusHours(6);
        assertEquals("2006-09-30T21:00:00.000-05:00", minus6.toString());
        DateTime minus7 = dt.minusHours(7);
        assertEquals("2006-09-30T20:00:00.000-05:00", minus7.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::test_DateTime_JustAfterLastEverOverlap
    public void test_DateTime_JustAfterLastEverOverlap() {
        
        DateTimeZone zone = new DateTimeZoneBuilder()
            .setStandardOffset(-3 * DateTimeConstants.MILLIS_PER_HOUR)
            .addRecurringSavings("SUMMER", 1 * DateTimeConstants.MILLIS_PER_HOUR, 2000, 2008,
                                    'w', 4, 10, 0, true, 23 * DateTimeConstants.MILLIS_PER_HOUR)
            .addRecurringSavings("WINTER", 0, 2000, 2008,
                                    'w', 8, 10, 0, true, 0 * DateTimeConstants.MILLIS_PER_HOUR)
            .toDateTimeZone("Zone", false);
        
        LocalDate date = new LocalDate(2008, 8, 10);
        assertEquals("2008-08-10", date.toString());
        
        DateTime dt = date.toDateTimeAtStartOfDay(zone);
        assertEquals("2008-08-10T00:00:00.000-03:00", dt.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithMinuteOfHourInDstChange_mockZone
    public void testWithMinuteOfHourInDstChange_mockZone() {
        DateTime cutover = new DateTime(2010, 10, 31, 1, 15, DateTimeZone.forOffsetHoursMinutes(0, 30));
        assertEquals("2010-10-31T01:15:00.000+00:30", cutover.toString());
        DateTimeZone halfHourZone = new MockZone(cutover.getMillis(), 3600000, -1800);
        DateTime pre = new DateTime(2010, 10, 31, 1, 0, halfHourZone);
        assertEquals("2010-10-31T01:00:00.000+01:00", pre.toString());
        DateTime post = new DateTime(2010, 10, 31, 1, 59, halfHourZone);
        assertEquals("2010-10-31T01:59:00.000+00:30", post.toString());
        
        DateTime testPre1 = pre.withMinuteOfHour(30);
        assertEquals("2010-10-31T01:30:00.000+01:00", testPre1.toString());  
        DateTime testPre2 = pre.withMinuteOfHour(50);
        assertEquals("2010-10-31T01:50:00.000+00:30", testPre2.toString());
        
        DateTime testPost1 = post.withMinuteOfHour(30);
        assertEquals("2010-10-31T01:30:00.000+00:30", testPost1.toString());  
        DateTime testPost2 = post.withMinuteOfHour(10);
        assertEquals("2010-10-31T01:10:00.000+01:00", testPost2.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithHourOfDayInDstChange
    public void testWithHourOfDayInDstChange() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.withHourOfDay(2);
        assertEquals("2010-10-31T02:30:10.123+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithMinuteOfHourInDstChange
    public void testWithMinuteOfHourInDstChange() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.withMinuteOfHour(0);
        assertEquals("2010-10-31T02:00:10.123+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithSecondOfMinuteInDstChange
    public void testWithSecondOfMinuteInDstChange() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.withSecondOfMinute(0);
        assertEquals("2010-10-31T02:30:00.123+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithMillisOfSecondInDstChange_Paris_summer
    public void testWithMillisOfSecondInDstChange_Paris_summer() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.withMillisOfSecond(0);
        assertEquals("2010-10-31T02:30:10.000+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithMillisOfSecondInDstChange_Paris_winter
    public void testWithMillisOfSecondInDstChange_Paris_winter() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+01:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+01:00", dateTime.toString());
        DateTime test = dateTime.withMillisOfSecond(0);
        assertEquals("2010-10-31T02:30:10.000+01:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithMillisOfSecondInDstChange_NewYork_summer
    public void testWithMillisOfSecondInDstChange_NewYork_summer() {
        DateTime dateTime = new DateTime("2007-11-04T01:30:00.123-04:00", ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.123-04:00", dateTime.toString());
        DateTime test = dateTime.withMillisOfSecond(0);
        assertEquals("2007-11-04T01:30:00.000-04:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testWithMillisOfSecondInDstChange_NewYork_winter
    public void testWithMillisOfSecondInDstChange_NewYork_winter() {
        DateTime dateTime = new DateTime("2007-11-04T01:30:00.123-05:00", ZONE_NEW_YORK);
        assertEquals("2007-11-04T01:30:00.123-05:00", dateTime.toString());
        DateTime test = dateTime.withMillisOfSecond(0);
        assertEquals("2007-11-04T01:30:00.000-05:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testPlusMinutesInDstChange
    public void testPlusMinutesInDstChange() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.plusMinutes(1);
        assertEquals("2010-10-31T02:31:10.123+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testPlusSecondsInDstChange
    public void testPlusSecondsInDstChange() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.plusSeconds(1);
        assertEquals("2010-10-31T02:30:11.123+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testPlusMillisInDstChange
    public void testPlusMillisInDstChange() {
        DateTime dateTime = new DateTime("2010-10-31T02:30:10.123+02:00", ZONE_PARIS);
        assertEquals("2010-10-31T02:30:10.123+02:00", dateTime.toString());
        DateTime test = dateTime.plusMillis(1);
        assertEquals("2010-10-31T02:30:10.124+02:00", test.toString());
    }

// org.joda.time.TestDateTimeZoneCutover::testBug2182444_usCentral
    public void testBug2182444_usCentral() {
        Chronology chronUSCentral = GregorianChronology.getInstance(DateTimeZone.forID("US/Central"));
        Chronology chronUTC = GregorianChronology.getInstance(DateTimeZone.UTC);
        DateTime usCentralStandardInUTC = new DateTime(2008, 11, 2, 7, 0, 0, 0, chronUTC);
        DateTime usCentralDaylightInUTC = new DateTime(2008, 11, 2, 6, 0, 0, 0, chronUTC);
        assertTrue("Should be standard time", chronUSCentral.getZone().isStandardOffset(usCentralStandardInUTC.getMillis()));
        assertFalse("Should be daylight time", chronUSCentral.getZone().isStandardOffset(usCentralDaylightInUTC.getMillis()));
        
        DateTime usCentralStandardInUSCentral = usCentralStandardInUTC.toDateTime(chronUSCentral);
        DateTime usCentralDaylightInUSCentral = usCentralDaylightInUTC.toDateTime(chronUSCentral);
        assertEquals(1, usCentralStandardInUSCentral.getHourOfDay());
        assertEquals(usCentralStandardInUSCentral.getHourOfDay(), usCentralDaylightInUSCentral.getHourOfDay());
        assertTrue(usCentralStandardInUSCentral.getMillis() != usCentralDaylightInUSCentral.getMillis());
        assertEquals(usCentralStandardInUSCentral, usCentralStandardInUSCentral.withHourOfDay(1));
        assertEquals(usCentralStandardInUSCentral.getMillis() + 3, usCentralStandardInUSCentral.withMillisOfSecond(3).getMillis());
        assertEquals(usCentralDaylightInUSCentral, usCentralDaylightInUSCentral.withHourOfDay(1));
        assertEquals(usCentralDaylightInUSCentral.getMillis() + 3, usCentralDaylightInUSCentral.withMillisOfSecond(3).getMillis());
    }

// org.joda.time.TestDateTimeZoneCutover::testBug2182444_ausNSW
    public void testBug2182444_ausNSW() {
        Chronology chronAusNSW = GregorianChronology.getInstance(DateTimeZone.forID("Australia/NSW"));
        Chronology chronUTC = GregorianChronology.getInstance(DateTimeZone.UTC);
        DateTime australiaNSWStandardInUTC = new DateTime(2008, 4, 5, 16, 0, 0, 0, chronUTC);
        DateTime australiaNSWDaylightInUTC = new DateTime(2008, 4, 5, 15, 0, 0, 0, chronUTC);
        assertTrue("Should be standard time", chronAusNSW.getZone().isStandardOffset(australiaNSWStandardInUTC.getMillis()));
        assertFalse("Should be daylight time", chronAusNSW.getZone().isStandardOffset(australiaNSWDaylightInUTC.getMillis()));
        
        DateTime australiaNSWStandardInAustraliaNSW = australiaNSWStandardInUTC.toDateTime(chronAusNSW);
        DateTime australiaNSWDaylightInAusraliaNSW = australiaNSWDaylightInUTC.toDateTime(chronAusNSW);
        assertEquals(2, australiaNSWStandardInAustraliaNSW.getHourOfDay());
        assertEquals(australiaNSWStandardInAustraliaNSW.getHourOfDay(), australiaNSWDaylightInAusraliaNSW.getHourOfDay());
        assertTrue(australiaNSWStandardInAustraliaNSW.getMillis() != australiaNSWDaylightInAusraliaNSW.getMillis());
        assertEquals(australiaNSWStandardInAustraliaNSW, australiaNSWStandardInAustraliaNSW.withHourOfDay(2));
        assertEquals(australiaNSWStandardInAustraliaNSW.getMillis() + 3, australiaNSWStandardInAustraliaNSW.withMillisOfSecond(3).getMillis());
        assertEquals(australiaNSWDaylightInAusraliaNSW, australiaNSWDaylightInAusraliaNSW.withHourOfDay(2));
        assertEquals(australiaNSWDaylightInAusraliaNSW.getMillis() + 3, australiaNSWDaylightInAusraliaNSW.withMillisOfSecond(3).getMillis());
    }

// org.joda.time.TestDateTime_Basics::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestDateTime_Basics::testGet_DateTimeField
    public void testGet_DateTimeField() {
        DateTime test = new DateTime();
        assertEquals(1, test.get(ISO_DEFAULT.era()));
        assertEquals(20, test.get(ISO_DEFAULT.centuryOfEra()));
        assertEquals(2, test.get(ISO_DEFAULT.yearOfCentury()));
        assertEquals(2002, test.get(ISO_DEFAULT.yearOfEra()));
        assertEquals(2002, test.get(ISO_DEFAULT.year()));
        assertEquals(6, test.get(ISO_DEFAULT.monthOfYear()));
        assertEquals(9, test.get(ISO_DEFAULT.dayOfMonth()));
        assertEquals(2002, test.get(ISO_DEFAULT.weekyear()));
        assertEquals(23, test.get(ISO_DEFAULT.weekOfWeekyear()));
        assertEquals(7, test.get(ISO_DEFAULT.dayOfWeek()));
        assertEquals(160, test.get(ISO_DEFAULT.dayOfYear()));
        assertEquals(0, test.get(ISO_DEFAULT.halfdayOfDay()));
        assertEquals(1, test.get(ISO_DEFAULT.hourOfHalfday()));
        assertEquals(1, test.get(ISO_DEFAULT.clockhourOfDay()));
        assertEquals(1, test.get(ISO_DEFAULT.clockhourOfHalfday()));
        assertEquals(1, test.get(ISO_DEFAULT.hourOfDay()));
        assertEquals(0, test.get(ISO_DEFAULT.minuteOfHour()));
        assertEquals(60, test.get(ISO_DEFAULT.minuteOfDay()));
        assertEquals(0, test.get(ISO_DEFAULT.secondOfMinute()));
        assertEquals(60 * 60, test.get(ISO_DEFAULT.secondOfDay()));
        assertEquals(0, test.get(ISO_DEFAULT.millisOfSecond()));
        assertEquals(60 * 60 * 1000, test.get(ISO_DEFAULT.millisOfDay()));
        try {
            test.get((DateTimeField) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testGet_DateTimeFieldType
    public void testGet_DateTimeFieldType() {
        DateTime test = new DateTime();
        assertEquals(1, test.get(DateTimeFieldType.era()));
        assertEquals(20, test.get(DateTimeFieldType.centuryOfEra()));
        assertEquals(2, test.get(DateTimeFieldType.yearOfCentury()));
        assertEquals(2002, test.get(DateTimeFieldType.yearOfEra()));
        assertEquals(2002, test.get(DateTimeFieldType.year()));
        assertEquals(6, test.get(DateTimeFieldType.monthOfYear()));
        assertEquals(9, test.get(DateTimeFieldType.dayOfMonth()));
        assertEquals(2002, test.get(DateTimeFieldType.weekyear()));
        assertEquals(23, test.get(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(7, test.get(DateTimeFieldType.dayOfWeek()));
        assertEquals(160, test.get(DateTimeFieldType.dayOfYear()));
        assertEquals(0, test.get(DateTimeFieldType.halfdayOfDay()));
        assertEquals(1, test.get(DateTimeFieldType.hourOfHalfday()));
        assertEquals(1, test.get(DateTimeFieldType.clockhourOfDay()));
        assertEquals(1, test.get(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(1, test.get(DateTimeFieldType.hourOfDay()));
        assertEquals(0, test.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(60, test.get(DateTimeFieldType.minuteOfDay()));
        assertEquals(0, test.get(DateTimeFieldType.secondOfMinute()));
        assertEquals(60 * 60, test.get(DateTimeFieldType.secondOfDay()));
        assertEquals(0, test.get(DateTimeFieldType.millisOfSecond()));
        assertEquals(60 * 60 * 1000, test.get(DateTimeFieldType.millisOfDay()));
        try {
            test.get((DateTimeFieldType) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testIsSupported_DateTimeFieldType
    public void testIsSupported_DateTimeFieldType() {
        DateTime test = new DateTime();
        assertEquals(true, test.isSupported(DateTimeFieldType.era()));
        assertEquals(true, test.isSupported(DateTimeFieldType.centuryOfEra()));
        assertEquals(true, test.isSupported(DateTimeFieldType.yearOfCentury()));
        assertEquals(true, test.isSupported(DateTimeFieldType.yearOfEra()));
        assertEquals(true, test.isSupported(DateTimeFieldType.year()));
        assertEquals(true, test.isSupported(DateTimeFieldType.monthOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekyear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.weekOfWeekyear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfWeek()));
        assertEquals(true, test.isSupported(DateTimeFieldType.dayOfYear()));
        assertEquals(true, test.isSupported(DateTimeFieldType.halfdayOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfHalfday()));
        assertEquals(true, test.isSupported(DateTimeFieldType.clockhourOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.clockhourOfHalfday()));
        assertEquals(true, test.isSupported(DateTimeFieldType.hourOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfHour()));
        assertEquals(true, test.isSupported(DateTimeFieldType.minuteOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.secondOfMinute()));
        assertEquals(true, test.isSupported(DateTimeFieldType.secondOfDay()));
        assertEquals(true, test.isSupported(DateTimeFieldType.millisOfSecond()));
        assertEquals(true, test.isSupported(DateTimeFieldType.millisOfDay()));
        assertEquals(false, test.isSupported(null));
    }

// org.joda.time.TestDateTime_Basics::testGetters
    public void testGetters() {
        DateTime test = new DateTime();
        
        assertEquals(ISO_DEFAULT, test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        
        assertEquals(1, test.getEra());
        assertEquals(20, test.getCenturyOfEra());
        assertEquals(2, test.getYearOfCentury());
        assertEquals(2002, test.getYearOfEra());
        assertEquals(2002, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(9, test.getDayOfMonth());
        assertEquals(2002, test.getWeekyear());
        assertEquals(23, test.getWeekOfWeekyear());
        assertEquals(7, test.getDayOfWeek());
        assertEquals(160, test.getDayOfYear());
        assertEquals(1, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(60, test.getMinuteOfDay());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(60 * 60, test.getSecondOfDay());
        assertEquals(0, test.getMillisOfSecond());
        assertEquals(60 * 60 * 1000, test.getMillisOfDay());
    }

// org.joda.time.TestDateTime_Basics::testWithers
    public void testWithers() {
        DateTime test = new DateTime(1970, 6, 9, 10, 20, 30, 40, GJ_DEFAULT);
        check(test.withYear(2000), 2000, 6, 9, 10, 20, 30, 40);
        check(test.withMonthOfYear(2), 1970, 2, 9, 10, 20, 30, 40);
        check(test.withDayOfMonth(2), 1970, 6, 2, 10, 20, 30, 40);
        check(test.withDayOfYear(6), 1970, 1, 6, 10, 20, 30, 40);
        check(test.withDayOfWeek(6), 1970, 6, 13, 10, 20, 30, 40);
        check(test.withWeekOfWeekyear(6), 1970, 2, 3, 10, 20, 30, 40);
        check(test.withWeekyear(1971), 1971, 6, 15, 10, 20, 30, 40);
        check(test.withYearOfCentury(60), 1960, 6, 9, 10, 20, 30, 40);
        check(test.withCenturyOfEra(21), 2070, 6, 9, 10, 20, 30, 40);
        check(test.withYearOfEra(1066), 1066, 6, 9, 10, 20, 30, 40);
        check(test.withEra(DateTimeConstants.BC), -1970, 6, 9, 10, 20, 30, 40);
        check(test.withHourOfDay(6), 1970, 6, 9, 6, 20, 30, 40);
        check(test.withMinuteOfHour(6), 1970, 6, 9, 10, 6, 30, 40);
        check(test.withSecondOfMinute(6), 1970, 6, 9, 10, 20, 6, 40);
        check(test.withMillisOfSecond(6), 1970, 6, 9, 10, 20, 30, 6);
        check(test.withMillisOfDay(61234), 1970, 6, 9, 0, 1, 1, 234);
        
        try {
            test.withMonthOfYear(0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.withMonthOfYear(13);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testEqualsHashCode
    public void testEqualsHashCode() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test2 = new DateTime(TEST_TIME1);
        assertEquals(true, test1.equals(test2));
        assertEquals(true, test2.equals(test1));
        assertEquals(true, test1.equals(test1));
        assertEquals(true, test2.equals(test2));
        assertEquals(true, test1.hashCode() == test2.hashCode());
        assertEquals(true, test1.hashCode() == test1.hashCode());
        assertEquals(true, test2.hashCode() == test2.hashCode());
        
        DateTime test3 = new DateTime(TEST_TIME2);
        assertEquals(false, test1.equals(test3));
        assertEquals(false, test2.equals(test3));
        assertEquals(false, test3.equals(test1));
        assertEquals(false, test3.equals(test2));
        assertEquals(false, test1.hashCode() == test3.hashCode());
        assertEquals(false, test2.hashCode() == test3.hashCode());
        
        assertEquals(false, test1.equals("Hello"));
        assertEquals(true, test1.equals(new MockInstant()));
        assertEquals(false, test1.equals(new DateTime(TEST_TIME1, GREGORIAN_DEFAULT)));
        assertEquals(true, new DateTime(TEST_TIME1, new MockEqualsChronology()).equals(new DateTime(TEST_TIME1, new MockEqualsChronology())));
        assertEquals(false, new DateTime(TEST_TIME1, new MockEqualsChronology()).equals(new DateTime(TEST_TIME1, ISO_DEFAULT)));
    }

// org.joda.time.TestDateTime_Basics::testCompareTo
    public void testCompareTo() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test1a = new DateTime(TEST_TIME1);
        assertEquals(0, test1.compareTo(test1a));
        assertEquals(0, test1a.compareTo(test1));
        assertEquals(0, test1.compareTo(test1));
        assertEquals(0, test1a.compareTo(test1a));
        
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(-1, test1.compareTo(test2));
        assertEquals(+1, test2.compareTo(test1));
        
        DateTime test3 = new DateTime(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(-1, test1.compareTo(test3));
        assertEquals(+1, test3.compareTo(test1));
        assertEquals(0, test3.compareTo(test2));
        
        assertEquals(+1, test2.compareTo(new MockInstant()));
        assertEquals(0, test1.compareTo(new MockInstant()));
        
        try {
            test1.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}

    }

// org.joda.time.TestDateTime_Basics::testIsEqual_long
    public void testIsEqual_long() {
        assertEquals(false, new DateTime(TEST_TIME1).isEqual(TEST_TIME2));
        assertEquals(true, new DateTime(TEST_TIME1).isEqual(TEST_TIME1));
        assertEquals(false, new DateTime(TEST_TIME2).isEqual(TEST_TIME1));
    }

// org.joda.time.TestDateTime_Basics::testIsEqualNow
    public void testIsEqualNow() {
        assertEquals(false, new DateTime(TEST_TIME_NOW - 1).isEqualNow());
        assertEquals(true, new DateTime(TEST_TIME_NOW).isEqualNow());
        assertEquals(false, new DateTime(TEST_TIME_NOW + 1).isEqualNow());
    }

// org.joda.time.TestDateTime_Basics::testIsEqual_RI
    public void testIsEqual_RI() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test1a = new DateTime(TEST_TIME1);
        assertEquals(true, test1.isEqual(test1a));
        assertEquals(true, test1a.isEqual(test1));
        assertEquals(true, test1.isEqual(test1));
        assertEquals(true, test1a.isEqual(test1a));
        
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(false, test1.isEqual(test2));
        assertEquals(false, test2.isEqual(test1));
        
        DateTime test3 = new DateTime(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(false, test1.isEqual(test3));
        assertEquals(false, test3.isEqual(test1));
        assertEquals(true, test3.isEqual(test2));
        
        assertEquals(false, test2.isEqual(new MockInstant()));
        assertEquals(true, test1.isEqual(new MockInstant()));
        
        assertEquals(false, new DateTime(TEST_TIME_NOW + 1).isEqual(null));
        assertEquals(true, new DateTime(TEST_TIME_NOW).isEqual(null));
        assertEquals(false, new DateTime(TEST_TIME_NOW - 1).isEqual(null));
    }

// org.joda.time.TestDateTime_Basics::testIsBefore_long
    public void testIsBefore_long() {
        assertEquals(true, new DateTime(TEST_TIME1).isBefore(TEST_TIME2));
        assertEquals(false, new DateTime(TEST_TIME1).isBefore(TEST_TIME1));
        assertEquals(false, new DateTime(TEST_TIME2).isBefore(TEST_TIME1));
    }

// org.joda.time.TestDateTime_Basics::testIsBeforeNow
    public void testIsBeforeNow() {
        assertEquals(true, new DateTime(TEST_TIME_NOW - 1).isBeforeNow());
        assertEquals(false, new DateTime(TEST_TIME_NOW).isBeforeNow());
        assertEquals(false, new DateTime(TEST_TIME_NOW + 1).isBeforeNow());
    }

// org.joda.time.TestDateTime_Basics::testIsBefore_RI
    public void testIsBefore_RI() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test1a = new DateTime(TEST_TIME1);
        assertEquals(false, test1.isBefore(test1a));
        assertEquals(false, test1a.isBefore(test1));
        assertEquals(false, test1.isBefore(test1));
        assertEquals(false, test1a.isBefore(test1a));
        
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.isBefore(test2));
        assertEquals(false, test2.isBefore(test1));
        
        DateTime test3 = new DateTime(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(true, test1.isBefore(test3));
        assertEquals(false, test3.isBefore(test1));
        assertEquals(false, test3.isBefore(test2));
        
        assertEquals(false, test2.isBefore(new MockInstant()));
        assertEquals(false, test1.isBefore(new MockInstant()));
        
        assertEquals(false, new DateTime(TEST_TIME_NOW + 1).isBefore(null));
        assertEquals(false, new DateTime(TEST_TIME_NOW).isBefore(null));
        assertEquals(true, new DateTime(TEST_TIME_NOW - 1).isBefore(null));
    }

// org.joda.time.TestDateTime_Basics::testIsAfter_long
    public void testIsAfter_long() {
        assertEquals(false, new DateTime(TEST_TIME1).isAfter(TEST_TIME2));
        assertEquals(false, new DateTime(TEST_TIME1).isAfter(TEST_TIME1));
        assertEquals(true, new DateTime(TEST_TIME2).isAfter(TEST_TIME1));
    }

// org.joda.time.TestDateTime_Basics::testIsAfterNow
    public void testIsAfterNow() {
        assertEquals(false, new DateTime(TEST_TIME_NOW - 1).isAfterNow());
        assertEquals(false, new DateTime(TEST_TIME_NOW).isAfterNow());
        assertEquals(true, new DateTime(TEST_TIME_NOW + 1).isAfterNow());
    }

// org.joda.time.TestDateTime_Basics::testIsAfter_RI
    public void testIsAfter_RI() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test1a = new DateTime(TEST_TIME1);
        assertEquals(false, test1.isAfter(test1a));
        assertEquals(false, test1a.isAfter(test1));
        assertEquals(false, test1.isAfter(test1));
        assertEquals(false, test1a.isAfter(test1a));
        
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(false, test1.isAfter(test2));
        assertEquals(true, test2.isAfter(test1));
        
        DateTime test3 = new DateTime(TEST_TIME2, GREGORIAN_PARIS);
        assertEquals(false, test1.isAfter(test3));
        assertEquals(true, test3.isAfter(test1));
        assertEquals(false, test3.isAfter(test2));
        
        assertEquals(true, test2.isAfter(new MockInstant()));
        assertEquals(false, test1.isAfter(new MockInstant()));
        
        assertEquals(true, new DateTime(TEST_TIME_NOW + 1).isAfter(null));
        assertEquals(false, new DateTime(TEST_TIME_NOW).isAfter(null));
        assertEquals(false, new DateTime(TEST_TIME_NOW - 1).isAfter(null));
    }

// org.joda.time.TestDateTime_Basics::testSerialization
    public void testSerialization() throws Exception {
        DateTime test = new DateTime(TEST_TIME_NOW);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(test);
        byte[] bytes = baos.toByteArray();
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTime result = (DateTime) ois.readObject();
        ois.close();
        
        assertEquals(test, result);
    }

// org.joda.time.TestDateTime_Basics::testToString
    public void testToString() {
        DateTime test = new DateTime(TEST_TIME_NOW);
        assertEquals("2002-06-09T01:00:00.000+01:00", test.toString());
        
        test = new DateTime(TEST_TIME_NOW, PARIS);
        assertEquals("2002-06-09T02:00:00.000+02:00", test.toString());
    }

// org.joda.time.TestDateTime_Basics::testToString_String
    public void testToString_String() {
        DateTime test = new DateTime(TEST_TIME_NOW);
        assertEquals("2002 01", test.toString("yyyy HH"));
        assertEquals("2002-06-09T01:00:00.000+01:00", test.toString((String) null));
    }

// org.joda.time.TestDateTime_Basics::testToString_String_Locale
    public void testToString_String_Locale() {
        DateTime test = new DateTime(TEST_TIME_NOW);
        assertEquals("Sun 9/6", test.toString("EEE d/M", Locale.ENGLISH));
        assertEquals("dim. 9/6", test.toString("EEE d/M", Locale.FRENCH));
        assertEquals("2002-06-09T01:00:00.000+01:00", test.toString(null, Locale.ENGLISH));
        assertEquals("Sun 9/6", test.toString("EEE d/M", null));
        assertEquals("2002-06-09T01:00:00.000+01:00", test.toString(null, null));
    }

// org.joda.time.TestDateTime_Basics::testToString_DTFormatter
    public void testToString_DTFormatter() {
        DateMidnight test = new DateMidnight(TEST_TIME_NOW);
        assertEquals("2002 00", test.toString(DateTimeFormat.forPattern("yyyy HH")));
        assertEquals("2002-06-09T00:00:00.000+01:00", test.toString((DateTimeFormatter) null));
    }

// org.joda.time.TestDateTime_Basics::testToInstant
    public void testToInstant() {
        DateTime test = new DateTime(TEST_TIME1);
        Instant result = test.toInstant();
        assertEquals(TEST_TIME1, result.getMillis());
    }

// org.joda.time.TestDateTime_Basics::testToDateTime
    public void testToDateTime() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.toDateTime();
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testToDateTimeISO
    public void testToDateTimeISO() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.toDateTimeISO();
        assertSame(test, result);
        
        test = new DateTime(TEST_TIME1, ISO_PARIS);
        result = test.toDateTimeISO();
        assertSame(DateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
        assertNotSame(test, result);
        
        test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        result = test.toDateTimeISO();
        assertSame(DateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
        assertNotSame(test, result);
        
        test = new DateTime(TEST_TIME1, new MockNullZoneChronology());
        result = test.toDateTimeISO();
        assertSame(DateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
        assertNotSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testToDateTime_DateTimeZone
    public void testToDateTime_DateTimeZone() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.toDateTime(LONDON);
        assertSame(test, result);

        test = new DateTime(TEST_TIME1);
        result = test.toDateTime(PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(PARIS, result.getZone());

        test = new DateTime(TEST_TIME1, PARIS);
        result = test.toDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(LONDON, result.getZone());

        test = new DateTime(TEST_TIME1);
        result = test.toDateTime((DateTimeZone) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testToDateTime_Chronology
    public void testToDateTime_Chronology() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.toDateTime(ISO_DEFAULT);
        assertSame(test, result);

        test = new DateTime(TEST_TIME1);
        result = test.toDateTime(GREGORIAN_PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GREGORIAN_PARIS, result.getChronology());

        test = new DateTime(TEST_TIME1, GREGORIAN_PARIS);
        result = test.toDateTime((Chronology) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateTime(TEST_TIME1);
        result = test.toDateTime((Chronology) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testToMutableDateTime
    public void testToMutableDateTime() {
        DateTime test = new DateTime(TEST_TIME1, PARIS);
        MutableDateTime result = test.toMutableDateTime();
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
    }

// org.joda.time.TestDateTime_Basics::testToMutableDateTimeISO
    public void testToMutableDateTimeISO() {
        DateTime test = new DateTime(TEST_TIME1, PARIS);
        MutableDateTime result = test.toMutableDateTimeISO();
        assertSame(MutableDateTime.class, result.getClass());
        assertSame(ISOChronology.class, result.getChronology().getClass());
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
    }

// org.joda.time.TestDateTime_Basics::testToMutableDateTime_DateTimeZone
    public void testToMutableDateTime_DateTimeZone() {
        DateTime test = new DateTime(TEST_TIME1);
        MutableDateTime result = test.toMutableDateTime(LONDON);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateTime(TEST_TIME1);
        result = test.toMutableDateTime(PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());

        test = new DateTime(TEST_TIME1, PARIS);
        result = test.toMutableDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateTime(TEST_TIME1);
        result = test.toMutableDateTime((DateTimeZone) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
    }

// org.joda.time.TestDateTime_Basics::testToMutableDateTime_Chronology
    public void testToMutableDateTime_Chronology() {
        DateTime test = new DateTime(TEST_TIME1);
        MutableDateTime result = test.toMutableDateTime(ISO_DEFAULT);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateTime(TEST_TIME1);
        result = test.toMutableDateTime(GREGORIAN_PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GREGORIAN_PARIS, result.getChronology());

        test = new DateTime(TEST_TIME1, GREGORIAN_PARIS);
        result = test.toMutableDateTime((Chronology) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());

        test = new DateTime(TEST_TIME1);
        result = test.toMutableDateTime((Chronology) null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
    }

// org.joda.time.TestDateTime_Basics::testToDate
    public void testToDate() {
        DateTime test = new DateTime(TEST_TIME1);
        Date result = test.toDate();
        assertEquals(test.getMillis(), result.getTime());
    }

// org.joda.time.TestDateTime_Basics::testToCalendar_Locale
    public void testToCalendar_Locale() {
        DateTime test = new DateTime(TEST_TIME1);
        Calendar result = test.toCalendar(null);
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/London"), result.getTimeZone());

        test = new DateTime(TEST_TIME1, PARIS);
        result = test.toCalendar(null);
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/Paris"), result.getTimeZone());

        test = new DateTime(TEST_TIME1, PARIS);
        result = test.toCalendar(Locale.UK);
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/Paris"), result.getTimeZone());
    }

// org.joda.time.TestDateTime_Basics::testToGregorianCalendar
    public void testToGregorianCalendar() {
        DateTime test = new DateTime(TEST_TIME1);
        GregorianCalendar result = test.toGregorianCalendar();
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/London"), result.getTimeZone());

        test = new DateTime(TEST_TIME1, PARIS);
        result = test.toGregorianCalendar();
        assertEquals(test.getMillis(), result.getTime().getTime());
        assertEquals(TimeZone.getTimeZone("Europe/Paris"), result.getTimeZone());
    }

// org.joda.time.TestDateTime_Basics::testToDateMidnight
    public void testToDateMidnight() {
        DateTime base = new DateTime(TEST_TIME1, COPTIC_DEFAULT);
        DateMidnight test = base.toDateMidnight();
        assertEquals(new DateMidnight(base, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateTime_Basics::testToYearMonthDay
    public void testToYearMonthDay() {
        DateTime base = new DateTime(TEST_TIME1, COPTIC_DEFAULT);
        YearMonthDay test = base.toYearMonthDay();
        assertEquals(new YearMonthDay(TEST_TIME1, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateTime_Basics::testToTimeOfDay
    public void testToTimeOfDay() {
        DateTime base = new DateTime(TEST_TIME1, COPTIC_DEFAULT);
        TimeOfDay test = base.toTimeOfDay();
        assertEquals(new TimeOfDay(TEST_TIME1, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateTime_Basics::testToLocalDateTime
    public void testToLocalDateTime() {
        DateTime base = new DateTime(TEST_TIME1, COPTIC_DEFAULT);
        LocalDateTime test = base.toLocalDateTime();
        assertEquals(new LocalDateTime(TEST_TIME1, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateTime_Basics::testToLocalDate
    public void testToLocalDate() {
        DateTime base = new DateTime(TEST_TIME1, COPTIC_DEFAULT);
        LocalDate test = base.toLocalDate();
        assertEquals(new LocalDate(TEST_TIME1, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateTime_Basics::testToLocalTime
    public void testToLocalTime() {
        DateTime base = new DateTime(TEST_TIME1, COPTIC_DEFAULT);
        LocalTime test = base.toLocalTime();
        assertEquals(new LocalTime(TEST_TIME1, COPTIC_DEFAULT), test);
    }

// org.joda.time.TestDateTime_Basics::testWithMillis_long
    public void testWithMillis_long() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.withMillis(TEST_TIME2);
        assertEquals(TEST_TIME2, result.getMillis());
        assertEquals(test.getChronology(), result.getChronology());
        
        test = new DateTime(TEST_TIME1, GREGORIAN_PARIS);
        result = test.withMillis(TEST_TIME2);
        assertEquals(TEST_TIME2, result.getMillis());
        assertEquals(test.getChronology(), result.getChronology());
        
        test = new DateTime(TEST_TIME1);
        result = test.withMillis(TEST_TIME1);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testWithChronology_Chronology
    public void testWithChronology_Chronology() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.withChronology(GREGORIAN_PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GREGORIAN_PARIS, result.getChronology());
        
        test = new DateTime(TEST_TIME1, GREGORIAN_PARIS);
        result = test.withChronology(null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
        
        test = new DateTime(TEST_TIME1);
        result = test.withChronology(null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_DEFAULT, result.getChronology());
        
        test = new DateTime(TEST_TIME1);
        result = test.withChronology(ISO_DEFAULT);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testWithZone_DateTimeZone
    public void testWithZone_DateTimeZone() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.withZone(PARIS);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
        
        test = new DateTime(TEST_TIME1, GREGORIAN_PARIS);
        result = test.withZone(null);
        assertEquals(test.getMillis(), result.getMillis());
        assertEquals(GREGORIAN_DEFAULT, result.getChronology());
        
        test = new DateTime(TEST_TIME1);
        result = test.withZone(null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testWithZoneRetainFields_DateTimeZone
    public void testWithZoneRetainFields_DateTimeZone() {
        DateTime test = new DateTime(TEST_TIME1);
        DateTime result = test.withZoneRetainFields(PARIS);
        assertEquals(test.getMillis() - DateTimeConstants.MILLIS_PER_HOUR, result.getMillis());
        assertEquals(ISO_PARIS, result.getChronology());
        
        test = new DateTime(TEST_TIME1);
        result = test.withZoneRetainFields(LONDON);
        assertSame(test, result);
        
        test = new DateTime(TEST_TIME1);
        result = test.withZoneRetainFields(null);
        assertSame(test, result);
        
        test = new DateTime(TEST_TIME1, GREGORIAN_PARIS);
        result = test.withZoneRetainFields(null);
        assertEquals(test.getMillis() + DateTimeConstants.MILLIS_PER_HOUR, result.getMillis());
        assertEquals(GREGORIAN_DEFAULT, result.getChronology());
        
        test = new DateTime(TEST_TIME1, new MockNullZoneChronology());
        result = test.withZoneRetainFields(LONDON);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testWithDate_int_int_int
    public void testWithDate_int_int_int() {
        DateTime test = new DateTime(2002, 4, 5, 1, 2, 3, 4, ISO_UTC);
        DateTime result = test.withDate(2003, 5, 6);
        DateTime expected = new DateTime(2003, 5, 6, 1, 2, 3, 4, ISO_UTC);
        assertEquals(expected, result);
        
        test = new DateTime(TEST_TIME1);
        try {
            test.withDate(2003, 13, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testWithTime_int_int_int
    public void testWithTime_int_int_int() {
        DateTime test = new DateTime(TEST_TIME1 - 12345L, BUDDHIST_UTC);
        DateTime result = test.withTime(12, 24, 0, 0);
        assertEquals(TEST_TIME1, result.getMillis());
        assertEquals(BUDDHIST_UTC, result.getChronology());
        
        test = new DateTime(TEST_TIME1);
        try {
            test.withTime(25, 1, 1, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testWithFields_RPartial
    public void testWithFields_RPartial() {
        DateTime test = new DateTime(2004, 5, 6, 7, 8, 9, 0);
        DateTime result = test.withFields(new YearMonthDay(2003, 4, 5));
        DateTime expected = new DateTime(2003, 4, 5, 7, 8, 9, 0);
        assertEquals(expected, result);
        
        test = new DateTime(TEST_TIME1);
        result = test.withFields(null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testWithField1
    public void testWithField1() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime result = test.withField(DateTimeFieldType.year(), 2006);
        
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), test);
        assertEquals(new DateTime(2006, 6, 9, 0, 0, 0, 0), result);
    }

// org.joda.time.TestDateTime_Basics::testWithField2
    public void testWithField2() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        try {
            test.withField(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testWithFieldAdded1
    public void testWithFieldAdded1() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime result = test.withFieldAdded(DurationFieldType.years(), 6);
        
        assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), test);
        assertEquals(new DateTime(2010, 6, 9, 0, 0, 0, 0), result);
    }

// org.joda.time.TestDateTime_Basics::testWithFieldAdded2
    public void testWithFieldAdded2() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        try {
            test.withFieldAdded(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testWithFieldAdded3
    public void testWithFieldAdded3() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        try {
            test.withFieldAdded(null, 6);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Basics::testWithFieldAdded4
    public void testWithFieldAdded4() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime result = test.withFieldAdded(DurationFieldType.years(), 0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testWithDurationAdded_long_int
    public void testWithDurationAdded_long_int() {
        DateTime test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        DateTime result = test.withDurationAdded(123456789L, 1);
        DateTime expected = new DateTime(TEST_TIME1 + 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(123456789L, 0);
        assertSame(test, result);
        
        result = test.withDurationAdded(123456789L, 2);
        expected = new DateTime(TEST_TIME1 + (2L * 123456789L), BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(123456789L, -3);
        expected = new DateTime(TEST_TIME1 - (3L * 123456789L), BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateTime_Basics::testWithDurationAdded_RD_int
    public void testWithDurationAdded_RD_int() {
        DateTime test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        DateTime result = test.withDurationAdded(new Duration(123456789L), 1);
        DateTime expected = new DateTime(TEST_TIME1 + 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(null, 1);
        assertSame(test, result);
        
        result = test.withDurationAdded(new Duration(123456789L), 0);
        assertSame(test, result);
        
        result = test.withDurationAdded(new Duration(123456789L), 2);
        expected = new DateTime(TEST_TIME1 + (2L * 123456789L), BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withDurationAdded(new Duration(123456789L), -3);
        expected = new DateTime(TEST_TIME1 - (3L * 123456789L), BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateTime_Basics::testWithDurationAdded_RP_int
    public void testWithDurationAdded_RP_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.withPeriodAdded(new Period(1, 2, 3, 4, 5, 6, 7, 8), 1);
        DateTime expected = new DateTime(2003, 7, 28, 6, 8, 10, 12, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withPeriodAdded(null, 1);
        assertSame(test, result);
        
        result = test.withPeriodAdded(new Period(1, 2, 3, 4, 5, 6, 7, 8), 0);
        assertSame(test, result);
        
        result = test.withPeriodAdded(new Period(1, 2, 0, 4, 5, 6, 7, 8), 3);
        expected = new DateTime(2005, 11, 15, 16, 20, 24, 28, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.withPeriodAdded(new Period(1, 2, 0, 1, 1, 2, 3, 4), -1);
        expected = new DateTime(2001, 3, 2, 0, 0, 0, 0, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateTime_Basics::testPlus_long
    public void testPlus_long() {
        DateTime test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        DateTime result = test.plus(123456789L);
        DateTime expected = new DateTime(TEST_TIME1 + 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateTime_Basics::testPlus_RD
    public void testPlus_RD() {
        DateTime test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        DateTime result = test.plus(new Duration(123456789L));
        DateTime expected = new DateTime(TEST_TIME1 + 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plus((ReadableDuration) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlus_RP
    public void testPlus_RP() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plus(new Period(1, 2, 3, 4, 5, 6, 7, 8));
        DateTime expected = new DateTime(2003, 7, 28, 6, 8, 10, 12, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusYears_int
    public void testPlusYears_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusYears(1);
        DateTime expected = new DateTime(2003, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusMonths_int
    public void testPlusMonths_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusMonths(1);
        DateTime expected = new DateTime(2002, 6, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusWeeks_int
    public void testPlusWeeks_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusWeeks(1);
        DateTime expected = new DateTime(2002, 5, 10, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusDays_int
    public void testPlusDays_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusDays(1);
        DateTime expected = new DateTime(2002, 5, 4, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusHours_int
    public void testPlusHours_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusHours(1);
        DateTime expected = new DateTime(2002, 5, 3, 2, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusMinutes_int
    public void testPlusMinutes_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusMinutes(1);
        DateTime expected = new DateTime(2002, 5, 3, 1, 3, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusSeconds_int
    public void testPlusSeconds_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusSeconds(1);
        DateTime expected = new DateTime(2002, 5, 3, 1, 2, 4, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testPlusMillis_int
    public void testPlusMillis_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.plusMillis(1);
        DateTime expected = new DateTime(2002, 5, 3, 1, 2, 3, 5, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.plusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinus_long
    public void testMinus_long() {
        DateTime test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        DateTime result = test.minus(123456789L);
        DateTime expected = new DateTime(TEST_TIME1 - 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
    }

// org.joda.time.TestDateTime_Basics::testMinus_RD
    public void testMinus_RD() {
        DateTime test = new DateTime(TEST_TIME1, BUDDHIST_DEFAULT);
        DateTime result = test.minus(new Duration(123456789L));
        DateTime expected = new DateTime(TEST_TIME1 - 123456789L, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minus((ReadableDuration) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinus_RP
    public void testMinus_RP() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        DateTime expected = new DateTime(2001, 3, 26, 0, 1, 2, 3, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minus((ReadablePeriod) null);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusYears_int
    public void testMinusYears_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusYears(1);
        DateTime expected = new DateTime(2001, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusYears(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusMonths_int
    public void testMinusMonths_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusMonths(1);
        DateTime expected = new DateTime(2002, 4, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusMonths(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusWeeks_int
    public void testMinusWeeks_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusWeeks(1);
        DateTime expected = new DateTime(2002, 4, 26, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusWeeks(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusDays_int
    public void testMinusDays_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusDays(1);
        DateTime expected = new DateTime(2002, 5, 2, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusDays(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusHours_int
    public void testMinusHours_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusHours(1);
        DateTime expected = new DateTime(2002, 5, 3, 0, 2, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusHours(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusMinutes_int
    public void testMinusMinutes_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusMinutes(1);
        DateTime expected = new DateTime(2002, 5, 3, 1, 1, 3, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusMinutes(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusSeconds_int
    public void testMinusSeconds_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusSeconds(1);
        DateTime expected = new DateTime(2002, 5, 3, 1, 2, 2, 4, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusSeconds(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testMinusMillis_int
    public void testMinusMillis_int() {
        DateTime test = new DateTime(2002, 5, 3, 1, 2, 3, 4, BUDDHIST_DEFAULT);
        DateTime result = test.minusMillis(1);
        DateTime expected = new DateTime(2002, 5, 3, 1, 2, 3, 3, BUDDHIST_DEFAULT);
        assertEquals(expected, result);
        
        result = test.minusMillis(0);
        assertSame(test, result);
    }

// org.joda.time.TestDateTime_Basics::testProperty
    public void testProperty() {
        DateTime test = new DateTime();
        assertEquals(test.year(), test.property(DateTimeFieldType.year()));
        assertEquals(test.dayOfWeek(), test.property(DateTimeFieldType.dayOfWeek()));
        assertEquals(test.secondOfMinute(), test.property(DateTimeFieldType.secondOfMinute()));
        assertEquals(test.millisOfSecond(), test.property(DateTimeFieldType.millisOfSecond()));
        DateTimeFieldType bad = new DateTimeFieldType("bad") {
            public DurationFieldType getDurationType() {
                return DurationFieldType.weeks();
            }
            public DurationFieldType getRangeDurationType() {
                return null;
            }
            public DateTimeField getField(Chronology chronology) {
                return UnsupportedDateTimeField.getInstance(this, UnsupportedDurationField.getInstance(getDurationType()));
            }
        };
        try {
            test.property(bad);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.property(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestDateTime_Constructors::testParse_noFormatter
    public void testParse_noFormatter() throws Throwable {
        assertEquals(new DateTime(2010, 6, 30, 1, 20, ISOChronology.getInstance(DateTimeZone.forOffsetHours(2))), DateTime.parse("2010-06-30T01:20+02:00"));
        assertEquals(new DateTime(2010, 1, 2, 14, 50, ISOChronology.getInstance(LONDON)), DateTime.parse("2010-002T14:50"));
    }

// org.joda.time.TestDateTime_Constructors::testParse_formatter
    public void testParse_formatter() throws Throwable {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy--dd MM HH").withChronology(ISOChronology.getInstance(PARIS));
        assertEquals(new DateTime(2010, 6, 30, 13, 0, ISOChronology.getInstance(PARIS)), DateTime.parse("2010--30 06 13", f));
    }

// org.joda.time.TestDateTime_Constructors::testConstructor
    public void testConstructor() throws Throwable {
        DateTime test = new DateTime();
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_DateTimeZone
    public void testConstructor_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullDateTimeZone
    public void testConstructor_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime((DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Chronology
    public void testConstructor_Chronology() throws Throwable {
        DateTime test = new DateTime(GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullChronology
    public void testConstructor_nullChronology() throws Throwable {
        DateTime test = new DateTime((Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long1
    public void testConstructor_long1() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long2
    public void testConstructor_long2() throws Throwable {
        DateTime test = new DateTime(TEST_TIME2);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long1_DateTimeZone
    public void testConstructor_long1_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long2_DateTimeZone
    public void testConstructor_long2_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(TEST_TIME2, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long_nullDateTimeZone
    public void testConstructor_long_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long1_Chronology
    public void testConstructor_long1_Chronology() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long2_Chronology
    public void testConstructor_long2_Chronology() throws Throwable {
        DateTime test = new DateTime(TEST_TIME2, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME2, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_long_nullChronology
    public void testConstructor_long_nullChronology() throws Throwable {
        DateTime test = new DateTime(TEST_TIME1, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object
    public void testConstructor_Object() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_invalidObject
    public void testConstructor_invalidObject() throws Throwable {
        try {
            new DateTime(new Object());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject
    public void testConstructor_nullObject() throws Throwable {
        DateTime test = new DateTime((Object) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_badconverterObject
    public void testConstructor_badconverterObject() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateTime test = new DateTime(new Integer(0));
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString1
    public void testConstructor_ObjectString1() throws Throwable {
        DateTime test = new DateTime("1972-12-03");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
        assertEquals(0, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString2
    public void testConstructor_ObjectString2() throws Throwable {
        DateTime test = new DateTime("2006-06-03T+14:00");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(2006, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(2, test.getDayOfMonth());  
        assertEquals(11, test.getHourOfDay());  
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString3
    public void testConstructor_ObjectString3() throws Throwable {
        DateTime test = new DateTime("1972-12-03T10:20:30.040");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(1972, test.getYear());
        assertEquals(12, test.getMonthOfYear());
        assertEquals(3, test.getDayOfMonth());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString4
    public void testConstructor_ObjectString4() throws Throwable {
        DateTime test = new DateTime("2006-06-03T10:20:30.040+14:00");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(2006, test.getYear());
        assertEquals(6, test.getMonthOfYear());
        assertEquals(2, test.getDayOfMonth());  
        assertEquals(21, test.getHourOfDay());  
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString5
    public void testConstructor_ObjectString5() throws Throwable {
        DateTime test = new DateTime("T10:20:30.040");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(1970, test.getYear());
        assertEquals(1, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
        assertEquals(10, test.getHourOfDay());
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString6
    public void testConstructor_ObjectString6() throws Throwable {
        DateTime test = new DateTime("T10:20:30.040+14:00");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(1969, test.getYear());  
        assertEquals(12, test.getMonthOfYear());  
        assertEquals(31, test.getDayOfMonth());  
        assertEquals(21, test.getHourOfDay());  
        assertEquals(20, test.getMinuteOfHour());
        assertEquals(30, test.getSecondOfMinute());
        assertEquals(40, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectString7
    public void testConstructor_ObjectString7() throws Throwable {
        DateTime test = new DateTime("10");
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(10, test.getYear());
        assertEquals(1, test.getMonthOfYear());
        assertEquals(1, test.getDayOfMonth());
        assertEquals(0, test.getHourOfDay());
        assertEquals(0, test.getMinuteOfHour());
        assertEquals(0, test.getSecondOfMinute());
        assertEquals(0, test.getMillisOfSecond());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectStringEx1
    public void testConstructor_ObjectStringEx1() throws Throwable {
        try {
            new DateTime("10:20:30.040");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_ObjectStringEx2
    public void testConstructor_ObjectStringEx2() throws Throwable {
        try {
            new DateTime("10:20:30.040+14:00");
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object_DateTimeZone
    public void testConstructor_Object_DateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_invalidObject_DateTimeZone
    public void testConstructor_invalidObject_DateTimeZone() throws Throwable {
        try {
            new DateTime(new Object(), PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject_DateTimeZone
    public void testConstructor_nullObject_DateTimeZone() throws Throwable {
        DateTime test = new DateTime((Object) null, PARIS);
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object_nullDateTimeZone
    public void testConstructor_Object_nullDateTimeZone() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject_nullDateTimeZone
    public void testConstructor_nullObject_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime((Object) null, (DateTimeZone) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_badconverterObject_DateTimeZone
    public void testConstructor_badconverterObject_DateTimeZone() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateTime test = new DateTime(new Integer(0), GregorianChronology.getInstance());
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object_Chronology
    public void testConstructor_Object_Chronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_invalidObject_Chronology
    public void testConstructor_invalidObject_Chronology() throws Throwable {
        try {
            new DateTime(new Object(), GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject_Chronology
    public void testConstructor_nullObject_Chronology() throws Throwable {
        DateTime test = new DateTime((Object) null, GregorianChronology.getInstance());
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_Object_nullChronology
    public void testConstructor_Object_nullChronology() throws Throwable {
        Date date = new Date(TEST_TIME1);
        DateTime test = new DateTime(date, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME1, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_nullObject_nullChronology
    public void testConstructor_nullObject_nullChronology() throws Throwable {
        DateTime test = new DateTime((Object) null, (Chronology) null);
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_badconverterObject_Chronology
    public void testConstructor_badconverterObject_Chronology() throws Throwable {
        try {
            ConverterManager.getInstance().addInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
            DateTime test = new DateTime(new Integer(0), GregorianChronology.getInstance());
            assertEquals(ISOChronology.getInstance(), test.getChronology());
            assertEquals(0L, test.getMillis());
        } finally {
            ConverterManager.getInstance().removeInstantConverter(MockZeroNullIntegerConverter.INSTANCE);
        }
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int
    public void testConstructor_int_int_int_int_int() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_DateTimeZone
    public void testConstructor_int_int_int_int_int_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 2, 0, PARIS);  
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_nullDateTimeZone
    public void testConstructor_int_int_int_int_int_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, (DateTimeZone) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_Chronology
    public void testConstructor_int_int_int_int_int_Chronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, GregorianChronology.getInstance());  
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_nullChronology
    public void testConstructor_int_int_int_int_int_nullChronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, (Chronology) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int
    public void testConstructor_int_int_int_int_int_int() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_DateTimeZone
    public void testConstructor_int_int_int_int_int_int_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 2, 0, 0, PARIS);  
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_nullDateTimeZone
    public void testConstructor_int_int_int_int_int_int_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, (DateTimeZone) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_Chronology
    public void testConstructor_int_int_int_int_int_int_Chronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, GregorianChronology.getInstance());  
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_nullChronology
    public void testConstructor_int_int_int_int_int_int_nullChronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, (Chronology) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int
    public void testConstructor_int_int_int_int_int_int_int() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, 0);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(LONDON, test.getZone());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        try {
            new DateTime(Integer.MIN_VALUE, 6, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(Integer.MAX_VALUE, 6, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 0, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 13, 9, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 0, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 31, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateTime(2002, 7, 31, 0, 0, 0, 0);
        try {
            new DateTime(2002, 7, 32, 0, 0, 0, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_DateTimeZone
    public void testConstructor_int_int_int_int_int_int_int_DateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 2, 0, 0, 0, PARIS);  
        assertEquals(ISOChronology.getInstance(PARIS), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        try {
            new DateTime(Integer.MIN_VALUE, 6, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(Integer.MAX_VALUE, 6, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 0, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 13, 9, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 0, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 31, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateTime(2002, 7, 31, 0, 0, 0, 0, PARIS);
        try {
            new DateTime(2002, 7, 32, 0, 0, 0, 0, PARIS);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_nullDateTimeZone
    public void testConstructor_int_int_int_int_int_int_int_nullDateTimeZone() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, 0, (DateTimeZone) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_Chronology
    public void testConstructor_int_int_int_int_int_int_int_Chronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, 0, GregorianChronology.getInstance());  
        assertEquals(GregorianChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
        try {
            new DateTime(Integer.MIN_VALUE, 6, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(Integer.MAX_VALUE, 6, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 0, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 13, 9, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 0, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new DateTime(2002, 6, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
        new DateTime(2002, 7, 31, 0, 0, 0, 0, GregorianChronology.getInstance());
        try {
            new DateTime(2002, 7, 32, 0, 0, 0, 0, GregorianChronology.getInstance());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Constructors::testConstructor_int_int_int_int_int_int_int_nullChronology
    public void testConstructor_int_int_int_int_int_int_int_nullChronology() throws Throwable {
        DateTime test = new DateTime(2002, 6, 9, 1, 0, 0, 0, (Chronology) null);  
        assertEquals(ISOChronology.getInstance(), test.getChronology());
        assertEquals(TEST_TIME_NOW, test.getMillis());
    }

// org.joda.time.TestDateTime_Properties::testTest
    public void testTest() {
        assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
        assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
        assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetEra
    public void testPropertyGetEra() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().era(), test.era().getField());
        assertEquals("era", test.era().getName());
        assertEquals("Property[era]", test.era().toString());
        assertSame(test, test.era().getDateTime());
        assertEquals(1, test.era().get());
        assertEquals("1", test.era().getAsString());
        assertEquals("AD", test.era().getAsText());
        assertEquals("AD", test.era().getField().getAsText(1, Locale.ENGLISH));
        assertEquals("ap. J.-C.", test.era().getAsText(Locale.FRENCH));
        assertEquals("ap. J.-C.", test.era().getField().getAsText(1, Locale.FRENCH));
        assertEquals("AD", test.era().getAsShortText());
        assertEquals("AD", test.era().getField().getAsShortText(1, Locale.ENGLISH));
        assertEquals("ap. J.-C.", test.era().getAsShortText(Locale.FRENCH));
        assertEquals("ap. J.-C.", test.era().getField().getAsShortText(1, Locale.FRENCH));
        assertEquals(test.getChronology().eras(), test.era().getDurationField());
        assertEquals(null, test.era().getRangeDurationField());
        assertEquals(2, test.era().getMaximumTextLength(null));
        assertEquals(9, test.era().getMaximumTextLength(Locale.FRENCH));
        assertEquals(2, test.era().getMaximumShortTextLength(null));
        assertEquals(9, test.era().getMaximumShortTextLength(Locale.FRENCH));
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetYearOfEra
    public void testPropertyGetYearOfEra() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().yearOfEra(), test.yearOfEra().getField());
        assertEquals("yearOfEra", test.yearOfEra().getName());
        assertEquals("Property[yearOfEra]", test.yearOfEra().toString());
        assertSame(test, test.yearOfEra().getDateTime());
        assertEquals(2004, test.yearOfEra().get());
        assertEquals("2004", test.yearOfEra().getAsString());
        assertEquals("2004", test.yearOfEra().getAsText());
        assertEquals("2004", test.yearOfEra().getAsText(Locale.FRENCH));
        assertEquals("2004", test.yearOfEra().getAsShortText());
        assertEquals("2004", test.yearOfEra().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().years(), test.yearOfEra().getDurationField());
        assertEquals(null, test.yearOfEra().getRangeDurationField());
        assertEquals(9, test.yearOfEra().getMaximumTextLength(null));
        assertEquals(9, test.yearOfEra().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetCenturyOfEra
    public void testPropertyGetCenturyOfEra() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().centuryOfEra(), test.centuryOfEra().getField());
        assertEquals("centuryOfEra", test.centuryOfEra().getName());
        assertEquals("Property[centuryOfEra]", test.centuryOfEra().toString());
        assertSame(test, test.centuryOfEra().getDateTime());
        assertEquals(20, test.centuryOfEra().get());
        assertEquals("20", test.centuryOfEra().getAsString());
        assertEquals("20", test.centuryOfEra().getAsText());
        assertEquals("20", test.centuryOfEra().getAsText(Locale.FRENCH));
        assertEquals("20", test.centuryOfEra().getAsShortText());
        assertEquals("20", test.centuryOfEra().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().centuries(), test.centuryOfEra().getDurationField());
        assertEquals(null, test.centuryOfEra().getRangeDurationField());
        assertEquals(7, test.centuryOfEra().getMaximumTextLength(null));
        assertEquals(7, test.centuryOfEra().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetYearOfCentury
    public void testPropertyGetYearOfCentury() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().yearOfCentury(), test.yearOfCentury().getField());
        assertEquals("yearOfCentury", test.yearOfCentury().getName());
        assertEquals("Property[yearOfCentury]", test.yearOfCentury().toString());
        assertSame(test, test.yearOfCentury().getDateTime());
        assertEquals(4, test.yearOfCentury().get());
        assertEquals("4", test.yearOfCentury().getAsString());
        assertEquals("4", test.yearOfCentury().getAsText());
        assertEquals("4", test.yearOfCentury().getAsText(Locale.FRENCH));
        assertEquals("4", test.yearOfCentury().getAsShortText());
        assertEquals("4", test.yearOfCentury().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().years(), test.yearOfCentury().getDurationField());
        assertEquals(test.getChronology().centuries(), test.yearOfCentury().getRangeDurationField());
        assertEquals(2, test.yearOfCentury().getMaximumTextLength(null));
        assertEquals(2, test.yearOfCentury().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetWeekyear
    public void testPropertyGetWeekyear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().weekyear(), test.weekyear().getField());
        assertEquals("weekyear", test.weekyear().getName());
        assertEquals("Property[weekyear]", test.weekyear().toString());
        assertSame(test, test.weekyear().getDateTime());
        assertEquals(2004, test.weekyear().get());
        assertEquals("2004", test.weekyear().getAsString());
        assertEquals("2004", test.weekyear().getAsText());
        assertEquals("2004", test.weekyear().getAsText(Locale.FRENCH));
        assertEquals("2004", test.weekyear().getAsShortText());
        assertEquals("2004", test.weekyear().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().weekyears(), test.weekyear().getDurationField());
        assertEquals(null, test.weekyear().getRangeDurationField());
        assertEquals(9, test.weekyear().getMaximumTextLength(null));
        assertEquals(9, test.weekyear().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetYear
    public void testPropertyGetYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().year(), test.year().getField());
        assertEquals("year", test.year().getName());
        assertEquals("Property[year]", test.year().toString());
        assertSame(test, test.year().getDateTime());
        assertEquals(2004, test.year().get());
        assertEquals("2004", test.year().getAsString());
        assertEquals("2004", test.year().getAsText());
        assertEquals("2004", test.year().getAsText(Locale.FRENCH));
        assertEquals("2004", test.year().getAsShortText());
        assertEquals("2004", test.year().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().years(), test.year().getDurationField());
        assertEquals(null, test.year().getRangeDurationField());
        assertEquals(9, test.year().getMaximumTextLength(null));
        assertEquals(9, test.year().getMaximumShortTextLength(null));
        assertEquals(-292275054, test.year().getMinimumValue());
        assertEquals(-292275054, test.year().getMinimumValueOverall());
        assertEquals(292278993, test.year().getMaximumValue());
        assertEquals(292278993, test.year().getMaximumValueOverall());
    }

// org.joda.time.TestDateTime_Properties::testPropertyLeapYear
    public void testPropertyLeapYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertEquals(true, test.year().isLeap());
        assertEquals(1, test.year().getLeapAmount());
        assertEquals(test.getChronology().days(), test.year().getLeapDurationField());
        test = new DateTime(2003, 6, 9, 0, 0, 0, 0);
        assertEquals(false, test.year().isLeap());
        assertEquals(0, test.year().getLeapAmount());
        assertEquals(test.getChronology().days(), test.year().getLeapDurationField());
    }

// org.joda.time.TestDateTime_Properties::testPropertyAddYear
    public void testPropertyAddYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.year().addToCopy(9);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2013-06-09T00:00:00.000+01:00", copy.toString());
        
        copy = test.year().addToCopy(0);
        assertEquals("2004-06-09T00:00:00.000+01:00", copy.toString());
        
        copy = test.year().addToCopy(292277023 - 2004);
        assertEquals(292277023, copy.getYear());
        
        try {
            test.year().addToCopy(292278993 - 2004 + 1);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        copy = test.year().addToCopy(-2004);
        assertEquals(0, copy.getYear());
        
        copy = test.year().addToCopy(-2005);
        assertEquals(-1, copy.getYear());
        
        try {
            test.year().addToCopy(-292275054 - 2004 - 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Properties::testPropertyAddWrapFieldYear
    public void testPropertyAddWrapFieldYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.year().addWrapFieldToCopy(9);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2013-06-09T00:00:00.000+01:00", copy.toString());
        
        copy = test.year().addWrapFieldToCopy(0);
        assertEquals(2004, copy.getYear());
        
        copy = test.year().addWrapFieldToCopy(292278993 - 2004 + 1);
        assertEquals(-292275054, copy.getYear());
        
        copy = test.year().addWrapFieldToCopy(-292275054 - 2004 - 1);
        assertEquals(292278993, copy.getYear());
    }

// org.joda.time.TestDateTime_Properties::testPropertySetYear
    public void testPropertySetYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.year().setCopy(1960);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("1960-06-09T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertySetTextYear
    public void testPropertySetTextYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.year().setCopy("1960");
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("1960-06-09T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyCompareToYear
    public void testPropertyCompareToYear() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.year().compareTo(test2) < 0);
        assertEquals(true, test2.year().compareTo(test1) > 0);
        assertEquals(true, test1.year().compareTo(test1) == 0);
        try {
            test1.year().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Properties::testPropertyCompareToYear2
    public void testPropertyCompareToYear2() {
        DateTime test1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        YearMonthDay ymd1 = new YearMonthDay(2003, 6, 9);
        YearMonthDay ymd2 = new YearMonthDay(2004, 6, 9);
        YearMonthDay ymd3 = new YearMonthDay(2005, 6, 9);
        assertEquals(true, test1.year().compareTo(ymd1) > 0);
        assertEquals(true, test1.year().compareTo(ymd2) == 0);
        assertEquals(true, test1.year().compareTo(ymd3) < 0);
        try {
            test1.year().compareTo((ReadablePartial) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Properties::testPropertyEqualsHashCodeYear
    public void testPropertyEqualsHashCodeYear() {
        DateTime test1 = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertEquals(true, test1.year().equals(test1.year()));
        assertEquals(true, test1.year().equals(new DateTime(2004, 6, 9, 0, 0, 0, 0).year()));
        assertEquals(false, test1.year().equals(new DateTime(2004, 6, 9, 0, 0, 0, 0).monthOfYear()));
        assertEquals(false, test1.year().equals(new DateTime(2004, 6, 9, 0, 0, 0, 0, CopticChronology.getInstance()).year()));
        
        assertEquals(true, test1.year().hashCode() == test1.year().hashCode());
        assertEquals(true, test1.year().hashCode() == new DateTime(2004, 6, 9, 0, 0, 0, 0).year().hashCode());
        assertEquals(false, test1.year().hashCode() == new DateTime(2004, 6, 9, 0, 0, 0, 0).monthOfYear().hashCode());
        assertEquals(false, test1.year().hashCode() == new DateTime(2004, 6, 9, 0, 0, 0, 0, CopticChronology.getInstance()).year().hashCode());
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetMonthOfYear
    public void testPropertyGetMonthOfYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().monthOfYear(), test.monthOfYear().getField());
        assertEquals("monthOfYear", test.monthOfYear().getName());
        assertEquals("Property[monthOfYear]", test.monthOfYear().toString());
        assertSame(test, test.monthOfYear().getDateTime());
        assertEquals(6, test.monthOfYear().get());
        assertEquals("6", test.monthOfYear().getAsString());
        assertEquals("June", test.monthOfYear().getAsText());
        assertEquals("June", test.monthOfYear().getField().getAsText(6, Locale.ENGLISH));
        assertEquals("juin", test.monthOfYear().getAsText(Locale.FRENCH));
        assertEquals("juin", test.monthOfYear().getField().getAsText(6, Locale.FRENCH));
        assertEquals("Jun", test.monthOfYear().getAsShortText());
        assertEquals("Jun", test.monthOfYear().getField().getAsShortText(6, Locale.ENGLISH));
        assertEquals("juin", test.monthOfYear().getAsShortText(Locale.FRENCH));
        assertEquals("juin", test.monthOfYear().getField().getAsShortText(6, Locale.FRENCH));
        assertEquals(test.getChronology().months(), test.monthOfYear().getDurationField());
        assertEquals(test.getChronology().years(), test.monthOfYear().getRangeDurationField());
        assertEquals(9, test.monthOfYear().getMaximumTextLength(null));
        assertEquals(3, test.monthOfYear().getMaximumShortTextLength(null));
        test = new DateTime(2004, 7, 9, 0, 0, 0, 0);
        assertEquals("juillet", test.monthOfYear().getAsText(Locale.FRENCH));
        assertEquals("juillet", test.monthOfYear().getField().getAsText(7, Locale.FRENCH));
        assertEquals("juil.", test.monthOfYear().getAsShortText(Locale.FRENCH));
        assertEquals("juil.", test.monthOfYear().getField().getAsShortText(7, Locale.FRENCH));
        assertEquals(1, test.monthOfYear().getMinimumValue());
        assertEquals(1, test.monthOfYear().getMinimumValueOverall());
        assertEquals(12, test.monthOfYear().getMaximumValue());
        assertEquals(12, test.monthOfYear().getMaximumValueOverall());
        assertEquals(1, test.monthOfYear().getMinimumValue());
        assertEquals(1, test.monthOfYear().getMinimumValueOverall());
        assertEquals(12, test.monthOfYear().getMaximumValue());
        assertEquals(12, test.monthOfYear().getMaximumValueOverall());
    }

// org.joda.time.TestDateTime_Properties::testPropertyLeapMonthOfYear
    public void testPropertyLeapMonthOfYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertEquals(false, test.monthOfYear().isLeap());
        assertEquals(0, test.monthOfYear().getLeapAmount());
        assertEquals(test.getChronology().days(), test.monthOfYear().getLeapDurationField());
        
        test = new DateTime(2004, 2, 9, 0, 0, 0, 0);
        assertEquals(true, test.monthOfYear().isLeap());
        assertEquals(1, test.monthOfYear().getLeapAmount());
        assertEquals(test.getChronology().days(), test.monthOfYear().getLeapDurationField());
        
        test = new DateTime(2003, 6, 9, 0, 0, 0, 0);
        assertEquals(false, test.monthOfYear().isLeap());
        assertEquals(0, test.monthOfYear().getLeapAmount());
        assertEquals(test.getChronology().days(), test.monthOfYear().getLeapDurationField());
        
        test = new DateTime(2003, 2, 9, 0, 0, 0, 0);
        assertEquals(false, test.monthOfYear().isLeap());
        assertEquals(0, test.monthOfYear().getLeapAmount());
        assertEquals(test.getChronology().days(), test.monthOfYear().getLeapDurationField());
    }

// org.joda.time.TestDateTime_Properties::testPropertyAddMonthOfYear
    public void testPropertyAddMonthOfYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.monthOfYear().addToCopy(6);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-12-09T00:00:00.000Z", copy.toString());
        
        copy = test.monthOfYear().addToCopy(7);
        assertEquals("2005-01-09T00:00:00.000Z", copy.toString());
        
        copy = test.monthOfYear().addToCopy(-5);
        assertEquals("2004-01-09T00:00:00.000Z", copy.toString());
        
        copy = test.monthOfYear().addToCopy(-6);
        assertEquals("2003-12-09T00:00:00.000Z", copy.toString());
        
        test = new DateTime(2004, 1, 31, 0, 0, 0, 0);
        copy = test.monthOfYear().addToCopy(1);
        assertEquals("2004-01-31T00:00:00.000Z", test.toString());
        assertEquals("2004-02-29T00:00:00.000Z", copy.toString());
        
        copy = test.monthOfYear().addToCopy(2);
        assertEquals("2004-03-31T00:00:00.000+01:00", copy.toString());
        
        copy = test.monthOfYear().addToCopy(3);
        assertEquals("2004-04-30T00:00:00.000+01:00", copy.toString());
        
        test = new DateTime(2003, 1, 31, 0, 0, 0, 0);
        copy = test.monthOfYear().addToCopy(1);
        assertEquals("2003-02-28T00:00:00.000Z", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyAddWrapFieldMonthOfYear
    public void testPropertyAddWrapFieldMonthOfYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.monthOfYear().addWrapFieldToCopy(4);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-10-09T00:00:00.000+01:00", copy.toString());
        
        copy = test.monthOfYear().addWrapFieldToCopy(8);
        assertEquals("2004-02-09T00:00:00.000Z", copy.toString());
        
        copy = test.monthOfYear().addWrapFieldToCopy(-8);
        assertEquals("2004-10-09T00:00:00.000+01:00", copy.toString());
        
        test = new DateTime(2004, 1, 31, 0, 0, 0, 0);
        copy = test.monthOfYear().addWrapFieldToCopy(1);
        assertEquals("2004-01-31T00:00:00.000Z", test.toString());
        assertEquals("2004-02-29T00:00:00.000Z", copy.toString());
        
        copy = test.monthOfYear().addWrapFieldToCopy(2);
        assertEquals("2004-03-31T00:00:00.000+01:00", copy.toString());
        
        copy = test.monthOfYear().addWrapFieldToCopy(3);
        assertEquals("2004-04-30T00:00:00.000+01:00", copy.toString());
        
        test = new DateTime(2005, 1, 31, 0, 0, 0, 0);
        copy = test.monthOfYear().addWrapFieldToCopy(1);
        assertEquals("2005-01-31T00:00:00.000Z", test.toString());
        assertEquals("2005-02-28T00:00:00.000Z", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertySetMonthOfYear
    public void testPropertySetMonthOfYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.monthOfYear().setCopy(12);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-12-09T00:00:00.000Z", copy.toString());
        
        test = new DateTime(2004, 1, 31, 0, 0, 0, 0);
        copy = test.monthOfYear().setCopy(2);
        assertEquals("2004-02-29T00:00:00.000Z", copy.toString());
        
        try {
            test.monthOfYear().setCopy(13);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.monthOfYear().setCopy(0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Properties::testPropertySetTextMonthOfYear
    public void testPropertySetTextMonthOfYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.monthOfYear().setCopy("12");
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-12-09T00:00:00.000Z", copy.toString());
        
        copy = test.monthOfYear().setCopy("December");
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-12-09T00:00:00.000Z", copy.toString());
        
        copy = test.monthOfYear().setCopy("Dec");
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-12-09T00:00:00.000Z", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyCompareToMonthOfYear
    public void testPropertyCompareToMonthOfYear() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.monthOfYear().compareTo(test2) < 0);
        assertEquals(true, test2.monthOfYear().compareTo(test1) > 0);
        assertEquals(true, test1.monthOfYear().compareTo(test1) == 0);
        try {
            test1.monthOfYear().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.monthOfYear().compareTo(dt2) < 0);
        assertEquals(true, test2.monthOfYear().compareTo(dt1) > 0);
        assertEquals(true, test1.monthOfYear().compareTo(dt1) == 0);
        try {
            test1.monthOfYear().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetDayOfMonth
    public void testPropertyGetDayOfMonth() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().dayOfMonth(), test.dayOfMonth().getField());
        assertEquals("dayOfMonth", test.dayOfMonth().getName());
        assertEquals("Property[dayOfMonth]", test.dayOfMonth().toString());
        assertSame(test, test.dayOfMonth().getDateTime());
        assertEquals(9, test.dayOfMonth().get());
        assertEquals("9", test.dayOfMonth().getAsString());
        assertEquals("9", test.dayOfMonth().getAsText());
        assertEquals("9", test.dayOfMonth().getAsText(Locale.FRENCH));
        assertEquals("9", test.dayOfMonth().getAsShortText());
        assertEquals("9", test.dayOfMonth().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().days(), test.dayOfMonth().getDurationField());
        assertEquals(test.getChronology().months(), test.dayOfMonth().getRangeDurationField());
        assertEquals(2, test.dayOfMonth().getMaximumTextLength(null));
        assertEquals(2, test.dayOfMonth().getMaximumShortTextLength(null));
        assertEquals(1, test.dayOfMonth().getMinimumValue());
        assertEquals(1, test.dayOfMonth().getMinimumValueOverall());
        assertEquals(30, test.dayOfMonth().getMaximumValue());
        assertEquals(31, test.dayOfMonth().getMaximumValueOverall());
        assertEquals(false, test.dayOfMonth().isLeap());
        assertEquals(0, test.dayOfMonth().getLeapAmount());
        assertEquals(null, test.dayOfMonth().getLeapDurationField());
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetMaxMinValuesDayOfMonth
    public void testPropertyGetMaxMinValuesDayOfMonth() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertEquals(1, test.dayOfMonth().getMinimumValue());
        assertEquals(1, test.dayOfMonth().getMinimumValueOverall());
        assertEquals(30, test.dayOfMonth().getMaximumValue());
        assertEquals(31, test.dayOfMonth().getMaximumValueOverall());
        test = new DateTime(2004, 7, 9, 0, 0, 0, 0);
        assertEquals(31, test.dayOfMonth().getMaximumValue());
        test = new DateTime(2004, 2, 9, 0, 0, 0, 0);
        assertEquals(29, test.dayOfMonth().getMaximumValue());
        test = new DateTime(2003, 2, 9, 0, 0, 0, 0);
        assertEquals(28, test.dayOfMonth().getMaximumValue());
    }

// org.joda.time.TestDateTime_Properties::testPropertyAddDayOfMonth
    public void testPropertyAddDayOfMonth() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfMonth().addToCopy(9);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-18T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfMonth().addToCopy(21);
        assertEquals("2004-06-30T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfMonth().addToCopy(22);
        assertEquals("2004-07-01T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfMonth().addToCopy(22 + 30);
        assertEquals("2004-07-31T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfMonth().addToCopy(22 + 31);
        assertEquals("2004-08-01T00:00:00.000+01:00", copy.toString());

        copy = test.dayOfMonth().addToCopy(21 + 31 + 31 + 30 + 31 + 30 + 31);
        assertEquals("2004-12-31T00:00:00.000Z", copy.toString());
        
        copy = test.dayOfMonth().addToCopy(22 + 31 + 31 + 30 + 31 + 30 + 31);
        assertEquals("2005-01-01T00:00:00.000Z", copy.toString());
        
        copy = test.dayOfMonth().addToCopy(-8);
        assertEquals("2004-06-01T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfMonth().addToCopy(-9);
        assertEquals("2004-05-31T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfMonth().addToCopy(-8 - 31 - 30 - 31 - 29 - 31);
        assertEquals("2004-01-01T00:00:00.000Z", copy.toString());
        
        copy = test.dayOfMonth().addToCopy(-9 - 31 - 30 - 31 - 29 - 31);
        assertEquals("2003-12-31T00:00:00.000Z", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyAddWrapFieldDayOfMonth
    public void testPropertyAddWrapFieldDayOfMonth() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfMonth().addWrapFieldToCopy(21);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-30T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfMonth().addWrapFieldToCopy(22);
        assertEquals("2004-06-01T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfMonth().addWrapFieldToCopy(-12);
        assertEquals("2004-06-27T00:00:00.000+01:00", copy.toString());
        
        test = new DateTime(2004, 7, 9, 0, 0, 0, 0);
        copy = test.dayOfMonth().addWrapFieldToCopy(21);
        assertEquals("2004-07-30T00:00:00.000+01:00", copy.toString());
    
        copy = test.dayOfMonth().addWrapFieldToCopy(22);
        assertEquals("2004-07-31T00:00:00.000+01:00", copy.toString());
    
        copy = test.dayOfMonth().addWrapFieldToCopy(23);
        assertEquals("2004-07-01T00:00:00.000+01:00", copy.toString());
    
        copy = test.dayOfMonth().addWrapFieldToCopy(-12);
        assertEquals("2004-07-28T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertySetDayOfMonth
    public void testPropertySetDayOfMonth() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfMonth().setCopy(12);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-12T00:00:00.000+01:00", copy.toString());
        
        try {
            test.dayOfMonth().setCopy(31);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.dayOfMonth().setCopy(0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Properties::testPropertySetTextDayOfMonth
    public void testPropertySetTextDayOfMonth() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfMonth().setCopy("12");
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-12T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyWithMaximumValueDayOfMonth
    public void testPropertyWithMaximumValueDayOfMonth() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfMonth().withMaximumValue();
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-30T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyWithMinimumValueDayOfMonth
    public void testPropertyWithMinimumValueDayOfMonth() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfMonth().withMinimumValue();
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-01T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyCompareToDayOfMonth
    public void testPropertyCompareToDayOfMonth() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.dayOfMonth().compareTo(test2) < 0);
        assertEquals(true, test2.dayOfMonth().compareTo(test1) > 0);
        assertEquals(true, test1.dayOfMonth().compareTo(test1) == 0);
        try {
            test1.dayOfMonth().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.dayOfMonth().compareTo(dt2) < 0);
        assertEquals(true, test2.dayOfMonth().compareTo(dt1) > 0);
        assertEquals(true, test1.dayOfMonth().compareTo(dt1) == 0);
        try {
            test1.dayOfMonth().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetDayOfYear
    public void testPropertyGetDayOfYear() {
        
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().dayOfYear(), test.dayOfYear().getField());
        assertEquals("dayOfYear", test.dayOfYear().getName());
        assertEquals("Property[dayOfYear]", test.dayOfYear().toString());
        assertSame(test, test.dayOfYear().getDateTime());
        assertEquals(161, test.dayOfYear().get());
        assertEquals("161", test.dayOfYear().getAsString());
        assertEquals("161", test.dayOfYear().getAsText());
        assertEquals("161", test.dayOfYear().getAsText(Locale.FRENCH));
        assertEquals("161", test.dayOfYear().getAsShortText());
        assertEquals("161", test.dayOfYear().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().days(), test.dayOfYear().getDurationField());
        assertEquals(test.getChronology().years(), test.dayOfYear().getRangeDurationField());
        assertEquals(3, test.dayOfYear().getMaximumTextLength(null));
        assertEquals(3, test.dayOfYear().getMaximumShortTextLength(null));
        assertEquals(false, test.dayOfYear().isLeap());
        assertEquals(0, test.dayOfYear().getLeapAmount());
        assertEquals(null, test.dayOfYear().getLeapDurationField());
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetMaxMinValuesDayOfYear
    public void testPropertyGetMaxMinValuesDayOfYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertEquals(1, test.dayOfYear().getMinimumValue());
        assertEquals(1, test.dayOfYear().getMinimumValueOverall());
        assertEquals(366, test.dayOfYear().getMaximumValue());
        assertEquals(366, test.dayOfYear().getMaximumValueOverall());
        test = new DateTime(2002, 6, 9, 0, 0, 0, 0);
        assertEquals(365, test.dayOfYear().getMaximumValue());
        assertEquals(366, test.dayOfYear().getMaximumValueOverall());
    }

// org.joda.time.TestDateTime_Properties::testPropertyAddDayOfYear
    public void testPropertyAddDayOfYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfYear().addToCopy(9);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-18T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfYear().addToCopy(21);
        assertEquals("2004-06-30T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfYear().addToCopy(22);
        assertEquals("2004-07-01T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfYear().addToCopy(21 + 31 + 31 + 30 + 31 + 30 + 31);
        assertEquals("2004-12-31T00:00:00.000Z", copy.toString());
        
        copy = test.dayOfYear().addToCopy(22 + 31 + 31 + 30 + 31 + 30 + 31);
        assertEquals("2005-01-01T00:00:00.000Z", copy.toString());
        
        copy = test.dayOfYear().addToCopy(-8);
        assertEquals("2004-06-01T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfYear().addToCopy(-9);
        assertEquals("2004-05-31T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfYear().addToCopy(-8 - 31 - 30 - 31 - 29 - 31);
        assertEquals("2004-01-01T00:00:00.000Z", copy.toString());
        
        copy = test.dayOfYear().addToCopy(-9 - 31 - 30 - 31 - 29 - 31);
        assertEquals("2003-12-31T00:00:00.000Z", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyAddWrapFieldDayOfYear
    public void testPropertyAddWrapFieldDayOfYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfYear().addWrapFieldToCopy(21);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-30T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfYear().addWrapFieldToCopy(22);
        assertEquals("2004-07-01T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfYear().addWrapFieldToCopy(-12);
        assertEquals("2004-05-28T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfYear().addWrapFieldToCopy(205);
        assertEquals("2004-12-31T00:00:00.000Z", copy.toString());
        
        copy = test.dayOfYear().addWrapFieldToCopy(206);
        assertEquals("2004-01-01T00:00:00.000Z", copy.toString());
        
        copy = test.dayOfYear().addWrapFieldToCopy(-160);
        assertEquals("2004-01-01T00:00:00.000Z", copy.toString());
        
        copy = test.dayOfYear().addWrapFieldToCopy(-161);
        assertEquals("2004-12-31T00:00:00.000Z", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertySetDayOfYear
    public void testPropertySetDayOfYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfYear().setCopy(12);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-01-12T00:00:00.000Z", copy.toString());
        
        try {
            test.dayOfYear().setCopy(367);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.dayOfYear().setCopy(0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Properties::testPropertySetTextDayOfYear
    public void testPropertySetTextDayOfYear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfYear().setCopy("12");
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-01-12T00:00:00.000Z", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyCompareToDayOfYear
    public void testPropertyCompareToDayOfYear() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.dayOfYear().compareTo(test2) < 0);
        assertEquals(true, test2.dayOfYear().compareTo(test1) > 0);
        assertEquals(true, test1.dayOfYear().compareTo(test1) == 0);
        try {
            test1.dayOfYear().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.dayOfYear().compareTo(dt2) < 0);
        assertEquals(true, test2.dayOfYear().compareTo(dt1) > 0);
        assertEquals(true, test1.dayOfYear().compareTo(dt1) == 0);
        try {
            test1.dayOfYear().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetWeekOfWeekyear
    public void testPropertyGetWeekOfWeekyear() {
        
        
        
        
        
        
        
        
        
        
        
        
        
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().weekOfWeekyear(), test.weekOfWeekyear().getField());
        assertEquals("weekOfWeekyear", test.weekOfWeekyear().getName());
        assertEquals("Property[weekOfWeekyear]", test.weekOfWeekyear().toString());
        assertSame(test, test.weekOfWeekyear().getDateTime());
        assertEquals(24, test.weekOfWeekyear().get());
        assertEquals("24", test.weekOfWeekyear().getAsString());
        assertEquals("24", test.weekOfWeekyear().getAsText());
        assertEquals("24", test.weekOfWeekyear().getAsText(Locale.FRENCH));
        assertEquals("24", test.weekOfWeekyear().getAsShortText());
        assertEquals("24", test.weekOfWeekyear().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().weeks(), test.weekOfWeekyear().getDurationField());
        assertEquals(test.getChronology().weekyears(), test.weekOfWeekyear().getRangeDurationField());
        assertEquals(2, test.weekOfWeekyear().getMaximumTextLength(null));
        assertEquals(2, test.weekOfWeekyear().getMaximumShortTextLength(null));
        assertEquals(false, test.weekOfWeekyear().isLeap());
        assertEquals(0, test.weekOfWeekyear().getLeapAmount());
        assertEquals(null, test.weekOfWeekyear().getLeapDurationField());
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetMaxMinValuesWeekOfWeekyear
    public void testPropertyGetMaxMinValuesWeekOfWeekyear() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertEquals(1, test.weekOfWeekyear().getMinimumValue());
        assertEquals(1, test.weekOfWeekyear().getMinimumValueOverall());
        assertEquals(53, test.weekOfWeekyear().getMaximumValue());
        assertEquals(53, test.weekOfWeekyear().getMaximumValueOverall());
        test = new DateTime(2005, 6, 9, 0, 0, 0, 0);
        assertEquals(52, test.weekOfWeekyear().getMaximumValue());
        assertEquals(53, test.weekOfWeekyear().getMaximumValueOverall());
    }

// org.joda.time.TestDateTime_Properties::testPropertyAddWeekOfWeekyear
    public void testPropertyAddWeekOfWeekyear() {
        DateTime test = new DateTime(2004, 6, 7, 0, 0, 0, 0);
        DateTime copy = test.weekOfWeekyear().addToCopy(1);
        assertEquals("2004-06-07T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-14T00:00:00.000+01:00", copy.toString());
        
        copy = test.weekOfWeekyear().addToCopy(29);
        assertEquals("2004-12-27T00:00:00.000Z", copy.toString());
        
        copy = test.weekOfWeekyear().addToCopy(30);
        assertEquals("2005-01-03T00:00:00.000Z", copy.toString());
        
        copy = test.weekOfWeekyear().addToCopy(-22);
        assertEquals("2004-01-05T00:00:00.000Z", copy.toString());
        
        copy = test.weekOfWeekyear().addToCopy(-23);
        assertEquals("2003-12-29T00:00:00.000Z", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyAddWrapFieldWeekOfWeekyear
    public void testPropertyAddWrapFieldWeekOfWeekyear() {
        DateTime test = new DateTime(2004, 6, 7, 0, 0, 0, 0);
        DateTime copy = test.weekOfWeekyear().addWrapFieldToCopy(1);
        assertEquals("2004-06-07T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-14T00:00:00.000+01:00", copy.toString());
        
        copy = test.weekOfWeekyear().addWrapFieldToCopy(29);
        assertEquals("2004-12-27T00:00:00.000Z", copy.toString());
        
        copy = test.weekOfWeekyear().addWrapFieldToCopy(30);
        assertEquals("2003-12-29T00:00:00.000Z", copy.toString());
        
        copy = test.weekOfWeekyear().addWrapFieldToCopy(-23);
        assertEquals("2003-12-29T00:00:00.000Z", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertySetWeekOfWeekyear
    public void testPropertySetWeekOfWeekyear() {
        DateTime test = new DateTime(2004, 6, 7, 0, 0, 0, 0);
        DateTime copy = test.weekOfWeekyear().setCopy(4);
        assertEquals("2004-06-07T00:00:00.000+01:00", test.toString());
        assertEquals("2004-01-19T00:00:00.000Z", copy.toString());
        
        try {
            test.weekOfWeekyear().setCopy(54);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.weekOfWeekyear().setCopy(0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Properties::testPropertySetTextWeekOfWeekyear
    public void testPropertySetTextWeekOfWeekyear() {
        DateTime test = new DateTime(2004, 6, 7, 0, 0, 0, 0);
        DateTime copy = test.weekOfWeekyear().setCopy("4");
        assertEquals("2004-06-07T00:00:00.000+01:00", test.toString());
        assertEquals("2004-01-19T00:00:00.000Z", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyCompareToWeekOfWeekyear
    public void testPropertyCompareToWeekOfWeekyear() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.weekOfWeekyear().compareTo(test2) < 0);
        assertEquals(true, test2.weekOfWeekyear().compareTo(test1) > 0);
        assertEquals(true, test1.weekOfWeekyear().compareTo(test1) == 0);
        try {
            test1.weekOfWeekyear().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test1.weekOfWeekyear().compareTo(dt2) < 0);
        assertEquals(true, test2.weekOfWeekyear().compareTo(dt1) > 0);
        assertEquals(true, test1.weekOfWeekyear().compareTo(dt1) == 0);
        try {
            test1.weekOfWeekyear().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetDayOfWeek
    public void testPropertyGetDayOfWeek() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        assertSame(test.getChronology().dayOfWeek(), test.dayOfWeek().getField());
        assertEquals("dayOfWeek", test.dayOfWeek().getName());
        assertEquals("Property[dayOfWeek]", test.dayOfWeek().toString());
        assertSame(test, test.dayOfWeek().getDateTime());
        assertEquals(3, test.dayOfWeek().get());
        assertEquals("3", test.dayOfWeek().getAsString());
        assertEquals("Wednesday", test.dayOfWeek().getAsText());
        assertEquals("Wednesday", test.dayOfWeek().getField().getAsText(3, Locale.ENGLISH));
        assertEquals("mercredi", test.dayOfWeek().getAsText(Locale.FRENCH));
        assertEquals("mercredi", test.dayOfWeek().getField().getAsText(3, Locale.FRENCH));
        assertEquals("Wed", test.dayOfWeek().getAsShortText());
        assertEquals("Wed", test.dayOfWeek().getField().getAsShortText(3, Locale.ENGLISH));
        assertEquals("mer.", test.dayOfWeek().getAsShortText(Locale.FRENCH));
        assertEquals("mer.", test.dayOfWeek().getField().getAsShortText(3, Locale.FRENCH));
        assertEquals(test.getChronology().days(), test.dayOfWeek().getDurationField());
        assertEquals(test.getChronology().weeks(), test.dayOfWeek().getRangeDurationField());
        assertEquals(9, test.dayOfWeek().getMaximumTextLength(null));
        assertEquals(8, test.dayOfWeek().getMaximumTextLength(Locale.FRENCH));
        assertEquals(3, test.dayOfWeek().getMaximumShortTextLength(null));
        assertEquals(4, test.dayOfWeek().getMaximumShortTextLength(Locale.FRENCH));
        assertEquals(1, test.dayOfWeek().getMinimumValue());
        assertEquals(1, test.dayOfWeek().getMinimumValueOverall());
        assertEquals(7, test.dayOfWeek().getMaximumValue());
        assertEquals(7, test.dayOfWeek().getMaximumValueOverall());
        assertEquals(false, test.dayOfWeek().isLeap());
        assertEquals(0, test.dayOfWeek().getLeapAmount());
        assertEquals(null, test.dayOfWeek().getLeapDurationField());
    }

// org.joda.time.TestDateTime_Properties::testPropertyAddDayOfWeek
    public void testPropertyAddDayOfWeek() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfWeek().addToCopy(1);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-10T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfWeek().addToCopy(21);
        assertEquals("2004-06-30T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfWeek().addToCopy(22);
        assertEquals("2004-07-01T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfWeek().addToCopy(21 + 31 + 31 + 30 + 31 + 30 + 31);
        assertEquals("2004-12-31T00:00:00.000Z", copy.toString());
        
        copy = test.dayOfWeek().addToCopy(22 + 31 + 31 + 30 + 31 + 30 + 31);
        assertEquals("2005-01-01T00:00:00.000Z", copy.toString());
        
        copy = test.dayOfWeek().addToCopy(-8);
        assertEquals("2004-06-01T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfWeek().addToCopy(-9);
        assertEquals("2004-05-31T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfWeek().addToCopy(-8 - 31 - 30 - 31 - 29 - 31);
        assertEquals("2004-01-01T00:00:00.000Z", copy.toString());
        
        copy = test.dayOfWeek().addToCopy(-9 - 31 - 30 - 31 - 29 - 31);
        assertEquals("2003-12-31T00:00:00.000Z", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyAddLongDayOfWeek
    public void testPropertyAddLongDayOfWeek() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfWeek().addToCopy(1L);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-10T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyAddWrapFieldDayOfWeek
    public void testPropertyAddWrapFieldDayOfWeek() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);  
        DateTime copy = test.dayOfWeek().addWrapFieldToCopy(1);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-10T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfWeek().addWrapFieldToCopy(5);
        assertEquals("2004-06-07T00:00:00.000+01:00", copy.toString());
        
        copy = test.dayOfWeek().addWrapFieldToCopy(-10);
        assertEquals("2004-06-13T00:00:00.000+01:00", copy.toString());
        
        test = new DateTime(2004, 6, 2, 0, 0, 0, 0);
        copy = test.dayOfWeek().addWrapFieldToCopy(5);
        assertEquals("2004-06-02T00:00:00.000+01:00", test.toString());
        assertEquals("2004-05-31T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertySetDayOfWeek
    public void testPropertySetDayOfWeek() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfWeek().setCopy(4);
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-10T00:00:00.000+01:00", copy.toString());
        
        try {
            test.dayOfWeek().setCopy(8);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            test.dayOfWeek().setCopy(0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Properties::testPropertySetTextDayOfWeek
    public void testPropertySetTextDayOfWeek() {
        DateTime test = new DateTime(2004, 6, 9, 0, 0, 0, 0);
        DateTime copy = test.dayOfWeek().setCopy("4");
        assertEquals("2004-06-09T00:00:00.000+01:00", test.toString());
        assertEquals("2004-06-10T00:00:00.000+01:00", copy.toString());
        copy = test.dayOfWeek().setCopy("Mon");
        assertEquals("2004-06-07T00:00:00.000+01:00", copy.toString());
        copy = test.dayOfWeek().setCopy("Tuesday");
        assertEquals("2004-06-08T00:00:00.000+01:00", copy.toString());
        copy = test.dayOfWeek().setCopy("lundi", Locale.FRENCH);
        assertEquals("2004-06-07T00:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyCompareToDayOfWeek
    public void testPropertyCompareToDayOfWeek() {
        DateTime test1 = new DateTime(TEST_TIME1);
        DateTime test2 = new DateTime(TEST_TIME2);
        assertEquals(true, test2.dayOfWeek().compareTo(test1) < 0);
        assertEquals(true, test1.dayOfWeek().compareTo(test2) > 0);
        assertEquals(true, test1.dayOfWeek().compareTo(test1) == 0);
        try {
            test1.dayOfWeek().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        
        DateTime dt1 = new DateTime(TEST_TIME1);
        DateTime dt2 = new DateTime(TEST_TIME2);
        assertEquals(true, test2.dayOfWeek().compareTo(dt1) < 0);
        assertEquals(true, test1.dayOfWeek().compareTo(dt2) > 0);
        assertEquals(true, test1.dayOfWeek().compareTo(dt1) == 0);
        try {
            test1.dayOfWeek().compareTo((ReadableInstant) null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetHourOfDay
    public void testPropertyGetHourOfDay() {
        DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().hourOfDay(), test.hourOfDay().getField());
        assertEquals("hourOfDay", test.hourOfDay().getName());
        assertEquals("Property[hourOfDay]", test.hourOfDay().toString());
        assertSame(test, test.hourOfDay().getDateTime());
        assertEquals(13, test.hourOfDay().get());
        assertEquals("13", test.hourOfDay().getAsString());
        assertEquals("13", test.hourOfDay().getAsText());
        assertEquals("13", test.hourOfDay().getAsText(Locale.FRENCH));
        assertEquals("13", test.hourOfDay().getAsShortText());
        assertEquals("13", test.hourOfDay().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().hours(), test.hourOfDay().getDurationField());
        assertEquals(test.getChronology().days(), test.hourOfDay().getRangeDurationField());
        assertEquals(2, test.hourOfDay().getMaximumTextLength(null));
        assertEquals(2, test.hourOfDay().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetDifferenceHourOfDay
    public void testPropertyGetDifferenceHourOfDay() {
        DateTime test1 = new DateTime(2004, 6, 9, 13, 30, 0, 0);
        DateTime test2 = new DateTime(2004, 6, 9, 15, 30, 0, 0);
        assertEquals(-2, test1.hourOfDay().getDifference(test2));
        assertEquals(2, test2.hourOfDay().getDifference(test1));
        assertEquals(-2L, test1.hourOfDay().getDifferenceAsLong(test2));
        assertEquals(2L, test2.hourOfDay().getDifferenceAsLong(test1));
        
        DateTime test = new DateTime(TEST_TIME_NOW + (13L * DateTimeConstants.MILLIS_PER_HOUR));
        assertEquals(13, test.hourOfDay().getDifference(null));
        assertEquals(13L, test.hourOfDay().getDifferenceAsLong(null));
    }

// org.joda.time.TestDateTime_Properties::testPropertyRoundFloorHourOfDay
    public void testPropertyRoundFloorHourOfDay() {
        DateTime test = new DateTime(2004, 6, 9, 13, 30, 0, 0);
        DateTime copy = test.hourOfDay().roundFloorCopy();
        assertEquals("2004-06-09T13:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyRoundCeilingHourOfDay
    public void testPropertyRoundCeilingHourOfDay() {
        DateTime test = new DateTime(2004, 6, 9, 13, 30, 0, 0);
        DateTime copy = test.hourOfDay().roundCeilingCopy();
        assertEquals("2004-06-09T14:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyRoundHalfFloorHourOfDay
    public void testPropertyRoundHalfFloorHourOfDay() {
        DateTime test = new DateTime(2004, 6, 9, 13, 30, 0, 0);
        DateTime copy = test.hourOfDay().roundHalfFloorCopy();
        assertEquals("2004-06-09T13:00:00.000+01:00", copy.toString());
        
        test = new DateTime(2004, 6, 9, 13, 30, 0, 1);
        copy = test.hourOfDay().roundHalfFloorCopy();
        assertEquals("2004-06-09T14:00:00.000+01:00", copy.toString());
        
        test = new DateTime(2004, 6, 9, 13, 29, 59, 999);
        copy = test.hourOfDay().roundHalfFloorCopy();
        assertEquals("2004-06-09T13:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyRoundHalfCeilingHourOfDay
    public void testPropertyRoundHalfCeilingHourOfDay() {
        DateTime test = new DateTime(2004, 6, 9, 13, 30, 0, 0);
        DateTime copy = test.hourOfDay().roundHalfCeilingCopy();
        assertEquals("2004-06-09T14:00:00.000+01:00", copy.toString());
        
        test = new DateTime(2004, 6, 9, 13, 30, 0, 1);
        copy = test.hourOfDay().roundHalfCeilingCopy();
        assertEquals("2004-06-09T14:00:00.000+01:00", copy.toString());
        
        test = new DateTime(2004, 6, 9, 13, 29, 59, 999);
        copy = test.hourOfDay().roundHalfCeilingCopy();
        assertEquals("2004-06-09T13:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyRoundHalfEvenHourOfDay
    public void testPropertyRoundHalfEvenHourOfDay() {
        DateTime test = new DateTime(2004, 6, 9, 13, 30, 0, 0);
        DateTime copy = test.hourOfDay().roundHalfEvenCopy();
        assertEquals("2004-06-09T14:00:00.000+01:00", copy.toString());
        
        test = new DateTime(2004, 6, 9, 14, 30, 0, 0);
        copy = test.hourOfDay().roundHalfEvenCopy();
        assertEquals("2004-06-09T14:00:00.000+01:00", copy.toString());
        
        test = new DateTime(2004, 6, 9, 13, 30, 0, 1);
        copy = test.hourOfDay().roundHalfEvenCopy();
        assertEquals("2004-06-09T14:00:00.000+01:00", copy.toString());
        
        test = new DateTime(2004, 6, 9, 13, 29, 59, 999);
        copy = test.hourOfDay().roundHalfEvenCopy();
        assertEquals("2004-06-09T13:00:00.000+01:00", copy.toString());
    }

// org.joda.time.TestDateTime_Properties::testPropertyRemainderHourOfDay
    public void testPropertyRemainderHourOfDay() {
        DateTime test = new DateTime(2004, 6, 9, 13, 30, 0, 0);
        assertEquals(30L * DateTimeConstants.MILLIS_PER_MINUTE, test.hourOfDay().remainder());
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetMinuteOfHour
    public void testPropertyGetMinuteOfHour() {
        DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().minuteOfHour(), test.minuteOfHour().getField());
        assertEquals("minuteOfHour", test.minuteOfHour().getName());
        assertEquals("Property[minuteOfHour]", test.minuteOfHour().toString());
        assertSame(test, test.minuteOfHour().getDateTime());
        assertEquals(23, test.minuteOfHour().get());
        assertEquals("23", test.minuteOfHour().getAsString());
        assertEquals("23", test.minuteOfHour().getAsText());
        assertEquals("23", test.minuteOfHour().getAsText(Locale.FRENCH));
        assertEquals("23", test.minuteOfHour().getAsShortText());
        assertEquals("23", test.minuteOfHour().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().minutes(), test.minuteOfHour().getDurationField());
        assertEquals(test.getChronology().hours(), test.minuteOfHour().getRangeDurationField());
        assertEquals(2, test.minuteOfHour().getMaximumTextLength(null));
        assertEquals(2, test.minuteOfHour().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetMinuteOfDay
    public void testPropertyGetMinuteOfDay() {
        DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().minuteOfDay(), test.minuteOfDay().getField());
        assertEquals("minuteOfDay", test.minuteOfDay().getName());
        assertEquals("Property[minuteOfDay]", test.minuteOfDay().toString());
        assertSame(test, test.minuteOfDay().getDateTime());
        assertEquals(803, test.minuteOfDay().get());
        assertEquals("803", test.minuteOfDay().getAsString());
        assertEquals("803", test.minuteOfDay().getAsText());
        assertEquals("803", test.minuteOfDay().getAsText(Locale.FRENCH));
        assertEquals("803", test.minuteOfDay().getAsShortText());
        assertEquals("803", test.minuteOfDay().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().minutes(), test.minuteOfDay().getDurationField());
        assertEquals(test.getChronology().days(), test.minuteOfDay().getRangeDurationField());
        assertEquals(4, test.minuteOfDay().getMaximumTextLength(null));
        assertEquals(4, test.minuteOfDay().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetSecondOfMinute
    public void testPropertyGetSecondOfMinute() {
        DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().secondOfMinute(), test.secondOfMinute().getField());
        assertEquals("secondOfMinute", test.secondOfMinute().getName());
        assertEquals("Property[secondOfMinute]", test.secondOfMinute().toString());
        assertSame(test, test.secondOfMinute().getDateTime());
        assertEquals(43, test.secondOfMinute().get());
        assertEquals("43", test.secondOfMinute().getAsString());
        assertEquals("43", test.secondOfMinute().getAsText());
        assertEquals("43", test.secondOfMinute().getAsText(Locale.FRENCH));
        assertEquals("43", test.secondOfMinute().getAsShortText());
        assertEquals("43", test.secondOfMinute().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().seconds(), test.secondOfMinute().getDurationField());
        assertEquals(test.getChronology().minutes(), test.secondOfMinute().getRangeDurationField());
        assertEquals(2, test.secondOfMinute().getMaximumTextLength(null));
        assertEquals(2, test.secondOfMinute().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetSecondOfDay
    public void testPropertyGetSecondOfDay() {
        DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().secondOfDay(), test.secondOfDay().getField());
        assertEquals("secondOfDay", test.secondOfDay().getName());
        assertEquals("Property[secondOfDay]", test.secondOfDay().toString());
        assertSame(test, test.secondOfDay().getDateTime());
        assertEquals(48223, test.secondOfDay().get());
        assertEquals("48223", test.secondOfDay().getAsString());
        assertEquals("48223", test.secondOfDay().getAsText());
        assertEquals("48223", test.secondOfDay().getAsText(Locale.FRENCH));
        assertEquals("48223", test.secondOfDay().getAsShortText());
        assertEquals("48223", test.secondOfDay().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().seconds(), test.secondOfDay().getDurationField());
        assertEquals(test.getChronology().days(), test.secondOfDay().getRangeDurationField());
        assertEquals(5, test.secondOfDay().getMaximumTextLength(null));
        assertEquals(5, test.secondOfDay().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetMillisOfSecond
    public void testPropertyGetMillisOfSecond() {
        DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().millisOfSecond(), test.millisOfSecond().getField());
        assertEquals("millisOfSecond", test.millisOfSecond().getName());
        assertEquals("Property[millisOfSecond]", test.millisOfSecond().toString());
        assertSame(test, test.millisOfSecond().getDateTime());
        assertEquals(53, test.millisOfSecond().get());
        assertEquals("53", test.millisOfSecond().getAsString());
        assertEquals("53", test.millisOfSecond().getAsText());
        assertEquals("53", test.millisOfSecond().getAsText(Locale.FRENCH));
        assertEquals("53", test.millisOfSecond().getAsShortText());
        assertEquals("53", test.millisOfSecond().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().millis(), test.millisOfSecond().getDurationField());
        assertEquals(test.getChronology().seconds(), test.millisOfSecond().getRangeDurationField());
        assertEquals(3, test.millisOfSecond().getMaximumTextLength(null));
        assertEquals(3, test.millisOfSecond().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateTime_Properties::testPropertyGetMillisOfDay
    public void testPropertyGetMillisOfDay() {
        DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
        assertSame(test.getChronology().millisOfDay(), test.millisOfDay().getField());
        assertEquals("millisOfDay", test.millisOfDay().getName());
        assertEquals("Property[millisOfDay]", test.millisOfDay().toString());
        assertSame(test, test.millisOfDay().getDateTime());
        assertEquals(48223053, test.millisOfDay().get());
        assertEquals("48223053", test.millisOfDay().getAsString());
        assertEquals("48223053", test.millisOfDay().getAsText());
        assertEquals("48223053", test.millisOfDay().getAsText(Locale.FRENCH));
        assertEquals("48223053", test.millisOfDay().getAsShortText());
        assertEquals("48223053", test.millisOfDay().getAsShortText(Locale.FRENCH));
        assertEquals(test.getChronology().millis(), test.millisOfDay().getDurationField());
        assertEquals(test.getChronology().days(), test.millisOfDay().getRangeDurationField());
        assertEquals(8, test.millisOfDay().getMaximumTextLength(null));
        assertEquals(8, test.millisOfDay().getMaximumShortTextLength(null));
    }

// org.joda.time.TestDateTime_Properties::testPropertyToIntervalYearOfEra
    public void testPropertyToIntervalYearOfEra() {
      DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.yearOfEra().toInterval();
      assertEquals(new DateTime(2004, 1, 1, 0, 0, 0, 0), testInterval.getStart());
      assertEquals(new DateTime(2005, 1, 1, 0, 0, 0, 0), testInterval.getEnd());
    }

// org.joda.time.TestDateTime_Properties::testPropertyToIntervalYearOfCentury
    public void testPropertyToIntervalYearOfCentury() {
      DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.yearOfCentury().toInterval();
      assertEquals(new DateTime(2004, 1, 1, 0, 0, 0, 0), testInterval.getStart());
      assertEquals(new DateTime(2005, 1, 1, 0, 0, 0, 0), testInterval.getEnd());
    }

// org.joda.time.TestDateTime_Properties::testPropertyToIntervalYear
    public void testPropertyToIntervalYear() {
      DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.year().toInterval();
      assertEquals(new DateTime(2004, 1, 1, 0, 0, 0, 0), testInterval.getStart());
      assertEquals(new DateTime(2005, 1, 1, 0, 0, 0, 0), testInterval.getEnd());
    }

// org.joda.time.TestDateTime_Properties::testPropertyToIntervalMonthOfYear
    public void testPropertyToIntervalMonthOfYear() {
      DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.monthOfYear().toInterval();
      assertEquals(new DateTime(2004, 6, 1, 0, 0, 0, 0), testInterval.getStart());
      assertEquals(new DateTime(2004, 7, 1, 0, 0, 0, 0), testInterval.getEnd());
    }

// org.joda.time.TestDateTime_Properties::testPropertyToIntervalDayOfMonth
    public void testPropertyToIntervalDayOfMonth() {
      DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.dayOfMonth().toInterval();
      assertEquals(new DateTime(2004, 6, 9, 0, 0, 0, 0), testInterval.getStart());
      assertEquals(new DateTime(2004, 6, 10, 0, 0, 0, 0), testInterval.getEnd());

      DateTime febTest = new DateTime(2004, 2, 29, 13, 23, 43, 53);
      Interval febTestInterval = febTest.dayOfMonth().toInterval();
      assertEquals(new DateTime(2004, 2, 29, 0, 0, 0, 0), febTestInterval.getStart());
      assertEquals(new DateTime(2004, 3, 1, 0, 0, 0, 0), febTestInterval.getEnd());
    }

// org.joda.time.TestDateTime_Properties::testPropertyToIntervalHourOfDay
    public void testPropertyToIntervalHourOfDay() {
      DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.hourOfDay().toInterval();
      assertEquals(new DateTime(2004, 6, 9, 13, 0, 0, 0), testInterval.getStart());
      assertEquals(new DateTime(2004, 6, 9, 14, 0, 0, 0), testInterval.getEnd());

      DateTime midnightTest = new DateTime(2004, 6, 9, 23, 23, 43, 53);
      Interval midnightTestInterval = midnightTest.hourOfDay().toInterval();
      assertEquals(new DateTime(2004, 6, 9, 23, 0, 0, 0), midnightTestInterval.getStart());
      assertEquals(new DateTime(2004, 6, 10, 0, 0, 0, 0), midnightTestInterval.getEnd());
    }

// org.joda.time.TestDateTime_Properties::testPropertyToIntervalMinuteOfHour
    public void testPropertyToIntervalMinuteOfHour() {
      DateTime test = new DateTime(2004, 6, 9, 13, 23, 43, 53);
      Interval testInterval = test.minuteOfHour().toInterval();
      assertEquals(new DateTime(2004, 6, 9, 13, 23, 0, 0), testInterval.getStart());
      assertEquals(new DateTime(2004, 6, 9, 13, 24, 0, 0), testInterval.getEnd());
    }
