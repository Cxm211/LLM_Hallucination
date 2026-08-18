Element getActiveFormattingElement(String nodeName) {
        for (int pos = formattingElements.size() -1; pos >= 0; pos--) {
            Element next = formattingElements.get(pos);
            if (next == null)
                break;
            else if (next.nodeName().equals(nodeName))
                return next;
        }
        return null;
    }