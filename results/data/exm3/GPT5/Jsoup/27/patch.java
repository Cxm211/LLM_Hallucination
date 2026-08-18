static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1);
            if (charset == null) return null;
            charset = charset.trim();
            if (charset.length() == 0) return null;
            if ((charset.startsWith("\"") && charset.endsWith("\"")) || (charset.startsWith("'") && charset.endsWith("'"))) {
                if (charset.length() >= 2)
                    charset = charset.substring(1, charset.length() - 1).trim();
            }
            if (charset.length() == 0) return null;
            try {
                if (java.nio.charset.Charset.isSupported(charset)) {
                    return charset;
                }
            } catch (java.nio.charset.IllegalCharsetNameException e) {
                return null;
            }
        }
        return null;
    }