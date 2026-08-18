    void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) throws IOException {
        // check if values are non-empty
        String name = attr(NAME);
        String publicId = attr(PUBLIC_ID);
        String systemId = attr(SYSTEM_ID);
        boolean hasName = name != null && name.length() > 0;
        boolean hasPublic = publicId != null && publicId.length() > 0;
        boolean hasSystem = systemId != null && systemId.length() > 0;

        if (out.syntax() == Syntax.html && !hasPublic && !hasSystem) {
            // looks like a html5 doctype, go lowercase for aesthetics
            accum.append("<!doctype");
        } else {
            accum.append("<!DOCTYPE");
        }
        if (hasName)
            accum.append(" ").append(name);
        if (hasPublic) {
            accum.append(" PUBLIC \"").append(publicId).append('"');
            if (hasSystem)
                accum.append(" \"").append(systemId).append('"');
        } else if (hasSystem) {
            accum.append(" SYSTEM \"").append(systemId).append('"');
        }
        accum.append('>');
    }