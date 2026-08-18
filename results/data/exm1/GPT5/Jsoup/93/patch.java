public List<Connection.KeyVal> formData() {
        ArrayList<Connection.KeyVal> data = new ArrayList<>();

        // iterate the form control elements and accumulate their values
        for (Element el: elements) {
            if (!el.tag().isFormSubmittable()) continue; // contents are form listable, superset of submitable
            if (el.hasAttr("disabled")) continue; // skip disabled form inputs

            // get the first occurrence of the name attribute, if duplicated
            String name = null;
            for (Attribute attr : el.attributes()) {
                if (attr.getKey().equalsIgnoreCase("name")) { name = attr.getValue(); break; }
            }
            if (name == null || name.length() == 0) continue;

            String type = el.attr("type");

            if ("select".equals(el.normalName())) {
                Elements options = el.select("option[selected]");
                boolean set = false;
                for (Element option: options) {
                    data.add(HttpConnection.KeyVal.create(name, option.val()));
                    set = true;
                }
                if (!set) {
                    Element option = el.select("option").first();
                    if (option != null)
                        data.add(HttpConnection.KeyVal.create(name, option.val()));
                }
            } else if ("checkbox".equalsIgnoreCase(type) || "radio".equalsIgnoreCase(type)) {
                // only add checkbox or radio if they have the checked attribute
                if (el.hasAttr("checked")) {
                    final String val = el.val().length() >  0 ? el.val() : "on";
                    data.add(HttpConnection.KeyVal.create(name, val));
                }
            } else {
                // skip non-successful input types like button
                if ("input".equals(el.normalName())) {
                    String t = type == null ? "" : type;
                    if ("button".equalsIgnoreCase(t)) continue;
                }
                // prefer the first occurrence of the value attribute if duplicated (for inputs)
                String value;
                if ("input".equals(el.normalName())) {
                    String firstVal = null;
                    for (Attribute attr : el.attributes()) {
                        if (attr.getKey().equalsIgnoreCase("value")) { firstVal = attr.getValue(); break; }
                    }
                    value = firstVal != null ? firstVal : el.val();
                } else {
                    value = el.val();
                }
                data.add(HttpConnection.KeyVal.create(name, value));
            }
        }
        return data;
    }