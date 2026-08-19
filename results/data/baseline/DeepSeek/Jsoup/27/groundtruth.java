// ===== FIXED org.jsoup.helper.DataUtil :: getCharsetFromContentType(String) [lines 127-137] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-27-fixed/src/main/java/org/jsoup/helper/DataUtil.java =====
    static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
            if (Charset.isSupported(charset)) return charset;
            charset = charset.toUpperCase(Locale.ENGLISH);
            if (Charset.isSupported(charset)) return charset;
        }
        return null;
    }
