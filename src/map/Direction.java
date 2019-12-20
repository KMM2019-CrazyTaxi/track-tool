package map;

public enum Direction {
    STRAIGHT,
    LEFT,
    RIGHT;

    public byte code() {
        return (byte) this.ordinal();
    }

    public Direction reverse() {
        switch(this) {
            case STRAIGHT:
                return STRAIGHT;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            default:
                throw new IllegalArgumentException("DAFUQ? " + this);
        }
    }

    public static Direction fromByte(byte b) {
        if ((b & 0xff) >= Direction.values().length)
            throw new IllegalArgumentException("Given byte is out of range. (" + b + ")");
        return Direction.values()[b];
    }
}
