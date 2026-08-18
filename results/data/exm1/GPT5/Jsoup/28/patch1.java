static String unescape(String string, boolean strict) {
        if (!string.contains("&"))
            return string;
        Matcher m = strict? strictUnescapePattern.matcher(string) : unescapePattern.matcher(string);
        StringBuffer accum = new StringBuffer(string.length());
        while (m.find()) {
            int charval = -1;
            String num = m.group(3);
            if (num != null) {
                try {
                    int base = m.group(2) != null ? 16 : 10;
                    charval = Integer.valueOf(num, base);
                    if (charval < 0 || (charval >= 0xD800 && charval <= 0xDFFF) || charval > 0x10FFFF) {
                        charval = -1;
                    }
                } catch (NumberFormatException e) {
                    charval = -1;
                }
            } else {
                String name = m.group(1);
                if (full.containsKey(name))
                    charval = full.get(name);
            }
            if (charval != -1) {
                String c = (charval <= 0xFFFF) ? Character.toString((char) charval) : new String(Character.toChars(charval));
                m.appendReplacement(accum, Matcher.quoteReplacement(c));
            } else {
                m.appendReplacement(accum, Matcher.quoteReplacement(m.group(0)));
            }
        }
        m.appendTail(accum);
        return accum.toString();
    }