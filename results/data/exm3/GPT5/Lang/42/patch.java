public void escape(Writer writer, String str) throws IOException {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            int codePoint;
            if (Character.isHighSurrogate(c) && i + 1 < len) {
                char d = str.charAt(i + 1);
                if (Character.isLowSurrogate(d)) {
                    codePoint = Character.toCodePoint(c, d);
                    i++; // consume the low surrogate
                } else {
                    codePoint = c;
                }
            } else {
                codePoint = c;
            }

            String entityName = null;
            if (codePoint <= 0xFFFF) {
                entityName = this.entityName((char) codePoint);
            }

            if (entityName == null) {
                if (codePoint > 0x7F) {
                    writer.write("&#");
                    writer.write(Integer.toString(codePoint, 10));
                    writer.write(';');
                } else {
                    writer.write((char) codePoint);
                }
            } else {
                writer.write('&');
                writer.write(entityName);
                writer.write(';');
            }
        }
    }