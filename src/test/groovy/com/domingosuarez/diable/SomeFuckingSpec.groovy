package com.domingosuarez.diable

import com.domingosuarez.diable.provider.FooProvider
import com.domingosuarez.diable.provider.FooProviderImpl
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

      Class<?> c = book.getClass() ;

      Field chap = c.getDeclaredField("foo") ;
      println "field: ${chap}"
      def value = ProviderFactory.findValue(chap)
      println "value: ${value}"

      def value1 = ProviderFactory.findValue("foo", c)

      println "value1: ${value1?.dump()}"
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

  def 'Should modify constructor'() {
    setup:
      ProviderFactory.registry = new ArrayList<Provider>()
      ProviderFactory.registerProvider(new FooProviderImpl())
    when:
      WithDiable withDiable = new WithDiable()
    then:
      withDiable != null
      withDiable.map == FooProviderImpl.testMap
  }

  def 'Should create object even if there is no provider'() {
    setup:
      ProviderFactory.registry = new ArrayList<Provider>()
    when:
      WithDiable withDiable = new WithDiable()
    then:
      withDiable != null
      withDiable.map == null
  }

  def 'Should modify existing constructor with no parameter'() {
    setup:
      ProviderFactory.registry = new ArrayList<Provider>()
      ProviderFactory.registerProvider(new FooProviderImpl())
    when:
      WithDiableAndConstructors diabled = new WithDiableAndConstructors()
    then:
      diabled != null
      diabled.name == 'BASE CONSTRUCTOR'
      diabled.map == FooProviderImpl.testMap
  }

  def 'Should modify existing constructor with parameter'() {
    setup:
      ProviderFactory.registry = new ArrayList<Provider>()
      ProviderFactory.registerProvider(new FooProviderImpl())
      String name = "OTHER NAME"
    when:
      WithDiableAndConstructors diabled = new WithDiableAndConstructors(name)
    then:
      diabled != null
      diabled.name == name
      diabled.map == FooProviderImpl.testMap
  }
}


class AClass {
  @FooProvider
  String foo
}