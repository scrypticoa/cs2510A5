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
  
  static ConsLoColor gen(Color firstColor, Color...cols) { // purely for testing, not used in game logic
    ILoColor temp = new MtLoColor();
    for (int i = cols.length-1; i > -1; i--) {
      temp = new ConsLoColor(cols[i], temp);
    }
    return new ConsLoColor(firstColor, temp);
  }
  
  //calculates length of the list
  public int calcLength() {
    return 1 + this.rest.calcLength();
  }
  
  //gets the color at an index, starts at 0
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