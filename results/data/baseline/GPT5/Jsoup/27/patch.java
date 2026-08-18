static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1);
            if (charset == null) return null;
            charset = charset.trim();
            if (charset.length() >= 2) {
                char first = charset.charAt(0);
                char last = charset.charAt(charset.length() - 1);
                if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                    charset = charset.substring(1, charset.length() - 1).trim();
                }
            }
            charset = charset.toUpperCase(Locale.ENGLISH);
            return charset;
        }
        return null;
    }