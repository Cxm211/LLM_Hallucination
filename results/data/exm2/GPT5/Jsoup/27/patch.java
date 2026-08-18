    static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
            if (charset.length() > 0 && charset.startsWith("\"") && charset.endsWith("\"")) charset = charset.substring(1, charset.length() - 1);
            if (charset.length() == 0) return null;
            if (!java.nio.charset.Charset.isSupported(charset)) return null;
            return charset;
        }
        return null;
    }