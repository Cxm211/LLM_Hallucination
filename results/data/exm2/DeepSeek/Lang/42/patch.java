public void escape(Writer writer, String str) throws IOException {
    int len = str.length();
    for (int i = 0; i < len; ) {
        int codePoint = str.codePointAt(i);
        if (Character.isSupplementaryCodePoint(codePoint)) {
            writer.write("&#");
            writer.write(Integer.toString(codePoint, 10));
            writer.write(';');
        } else {
            char c = (char) codePoint;
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
        i += Character.charCount(codePoint);
    }
}