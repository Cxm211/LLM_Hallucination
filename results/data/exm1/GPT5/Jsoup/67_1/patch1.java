private boolean inSpecificScope(String[] targetNames, String[] baseTypes, String[] extraTypes) {
        // Ensure arrays are sorted for inSorted lookup, without mutating shared inputs
        if (targetNames != null) {
            String[] tn = targetNames.clone();
            java.util.Arrays.sort(tn);
            targetNames = tn;
        }
        if (baseTypes != null) {
            String[] bt = baseTypes.clone();
            java.util.Arrays.sort(bt);
            baseTypes = bt;
        }
        if (extraTypes != null) {
            String[] et = extraTypes.clone();
            java.util.Arrays.sort(et);
            extraTypes = et;
        }
        int depth = stack.size() -1;
        for (int pos = depth; pos >= 0; pos--) {
            Element el = stack.get(pos);
            String elName = el.nodeName();
            if (inSorted(elName, targetNames))
                return true;
            if (inSorted(elName, baseTypes))
                return false;
            if (extraTypes != null && inSorted(elName, extraTypes))
                return false;
        }
        Validate.fail("Should not be reachable");
        return false;
    }