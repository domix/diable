package com.domingosuarez.diable

import com.domingosuarez.diable.provider.FooProvider
import spock.lang.Ignore
import spock.lang.Specification

import java.lang.reflect.Field

/**
 * Created by domix on 18/05/15.
 */
class SomeFuckingSpec extends Specification {

  def foo() {
    when:
      def withDiable = new WithDiable()
      withDiable.foo = 'demo'
    then:
      withDiable.foo == 'demo'
    when:
      withDiable = new WithDiable()
    then:
      withDiable.foo == null
    when:
      withDiable = new WithDiable(foo: 'hola')
    then:
      withDiable.foo
  }

  //@Ignore
  def bar() {

    when:
      //def value = ProviderFactory.findValue(field)
      def book = new AClass()

      Class<?> c = book.getClass();

      Field chap = c.getDeclaredField("foo");
      def value = ProviderFactory.findValue(chap)
      println value
    then:

      println chap.dump()

      //where:

      /*field << new AClass().properties.findAll {
        !['metaClass', 'class'].contains(it)
      }.collect {

        def clazz = Class.forName(new AClass().getClass().name)
        println clazz.dump()

        clazz.getDeclaredField(it.key)
      }*/
  }
}


class AClass {
  @FooProvider
  String foo
}