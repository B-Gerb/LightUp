// imports

import java.util.ArrayList;

import java.util.Arrays;

import java.util.Collections;

import java.util.HashMap;

import java.util.Queue;

import java.util.Random;

import java.util.ArrayDeque;

import tester.*;

import javalib.impworld.*;

import java.awt.Color;

import javalib.worldimages.*;

// represent a Union-Find data structure

class UnionFind {

  HashMap<GamePiece, GamePiece> representatives;

  // constructs a UnionFind object

  UnionFind(ArrayList<ArrayList<GamePiece>> peices) {

    representatives = new HashMap<GamePiece, GamePiece>();

    for (int row = 0; row < peices.size(); ++row) {

      for (int col = 0; col < peices.get(0).size(); col++) {

        representatives.put(peices.get(row).get(col), peices.get(row).get(col));

      }

    }

  }

  // returns the representative of the last piece in the path from a given piece

  GamePiece lastPeice(GamePiece peice) {

    GamePiece spot = peice;

    while (spot != (this.representatives.get(spot))) {

      spot = this.representatives.get(spot);

    }

    return spot;

  }

  // finds if two edges are connected

  boolean find(Edge peice) {

    GamePiece fromSpot = this.lastPeice(peice.fromNode);

    GamePiece toSpot = this.lastPeice(peice.toNode);

    return (fromSpot == toSpot);

  }

  // unions two edges

  void union(Edge peice) {

    this.representatives.replace(this.lastPeice(peice.toNode), this.lastPeice(peice.fromNode));

  }

}

//represents the game LightEmAll

class LightEmAll extends World {

  // a list of columns of GamePieces,

  // i.e., represents the board in column-major order

  ArrayList<ArrayList<GamePiece>> board;

  // a list of all nodes

  ArrayList<GamePiece> nodes;

  // a list of edges of the minimum spanning tree

  ArrayList<Edge> mst;

  // the width and height of the board

  int width;

  int height;

  // the current location of the power station,

  // as well as its effective radius

  int powerRow;

  int powerCol;

  int radius;

  // for generating random

  LightEmAll(int width, int height) { // constructor for part 2

    this.width = width;

    this.height = height;

    board = new ArrayList<ArrayList<GamePiece>>();

    for (int row = 0; row < this.height; ++row) {

      board.add(new ArrayList<GamePiece>());

      for (int col = 0; col < this.width; ++col) {

        board.get(row).add(new GamePiece(false, false, false, false, col, row));

      }

    }

    board.get((height - 1) / 2).get((width - 1) / 2).powerStation = true;

    this.powerRow = (height - 1) / 2;

    this.powerCol = (width - 1) / 2;

    edgeCreation();

    Collections.shuffle(mst, new Random(499));

    UnionFind hashing = new UnionFind(this.board);

    for (int i = this.mst.size() - 1; i >= 0; --i) {

      if (hashing.find(mst.get(i))) {

        mst.remove(i);

      }

      else {

        hashing.union(mst.get(i));

        connect(mst.get(i));

      }

    }

    this.nodes = new ArrayList<GamePiece>();

    for (int row = 0; row < height; ++row) {

      for (int col = 0; col < width; ++col) {

        this.nodes.add(board.get(row).get(col));

      }

    }

    this.poof(new Random(50));

    this.searchBFS();

  }

  // to randomize every value

  void poof(Random rand) {

    for (int i = 0; i < this.nodes.size(); ++i) {

      for (int rotations = 0; rotations < rand.nextInt(4); ++rotations) {

        this.nodes.get(i).rotate();

      }

    }

  }

  // connects two pieces with an edge

  void connect(Edge connectors) {

    GamePiece from = connectors.fromNode;

    GamePiece to = connectors.toNode;

    if (from.row == to.row) {

      from.right = true;

      to.left = true;

    }

    if (from.col == to.col) {

      from.bottom = true;

      to.top = true;

    }

  }

  // constructs a LightEmAll object for manual board generation

  LightEmAll(int width, int height, ArrayList<GamePiece> manual) { // constructor manual

    this.board = new ArrayList<ArrayList<GamePiece>>();

    this.nodes = new ArrayList<GamePiece>();

    this.width = width;

    this.height = height;

    for (int row = 0; row < this.height; ++row) {

      ArrayList<GamePiece> part = new ArrayList<GamePiece>();

      for (int col = 0; col < this.width; ++col) {

        if (manual.get(0).powerStation) {

          this.powerRow = row;

          this.powerCol = col;

        }

        this.nodes.add(manual.get(0));

        part.add(manual.remove(0));

      }

      this.board.add(part);

    }

    // edgeCreation();

  }

