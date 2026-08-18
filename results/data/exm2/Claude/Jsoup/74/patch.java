public static boolean isActuallyWhitespace(int c){
    return c == ' ' || c == '	' || c == '
' || c == '' || c == '' || c == 160
        || Character.getType(c) == Character.FORMAT && (c == 8203 || c == 8204 || c == 8205 || c == 173);
    // 160 is &nbsp; (non-breaking space). Not in the spec but expected.
}