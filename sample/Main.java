package sample;

import javafx.scene.input.KeyEvent;

import java.util.Random;

import javafx.application.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.*;
import javafx.geometry.*;
import javafx.scene.control.*;


import javafx.scene.input.KeyCode;

public class Main extends Application {
    MediaPlayer mediaplayer;

    public Main() {
        images = new ImageView[height][width];
    }

    public static void main(String[] args) {
        launch(args); // по сути, запуск метода start
    }


    public void start(Stage primaryStage) {
        options(primaryStage); // первоначальные настройки приложения
        generateMaze(); // создание лабиринта
        showMaze(); // показ лабиринта
        gameProcess(); // начало игрового процесса (управление стрелками и тд.)
        Media musicFile = new Media("file:///C:/Java/IDEA/labirint/src/veryu.mp3");
        mediaplayer = new MediaPlayer(musicFile);
        mediaplayer.setAutoPlay(true);
        mediaplayer.setVolume(0.1);
    }

    //[][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
    //][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
    //[][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][]
    /// ПЕРЕМЕННЫЕ КЛАССА, которые будут доступны во всех методах:
    int height = 35; // высота лабиринта (количество строк)
    int width = 60; // ширина лабиринта (количество столбцов в каждой строке)

    enum GameObject {

        HALL, WALL, CHAR, CASH, ENEMY, PILL, COFFEE // BOMB, ENEMY2, BOSS ...
    }

    GameObject[][] maze = new GameObject[height][width];

    ImageView[][] images; // массив ссылок
    // на элементы управления, на которых будут размещены картинки

    // пути к картинкам
    Image hall = new Image("/img/hall.png");
    Image wall = new Image("/img/wall.png");
    Image character = new Image("/img/char.png");
    Image cash = new Image("/img/cash2.png");
    Image enemy = new Image("/img/enemy.png");
    Image pill = new Image("/img/pill.png");
    Image coffee = new Image("/img/coffee.png");

    GridPane layout; // менеджер компоновки. по сути, это панель, на которую
    // определённым образом выкладываются различные элементы управления

    Stage stage; // ссылка на окно приложения
    Scene scene; // ссылка на клиентскую область окна

    Random r = new Random();

    int smileX = 0;
    int smileY = 2; // стартовая позиция игрового персонажика

    int exitX = 2;
    int exitY = 0;

    int medal = 0;
    int totalMedal = 0;
    int enemyDie = 0;
    int totalEnemy = 0;

    int health = 100;
    int coffeecost = 25;
    int step = 0;// сколько прощел от кофе

    int energy = 500;
    int kickCost = 10;
    boolean wasAtPill = false;
    boolean wasAtCoffe = false;

