static String getCharsetFromContentType(String contentType) {
    if (contentType == null) return null;
    Matcher m = charsetPattern.matcher(contentType);
    if (m.find()) {
        String charset = m.group(1).trim();
        if (charset.startsWith("\"") && charset.endsWith("\"")) {
            charset = charset.substring(1, charset.length() - 1).trim();
        }
        try {
            Charset.forName(charset);
            return charset;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    return null;
}