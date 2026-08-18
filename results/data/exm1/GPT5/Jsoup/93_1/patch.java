public List<Connection.KeyVal> formData() {
        ArrayList<Connection.KeyVal> data = new ArrayList<>();

        // iterate the form control elements and accumulate their values
        for (Element el: elements) {
            if (!el.tag().isFormSubmittable()) continue; // contents are form listable, superset of submitable
            if (el.hasAttr("disabled")) continue; // skip disabled form inputs
            String name = el.attr("name");
            if (name.length() == 0) continue;
            String type = el.attr("type");


            if ("select".equals(el.normalName())) {
                Elements options = el.select("option[selected]");
                boolean isMultiple = el.hasAttr("multiple");
                if (isMultiple) {
                    // for multiple selects, submit all selected options; if none selected, submit nothing
                    for (Element option : options) {
                        data.add(HttpConnection.KeyVal.create(name, option.val()));
                    }
                } else {
                    // single select: submit the first selected option, if any; otherwise the first option
                    Element option = options.first();
                    if (option != null) {
                        data.add(HttpConnection.KeyVal.create(name, option.val()));
                    } else {
                        option = el.select("option").first();
                        if (option != null)
                            data.add(HttpConnection.KeyVal.create(name, option.val()));
                    }
                }
            } else if ("checkbox".equalsIgnoreCase(type) || "radio".equalsIgnoreCase(type)) {
                // only add checkbox or radio if they have the checked attribute
                if (el.hasAttr("checked")) {
                    final String val = el.val().length() >  0 ? el.val() : "on";
                    data.add(HttpConnection.KeyVal.create(name, val));
                }
            } else {
                data.add(HttpConnection.KeyVal.create(name, el.val()));
            }
        }
        return data;
    }