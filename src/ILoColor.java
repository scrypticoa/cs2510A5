import tester.*;
import java.util.function.*;
import java.awt.Color;

//A list of Colors
interface ILoColor {
  
  //calculates length of the list
  int calcLength();
  
  //gets the color at an index, starts at 0
  Color get(int index);
  
  <T> T fold(T initial, BiFunction<T, Color, T> folder);
}

class MtLoColor implements ILoColor {
  
  /* TEMPLATE
   * METHODS:
   * this.calcLength() ... int
   * this.get(int index) ... Color
   * this.fold(T initial, BiFunction<T, Color, T> folder) ...  <T>
   */
  
  //calculates length of the list
  public int calcLength() {
    return 0;
  }
  
  //gets the color at an index, starts at 0
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
  
  // purely for testing, not used in game logic
  static ConsLoColor gen(Color firstColor, Color...cols) { 
    ILoColor temp = new MtLoColor();
    for (int i = cols.length - 1; i > -1; i--) {
      temp = new ConsLoColor(cols[i], temp);
    }
    return new ConsLoColor(firstColor, temp);
  }
  
  /* TEMPLATE
   * FIELDS: 
   * this.first ... Color
   * this.rest ... ILoColor
   * 
   * METHODS:
   * this.calcLength() ... int
   * this.get(int index) ... Color
   * this.fold(T initial, BiFunction<T, Color, T> folder) ...  <T>
   * 
   * METHODS OF FIELDS:
   * rest.calcLength() ... int
   * rest.get(int index) ... Color
   * rest.fold(T initial, BiFunction<T, Color, T> folder) ...  <T>
   */
  
  //calculates length of the list
  public int calcLength() {
    return 1 + this.rest.calcLength();
  }
  
  //gets the color at an index, starts at 0
  public Color get(int index) {
    if (index == 0) {
      return this.first;
    }
    return this.rest.get(index - 1);
  }
  
  public <T> T fold(T initial, BiFunction<T, Color, T> folder) {
    return this.rest.fold(
        folder.apply(initial, this.first), 
        folder);
  }
}

// tests for ILoColors
class ExamplesILoColor {
  
  ILoColor example = ConsLoColor.gen(Color.red, Color.blue, Color.black);
  ILoColor empty = new MtLoColor();
  
  //tests calcLength
  public boolean testCalcLength(Tester t) {
    boolean out = true;
    
    // test generic list
    out |= t.checkExpect(example.calcLength(), 3);
    
    // test empty list
    out |= t.checkExpect(empty.calcLength(), 0);
    
    return out;
  }
  
  //tests Fold
  public boolean testFold(Tester t) {
    boolean out = true;
    
    ILoColor init = new MtLoColor();
    
    out |= t.checkExpect(example.fold(init,
        (ls, col) -> {
          return new ConsLoColor(col, ls);
        }), ConsLoColor.gen(Color.black, Color.blue, Color.red));
    
    return out;
  }
  
  public boolean testGet(Tester t) {
    boolean out = true;
    
    // generic valid get
    out |= t.checkExpect(example.get(1), Color.blue);
    
    // out of bounds get
    out |= t.checkException(new IndexOutOfBoundsException(), example, "get", 5);
    
    return out;
  }
}




