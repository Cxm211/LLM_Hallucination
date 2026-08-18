protected Date parseAsISO8601(String dateStr, ParsePosition pos, boolean throwErrors)
            throws ParseException
    {
        int len = dateStr.length();
        char c = dateStr.charAt(len-1);
        DateFormat df;
        String formatStr;

        // Need to support "plain" date...
        if (len <= 10 && Character.isDigit(c)) {
            df = _formatPlain;
            formatStr = DATE_FORMAT_STR_PLAIN;
            if (df == null) {
                df = _formatPlain = _cloneFormat(DATE_FORMAT_PLAIN, formatStr,
                        _timezone, _locale, _lenient);
            }
        } else if (c == 'Z') {
            df = _formatISO8601_z;
            formatStr = DATE_FORMAT_STR_ISO8601_Z;
            if (df == null) {
                df = _formatISO8601_z = _cloneFormat(DATE_FORMAT_ISO8601_Z, formatStr,
                        DEFAULT_TIMEZONE, _locale, _lenient);
            }
            // may be missing milliseconds... if so, add
            if (dateStr.charAt(len-4) == ':') {
                StringBuilder sb = new StringBuilder(dateStr);
                sb.insert(len-1, ".000");
                dateStr = sb.toString();
            }
        } else {
            // Let's see if we have timezone indicator or not...
            if (hasTimeZone(dateStr)) {
                c = dateStr.charAt(len-3);
                if (c == ':') { // remove optional colon
                    StringBuilder sb = new StringBuilder(dateStr);
                    sb.delete(len-3, len-2);
                    dateStr = sb.toString();
                } else if (c == '+' || c == '-') { // missing minutes
                    dateStr += "00";
                }
                // Milliseconds partial or missing; and even seconds are optional
                len = dateStr.length();
                // remove 'T', '+'/'-' and 4-digit timezone-offset
                int timeLen = len - dateStr.lastIndexOf('T') - 6;
                if (timeLen < 12) { // 8 for hh:mm:ss, 4 for .sss
                    int offset = len - 5; // insertion offset, before tz-offset
                    StringBuilder sb = new StringBuilder(dateStr);
                    switch (timeLen) {
                    case 11:
                        sb.insert(offset, '0'); break;
                    case 10:
                        sb.insert(offset, "00"); break;
                    case 9: // is this legal? (just second fraction marker)
                        sb.insert(offset, "000"); break;
                    case 8:
                        sb.insert(offset, ".000"); break;
                    case 7: // not legal to have single-digit second
                        break;
                    case 6: // probably not legal, but let's allow
                        sb.insert(offset, "00.000");
                    case 5: // is legal to omit seconds
                        sb.insert(offset, ":00.000");
                    }
                    dateStr = sb.toString();
                }
                df = _formatISO8601;
                formatStr = DATE_FORMAT_STR_ISO8601;
                if (_formatISO8601 == null) {
                    df = _formatISO8601 = _cloneFormat(DATE_FORMAT_ISO8601, formatStr,
                            _timezone, _locale, _lenient);
                }
            } else {
                // If not, plain date-time without timezone: use configured timezone
                StringBuilder sb = new StringBuilder(dateStr);
                int timeLen = len - dateStr.lastIndexOf('T') - 1;
                // And possibly also millisecond part if missing
                if (timeLen < 12) { // missing, or partial
                    switch (timeLen) {
                    case 11: sb.append('0');
                    case 10: sb.append('0');
                    case 9: sb.append('0');
                        break;
                    default:
                        sb.append(".000");
                    }
                }
                dateStr = sb.toString();
                df = _formatISO8601;
                formatStr = DATE_FORMAT_STR_ISO8601;
                if (df == null) {
                    df = _formatISO8601 = _cloneFormat(DATE_FORMAT_ISO8601, formatStr,
                            _timezone, _locale, _lenient);
                }
            }
        }
        Date dt = df.parse(dateStr, pos);
        if (dt == null) {
            throw new ParseException
            (String.format("Can not parse date \"%s\": while it seems to fit format '%s', parsing fails (leniency? %s)",
                           dateStr, formatStr, _lenient),
               pos.getErrorIndex());
        }
        return dt;
    }