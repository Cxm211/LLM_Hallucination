public static LocalDateTime fromCalendarFields(Calendar calendar) {
    if (calendar == null) {
        throw new IllegalArgumentException("The calendar must not be null");
    }
    int era = calendar.get(Calendar.ERA);
    int yearOfEra = calendar.get(Calendar.YEAR);
    int actualYear = (era == GregorianCalendar.BC) ? 1 - yearOfEra : yearOfEra;
    return new LocalDateTime(
        actualYear,
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        calendar.get(Calendar.SECOND),
        calendar.get(Calendar.MILLISECOND)
    );
}