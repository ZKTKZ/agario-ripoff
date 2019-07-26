import java.util.ListIterator;
import java.util.concurrent.Callable;
import static java.lang.Math.sqrt;
import static javafx.scene.paint.Color.*;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;


public class Blob {
    private DoubleProperty xVelocity, yVelocity;
    final private ReadOnlyDoubleWrapper init;
    public double radius, decay, xAccel = 0.0, yAccel = 0.0;
    public int type;
    private boolean magnetized;
    protected Circle circle;

    public Blob() {
        init = null;
    }

    public Blob(double centerX, double centerY, double radius, double xVelocity, double yVelocity) {

        this.circle = new Circle(centerX, centerY, radius);
        this.xVelocity = new SimpleDoubleProperty(this, "xVelocity", xVelocity);
        this.yVelocity = new SimpleDoubleProperty(this, "yVelocity", yVelocity);
        this.init = new ReadOnlyDoubleWrapper(this, "init");
        init.bind(Bindings.createDoubleBinding(new Callable<Double>() { // initializes properties i.e. radius, velocity
            @Override
            public Double call() throws Exception {
                final double xVel = getXVelocity();
                final double yVel = getYVelocity();
                return sqrt(xVel * xVel + yVel * yVel);
            }
        }, this.xVelocity, this.yVelocity));
        this.radius = radius;
        circle.setRadius(radius);
    }

    //getters and setters for shape, radius, and velocity
    public Shape getCircle() {
        return circle;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double newRadius) {
        radius = newRadius;
    }

    public double getXVelocity() {
        return xVelocity.get();
    }

    public void setXVelocity(double xVelocity) {
        this.xVelocity.set(xVelocity);
    }

    public double getYVelocity() {
        return yVelocity.get();
    }

    public void setYVelocity(double yVelocity) {
        this.yVelocity.set(yVelocity);
    }

    public void setDecay(double decay) {
        this.decay = decay;
    }

    public DoubleProperty yVelocityProperty() {
        return yVelocity;
    }

    public double getCenterX() {
        return circle.getCenterX();
    }

    public final void setCenterX(double centerX) {
        circle.setCenterX(centerX);
    }

    public double getCenterY() {
        return circle.getCenterY();
    }

    public final void setCenterY(double centerY) {
        circle.setCenterY(centerY);
    }

    public double getDecay() {
        return decay;
    }

    public final int getType() {
        return type;
    }

    public double getxAccel() {
        return xAccel;
    }

    public double getyAccel() {
        return yAccel;
    }

    public void setxAccel(double xAccel) {
        this.xAccel = xAccel;
    }

    public void setyAccel(double yAccel) {
        this.yAccel = yAccel;
    }

    public boolean getMagnetized() {
        return magnetized;
    }

    public double distance(Blob b1) {
        return sqrt(Math.pow(this.getCenterX() - b1.getCenterX(), 2) + Math.pow(this.getCenterY() - b1.getCenterY(), 2));
    }


    public boolean colliding(final Blob b1, final Blob b2, final double deltaX, final double deltaY) { //checks if two blobs have collided
        double rSum = b1.getRadius() + b2.getRadius();
        return Math.pow(deltaX, 2) + Math.pow(deltaY, 2) <= Math.pow(rSum, 2);
    }

    public void consume(Blob b1, Blob b2) { //causes 1 blob to be consumed when two blobs collide
        if (b1.getType() != 6 || b2.getType() != 6) { //food can't consume food
            if (b1.getRadius() > b2.getRadius()) { //the blob with the larger radius consumes the smaller one
                b1.radius = sqrt(Math.pow(b1.getRadius(), 2) + Math.pow(b2.getRadius(), 2));
                b2.radius = 0; //a consumed blob has its radius set to 0, which marks it for deletion when updateWorld() is called
            } else {
                b2.radius = sqrt(Math.pow(b1.getRadius(), 2) + Math.pow(b2.getRadius(), 2));
                b1.radius = 0;
            }
        }
    }

    public boolean colliding(Blob b, Wall w) { //checks if a wall and blob are colliding
        double distance = sqrt(Math.pow(b.getCenterX() - w.getCenterX(), 2) + Math.pow(b.getCenterY() - w.getCenterY(), 2));
        return (distance <= b.getRadius() + 16.8);
    }


    public void checkCollisions(ObservableList<Blob> blobs, ObservableList<Wall> walls, double maxX, double maxY) { // checks collisions, and updates blobs' states accordingly
        for (ListIterator<Blob> slowIt = blobs.listIterator(); slowIt.hasNext(); ) {
            Blob b1 = slowIt.next();

            // check wall collisions:
            double xVel = b1.getXVelocity();
            double yVel = b1.getYVelocity();
            if ((b1.getCenterX() - b1.getRadius() <= 0 && xVel < 0 || (b1.getCenterX() + b1.getRadius()) >= maxX && xVel > 0)) { // x-velocity is inverted to prevent blobs from going out of bounds
                b1.setXVelocity(-xVel);
            }
            if ((b1.getCenterY() - b1.getRadius() <= 0 && yVel < 0) || (b1.getCenterY() + b1.getRadius() >= maxY && yVel > 0)) {
                b1.setYVelocity(-yVel);
            }

            for (Wall w : walls) {
                if (colliding(b1, w)) {
                    if (w.getType() == 0) {
                        b1.setXVelocity(-xVel);
                        b1.setYVelocity(-yVel);
                    } else if (w.getType() == 1) {
                        b1.radius = 0; //kills blob
                        w.setType(-1); //sets type as -1, which marks it for deletion when updateWorld() is called
                    } else {
                        b1.getCircle().setStroke(YELLOW);
                        b1.getCircle().setStrokeWidth(3);
                        b1.magnetized = true;
                        w.setType(-1);
                    }
                }
            }


            for (ListIterator<Blob> fastIt = blobs.listIterator(slowIt.nextIndex()); fastIt.hasNext(); ) {
                Blob b2 = fastIt.next();
                final double deltaX = b2.getCenterX() - b1.getCenterX();
                final double deltaY = b2.getCenterY() - b1.getCenterY();
                if (colliding(b1, b2, deltaX, deltaY)) {
                    consume(b1, b2);
                }
            }
        }
    }
}






