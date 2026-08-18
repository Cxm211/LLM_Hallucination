private void popStackToClose(Token.EndTag endTag) {
    String elName = endTag.name();
    // Find the first matching element from the top
    for (int i = stack.size() - 1; i >= 0; i--) {
        Element next = stack.get(i);
        if (next.nodeName().equals(elName)) {
            // Pop elements until this one is removed
            while (true) {
                Element popped = stack.remove(stack.size() - 1);
                if (popped == next)
                    break;
            }
            return;
        }
    }
    // If no matching element found, pop the innermost element (for XML recovery)
    if (!stack.isEmpty()) {
        stack.remove(stack.size() - 1);
    }
}