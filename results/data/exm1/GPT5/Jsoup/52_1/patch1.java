public String getWholeDeclaration() {
        final String decl = this.name;
        if (decl.equals("xml") && attributes.size() > 0 ) {
            StringBuilder sb = new StringBuilder(decl);
            for (Attribute attribute : attributes) {
                String key = attribute.getKey();
                String value = attribute.getValue();
                sb.append(" ").append(key).append("=\"").append(value != null ? value : "").append("\"");
            }
            return sb.toString();
        }
        else {
            return this.name;
        }
    }