    private boolean inSpecificScope(String[] targetNames, String[] baseTypes, String[] extraTypes) {
        int depth = stack.size() -1;
        for (int pos = depth; pos >= 0; pos--) {
            Element el = stack.get(pos);
            String elName = el.nodeName();
            // targetNames
            boolean inTarget;
            if (targetNames == TagSearchSpecial) {
                inTarget = TagSearchSpecialSet.contains(elName);
            } else {
                inTarget = inSorted(elName, targetNames);
            }
            if (inTarget)
                return true;
            // baseTypes
            boolean inBase;
            if (baseTypes == TagSearchSpecial) {
                inBase = TagSearchSpecialSet.contains(elName);
            } else {
                inBase = inSorted(elName, baseTypes);
            }
            if (inBase)
                return false;
            // extraTypes
            if (extraTypes != null) {
                boolean inExtra;
                if (extraTypes == TagSearchSpecial) {
                    inExtra = TagSearchSpecialSet.contains(elName);
                } else {
                    inExtra = inSorted(elName, extraTypes);
                }
                if (inExtra)
                    return false;
            }
        }
        Validate.fail("Should not be reachable");
        return false;
    }