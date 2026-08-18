public static String sanitize(String s) {
        if (s == null) {
            return null;
        }
        final char[] chars = s.toCharArray();
        final int len = chars.length;
        final StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            final char c = chars[i];
            if (!Character.isISOControl(c)) {
                Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
                if (block != null && block != Character.UnicodeBlock.SPECIALS) {
                    sb.append(c);
                    continue;
                }
            }
            sb.append('?');
        }
        String res = sb.toString();
        if (res.length() > 255) {
            return res.substring(0, 252) + "...";
        }
        return res;
    }