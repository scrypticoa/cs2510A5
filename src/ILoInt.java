import java.awt.Color;
import java.util.function.BiFunction;
import tester.Tester;

interface ILoInt {
  // add 1 to the int at position pos
  ILoInt addAtPos(int pos);

  // adds a int to the front of the list
  ILoInt insert(int num);

  // removes the first int in the list
  ILoInt remove();

  // creates a copy the list
  ILoInt clone();

  // starts the comparison of 2 lists
  Result compare(ILoInt other, Game game);

  // helper to find the first of the other list
  Result comparePass(ILoInt other, Game game, Result result);

  // compares two list to find exact and inexact matches
  Result doCompare(ILoInt other, Game game, Result result, int num);

  // finds sum of list
  int sum();

  // returns the minimum at each index between this and list, stopping when either
  // list
  // ends, passes this first value to the compared list
  ILoInt mins(ILoInt list);

  // helper for mins, which receives the other compared list's first value, and
  // generates the minimum for the current index
  ILoInt doMins(int otherFirst, ILoInt otherRest);

  // finds the length of a list
  int calcLength();

  // modifies input value according to index value and passes it to next element,
  // Returns input at the end of list
  <T> T fold(T initial, BiFunction<T, Integer, T> folder);

  // puts the list into a string in order
  String print();
}

class MtLoInt implements ILoInt {

  /*TEMPLATE:
   * METHODS:
   * this.addAtPos(int pos) ... ILoInt
   * this.insert(int num) ... ILoInt
   * this.remove() ... ILoInt
   * this.clone() ... ILoInt
   * this.compare(ILoInt other, Game game) ... Result
   * this.comparePass(ILoInt other, Game game, Result result) ... Result
   * this.doCompare(ILoInt other, Game game, Result result, int num) ... Result
   * this.sum() ... int
   * this.mins(ILoInt list) ... ILoInt
   * this.doMins(int otherFirst, ILoInt otherRest) ... ILoInt
   * this.fold(T initial, BiFunction<T, Integer, T> folder) ... <T>
   * this.print() ... String
   */

  // add 1 to the int at position pos
  public ILoInt addAtPos(int pos) {
    throw new IndexOutOfBoundsException();
  }

  // adds a int to the front of the list
  public ILoInt insert(int num) {
    return new ConsLoInt(num, new MtLoInt());
  }

  // removes the first int in the list
  public ILoInt remove() {
    return new MtLoInt();
  }

  // creates a copy the list
  public ILoInt clone() {
    return new MtLoInt();
  }

  // starts the comparison of 2 lists
  public Result compare(ILoInt other, Game game) {
    return game.genBlankResult();
    // error
  }

  // helper to find the first of the other list
  public Result comparePass(ILoInt other, Game game, Result result) {
    return result;
  }

  // compares two list to find exact and inexact matches
  public Result doCompare(ILoInt other, Game game, Result result, int num) {
    return game.genBlankResult();
    // error
  }

  // finds sum of list
  public int sum() {
    return 0;
  }

  // returns the minimum at each index between this and list, stopping when either
  // list
  // ends, passes this first value to the compared list
  public ILoInt mins(ILoInt list) {
    return new MtLoInt();
  }

  // helper for mins, which receives the other compared list's first value, and
  // generates the minimum for the current index
  public ILoInt doMins(int otherFirst, ILoInt otherRest) {
    return new MtLoInt();
  }

  // finds the lenght of a list
  public int calcLength() {
    return 0;
  }

  // modifies input value according to index value and passes it to next element,
  // Returns input at the end of list
  public <T> T fold(T initial, BiFunction<T, Integer, T> folder) {
    return initial;
  }

  // puts the list into a string in order
  public String print() {
    return "";
  }
}

class ConsLoInt implements ILoInt {
  int first;
  ILoInt rest;

  ConsLoInt(int first, ILoInt rest) {
    this.first = first;
    this.rest = rest;
  }

