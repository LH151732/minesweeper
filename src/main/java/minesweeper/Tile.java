package minesweeper;

import java.util.ArrayList;
import java.util.List;
import processing.core.PConstants;
import processing.core.PImage;

public class Tile {
  private boolean revealed;
  private boolean flagged;
  private int x;
  private int y;
  private boolean mine;
  private int explosionFrame;
  private boolean exploding;
  private static final int EXPLOSION_FRAMES = 10;
  private static final int EXPLOSION_SPEED = 5; // 每5帧更新一次爆炸动画

  public Tile(int x, int y) {
    this.x = x;
    this.y = y;
    reset();
  }

  public void reset() {
    this.revealed = false;
    this.flagged = false;
    this.mine = false;
    this.explosionFrame = 0;
    this.exploding = false;
  }

  public void draw(App app) {
    PImage tile = app.getSprite(revealed ? "tile" : "tile1");
    if (!revealed && isMouseOver(app)) {
      tile = app.getSprite(app.mousePressed && app.mouseButton == PConstants.LEFT ? "tile" : "tile2");
    }
    app.image(tile, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);

    if (revealed) {
      if (mine) {
        if (exploding) {
          if (explosionFrame < EXPLOSION_FRAMES) {
            PImage explosionSprite = app.getSprite("mine" + explosionFrame);
            app.image(explosionSprite, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);
            if (app.frameCount % EXPLOSION_SPEED == 0) {
              explosionFrame++;
            }
          } else {
            PImage wallSprite = app.getSprite("wall0");
            app.image(wallSprite, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);
          }
        } else {
          PImage mineSprite = app.getSprite("mine0");
          app.image(mineSprite, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);
        }
      } else {
        int mineCount = countAdjacentMines(app);
        if (mineCount > 0) {
          app.fill(
              App.mineCountColour[mineCount][0],
              App.mineCountColour[mineCount][1],
              App.mineCountColour[mineCount][2]);
          app.textAlign(PConstants.CENTER, PConstants.CENTER);
          app.textSize(18);
          app.text(
              String.valueOf(mineCount),
              (x + 0.5f) * App.CELLSIZE,
              (y + 0.5f) * App.CELLSIZE + App.TOPBAR);
        }
      }
    } else if (flagged) {
      PImage flag = app.getSprite("flag");
      app.image(flag, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);
    }
  }

  private boolean isMouseOver(App app) {
    return app.mouseX >= x * App.CELLSIZE
        && app.mouseX < (x + 1) * App.CELLSIZE
        && app.mouseY >= y * App.CELLSIZE + App.TOPBAR
        && app.mouseY < (y + 1) * App.CELLSIZE + App.TOPBAR;
  }

  public void onClick(App app) {
    if (!revealed && !flagged && !app.isGameOver()) {
      reveal();
      if (mine) {
        exploding = true;
        app.gameOver(false);
      } else if (countAdjacentMines(app) == 0) {
        revealAdjacentTiles(app);
      }
    }
  }

  public void toggleFlag() {
    if (!revealed) {
      flagged = !flagged;
    }
  }

  public void reveal() {
    if (!flagged) {
      revealed = true;
    }
  }

  private void revealAdjacentTiles(App app) {
    for (Tile t : getAdjacentTiles(app)) {
      if (!t.isRevealed() && !t.isFlagged()) {
        t.reveal();
        if (t.countAdjacentMines(app) == 0) {
          t.revealAdjacentTiles(app);
        }
      }
    }
  }

  public List<Tile> getAdjacentTiles(App app) {
    ArrayList<Tile> result = new ArrayList<>();
    Tile[][] board = app.getBoard();
    for (int dy = -1; dy <= 1; dy++) {
      for (int dx = -1; dx <= 1; dx++) {
        if (dx == 0 && dy == 0)
          continue;
        int newX = x + dx;
        int newY = y + dy;
        if (newX >= 0 && newX < board[0].length && newY >= 0 && newY < board.length) {
          result.add(board[newY][newX]);
        }
      }
    }
    return result;
  }

  public int countAdjacentMines(App app) {
    return (int) getAdjacentTiles(app).stream().filter(Tile::hasMine).count();
  }

  public boolean hasMine() {
    return mine;
  }

  public void setMine(boolean mine) {
    this.mine = mine;
  }

  public boolean isRevealed() {
    return revealed;
  }

  public boolean isFlagged() {
    return flagged;
  }

  public void startExplosion() {
    if (mine && !exploding) {
      exploding = true;
      explosionFrame = 0;
    }
  }

  public boolean isExploding() {
    return exploding;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}
