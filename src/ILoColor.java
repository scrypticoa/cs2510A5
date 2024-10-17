import tester.*;
import java.util.function.*;
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
  
  static ConsLoColor gen(Color firstColor, Color...cols) { // purely for testing, not used in game logic
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