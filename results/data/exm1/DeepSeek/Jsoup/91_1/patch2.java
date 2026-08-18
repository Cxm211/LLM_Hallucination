boolean containsIgnoreCase(String seq) {
        String search = seq.toLowerCase(Locale.ENGLISH);
        for (int i = 0; i <= charBuf.length - search.length(); i++) {
            if (String.valueOf(charBuf, i, search.length()).toLowerCase(Locale.ENGLISH).equals(search))
                return true;
        }
        return false;
    }