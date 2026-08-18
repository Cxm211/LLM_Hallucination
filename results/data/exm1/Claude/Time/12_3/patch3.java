public static LocalDateTime fromDateFields(Date date) {
    if (date == null) {
        throw new IllegalArgumentException("The date must not be null");
    }
    int year = date.getYear() + 1900;
    if (year <= 0) {
        year = year - 1;
    }
    return new LocalDateTime(
        year,
        date.getMonth() + 1,
        date.getDate(),
        date.getHours(),
        date.getMinutes(),
        date.getSeconds(),
        (((int) (date.getTime() % 1000)) + 1000) % 1000
    );
}