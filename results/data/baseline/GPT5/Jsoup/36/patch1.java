static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
            if (charset.startsWith("\"") && charset.endsWith("\"") && charset.length() > 1)
                charset = charset.substring(1, charset.length() - 1);
            if (charset.startsWith("'") && charset.endsWith("'") && charset.length() > 1)
                charset = charset.substring(1, charset.length() - 1);
            try {
                if (Charset.isSupported(charset)) return charset;
                charset = charset.toUpperCase(Locale.ENGLISH);
                if (Charset.isSupported(charset)) return charset;
            } catch (IllegalCharsetNameException e) {
                return null;
            }
            // if our advanced charset matching fails.... we just take the default
        }
        return null;
    }