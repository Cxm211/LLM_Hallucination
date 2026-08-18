    public String absUrl(String attributeKey) {
        Validate.notEmpty(attributeKey);

        String relUrl = attr(attributeKey);
        if (!hasAttr(attributeKey)) {
            return ""; // nothing to make absolute with
        } else {
            URL base;
            try {
                try {
                    base = new URL(baseUri);
                } catch (MalformedURLException e) {
                    // the base is unsuitable, but the attribute may be abs on its own, so try that
                    URL abs = new URL(relUrl);
                    return abs.toExternalForm();
                }
                // workaround: java resolves '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' as desired
                URL abs;
                if (relUrl.startsWith("?")) {
                    // Workaround for bug in Java's URL constructor when resolving a query relative to a base with a query
                    String path = base.getPath();
                    if (path == null || path.isEmpty()) {
                        path = "/";
                    }
                    // Construct URL manually preserving the path
                    abs = new URL(base.getProtocol(), base.getAuthority(), path + relUrl);
                } else {
                    abs = new URL(base, relUrl);
                }
                return abs.toExternalForm();
            } catch (MalformedURLException e) {
                return "";
            }
        }
    }