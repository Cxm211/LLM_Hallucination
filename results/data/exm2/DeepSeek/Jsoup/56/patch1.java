    void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) throws IOException {
        if (out.syntax() == Syntax.html && !has(PUBLIC_ID) && !has(SYSTEM_ID)) {
            // looks like a html5 doctype, go lowercase for aesthetics
            accum.append("<!doctype");
        } else {
            accum.append("<!DOCTYPE");
        }
        if (has(NAME))
            accum.append(" ").append(attr(NAME));
        if (has(PUBLIC_ID))
            accum.append(" PUBLIC \"").append(attr(PUBLIC_ID)).append('"');
        if (has(SYSTEM_ID)) {
            if (has(PUBLIC_ID)) {
                // public id already output, system id is just a space and quote
                accum.append(" \"").append(attr(SYSTEM_ID)).append('"');
            } else {
                // system only
                accum.append(" SYSTEM \"").append(attr(SYSTEM_ID)).append('"');
            }
        }
        accum.append('>');
    }