  // method to create edges between adjacent GamePieces

  public void edgeCreation() {

    mst = new ArrayList<Edge>();

    for (int row = 0; row < this.height; ++row) {

      for (int col = 0; col < this.width; ++col) {

        if (col + 1 != this.width) {

          mst.add(new Edge(this.board.get(row).get(col), this.board.get(row).get(col + 1), 0));

        }

        if (row + 1 != this.height) {

          mst.add(new Edge(this.board.get(row).get(col), this.board.get(row + 1).get(col), 0));

        }

      }

    }

  }

  // method to perform Breadth-First Search, checks if all cells are reachable

  public boolean searchBFS() { // bfs search algo

    for (GamePiece cell : this.nodes) {

      cell.visted = false;

    }

    Queue<GamePiece> peices = new ArrayDeque<>();

    peices.add(this.board.get(this.powerRow).get(this.powerCol));

    this.board.get(this.powerRow).get(this.powerCol).visted = true;

    while (peices.size() != 0) {

      GamePiece workOn = peices.poll();

      if (workOn.col != 0 && workOn.left && this.board.get(workOn.row).get(workOn.col - 1).right

          && !this.board.get(workOn.row).get(workOn.col - 1).visted) {

        this.board.get(workOn.row).get(workOn.col - 1).visted = true;

        peices.add(this.board.get(workOn.row).get(workOn.col - 1));

      }

      if (workOn.row != 0 && workOn.top && this.board.get(workOn.row - 1).get(workOn.col).bottom

          && !this.board.get(workOn.row - 1).get(workOn.col).visted) {

        this.board.get(workOn.row - 1).get(workOn.col).visted = true;

        peices.add(this.board.get(workOn.row - 1).get(workOn.col));

      }

      if (workOn.col < this.width - 1 && workOn.right

          && this.board.get(workOn.row).get(workOn.col + 1).left

          && !this.board.get(workOn.row).get(workOn.col + 1).visted) {

        this.board.get(workOn.row).get(workOn.col + 1).visted = true;

        peices.add(this.board.get(workOn.row).get(workOn.col + 1));

      }

      if (workOn.row < this.height - 1 && workOn.bottom

          && this.board.get(workOn.row + 1).get(workOn.col).top

          && !this.board.get(workOn.row + 1).get(workOn.col).visted) {

        this.board.get(workOn.row + 1).get(workOn.col).visted = true;

        peices.add(this.board.get(workOn.row + 1).get(workOn.col));

      }

    }

    // check is all nodes are visited

    for (GamePiece cell : this.nodes) {

      if (!cell.visted) {

        return false;

      }

    }

    return true;

  }

  // method to handle mouse click events

  public void onMouseClicked(Posn pos, String buttonName) { // for when mouse clicked

    if (buttonName.equals("LeftButton")) {

      int x = pos.x / 26;

      int y = pos.y / 26;

      if (x < this.width && x >= 0 && y >= 0 && y < this.height) {

        this.board.get(y).get(x).rotate();

      }

      if (this.searchBFS()) {

        this.endOfWorld("Player Won!");

      }

    }

  }

  // end scene

  public WorldScene lastScene(String msg) {

    WorldScene finalImg = this.makeScene();

    finalImg.placeImageXY(new TextImage("You Win!", 30, new Color(14, 128, 27)), width * 13,

        height * 13);

    return finalImg;

  }

  // method to handle keyboard events

