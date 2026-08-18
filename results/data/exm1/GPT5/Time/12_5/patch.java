public static LocalDate fromCalendarFields(Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("The calendar must not be null");
        }
        int yearOfEra = calendar.get(Calendar.YEAR);
        int era = calendar.get(Calendar.ERA);
        int year = (era == GregorianCalendar.BC) ? (1 - yearOfEra) : yearOfEra;
        return new LocalDate(
            year,
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        );
    }