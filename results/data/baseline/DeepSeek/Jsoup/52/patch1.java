public String getWholeDeclaration() {
    final String decl = this.name;
    if(decl.equals("xml") && attributes.size() > 0 ) {
        StringBuilder sb = new StringBuilder(decl);
        final String version = attributes.get("version");
        if( version != null ) {
            sb.append(" version=\"").append(version).append("\"");
        }
        final String encoding = attributes.get("encoding");
        if( encoding != null ) {
            sb.append(" encoding=\"").append(encoding).append("\"");
        }
        return sb.toString();
    }
    else {
        if(attributes.size() > 0) {
            StringBuilder sb = new StringBuilder(decl);
            for (Attribute attribute : attributes) {
                sb.append(" ").append(attribute.getKey()).append("=\"").append(attribute.getValue()).append("\"");
            }
            return sb.toString();
        } else {
            return decl;
        }
    }
}