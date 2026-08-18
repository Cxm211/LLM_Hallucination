public static LocalDate fromCalendarFields(Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("The calendar must not be null");
        }
        int year = calendar.get(Calendar.YEAR);
        int era = calendar.get(Calendar.ERA);
        // Adjust for BC era to use astronomical year numbering (1 BC = year 0)
        if (era == GregorianCalendar.BC) {
            year = 1 - year;
        }
        return new LocalDate(
            year,
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        );
    }