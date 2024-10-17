import tester.*;
import java.util.Random;
import java.util.function.*;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;

interface ILoColor {
  int calcLength();
  Color get(int index);
  <T> T fold(T initial, BiFunction<T, Color, T> folder);
}

class MtLoColor implements ILoColor {
  public int calcLength() {
    return 0;
  }
  
  public Color get(int index) {
    throw new IndexOutOfBoundsException();
  }
  
  public <T> T fold(T initial, BiFunction<T, Color, T> folder) {
    return initial;
  }
}

class ConsLoColor implements ILoColor {
  Color first;
  ILoColor rest;
  
  public ConsLoColor(Color first, ILoColor rest) {
    this.first = first;
    this.rest = rest;
  }
  
  static ConsLoColor gen(Color firstColor, Color...cols) {
    ILoColor temp = new MtLoColor();
    for (int i = cols.length-1; i > -1; i--) {
      temp = new ConsLoColor(cols[i], temp);
    }
    return new ConsLoColor(firstColor, temp);
  }
  
  public int calcLength() {
    return 1 + this.rest.calcLength();
  }
  
  public Color get(int index) {
    if (index == 0) {
      return this.first;
    }
    return this.rest.get(index-1);
  }
  
  public <T> T fold(T initial, BiFunction<T, Color, T> folder) {
    return this.rest.fold(
        folder.apply(initial, this.first), 
        folder);
  }
}

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
      int attemptCount, boolean duplicatesAllowed) {
    this.gameColors = gameColors;
    this.sequenceLength = sequenceLength;
    this.maxGuesses = attemptCount;
    this.duplicatesAllowed = duplicatesAllowed;
    
    this.numColors = gameColors.calcLength();
    
    this.guess = new MtLoInt();
    this.guessLength = 0;
    
    this.rand = new Random();
    this.sequence = this.genSequence();
    System.out.println(sequence.print());
    
    this.art = new GameArt(this);
    
    this.validate();
  }
  
  private void validate() {
    if (this.sequenceLength < 1) throw new IllegalArgumentException();
    if (this.maxGuesses < 1) throw new IllegalArgumentException();
    if (this.numColors < 1) throw new IllegalArgumentException();
    if (!this.duplicatesAllowed && this.sequenceLength > this.numColors)
      throw new IllegalArgumentException();
  }
  
  private ILoInt genSequence() {
    if (duplicatesAllowed) {
      ILoInt seq = new MtLoInt();
      for (int i = 0; i < this.sequenceLength; i++) {
        seq = seq.insert(this.rand.nextInt(this.numColors));
      }
      return seq;
    }
    String seen = "";
    ILoInt seq = new MtLoInt();
    for (int i = 0; i < this.sequenceLength; i++) {
      Integer val = -1;
      do {
        val = this.rand.nextInt(this.numColors);
      } while(seen.contains(val.toString()));
      seq = seq.insert(val);
      seen += val;
    }
    return seq;
  }
  
  public boolean startGame() {
    return art.doBigBang(this);
  }
  
  @Override
  public WorldScene makeScene() {
    return art.produceImage();
  }
  
  @Override
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
  
  public Result genBlankResult() {
    return new Result(
        0,
        new ConsLoInt(this.numColors),
        new ConsLoInt(this.numColors));
  }
  
  private void addGuess(int color) {
    this.guess = this.guess.insert(color);
    this.guessLength++;
    
    this.art.updateGuessSlots(this.guess);
  }
  
  private void removeGuess() {
    if (this.guessLength > 0) {
      this.guess = this.guess.remove();
      this.guessLength--;
    }
    
    this.art.updateGuessSlots(this.guess);
  }
  
  private void submitGuess() {
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
    
    WorldScene worldScene;
    int height = 200;
    
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
    
    Double gameWidth;
    
    WorldImage emptyDot;
    
    public GameArt(Game game) {
      this.maxGuesses = game.maxGuesses;
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
      this.winSequenceIMG = this.genSequence(game.sequence, "Win!");
      this.loseSequenceIMG = this.genSequence(game.sequence, "Lose!");
      this.hiddenSequence = this.genHiddenSequence();
      
      this.gameWidth = Math.max(
          this.availableColorsIMG.getWidth(),
          this.guessSlots.getWidth() + (this.dotSquareSide * 2));
      this.gameWidth = Math.max(this.gameWidth, this.hiddenSequence.getWidth());
      this.gameWidth = Math.max(this.gameWidth, this.winSequenceIMG.getWidth());
      this.gameWidth = Math.ceil(Math.max(this.gameWidth, this.loseSequenceIMG.getWidth()));      
    
      WorldImage screen = new AboveAlignImage(
          AlignModeX.LEFT,
          this.hiddenSequence,
          this.guessSlots,
          this.availableColorsIMG);
      Double height = Math.ceil(screen.getHeight());
      this.height = height.intValue();
      
      this.worldScene = new WorldScene(gameWidth.intValue(), this.height);
    }
    
    public boolean doBigBang(Game game) {
      return game.bigBang(this.gameWidth.intValue(), this.height, 2);
    }
    
    public WorldScene produceImage() {
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
              gameWidth.intValue(), screenHeight.intValue(),
              OutlineMode.SOLID, this.bgColor));
      
      /*this.worldScene = worldScene.placeImageXY(
          new RectangleImage(this.gameWidth.intValue(), this.height, OutlineMode.SOLID, this.bgColor),
          this.gameWidth.intValue() / 2, this.height / 2);*/
      this.worldScene = worldScene.placeImageXY(screen, this.gameWidth.intValue() / 2, this.height / 2);
      
      return this.worldScene;
    }
    
    public void submitFalseGuess(ILoInt guess, int exact, int inexact) {
      WorldImage guessOutput = new BesideImage(
          genColorList(guess, this.sequenceLength),
          genGuessResult(exact, inexact));
      updateGuessImage(guessOutput);
      
      prepareNextGuess();
      
      if (this.numGuesses == this.maxGuesses) {
        this.hiddenSequence = this.loseSequenceIMG;
      }
    }
    
    public void submitCorrectGuess(ILoInt guess, int exact, int inexact) {
      WorldImage guessOutput = new BesideImage(
          genColorList(guess, this.sequenceLength),
          genGuessResult(exact, inexact));
      updateGuessImage(guessOutput);
      
      prepareNextGuess();
      
      this.hiddenSequence = this.winSequenceIMG;
    }
    
    private void prepareNextGuess() {      
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
      this.guessSlots = new AboveImage(
          this.genColorList(new MtLoInt(), this.sequenceLength),
          this.guessSlots);
      
    }
    
    private WorldImage genHiddenSequence() {
      return new RectangleImage(
          this.sequenceLength * this.dotSquareSide,
          this.dotSquareSide, 
          OutlineMode.SOLID, Color.black);
    }
    
    private WorldImage genSequence(ILoInt seq, String text) {
      WorldImage seqImg = genColorList(seq, seq.calcLength());
      
      WorldImage textBG = new RectangleImage(this.dotSquareSide*2, this.dotSquareSide, OutlineMode.SOLID, this.bgColor);
      
      seqImg = new BesideImage(
          seqImg,
          new OverlayImage(new TextImage(text, 20, Color.white), textBG));
      return seqImg;
    }
    
    private WorldImage genAvailableColorsIMG() {
      return genColorList(this.colors);
    }
    
    public void updateGuessSlots(ILoInt guess) {
      updateGuessImage(genColorList(guess, this.sequenceLength));
    }
    
    private void updateGuessImage(WorldImage image) {
      this.guessSlots = new OverlayOffsetAlign(
          AlignModeX.LEFT, AlignModeY.BOTTOM,
          image,
          0, this.calcGuessLine() * this.dotSquareSide,
          this.guessSlots);
    }
    
    private WorldImage genInitGuessSlots() {
      WorldImage initial = new EmptyImage();
      for (int i = 0; i < this.screenGuesses; i++) {
        initial = new AboveImage(
            genColorList(new MtLoInt(), this.sequenceLength),
            initial);
      }
      return initial;
    }
    
    private WorldImage genGuessResult(int exact, int inexact) {
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
    
    private WorldImage genColorList(ILoColor colList) {
      WorldImage initial = new EmptyImage();
      
      initial = colList.fold(
          initial,
          (init, col) -> new BesideImage(
              init,
              genFilledDot(col)));
      
      return initial;
    }
    
    private WorldImage genColorList(ILoInt colList, int numSlots) {
      WorldImage initial = new EmptyImage();
      
      for (int i = colList.calcLength(); i < numSlots; i++) {
         initial = new BesideImage(this.emptyDot, initial);
      }
      
      initial = colList.fold(
          initial,
          (init, col) -> new BesideImage(
              genFilledDot(col), 
              init));
      
      return initial;
    }
    
    public WorldImage genFilledDot(Color col) {
      return new OverlayImage(
          new CircleImage(
              (this.dotSquareSide / 2) - this.dotRadiusGap,
              OutlineMode.SOLID, col),
          this.emptyDot);
    }
    
    public WorldImage genFilledDot(int index) {
      return new OverlayImage(
          new CircleImage(
              (this.dotSquareSide / 2) - this.dotRadiusGap,
              OutlineMode.SOLID, this.getColor(index)),
          this.emptyDot);
    }
    
    private Color getColor(int index) {
      return this.colors.get(index);
    }
    
    private int calcGuessLine() {
      if (this.numGuesses >= this.screenGuesses-1) {
        if (this.numGuesses == this.maxGuesses-1) {
          return this.screenGuesses - 1;
        } else return this.screenGuesses - 2;
      }
      return this.numGuesses;
    }
  }
}

