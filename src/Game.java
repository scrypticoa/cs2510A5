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
    
    this.guess = new MtLoInt();
    this.guessLength = 0;
    
    this.rand = setRand;
    
    this.sequence = this.genSequence();
    System.out.println(sequence.print());
    
    this.art = new GameArt(this);
    
    this.validate();
  }
  
  public Game(ILoColor gameColors, int sequenceLength,
      int attemptCount, boolean duplicatesAllowed) {
    this(gameColors, sequenceLength, attemptCount, duplicatesAllowed, new Random());
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
    }
    
    public boolean doBigBang(Game game) {
      return game.bigBang(this.gameWidth.intValue(), this.height, 10);
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
      
      WorldScene worldScene = new WorldScene(gameWidth.intValue(), this.height);
      worldScene = worldScene.placeImageXY(screen, this.gameWidth.intValue() / 2, this.height / 2);
      
      return worldScene;
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
      this.guessSlots = new AboveAlignImage( AlignModeX.LEFT,
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
