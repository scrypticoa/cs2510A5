import tester.*;
import java.util.Random;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;

class Game extends World {
  GameArt art;
  
  boolean finished = false;
  
  ILoInt sequence;
  
  Random rand;
  
  ILoInt guess;
  int guessLength;
  int numGuesses = 0;
  
  int numColors;
  
  // constructor settings
  ILoColor gameColors;
  int sequenceLength;
  int maxGuesses;
  boolean duplicatesAllowed;
  
  public Game(ILoColor gameColors, int sequenceLength,
      int attemptCount, boolean duplicatesAllowed, Random setRand) {
    this.gameColors = gameColors;
    this.sequenceLength = sequenceLength;
    this.maxGuesses = attemptCount;
    this.duplicatesAllowed = duplicatesAllowed;
    
    this.numColors = gameColors.calcLength();
    
    this.validate();
    
    this.guess = new MtLoInt();
    this.guessLength = 0;
    
    this.rand = setRand;
    
    this.sequence = this.genSequence();
    System.out.println(sequence.print());
    
    this.art = new GameArt(this);
  }
  
  public Game(ILoColor gameColors, int sequenceLength,
      int attemptCount, boolean duplicatesAllowed) {
    this(gameColors, sequenceLength, attemptCount, duplicatesAllowed, new Random());
  }
  
  /* TEMPLATE:
   * 
   * FIELDS:
   * this.art ... GameArt
   * this.finished ... boolean
   * this.sequence ... ILoInt
   * this.rand ... Random
   * this.guess ... ILoInt
   * this.guessLenght ... int
   * this.numGuesses ... int
   * this.numColors ... int
   * this.gameColors ... ILoColor
   * this.sequencelLength ... int
   * this.duplicatesAllowed ... boolean
   * 
   * METHODS:
   * this.validate() ... void
   * this.genSequence() ... ILoInt
   * this.genSequenceDuplicates(int length, ILoInt seq) ... ILoInt
   * this.genSequenceNoDuplicates(int length, ILoInt seq, String seen) ... ILoInt
   * this.startGame() ... boolean
   * this.makeScene() ... WorldScene
   * this.onKeyEvent(String key) ... Game
   * this.genBlankResult() ... Result
   * this.addGuess(int color) ... void
   * this.removeGuess() ... void
   * this.submitGuess() ... void
   * 
   * METHODS OF FIELDS:
   * art.doBigBang(Game game) ... boolean
   * art.produceImage() ... WolrdScene
   * art.submitFalseGuess(ILoInt guess, int exact, int inexact) ... void
   * art.prepareNextGuess() ... void
   * art.genHiddenSequence() ... WorldImage
   * art.genSequence() ... WorldImage
   * art.genAvailableColorsIMG() ... WorldImage
   * art.updateGuessSlots(ILoInt guess) ... void
   * art.updateGuessImage(WorldImage image) ... void
   * art.genInitGuessSlots() ... WorldImage
   * art.doGenInitGuessSlots(int length, WorldImage img, WorldImage slots) ... WorldImage
   * art.genGuessResult(int exact, int inexact) ... WorldImage
   * art.genColorList(ILoColor colList) ... WorldImage
   * art.genColorList(ILoInt colList, int numSlots) ... WorldImage
   * art.genEmptyDotsBeside(int length, WorldImage img) ... WorldImage
   * art.genFilledDot(Color col) ... WorldImage
   * art.genFilledDot(int index) ... WorldImage
   * art.getColor(int index) ... Color
   * art.calcGuessLine() ... int
   * 
   * sequence.addAtPos(int pos) ... ILoInt
   * sequence.insert(int num) ... ILoInt
   * sequence.remove() ... ILoInt
   * sequence.clone() ... ILoInt
   * sequence.compare(ILoInt other, Game game) ... Result
   * sequence.comparePass(ILoInt other, Game game, Result result) ... Result
   * sequence.doCompare(ILoInt other, Game game, Result result, int num) ... Result
   * sequence.sum() ... int
   * sequence.mins(ILoInt list) ... ILoInt
   * sequence.doMins(int otherFirst, ILoInt otherRest) ... ILoInt
   * sequence.fold(T initial, BiFunction<T, Integer, T> folder) ... <T>
   * sequence.print() ... String
   * 
   * guess.addAtPos(int pos) ... ILoInt
   * guess.insert(int num) ... ILoInt
   * guess.remove() ... ILoInt
   * guess.clone() ... ILoInt
   * guess.compare(ILoInt other, Game game) ... Result
   * guess.comparePass(ILoInt other, Game game, Result result) ... Result
   * guess.doCompare(ILoInt other, Game game, Result result, int num) ... Result
   * guess.sum() ... int
   * guess.mins(ILoInt list) ... ILoInt
   * guess.doMins(int otherFirst, ILoInt otherRest) ... ILoInt
   * guess.fold(T initial, BiFunction<T, Integer, T> folder) ... <T>
   * guess.print() ... String
   * 
   * gameColors.calcLength() ... int
   * gameColors.get(int index) ... Color
   * gameColors.fold(T initial, BiFunction<T, Color, T> folder) ...  <T>
   */
  
  
  void validate() {
    if (this.sequenceLength < 1) throw new IllegalArgumentException();
    if (this.maxGuesses < 1) throw new IllegalArgumentException();
    if (this.numColors < 1) throw new IllegalArgumentException();
    if (!this.duplicatesAllowed && this.sequenceLength > this.numColors)
      throw new IllegalArgumentException();
  }
  
