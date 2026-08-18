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
            // Normalize time part to ensure seconds and milliseconds
            int tIndex = dateStr.lastIndexOf('T');
            if (tIndex > 0) {
                int timeLen = len - 1 - tIndex - 1; // exclude 'T' and trailing 'Z'
                StringBuilder sb = null;
                if (timeLen == 5) { // HH:mm -> add :ss.SSS
                    sb = new StringBuilder(dateStr);
                    sb.insert(len-1, ":00.000");
                    dateStr = sb.toString();
                    len = dateStr.length();
                } else if (timeLen == 8) { // HH:mm:ss -> add .SSS
                    if (dateStr.charAt(len-4) == ':') {
                        sb = new StringBuilder(dateStr);
                        sb.insert(len-1, ".000");
                        dateStr = sb.toString();
                        len = dateStr.length();
                    } else if (dateStr.charAt(len-5) != '.') { // no millis
                        sb = new StringBuilder(dateStr);
                        sb.insert(len-1, ".000");
                        dateStr = sb.toString();
                        len = dateStr.length();
                    }
                }
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
                // Milliseconds partial or missing; and even seconds are optional
                len = dateStr.length();

                // Normalize to ensure we have seconds and milliseconds before timezone offset (which is 5 chars: "+HHMM" or "-HHMM")
                int tzStart = len - 5; // start index of timezone offset
                StringBuilder sb = null;
                // Check if seconds are present: if char at tzStart-3 is ':' we have ":ss" present
                boolean hasSeconds = (tzStart - 3) >= 0 && dateStr.charAt(tzStart - 3) == ':';
                if (!hasSeconds) {
                    // insert ":00.000" before timezone
                    sb = new StringBuilder(dateStr);
                    sb.insert(tzStart, ":00.000");
                    dateStr = sb.toString();
                    len = dateStr.length();
                } else {
                    // has seconds; ensure milliseconds exist
                    // if there's no '.' before tzStart (after seconds), add ".000"
                    boolean hasMillis = (tzStart - 4) >= 0 && dateStr.charAt(tzStart - 4) == '.';
                    if (!hasMillis) {
                        sb = new StringBuilder(dateStr);
                        sb.insert(tzStart, ".000");
                        dateStr = sb.toString();
                        len = dateStr.length();
                    }
                }

                df = _formatISO8601;
                if (_formatISO8601 == null) {
                    df = _formatISO8601 = _cloneFormat(DATE_FORMAT_ISO8601, DATE_FORMAT_STR_ISO8601, _timezone, _locale);
                }
            } else {
                // If not, plain date. Easiest to just patch 'Z' in the end?
                StringBuilder sb = new StringBuilder(dateStr);
                // And possible also millisecond part if missing
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