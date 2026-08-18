static boolean preserveWhitespace(Node node) {
    // looks only at this element and five levels up, to prevent recursion & needless stack searches
    if (node != null && node instanceof Element) {
        Element el = (Element) node;
        int levelsToCheck = 6; // this element + 5 levels up
        
        while (el != null && levelsToCheck > 0) {
            if (el.tag.preserveWhitespace())
                return true;
            el = el.parent();
            levelsToCheck--;
        }
    }
    return false;
}