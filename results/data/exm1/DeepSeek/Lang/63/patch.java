public static String formatPeriod(long startMillis, long endMillis, String format, boolean padWithZeros, TimeZone timezone) {
    long millis = endMillis - startMillis;
    if (millis < 28 * DateUtils.MILLIS_PER_DAY) {
        return formatDuration(millis, format, padWithZeros);
    }
    Token[] tokens = lexx(format);
    Calendar start = Calendar.getInstance(timezone);
    start.setTime(new Date(startMillis));
    Calendar end = Calendar.getInstance(timezone);
    end.setTime(new Date(endMillis));

    int years = reduceAndCorrect(start, end, Calendar.YEAR, 0);
    start.add(Calendar.YEAR, years);
    int months = reduceAndCorrect(start, end, Calendar.MONTH, 0);
    start.add(Calendar.MONTH, months);
    int days = reduceAndCorrect(start, end, Calendar.DAY_OF_MONTH, 0);
    start.add(Calendar.DAY_OF_MONTH, days);
    int hours = reduceAndCorrect(start, end, Calendar.HOUR_OF_DAY, 0);
    start.add(Calendar.HOUR_OF_DAY, hours);
    int minutes = reduceAndCorrect(start, end, Calendar.MINUTE, 0);
    start.add(Calendar.MINUTE, minutes);
    int seconds = reduceAndCorrect(start, end, Calendar.SECOND, 0);
    start.add(Calendar.SECOND, seconds);
    int milliseconds = reduceAndCorrect(start, end, Calendar.MILLISECOND, 0);

    if (!Token.containsTokenWithValue(tokens, y)) {
        if (Token.containsTokenWithValue(tokens, M)) {
            months += 12 * years;
            years = 0;
        } else {
            days += 365 * years;
            years = 0;
        }
    }
    if (!Token.containsTokenWithValue(tokens, M)) {
        days += end.get(Calendar.DAY_OF_YEAR) - start.get(Calendar.DAY_OF_YEAR);
        months = 0;
    }
    if (!Token.containsTokenWithValue(tokens, d)) {
        hours += 24 * days;
        days = 0;
    }
    if (!Token.containsTokenWithValue(tokens, H)) {
        minutes += 60 * hours;
        hours = 0;
    }
    if (!Token.containsTokenWithValue(tokens, m)) {
        seconds += 60 * minutes;
        minutes = 0;
    }
    if (!Token.containsTokenWithValue(tokens, s)) {
        milliseconds += 1000 * seconds;
        seconds = 0;
    }
    return format(tokens, years, months, days, hours, minutes, seconds, milliseconds, padWithZeros);
}