  ILoInt genSequence() {
    if (duplicatesAllowed) {
      return genSequenceDuplicates(this.sequenceLength, new MtLoInt());
    }
    return genSequenceNoDuplicates(this.sequenceLength, new MtLoInt(), "");
  }
  
  ILoInt genSequenceDuplicates(int length, ILoInt seq) {
    if (length < 1) return seq;
    return genSequenceDuplicates(length - 1, seq.insert(this.rand.nextInt(this.numColors)));
  }
  
  ILoInt genSequenceNoDuplicates(int length, ILoInt seq, String seen) {
    if (length < 1) return seq;
    Integer val = this.rand.nextInt(this.numColors);
    if (seen.contains(val.toString())) return genSequenceNoDuplicates(length, seq, seen);
    return genSequenceNoDuplicates(length - 1, seq.insert(val), seen.concat(val.toString()));
  }
  
  boolean startGame() {
    return art.doBigBang(this);
  }

  public WorldScene makeScene() {
    return art.produceImage();
  }
  
  public Game onKeyEvent(String key) {
    if (this.finished) return this;
    String nums = "123456789";
    if (nums.contains(key)) {
      int keyInt = Integer.parseInt(key) - 1;
      if (this.guessLength < this.sequenceLength && keyInt < this.numColors) {
        addGuess(keyInt);
      }
    } else if (key.equals("delete" )) {
      removeGuess();
    } else if (key.equals("enter")) {
      if (this.guessLength == this.sequenceLength) {
        submitGuess();
      }
    }
    return this;
  }
  
  Result genBlankResult() {
    return new Result(
        0,
        new ConsLoInt(this.numColors),
        new ConsLoInt(this.numColors));
  }
  
  void addGuess(int color) {
    this.guess = this.guess.insert(color);
    this.guessLength++;
    
    this.art.updateGuessSlots(this.guess);
  }
  
  void removeGuess() {
    if (this.guessLength > 0) {
      this.guess = this.guess.remove();
      this.guessLength--;
    }
    
    this.art.updateGuessSlots(this.guess);
  }
  
  void submitGuess() {
    this.numGuesses++;
    
    Result res = this.guess.compare(this.sequence, this);
    
    if (res.didWin(this.sequenceLength)) {
      finished = true;
      art.submitCorrectGuess(this.guess, this.sequenceLength, 0);
    } else {
      if (this.numGuesses == this.maxGuesses) finished = true;
      art.submitFalseGuess(guess, res.exact, res.calcInexactCount());
    }
    
    this.guess = new MtLoInt();
    this.guessLength = 0;
  }
  
  class GameArt {
    ILoColor colors;
    
    int width;
    int height;
    
    int maxGuesses;
    int numGuesses = 0;
    int screenGuesses = 10;
    int sequenceLength;
    
    WorldImage availableColorsIMG;
    WorldImage guessSlots;
    WorldImage winSequenceIMG;
    WorldImage loseSequenceIMG;
    WorldImage hiddenSequence;
    
    int dotSquareSide = 26; // even
    int dotRadiusGap = 2;
    Color bgColor = new Color(150, 0, 0);
    Color outlineColor = Color.black;
    
    WorldImage emptyDot;
    
