// ===== FIXED org.jsoup.nodes.Attributes :: html(Appendable, Document) [lines 310-325] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-75-fixed/src/main/java/org/jsoup/nodes/Attributes.java =====
    final void html(final Appendable accum, final Document.OutputSettings out) throws IOException {
        final int sz = size;
        for (int i = 0; i < sz; i++) {
            // inlined from Attribute.html()
            final String key = keys[i];
            final String val = vals[i];
            accum.append(' ').append(key);

            // collapse checked=null, checked="", checked=checked; write out others
            if (!Attribute.shouldCollapseAttribute(key, val, out)) {
                accum.append("=\"");
                Entities.escape(accum, val == null ? EmptyString : val, out, true, false, false);
                accum.append('"');
            }
        }
    }
