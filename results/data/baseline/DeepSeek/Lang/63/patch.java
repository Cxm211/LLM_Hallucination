public static String formatPeriod(long startMillis, long endMillis, String format, boolean padWithZeros, 
            TimeZone timezone) {

        long millis = endMillis - startMillis;
        if (millis < 28 * DateUtils.MILLIS_PER_DAY) {
            return formatDuration(millis, format, padWithZeros);
        }

        Token[] tokens = lexx(format);

        // timezones get funky around 0, so normalizing everything to GMT 
        // stops the hours being off
        Calendar start = Calendar.getInstance(timezone);
        start.setTime(new Date(startMillis));
        Calendar end = Calendar.getInstance(timezone);
        end.setTime(new Date(endMillis));

        // Clone the calendars to avoid side effects in reduceAndCorrect
        Calendar startClone = (Calendar) start.clone();
        Calendar endClone = (Calendar) end.clone();

        // initial estimates
        int milliseconds = endClone.get(Calendar.MILLISECOND) - startClone.get(Calendar.MILLISECOND);
        int seconds = endClone.get(Calendar.SECOND) - startClone.get(Calendar.SECOND);
        int minutes = endClone.get(Calendar.MINUTE) - startClone.get(Calendar.MINUTE);
        int hours = endClone.get(Calendar.HOUR_OF_DAY) - startClone.get(Calendar.HOUR_OF_DAY);
        int days = endClone.get(Calendar.DAY_OF_MONTH) - startClone.get(Calendar.DAY_OF_MONTH);
        int months = endClone.get(Calendar.MONTH) - startClone.get(Calendar.MONTH);
        int years = endClone.get(Calendar.YEAR) - startClone.get(Calendar.YEAR);

        // each initial estimate is adjusted in case it is under 0
        while (milliseconds < 0) {
            milliseconds += 1000;
            seconds -= 1;
        }
        while (seconds < 0) {
            seconds += 60;
            minutes -= 1;
        }
        while (minutes < 0) {
            minutes += 60;
            hours -= 1;
        }
        while (hours < 0) {
            hours += 24;
            days -= 1;
        }
        while (days < 0) {
            days += 31;
//days += 31; // TODO: Need tests to show this is bad and the new code is good.
// HEN: It's a tricky subject. Jan 15th to March 10th. If I count days-first it is 
// 1 month and 26 days, but if I count month-first then it is 1 month and 23 days.
// Also it's contextual - if asked for no M in the format then I should probably 
// be doing no calculating here.
            months -= 1;
        }
        while (months < 0) {
            months += 12;
            years -= 1;
        }
        milliseconds -= reduceAndCorrect(startClone, endClone, Calendar.MILLISECOND, milliseconds);
        seconds -= reduceAndCorrect(startClone, endClone, Calendar.SECOND, seconds);
        minutes -= reduceAndCorrect(startClone, endClone, Calendar.MINUTE, minutes);
        hours -= reduceAndCorrect(startClone, endClone, Calendar.HOUR_OF_DAY, hours);
        days -= reduceAndCorrect(startClone, endClone, Calendar.DAY_OF_MONTH, days);
        months -= reduceAndCorrect(startClone, endClone, Calendar.MONTH, months);
        years -= reduceAndCorrect(startClone, endClone, Calendar.YEAR, years);

        // This next block of code adds in values that 
        // aren't requested. This allows the user to ask for the 
        // number of months and get the real count and not just 0->11.
        if (!Token.containsTokenWithValue(tokens, y)) {
            if (Token.containsTokenWithValue(tokens, M)) {
                months += 12 * years;
                years = 0;
            } else {
                // TODO: this is a bit weak, needs work to know about leap years
                days += 365 * years;
                years = 0;
            }
        }
        if (!Token.containsTokenWithValue(tokens, M)) {
            days += endClone.get(Calendar.DAY_OF_YEAR) - startClone.get(Calendar.DAY_OF_YEAR);
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