    public GameArt(Game game) {
      this.maxGuesses = game.maxGuesses;
      this.screenGuesses = Math.min(this.screenGuesses, game.maxGuesses);
      
      this.colors = game.gameColors;
      this.sequenceLength = game.sequenceLength;
      
      this.emptyDot = new RectangleImage(
          this.dotSquareSide, 
          this.dotSquareSide,
          OutlineMode.SOLID,
          this.bgColor);
      this.emptyDot = new OverlayImage( 
          new CircleImage(
              (this.dotSquareSide / 2) - this.dotRadiusGap,
              OutlineMode.OUTLINE, this.outlineColor),
          this.emptyDot);
      
      this.guessSlots = this.genInitGuessSlots();
      this.availableColorsIMG = this.genAvailableColorsIMG();
      this.winSequenceIMG = this.genResultSequence(game.sequence, "Win!");
      this.loseSequenceIMG = this.genResultSequence(game.sequence, "Lose!");
      this.hiddenSequence = this.genHiddenSequence();
    
      WorldImage demoScreen = new AboveAlignImage(
          AlignModeX.LEFT,
          this.loseSequenceIMG,
          this.guessSlots,
          this.availableColorsIMG);
      
      Double dHeight = Math.ceil(demoScreen.getHeight());
      this.height = dHeight.intValue();
      
      Double dWidth = Math.ceil(demoScreen.getWidth());
      this.width = dWidth.intValue();
    }
    
    /* TEMPLATE:
     * 
     * FIELDS:
     * this.colors ... ILoColor
     * this.height ... int
     * this.maxGuesses ... int
     * this.numGuesses ... int
     * this.screenGuesses ... int
     * this.sequenceLength ... int
     * this.availableColorsIMG ... WorldImage
     * this.guessSlots ... WorldImage
     * this.winSequenceIMG ... WorldImage
     * this.loseSequenceIMG  ... WorldImage
     * this.hiddenSequence ... WorldImage
     * this.dotSquareSide ... int
     * this.dotRadiusGap ... int
     * this.bgColor ... Color
     * this.outlineColor ... Color
     * this.gameWidth ... double
     * this.emptyDot ... WorldImage
     * 
     * METHODS:
     * this.doBigBang(Game game) ... boolean
     * this.produceImage() ... WolrdScene
     * this.submitFalseGuess(ILoInt guess, int exact, int inexact) ... void
     * this.prepareNextGuess() ... void
     * this.genHiddenSequence() ... WorldImage
     * this.genSequence() ... WorldImage
     * this.genAvailableColorsIMG() ... WorldImage
     * this.updateGuessSlots(ILoInt guess) ... void
     * this.updateGuessImage(WorldImage image) ... void
     * this.genInitGuessSlots() ... WorldImage
     * this.doGenInitGuessSlots(int length, WorldImage img, WorldImage slots) ... WorldImage
     * this.genGuessResult(int exact, int inexact) ... WorldImage
     * this.genColorList(ILoColor colList) ... WorldImage
     * this.genColorList(ILoInt colList, int numSlots) ... WorldImage
     * this.genEmptyDotsBeside(int length, WorldImage img) ... WorldImage
     * this.genFilledDot(Color col) ... WorldImage
     * this.genFilledDot(int index) ... WorldImage
     * this.getColor(int index) ... Color
     * calcGuessLine() ... int
     * 
     * METHODS OF FIELDS:
     * colors.calcLength() ... int
     * colors.get(int index) ... Color
     * colors.fold(T initial, BiFunction<T, Color, T> folder) ...  <T>
     */
    
    boolean doBigBang(Game game) {
      return game.bigBang(this.gameWidth.intValue(), this.height, 10);
    }
    
    WorldScene produceImage() {
      
      WorldImage screen = new AboveAlignImage(
          AlignModeX.LEFT,
          new EmptyImage(),
          this.hiddenSequence,
          this.guessSlots,
          this.availableColorsIMG);
      Double screenHeight = Math.ceil(screen.getHeight());
      
      screen = new OverlayOffsetAlign(
          AlignModeX.LEFT, AlignModeY.BOTTOM,
          screen, 0, 0,
          new RectangleImage(
              this.width, screenHeight.intValue(),
              OutlineMode.SOLID, this.bgColor));
      
      /*this.worldScene = worldScene.placeImageXY(
          new RectangleImage(this.gameWidth.intValue(), this.height, OutlineMode.SOLID, this.bgColor),
          this.gameWidth.intValue() / 2, this.height / 2);*/
      
      WorldScene worldScene = new WorldScene(this.width, this.height);
      worldScene = worldScene.placeImageXY(screen, this.width / 2, this.height / 2);
      
      return worldScene;
    }
    
