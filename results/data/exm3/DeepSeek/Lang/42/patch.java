    public void escape(Writer writer, String str) throws IOException {
        int len = str.length();
        for (int i = 0; i < len; ) {
            int codePoint = str.codePointAt(i);
            String entityName = null;
            if (codePoint <= Character.MAX_VALUE) {
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
            i += Character.charCount(codePoint);
        }
    }