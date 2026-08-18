boolean inSelectScope(String targetName) {
    for (int pos = stack.size() -1; pos >= 0; pos--) {
        Element el = stack.get(pos);
        String elName = el.nodeName();
        if (elName.equals(targetName))
            return true;
        if (!inSorted(elName, TagSearchSelectScope))
            return false;
    }
    Validate.fail("Should not be reachable");
    return false;
}