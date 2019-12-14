package map;

public class Position {
    public double x;
    public double y;

    public Position() {
        x = 0;
        y = 0;
    }

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Position(Position p) {
        this.x = p.x;
        this.y = p.y;
    }

    public void add(Position p) {
        this.x += p.x;
        this.y += p.y;
    }

    public void subtract(Position p) {
        this.x -= p.x;
        this.y -= p.y;
    }

    public void multiply(double k) {
        this.x *= k;
        this.y *= k;
    }

    public void divide(double k) {
        this.x /= k;
        this.y /= k;
    }

    public void rotate(double v) {
        double x = (this.x * Math.cos(v) - this.y * Math.sin(v));
        double y = (this.x * Math.sin(v) + this.y * Math.cos(v));

        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
