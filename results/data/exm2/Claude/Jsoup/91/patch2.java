boolean containsIgnoreCase(String seq) {
    // used to check presence of </title>, </style>. only finds consistent case.
    String loScan = seq.toLowerCase(Locale.ENGLISH);
    String hiScan = seq.toUpperCase(Locale.ENGLISH);
    boolean found = (nextIndexOf(loScan) > -1) || (nextIndexOf(hiScan) > -1);
    return found;
}