    void submitFalseGuess(ILoInt guess, int exact, int inexact) {
      WorldImage guessOutput = new BesideImage(
          genColorList(guess, this.sequenceLength),
          genGuessResult(exact, inexact));
      updateGuessImage(guessOutput);
      
      prepareNextGuess();
      
      if (this.numGuesses == this.maxGuesses) {
        this.hiddenSequence = this.loseSequenceIMG;
      }
    }
    
    void submitCorrectGuess(ILoInt guess, int exact, int inexact) {
      WorldImage guessOutput = new BesideImage(
          genColorList(guess, this.sequenceLength),
          genGuessResult(exact, inexact));
      updateGuessImage(guessOutput);
      
      prepareNextGuess();
      
      this.hiddenSequence = this.winSequenceIMG;
    }
    
    void prepareNextGuess() {      
      int prevGuessLine = this.calcGuessLine();
      this.numGuesses++;
      
      if (this.calcGuessLine() != prevGuessLine) return;

      Double height = Math.ceil(this.guessSlots.getWidth());
      Double width = Math.ceil(this.guessSlots.getHeight()) - this.dotSquareSide;
      this.guessSlots = new CropImage(
          0, 0,
          height.intValue(),
          width.intValue(),
          this.guessSlots);
      this.guessSlots = new AboveAlignImage( AlignModeX.LEFT,
          this.genColorList(new MtLoInt(), this.sequenceLength),
          this.guessSlots);
      
    }
    
    WorldImage genHiddenSequence() {
      return new RectangleImage(
          this.sequenceLength * this.dotSquareSide,
          this.dotSquareSide, 
          OutlineMode.SOLID, Color.black);
    }
    
    WorldImage genResultSequence(ILoInt seq, String text) {
      WorldImage seqImg = genColorList(seq, seq.calcLength());
      
      WorldImage textBG = new RectangleImage(this.dotSquareSide*2, this.dotSquareSide, OutlineMode.SOLID, this.bgColor);
      
      seqImg = new BesideImage(
          seqImg,
          new OverlayImage(new TextImage(text, 20, Color.white), textBG));
      return seqImg;
    }
    
    WorldImage genAvailableColorsIMG() {
      return genColorList(this.colors);
    }
    
    void updateGuessSlots(ILoInt guess) {
      updateGuessImage(genColorList(guess, this.sequenceLength));
    }
    
    void updateGuessImage(WorldImage image) {
      this.guessSlots = new OverlayOffsetAlign(
          AlignModeX.LEFT, AlignModeY.BOTTOM,
          image,
          0, this.calcGuessLine() * this.dotSquareSide,
          this.guessSlots);
    }
    
    WorldImage genInitGuessSlots() {
      WorldImage emptySlots = genColorList(new MtLoInt(), this.sequenceLength);
      
      return doGenInitGuessSlots(this.screenGuesses, new EmptyImage(), emptySlots);
    }
    
    WorldImage doGenInitGuessSlots(int length, WorldImage img, WorldImage slots) {
      if (length < 1) return img;
      return doGenInitGuessSlots(length - 1, new AboveImage(slots, img), slots);
    }
    
    WorldImage genGuessResult(int exact, int inexact) {
      WorldImage box = new RectangleImage(
          this.dotSquareSide, 
          this.dotSquareSide,
          OutlineMode.SOLID,
          this.bgColor);
      WorldImage exactRes = new OverlayImage(
          new TextImage(Integer.toString(exact), 20, Color.white),
          box);
      WorldImage inexactRes = new OverlayImage(
          new TextImage(Integer.toString(inexact), 20, Color.white),
          box);
      return new BesideImage(exactRes, inexactRes);
    }
    
    WorldImage genColorList(ILoColor colList) {
      WorldImage initial = new EmptyImage();
      
      initial = colList.fold(
          initial,
          (init, col) -> new BesideImage(
              init,
              genFilledDot(col)));
      
      return initial;
    }
    
    WorldImage genColorList(ILoInt colList, int numSlots) {
      WorldImage initial = genEmptyDotsBeside(numSlots - colList.calcLength(), new EmptyImage());
      
      initial = colList.fold(
          initial,
          (init, col) -> new BesideImage(
              genFilledDot(col), 
              init));
      
      return initial;
    }
    
    WorldImage genEmptyDotsBeside(int length, WorldImage img) {
      if (length < 1) return img;
      return genEmptyDotsBeside(length - 1, new BesideImage(this.emptyDot, img));
    }
    
