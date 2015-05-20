package com.domingosuarez.diable;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Stream.of;

/**
 * Created by domix on 18/05/15.
 */
public class ProviderFactory {
  static List<Provider> registry = new ArrayList<>();

  static {
    new FastClasspathScanner().matchClassesImplementing(Provider.class, c -> {
      try {
        Provider provider = c.newInstance();
        registry.add(provider);
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }).scan();
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
      Field field = parent.getDeclaredField("foo");
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
