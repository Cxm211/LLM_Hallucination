    public boolean hasClass(String className) {
        final String classAttr = attributes.get("class");
        if (classAttr == null || classAttr.length() == 0) {
            return false;
        }
        final int len = classAttr.length();
        final int wantLen = className.length();

        if (len < wantLen) {
            return false;
        }

        // scan for whitespace and compare regions (with no string or arraylist allocations)
        boolean inClass = false;
        int start = 0;
        for (int i = 0; i < len; i++) {
            if (Character.isWhitespace(classAttr.charAt(i))) {
                if (inClass) {
                    // white space ends a class name, compare it with the requested one, ignore case
                    if (i - start == wantLen && classAttr.regionMatches(true, start, className, 0, wantLen)) {
                        return true;
                    }
                    inClass = false;
                }
            } else {
                if (!inClass) {
                    // we're in a class name : keep the start of the substring
                    inClass = true;
                    start = i;
                }
            }
        }

        // check the last entry
        if (inClass && len - start == wantLen) {
            return classAttr.regionMatches(true, start, className, 0, wantLen);
        }

        return false;
    }