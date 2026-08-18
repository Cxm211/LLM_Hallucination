void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        String name = attr("name");
        String pub = attr("publicId");
        String sys = attr("systemId");
        boolean hasPub = !StringUtil.isBlank(pub);
        boolean hasSys = !StringUtil.isBlank(sys);

        accum.append("<!DOCTYPE ").append(name);
        if (hasPub) {
            accum.append(" PUBLIC \"").append(pub).append("\"");
        }
        if (hasSys) {
            accum.append(' ').append('"').append(sys).append('"');
        }
        accum.append('>');
    }