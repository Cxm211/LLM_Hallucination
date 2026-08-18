static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
            // remove optional surrounding quotes
            if ((charset.startsWith("\"") && charset.endsWith("\"")) || (charset.startsWith("'") && charset.endsWith("'"))) {
                charset = charset.substring(1, charset.length() - 1).trim();
            }
            try {
                if (Charset.isSupported(charset))
                    return charset; // preserve original case
            } catch (IllegalCharsetNameException e) {
                // fall through to return null
            }
        }
        return null;
    }