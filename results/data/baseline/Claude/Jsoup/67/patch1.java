private boolean inSpecificScope(String[] targetNames, String[] baseTypes, String[] extraTypes) {
        int depth = stack.size() -1;
        for (int pos = depth; pos >= 0; pos--) {
            Element el = stack.get(pos);
            String elName = el.nodeName();
            if (inSorted(targetNames, elName))
                return true;
            if (inSorted(baseTypes, elName))
                return false;
            if (extraTypes != null && inSorted(extraTypes, elName))
                return false;
        }
        Validate.fail("Should not be reachable");
        return false;
    }