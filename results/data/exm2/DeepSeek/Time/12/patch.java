    public static LocalDate fromCalendarFields(Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("The calendar must not be null");
        }
        int year = calendar.get(Calendar.YEAR);
        if (calendar.get(Calendar.ERA) == Calendar.BC) {
            year = 1 - year;
        }
        return new LocalDate(
            year,
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        );
    }