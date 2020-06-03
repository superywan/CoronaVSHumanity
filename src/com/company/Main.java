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

    //IMAGES
    static final Image PLAYER_IMG = new Image("file:img/nurse.png");
    static final Image SICKPLAYER_IMG = new Image("file:img/nurse_sick.png");
    static final Image SYRINGE1_IMG = new Image("file:img/syringe1.png");
    static final Image SYRINGE2_IMG = new Image("file:img/syringe2.png");
    static final Image VIRUS1_IMG = new Image("file:img/virus1.png");
    static final Image VIRUS2_IMG = new Image("file:img/virus2.png");
    static final Image VIRUS3_IMG = new Image("file:img/virus3.png");
    static final Image VIRUS4_IMG = new Image("file:img/virus4.png");
    static final Image EXPLOSION_IMG = new Image("file:img/explosion.png");
    static final Image EARTH_IMG = new Image("file:img/earth.png");
    static final int EXPLOSION_STEPS = 15;

    //START
    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), e -> run(gc)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        canvas.setCursor(Cursor.MOVE);
        canvas.setOnMouseMoved(e -> mouseX = e.getX());
        canvas.setOnMouseClicked(e -> {
            if(shots.size() < MAX_SHOTS) shots.add(player.shoot());
            if(gameOver) {
                gameOver = false;
                setup();
            }
        });
        setup();
        stage.setScene(new Scene(new StackPane(canvas)));
        stage.setTitle("Corona VS Humanity");
        stage.show();
    }

    //SETUP GAME
    private void setup() {
        shots = new ArrayList<>();
        viruses = new ArrayList<>();
        player = new Nurse(WIDTH / 2, HEIGHT - PLAYER_SIZE, PLAYER_SIZE, PLAYER_IMG);
        score = 0;
        IntStream.range(0, MAX_VIRUS).mapToObj(i -> this.newVirus()).forEach(viruses::add);
    }

    //RUN GRAPHICS
    private void run(GraphicsContext gc) {
        gc.setFill(new ImagePattern(EARTH_IMG, 0, 0, 1, 1, true));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font(20));
        gc.setFill(Color.WHITE);
        gc.fillText("Score: " + score, 60,  20);

        if(gameOver) {
            for( int i = 0; i < viruses.size(); i++ ) {
                viruses.get(i).SPEED = 0;
            }
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, WIDTH, HEIGHT);
            gc.setFont(Font.font(35));
            gc.setFill(Color.WHITE);
            gc.fillText("Game Over \n Your Score is: " + score + " \n Click to play again", WIDTH / 2, HEIGHT /2.5);
        }

        player.update();
        player.draw();
        player.posX = (int) mouseX;

        viruses.stream().peek(MovingObject::update).peek(MovingObject::draw).forEach(e -> {
            if(player.colide(e) && !player.exploding) {
                player.explode();
            }
        });

        for (int i = shots.size() - 1; i >=0 ; i--) {
            Shot shot = shots.get(i);
            if(shot.posY < 0 || shot.toRemove)  {
                shots.remove(i);
                continue;
            }
            shot.update();
            shot.draw();
            for (Virus virus : viruses) {
                if(shot.colide(virus) && !virus.exploding) {
                    score++;
                    virus.explode();
                    shot.toRemove = true;
                }
            }
        }

        for (int i = viruses.size() - 1; i >= 0; i--){
            if(viruses.get(i).destroyed)  {
                viruses.set(i, newVirus());
            }
        }
        gameOver = player.destroyed;
    }

    //ROOT MovingObject
    public class MovingObject {

        int posX, posY, size;
        boolean exploding, destroyed;
        Image img;
        int explosionStep = 0;

        public MovingObject(int posX, int posY, int size, Image image) {
            this.posX = posX;
            this.posY = posY;
            this.size = size;
            img = image;
        }

        public Shot shoot() {
            return new Shot(posX + size / 2 - Shot.size / 2, posY - Shot.size);
        }

        public void update() {
            if(exploding) explosionStep++;
            destroyed = explosionStep > EXPLOSION_STEPS;
        }

        public void draw() {}

        public boolean colide(MovingObject other) {
            int d = distance(this.posX + size / 2, this.posY + size /2,
                    other.posX + other.size / 2, other.posY + other.size / 2);
            return d < other.size / 2 + this.size / 2 ;
        }

        public void explode() {
            exploding = true;
            explosionStep = -1;
        }

    }

    //CHILD Player
    public class Nurse extends MovingObject {
        public Nurse(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
        }

        public void draw() {
            if(exploding) {
                gc.drawImage(SICKPLAYER_IMG,
                        posX, posY, size, size);
            } else {
                gc.drawImage(img, posX, posY, size, size);
            }
        }
    }

    //CHILD Virus
    public class Virus extends MovingObject {

        double SPEED = (score/15) + 1;

        public Virus(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
        }

        public void update() {
            super.update();
            if(!exploding && !destroyed) posY += SPEED;
            if(posY > HEIGHT) destroyed = true;
        }
        public void draw() {
            if(exploding) {
                gc.drawImage(EXPLOSION_IMG,
                        posX, posY, size, size);
            } else {
                gc.drawImage(img, posX, posY, size, size);
            }
        }
    }

    //Shot SYRINGE
    public class Shot {
        public boolean toRemove;
        Image image = SYRINGE1_IMG;
        int posX = (int)(image.getWidth() / 2);
        int posY = 300;
        int speed = 3;
        static final int size = 6;

        public Shot(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
        }

        public void update() {
            posY-=speed;
        }

        public void draw() {
            gc.drawImage(SYRINGE1_IMG, posX - 12 , posY, 30 , 90);
            gc.setFill(Color.TRANSPARENT);
            if ((score >=50 && score<=70) || (score >=100 && score<=120) || (score >=200 && score<=230)) {
                gc.drawImage(SYRINGE2_IMG, posX - 12, posY, 30, 90);
                speed = 7;
                gc.fillRect(posX - 5, posY-10, size+10, size+30);
            } else {
                gc.fillOval(posX, posY, size, size);
            }
        }

        public boolean colide(MovingObject MovingObject) {
            int distance = distance(this.posX + size / 2, this.posY + size / 2,
                    MovingObject.posX + MovingObject.size / 2, MovingObject.posY + MovingObject.size / 2);
            return distance  < MovingObject.size / 2 + size / 2;
        }
    }

    Virus newVirus() {
        if (score >= 0 && score < 20) return new Virus(50 + RANDOM.nextInt(WIDTH - 100), 0, PLAYER_SIZE, VIRUS1_IMG);
        if (score >= 20 && score < 50) return new Virus(50 + RANDOM.nextInt(WIDTH - 100), 0, PLAYER_SIZE, VIRUS2_IMG);
        if (score >= 50 && score <= 80) return new Virus(50 + RANDOM.nextInt(WIDTH - 100), 0, PLAYER_SIZE, VIRUS3_IMG);
        if (score >= 80) return new Virus(50 + RANDOM.nextInt(WIDTH - 100), 0, PLAYER_SIZE, VIRUS4_IMG);
        return null;
    }

    int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }

    public static void main(String[] args) {
        launch();
    }
}
