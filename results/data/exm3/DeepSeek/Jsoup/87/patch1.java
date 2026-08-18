    Element getFromStack(String elName) {
        for (int pos = stack.size() -1; pos >= 0; pos--) {
            Element next = stack.get(pos);
            if (next.nodeName().equalsIgnoreCase(elName)) {
                return next;
            }
        }
        return null;
    }