  ConsLoInt(int size) throws IllegalArgumentException {
    if (size < 1)
      throw new IllegalArgumentException(
          "ConsLoInt zero constructor cannot accpet size less than one");
    first = 0;
    if (size > 1) {
      rest = new ConsLoInt(size - 1);
    }
    else {
      rest = new MtLoInt();
    }
  }

  static ConsLoInt gen(int firstNum, int... nums) { // purely for testing, not used in game logic
    ILoInt temp = new MtLoInt();
    for (int i = nums.length - 1; i > -1; i--) {
      temp = new ConsLoInt(nums[i], temp);
    }
    return new ConsLoInt(firstNum, temp);
  }

  /*
   * TEMPLATE: FIELDS: this.first ... int this.rest ... ILoInt
   * 
   * METHODS: this.addAtPos(int pos) ... ILoInt this.insert(int num) ... ILoInt
   * this.remove() ... ILoInt this.clone() ... ILoInt this.compare(ILoInt other,
   * Game game) ... Result this.comparePass(ILoInt other, Game game, Result
   * result) ... Result this.doCompare(ILoInt other, Game game, Result result, int
   * num) ... Result this.sum() ... int this.mins(ILoInt list) ... ILoInt
   * this.minsCompare(int otherFirst) ... int this.minsRHelp(ILoInt list) ...
   * ILoint this.fold(T initial, BiFunction<T, Integer, T> folder) ... <T>
   * this.print() ... String
   * 
   * METHODS OF FIELDS: rest.addAtPos(int pos) ... ILoInt rest.insert(int num) ...
   * ILoInt rest.remove() ... ILoInt rest.clone() ... ILoInt rest.compare(ILoInt
   * other, Game game) ... Result rest.comparePass(ILoInt other, Game game, Result
   * result) ... Result rest.doCompare(ILoInt other, Game game, Result result, int
   * num) ... Result rest.sum() ... int rest.mins(ILoInt list) ... ILoInt
   * rest.minsCompare(int otherFirst) ... int rest.minsRHelp(ILoInt list) ...
   * ILoint rest.fold(T initial, BiFunction<T, Integer, T> folder) ... <T>
   * rest.print() ... String
   */

  // adds one to the given pos
  public ILoInt addAtPos(int pos) {
    if (pos == 0) {
      return new ConsLoInt(this.first + 1, this.rest.clone());
    }
    return new ConsLoInt(this.first, this.rest.addAtPos(pos - 1));
  }

  // adds a int to the front of the list
  public ILoInt insert(int num) {
    return new ConsLoInt(num, this.clone());
  }

  // removes the first int in the list
  public ILoInt remove() {
    return this.rest.clone();
  }

  // creates a copy the list
  public ILoInt clone() {
    return new ConsLoInt(this.first, this.rest.clone());
  }

  // starts the comparison of 2 lists
  public Result compare(ILoInt other, Game game) {
    Result result = game.genBlankResult();
    return this.comparePass(other, game, result);
  }

  // helper to find the first of the other list
  public Result comparePass(ILoInt other, Game game, Result result) {
    return other.doCompare(this.rest, game, result, this.first);
  }

  // compares two list to find exact and inexact matches
  public Result doCompare(ILoInt other, Game game, Result result, int num) {
    if (num == this.first) {
      result.addExact();
    }
    else {
      result.addInexact1(num);
      result.addInexact2(this.first);
    }

    return other.comparePass(this.rest, game, result);
  }

  // finds the sum of the list
  public int sum() {
    return this.first + this.rest.sum();
  }

  // returns the minimum at each index between this and list, stopping when either
  // list
  // ends, passes this first value to the compared list
  public ILoInt mins(ILoInt list) {
    return list.doMins(this.first, this.rest);
  }

