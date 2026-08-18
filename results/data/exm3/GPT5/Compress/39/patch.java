public static String sanitize(String s) {
        final char[] chars = s.toCharArray();
        final int len = chars.length;
        final StringBuilder sb = new StringBuilder();
        final int max = 255;
        final String ellipsis = "...";
        for (int i = 0; i < len; i++) {
            final char c = chars[i];
            if (!Character.isISOControl(c)) {
                Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
                if (block != null && block != Character.UnicodeBlock.SPECIALS) {
                    sb.append(c);
                } else {
                    sb.append('?');
                }
            } else {
                sb.append('?');
            }
            if (i < len - 1 && sb.length() >= max - ellipsis.length()) {
                sb.append(ellipsis);
                break;
            }
        }
        return sb.toString();
    }