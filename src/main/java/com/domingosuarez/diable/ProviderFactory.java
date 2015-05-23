package com.domingosuarez.diable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
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
    return ofNullable(parent)
      .map(clazz -> asList(clazz.getDeclaredFields()).stream())
      .map(f -> getField(fieldName, f))
      .map(ProviderFactory::findValue)
      .orElse(null);
  }

  private static Field getField(String fieldName, Stream<Field> f) {
    return f
      .filter(field -> field.getName().equals(getFieldName(fieldName)))
      .findFirst()
      .orElse(null);
  }

  private static String getFieldName(String fieldName) {
    return "$" + ofNullable(fieldName).orElse("");
  }

  public static Object findValue(Field field) {
    return of(field.getDeclaredAnnotations())
      .map(ProviderFactory::findProvider)
      .filter(provider -> provider != null)
      .findFirst()
      .orElseGet(ProviderFactory::getNullProvider)
      .get(field.getType(), field.getName());
  }

  private static NullProvider getNullProvider() {
    return new NullProvider();
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
