public static final byte[] encodeQuotedPrintable(BitSet printable, byte[] bytes) {
    if (bytes == null) {
        return null;
    }
    if (printable == null) {
        printable = PRINTABLE_CHARS;
    }
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int lineLength = 0;
    int length = bytes.length;
    
    for (int i = 0; i < length; i++) {
        byte c = bytes[i];
        int b = c;
        if (b < 0) {
            b = 256 + b;
        }
        
        // Check if this is trailing whitespace that needs encoding
        boolean encodeWhitespace = false;
        if (b == ' ' || b == '\t') {
            // Look ahead to see if this is trailing whitespace
            boolean isTrailing = true;
            for (int j = i + 1; j < length; j++) {
                byte next = bytes[j];
                int nb = next < 0 ? 256 + next : next;
                if (nb != ' ' && nb != '\t') {
                    isTrailing = false;
                    break;
                }
            }
            encodeWhitespace = isTrailing;
        }
        
        if (!encodeWhitespace && printable.get(b)) {
            // Safe to add literal character
            int bytesNeeded = 1;
            
            // Check if we need a soft line break
            if (lineLength + bytesNeeded > 76) {
                buffer.write(ESCAPE_CHAR);
                buffer.write('\r');
                buffer.write('\n');
                lineLength = 0;
            }
            
            buffer.write(b);
            lineLength++;
        } else {
            // Need to encode this byte
            int bytesNeeded = 3; // =XX
            
            // Check if we need a soft line break before encoding
            if (lineLength + bytesNeeded > 76) {
                buffer.write(ESCAPE_CHAR);
                buffer.write('\r');
                buffer.write('\n');
                lineLength = 0;
            }
            
            encodeQuotedPrintable(b, buffer);
            lineLength += 3;
        }
    }
    
    return buffer.toByteArray();
}