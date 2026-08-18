// ===== FIXED org.jsoup.parser.HtmlTreeBuilderState :: anyOtherEndTag(Token, HtmlTreeBuilder) [lines 763-782] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-62-fixed/src/main/java/org/jsoup/parser/HtmlTreeBuilderState.java =====
        boolean anyOtherEndTag(Token t, HtmlTreeBuilder tb) {
            String name = t.asEndTag().name(); // matches with case sensitivity if enabled
            ArrayList<Element> stack = tb.getStack();
            for (int pos = stack.size() -1; pos >= 0; pos--) {
                Element node = stack.get(pos);
                if (node.nodeName().equals(name)) {
                    tb.generateImpliedEndTags(name);
                    if (!name.equals(tb.currentElement().nodeName()))
                        tb.error(this);
                    tb.popStackToClose(name);
                    break;
                } else {
                    if (tb.isSpecial(node)) {
                        tb.error(this);
                        return false;
                    }
                }
            }
            return true;
        }
