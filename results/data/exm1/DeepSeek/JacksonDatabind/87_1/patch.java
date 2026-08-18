protected Date parseAsISO8601(String dateStr, ParsePosition pos, boolean throwErrors) throws ParseException {
        int len = dateStr.length();
        char c = dateStr.charAt(len-1);
        DateFormat df;
        String formatStr;
        if (len <= 10 && Character.isDigit(c)) {
            df = _formatPlain;
            formatStr = DATE_FORMAT_STR_PLAIN;
            if (df == null) {
                df = _formatPlain = _cloneFormat(DATE_FORMAT_PLAIN, formatStr, _timezone, _locale, _lenient);
            }
        } else if (c == 'Z') {
            df = _formatISO8601_z;
            formatStr = DATE_FORMAT_STR_ISO8601_Z;
            if (df == null) {
                df = _formatISO8601_z = _cloneFormat(DATE_FORMAT_ISO8601_Z, formatStr, DEFAULT_TIMEZONE, _locale, _lenient);
            }
            if (dateStr.charAt(len-4) == ':') {
                StringBuilder sb = new StringBuilder(dateStr);
                sb.insert(len-1, ".000");
                dateStr = sb.toString();
            }
        } else {
            if (hasTimeZone(dateStr)) {
                c = dateStr.charAt(len-3);
                if (c == ':') {
                    StringBuilder sb = new StringBuilder(dateStr);
                    sb.delete(len-3, len-2);
                    dateStr = sb.toString();
                } else if (c == '+' || c == '-') {
                    dateStr += "00";
                }
                len = dateStr.length();
                int timeLen = len - dateStr.lastIndexOf('T') - 6;
                if (timeLen < 12) {
                    int offset = len - 5;
                    StringBuilder sb = new StringBuilder(dateStr);
                    switch (timeLen) {
                    case 11:
                        sb.insert(offset, '0'); break;
                    case 10:
                        sb.insert(offset, "00"); break;
                    case 9:
                        sb.insert(offset, "000"); break;
                    case 8:
                        sb.insert(offset, ".000"); break;
                    case 7:
                        break;
                    case 6:
                        sb.insert(offset, "00.000"); break;
                    case 5:
                        sb.insert(offset, ":00.000");
                    }
                    dateStr = sb.toString();
                }
                df = _formatISO8601;
                formatStr = DATE_FORMAT_STR_ISO8601;
                if (_formatISO8601 == null) {
                    df = _formatISO8601 = _cloneFormat(DATE_FORMAT_ISO8601, formatStr, _timezone, _locale, _lenient);
                }
            } else {
                StringBuilder sb = new StringBuilder(dateStr);
                int timeLen = len - dateStr.lastIndexOf('T') - 1;
                if (timeLen < 12) {
                    switch (timeLen) {
                    case 11: sb.append('0');
                    case 10: sb.append('0');
                    case 9: sb.append('0');
                        break;
                    default:
                        sb.append(".000");
                    }
                }
                sb.append('Z');
                dateStr = sb.toString();
                df = _formatISO8601_z;
                formatStr = DATE_FORMAT_STR_ISO8601_Z;
                if (df == null) {
                    df = _formatISO8601_z = _cloneFormat(DATE_FORMAT_ISO8601_Z, formatStr, DEFAULT_TIMEZONE, _locale, _lenient);
                }
            }
        }
        Date dt = df.parse(dateStr, pos);
        if (dt == null) {
            throw new ParseException(String.format("Can not parse date \"%s\": while it seems to fit format '%s', parsing fails (leniency? %s)", dateStr, formatStr, _lenient), pos.getErrorIndex());
        }
        return dt;
    }