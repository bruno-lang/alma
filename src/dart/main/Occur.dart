part of lingukit;

class Occur {
  
  /**
   * { min, max }
   */
  Occur.occur( int min, int max ) : this (min, max);
  
  Occur.x(int times) : this(times, times);
  
  static final Occur never = new Occur(0, 0);
  static final Occur once = new Occur(1 , 1);
  
  /**
   * *
   */
  static final Occur star = new Occur(0 , 1000);
  
  /**
   * +
   */
  static final Occur plus = new Occur(1 , 1000);
  
  /**
   * ?
   */
  static final Occur qmark = new Occur(0 , 1);

  Occur(this.min, this.max);

  final int min;
  final int max;
  
  String toString() {
    if (min == star.min && max == star.max) {
      return "*";
    }
    if (min == plus.min && max == plus.max) {
      return "+";
    }
    if (min == qmark.min && max == qmark.max) {
      return "?";
    }
    if (min == max) {
      return "x"+min.toString();
    }
    if (max == plus.max) {
      return "x"+min.toString()+"+";
    }
    return "x"+min.toString()+"-"+max.toString()+"";
  }
}