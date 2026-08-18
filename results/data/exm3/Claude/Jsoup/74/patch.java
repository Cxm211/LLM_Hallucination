public static boolean isActuallyWhitespace(int c){
    if (c == ' ' || c == '\t' || c == '\n' || c == '\f' || c == '\r' || c == 160) {
        return true;
    }
    // Handle invisible format characters that should be normalized out from text
    // Character.FORMAT (type 16): soft hyphen, zero-width space, zero-width non-joiner, zero-width joiner
    return Character.getType(c) == Character.FORMAT && (c == 173 || c == 8203 || c == 8204 || c == 8205);
}