class Result {
  int exact;
  ILoInt inexactCount1;
  ILoInt inexactCount2;
  
  Result(int exact,  ILoInt inexactCount1, ILoInt inexactCount2){
    this.exact = exact;
    this.inexactCount1 = inexactCount1;
    this.inexactCount2 = inexactCount2;
  }
  
  public int addExact() {
    exact++;
    return exact;
  }
  
  public ILoInt addInexact1(int pos) {
    this.inexactCount1 = this.inexactCount1.addAtPos(pos);
    return this.inexactCount1;
  }
  
  public ILoInt addInexact2(int pos) {
    this.inexactCount2 = this.inexactCount2.addAtPos(pos);
    return this.inexactCount2;
  }
  
  public int calcInexactCount() {
    ILoInt mins = inexactCount1.mins(inexactCount2);
    return mins.sum();
  }
  
  public boolean didWin(int sequenceLength) {
    return this.exact == sequenceLength;
  }
}

interface ILoInt{
  //add 1 to the int at position pos
  ILoInt addAtPos(int pos);
  
  ILoInt insert(int num);
  
  ILoInt remove();
  
  ILoInt clone();
  
  Result compare(ILoInt other, Game game);
  Result comparePass(ILoInt other, Game game, Result result);
  Result doCompare(ILoInt other, Game game, Result result, int num);
  //finds sum of list
  int sum();
  
