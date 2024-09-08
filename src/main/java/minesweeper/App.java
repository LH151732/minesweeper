package minesweeper;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.Random;

public class App extends PApplet {

    public static final int CELLSIZE = 32;
    public static final int TOPBAR = 64;
    public static int WIDTH = 864;
    public static int HEIGHT = 640;
    public static final int BOARD_WIDTH = WIDTH / CELLSIZE;
    public static final int BOARD_HEIGHT = (HEIGHT - TOPBAR) / CELLSIZE;

    public static final int FPS = 30;
    public static final int DEFAULT_MINES = 100;

    public static Random random = new Random();

    public static int[][] mineCountColour = new int[][]{
            {0, 0, 0},
            {0, 0, 255},
            {0, 133, 0},
            {255, 0, 0},
            {0, 0, 132},
            {132, 0, 0},
            {0, 132, 132},
            {132, 0, 132},
            {32, 32, 32}
    };

    private Tile[][] board;
    private java.util.HashMap<String, PImage> sprites = new java.util.HashMap<>();
    private boolean gameOver;
    private boolean win;
    private int mineCount;
    private int timer;
    private int startTime;

    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    @Override
    public void setup() {
        frameRate(FPS);
        loadSprites();
        initializeBoard();
        resetGame();
    }

    private void loadSprites() {
        String[] spriteNames = {"tile1", "tile2", "flag", "tile"};
        for (String name : spriteNames) {
            getSprite(name);
        }
        for (int i = 0; i < 10; i++) {
            getSprite("mine" + i);
        }
    }

    private void initializeBoard() {
        board = new Tile[BOARD_HEIGHT][BOARD_WIDTH];
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                board[y][x] = new Tile(x, y);
            }
        }
    }

    private void resetGame() {
        gameOver = false;
        win = false;
        mineCount = DEFAULT_MINES;
        timer = 0;
        startTime = millis();

        // Reset all tiles
        for (Tile[] row : board) {
            for (Tile tile : row) {
                tile.reset();
            }
        }

        // Place mines
        int minesPlaced = 0;
        while (minesPlaced < mineCount) {
            int x = random.nextInt(BOARD_WIDTH);
            int y = random.nextInt(BOARD_HEIGHT);
            if (!board[y][x].hasMine()) {
                board[y][x].setMine(true);
                minesPlaced++;
            }
        }
    }

    @Override
    public void draw() {
        background(200, 200, 200);
        drawBoard();
        drawTopBar();
        checkWinCondition();
    }

    private void drawBoard() {
        for (Tile[] row : board) {
            for (Tile tile : row) {
                tile.draw(this);
            }
        }
    }

    private void drawTopBar() {
        fill(150);
        rect(0, 0, WIDTH, TOPBAR);
        
        if (!gameOver) {
            timer = (millis() - startTime) / 1000;
        }
        
        textAlign(RIGHT, CENTER);
        textSize(24);
        fill(255);
        text("Time: " + timer, WIDTH - 10, TOPBAR / 2);

        if (gameOver) {
            textAlign(CENTER, CENTER);
            textSize(30);
            fill(255);
            text(win ? "You win!" : "You lost!", WIDTH / 2, TOPBAR / 2);
        }
    }

    private void checkWinCondition() {
        if (!gameOver) {
            boolean allNonMinesRevealed = true;
            for (Tile[] row : board) {
                for (Tile tile : row) {
                    if (!tile.hasMine() && !tile.isRevealed()) {
                        allNonMinesRevealed = false;
                        break;
                    }
                }
                if (!allNonMinesRevealed) break;
            }
            if (allNonMinesRevealed) {
                gameOver(true);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!gameOver && e.getY() > TOPBAR) {
            int x = e.getX() / CELLSIZE;
            int y = (e.getY() - TOPBAR) / CELLSIZE;
            if (x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT) {
                if (e.getButton() == LEFT) {
                    board[y][x].onClick(this);
                } else if (e.getButton() == RIGHT) {
                    board[y][x].toggleFlag();
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKey() == 'r' || event.getKey() == 'R') {
            resetGame();
        }
    }

    public void gameOver(boolean playerWins) {
        gameOver = true;
        win = playerWins;
        if (!playerWins) {
            revealAllMines();
        }
    }

    private void revealAllMines() {
        for (Tile[] row : board) {
            for (Tile tile : row) {
                if (tile.hasMine()) {
                    tile.reveal();
                }
            }
        }
    }

    public PImage getSprite(String s) {
        return sprites.computeIfAbsent(s, key -> loadImage(this.getClass().getResource(key + ".png").getPath().replace("%20", " ")));
    }

    public Tile[][] getBoard() {
        return this.board;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public static void main(String[] args) {
        PApplet.main("minesweeper.App");
    }
}
