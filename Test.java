/*import java.util.ListIterator;
import java.util.concurrent.Callable;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import java.util.Random;

import static javafx.scene.paint.Color.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

public class BlobSim extends Application { // The 'entry-point' for a JAVAFX file

    private ObservableList<Blob> blobs = FXCollections.observableArrayList(); // list of blobs with listeners attached
    private ObservableList<Wall> walls = FXCollections.observableArrayList(); // list of walls with listeners attached

    private static final double MIN_RADIUS = 10; // lower bound on blob size
    private static final double MIN_SPEED = 20;
    private static final double MAX_SPEED = 250;
    private int timeSpeed = 2, autoType = 100, gravityDirection = 4; // default timeSpeed, autoType (off), and gravity direction (off)

    @Override
    public void start(Stage primaryStage) {   // Every JavaFX Application MUST have a start method
        primaryStage.setTitle("Blob Simulator");
        primaryStage.setResizable(false);

        final Pane blobContainer = new Pane(); // A container for the blobs
        final BorderPane root = new BorderPane(); // The 'super' container for all other containers, objects in the sim

        Button resetButton = new Button("Reset"); // button to restart simulation
        resetButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) { // triggered by mouse action
                blobs.clear(); //resets the simulation
                walls.clear();
                autoType = 100; //turns off autogeneration
            }
        });

        String [] gravityChoices = {"Up", "Left", "Down", "Right", "Off"}; //choices for the direction of gravity
        ComboBox gravityButton  = new ComboBox(); // a drag-down button is added for gravity
        for (String gravityChoice : gravityChoices) {
            gravityButton.getItems().add(gravityChoice);
        }
        gravityButton.setPromptText("Gravity");

        gravityButton.setOnAction((e)-> { //determines the direction of gravity
            if (gravityButton.getValue().equals(gravityChoices[0]))
                gravityDirection = 0;
            else if (gravityButton.getValue().equals(gravityChoices[1]))
                gravityDirection = 1;
            else if (gravityButton.getValue().equals(gravityChoices[2]))
                gravityDirection = 2;
            else if (gravityButton.getValue().equals(gravityChoices[3]))
                gravityDirection = 3;
            else if (gravityButton.getValue().equals(gravityChoices[4])) //no gravity
                gravityDirection = 4;
        });

        String[] choices = {"Blob", "Fast Blob", "Slow Blob", "Sonic", "Blub", "Chaser", "Food", "Random"}; //the entities that can be added to the simulation
        ComboBox blobButton = new ComboBox();
        for (String choice : choices) {
            blobButton.getItems().add(choice);
        }
        blobButton.setValue("Blobs");

        String [] obstacle = {"Wall", "Bomb"};
        ComboBox obstacleButton = new ComboBox();
        for (String o : obstacle)
            obstacleButton.getItems().add(o);
        obstacleButton.setValue("Obstacles");


        ToggleButton autoButton = new ToggleButton("Auto"); //toggles the autogenerator
        autoButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                if (autoButton.isSelected()) { //generates the selected type
                    autoButton.setStyle("-fx-focus-color: deepskyblue;");
                    if (blobButton.getValue().equals(choices[0])) //blob
                        autoType = 0;
                    else if (blobButton.getValue().equals(choices[1])) //fast blob
                        autoType = 1;
                    else if (blobButton.getValue().equals(choices[2])) //slow blob
                        autoType = 2;
                    else if (blobButton.getValue().equals(choices[3])) //sonic
                        autoType = 3;
                    else if (blobButton.getValue().equals(choices[4])) //blub
                        autoType = 4;
                    else if (blobButton.getValue().equals(choices[5])) //chaser
                        autoType = 5;
                    else if (blobButton.getValue().equals(choices[7])) //random
                        autoType = 7;
                }
                else{
                    autoType = 100; //disables the autogenerator
                    autoButton.setStyle("-fx-focus-color: transparent;");
                }
            }
        });

        String[] timeSpeeds = {"x0.2", "x0.5", "x1", "x2", "x5"}; //timescales that the user can choose
        ComboBox changeTime = new ComboBox();
        for (String timeSpeed1 : timeSpeeds) changeTime.getItems().add(timeSpeed1);
        changeTime.setPromptText("Timescale");

        changeTime.setOnAction((e) -> { //gets user's choice from the changeTime combobox
            if (changeTime.getValue().equals(timeSpeeds[0]))
                timeSpeed = 0;
            else if (changeTime.getValue().equals(timeSpeeds[1]))
                timeSpeed = 1;
            if (changeTime.getValue().equals(timeSpeeds[2]))
                timeSpeed = 2;
            if (changeTime.getValue().equals(timeSpeeds[3]))
                timeSpeed = 3;
            if (changeTime.getValue().equals(timeSpeeds[4]))
                timeSpeed = 4;
        });

        constrainBlobsOnResize(blobContainer);
        blobContainer.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() { //adds a blob of the selected type
            @Override
            public void handle(MouseEvent event) {
                if (blobButton.getValue().equals(choices[0])) //blob
                    createBlob(MIN_RADIUS, MIN_SPEED, MAX_SPEED, event.getX(), event.getY(), 0);
                else if (blobButton.getValue().equals(choices[1])) //fast blob
                    createBlob(MIN_RADIUS, MIN_SPEED, MAX_SPEED, event.getX(), event.getY(), 1);
                else if (blobButton.getValue().equals(choices[2])) //slow blob
                    createBlob(MIN_RADIUS, MIN_SPEED, MAX_SPEED, event.getX(), event.getY(), 2);
                else if (blobButton.getValue().equals(choices[3])) //sonic
                    createBlob(MIN_RADIUS, MIN_SPEED, MAX_SPEED, event.getX(), event.getY(), 3);
                else if (blobButton.getValue().equals(choices[4])) //blub
                    createBlob(MIN_RADIUS, MIN_SPEED, MAX_SPEED, event.getX(), event.getY(), 4);
                else if (blobButton.getValue().equals(choices[5])) //chaser
                    createBlob(MIN_RADIUS, MIN_SPEED, MAX_SPEED, event.getX(), event.getY(), 5);
                else if (blobButton.getValue().equals(choices[6])) //food
                    createBlob(MIN_RADIUS, MIN_SPEED, MAX_SPEED, event.getX(), event.getY(), 6);
                else if (blobButton.getValue().equals(choices[7])) //adds a random blob
                    createBlob(MIN_RADIUS, MIN_SPEED, MAX_SPEED, event.getX(), event.getY(), (int)(7*Math.random()));
                else if (obstacleButton.getValue().equals(obstacle[0])) //draws a wall
                    createWall(event.getX()-15,event.getY()-15,0);
                else if (obstacleButton.getValue().equals(obstacle[1])) //draws a bomb
                    createWall(event.getX()-15,event.getY()-15,1);
            }
        });

        blobs.addListener(new ListChangeListener<Blob>() {
            @Override
            public void onChanged(Change<? extends Blob> change) {
                while (change.next()) {
                    for (Blob b : change.getAddedSubList()) {
                        blobContainer.getChildren().add(b.getView());
                    }
                    for (Blob b : change.getRemoved()) {
                        blobContainer.getChildren().remove(b.getView());
                    }
                }
            }
        });

        walls.addListener(new ListChangeListener<Wall>() {
            @Override
            public void onChanged(Change<? extends Wall> change) {
                while (change.next()) {
                    for (Wall w : change.getAddedSubList()) {
                        blobContainer.getChildren().add(w.getView());
                    }
                    for (Wall w : change.getRemoved()) {
                        blobContainer.getChildren().remove(w.getView());
                    }
                }
            }
        });

        HBox buttons = new HBox(); //puts all the components onto a horizontal line
        buttons.setSpacing(5);
        buttons.setPadding(new Insets(0, 10, 10, 10));
        buttons.getChildren().addAll(resetButton, blobButton, obstacleButton, gravityButton, autoButton, changeTime);

        root.setCenter(blobContainer); //adds container
        root.setBottom(buttons); //adds buttons

        final Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        startAnimation(blobContainer);
    }

    private void startAnimation(final Pane blobContainer) {
        final LongProperty lastUpdateTime = new SimpleLongProperty(0);
        final AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long currentTime) {
                if (lastUpdateTime.get() > 0) {
                    long elapsedTime = currentTime - lastUpdateTime.get();
                    checkCollisions(blobContainer.getWidth(), blobContainer.getHeight());
                    updateWorld(elapsedTime,timeSpeed);
                    updateFood();
                }
                lastUpdateTime.set(currentTime);
            }
        };
        timer.start();
    }

    private void updateWorld(long elapsedTime, int timeSpeed) {
        double elapsedSeconds = elapsedTime/5_000_000_000.0;
        if (timeSpeed == 1)
            elapsedSeconds = elapsedTime / 2_000_000_000.0;
        else if (timeSpeed == 2)
            elapsedSeconds = elapsedTime/1_000_000_000.0;
        else if (timeSpeed == 3)
            elapsedSeconds = elapsedTime/5_000_000_00.0;
        else if (timeSpeed == 4)
            elapsedSeconds = elapsedTime/2_000_000_00.0;

        if (autoType!=100 && blobs.size() <= 60)
            generate(autoType);
        //**************************************** MYSTERY EXCEPTION ***************************************************
        try {
            for (Blob b : blobs) {

                b.radius *= b.getDecay(); //radius decays
                b.view.setRadius(b.radius);

                if (b.getType() == 5) {
                    int target = -1;
                    double angle, accel = 600;

                    for (int i = 0; i<=blobs.size()-1; i++) {
                        if (!(blobs.get(i).getRadius() == 0 || blobs.get(i).getType() == 6)) {
                            target = i;
                            break;
                        }
                    }
                    if (target == -1) {
                        angle = 0;
                        accel = 0;
                    }
                    else {
                        angle = Math.atan2(blobs.get(target).getCenterY() - b.getCenterY(), blobs.get(target).getCenterX() - b.getCenterX());
                    }

                    b.setxAccel(accel*cos(angle));
                    b.setyAccel(accel*sin(angle));

                    if (gravityDirection == 0) { //up
                        b.setxAccel(b.getxAccel());
                        b.setyAccel((-500 + b.getyAccel()));
                    } else if (gravityDirection == 1) { //left
                        b.setxAccel((-500 + b.getxAccel()));
                        b.setyAccel(b.getyAccel());
                    } else if (gravityDirection == 2) { //down
                        b.setxAccel(b.getxAccel());
                        b.setyAccel((500 + b.getyAccel()));
                    } else if (gravityDirection == 3) { //right
                        b.setxAccel((500 + b.getxAccel()));
                        b.setyAccel(b.getyAccel());
                    }
                    else {
                        b.setxAccel(b.getxAccel());
                        b.setyAccel(b.getyAccel());
                    }

                    b.setXVelocity(b.getXVelocity() + b.getxAccel() * elapsedSeconds); //accelerate towards the target
                    b.setYVelocity(b.getYVelocity() + b.getyAccel() * elapsedSeconds);
                    b.setCenterX(b.getCenterX() + elapsedSeconds * b.getXVelocity());
                    b.setCenterY(b.getCenterY() + elapsedSeconds * b.getYVelocity());
                } else {
                    if (gravityDirection == 0) { //up
                        b.setxAccel(0);
                        b.setyAccel(-500);
                    } else if (gravityDirection == 1) { //left
                        b.setxAccel(-500);
                        b.setyAccel(0);
                    } else if (gravityDirection == 2) { //down
                        b.setxAccel(0);
                        b.setyAccel(500);
                    } else if (gravityDirection == 3) { //right
                        b.setxAccel(500);
                        b.setyAccel(0);
                    }
                    else {
                        b.setxAccel(0);
                        b.setyAccel(0);
                    }

                    b.setXVelocity(b.getXVelocity() + b.getxAccel() * elapsedSeconds); //apply acceleration in the corresponding direction
                    b.setYVelocity(b.getYVelocity() + b.getyAccel() * elapsedSeconds);
                    b.setCenterX(b.getCenterX() + elapsedSeconds * b.getXVelocity()); // change in distance = time * velocity
                    b.setCenterY(b.getCenterY() + elapsedSeconds * b.getYVelocity());
                }

                if (b.getRadius() < 5) // blobs die when their radius decays to less than 5
                    blobs.remove(b);
                else if (b.getRadius() > 75)
                    b.setDecay(b.decay / 1.00005);
            }

            for (Wall w : walls) { //removes bombs that have exploded
                if (w.getType() == -1)
                    walls.remove(w);
            }
        } catch (Exception e) {
            System.out.println("oh no"); //oh no
        }
    }

    private void updateFood() { //updates food
        int countFood = 0;
        for (Blob b : blobs) { //counts the number food particles onscreen
            if (b.getType() == 6) {
                countFood++;
            }
        }

        while (countFood < 45) { //continually adds new food particles until there are 45
            createBlob(5, 0, 0, 990 * Math.random() + 5, 590 * Math.random() + 5, 6);
            countFood++;
        }
    }

    private void constrainBlobsOnResize(final Pane blobContainer) {
        blobContainer.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.doubleValue() < oldValue.doubleValue()) {
                    for (Blob b : blobs) {
                        double max = newValue.doubleValue() - b.getRadius();
                        if (b.getCenterX() > max) {
                            b.setCenterX(max);
                        }
                    }
                }
            }
        });

        blobContainer.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue, Number newValue) {
                if (newValue.doubleValue() < oldValue.doubleValue()) {
                    for (Blob b : blobs) {
                        double max = newValue.doubleValue() - b.getRadius();
                        if (b.getCenterY() > max) {
                            b.setCenterY(max);
                        }
                    }
                }
            }

        });
    }

    //*************************************************** BLOB CLASS ***************************************************

    private static class Blob {
        private DoubleProperty xVelocity;
        private DoubleProperty yVelocity;
        private final ReadOnlyDoubleWrapper speed;
        private double radius, decay, xAccel, yAccel;
        private int type;
        private Circle view;

        public Blob(double centerX, double centerY, double radius, double xVelocity, double yVelocity) {

            this.view = new Circle(centerX, centerY, radius);
            this.xVelocity = new SimpleDoubleProperty(this, "xVelocity", xVelocity);
            this.yVelocity = new SimpleDoubleProperty(this, "yVelocity", yVelocity);
            this.speed = new ReadOnlyDoubleWrapper(this, "speed");
            speed.bind(Bindings.createDoubleBinding(new Callable<Double>() {

                @Override
                public Double call() throws Exception {
                    final double xVel = getXVelocity();
                    final double yVel = getYVelocity();
                    return sqrt(xVel * xVel + yVel * yVel);
                }
            }, this.xVelocity, this.yVelocity));
            this.radius = radius;
            view.setRadius(radius);
        }

        //getters and setters
        public double getRadius() {
            return radius;
        }

        public double getXVelocity() {
            return xVelocity.get();
        }

        public void setXVelocity(double xVelocity) {
            this.xVelocity.set(xVelocity);
        }

        public DoubleProperty xVelocityProperty() {
            return xVelocity;
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

        public double getSpeed() {
            return speed.get();
        }

        public ReadOnlyDoubleProperty speedProperty() {
            return speed.getReadOnlyProperty();
        }

        public double getCenterX() {
            return view.getCenterX();
        }

        public final void setCenterX(double centerX) {
            view.setCenterX(centerX);
        }

        public final DoubleProperty centerXProperty() {
            return view.centerXProperty();
        }

        public  double getCenterY() {
            return view.getCenterY();
        }

        public final void setCenterY(double centerY) {
            view.setCenterY(centerY);
        }

        public final DoubleProperty centerYProperty() {
            return view.centerYProperty();
        }

        public Shape getView() {
            return view;
        }

        public double getDecay() {
            return decay;
        }

        public final int getType() {
            return type;
        }

        public double getxAccel () {
            return xAccel;
        }

        public double getyAccel () {
            return yAccel;
        }

        public void setxAccel (double xAccel) {
            this.xAccel = xAccel;
        }

        public void setyAccel (double yAccel) {
            this.yAccel = yAccel;
        }
    }

    public boolean colliding(final Blob b1, final Blob b2, final double deltaX, final double deltaY) { //checks if two blobs have collided
        double rSum = b1.getRadius() + b2.getRadius();
        return Math.pow(deltaX, 2) + Math.pow(deltaY, 2) <= Math.pow(rSum, 2);
    }

    private void consume(Blob b1, Blob b2) { //causes 1 blob to be consumed when two blobs collide
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

    public boolean colliding (Blob b, Wall w) { //checks if a wall and blob are colliding
        double distance = sqrt(Math.pow(b.getCenterX()-w.getCenterX(),2) + Math.pow(b.getCenterY()-w.getCenterY(),2));
        return(distance <= b.getRadius() + 16.8);
    }


    private void checkCollisions(double maxX, double maxY) { // checks collisions, and updates blobs' states accordingly
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

            for (Wall w: walls) {
                if (colliding(b1, w)) {
                    if (w.getType()==0) {
                        b1.setXVelocity(-xVel);
                        b1.setYVelocity(-yVel);
                    }
                    else {
                        b1.radius = 0; //kills blob
                        w.type = -1; //sets type as -1, which marks it for deletion when updateWorld() is called
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

    private void createBlob(double minRadius, double minSpeed, double maxSpeed, double initialX, double initialY, int type) { //creates a blob
        final Random rng = new Random();
        final double angle = 2 * PI * rng.nextDouble();

        if (type == 0) { //default (green) blob
            double radius = 1.2 * minRadius + 10 * rng.nextDouble();

            final double speed = minSpeed + (maxSpeed - minSpeed) * rng.nextDouble();
            Blob blob = new Blob(initialX, initialY, radius, speed * cos(angle), speed * sin(angle));
            blob.getView().setFill(GREEN);
            blob.decay = 0.9995;
            blob.type = 0;

            blob.xAccel = 0;
            blob.yAccel = 0;

            blobs.add(blob);
        } else if (type == 1) { //fast (red) blob
            double radius = minRadius + 5 * rng.nextDouble();

            final double speed = 2.5 * minSpeed + 1.75 * (maxSpeed - minSpeed) * rng.nextDouble();
            Blob blob = new Blob(initialX, initialY, radius, speed * cos(angle), speed * sin(angle));
            blob.getView().setFill(RED);
            blob.decay = 0.9999;
            blob.type = 1;

            blob.xAccel = 0;
            blob.yAccel = 0;

            blobs.add(blob);
        } else if (type == 2) { //slow (orange) blob
            double radius = 1.5 * minRadius + 20 * rng.nextDouble();

            final double speed = 0.75 * minSpeed + 0.5 * (maxSpeed - minSpeed) * rng.nextDouble();
            Blob blob = new Blob(initialX, initialY, radius, speed * cos(angle), speed * sin(angle));
            blob.getView().setFill(ORANGE);
            blob.decay = 0.9975;
            blob.type = 2;

            blob.xAccel = 0;
            blob.yAccel = 0;

            blobs.add(blob);
        } else if (type == 3) { //super fast (blue) blob
            double radius = 20;

            final double speed = 2000;
            Blob blob = new Blob(initialX, initialY, radius, speed * cos(angle), speed * sin(angle));
            blob.decay = 0.99;
            blob.getView().setFill(BLUE);
            blob.type = 3;

            blob.xAccel = 0;
            blob.yAccel = 0;

            blobs.add(blob);
        }
        else if (type == 4) { //super slow (magenta) blob
            double radius = 35;

            final double speed = 0.5*minSpeed+10*rng.nextDouble()-5;
            Blob blob = new Blob(initialX, initialY, radius, speed * cos(angle), speed * sin(angle));
            blob.decay = 0.99999;
            blob.getView().setFill(MAGENTA);
            blob.type = 4;

            blob.xAccel = 0;
            blob.yAccel = 0;

            blobs.add(blob);
        }
        else if (type == 5) { //Chaser
            double radius = 40;

            Blob blob = new Blob(initialX, initialY, radius, cos(angle), sin(angle));
            blob.getView().setFill(GREY);
            blob.decay = 0.995;
            blob.type = 5;
            blobs.add(blob);
        }
        else if (type == 6) { //food
            double radius = 5;

            Blob blob = new Blob(initialX, initialY, radius, 0, 0);
            blob.getView().setFill(YELLOWGREEN);
            blob.decay = 1;

            blob.xAccel = 0;
            blob.yAccel = 0;

            blob.type = 6;
            blobs.add(blob);
        }
    }

    public void generate (int type) { //automatically generates blobs of a specified type
        createBlob(MIN_RADIUS, MIN_SPEED, MAX_SPEED, 950*Math.random()+20, 550*Math.random()+20, type);
        if (type==7) //randomly generates blobs
            createBlob(MIN_RADIUS, MIN_SPEED, MAX_SPEED, 950*Math.random()+20, 550*Math.random()+20, (int)(5*Math.random()));
    }

    private static class Wall {
        private double xPos, yPos;
        private int type;
        private Rectangle view;

        public Wall (double x, double y) {
            xPos=x;
            yPos=y;

            view = new Rectangle(x,y,30,30);
            view.setX(xPos);
            view.setY(yPos);
        }

        private Shape getView () {
            return view;
        }

        private double getCenterY () {
            return yPos+15;
        }

        private double getCenterX () {
            return xPos+15;
        }

        private int getType() {
            return type;
        }
    }

    private void createWall (double x, double y, int type) {
        Wall w = new Wall(x, y);
        if (type == 0) { //regular
            w.type = 0;

            w.view.setFill(WHITE);
            w.view.setStroke(GRAY);
            w.view.setStrokeWidth(3);
            walls.add(w);
        }
        else if (type == 1) { //bomb
            w.type=1;

            w.view.setFill(BLACK);
            walls.add(w);
        }
    }

    //*************************************************** MAIN ***************************************************
    public static void main(String[] args) {
        launch(args);
    }
}*/