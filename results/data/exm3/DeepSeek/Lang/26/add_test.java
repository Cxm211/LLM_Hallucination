// org/apache/commons/lang3/time/FastDateFormatTest.java
public void testWeekNumbersVariousLocales() {
        Calendar cal = Calendar.getInstance();
        cal.set(2010, Calendar.JANUARY, 1, 12, 0, 0);
        Date d1 = cal.getTime();
        cal.set(2010, Calendar.JANUARY, 3, 12, 0, 0);
        Date d2 = cal.getTime();
        
        FastDateFormat fdf_sv = FastDateFormat.getInstance("EEEE', week 'ww", new Locale("sv", "SE"));
        SimpleDateFormat sdf_sv = new SimpleDateFormat("EEEE', week 'ww", new Locale("sv", "SE"));
        assertEquals(sdf_sv.format(d1), fdf_sv.format(d1));
        assertEquals(sdf_sv.format(d2), fdf_sv.format(d2));
        
        FastDateFormat fdf_us = FastDateFormat.getInstance("EEEE', week 'ww", Locale.US);
        SimpleDateFormat sdf_us = new SimpleDateFormat("EEEE', week 'ww", Locale.US);
        assertEquals(sdf_us.format(d1), fdf_us.format(d1));
        assertEquals(sdf_us.format(d2), fdf_us.format(d2));
        
        FastDateFormat fdf_uk = FastDateFormat.getInstance("EEEE', week 'ww", Locale.UK);
        SimpleDateFormat sdf_uk = new SimpleDateFormat("EEEE', week 'ww", Locale.UK);
        assertEquals(sdf_uk.format(d1), fdf_uk.format(d1));
        assertEquals(sdf_uk.format(d2), fdf_uk.format(d2));
    }
