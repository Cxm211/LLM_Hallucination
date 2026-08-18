public Date parseDate(String dateStr) throws IllegalArgumentException
    {
        try {
            DateFormat df = getDateFormat();
            return df.parse(dateStr);
        } catch (ParseException e) {
            // Fallbacks for common ISO-8601 variants
            try {
                // Try with timezone variants and optional milliseconds
                TimeZone gmt = TimeZone.getTimeZone("GMT");
                // With milliseconds + TZ (Z, +hh, +hhmm, +hh:mm)
                String[] patterns = new String[] {
                    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                    "yyyy-MM-dd'T'HH:mm:ss.SSSXX",
                    "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                    "yyyy-MM-dd'T'HH:mm:ssXXX",
                    "yyyy-MM-dd'T'HH:mm:ssXX",
                    "yyyy-MM-dd'T'HH:mm:ssX",
                    // No timezone provided -> assume GMT
                    "yyyy-MM-dd'T'HH:mm:ss.SSS",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    // Date-only
                    "yyyy-MM-dd"
                };
                for (String p : patterns) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.US);
                        // Assume GMT to match Jackson's StdDateFormat behavior when TZ absent
                        sdf.setTimeZone(gmt);
                        return sdf.parse(dateStr);
                    } catch (ParseException ignore) { }
                }
            } catch (Exception ignoreAll) { }
            throw new IllegalArgumentException(String.format(
                    "Failed to parse Date value '%s': %s", dateStr,
                    e.getMessage()));
        }
    }