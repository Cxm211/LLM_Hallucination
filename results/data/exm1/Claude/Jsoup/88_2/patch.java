public String getValue() {
        if (val != null && val.length() > 1 && val.charAt(0) == '(' && val.charAt(val.length() - 1) == ')') {
            return val.substring(1, val.length() - 1);
        }
        return val;
    }