  // returns the minimum at each index between this and list, stopping when either list
  // ends, passes this first value to the compared list
  ILoInt mins(ILoInt list);
  // helper for mins, which receives the other compared list's first value, and
  // generates the minimum for the current index
  ILoInt doMins(int otherFirst, ILoInt otherRest);
  
  int calcLength();
  
  <T> T fold(T initial, BiFunction<T, Integer, T> folder);
  
  String print();
}

class MtLoInt implements ILoInt{
  /*TEMPLATE:
   * METHODS:
   * addAtPos(int pos) ... ILoInt
   * insert(int num) ... ILoInt
   * remove() ... ILoInt
   * clone() ... ILoInt
   * compare(ILoInt other, Game game) ... Result
   * comparePass(ILoInt other, Game game, Result result) ... Result
   * doCompare(ILoInt other, Game game, Result result, int num) ... Result
   * sum() ... int
   * mins(ILoInt list) ... ILoInt
   * doMins(int otherFirst, ILoInt otherRest) ... ILoInt
   */
  
  public ILoInt addAtPos(int pos) {
    throw new IndexOutOfBoundsException();
  }
  
  public ILoInt insert(int num) {
    return new ConsLoInt(num, new MtLoInt());
  }
  
  public ILoInt remove() {
    return new MtLoInt();
  }
  
