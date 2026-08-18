public DocumentType(String name, String publicId, String systemId, String baseUri) {
        super(baseUri);

        Validate.notEmpty(name);
        attr("name", name);
        if (publicId != null)
            attr("publicId", publicId);
        if (systemId != null)
            attr("systemId", systemId);
    }