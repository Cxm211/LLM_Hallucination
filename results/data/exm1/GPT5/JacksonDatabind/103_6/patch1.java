public Date parseDate(String dateStr) throws IllegalArgumentException
    {
        try {
            DateFormat df = getDateFormat();
            synchronized (df) {
                return df.parse(dateStr);
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format(
                    "Failed to parse Date value '%s': %s", dateStr,
                    e.getMessage()));
        }
    }