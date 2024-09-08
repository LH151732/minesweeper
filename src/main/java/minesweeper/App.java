package minesweeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class App extends PApplet {

  public static final int CELLSIZE = 32;
  public static final int TOPBAR = 64;
  public static int WIDTH = 864;
  public static int HEIGHT = 640;
  public static final int BOARD_WIDTH = WIDTH / CELLSIZE;
  public static final int BOARD_HEIGHT = (HEIGHT - TOPBAR) / CELLSIZE;

  public static final int FPS = 30;
  public static final int DEFAULT_MINES = 100;
  private int mineCount;

  public static Random random = new Random();

  public static int[][] mineCountColour =
      new int[][] {
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
  private HashMap<String, PImage> sprites = new HashMap<>();
  private boolean gameOver;
  private boolean win;
  private int timer;
  private int startTime;
  private List<Tile> minesToExplode = new ArrayList<>();
  private Tile initialExplodedMine;
  private int explosionRadius = 0;
  private static final int MAX_EXPLOSION_RADIUS = Math.max(BOARD_WIDTH, BOARD_HEIGHT);
  private static final int EXPLOSION_SPEED = 2; // 每帧爆炸半径增加的速度

  @Override
  public void settings() {
    size(WIDTH, HEIGHT);
  }

  @Override
  public void setup() {
    frameRate(FPS);
    loadSprites();
    initializeBoard();
    setMineCount(args);
    resetGame();
  }

  private void loadSprites() {
    String[] spriteNames = {"tile1", "tile2", "flag", "tile", "wall0"};
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
    timer = 0;
    startTime = millis();
    minesToExplode.clear();

    for (Tile[] row : board) {
      for (Tile tile : row) {
        tile.reset();
      }
    }

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

  private void revealMinesInRadius() {
    int centerX = initialExplodedMine.getX();
    int centerY = initialExplodedMine.getY();

    for (int y = 0; y < BOARD_HEIGHT; y++) {
      for (int x = 0; x < BOARD_WIDTH; x++) {
        Tile tile = board[y][x];
        if (tile.hasMine() && !tile.isRevealed()) {
          int dx = x - centerX;
          int dy = y - centerY;
          double distance = Math.sqrt(dx * dx + dy * dy);
          if (distance <= explosionRadius) {
            tile.reveal();
            tile.startExplosion();
          }
        }
      }
    }
  }

  private void updateExplosion() {
    if (initialExplodedMine == null) {
      return;
    }

    if (explosionRadius < MAX_EXPLOSION_RADIUS) {
      explosionRadius += EXPLOSION_SPEED;
      revealMinesInRadius();
    }
  }

  @Override
  public void draw() {
    background(200, 200, 200);
    drawBoard();
    drawTopBar();
    checkWinCondition();

    if (gameOver && !win) {
      updateExplosion();
    }
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

  private void revealAllMines() {
    if (minesToExplode.isEmpty()) {
      for (Tile[] row : board) {
        for (Tile tile : row) {
          if (tile.hasMine()) {
            minesToExplode.add(tile);
          }
        }
      }
    } else {
      int currentFrame = frameCount / 3;
      if (currentFrame < minesToExplode.size()) {
        Tile currentTile = minesToExplode.get(currentFrame);
        currentTile.reveal();
        if (currentTile.hasMine()) {
          currentTile.onClick(this);
        }
      }
    }
  }

  public void setMineCount(String[] args) {
    mineCount = DEFAULT_MINES;
    if (args != null && args.length > 0) {
      try {
        int inputMines = Integer.parseInt(args[0]);
        if (inputMines > 0 && inputMines < BOARD_WIDTH * BOARD_HEIGHT) {
          mineCount = inputMines;
        }
      } catch (NumberFormatException e) {
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
      for (Tile[] row : board) {
        for (Tile tile : row) {
          if (tile.hasMine() && tile.isRevealed()) {
            initialExplodedMine = tile;
            explosionRadius = 0;
            return;
          }
        }
      }
    }
  }

  public PImage getSprite(String s) {
    return sprites.computeIfAbsent(
        s,
        key -> loadImage(this.getClass().getResource(key + ".png").getPath().replace("%20", " ")));
  }

  public Tile[][] getBoard() {
    return this.board;
  }

  public boolean isGameOver() {
    return gameOver;
  }

  public static void main(String[] args) {
    PApplet.runSketch(concat(new String[] {"minesweeper.App"}, args), new App());
  }
}
