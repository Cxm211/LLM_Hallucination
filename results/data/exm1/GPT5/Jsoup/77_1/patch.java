private void popStackToClose(Token.EndTag endTag) {
        String elName = endTag.name();
        int firstFound = -1;

        for (int pos = stack.size() - 1; pos >= 0; pos--) {
            Element next = stack.get(pos);
            if (next.nodeName().equals(elName)) {
                firstFound = pos;
                break;
            }
        }
        if (firstFound == -1)
            return; // not found, skip

        for (int pos = stack.size() - 1; pos >= firstFound; pos--) {
            stack.remove(pos);
        }
    }