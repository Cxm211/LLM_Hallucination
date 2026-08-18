public Date parseDate(String dateStr) throws IllegalArgumentException
    {
        try {
            DateFormat df = getDateFormat();
            return df.parse(dateStr);
        } catch (ParseException e) {
            String msg = e.getMessage();
            throw new IllegalArgumentException(String.format(
                    "Failed to parse Date value '%s': %s", dateStr,
                    msg == null ? "N/A" : msg));
        }
    }