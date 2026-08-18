protected Date parseAsISO8601(String dateStr, ParsePosition pos)
    {
        int len = dateStr.length();
        char c = dateStr.charAt(len-1);
        DateFormat df;

        if (len <= 10 && Character.isDigit(c)) {
            df = _formatPlain;
            if (df == null) {
                df = _formatPlain = _cloneFormat(DATE_FORMAT_PLAIN, DATE_FORMAT_STR_PLAIN, _timezone, _locale);
            }
        } else if (c == 'Z') {
            df = _formatISO8601_z;
            if (df == null) {
                df = _formatISO8601_z = _cloneFormat(DATE_FORMAT_ISO8601_Z, DATE_FORMAT_STR_ISO8601_Z, _timezone, _locale);
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
                // Determine where the timezone starts (always 5 chars: sign + 4 digits)
                int tzIndex = len - 5;
                String beforeTz = dateStr.substring(0, tzIndex);
                int colonCount = 0;
                for (int i = 0; i < beforeTz.length(); i++) {
                    if (beforeTz.charAt(i) == ':') colonCount++;
                }
                // Check if there is already a dot (milliseconds) before timezone
                boolean hasMillis = beforeTz.indexOf('.') >= 0;
                StringBuilder sb = new StringBuilder(dateStr);
                if (colonCount == 1 && !hasMillis) {
                    // Missing seconds, insert seconds and milliseconds
                    sb.insert(tzIndex, ":00.000");
                } else if (colonCount == 2 && !hasMillis) {
                    // Missing milliseconds only
                    sb.insert(tzIndex, ".000");
                }
                dateStr = sb.toString();
                df = _formatISO8601;
                if (_formatISO8601 == null) {
                    df = _formatISO8601 = _cloneFormat(DATE_FORMAT_ISO8601, DATE_FORMAT_STR_ISO8601, _timezone, _locale);
                }
            } else {
                StringBuilder sb = new StringBuilder(dateStr);
                int timeLen = len - dateStr.lastIndexOf('T') - 1;
                if (timeLen <= 8) {
                    sb.append(".000");
                }
                sb.append('Z');
                dateStr = sb.toString();
                df = _formatISO8601_z;
                if (df == null) {
                    df = _formatISO8601_z = _cloneFormat(DATE_FORMAT_ISO8601_Z, DATE_FORMAT_STR_ISO8601_Z,
                            _timezone, _locale);
                }
            }
        }
        return df.parse(dateStr, pos);
    }