    WorldImage genFilledDot(Color col) {
      return new OverlayImage(
          new CircleImage(
              (this.dotSquareSide / 2) - this.dotRadiusGap,
              OutlineMode.SOLID, col),
          this.emptyDot);
    }
    
    WorldImage genFilledDot(int index) {
      return new OverlayImage(
          new CircleImage(
              (this.dotSquareSide / 2) - this.dotRadiusGap,
              OutlineMode.SOLID, this.getColor(index)),
          this.emptyDot);
    }
    
    Color getColor(int index) {
      return this.colors.get(index);
    }
    
    int calcGuessLine() {
      if (this.numGuesses >= this.screenGuesses-1) {
        if (this.numGuesses == this.maxGuesses-1) {
          return this.screenGuesses - 1;
        } else return this.screenGuesses - 2;
      }
      return this.numGuesses;
    }
  }
}


class ExamplesGame {
  boolean testBigBang(Tester t) {
    boolean  out = true;
    // no duplicates
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.yellow, Color.magenta, Color.green), 5,
        10, false);
    out |= game.startGame();
    
    // duplicates, screen scrolling
    Game game2 = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 8,
        20, true);
    out |= game2.startGame();
    
    // shortened view window
    Game shortGame = new Game(
        ConsLoColor.gen(Color.red, Color.orange, Color.magenta), 3,
        4, true);
    out |= shortGame.startGame();
    
    // set random
    Game gameSet = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 8,
        20, true, new Random(1234567));
    out |= gameSet.startGame();
    
    return out;
  }
  
  boolean testConstructorValidate(Tester t) {
    boolean out = true;
    
    // no duplicates, sequence exceeds color list
    out |= t.checkConstructorException(new IllegalArgumentException(), "Game", 
        ConsLoColor.gen(Color.red, Color.blue, Color.yellow, Color.magenta, Color.green),
        6,
        10,
        false);
    
    // 0 or less sequence length
    out |= t.checkConstructorException(new IllegalArgumentException(), "Game", 
        ConsLoColor.gen(Color.red, Color.blue, Color.yellow, Color.magenta, Color.green),
        0,
        10,
        true);
    out |= t.checkConstructorException(new IllegalArgumentException(), "Game", 
        ConsLoColor.gen(Color.red, Color.blue, Color.yellow, Color.magenta, Color.green),
        -1,
        10,
        true);
    
    // no colors
    out |= t.checkConstructorException(new IllegalArgumentException(), "Game", 
        new MtLoColor(),
        6,
        10,
        true);
    
    // no guesses
    out |= t.checkConstructorException(new IllegalArgumentException(), "Game", 
        ConsLoColor.gen(Color.red, Color.blue, Color.yellow, Color.magenta, Color.green),
        6,
        0,
        true);
    out |= t.checkConstructorException(new IllegalArgumentException(), "Game", 
        ConsLoColor.gen(Color.red, Color.blue, Color.yellow, Color.magenta, Color.green),
        6,
        -1,
        true);
    
    return out;
  }
  
  boolean testGenSequence(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 8,
        20, true, new Random(1));
    out |= t.checkExpect(game.genSequence(), ConsLoInt.gen(2, 3, 0, 3, 0, 0, 2, 3));
    
    // generic no duplicates set random
    Game game2 = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, false, new Random(1));
    out |= t.checkExpect(game2.genSequence(), ConsLoInt.gen(1, 3, 0, 2));
    
    return out;
  }
  
  boolean testGenSequenceDuplicates(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 8,
        20, true, new Random(1));
    out |= t.checkExpect(game.genSequenceDuplicates(8, new MtLoInt()), new MtLoInt());
    
    return out;
  }
  
  boolean testGenSequenceNoDuplicates(Tester t) {
    boolean out = true;

    // generic no duplicates set random
    Game game2 = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, false, new Random(1));
    out |= t.checkExpect(game2.genSequenceNoDuplicates(4, new MtLoInt(), ""), new MtLoInt());
    
    return out;
  }
  
  boolean testMakeScene(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white), 3,
        3, true, new Random(1));
    
    // initial blank scene:
    out |= t.checkExpect(game.makeScene(), new MtLoInt());
    
    // after inputting a guess:
    game.addGuess(1);
    game.addGuess(1);
    game.addGuess(1);
    game.addGuess(1);
    game.submitGuess();
    
    out |= t.checkExpect(game.makeScene(), new MtLoInt());
    
    return out;
  }
  
  boolean testOnKeyEvent(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    // number key out of range
    game.onKeyEvent("7");
    out |= t.checkExpect(game.guessLength, 0);
    
    // number key in range
    game.onKeyEvent("2");
    out |= t.checkExpect(game.guessLength, 1);
    out |= t.checkExpect(game.guess, ConsLoInt.gen(2));
    
    // delete 
    game.onKeyEvent("delete");
    out |= t.checkExpect(game.guessLength, 0);
    out |= t.checkExpect(game.guess, new MtLoInt());
    
    //
    game.onKeyEvent("2");
    game.onKeyEvent("2");
    game.onKeyEvent("2");
    game.onKeyEvent("2");
    
    // full guess number keys
    game.onKeyEvent("1");
    out |= t.checkExpect(game.guessLength, 4);
    out |= t.checkExpect(game.guess, ConsLoInt.gen(2, 2, 2, 2));
    
    // enter
    game.onKeyEvent("enter");
    
    out |= t.checkExpect(game.numGuesses, 1);
    out |= t.checkExpect(game.guessLength, 0);
    out |= t.checkExpect(game.guess, new MtLoInt());
    
    return out;
  }
  
  boolean testGenBlankResult(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    out |= t.checkExpect(game.genBlankResult(),
        new Result(0, new ConsLoInt(4), new ConsLoInt(4)));
    
    return out;
  }
  
  boolean testAddGuess(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    // guess in correct range
    game.addGuess(2);
    out |= t.checkExpect(game.guessLength, 1);
    out |= t.checkExpect(game.guess, ConsLoInt.gen(2));
    out |= t.checkNoException(game, "makeScene");
    
    // guess not in correct range
    out |= t.checkException(new IndexOutOfBoundsException(), game, "addGuess", 4);
    
    return out;
  }
  
  boolean testRemoveGuess(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    game.addGuess(1);
    game.addGuess(2);
    
    // remove when multiple guesses are present
    game.removeGuess();
    out |= t.checkExpect(game.guessLength, 1);
    out |= t.checkExpect(game.guess, ConsLoInt.gen(1));
    
    // remove when single guess is present
    game.removeGuess();
    out |= t.checkExpect(game.guessLength, 0);
    out |= t.checkExpect(game.guess, new MtLoInt());
    
    // remove when guess is blank
    game.removeGuess();
    out |= t.checkExpect(game.guessLength, 0);
    out |= t.checkExpect(game.guess, new MtLoInt());
    
    return out;
  }
  
  boolean testSubmitGuess(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    game.addGuess(1);
    game.addGuess(2);
    game.addGuess(2);
    game.addGuess(2);
    
    game.submitGuess();
    
    out |= t.checkExpect(game.numGuesses, 1);
    out |= t.checkExpect(game.guess, new MtLoInt());
    out |= t.checkExpect(game.guessLength, 0);
    
    return out;
  }
}

