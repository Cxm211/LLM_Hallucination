private boolean inSpecificScope(String[] targetNames, String[] baseTypes, String[] extraTypes) {
        int depth = stack.size() - 1;
        for (int pos = depth; pos >= 0; pos--) {
            Element el = stack.get(pos);
            String elName = el.nodeName();
            // check targets
            for (int i = 0; i < targetNames.length; i++) {
                if (elName.equals(targetNames[i]))
                    return true;
            }
            // check base types
            if (baseTypes != null) {
                for (int i = 0; i < baseTypes.length; i++) {
                    if (elName.equals(baseTypes[i]))
                        return false;
                }
            }
            // check extra types
            if (extraTypes != null) {
                for (int i = 0; i < extraTypes.length; i++) {
                    if (elName.equals(extraTypes[i]))
                        return false;
                }
            }
        }
        Validate.fail("Should not be reachable");
        return false;
    }