  // helper for mins, which receives the other compared list's first value, and
  // generates the minimum for the current index
  public ILoInt doMins(int otherFirst, ILoInt otherRest) {
    return new ConsLoInt(Math.min(this.first, otherFirst), otherRest.mins(this.rest));
  }

  // finds the lenght of a list
  public int calcLength() {
    return 1 + this.rest.calcLength();
  }

  // modifies input value according to index value and passes it to next element,
  // Returns input at the end of list
  public <T> T fold(T initial, BiFunction<T, Integer, T> folder) {
    return this.rest.fold(folder.apply(initial, this.first), folder);
  }

  // puts the list into a string in order
  public String print() {
    return this.first + this.rest.print();
  }
}

//contains tests for ILoInt
class ExamplesILoInt {
  ILoInt counting = new ConsLoInt(1,
      new ConsLoInt(2, new ConsLoInt(3, new ConsLoInt(4, new MtLoInt()))));

  ILoInt countingGen = ConsLoInt.gen(1, 2, 3, 4);

  ILoInt reverseCounting = ConsLoInt.gen(4, 3, 2, 1);

  ILoInt singleGen = ConsLoInt.gen(1);

  ILoInt empty = new MtLoInt();

  // tests zerosConstructor
  public boolean testZerosConstructor(Tester t) {
    return t
        .checkConstructorException(new IllegalArgumentException(
            "ConsLoInt zero constructor cannot accpet size less than one"), "ConsLoInt", 0)
        && t.checkExpect(new ConsLoInt(5), ConsLoInt.gen(0, 0, 0, 0, 0));
  }

  // tests gen
  public boolean testGen(Tester t) {
    return t.checkExpect(countingGen, counting)
        && t.checkExpect(singleGen, new ConsLoInt(1, new MtLoInt()));
  }

  // tests addAtPos
  public boolean testAddAtPos(Tester t) {
    return t.checkExpect(counting.addAtPos(2), ConsLoInt.gen(1, 2, 4, 4))
        && t.checkException(new IndexOutOfBoundsException(), counting, "addAtPos", 4);
  }

  // tests insert
  public boolean testInsert(Tester t) {
    return t.checkExpect(empty.insert(5), ConsLoInt.gen(5))
        && t.checkExpect(counting.insert(0), ConsLoInt.gen(0, 1, 2, 3, 4));
  }

  // tests remove
  public boolean testRemove(Tester t) {
    return t.checkExpect(empty.remove(), empty)
        && t.checkExpect(counting.remove(), ConsLoInt.gen(2, 3, 4));
  }

  // tests clone
  public boolean testClone(Tester t) {
    return t.checkExpect(counting.clone(), counting);
  }

  // tests compare
  public boolean testCompare(Tester t) {
    Game dummyGame = new Game(
        ConsLoColor.gen(Color.black, Color.red, Color.white, Color.cyan, Color.orange), 4, 9, true);
    Result res = counting.compare(reverseCounting, dummyGame);
    return t.checkExpect(res.calcInexactCount(), 4);
  }

  // tests sum
  public boolean testSum(Tester t) {
    return t.checkExpect(counting.sum(), 10) && t.checkExpect(empty.sum(), 0);
  }

  // tests mins
  public boolean testMins(Tester t) {
    return t.checkExpect(counting.mins(empty), empty)
        && t.checkExpect(counting.mins(reverseCounting), ConsLoInt.gen(1, 2, 2, 1))
        && t.checkExpect(empty.mins(counting), empty)
        && t.checkExpect(counting.mins(singleGen), singleGen);
  }

  // tests print
  public boolean testPrint(Tester t) {
    // tests a genric list
    return t.checkExpect(counting.print(), "1234")
        // test an empty
        && t.checkExpect(empty.print(), "");
  }

  // tests calcLength
  public boolean testCalcLength(Tester t) {
    // tests a genric list
    return t.checkExpect(counting.calcLength(), 4)
        // test an empty
        && t.checkExpect(empty.calcLength(), 0);
  }
}