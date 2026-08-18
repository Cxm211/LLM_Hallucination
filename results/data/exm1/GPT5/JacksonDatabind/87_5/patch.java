protected Date parseAsISO8601(String dateStr, ParsePosition pos, boolean throwErrors)
            throws ParseException
    {
        int origStart = (pos == null) ? 0 : pos.getIndex();
        String tail = dateStr.substring(origStart);
        int i = 0, end = tail.length();
        while (i < end) {
            char ch = tail.charAt(i);
            if (Character.isDigit(ch) || ch == '-' || ch == ':' || ch == 'T'
                    || ch == 'Z' || ch == '+' || ch == '.') {
                ++i;
            } else {
                break;
            }
        }
        String originalPart = tail.substring(0, i);
        String workStr = originalPart;

        int len = workStr.length();
        if (len == 0) {
            throw new ParseException("Can not parse date: empty input", origStart);
        }
        char c = workStr.charAt(len-1);
        DateFormat df;
        String formatStr;

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
            if (len >= 4 && workStr.charAt(len-4) == ':') {
                StringBuilder sb = new StringBuilder(workStr);
                sb.insert(len-1, ".000");
                workStr = sb.toString();
            }
        } else {
            if (hasTimeZone(workStr)) {
                c = workStr.charAt(len-3);
                if (c == ':') {
                    StringBuilder sb = new StringBuilder(workStr);
                    sb.delete(len-3, len-2);
                    workStr = sb.toString();
                } else if (c == '+' || c == '-') {
                    workStr += "00";
                }
                len = workStr.length();
                int timeLen = len - workStr.lastIndexOf('T') - 6;
                if (timeLen < 12) {
                    int offset = len - 5;
                    StringBuilder sb = new StringBuilder(workStr);
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
                        sb.insert(offset, "00.000");
                    case 5:
                        sb.insert(offset, ":00.000");
                    }
                    workStr = sb.toString();
                }
                df = _formatISO8601;
                formatStr = DATE_FORMAT_STR_ISO8601;
                if (_formatISO8601 == null) {
                    df = _formatISO8601 = _cloneFormat(DATE_FORMAT_ISO8601, formatStr,
                            _timezone, _locale, _lenient);
                }
            } else {
                StringBuilder sb = new StringBuilder(workStr);
                int timeLen = len - workStr.lastIndexOf('T') - 1;
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
                workStr = sb.toString();
                df = _formatISO8601_z;
                formatStr = DATE_FORMAT_STR_ISO8601_Z;
                if (df == null) {
                    df = _formatISO8601_z = _cloneFormat(DATE_FORMAT_ISO8601_Z, formatStr,
                            DEFAULT_TIMEZONE, _locale, _lenient);
                }
            }
        }
        ParsePosition tmpPos = new ParsePosition(0);
        Date dt = df.parse(workStr, tmpPos);
        if (dt == null) {
            throw new ParseException
            (String.format("Can not parse date \"%s\": while it seems to fit format '%s', parsing fails (leniency? %s)",
                           workStr, formatStr, _lenient),
               tmpPos.getErrorIndex());
        }
        if (pos != null) {
            pos.setIndex(origStart + originalPart.length());
        }
        return dt;
    }