static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
            // take first if multiple separated by comma
            int comma = charset.indexOf(',');
            if (comma != -1)
                charset = charset.substring(0, comma).trim();
            // handle duplicate prefix like charset=charset=iso-8859-1
            String lc = charset.toLowerCase(Locale.ENGLISH);
            while (lc.startsWith("charset=")) {
                charset = charset.substring(8).trim();
                lc = charset.toLowerCase(Locale.ENGLISH);
            }
            // strip surrounding quotes if present
            if (charset.length() > 0) {
                char first = charset.charAt(0);
                if (first == '"' || first == '\'')
                    charset = charset.substring(1);
            }
            if (charset.length() > 0) {
                char last = charset.charAt(charset.length() - 1);
                if (last == '"' || last == '\'')
                    charset = charset.substring(0, charset.length() - 1);
            }
            if (charset.length() == 0) return null;
            if (Charset.isSupported(charset)) return charset;
            String upper = charset.toUpperCase(Locale.ENGLISH);
            if (Charset.isSupported(upper)) return upper;
            // if our advanced charset matching fails.... we just take the default
        }
        return null;
    }