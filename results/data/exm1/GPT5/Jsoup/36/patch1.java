static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        String lower = contentType.toLowerCase(Locale.ENGLISH);
        int start = lower.indexOf("charset=");
        if (start == -1) return null;
        String after = contentType.substring(start + 8); // after 'charset='
        // cut at separators ; or ,
        int semi = after.indexOf(';');
        if (semi != -1) after = after.substring(0, semi);
        int comma = after.indexOf(',');
        if (comma != -1) after = after.substring(0, comma);
        after = after.trim();
        // strip surrounding quotes if any
        if (after.length() > 0 && (after.charAt(0) == '"' || after.charAt(0) == '\'')) {
            after = after.substring(1);
        }
        if (after.length() > 0 && (after.charAt(after.length() - 1) == '"' || after.charAt(after.length() - 1) == '\'')) {
            after = after.substring(0, after.length() - 1);
        }
        // handle duplicate 'charset=' prefixes
        String aLower = after.toLowerCase(Locale.ENGLISH);
        while (aLower.startsWith("charset=")) {
            after = after.substring(8);
            aLower = after.toLowerCase(Locale.ENGLISH);
        }
        after = after.trim();
        if (after.length() == 0) return null;
        try {
            if (Charset.isSupported(after)) return after;
            String upper = after.toUpperCase(Locale.ENGLISH);
            if (Charset.isSupported(upper)) return upper;
        } catch (IllegalCharsetNameException e) {
            return null;
        }
        return null;
    }