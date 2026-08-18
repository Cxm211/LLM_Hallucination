public Object getValue() {
    if (node instanceof Element) {
        Element element = (Element) node;
        if (element.getTextTrim() != null) {
            String text = element.getText();
            String space = getLanguage();
            if (space != null && space.equals("preserve")) {
                return text;
            } else {
                return text.trim();
            }
        }
        return null;
    }
    if (node instanceof Comment) {
        String text = ((Comment) node).getText();
        if (text != null) {
            text = text.trim();
        }
        return text;
    }
    if (node instanceof Text) {
        String text = ((Text) node).getText();
        if (text != null) {
            text = text.trim();
        }
        return text;
    }
    if (node instanceof CDATA) {
        String text = ((CDATA) node).getText();
        if (text != null) {
            text = text.trim();
        }
        return text;
    }
    if (node instanceof ProcessingInstruction) {
        String text = ((ProcessingInstruction) node).getData();
        if (text != null) {
            text = text.trim();
        }
        return text;
    }
    return null;
}