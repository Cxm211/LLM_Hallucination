    private boolean inSpecificScope(String[] targetNames, String[] baseTypes, String[] extraTypes) {
        // https://html.spec.whatwg.org/multipage/parsing.html#has-an-element-in-the-specific-scope
        int bottom = stack.size() - 1;
        // don't walk too far up the tree - limit to MaxScopeSearchDepth elements from the current top
        final int top = bottom > MaxScopeSearchDepth ? bottom - MaxScopeSearchDepth : 0;

        for (int pos = bottom; pos >= top; pos--) {
            final String elName = stack.get(pos).nodeName();
            if (inSorted(elName, targetNames))
                return true;
            if (inSorted(elName, baseTypes))
                return false;
            if (extraTypes != null && inSorted(elName, extraTypes))
                return false;
        }
        return false;
    }