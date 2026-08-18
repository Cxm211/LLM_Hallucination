boolean process(Token t, HtmlTreeBuilder tb) {
            if (isWhitespace(t)) {
                return true;
            } else if (t.isComment()) {
                tb.insert(t.asComment());
            } else if (t.isDoctype()) {
                Token.Doctype d = t.asDoctype();
                DocumentType doctype = new DocumentType(
                    tb.settings.normalizeTag(d.getName()), d.getPublicIdentifier(), d.getSystemIdentifier(), tb.getBaseUri());
                tb.getDocument().appendChild(doctype);
                if (d.isForceQuirks())
                    tb.getDocument().quirksMode(Document.QuirksMode.quirks);
                tb.transition(BeforeHtml);
            } else {
                tb.transition(BeforeHtml);
                return tb.process(t);
            }
            return true;
        }