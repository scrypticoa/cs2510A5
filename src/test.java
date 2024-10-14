//import javalib.funworld.*;
import java.awt.*;

interface ILoColor {
  ILoColor insert(Color color);
  
  ILoColor remove();
  
  ILoColor clone();
  
  ILoColor reverse();
  ILoColor doReverse(ILoColor result);
}

class MtLoColor implements ILoColor {
  
  public ILoColor insert(Color color) {
    return new ConsLoColor(color, new MtLoColor());
  }
  
  public ILoColor remove() {
    return new MtLoColor();
  }
  
  public ILoColor clone() {
    return new MtLoColor();
  }
  
  public ILoColor reverse() {
    return new MtLoColor();
  }
  
  public ILoColor doReverse(ILoColor result) {
    return result;
  }
}

class ConsLoColor implements ILoColor {
  Color first;
  ILoColor rest;
  
  public ConsLoColor(Color first, ILoColor rest) {
    this.first = first;
    this.rest = rest;
  }
  
  public ILoColor insert(Color color) {
    return new ConsLoColor(color, this.clone());
  }
  
  public ILoColor remove() {
    return this.rest;
  }
  
  public ILoColor clone() {
    return new ConsLoColor(this.first, this.rest.clone());
  }
  
  public ILoColor reverse() {
    return this.doReverse(new MtLoColor());
  }
  
  public ILoColor doReverse(ILoColor result) {
    return rest.doReverse(new ConsLoColor(first, result));
  }
}