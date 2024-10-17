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
