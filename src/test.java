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
  
  ILoInt sequence;
  
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
    
    this.validate();
  }
  
  private void validate() {
    if (this.sequenceLength < 1) throw new IllegalArgumentException();
    if (this.maxGuesses < 1) throw new IllegalArgumentException();
    if (this.numColors < 1) throw new IllegalArgumentException();
    if (!this.duplicatesAllowed && this.sequenceLength > this.numColors)
      throw new IllegalArgumentException();
  }
  
  public WorldScene makeScene() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public Game onKeyEvent(String key) {
    String nums = "123456789";
    if (nums.contains(key)) {
      if (this.guessLength < this.sequenceLength) {
        addGuess(Integer.parseInt(key));
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
  }
  
  private void removeGuess() {
    if (this.guessLength > 1) {
      this.guess = this.guess.remove();
      this.guessLength--;
    }
  }
  
  private void submitGuess() {
    Result res = this.guess.compare(this.sequence, this);
    //update image
  }
  
  class GameArt {
    ILoColor colors;
    
    int maxGuesses;
    int numGuesses = 0;
    
    WorldImage availableColors;
    WorldImage guessSlots;
    WorldImage correctSequence;
    WorldImage hiddenSequence;
    
    int dotSquareSide = 10;
    int dotRadiusGap = 2;
    Color bgColor = new Color(150, 0, 0);
    Color outlineColor = Color.black;
    
    WorldImage emptyDot;
    
    public GameArt(Game game) {
      this.maxGuesses = game.maxGuesses;
      this.colors = game.gameColors;
      
      this.emptyDot = new RectangleImage(
          this.dotSquareSide, 
          this.dotSquareSide,
          OutlineMode.SOLID,
          this.bgColor);
      this.emptyDot = new OverlayImage(this.emptyDot, 
          new CircleImage(
              this.dotSquareSide - this.dotRadiusGap,
              OutlineMode.OUTLINE, this.outlineColor));
    }
    
    public WorldImage produceImage() {
      return availableColors;
    }
    
    private Color getColor(int index) {
      return this.colors.get(index);
    }
    
    public WorldImage genFilledDot(Color col) {
      return new OverlayImage(this.emptyDot,
          new CircleImage(
              this.dotSquareSide - this.dotRadiusGap,
              OutlineMode.SOLID, col));
    }
    
    public WorldImage genFilledDot(int index) {
      return new OverlayImage(this.emptyDot,
          new CircleImage(
              this.dotSquareSide - this.dotRadiusGap,
              OutlineMode.SOLID, this.getColor(index)));
    }
    
    private WorldImage genColorList(ILoColor colList) {
      WorldImage initial = new EmptyImage();
      
      initial = colList.fold(
          initial,
          (init, col) -> new BesideImage(
              genFilledDot(col), 
              init));
      
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
  
}