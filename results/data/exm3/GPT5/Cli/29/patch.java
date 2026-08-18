static String stripLeadingAndTrailingQuotes(String str)
    {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length >= 2 && str.startsWith("\"") && str.endsWith("\""))
        {
            return str.substring(1, length - 1);
        }
        return str;
    }