    public void options(Stage primaryStage) {
        ////////////////////////////////////////////////////////////////////////
        /// настройки окна
        stage = primaryStage;
        stage.setTitle("Java FX Maze"); // установка текста в заголовке окна
        stage.setResizable(false); // размеры окна нельзя будет изменить
        stage.getIcons().add(character); // иконка приложения
        ////////////////////////////////////////////////////////////////////////
        /// настройки панели элементов
        layout = new GridPane(); // элементы будут выкладываться в виде сетки
        layout.setPadding(new Insets(5, 5, 5, 5)); // отступы панели от клиентской части окна
        layout.setStyle("-fx-background-color: rgb(92, 118, 137);"); // фон панели
        // layout.setGridLinesVisible(true); // сделать видимыми границы сетки
        /// жуткая процедура установки количества строк и столбцов панели:
        for (int i = 0; i < height; i++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / height);
            layout.getRowConstraints().add(rowConst);
        }
        for (int i = 0; i < width; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / width);
            layout.getColumnConstraints().add(colConst);
        }
        ////////////////////////////////////////////////////////////////////////
        /// настройка клиентской области окна: элементы кладём на панель, панель -
        // на клиентскую область, клиентскую область - привязываем к окну
        scene = new Scene(layout, 16 * width, 16 * height); // 16 px - размер
        // одной ячейки лабиринта по ширине и по высоте
        stage.setScene(scene);

        ////////////////////////////////////////////////////////////////////////
        // здесь (возможно) будут другие общие настройки
    }

    public void generateMaze() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                maze[y][x] = GameObject.HALL; // изначально, лабиринт пустой

                // в 1 случае из 5 - ставим стену
                if (r.nextInt(5) == 0) {
                    maze[y][x] = GameObject.WALL;
                }

                // в 1 случае из 250 - кладём денежку
                if (r.nextInt(250) == 0) {
                    maze[y][x] = GameObject.CASH;
                }
                if (r.nextInt(150) == 0) {//варим кофе
                    maze[y][x] = GameObject.COFFEE;
                }
                // в 1 случае из 250 - кладём таблетку
                if (r.nextInt(250) == 0) {
                    maze[y][x] = GameObject.PILL;
                }

                // в 1 случае из 250 - размещаем врага
                if (r.nextInt(250) == 0) {
                    maze[y][x] = GameObject.ENEMY;
                }

                // стены по периметру обязательны
                if (y == 0 || x == 0 || y == height - 1 | x == width - 1) {
                    maze[y][x] = GameObject.WALL;
                }

                // наш персонажик
                if (x == smileX && y == smileY) {
                    maze[y][x] = GameObject.CHAR;
                }

                // есть выход, и соседняя ячейка справа всегда свободна
                if (x == smileX + 1 && y == smileY || x == exitX && y == exitY) {
                    maze[y][x] = GameObject.HALL;
                }

            }
        }

        ////////////////////////////////////////////////
        // подсчёт количества различных объектов по их типам
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (maze[y][x] == GameObject.CASH) {
                    totalMedal++;
                }
                if (maze[y][x] == GameObject.ENEMY) {
                    totalEnemy++;
                }
            }
        }

    }

    public void showMaze() {

        Image current;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (maze[y][x] == GameObject.HALL) {
                    current = hall;
                } else if (maze[y][x] == GameObject.WALL) {
                    current = wall;
                } else if (maze[y][x] == GameObject.CHAR) {
                    current = character;
                } else if (maze[y][x] == GameObject.CASH) {
                    current = cash;
                } else if (maze[y][x] == GameObject.PILL) {
                    current = pill;
                } else if (maze[y][x] == GameObject.COFFEE) {
                    current = coffee;
                } else/* if (maze[y][x] == GameObject.ENEMY)*/ {
                    current = enemy;
                }

                images[y][x] = new ImageView(current);
                layout.add(images[y][x], x, y);
                //GridPane.setHalignment(imgView, HPos.CENTER);

            }
        }

        stage.show();
        stage.setTitle("Medals: " + medal + " / " + totalMedal + " Health " + health + " Energy " + energy + "Enemy: " + enemyDie + " / " + totalEnemy);
    }

    public void clearCell(int x, int y) {
        layout.getChildren().remove(images[y][x]);

        if (wasAtPill == false) {
            maze[y][x] = GameObject.HALL; // делаем пустую ячейку по указанной позиции
            images[y][x] = new ImageView(hall);
        } else {
            maze[y][x] = GameObject.PILL;
            images[y][x] = new ImageView(pill);
            if (health < 100) {
                maze[y][x] = GameObject.HALL; // делаем пустую ячейку по указанной позиции
                images[y][x] = new ImageView(hall);
                step = 10;
            }

            layout.add(images[y][x], x, y);
        }

        if (wasAtCoffe == false) {// делаю не кушать кофе
            maze[y][x] = GameObject.HALL; // делаем пустую ячейку по указанной позиции
            images[y][x] = new ImageView(hall);
        } else {
            maze[y][x] = GameObject.COFFEE;
            images[y][x] = new ImageView(coffee);
            if (step > 0) {
                maze[y][x] = GameObject.HALL; // делаем пустую ячейку по указанной позиции
                images[y][x] = new ImageView(hall);

            }

            layout.add(images[y][x], x, y);
        }
    }

    public void setSmile(int x, int y) {
        maze[y][x] = GameObject.CHAR;
        layout.getChildren().remove(images[y][x]);
        images[y][x] = new ImageView(character);
        layout.add(images[y][x], x, y);
    }

    public void gameProcess() {
        scene.setOnKeyPressed((KeyEvent t) -> {


            clearCell(smileX, smileY);

            if (t.getCode() == KeyCode.RIGHT && maze[smileY][smileX + 1] != GameObject.WALL) {
                smileX++;
                energy--;
                step--;
                if (energy <= 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sorry!");
                    alert.setHeaderText("Look, an Information Dialog");
                    alert.setContentText("Поражение – закончилась энергия");

                    alert.showAndWait();

                    System.exit(0);
                }
            } else if (t.getCode() == KeyCode.LEFT && smileX > 0 && maze[smileY][smileX - 1] != GameObject.WALL) {
                smileX--;
                energy--;
                step--;
                if (energy <= 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sorry!");
                    alert.setHeaderText("Look, an Information Dialog");
                    alert.setContentText("Поражение – закончилась энергия");

                    alert.showAndWait();

                    System.exit(0);
                }
            } else if (t.getCode() == KeyCode.UP && smileY > 0 && maze[smileY - 1][smileX] != GameObject.WALL) {
                smileY--;
                energy--;
                step--;
                if (energy <= 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sorry!");
                    alert.setHeaderText("Look, an Information Dialog");
                    alert.setContentText("Поражение – закончилась энергия");

                    alert.showAndWait();

                    System.exit(0);
                }
            } else if (t.getCode() == KeyCode.DOWN && smileY < height - 1 && maze[smileY + 1][smileX] != GameObject.WALL) {
                smileY++;
                energy--;
                step--;
                if (energy <= 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sorry!");
                    alert.setHeaderText("Look, an Information Dialog");
                    alert.setContentText("Поражение – закончилась энергия");

                    alert.showAndWait();

                    System.exit(0);
                }
            } else if (t.getCode() == KeyCode.SHIFT) {//дерется
                if (energy > kickCost) {
                    energy -= kickCost;
                    int radius = 2;
                    for (int y = smileY - radius; y < smileY + radius; y++) {
                        for (int x = smileX - radius; x < smileX + radius; x++) {
                            if (y >= 0 && y < height && x >= 0 && x < width) {
                                if (maze[y][x] == GameObject.ENEMY) {
                                    clearCell(x, y);
                                    enemyDie++;
                                    Media musicFile = new Media("file:///C:/Java/IDEA/labirint/src/Pinok.mp3");
                                    mediaplayer = new MediaPlayer(musicFile);
                                    mediaplayer.setAutoPlay(true);
                                    mediaplayer.setVolume(0.1);
                                }
                            }
                        }
                    }
                }
            }

            if (maze[smileY][smileX] == GameObject.CASH) {
                medal++;
            }
            if (maze[smileY][smileX] == GameObject.ENEMY) {
                enemyDie++;
            }
            if (maze[smileY][smileX] == GameObject.COFFEE) {
                if (step <= 0) {
                    energy += coffeecost;
                }
            }
            wasAtCoffe = (step > 0) ? true : false;
            if (maze[smileY][smileX] == GameObject.ENEMY) {// ВРАГИ
                health -= 20;
            }

            if (maze[smileY][smileX] == GameObject.PILL) {
                wasAtPill = true;
                if (health < 100) {
                    health += 5;

                    if (health > 100)
                        health = 100;
                }
            } else {
                wasAtPill = false;
            }

            setSmile(smileX, smileY);

            if (smileX == exitX && smileY == exitY) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Congratulations!");
                alert.setHeaderText("Look, an Information Dialog");
                alert.setContentText("Вы выиграли, найден выход из лабиринта!");

                alert.showAndWait();

                System.exit(0);
            }
            if (medal == totalMedal) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Congratulations!");
                alert.setHeaderText("Look, an Information Dialog");
                alert.setContentText("Вы выиграли, собрали все медали!");

                alert.showAndWait();

                System.exit(0);
            }
            if (enemyDie == totalEnemy) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Congratulations!");
                alert.setHeaderText("Look, an Information Dialog");
                alert.setContentText("победа - враги уничтожены!");

                alert.showAndWait();

                System.exit(0);
            }

            if (health <= 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sorry!");
                alert.setHeaderText("Look, an Information Dialog");
                alert.setContentText("Поражение - закончилось здоровье");

                alert.showAndWait();

                System.exit(0);
            }
            stage.setTitle("Medals: " + medal + " / " + totalMedal + " Health " + health + " Energy " + energy + "Enemy: " + enemyDie + " / " + totalEnemy);

        });
    }
}


