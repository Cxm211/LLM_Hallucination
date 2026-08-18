public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof java.util.List)) {
            return false;
        }
        return super.equals(obj);

    }