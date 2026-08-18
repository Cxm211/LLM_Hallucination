public int[] add(ReadablePartial partial, int fieldIndex, int[] values, int valueToAdd) {
        if (valueToAdd == 0) {
            return values;
        }
        return super.add(partial, fieldIndex, values, valueToAdd);
    }