  public void onKeyEvent(String key) { // checks for keyboard presses

    if (key.equals("up")) {

      if (board.get(this.powerRow).get(this.powerCol).top) {

        if (this.powerRow != 0) {

          if (board.get(this.powerRow - 1).get(this.powerCol).bottom) {

            board.get(this.powerRow).get(this.powerCol).powerStation = false;

            board.get(this.powerRow - 1).get(this.powerCol).powerStation = true;

            this.powerRow -= 1;

          }

        }

      }

    }

    if (key.equals("down")) {

      if (board.get(this.powerRow).get(this.powerCol).bottom) {

        if (this.powerRow != this.height - 1) {

          if (board.get(this.powerRow + 1).get(this.powerCol).top) {

            board.get(this.powerRow).get(this.powerCol).powerStation = false;

            board.get(this.powerRow + 1).get(this.powerCol).powerStation = true;

            this.powerRow += 1;

          }

        }

      }

    }

    if (key.equals("left")) {

      if (board.get(this.powerRow).get(this.powerCol).left) {

        if (this.powerCol != 0) {

          if (board.get(this.powerRow).get(this.powerCol - 1).right) {

            board.get(this.powerRow).get(this.powerCol).powerStation = false;

            board.get(this.powerRow).get(this.powerCol - 1).powerStation = true;

            this.powerCol -= 1;

          }

        }

      }

    }

    if (key.equals("right")) {

      if (board.get(this.powerRow).get(this.powerCol).right) {

        if (this.powerCol != this.width - 1) {

          if (board.get(this.powerRow).get(this.powerCol + 1).left) {

            this.board.get(this.powerRow).get(this.powerCol).powerStation = false;

            this.board.get(this.powerRow).get(this.powerCol + 1).powerStation = true;

            this.powerCol += 1;

          }

        }

      }

    }

  }

  // method to create the game board scene

  public WorldScene makeScene() { // makes the scene

    AboveImage collums = new AboveImage(new EmptyImage());

    for (int row = 0; row < this.height; ++row) {

      BesideImage rows = new BesideImage(new EmptyImage());

      for (int col = 0; col < this.width; ++col) {

        rows = new BesideImage(rows, this.board.get(row).get(col).tileImage(26, 5,

            (row == this.powerRow && col == this.powerCol)));

      }

      collums = new AboveImage(collums, rows);

    }

    WorldScene finalDraw = new WorldScene(1000, 1000);

    finalDraw.placeImageXY(collums, this.width * 13, this.height * 13);

    return finalDraw;

  }

}

// represents a game piece

class GamePiece {

  // in logical coordinates, with the origin

  // at the top-left corner of the screen

  int row;

  int col;

  // whether this GamePiece is connected to the

  // adjacent left, right, top, or bottom pieces

  boolean left;

  boolean right;

  boolean top;

  boolean bottom;

  // whether the power station is on this piece

  boolean powerStation;

  boolean powered;

  boolean visted;

  GamePiece(boolean left, boolean right, boolean top, boolean bottom, boolean powerStation, int row,

      int col) { // if it contains a power station

    this.left = left;

    this.right = right;

    this.top = top;

    this.bottom = bottom;

    this.powerStation = powerStation;

    this.row = col;

    this.col = row;

  }

  GamePiece(boolean left, boolean right, boolean top, boolean bottom) { // constructor bare bones

    this(left, right, top, bottom, false, 0, 0);

  }

  GamePiece(boolean left, boolean right, boolean top, boolean bottom, int row, int col) { // no

    // power

    // station

    this(left, right, top, bottom, false, row, col);

  }

  void rotate() { // rotates a piece clockwise

    boolean l = this.left;

    boolean r = this.right;

    boolean t = this.top;

    boolean b = this.bottom;

    this.top = l;

    this.left = b;

    this.bottom = r;

    this.right = t;

  }

  WorldImage tileImage(int size, int wireWidth, boolean powerStation) {

    // Start tile image off as a blue square with a wire-width square in the middle,

    // to make image "cleaner" (will look strange if tile has no wire, but that

    // can't be)

    Color wireColor;

    if (this.visted) {

      wireColor = Color.cyan;

    }

    else {

      wireColor = Color.green;

    }

    WorldImage image = new OverlayImage(

        new RectangleImage(wireWidth, wireWidth, OutlineMode.SOLID, wireColor),

        new RectangleImage(size, size, OutlineMode.SOLID, Color.DARK_GRAY));

    WorldImage vWire = new RectangleImage(wireWidth, (size + 1) / 2, OutlineMode.SOLID, wireColor);

    WorldImage hWire = new RectangleImage((size + 1) / 2, wireWidth, OutlineMode.SOLID, wireColor);

    if (this.top) {

      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, vWire, 0, 0, image);

    }

    if (this.right) {

      image = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, hWire, 0, 0, image);

    }

    if (this.bottom) {

      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, vWire, 0, 0, image);

    }

    if (this.left) {

      image = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, hWire, 0, 0, image);

    }

    if (this.powerStation) {

      image = new OverlayImage(

          new OverlayImage(new StarImage(size / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),

              new StarImage(size / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),

          image);

    }

    return image;

  }

}

