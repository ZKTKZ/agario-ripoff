import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Wall {
    private double xPos, yPos;
    public int type;
    public Rectangle rect;

    public Wall(double x, double y) {
        xPos = x;
        yPos = y;

        rect = new Rectangle(x, y, 30, 30);
        rect.setX(xPos);
        rect.setY(yPos);
    }

	/* getters and setters are used to
	// get type and center coordinates
  	of Wall object */

    public Shape getRect() { // returns wall's shape
        return rect;
    }

    public double getCenterY() {
        return yPos + 15;
    }

    public double getCenterX() {
        return xPos + 15;
    }

    public int getType() {
        return type;
    }

    public void setType(int newType) {
        type = newType;
    }
}


