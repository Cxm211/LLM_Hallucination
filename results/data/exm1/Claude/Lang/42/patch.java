public void escape(Writer writer, String str) throws IOException {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            String entityName = this.entityName(c);
            if (entityName == null) {
                if (c > 0x7F) {
                    writer.write("&#");
                    if (Character.isHighSurrogate(c)) {
                        if (i + 1 < len) {
                            char low = str.charAt(i + 1);
                            if (Character.isLowSurrogate(low)) {
                                int codePoint = Character.toCodePoint(c, low);
                                writer.write(Integer.toString(codePoint, 10));
                                writer.write(';');
                                i++;
                                continue;
                            }
                        }
                    }
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