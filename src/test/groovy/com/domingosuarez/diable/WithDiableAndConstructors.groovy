package com.domingosuarez.diable

/**
 * Created by angelpimentel on 7/3/15.
 */
@DIable
class WithDiableAndConstructors {
  String name
  Map<String, String> map
  public WithDiableAndConstructors(){
    this.name = 'BASE CONSTRUCTOR'
  }
  public WithDiableAndConstructors(String name){
    this.name = name
  }
}
