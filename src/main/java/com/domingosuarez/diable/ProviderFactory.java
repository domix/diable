package com.domingosuarez.diable;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import java.lang.Throwable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Stream.of;

/**
 * Created by domix on 18/05/15.
 */
public class ProviderFactory {
  static List<com.domingosuarez.diable.Provider> registry = new ArrayList<>();

  static {
    /*new FastClasspathScanner().matchClassesImplementing(com.domingosuarez.diable.Provider.class, c -> {
      try {
        System.out.println("Provider found: " + c.getName());
        com.domingosuarez.diable.Provider provider = c.newInstance();
        registry.add(provider);
        System.out.println("Provider registered.");
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }).scan();*/

    /*FastClasspathScanner scanner = new FastClasspathScanner("com", "gex");
    scanner.scan();

    List<String> classesImplementingProvider = scanner.getClassesImplementing(com.domingosuarez.diable.Provider.class);

    System.out.println(classesImplementingProvider);


    classesImplementingProvider.forEach(providerClass -> {
      System.out.println("Provider found: " + providerClass);
      Class classProvider;

      try {
        classProvider = Class.forName(providerClass);
        com.domingosuarez.diable.Provider provider = (com.domingosuarez.diable.Provider) classProvider.newInstance();
        registry.add(provider);
        System.out.println("Provider registered.");
      } catch (Throwable exception) {
        exception.printStackTrace();
      }

    });*/
  }

  public static void registerProvider(Provider provider) {
    registry.add(provider);
  }

  public static Provider findProvider(Annotation annotation) {
    return registry.stream()
      .filter(provider -> provider.supports(annotation))
      .findFirst()
      .orElse(null);
  }


  public static Object findValue(String fieldName, Class parent) {
    Object result = null;
    try {
      Field field = parent.getDeclaredField(fieldName);
      result = findValue(field);
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
    return result;
  }

  public static Object findValue(Field field) {

    return of(field.getDeclaredAnnotations())
      .map(ProviderFactory::findProvider)
      .filter(provider -> provider != null)
      .findFirst()
      .orElseGet(() -> new NullProvider())
      .get(field.getType(), field.getName());
  }


  static class NullProvider implements Provider {

    @Override
    public <T> T get(Class<T> type, String name) {
      return null;
    }

    @Override
    public Boolean supports(Annotation annotation) {
      return false;
    }
  }
}
