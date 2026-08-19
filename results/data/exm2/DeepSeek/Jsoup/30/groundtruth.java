// ===== FIXED org.jsoup.safety.Cleaner :: copySafeNodes(Element, Element) [lines 112-117] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-30-fixed/src/main/java/org/jsoup/safety/Cleaner.java =====
    private int copySafeNodes(Element source, Element dest) {
        CleaningVisitor cleaningVisitor = new CleaningVisitor(source, dest);
        NodeTraversor traversor = new NodeTraversor(cleaningVisitor);
        traversor.traverse(source);
        return cleaningVisitor.numDiscarded;
    }
