    void resetInsertionMode() {
        boolean last = false;
        for (int pos = stack.size() -1; pos >= 0; pos--) {
            Element node = stack.get(pos);
            if (pos == 0) {
                last = true;
                node = contextElement;
            }
            String name = node.nodeName();
            if ("select".equalsIgnoreCase(name)) {
                transition(HtmlTreeBuilderState.InSelect);
                break; // frag
            } else if (("td".equalsIgnoreCase(name) || "th".equalsIgnoreCase(name) && !last)) {
                transition(HtmlTreeBuilderState.InCell);
                break;
            } else if ("tr".equalsIgnoreCase(name)) {
                transition(HtmlTreeBuilderState.InRow);
                break;
            } else if ("tbody".equalsIgnoreCase(name) || "thead".equalsIgnoreCase(name) || "tfoot".equalsIgnoreCase(name)) {
                transition(HtmlTreeBuilderState.InTableBody);
                break;
            } else if ("caption".equalsIgnoreCase(name)) {
                transition(HtmlTreeBuilderState.InCaption);
                break;
            } else if ("colgroup".equalsIgnoreCase(name)) {
                transition(HtmlTreeBuilderState.InColumnGroup);
                break; // frag
            } else if ("table".equalsIgnoreCase(name)) {
                transition(HtmlTreeBuilderState.InTable);
                break;
            } else if ("head".equalsIgnoreCase(name)) {
                transition(HtmlTreeBuilderState.InBody);
                break; // frag
            } else if ("body".equalsIgnoreCase(name)) {
                transition(HtmlTreeBuilderState.InBody);
                break;
            } else if ("frameset".equalsIgnoreCase(name)) {
                transition(HtmlTreeBuilderState.InFrameset);
                break; // frag
            } else if ("html".equalsIgnoreCase(name)) {
                transition(HtmlTreeBuilderState.BeforeHead);
                break; // frag
            } else if (last) {
                transition(HtmlTreeBuilderState.InBody);
                break; // frag
            }
        }
    }