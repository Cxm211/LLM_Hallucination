public String getWholeDeclaration() {
        final String decl = this.name;
        if (decl.equals("xml") && attributes.size() > 0) {
            StringBuilder sb = new StringBuilder(decl);
            for (Attribute attribute : attributes) {
                final String key = attribute.getKey();
                final String val = attribute.getValue();
                if (val != null) {
                    sb.append(" ").append(key).append("=\"").append(val).append("\"");
                } else {
                    sb.append(" ").append(key);
                }
            }
            return sb.toString();
        } else {
            return this.name;
        }
    }