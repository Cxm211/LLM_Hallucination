public static boolean containsAny(CharSequence cs, char[] searchChars) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
            return false;
        }
        int csLength = cs.length();
        int searchLength = searchChars.length;
        for (int i = 0; i < csLength; ) {
            int cp = Character.codePointAt(cs, i);
            if (cp < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                char ch = (char) cp;
                for (int j = 0; j < searchLength; j++) {
                    if (searchChars[j] == ch) {
                        return true;
                    }
                }
            }
            i += Character.charCount(cp);
        }
        return false;
    }