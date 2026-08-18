public static LocalDate fromCalendarFields(Calendar calendar) {
    if (calendar == null) {
        throw new IllegalArgumentException("The calendar must not be null");
    }
    int era = calendar.get(Calendar.ERA);
    int yearOfEra = calendar.get(Calendar.YEAR);
    int year = (era == 0) ? 1 - yearOfEra : yearOfEra;
    return new LocalDate(
        year,
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    );
}