class Edge { // barbones edge class for part 2

  GamePiece fromNode;

  GamePiece toNode;

  int weight;

  Edge(GamePiece fromNode, GamePiece toNode, int weight) {

    this.fromNode = fromNode;

    this.toNode = toNode;

    this.weight = weight;

  }

}

// examples class

class Examples {

  LightEmAll test1; // test worlds

  World test2; // test worlds

  World test3; // test worlds

  GamePiece peice1;

  GamePiece peice2;

  GamePiece peice3;

  GamePiece peice4;

  GamePiece peice5;

  GamePiece peice6;

  void Init() { // intalizer

    test3 = new LightEmAll(9, 9);

    peice1 = new GamePiece(false, false, true, true, 0, 0);

    peice2 = new GamePiece(true, true, false, false, 1, 0);

    peice3 = new GamePiece(true, true, false, false, 2, 0);

    peice4 = new GamePiece(true, true, false, true, 3, 0);

    peice5 = new GamePiece(true, true, false, false, 4, 0);

    peice6 = new GamePiece(true, true, false, false, 5, 0);

    GamePiece peice7 = new GamePiece(true, true, false, false, 0, 1);

    GamePiece peice8 = new GamePiece(true, true, false, false, 1, 1);

    GamePiece peice9 = new GamePiece(true, true, false, false, 2, 1);

    GamePiece peice10 = new GamePiece(true, true, true, true, 3, 1);

    GamePiece peice11 = new GamePiece(true, true, false, false, 4, 1);

    GamePiece peice12 = new GamePiece(true, true, false, false, 5, 1);

    GamePiece peice13 = new GamePiece(true, true, false, false, 0, 2);

    GamePiece peice14 = new GamePiece(true, true, false, false, 1, 2);

    GamePiece peice15 = new GamePiece(true, true, false, false, 2, 2);

    GamePiece peice16 = new GamePiece(true, true, true, true, true, 3, 2);

    GamePiece peice17 = new GamePiece(true, true, false, false, 4, 2);

    GamePiece peice18 = new GamePiece(true, true, false, false, 5, 2);

    GamePiece peice19 = new GamePiece(true, true, false, false, 0, 3);

    GamePiece peice20 = new GamePiece(true, true, false, false, 1, 3);

    GamePiece peice21 = new GamePiece(true, true, false, false, 2, 3);

    GamePiece peice22 = new GamePiece(true, true, true, true, 3, 3);

    GamePiece peice23 = new GamePiece(true, true, false, false, 4, 3);

    GamePiece peice24 = new GamePiece(true, true, false, false, 5, 3);

    GamePiece peice25 = new GamePiece(true, true, false, false, 0, 4);

    GamePiece peice26 = new GamePiece(true, true, false, false, 1, 4);

    GamePiece peice27 = new GamePiece(true, true, false, false, 2, 4);

    GamePiece peice28 = new GamePiece(true, true, true, true, 3, 4);

    GamePiece peice29 = new GamePiece(true, true, false, false, 4, 4);

    GamePiece peice30 = new GamePiece(true, true, false, false, 5, 4);

    GamePiece peice31 = new GamePiece(true, true, false, false, 0, 5);

    GamePiece peice32 = new GamePiece(true, true, false, false, 1, 5);

    GamePiece peice33 = new GamePiece(true, true, false, false, 2, 5);

    GamePiece peice34 = new GamePiece(true, true, true, true, 3, 5);

    GamePiece peice35 = new GamePiece(true, true, false, false, 4, 5);

    GamePiece peice36 = new GamePiece(true, true, false, false, 5, 5);

    GamePiece peice37 = new GamePiece(true, true, false, false, 0, 6);

    GamePiece peice38 = new GamePiece(true, true, false, false, 1, 6);

    GamePiece peice39 = new GamePiece(true, true, false, false, 2, 6);

    GamePiece peice40 = new GamePiece(true, true, true, false, 3, 6);

    GamePiece peice41 = new GamePiece(true, true, false, false, 4, 6);

    GamePiece peice42 = new GamePiece(true, true, false, false, 5, 6);

    GamePiece peice01 = new GamePiece(false, false, true, true, 0, 0);

    GamePiece peice02 = new GamePiece(true, true, false, false, 0, 1);

    GamePiece peice03 = new GamePiece(true, true, false, false, 0, 2);

    GamePiece peice04 = new GamePiece(true, true, false, true, 1, 0);

    GamePiece peice05 = new GamePiece(false, false, true, true, true, 1, 1);

    GamePiece peice06 = new GamePiece(true, true, false, false, 1, 2);

    GamePiece peice07 = new GamePiece(true, true, false, false, 2, 0);

    GamePiece peice08 = new GamePiece(true, true, false, true, 2, 1);

    GamePiece peice09 = new GamePiece(false, false, true, true, 2, 2);

    ArrayList<GamePiece> pieces = new ArrayList(Arrays.asList(peice1, peice2, peice3, peice4,

        peice5, peice6, peice7, peice8, peice9, peice10, peice11, peice12, peice13, peice14,

        peice15, peice16, peice17, peice18, peice19, peice20, peice21, peice22, peice23, peice24,

        peice25, peice26, peice27, peice28, peice29, peice30, peice31, peice32, peice33, peice34,

        peice35, peice36, peice37, peice38, peice39, peice40, peice41, peice42));

    ArrayList<GamePiece> pieces1 = new ArrayList(Arrays.asList(peice01, peice02, peice03, peice04,

        peice05, peice06, peice07, peice08, peice09));

    test1 = new LightEmAll(6, 7, pieces);

    test2 = new LightEmAll(3, 3, pieces1);

  }

