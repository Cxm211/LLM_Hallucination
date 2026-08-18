public void escape(Writer writer, String str) throws IOException {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            // Handle surrogate pairs to correctly output a single code point
            if (Character.isHighSurrogate(c) && i + 1 < len) {
                char c2 = str.charAt(i + 1);
                if (Character.isLowSurrogate(c2)) {
                    int codePoint = Character.toCodePoint(c, c2);
                    writer.write("&#");
                    writer.write(Integer.toString(codePoint, 10));
                    writer.write(';');
                    i++; // skip the low surrogate
                    continue;
                }
            }
            String entityName = this.entityName(c);
            if (entityName == null) {
                if (c > 0x7F) {
                    writer.write("&#");
                    writer.write(Integer.toString(c, 10));
                    writer.write(';');
                } else {
                    writer.write(c);
                }
            } else {
                writer.write('&');
                writer.write(entityName);
                writer.write(';');
            }
        }
    }