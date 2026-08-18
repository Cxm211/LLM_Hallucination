void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        String name = StringUtil.isBlank(attr("name")) ? "html" : attr("name");
        String publicId = attr("publicId");
        String systemId = attr("systemId");

        accum.append("<!DOCTYPE ").append(name);
        if (!StringUtil.isBlank(publicId)) {
            accum.append(" PUBLIC \"").append(publicId).append("\"");
        }
        if (!StringUtil.isBlank(systemId)) {
            if (StringUtil.isBlank(publicId)) accum.append(" SYSTEM");
            accum.append(" \"").append(systemId).append("\"");
        }
        accum.append('>');
    }