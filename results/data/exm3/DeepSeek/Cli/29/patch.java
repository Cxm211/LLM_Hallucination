    static String stripLeadingAndTrailingQuotes(String str)
    {
        if (str.length() >= 2 && str.startsWith("\"") && str.endsWith("\"") && str.indexOf('"', 1) == str.length() - 1) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }