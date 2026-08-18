boolean containsIgnoreCase(String seq) {
        String loScan = seq.toLowerCase(Locale.ENGLISH);
        String hiScan = seq.toUpperCase(Locale.ENGLISH);
        return (nextIndexOf(loScan) > -1) || (nextIndexOf(hiScan) > -1);
    }