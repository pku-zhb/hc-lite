package android.graphics;

/** JVM value stand-in: the SDK mockable jar otherwise discards constructor coordinates. */
public final class Point {
    public int x;
    public int y;
    public Point() {}
    public Point(int x, int y) { this.x = x; this.y = y; }
    @Override public boolean equals(Object other) {
        return other instanceof Point && ((Point) other).x == x && ((Point) other).y == y;
    }
    @Override public int hashCode() { return 31 * x + y; }
    @Override public String toString() { return "Point(" + x + "," + y + ")"; }
}
