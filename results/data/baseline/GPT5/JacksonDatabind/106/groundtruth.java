// ===== FIXED com.fasterxml.jackson.databind.node.TreeTraversingParser :: getIntValue() [lines 306-312] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-106-fixed/src/main/java/com/fasterxml/jackson/databind/node/TreeTraversingParser.java =====
    public int getIntValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (!node.canConvertToInt()) {
            reportOverflowInt();
        }
        return node.intValue();
    }

// ===== FIXED com.fasterxml.jackson.databind.node.TreeTraversingParser :: getLongValue() [lines 315-321] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-106-fixed/src/main/java/com/fasterxml/jackson/databind/node/TreeTraversingParser.java =====
    public long getLongValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (!node.canConvertToInt()) {
            reportOverflowLong();
        }
        return node.longValue();
    }
