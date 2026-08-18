public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            TypedValue other = (TypedValue) obj;
            if (value == null) {
                if (other.value != null) return false;
            } else if (!value.equals(other.value)) return false;
            return true;
        }