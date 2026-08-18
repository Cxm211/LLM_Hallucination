static String stripLeadingHyphens(String str)
    {
        if (str == null || str.isEmpty())
        {
            return str;
        }
        int i = 0;
        int len = str.length();
        while (i < len && str.charAt(i) == '-')
        {
            i++;
        }
        return i > 0 ? str.substring(i) : str;
    }