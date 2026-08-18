    public void unescape(Writer writer, String string) throws IOException {
        int firstAmp = string.indexOf('&');
        if (firstAmp < 0) {
            writer.write(string);
            return;
        }

        writer.write(string, 0, firstAmp);
        int len = string.length();
        for (int i = firstAmp; i < len; i++) {
            char c = string.charAt(i);
            if (c == '&') {
                int nextIdx = i+1;
                int semiColonIdx = string.indexOf(';', nextIdx);
                if (semiColonIdx == -1) {
                    writer.write(c);
                    continue;
                }
                int amphersandIdx = string.indexOf('&', i + 1);
                if( amphersandIdx != -1 && amphersandIdx < semiColonIdx ) {
                    // Then the text looks like &...&...;
                    writer.write(c);
                    continue;
                }
                String entityContent = string.substring(nextIdx, semiColonIdx);
                int entityValue = -1;
                int entityContentLen = entityContent.length();
                if (entityContentLen > 0) {
                    if (entityContent.charAt(0) == '#') {
                        if (entityContentLen > 1) {  
                            char isHexChar = entityContent.charAt(1);
                            try {
                                if (isHexChar == 'x' || isHexChar == 'X') {
                                    entityValue = Integer.parseInt(entityContent.substring(2), 16);
                                } else {
                                    entityValue = Integer.parseInt(entityContent.substring(1), 10);
                                }
                                if (entityValue < 0 || entityValue > 65535) {
                                    entityValue = -1;
                                }
                            } catch (NumberFormatException e) {
                            }
                        }
                    } else {
                        entityValue = this.entityValue(entityContent);
                    }
                }
                
                if (entityValue == -1) {
                    writer.write('&');
                    writer.write(entityContent);
                    writer.write(';');
                } else {
                    writer.write(entityValue);
                }
                i = semiColonIdx;
            } else {
                writer.write(c);
            }
        }
    }