static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
            // Remove surrounding quotes
            if (charset.startsWith("\"") || charset.startsWith("'")) {
                charset = charset.substring(1);
            }
            if (charset.endsWith("\"") || charset.endsWith("'")) {
                charset = charset.substring(0, charset.length() - 1);
            }
            charset = charset.trim();
            // Handle multiple charsets: take the first part before any comma or semicolon
            int comma = charset.indexOf(',');
            int semi = charset.indexOf(';');
            int cut = -1;
            if (comma >= 0 && semi >= 0) {
                cut = Math.min(comma, semi);
            } else if (comma >= 0) {
                cut = comma;
            } else if (semi >= 0) {
                cut = semi;
            }
            if (cut >= 0) {
                charset = charset.substring(0, cut).trim();
            }
            // Handle duplicate "charset=" prefix
            while (charset.startsWith("charset=")) {
                charset = charset.substring("charset=".length()).trim();
            }
            if (charset.isEmpty()) {
                return null;
            }
            try {
                if (Charset.isSupported(charset)) return charset;
                charset = charset.toUpperCase(Locale.ENGLISH);
                if (Charset.isSupported(charset)) return charset;
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }