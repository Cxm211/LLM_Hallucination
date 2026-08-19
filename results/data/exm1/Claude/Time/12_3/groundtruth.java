// ===== FIXED org.joda.time.LocalDate :: fromCalendarFields(Calendar) [lines 206-217] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-12-fixed/src/main/java/org/joda/time/LocalDate.java =====
    public static LocalDate fromCalendarFields(Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("The calendar must not be null");
        }
        int era = calendar.get(Calendar.ERA);
        int yearOfEra = calendar.get(Calendar.YEAR);
        return new LocalDate(
            (era == GregorianCalendar.AD ? yearOfEra : 1 - yearOfEra),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

// ===== FIXED org.joda.time.LocalDate :: fromDateFields(Date) [lines 240-255] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-12-fixed/src/main/java/org/joda/time/LocalDate.java =====
    public static LocalDate fromDateFields(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        if (date.getTime() < 0) {
            // handle years in era BC
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(date);
            return fromCalendarFields(cal);
        }
        return new LocalDate(
            date.getYear() + 1900,
            date.getMonth() + 1,
            date.getDate()
        );
    }

// ===== FIXED org.joda.time.LocalDateTime :: fromCalendarFields(Calendar) [lines 195-210] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-12-fixed/src/main/java/org/joda/time/LocalDateTime.java =====
    public static LocalDateTime fromCalendarFields(Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("The calendar must not be null");
        }
        int era = calendar.get(Calendar.ERA);
        int yearOfEra = calendar.get(Calendar.YEAR);
        return new LocalDateTime(
            (era == GregorianCalendar.AD ? yearOfEra : 1 - yearOfEra),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND),
            calendar.get(Calendar.MILLISECOND)
        );
    }

// ===== FIXED org.joda.time.LocalDateTime :: fromDateFields(Date) [lines 233-252] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-12-fixed/src/main/java/org/joda/time/LocalDateTime.java =====
    public static LocalDateTime fromDateFields(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        if (date.getTime() < 0) {
            // handle years in era BC
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(date);
            return fromCalendarFields(cal);
        }
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
