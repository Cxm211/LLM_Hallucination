    void popStackToBefore(String elName) {
        for (int pos = stack.size() -1; pos >= 0; pos--) {
            Element next = stack.get(pos);
            if (next.nodeName().equalsIgnoreCase(elName)) {
                break;
            } else {
                stack.remove(pos);
            }
        }
    }