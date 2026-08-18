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
            df = _formatISO8601_z;
            if (df == null) {
                df = _formatISO8601_z = _cloneFormat(DATE_FORMAT_ISO8601_Z, DATE_FORMAT_STR_ISO8601_Z, _timezone, _locale);
            }
            // Normalize time part: ensure seconds exist and milliseconds are present/padded
            int tIndex = dateStr.lastIndexOf('T');
            if (tIndex > 0) {
                String base = dateStr.substring(0, len - 1); // without trailing 'Z'
                String tz = "Z";
                String timePart = base.substring(tIndex + 1);
                int dot = timePart.indexOf('.');
                StringBuilder timeSb = new StringBuilder(timePart);
                if (dot >= 0) {
                    int msDigits = timePart.length() - dot - 1;
                    if (msDigits == 0) {
                        timeSb.append("000");
                    } else if (msDigits == 1) {
                        timeSb.append("00");
                    } else if (msDigits == 2) {
                        timeSb.append('0');
                    }
                } else {
                    if (timePart.length() == 5) { // HH:mm
                        timeSb.append(":00.000");
                    } else if (timePart.length() == 8) { // HH:mm:ss
                        timeSb.append(".000");
                    }
                }
                dateStr = base.substring(0, tIndex + 1) + timeSb.toString() + tz;
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
                } else if (c == '+' || c == '-') { // missing minutes
                    // let's just append '00'
                    dateStr += "00";
                }
                // Normalize time part before timezone: ensure seconds and milliseconds
                len = dateStr.length();
                int tzStart = len - 5; // "+hhmm" or "-hhmm"
                int tIndex = dateStr.lastIndexOf('T');
                if (tIndex > 0 && tzStart > tIndex) {
                    String base = dateStr.substring(0, tzStart);
                    String tz = dateStr.substring(tzStart);
                    String timePart = base.substring(tIndex + 1);
                    int dot = timePart.indexOf('.');
                    StringBuilder timeSb = new StringBuilder(timePart);
                    if (dot >= 0) {
                        int msDigits = timePart.length() - dot - 1;
                        if (msDigits == 0) {
                            timeSb.append("000");
                        } else if (msDigits == 1) {
                            timeSb.append("00");
                        } else if (msDigits == 2) {
                            timeSb.append('0');
                        }
                    } else {
                        if (timePart.length() == 5) { // HH:mm
                            timeSb.append(":00.000");
                        } else if (timePart.length() == 8) { // HH:mm:ss
                            timeSb.append(".000");
                        }
                    }
                    dateStr = base.substring(0, tIndex + 1) + timeSb.toString() + tz;
                }
                df = _formatISO8601;
                if (_formatISO8601 == null) {
                    df = _formatISO8601 = _cloneFormat(DATE_FORMAT_ISO8601, DATE_FORMAT_STR_ISO8601, _timezone, _locale);
                }
            } else {
                // If not, plain date. Easiest to just patch 'Z' in the end?
                StringBuilder sb = new StringBuilder(dateStr);
                int tIndex = dateStr.lastIndexOf('T');
                if (tIndex > 0) {
                    String timePart = dateStr.substring(tIndex + 1);
                    int dot = timePart.indexOf('.');
                    if (dot >= 0) {
                        int msDigits = timePart.length() - dot - 1;
                        if (msDigits == 0) {
                            sb.append("000");
                        } else if (msDigits == 1) {
                            sb.append("0");
                        } // if 2, we'll append one more below by length rule
                    } else {
                        if (timePart.length() == 5) { // HH:mm
                            sb.append(":00.000");
                        } else if (timePart.length() == 8) { // HH:mm:ss
                            sb.append(".000");
                        } else if (timePart.length() <= 8) {
                            sb.append(".000");
                        }
                    }
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