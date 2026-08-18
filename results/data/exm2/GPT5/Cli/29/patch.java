static String stripLeadingAndTrailingQuotes(String str)
    {
        int length = str.length();
        if (length >= 2 && str.startsWith("\"") && str.endsWith("\""))
        {
            return str.substring(1, length - 1);
        }
        return str;
    }