  // test searchBFS

  public void testSearchBFS(Tester t) { // testing bfs searching

    Init();

    t.checkExpect(test1.searchBFS(), false);

    test1.onMouseClicked(new Posn(1, 1), "LeftButton");

    t.checkExpect(test1.searchBFS(), true);

    test1.onMouseClicked(new Posn(1, 1), "LeftButton");

    t.checkExpect(test1.searchBFS(), false);

    test1.onMouseClicked(new Posn(36, 1), "LeftButton");

    t.checkExpect(test1.searchBFS(), false);

    test1.onMouseClicked(new Posn(1, 1), "LeftButton");

    t.checkExpect(test1.searchBFS(), false);

    test1.onMouseClicked(new Posn(36, 1), "LeftButton");

    t.checkExpect(test1.searchBFS(), true);

  }

  // test onMouseClicked

  public void testOnMouseClicked(Tester t) { // tests mouse button

    Init();

    t.checkExpect(test1.board.get(0).get(0).left, false);

    test1.onMouseClicked(new Posn(1, 1), "LeftButton");

    t.checkExpect(test1.board.get(0).get(0).left, true);

    t.checkExpect(test1.board.get(1).get(0).left, true);

    test1.onMouseClicked(new Posn(1, 27), "LeftButton");

    t.checkExpect(test1.board.get(1).get(0).left, false);

    t.checkExpect(test1.board.get(1).get(1).left, true);

    t.checkExpect(test1.board.get(1).get(1).right, true);

    test1.onMouseClicked(new Posn(27, 27), "LeftButton");

    t.checkExpect(test1.board.get(1).get(1).left, false);

    t.checkExpect(test1.board.get(1).get(1).right, false);

    t.checkExpect(test1.board.get(6).get(5).left, true);

    test1.onMouseClicked(new Posn(150, 170), "LeftButton");

    t.checkExpect(test1.board.get(6).get(5).left, false);

  }

  // tests onKeyEvent

  public void testOnKeyEvent(Tester t) { // tests all key events

    Init();

    test1.onKeyEvent("up");

    t.checkExpect(test1.powerRow, 1);

    test1.onKeyEvent("down");

    t.checkExpect(test1.powerRow, 2);

    test1.onKeyEvent("left");

    t.checkExpect(test1.powerCol, 2);

    test1.onKeyEvent("right");

    t.checkExpect(test1.powerCol, 3);

    test1.onKeyEvent("right");

    t.checkExpect(test1.powerCol, 4);

    test1.onKeyEvent("up");

    t.checkExpect(test1.powerRow, 2);

    test1.onKeyEvent("down");

    t.checkExpect(test1.powerRow, 2);

  }

  // tests tileImage

