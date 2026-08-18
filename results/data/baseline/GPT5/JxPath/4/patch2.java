private String stringValue(Node node) {
        int nodeType = node.getNodeType();
        if (nodeType == Node.COMMENT_NODE) {
            String text = ((Comment) node).getData();
            return text == null ? "" : text.trim();
        }
        if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
            String text = node.getNodeValue();
            return text == null ? "" : text.trim();
        }
        if (nodeType == Node.PROCESSING_INSTRUCTION_NODE) {
            String text = ((ProcessingInstruction) node).getData();
            return text == null ? "" : text.trim();
        }
        NodeList list = node.getChildNodes();
        StringBuffer buf = new StringBuffer(16);
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            short ct = child.getNodeType();
            if (ct == Node.TEXT_NODE || ct == Node.CDATA_SECTION_NODE) {
                String v = child.getNodeValue();
                if (v != null) {
                    buf.append(v);
                }
            } else if (ct != Node.COMMENT_NODE && ct != Node.PROCESSING_INSTRUCTION_NODE) {
                buf.append(stringValue(child));
            }
        }
        return buf.toString().trim();
    }