    static String stripLeadingAndTrailingQuotes(String str)
    {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\"') {
                count++;
            }
        }
        if (count == 2 && str.startsWith("\"") && str.endsWith("\"")) {
            str = str.substring(1, str.length() - 1);
        }
        return str;
    }