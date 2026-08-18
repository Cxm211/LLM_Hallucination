public String absUrl(String attributeKey) {
        Validate.notEmpty(attributeKey);

        if (!hasAttr(attributeKey)) {
            return ""; // nothing to make absolute with
        }

        String relUrl = attr(attributeKey);
        if (relUrl == null || relUrl.isEmpty()) {
            return "";
        }

        try {
            URL base = new URL(baseUri);
            URL abs = new URL(base, relUrl);
            return abs.toExternalForm();
        } catch (MalformedURLException e) {
            try {
                URL abs = new URL(relUrl);
                return abs.toExternalForm();
            } catch (MalformedURLException e2) {
                return "";
            }
        }
    }