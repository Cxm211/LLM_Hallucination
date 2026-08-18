public void escape(Writer writer, String str) throws IOException {
        if (str == null) {
            return;
        }
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            String entityName = this.entityName(c);
            if (entityName == null) {
                if (c > 0x7F) {
                    if (Character.isHighSurrogate(c) && i + 1 < len) {
                        char d = str.charAt(i + 1);
                        if (Character.isLowSurrogate(d)) {
                            int codePoint = Character.toCodePoint(c, d);
                            writer.write("&#");
                            writer.write(Integer.toString(codePoint, 10));
                            writer.write(';');
                            i++; // consume low surrogate
                            continue;
                        }
                    }
                    if (Character.isLowSurrogate(c)) {
                        // Unmatched low surrogate; fall back to numeric reference of the char value
                        writer.write("&#");
                        writer.write(Integer.toString(c, 10));
                        writer.write(';');
                    } else {
                        writer.write("&#");
                        writer.write(Integer.toString(c, 10));
                        writer.write(';');
                    }
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