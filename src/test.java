//import javalib.funworld.*;
import java.awt.*;

interface ILoColor {
  ILoColor insert(Color color);
  
  ILoColor remove();
  
  ILoColor clone();
  
  ILoColor reverse();
  ILoColor doReverse(ILoColor result);
  
  Result compare(ILoColor other);
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

class Result{
  int exact;
  ILoInt answer;
  ILoInt guess;
  
  Result(int exact,  ILoInt answer, ILoInt guess){
    this.exact = exact;
    this.answer = answer;
    this.guess = guess;
  }
}

interface ILoInt{
  ILoInt addAtPos(int pos);
}

class MtLoInt implements ILoInt{
  public ILoInt addAtPos(int pos) {
    return new MtLoInt();
  }
}

class ConsLoInt implements ILoInt{
  int first;
  ILoInt rest;
  
  ConsLoInt(int first, ILoInt rest){
    this.first = first;
    this.rest = rest;
  }
  
  ConsLoInt(int x){
    first = 0;
    if(x > 1) {
      rest =  new ConsLoInt(x-1);
    } else {
      rest = new MtLoInt();
    }
  }

  public ILoInt addAtPos(int pos) {
    if(pos == 0){
      return new ConsLoInt(this.first + 1, this.rest);
    }
    return new ConsLoInt(this.first, this.rest.addAtPos(pos-1));
  }
}







