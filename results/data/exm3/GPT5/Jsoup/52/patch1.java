public String getWholeDeclaration() {
        final String decl = this.name;
        if (decl.equals("xml")) {
            if (attributes.size() > 0) {
                StringBuilder sb = new StringBuilder(decl);
                for (Attribute attribute : attributes) {
                    String key = attribute.getKey();
                    String value = attribute.getValue();
                    if (value != null) {
                        sb.append(" ").append(key).append("=\"").append(value).append("\"");
                    }
                }
                return sb.toString();
            } else {
                return this.name;
            }
        } else {
            return this.name;
        }
    }