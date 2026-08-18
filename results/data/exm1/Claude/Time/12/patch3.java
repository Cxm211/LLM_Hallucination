public static LocalDateTime fromDateFields(Date date) {
    if (date == null) {
        throw new IllegalArgumentException("The date must not be null");
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