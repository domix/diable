package com.domingosuarez.diable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Stream.of;

/**
 * Created by domix on 18/05/15.
 */
public class ProviderFactory {
  static List<com.domingosuarez.diable.Provider> registry = new ArrayList<>();

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

    if (parent != null) {
      Field[] fields = parent.getDeclaredFields();

      Optional<Field> fieldFound = of(fields).filter(field -> field.getName().equals("$" + fieldName)).findFirst();

      if (fieldFound.isPresent()) {
        result = findValue(fieldFound.get());
      }
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
