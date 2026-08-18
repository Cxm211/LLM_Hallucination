    protected Date parseAsISO8601(String dateStr, ParsePosition pos)
    {
        /* 21-May-2009, tatu: DateFormat has very strict handling of
         * timezone  modifiers for ISO-8601. So we need to do some scrubbing.
         */

        /* First: do we have "zulu" format ('Z' == "GMT")? If yes, that's
         * quite simple because we already set date format timezone to be
         * GMT, and hence can just strip out 'Z' altogether
         */
        int len = dateStr.length();
        char c = dateStr.charAt(len-1);
        DateFormat df;

        // [JACKSON-200]: need to support "plain" date...
        if (len <= 10 && Character.isDigit(c)) {
            df = _formatPlain;
            if (df == null) {
                df = _formatPlain = _cloneFormat(DATE_FORMAT_PLAIN, DATE_FORMAT_STR_PLAIN, _timezone, _locale);
            }
        } else if (c == 'Z') {
            // handle Z format
            int tIndex = dateStr.indexOf('T');
            String timePart = dateStr.substring(tIndex+1, len-1); // exclude 'Z'
            // normalize time part to HH:mm:ss.SSS
            int dotIdx = timePart.indexOf('.');
            if (dotIdx >= 0) {
                String beforeDot = timePart.substring(0, dotIdx);
                String millis = timePart.substring(dotIdx+1);
                if (millis.length() < 3) {
                    while (millis.length() < 3) millis += "0";
                }
                int colons = 0;
                for (int i = 0; i < beforeDot.length(); i++) {
                    if (beforeDot.charAt(i) == ':') colons++;
                }
                if (colons == 0) {
                    beforeDot = beforeDot + ":00:00";
                } else if (colons == 1) {
                    beforeDot = beforeDot + ":00";
                }
                timePart = beforeDot + "." + millis;
            } else {
                int colons = 0;
                for (int i = 0; i < timePart.length(); i++) {
                    if (timePart.charAt(i) == ':') colons++;
                }
                if (colons == 0) {
                    timePart = timePart + ":00:00.000";
                } else if (colons == 1) {
                    timePart = timePart + ":00.000";
                } else if (colons == 2) {
                    timePart = timePart + ".000";
                }
            }
            dateStr = dateStr.substring(0, tIndex+1) + timePart + "Z";
            df = _formatISO8601_z;
            if (df == null) {
                df = _formatISO8601_z = _cloneFormat(DATE_FORMAT_ISO8601_Z, DATE_FORMAT_STR_ISO8601_Z, _timezone, _locale);
            }
        } else {
            // Let's see if we have timezone indicator or not...
            if (hasTimeZone(dateStr)) {
                c = dateStr.charAt(len-3);
                if (c == ':') { // remove optional colon
                    // remove colon
                    StringBuilder sb = new StringBuilder(dateStr);
                    sb.delete(len-3, len-2);
                    dateStr = sb.toString();
                    len = dateStr.length();
                } else if (c == '+' || c == '-') { // missing minutes
                    // let's just append '00'
                    dateStr += "00";
                    len = dateStr.length();
                }
                // Now dateStr ends with +HHMM or -HHMM
                int tIndex = dateStr.indexOf('T');
                int offsetStart = len - 5; // offset is 5 characters
                String timePart = dateStr.substring(tIndex+1, offsetStart);
                // normalize time part to HH:mm:ss.SSS
                int dotIdx = timePart.indexOf('.');
                if (dotIdx >= 0) {
                    String beforeDot = timePart.substring(0, dotIdx);
                    String millis = timePart.substring(dotIdx+1);
                    if (millis.length() < 3) {
                        while (millis.length() < 3) millis += "0";
                    }
                    int colons = 0;
                    for (int i = 0; i < beforeDot.length(); i++) {
                        if (beforeDot.charAt(i) == ':') colons++;
                    }
                    if (colons == 0) {
                        beforeDot = beforeDot + ":00:00";
                    } else if (colons == 1) {
                        beforeDot = beforeDot + ":00";
                    }
                    timePart = beforeDot + "." + millis;
                } else {
                    int colons = 0;
                    for (int i = 0; i < timePart.length(); i++) {
                        if (timePart.charAt(i) == ':') colons++;
                    }
                    if (colons == 0) {
                        timePart = timePart + ":00:00.000";
                    } else if (colons == 1) {
                        timePart = timePart + ":00.000";
                    } else if (colons == 2) {
                        timePart = timePart + ".000";
                    }
                }
                dateStr = dateStr.substring(0, tIndex+1) + timePart + dateStr.substring(offsetStart);
                df = _formatISO8601;
                if (_formatISO8601 == null) {
                    df = _formatISO8601 = _cloneFormat(DATE_FORMAT_ISO8601, DATE_FORMAT_STR_ISO8601, _timezone, _locale);
                }
            } else {
                // No timezone indicator
                int tIndex = dateStr.lastIndexOf('T');
                String timePart = dateStr.substring(tIndex+1);
                // normalize time part to HH:mm:ss.SSS
                int dotIdx = timePart.indexOf('.');
                if (dotIdx >= 0) {
                    String beforeDot = timePart.substring(0, dotIdx);
                    String millis = timePart.substring(dotIdx+1);
                    if (millis.length() < 3) {
                        while (millis.length() < 3) millis += "0";
                    }
                    int colons = 0;
                    for (int i = 0; i < beforeDot.length(); i++) {
                        if (beforeDot.charAt(i) == ':') colons++;
                    }
                    if (colons == 0) {
                        beforeDot = beforeDot + ":00:00";
                    } else if (colons == 1) {
                        beforeDot = beforeDot + ":00";
                    }
                    timePart = beforeDot + "." + millis;
                } else {
                    int colons = 0;
                    for (int i = 0; i < timePart.length(); i++) {
                        if (timePart.charAt(i) == ':') colons++;
                    }
                    if (colons == 0) {
                        timePart = timePart + ":00:00.000";
                    } else if (colons == 1) {
                        timePart = timePart + ":00.000";
                    } else if (colons == 2) {
                        timePart = timePart + ".000";
                    }
                }
                dateStr = dateStr.substring(0, tIndex+1) + timePart + "Z";
                df = _formatISO8601_z;
                if (df == null) {
                    df = _formatISO8601_z = _cloneFormat(DATE_FORMAT_ISO8601_Z, DATE_FORMAT_STR_ISO8601_Z,
                            _timezone, _locale);
                }
            }
        }
        return df.parse(dateStr, pos);
    }