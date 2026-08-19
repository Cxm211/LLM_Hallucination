// ===== FIXED org.jsoup.UncheckedIOException :: UncheckedIOException [lines 6-8] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-91-fixed/src/main/java/org/jsoup/UncheckedIOException.java =====
    public UncheckedIOException(IOException cause) {
        super(cause);
    }

// ===== FIXED org.jsoup.parser.CharacterReader :: CharacterReader [lines 30-40] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-91-fixed/src/main/java/org/jsoup/parser/CharacterReader.java =====
    public CharacterReader(Reader input, int sz) {
        Validate.notNull(input);
        Validate.isTrue(input.markSupported());
        reader = input;
        charBuf = new char[sz > maxBufferLen ? maxBufferLen : sz];
        bufferUp();

        if (isBinary()) {
            throw new UncheckedIOException("Input is binary and unsupported");
        }
    }

// ===== FIXED org.jsoup.parser.CharacterReader :: containsIgnoreCase(String) [lines 448-453] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-91-fixed/src/main/java/org/jsoup/parser/CharacterReader.java =====
    boolean containsIgnoreCase(String seq) {
        // used to check presence of </title>, </style>. only finds consistent case.
        String loScan = seq.toLowerCase(Locale.ENGLISH);
        String hiScan = seq.toUpperCase(Locale.ENGLISH);
        return (nextIndexOf(loScan) > -1) || (nextIndexOf(hiScan) > -1);
    }
