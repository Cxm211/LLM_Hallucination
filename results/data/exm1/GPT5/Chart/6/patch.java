public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        // Allow comparison with the superclass type to maintain symmetry
        if (!(obj instanceof ObjectList)) {
            return false;
        }
        return super.equals(obj);

    }