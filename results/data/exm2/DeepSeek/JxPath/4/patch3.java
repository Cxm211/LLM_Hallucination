public Object getValue() {
    if (node instanceof org.w3c.dom.Node) {
        return stringValue((org.w3c.dom.Node) node);
    } else if (node instanceof org.jdom.Element) {
        return ((org.jdom.Element) node).getTextTrim();
    } else if (node instanceof org.jdom.Comment) {
        String text = ((org.jdom.Comment) node).getText();
        return text == null ? null : text.trim();
    } else if (node instanceof org.jdom.Text) {
        return ((org.jdom.Text) node).getTextTrim();
    } else if (node instanceof org.jdom.CDATA) {
        return ((org.jdom.CDATA) node).getTextTrim();
    } else if (node instanceof org.jdom.ProcessingInstruction) {
        String text = ((org.jdom.ProcessingInstruction) node).getData();
        return text == null ? null : text.trim();
    }
    return null;
}