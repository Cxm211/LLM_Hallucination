// ===== FIXED org.jsoup.parser.CharacterReader :: CharacterReader [lines 16-22] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-18-fixed/src/main/java/org/jsoup/parser/CharacterReader.java =====
    CharacterReader(String input) {
        Validate.notNull(input);
        input = input.replaceAll("\r\n?", "\n"); // normalise carriage returns to newlines

        this.input = input;
        this.length = input.length();
    }

// ===== FIXED org.jsoup.parser.CharacterReader :: consumeToEnd() [lines 99-103] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-18-fixed/src/main/java/org/jsoup/parser/CharacterReader.java =====
    String consumeToEnd() {
        String data = input.substring(pos, input.length());
        pos = input.length();
        return data;
    }
