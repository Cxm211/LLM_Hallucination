public static boolean isActuallyWhitespace(int c){
    return c == ' ' || c == '\t' || c == '\n' || c == '\f' || c == '\r' || c == 160 || Character.isWhitespace(c);
}