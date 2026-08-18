boolean containsIgnoreCase(String seq) {
        String scanTarget = consumed(seq);
        String loScan = scanTarget.toLowerCase(Locale.ENGLISH);
        return loScan.contains(seq.toLowerCase(Locale.ENGLISH));
    }