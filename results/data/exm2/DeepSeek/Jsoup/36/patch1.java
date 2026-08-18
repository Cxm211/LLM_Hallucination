    static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
            // Clean the charset string: remove any leading "charset=" prefix and split on comma
            // First, remove any surrounding quotes
            charset = charset.replaceAll("^[\"']|[\"']$", "");
            // If the charset starts with "charset=", remove it (case-insensitive)
            if (charset.toLowerCase(Locale.ENGLISH).startsWith("charset=")) {
                charset = charset.substring("charset=".length());
            }
            // Split on comma and take the first part
            int commaIndex = charset.indexOf(',');
            if (commaIndex != -1) {
                charset = charset.substring(0, commaIndex).trim();
            }
            // Now check if supported
            if (Charset.isSupported(charset)) return charset;
            charset = charset.toUpperCase(Locale.ENGLISH);
            if (Charset.isSupported(charset)) return charset;
        }
        return null;
    }