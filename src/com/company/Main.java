/*
Final Team Project - Corona VS Humanity
Group Members:
- Eddy Yi (yi_wan@student.smc.edu)
- Jisoo Kim (kim_jisoo02@student.smc.edu)
- Joshua Gonzalez (gonzalez-hahn_joshua@student.smc.edu)
- Kenji Kano (kano_kenji@student.smc.edu)
- Sebastian Bruno (bruno_sebastian@student.smc.edu)
- Yeeun Min (min_yeeun@student.smc.edu)
 */

package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    //VARIABLES
    private GraphicsContext gc;
    private static final Random RANDOM = new Random();
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private double mouseX;
    MovingObject player;
    private static final int PLAYER_SIZE = 60;
    List<Shot> shots;
    List<Virus> viruses;
    private int score;
    boolean gameOver = false;
    final int MAX_VIRUS = 7;
    final int MAX_SHOTS = MAX_VIRUS * 2;
    static final int EXPLOSION_STEPS = 15;

    //IMAGES
    static final Image PLAYER_IMG = new Image("file:img/nurse_healthy.png");
    static final Image SICKPLAYER_IMG = new Image("file:img/nurse_sick.png");
    static final Image SYRINGE1_IMG = new Image("file:img/syringe_regular.png");
    static final Image SYRINGE2_IMG = new Image("file:img/syringe_upgrade.png");
    static final Image VIRUS1_IMG = new Image("file:img/virus_lv1.png");
    static final Image VIRUS2_IMG = new Image("file:img/virus_lv2.png");
    static final Image VIRUS3_IMG = new Image("file:img/virus_lv3.png");
    static final Image VIRUS4_IMG = new Image("file:img/virus_lv4.png");
    static final Image EXPLOSION_IMG = new Image("file:img/explosion.png");
    static final Image EARTH_IMG = new Image("file:img/earth.png");

    //start Method for JavaFX
    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas(WIDTH, HEIGHT); // New Canvas object
        gc = canvas.getGraphicsContext2D(); // Set gc to canvas
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), e -> run(gc))); // New Timeline object and set KeyFrame
        timeline.setCycleCount(Timeline.INDEFINITE); // timeline cycles indefinitely
        timeline.play(); // Play timeline
        // Mouse Event part
        canvas.setCursor(Cursor.MOVE);
        canvas.setOnMouseMoved(e -> mouseX = e.getX());
        canvas.setOnMouseClicked(e -> {
            if(shots.size() < MAX_SHOTS) shots.add(player.shoot()); // If shots are less than MAX_SHOT, add shot
            if(gameOver) { // If gameOver is true, gameover to false
                gameOver = false;
                setup(); // Set up new game
            }
        });
        setup(); // Set up new game
        stage.setScene(new Scene(new StackPane(canvas))); // Set new StackPane that has canvas to stage
        stage.setTitle("Corona VS Humanity"); // Set title
        stage.show(); // Show stage
    }

    // setup method
    private void setup() {
        shots = new ArrayList<>(); // New ArrayList for shots
        viruses = new ArrayList<>(); // New Array List for viruses
        player = new Nurse(WIDTH / 2, HEIGHT - PLAYER_SIZE, PLAYER_SIZE, PLAYER_IMG); // New Nurse Player
        score = 0; // Set score to 0
        IntStream.range(0, MAX_VIRUS).mapToObj(i -> this.newVirus()).forEach(viruses::add); // Set new viruses to fall down
    }

    //run Grapics
    private void run(GraphicsContext gc) {
        gc.setFill(new ImagePattern(EARTH_IMG, 0, 0, 1, 1, true)); // Set Background to earth image
        gc.fillRect(0, 0, WIDTH, HEIGHT); // Fills a rectangle using the current fill paint.
        gc.setTextAlign(TextAlignment.CENTER); // Set text alignment to center
        gc.setFont(Font.font(20)); // Set font size
        gc.setFill(Color.WHITE); // Set font color
        gc.fillText("Score: " + score, 60,  20); // Fill text that shows score on left top side
        // Game Over part
        if(gameOver) {
            for( int i = 0; i < viruses.size(); i++ ) { // Stop all the viruses when the game over
                viruses.get(i).SPEED = 0; // Set virus speed to 0
            }
            gc.setFill(Color.BLACK); // Set Background to Black
            gc.fillRect(0, 0, WIDTH, HEIGHT); // Fills a rectangle using the current fill paint.
            gc.setFont(Font.font(35)); // Set font size
            gc.setFill(Color.WHITE); // Set font color
            gc.fillText("Game Over \n Your Score is: " + score
                    + " \n Click to play again", WIDTH / 2, HEIGHT /2.5); // Fill text that shows final score on the middle
        }

        player.update(); // Update Nurse player
        player.draw(); // Draw Nurse player
        player.posX = (int) mouseX; // Move Player to the mouse x position

        viruses.stream().peek(MovingObject::update).peek(MovingObject::draw).forEach(e -> {
            if(player.colide(e) && !player.exploding) {
                player.explode();
            }
        });

        for (int i = shots.size() - 1; i >=0 ; i--) {
            Shot shot = shots.get(i);
            if(shot.posY < 0 || shot.toRemove)  { // If Syringe reach to the top, remove it
                shots.remove(i);
                continue;
            }
            shot.update(); // Update Shot shot
            shot.draw(); // Draw Shot shot
            for (Virus virus : viruses) { // For every virus object
                if(shot.colide(virus) && !virus.exploding) { // If virus got shot add score and remove
                    score++;
                    virus.explode();
                    shot.toRemove = true;
                }
            }
        }

        for (int i = viruses.size() - 1; i >= 0; i--){ // If the virus destroyed, add new virus object
            if(viruses.get(i).destroyed)  {
                viruses.set(i, newVirus());
            }
        }
        gameOver = player.destroyed;
    }

    // MovingObject Class (Root class for Nurse and Virus)
    public class MovingObject {
        // Variables
        int posX, posY, size;
        boolean exploding, destroyed;
        Image img;
        int explosionStep = 0;

        // Constructor for MovingObject
        public MovingObject(int posX, int posY, int size, Image image) {
            this.posX = posX;
            this.posY = posY;
            this.size = size;
            img = image;
        }

        // shoot method - empty method for Nurse
        public Shot shoot() {
            return null;
        }

        // update method
        public void update() {
            if(exploding) explosionStep++; // if exploding is true, ++ to explosion
            destroyed = explosionStep > EXPLOSION_STEPS;
        }

        // draw method - empty method for both child classes
        public void draw() {}

        // colide method
        public boolean colide(MovingObject other) { // hit with force when moving.
            int d = distance(this.posX + size / 2, this.posY + size /2,
                    other.posX + other.size / 2, other.posY + other.size / 2);
            return d < other.size / 2 + this.size / 2 ;
        }

        // explode method
        public void explode() {
            exploding = true;
            explosionStep = -1;
        }

    }

    // Nurse class (Child class of MovingObject)
    public class Nurse extends MovingObject {

        // Constructor for Nurse Object
        public Nurse(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
        }

        // shoot method
        public Shot shoot() {
            return new Shot(posX + size / 2 - Shot.size / 2, posY - Shot.size); // Return new Shot Object
        }

        // draw method
        public void draw() {
            if(exploding) { // If Nurse exploding is true, change image to SICKPLAYER_IMG
                gc.drawImage(SICKPLAYER_IMG,
                        posX, posY, size, size);
            } else {
                gc.drawImage(img, posX, posY, size, size); // Else, just use set image
            }
        }
    }

    // Virus class (Child class of MovingObject)
    public class Virus extends MovingObject {

        // Variable
        double SPEED = (score/15) + 1;

        // Constructor for Virus Object
        public Virus(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
        }

        // update method
        public void update() {
            super.update();
            if(!exploding && !destroyed) posY += SPEED; // If Virus object exploding and destroyed is false, add SPEED to posY
            if(posY > HEIGHT) destroyed = true; // If virus reach to the bottom, destroyed true
        }

        // draw method
        public void draw() {
            if(exploding) { // If Virus exploding is true, change image to EXPLOSION_IMG
                gc.drawImage(EXPLOSION_IMG,
                        posX, posY, size, size);
            } else { // Else, just use set image
                gc.drawImage(img, posX, posY, size, size);
            }
        }
    }

    // Shot class
    public class Shot {

        // Variables
        public boolean toRemove;
        Image image = SYRINGE1_IMG;
        int posX = (int)(image.getWidth() / 2);
        int posY = 300;
        int speed = 3;
        static final int size = 6;

        // Constructor for Shot Object
        public Shot(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
        }

        // update method
        public void update() {
            posY-=speed;
        }

        // draw method
        public void draw() {
            gc.drawImage(SYRINGE1_IMG, posX - 12 , posY, 30 , 90); // Draw image to regular syringe
            gc.setFill(Color.TRANSPARENT); // Change color to transparent so that remove the dot on the top of syringe

            // Upgrade Syringe part
            if ((score >=50 && score<=70) || (score >=100 && score<=120)
                    || (score >=200 && score<=230)) { // If one of the conditon is true, change image to upgrade syringe and fast upp the speed
                gc.drawImage(SYRINGE2_IMG, posX - 12, posY, 30, 90);
                speed = 12;
                gc.fillRect(posX - 5, posY-10, size+10, size+30);
            } else { // Else, just use regular syringe
                gc.fillOval(posX, posY, size, size);
            }
        }

        // colide method
        public boolean colide(MovingObject MovingObject) {
            int distance = distance(this.posX + size / 2, this.posY + size / 2,
                    MovingObject.posX + MovingObject.size / 2, MovingObject.posY + MovingObject.size / 2);
            return distance  < MovingObject.size / 2 + size / 2;
        }
    }

    Virus newVirus() { // Add new virus depending on players score
        if (score >= 0 && score < 20) return new Virus(50 + RANDOM.nextInt(WIDTH - 100), 0, PLAYER_SIZE, VIRUS1_IMG);
        if (score >= 20 && score < 50) return new Virus(50 + RANDOM.nextInt(WIDTH - 100), 0, PLAYER_SIZE, VIRUS2_IMG);
        if (score >= 50 && score <= 80) return new Virus(50 + RANDOM.nextInt(WIDTH - 100), 0, PLAYER_SIZE, VIRUS3_IMG);
        if (score >= 80) return new Virus(50 + RANDOM.nextInt(WIDTH - 100), 0, PLAYER_SIZE, VIRUS4_IMG);
        return null;
    }

    // distance method
    int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }

    // main method
    public static void main(String[] args) {
        launch(); // Launch the Applicaiton
    }
}
