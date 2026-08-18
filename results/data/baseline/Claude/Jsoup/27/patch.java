static String getCharsetFromContentType(String contentType) {
    if (contentType == null) return null;
    Matcher m = charsetPattern.matcher(contentType);
    if (m.find()) {
        String charset = m.group(1).trim();
        charset = charset.replace("\"" , "");
        if (charset.isEmpty()) return null;
        return charset;
    }
    return null;
}