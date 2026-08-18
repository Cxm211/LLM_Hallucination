void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) throws IOException {
        if (out.syntax() == Syntax.html && !has(PUBLIC_ID) && !has(SYSTEM_ID)) {
            // looks like a html5 doctype, go lowercase for aesthetics
            accum.append("<!doctype");
        } else {
            accum.append("<!DOCTYPE");
        }
        if (has(NAME)) {
            String name = attr(NAME);
            if (name.length() > 0)
                accum.append(" ").append(name);
        }
        if (has(PUBLIC_ID)) {
            String pub = attr(PUBLIC_ID);
            if (pub.length() > 0)
                accum.append(" PUBLIC \"").append(pub).append('"');
        }
        if (has(SYSTEM_ID)) {
            String sys = attr(SYSTEM_ID);
            if (sys.length() > 0)
                accum.append(" \"").append(sys).append('"');
        }
        accum.append('>');
    }