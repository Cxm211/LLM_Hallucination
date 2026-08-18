// buggy code
    static String stripLeadingAndTrailingQuotes(String str)
    {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.startsWith("\""))
        {
            str = str.substring(1);
        }
        if (!str.isEmpty() && str.endsWith("\""))
        {
            str = str.substring(0, str.length() - 1);
        }
        
        return str;
    }