  public ILoInt clone() {
    return new MtLoInt();
  }
  
  public Result compare(ILoInt other, Game game) {
    return game.genBlankResult();
    // error
  }
  
  public Result comparePass(ILoInt other, Game game, Result result) {
    return result;
  }
  
  public Result doCompare(ILoInt other, Game game, Result result, int num) {
    return game.genBlankResult();
    // error
  }

  public int sum() {
    return 0;
  }
  
  public ILoInt mins(ILoInt list) {
    return new MtLoInt();
  }

  public ILoInt doMins(int otherFirst, ILoInt otherRest) {
    return new MtLoInt();
  }
  
  public int calcLength() {
    return 0;
  }
  
  public <T> T fold(T initial, BiFunction<T, Integer, T> folder) {
    return initial;
  }
  
  public String print() {
    return "";
  }
}

class ConsLoInt implements ILoInt{
  int first;
  ILoInt rest;
  
  ConsLoInt(int first, ILoInt rest){
    this.first = first;
    this.rest = rest;
  }
  
  ConsLoInt(int size) throws IllegalArgumentException {
    if (size < 1) throw new IllegalArgumentException("ConsLoInt zero constructor cannot accpet size less than one");
    first = 0;
    if(size > 1) {
      rest =  new ConsLoInt(size-1);
    } else {
      rest = new MtLoInt();
    }
  }
  
  static ConsLoInt gen(int firstNum, int...nums) {
    ILoInt temp = new MtLoInt();
    for (int i = nums.length-1; i > -1; i--) {
      temp = new ConsLoInt(nums[i], temp);
    }
    return new ConsLoInt(firstNum, temp);
  }
  
  /*TEMPLATE:
   * FIELDS:
   * this.first ... int
   * this.rest ... ILoInt
   * METHODS:
   * addAtPos(int pos) ... ILoInt
   * insert(int num) ... ILoInt
   * remove() ... ILoInt
   * clone() ... ILoInt
   * compare(ILoInt other, Game game) ... Result
   * comparePass(ILoInt other, Game game, Result result) ... Result
   * doCompare(ILoInt other, Game game, Result result, int num) ... Result
   * sum() ... int
   * mins(ILoInt list) ... ILoInt
   * minsCompare(int otherFirst) ... int
   * minsRHelp(ILoInt list) ... ILoint
   * METHODS OF FIELDS
   * rest.addAtPos(int pos) ... ILoInt
   * rest.insert(int num) ... ILoInt
   * rest.remove() ... ILoInt
   * rest.clone() ... ILoInt
   * rest.compare(ILoInt other, Game game) ... Result
   * rest.comparePass(ILoInt other, Game game, Result result) ... Result
   * rest.doCompare(ILoInt other, Game game, Result result, int num) ... Result
   * rest.sum() ... int
   * rest.mins(ILoInt list) ... ILoInt
   * rest.minsCompare(int otherFirst) ... int
   * rest.minsRHelp(ILoInt list) ... ILoint
   */
  
  public ILoInt addAtPos(int pos) {
    if(pos == 0){
      return new ConsLoInt(this.first + 1, this.rest.clone());
    }
    return new ConsLoInt(this.first, this.rest.addAtPos(pos-1));
  }
  
  public ILoInt insert(int num) {
    return new ConsLoInt(num, this.clone());
  }
  
  public ILoInt remove() {
    return this.rest.clone();
  }
  
  public ILoInt clone() {
    return new ConsLoInt(this.first, this.rest.clone());
  }
  
  public Result compare(ILoInt other, Game game) {
    Result result = game.genBlankResult();
    return this.comparePass(other, game, result);
  }
  
