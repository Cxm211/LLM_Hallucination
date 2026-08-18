private String stringValue(Node node) {
        int nodeType = node.getNodeType();
        if (nodeType == Node.COMMENT_NODE) {
            return "";
        }
        if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
            String text = node.getNodeValue();
            return text == null ? "" : text;
        }
        if (nodeType == Node.PROCESSING_INSTRUCTION_NODE) {
            String text = ((ProcessingInstruction) node).getData();
            return text == null ? "" : text;
        }
        NodeList list = node.getChildNodes();
        StringBuffer buf = new StringBuffer(16);
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            short ct = child.getNodeType();
            if (ct == Node.TEXT_NODE || ct == Node.CDATA_SECTION_NODE) {
                buf.append(child.getNodeValue());
            } else if (ct == Node.ELEMENT_NODE) {
                buf.append(stringValue(child));
            } else {
                // ignore comments, processing instructions, etc.
            }
        }
        return buf.toString();
    }