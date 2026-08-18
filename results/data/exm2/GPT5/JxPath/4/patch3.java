public Object getValue() {
        if (node instanceof Element) {
            return ((Element) node).getText();
        }
        if (node instanceof Comment) {
            String text = ((Comment) node).getText();
            return text;
        }
        if (node instanceof Text) {
            return ((Text) node).getText();
        }
        if (node instanceof CDATA) {
            return ((CDATA) node).getText();
        }
        if (node instanceof ProcessingInstruction) {
            String text = ((ProcessingInstruction) node).getData();
            return text;
        }
        return null;
    }