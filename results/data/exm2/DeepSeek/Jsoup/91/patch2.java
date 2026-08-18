    boolean containsIgnoreCase(String seq) {
        // used to check presence of </title>, </style>. only finds consistent case.
        String loScan = seq.toLowerCase(Locale.ROOT);
        String hiScan = seq.toUpperCase(Locale.ROOT);
        return (nextIndexOf(loScan) > -1) || (nextIndexOf(hiScan) > -1);
    }