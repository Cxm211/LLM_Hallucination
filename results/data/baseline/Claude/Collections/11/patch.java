private void calculateHashCode(Object[] keys)
	{
		int total = 17;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != null) {
                total = total * 31 + keys[i].hashCode();
            }
        }
        hashCode = total;
	}