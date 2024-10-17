import tester.Tester;
//creates an object to hold info about exact and inexact guesses
class Result {
  int exact;
  ILoInt inexactCount1;
  ILoInt inexactCount2;
  
  Result(int exact,  ILoInt inexactCount1, ILoInt inexactCount2){
    this.exact = exact;
    this.inexactCount1 = inexactCount1;
    this.inexactCount2 = inexactCount2;
  }
  
  /* TEMPLATE
   * FIELDS:
   * this.exact ... int
   * this.inexactCount1 ... ILoInt
   * this.inexactCount2 ... ILoInt
   * 
   * METHODS:
   * this.addExact() ... int
   * this.addInexact1(int pos) ... ILoInt
   * this.addInexact2(int pos) ... ILoInt
   * this.calcInexactCount() ... int
   * this.didWin(int sequenceLength) ... boolean
   * 
   * METHODS OF FIELDS:
   * inexactCount1.addExact() ... int
   * inexactCount1.addInexact1(int pos) ... ILoInt
   * inexactCount1.addInexact2(int pos) ... ILoInt
   * inexactCount1.calcInexactCount() ... int
   * inexactCount1.didWin(int sequenceLength) ... boolean
   * inexactCount2.addExact() ... int
   * inexactCount2.addInexact1(int pos) ... ILoInt
   * inexactCount2.addInexact2(int pos) ... ILoInt
   * inexactCount2.calcInexactCount() ... int
   * inexactCount2.didWin(int sequenceLength) ... boolean
   */
  
  // adds one to exact
  public int addExact() {
    exact++;
    return exact;
  }
  
  // adds one at the position of pos on InexactCount1
  public ILoInt addInexact1(int pos) {
    this.inexactCount1 = this.inexactCount1.addAtPos(pos);
    return this.inexactCount1;
  }
  
  //adds one at the position of pos on InexactCount2
  public ILoInt addInexact2(int pos) {
    this.inexactCount2 = this.inexactCount2.addAtPos(pos);
    return this.inexactCount2;
  }
  
  // calculates the minimum of the guess vs key and then sums the min list to find
  // the amount of in exact
  public int calcInexactCount() {
    ILoInt mins = inexactCount1.mins(inexactCount2);
    return mins.sum();
  }
  
  // sees if the guess was correct
  public boolean didWin(int sequenceLength) {
    return this.exact == sequenceLength;
  }
}

//contains tests for Result
class ExamplesResult{
  ILoInt mt = new MtLoInt();
  ILoInt l1 = new ConsLoInt(3);
  ILoInt l2 = new ConsLoInt(1,  new ConsLoInt(0, new ConsLoInt(0, mt)));
  ILoInt l3 = new ConsLoInt(0,  new ConsLoInt(1, new ConsLoInt(0, mt)));
  Result r1 = new Result(0, l1, l1);
  Result r2 = new Result(2, mt, mt);
  
  ILoInt l4 = new ConsLoInt(1,  new ConsLoInt(5, new ConsLoInt(3, mt)));
  ILoInt l5 = new ConsLoInt(2,  new ConsLoInt(2, new ConsLoInt(4, mt)));
  
  Result r3 = new Result(0, l2, l3);
  Result r4 = new Result(0, l4, l5);
  
  //tests for addExact
  public boolean testAddExact(Tester t)
  {
    // tests a generic Result
    return t.checkExpect(r1.addExact(), new Result(1, l1, l1))
        //tests with mts
        && t.checkExpect(r2.addExact(), new Result(3, mt, mt));
  }
  
  //tests for addInexact1
  public boolean testAddInexact1(Tester t)
  {
    // tests a generic Result at pos 0
    return t.checkExpect(r1.addInexact1(0), new Result(1, l2, l1))
        //test a genric Result as pos >0
        && t.checkExpect(r1.addInexact1(2), new Result(1, l3, l1))
        //tests with mts
        && t.checkException(new IndexOutOfBoundsException(), r2,
            "addInexact1", 0);
  }
  
  //tests for addInexact2
  public boolean testAddInexact2(Tester t)
  {
    // tests a generic Result at pos 0
    return t.checkExpect(r1.addInexact2(0), new Result(1, l1, l2))
        //test a genric Result as pos >0
        && t.checkExpect(r1.addInexact2(2), new Result(1, l1, l3))
        //tests with mts
        && t.checkException(new IndexOutOfBoundsException(), r2,
            "addInexact2", 0);
  }
 
  //tests for didWin
  public boolean testCalcInexactCount(Tester t)
  {
    // tests with 2 mts
    return t.checkExpect(r1.calcInexactCount(), 0)
        // tets where there are zero inexacts
        && t.checkExpect(r3.calcInexactCount(), 0)
        // tests with more than one inexacts
        && t.checkExpect(r4.calcInexactCount(), 6);
  }
  
  //tests for didWin
  public boolean testDidWin(Tester t)
  {
    // tests a generic Result with a sequence length of zero
    return t.checkExpect(r1.didWin(0), true)
        // tests a false case
        && t.checkExpect(r1.didWin(2), false)
        // generic test
        && t.checkExpect(r2.didWin(2), true);
  }
}