  public Result comparePass(ILoInt other, Game game, Result result) {
    return other.doCompare(this.rest, game, result, this.first);
  }
  
  public Result doCompare(ILoInt other, Game game, Result result, int num) {
    if (num == this.first) {
      result.addExact();
    } else {
      result.addInexact1(num);
      result.addInexact2(this.first);
    }
    
    return other.comparePass(this.rest, game, result);
  }

  public int sum() {
    return this.first + this.rest.sum();
  }
  
  public ILoInt mins(ILoInt list) {
    return list.doMins(this.first, this.rest);
  }

  public ILoInt doMins(int otherFirst, ILoInt otherRest) {
    return new ConsLoInt(Math.min(this.first, otherFirst), otherRest.mins(this.rest));
  }
  
  public int calcLength() {
    return 1 + this.rest.calcLength();
  }
  
  public <T> T fold(T initial, BiFunction<T, Integer, T> folder) {
    return this.rest.fold(
        folder.apply(initial, this.first), 
        folder);
  }
  
  public String print() {
    return this.first + this.rest.print();
  }
}

class ExamplesILoInt {
  ILoInt counting = new ConsLoInt(1, 
      new ConsLoInt(2, 
          new ConsLoInt(3, 
              new ConsLoInt(4, 
                  new MtLoInt()))));
  
  ILoInt countingGen = ConsLoInt.gen(1, 2, 3, 4);
  
  ILoInt reverseCounting = ConsLoInt.gen(4, 3, 2, 1);
  
  ILoInt singleGen = ConsLoInt.gen(1);
  
  ILoInt empty = new MtLoInt();
  
  void testZerosConstructor(Tester t) {
    t.checkConstructorException(
        new IllegalArgumentException("ConsLoInt zero constructor cannot accpet size less than one"), 
        "ConsLoInt", 
        0);
    t.checkExpect(new ConsLoInt(5), ConsLoInt.gen(0, 0, 0, 0, 0));
  }
  
  void testGen(Tester t) {
    t.checkExpect(countingGen, counting);
    t.checkExpect(singleGen, new ConsLoInt(1, new MtLoInt()));
  }
  
  void testAddAtPos(Tester t) {
    t.checkExpect(counting.addAtPos(2), ConsLoInt.gen(1, 2, 4, 4));
    t.checkException(new IndexOutOfBoundsException(), counting, "addAtPos", 4);
  }
  
  void testInsert(Tester t) {
    t.checkExpect(empty.insert(5), ConsLoInt.gen(5));
    t.checkExpect(counting.insert(0), ConsLoInt.gen(0, 1, 2, 3, 4));
  }
  
  void testRemove(Tester t) {
    t.checkExpect(empty.remove(), empty);
    t.checkExpect(counting.remove(), ConsLoInt.gen(2, 3, 4));
  }
  
  void testClone(Tester t) {
    t.checkExpect(counting.clone(), counting);
  }
  
  void testCompare(Tester t) {
    Game dummyGame = new Game(ConsLoColor.gen(Color.black, Color.red, Color.white, Color.cyan, Color.orange), 4, 9, true);
    Result res = counting.compare(reverseCounting, dummyGame);
    t.checkExpect(res.calcInexactCount(), 4);
  }
  
  void testSum(Tester t) {
    t.checkExpect(counting.sum(), 10);
    t.checkExpect(empty.sum(), 0);
  }
  
  void testMins(Tester t) {
    t.checkExpect(counting.mins(empty), empty);
    t.checkExpect(counting.mins(reverseCounting), ConsLoInt.gen(1, 2, 2, 1));
    t.checkExpect(empty.mins(counting), empty);
    t.checkExpect(counting.mins(singleGen), singleGen);
  }
}

class ExamplesMastermind {
  boolean testBigBang(Tester t) {
    Game game = new Game(
        ConsLoColor.gen(Color.red, Color.blue, Color.yellow), 5,
        10, true);
    return game.startGame();
  }
}