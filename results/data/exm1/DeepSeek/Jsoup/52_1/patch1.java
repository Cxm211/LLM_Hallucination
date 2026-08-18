public String getWholeDeclaration() {
        final String decl = this.name;
        if(decl.equals("xml") && attributes.size() > 0 ) {
            StringBuilder sb = new StringBuilder(decl);
            for (Attribute attribute : attributes) {
                sb.append(" ").append(attribute.getKey()).append("=\"").append(attribute.getValue()).append("\"");
            }
            return sb.toString();
        }
        else {
            return this.name;
        }
    }