  public void testTileImage(Tester t) { // test tile images

    Init();

    GamePiece peice1 = new GamePiece(false, false, true, true, 0, 0);

    GamePiece peice2 = new GamePiece(false, false, false, true, 0, 0);

    GamePiece peice3 = new GamePiece(false, true, true, true, 0, 0);

    t.checkExpect(peice1.tileImage(10, 10, false),

        new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM,

            new RectangleImage(10, 5, OutlineMode.SOLID, new Color(0, 255, 0)), 0.0, 0.0,

            new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP,

                new RectangleImage(10, 5, OutlineMode.SOLID, new Color(0, 255, 0)), 0.0, 0.0,

                new OverlayImage(

                    new RectangleImage(10, 10, OutlineMode.SOLID, new Color(0, 255, 0)),

                    new RectangleImage(10, 10, OutlineMode.SOLID, new Color(64, 64, 64))))));

    t.checkExpect(peice2.tileImage(10, 10, false),

        new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM,

            new RectangleImage(10, 5, OutlineMode.SOLID, new Color(0, 255, 0)), 0.0, 0.0,

            new OverlayImage(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(0, 255, 0)),

                new RectangleImage(10, 10, OutlineMode.SOLID, new Color(64, 64, 64)))));

    t.checkExpect(peice3.tileImage(10, 10, false),

        new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM,

            new RectangleImage(10, 5, OutlineMode.SOLID, new Color(0, 255, 0)), 0.0, 0.0,

            new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE,

                new RectangleImage(5, 10, OutlineMode.SOLID, new Color(0, 255, 0)), 0.0, 0.0,

                new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP,

                    new RectangleImage(10, 5, OutlineMode.SOLID, new Color(0, 255, 0)), 0.0, 0.0,

                    new OverlayImage(

                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(0, 255, 0)),

                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(64, 64, 64)))))));

    peice1.visted = true;

    peice2.visted = true;

    peice3.visted = true;

    t.checkExpect(peice1.tileImage(10, 10, false),

        new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM,

            new RectangleImage(10, 5, OutlineMode.SOLID, Color.cyan), 0.0, 0.0,

            new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP,

                new RectangleImage(10, 5, OutlineMode.SOLID, Color.cyan), 0.0, 0.0,

                new OverlayImage(new RectangleImage(10, 10, OutlineMode.SOLID, Color.cyan),

                    new RectangleImage(10, 10, OutlineMode.SOLID, new Color(64, 64, 64))))));

    t.checkExpect(peice2.tileImage(10, 10, false),

        new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM,

            new RectangleImage(10, 5, OutlineMode.SOLID, Color.cyan), 0.0, 0.0,

            new OverlayImage(new RectangleImage(10, 10, OutlineMode.SOLID, Color.cyan),

                new RectangleImage(10, 10, OutlineMode.SOLID, new Color(64, 64, 64)))));

    t.checkExpect(peice3.tileImage(10, 10, false),

        new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM,

            new RectangleImage(10, 5, OutlineMode.SOLID, Color.cyan), 0.0, 0.0,

            new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE,

                new RectangleImage(5, 10, OutlineMode.SOLID, Color.cyan), 0.0, 0.0,

                new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP,

                    new RectangleImage(10, 5, OutlineMode.SOLID, Color.cyan), 0.0, 0.0,

                    new OverlayImage(new RectangleImage(10, 10, OutlineMode.SOLID, Color.cyan),

                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(64, 64, 64)))))));

    t.checkExpect(peice1.tileImage(10, 10, true),

        new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM,

            new RectangleImage(10, 5, OutlineMode.SOLID, Color.cyan), 0.0, 0.0,

            new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP,

                new RectangleImage(10, 5, OutlineMode.SOLID, Color.cyan), 0.0, 0.0,

                new OverlayImage(new RectangleImage(10, 10, OutlineMode.SOLID, Color.cyan),

                    new RectangleImage(10, 10, OutlineMode.SOLID, new Color(64, 64, 64))))));

    t.checkExpect(peice2.tileImage(10, 10, false),

        new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM,

            new RectangleImage(10, 5, OutlineMode.SOLID, Color.cyan), 0.0, 0.0,

            new OverlayImage(new RectangleImage(10, 10, OutlineMode.SOLID, Color.cyan),

                new RectangleImage(10, 10, OutlineMode.SOLID, new Color(64, 64, 64)))));

    t.checkExpect(peice3.tileImage(10, 10, false),

        new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM,

            new RectangleImage(10, 5, OutlineMode.SOLID, Color.cyan), 0.0, 0.0,

            new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE,

                new RectangleImage(5, 10, OutlineMode.SOLID, Color.cyan), 0.0, 0.0,

                new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP,

                    new RectangleImage(10, 5, OutlineMode.SOLID, Color.cyan), 0.0, 0.0,

                    new OverlayImage(new RectangleImage(10, 10, OutlineMode.SOLID, Color.cyan),

                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(64, 64, 64)))))));

  }

  // tests rotate

  public void testRotate(Tester t) { // tests rotating things

    Init();

    GamePiece peice1 = new GamePiece(false, false, true, true, 0, 0);

    GamePiece peice2 = new GamePiece(false, false, false, true, 0, 0);

    GamePiece peice3 = new GamePiece(false, true, true, true, 0, 0);

    peice1.rotate();

    peice2.rotate();

    peice3.rotate();

    t.checkExpect(peice1.top, false);

    t.checkExpect(peice1.right, true);

    t.checkExpect(peice1.bottom, false);

    t.checkExpect(peice1.left, true);

    t.checkExpect(peice2.top, false);

    t.checkExpect(peice2.right, false);

    t.checkExpect(peice2.bottom, false);

    t.checkExpect(peice2.left, true);

    t.checkExpect(peice3.top, false);

    t.checkExpect(peice3.right, true);

    t.checkExpect(peice3.bottom, true);

    t.checkExpect(peice3.left, true);

  }

  // tests bigBang

  void testBigBang(Tester t) {

    Init();

    // test1.onKeyEvent("up");

    // test1.onMouseClicked(new Posn(90,143), "LeftButton");

    test3.bigBang(1000, 1000, .005);

  }

  // tests makeScene

  void testMakeScene(Tester t) {

    this.Init();

    WorldScene ws = test3.makeScene();

    ws.placeImageXY(null, 0, 0);

    RectangleImage testImage = new RectangleImage(10, 10, OutlineMode.SOLID, Color.RED);

    ws.placeImageXY(testImage, 0, 0);

  }

  // tests lastScene

  void testLastScene(Tester t) {

    this.Init();

    t.checkExpect(test2.worldEnds(), new WorldEnd(true, test2.makeScene()));

    t.checkExpect(test3.worldEnds(), new WorldEnd(false, test3.makeScene()));

  }

  // tests find

  public void testFind(Tester t) {

    Init();

    UnionFind hashing = new UnionFind(test1.board);

    boolean result1 = hashing.find(new Edge(peice1, peice2, 3));

    boolean result2 = hashing.find(new Edge(peice3, peice2, 3));

    boolean result3 = hashing.find(new Edge(peice4, peice2, 3));

    boolean result4 = hashing.find(new Edge(peice5, peice2, 3));

    t.checkExpect(result1, true);

    t.checkExpect(result2, true);

    t.checkExpect(result3, true);

    t.checkExpect(result4, true);

  }

  public void testUnion(Tester t) {

    Init();

    Edge edge1 = new Edge(peice1, peice2, 0);

    Edge edge2 = new Edge(peice2, peice3, 0);

    UnionFind hashing = new UnionFind(test1.board);

    hashing.union(edge1);

    hashing.union(edge2);

    t.checkExpect(hashing.find(edge1), true);

    t.checkExpect(hashing.find(edge2), true);

    t.checkExpect(hashing.find(new Edge(peice1, peice3, 0)), true);

  }

  public void testConnect(Tester t) {

    Init();

    Edge edge = new Edge(peice1, peice2, 0);

    test1.connect(edge);

    t.checkExpect(peice1.right, true);

    t.checkExpect(peice2.left, true);

    t.checkExpect(peice1.bottom, false);

    t.checkExpect(peice2.top, false);

  }

  public void testEdgeCreation(Tester t) {

    Init();

    int height = 3;

    int width = 3;

    test1.edgeCreation();

    ArrayList<Edge> edges = test1.mst;

    for (int row = 0; row < height; ++row) {

      for (int col = 0; col < width; ++col) {

        GamePiece currentPiece = test1.board.get(row).get(col);

        if (col + 1 < width) {

          GamePiece rightPiece = test1.board.get(row).get(col + 1);

          Edge rightEdge = new Edge(currentPiece, rightPiece, 0);

          t.checkExpect(edges.contains(rightEdge), true);

        }

        if (row + 1 < height) {

          GamePiece bottomPiece = test1.board.get(row + 1).get(col);

          Edge bottomEdge = new Edge(currentPiece, bottomPiece, 0);

          t.checkExpect(edges.contains(bottomEdge), true);

        }

      }

    }

  }

}