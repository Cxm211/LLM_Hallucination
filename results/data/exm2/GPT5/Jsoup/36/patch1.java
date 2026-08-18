static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
            if (charset.length() == 0) return null;
            if ((charset.startsWith("\"") && charset.endsWith("\"")) || (charset.startsWith("'") && charset.endsWith("'")))
                charset = charset.substring(1, charset.length() - 1).trim();
            int comma = charset.indexOf(',');
            if (comma != -1)
                charset = charset.substring(0, comma).trim();
            int idx = charset.toLowerCase(Locale.ENGLISH).lastIndexOf("charset=");
            if (idx != -1)
                charset = charset.substring(idx + 8).trim();
            int eq = charset.lastIndexOf('=');
            if (eq != -1)
                charset = charset.substring(eq + 1).trim();
            if (charset.length() == 0) return null;
            try {
                if (Charset.isSupported(charset)) return charset;
                charset = charset.toUpperCase(Locale.ENGLISH);
                if (Charset.isSupported(charset)) return charset;
            } catch (IllegalCharsetNameException e) {
                return null;
            }
        }
        return null;
    }