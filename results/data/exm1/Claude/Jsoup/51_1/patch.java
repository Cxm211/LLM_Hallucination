boolean matchesLetter() {
    if (isEmpty())
        return false;
    char c = input[pos];
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == ':' || Character.isLetter(c);
}