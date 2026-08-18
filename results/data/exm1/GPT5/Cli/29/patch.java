static String stripLeadingAndTrailingQuotes(String str)
    {
        if (str == null)
        {
            return null;
        }
        if (str.length() >= 2 && str.startsWith("\"") && str.endsWith("\""))
        {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }