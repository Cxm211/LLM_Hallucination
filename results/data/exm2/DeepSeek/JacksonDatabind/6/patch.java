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
            // [JACKSON-334]: may be missing milliseconds... if so, add
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
                // Normalize time part to have seconds and 3-digit milliseconds
                int tIndex = dateStr.indexOf('T');
                int plusIndex = dateStr.indexOf('+', tIndex);
                int minusIndex = dateStr.indexOf('-', tIndex);
                int tzIndex = plusIndex != -1 ? plusIndex : minusIndex;
                String beforeTz = dateStr.substring(0, tzIndex);
                String afterTz = dateStr.substring(tzIndex);
                String timePart = dateStr.substring(tIndex+1, tzIndex);
                
                // Ensure seconds are present
                int colonCount = 0;
                int lastColonPos = -1;
                for (int i = 0; i < timePart.length(); i++) {
                    if (timePart.charAt(i) == ':') {
                        colonCount++;
                        lastColonPos = i;
                    }
                }
                int dotIndex = timePart.indexOf('.');
                if (colonCount == 1) {
                    // missing seconds
                    if (dotIndex != -1) {
                        // insert ":00" before dot
                        timePart = timePart.substring(0, dotIndex) + ":00" + timePart.substring(dotIndex);
                    } else {
                        timePart = timePart + ":00";
                    }
                    // update dotIndex
                    dotIndex = timePart.indexOf('.');
                }
                // Ensure milliseconds are exactly 3 digits
                if (dotIndex == -1) {
                    // no milliseconds
                    timePart = timePart + ".000";
                } else {
                    String millis = timePart.substring(dotIndex+1);
                    if (millis.length() < 3) {
                        // pad with zeros on the right
                        while (millis.length() < 3) {
                            millis = millis + "0";
                        }
                        timePart = timePart.substring(0, dotIndex+1) + millis;
                    }
                }
                // Reconstruct dateStr
                dateStr = beforeTz + timePart + afterTz;
                len = dateStr.length();
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