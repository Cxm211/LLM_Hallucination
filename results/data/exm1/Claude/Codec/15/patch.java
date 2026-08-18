private char getMappingCode(final String str, final int index) {
    final char mappedChar = this.map(str.charAt(index));
    if (index > 0 && mappedChar != '0') {
        final char hwChar = str.charAt(index - 1);
        if ('H' == hwChar || 'W' == hwChar) {
            if (index == 1) {
                return 0;
            }
            final char preHWChar = str.charAt(index - 2);
            final char firstCode = this.map(preHWChar);
            if (firstCode == mappedChar || 'H' == preHWChar || 'W' == preHWChar) {
                return 0;
            }
        }
    }
    return mappedChar;
}