class ExamplesGameArt {
  
  //generic duplicates set random
  Game blankGame = new Game(
    ConsLoColor.gen(Color.red, Color.orange, Color.white, Color.magenta), 4,
    20, true, new Random(1));
  
  boolean testDoBigBang(Tester t) {
    boolean  out = true;
    // no duplicates
    Game game1 = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.yellow, Color.magenta, Color.green), 5,
        10, false);
    out |= game1.art.doBigBang(game1);
    
    // duplicates, screen scrolling
    Game game2 = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 8,
        20, true);
    out |= game2.art.doBigBang(game2);
    
    // shortened view window
    Game shortGame = new Game(
        ConsLoColor.gen(Color.red, Color.orange, Color.magenta), 3,
        4, true);
    out |= shortGame.art.doBigBang(shortGame);
    
    // set random
    Game gameSet = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 8,
        20, true, new Random(1234567));
    out |= gameSet.art.doBigBang(gameSet);
    
    return out;
  }
  
  boolean testProduceImage(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    // starting scene
    out |= t.checkExpect(game.art.produceImage(), new MtLoInt());
    
    // guess partially entered
    game.addGuess(0);
    game.addGuess(1);
    out |= t.checkExpect(game.art.produceImage(), new MtLoInt());
    
    // win scene
    game.guess = game.sequence;
    game.guessLength = game.sequenceLength;
    game.submitGuess();
    
    out |= t.checkExpect(game.art.produceImage(), new MtLoInt());
    
    return out;
  }
  
  boolean testSubmitFalseGuess(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    game.art.submitFalseGuess(ConsLoInt.gen(1,1,1,1), 2, 0);
    
    out |= t.checkExpect(game.art.produceImage(), new MtLoInt());
    
    return out;
  }
  
  boolean testSubmitCorrectGuess(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    game.art.submitCorrectGuess(game.sequence, 4, 0);
    
    out |= t.checkExpect(game.art.produceImage(), new MtLoInt());
    
    return out;
  }
  
  boolean testPrepareNextGuess(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    // elevated guess due to prepare next guess
    game.art.prepareNextGuess();
    game.addGuess(1);
    
    out |= t.checkExpect(game.art.produceImage(), new MtLoInt());
    
    // scrolling functionality:
    
    // generic duplicates set random
    Game game2 = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    // non scroll guesses
    for (int i = 0; i < 8; i++) {
      game2.guess = ConsLoInt.gen(1, 1, 1, 1);
      game2.guessLength = 4;
      game2.submitGuess();
    }
    
    // scroll down:
    game2.art.prepareNextGuess();
    
    out |= t.checkExpect(game2.art.produceImage(), new MtLoInt());
    
    return out;
  }
  
  boolean testGenHiddenSequence(Tester t) {
    boolean out = true;
    
    out |= t.checkExpect(blankGame.art.genHiddenSequence(), new MtLoInt());
    
    // higher square size:
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    game.art.dotSquareSide *= 2;
    
    out |= t.checkExpect(game.art.genHiddenSequence(), new MtLoInt());
    
    return out;
  }
  
  boolean testGenResultSequence(Tester t) {
    boolean out = true;
    
    out |= t.checkExpect(blankGame.art.genResultSequence(blankGame.sequence, "Win!"), new MtLoInt());
    out |= t.checkExpect(blankGame.art.genResultSequence(blankGame.sequence, "Other"), new MtLoInt());
    
    return out;
  }
  
  boolean testGenAvailableColorsIMG(Tester t) {
    boolean out = true;
    
    // 4 colors
    out |= t.checkExpect(blankGame.art.genAvailableColorsIMG(), new MtLoInt());
    
    // 6 colors
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray, Color.magenta, Color.yellow), 4,
        20, true, new Random(1));
    
    out |= t.checkExpect(game.art.genAvailableColorsIMG(), new MtLoInt());
    
    return out;
  }
  
  boolean testUpdateGuessSlosts(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    game.art.updateGuessSlots(ConsLoInt.gen(1, 1, 1, 1));
    
    out |= t.checkExpect(game.art.produceImage(), new MtLoInt());
    
    return out;
  }
  
  boolean testUpdateGuessImage(Tester t) {
    boolean out = true;
    
    // normal guess update process:
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    game.art.updateGuessImage(game.art.genColorList(ConsLoInt.gen(1, 1, 1, 1), 0));
    
    out |= t.checkExpect(game.art.produceImage(), new MtLoInt());
    
    // other image, different guessLine
    
    // generic duplicates set random
    Game game2 = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    game2.numGuesses = 5;
    
    game2.art.updateGuessImage(new CircleImage(10, OutlineMode.SOLID, Color.BLUE));
    
    out |= t.checkExpect(game2.art.produceImage(), new MtLoInt());
    
    return out;
  }
  
  boolean testGenInitGuessSlots(Tester t) {
    boolean out = true;
    
    // generic
    out |= t.checkExpect(blankGame.art.genInitGuessSlots(), new MtLoInt());
    
    // small game
    Game shortGame = new Game(
        ConsLoColor.gen(Color.red, Color.orange, Color.magenta), 3,
        4, true);
    
    out |= t.checkExpect(shortGame.art.genInitGuessSlots(), new MtLoInt());
    
    return out;
  }
  
  boolean testDoGenInitGuessSlots(Tester t) {
    boolean out = true;
    
    WorldImage emptySlots = blankGame.art.genColorList(new MtLoInt(), blankGame.art.sequenceLength);
    
    out |= t.checkExpect(blankGame.art.doGenInitGuessSlots(
        blankGame.art.screenGuesses, 
        new EmptyImage(),
        emptySlots), new MtLoInt());
    
    return out;
  }
  
  boolean testGenGuessResult(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    game.guess = ConsLoInt.gen(1, 1, 1, 1);
    
    out |= t.checkExpect(game.art.genGuessResult(1, 1), new MtLoInt());
    out |= t.checkExpect(game.art.genGuessResult(4, 2), new MtLoInt());
    
    return out;
  }
  
  boolean testGenColorListColorInput(Tester t) {
    boolean out = true;
    
    // generic color list
    ILoColor cols = ConsLoColor.gen(Color.red, Color.black, Color.green, Color.white);
    
    out |= t.checkExpect(blankGame.art.genColorList(cols), new MtLoInt());
    
    // empty color list
    out |= t.checkExpect(blankGame.art.genColorList(new MtLoColor()), new EmptyImage());
    
    return out;
  }
  
  boolean testGenColorListIntInput (Tester t) {
    boolean out = true;
    
    // generic color list
    ILoInt cols = ConsLoInt.gen(0, 1, 2, 3);
    
    // full
    out |= t.checkExpect(blankGame.art.genColorList(cols, 0), new MtLoInt());
    
    // with empty slots
    out |= t.checkExpect(blankGame.art.genColorList(cols, 3), new MtLoInt());
    
    // empty color list
    out |= t.checkExpect(blankGame.art.genColorList(new MtLoInt(), 0), new EmptyImage());
    out |= t.checkExpect(blankGame.art.genColorList(new MtLoInt(), 3), new MtLoInt());
    
    return out;
  }
  
  boolean testGenEmptyDotBeside (Tester t) {
    boolean out = true;
    
    // no dots
    out |= t.checkExpect(blankGame.art.genEmptyDotsBeside(0, new EmptyImage()), new EmptyImage());
    
    // 1 dot
    out |= t.checkExpect(blankGame.art.genEmptyDotsBeside(1, new EmptyImage()), new EmptyImage());
    
    // several dots
    out |= t.checkExpect(blankGame.art.genEmptyDotsBeside(4, new EmptyImage()), new EmptyImage());
    
    return out;
  }
  
  boolean testGenFilledDotColorInput(Tester t) {
    boolean out = true;
    
    // red
    out |= t.checkExpect(blankGame.art.genFilledDot(Color.red), new EmptyImage());
    
    // white
    out |= t.checkExpect(blankGame.art.genFilledDot(Color.white), new EmptyImage());
    
    return out;
  }
  
  boolean testGenFilledDotIntInput(Tester t) {
    boolean out = true;
    
    // 0 color
    out |= t.checkExpect(blankGame.art.genFilledDot(0), new EmptyImage());
    
    // 1 color
    out |= t.checkExpect(blankGame.art.genFilledDot(1), new EmptyImage());
    
    return out;
  }
  
  boolean testGetColor(Tester t) {
    boolean out = true;
    
    // color in bounds
    
    out |= t.checkExpect(blankGame.art.getColor(0), Color.red);
    
    // color out of bounds
    
    out |= t.checkException(new IndexOutOfBoundsException(), 
        blankGame.art, "getColor", 5);
    
    return out;
  }
  
  boolean testCalcGuessLine(Tester t) {
    boolean out = true;
    
    // generic duplicates set random
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.white, Color.gray), 4,
        20, true, new Random(1));
    
    // initial case
    t.checkExpect(game.art.calcGuessLine(), 0);
    
    // guess two case
    game.guess = ConsLoInt.gen(1, 1, 1, 1);
    game.guessLength = 4;
    game.submitGuess();
    
    t.checkExpect(game.art.calcGuessLine(), game.art.dotSquareSide);
    
    // guess nine case
    for (int i = 0; i < 7; i++) {
      game.guess = ConsLoInt.gen(1, 1, 1, 1);
      game.guessLength = 4;
      game.submitGuess();
    }
    
    t.checkExpect(game.art.calcGuessLine(), game.art.dotSquareSide * 8);
    
    // guess ten case (scrolling)
    game.guess = ConsLoInt.gen(1, 1, 1, 1);
    game.guessLength = 4;
    game.submitGuess();
    
    t.checkExpect(game.art.calcGuessLine(), game.art.dotSquareSide * 8);
    
    // guess 20 case (stop scrolling)
    for (int i = 0; i < 10; i++) {
      game.guess = ConsLoInt.gen(1, 1, 1, 1);
      game.guessLength = 4;
      game.submitGuess();
    }
    
    t.checkExpect(game.art.calcGuessLine(), game.art.dotSquareSide * 9);
    
    // guess 21 case (return down to indicate not to scroll)
    for (int i = 0; i < 10; i++) {
      game.guess = ConsLoInt.gen(1, 1, 1, 1);
      game.guessLength = 4;
      game.submitGuess();
    }
    
    t.checkExpect(game.art.calcGuessLine(), game.art.dotSquareSide * 8);
    
    return out;
  }
}