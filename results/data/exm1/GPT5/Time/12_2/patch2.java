public static LocalDateTime fromCalendarFields(Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("The calendar must not be null");
        }
        int year = calendar.get(Calendar.YEAR);
        int era = calendar.get(Calendar.ERA);
        // Adjust for BC era to use astronomical year numbering (1 BC = year 0)
        if (era == GregorianCalendar.BC) {
            year = 1 - year;
        }
        return new LocalDateTime(
            year,
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND),
            calendar.get(Calendar.MILLISECOND)
        );
    }