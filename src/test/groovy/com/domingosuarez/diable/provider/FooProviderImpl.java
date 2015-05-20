package com.domingosuarez.diable.provider;

import com.domingosuarez.diable.Provider;

import java.lang.annotation.Annotation;

/**
 * Created by domix on 18/05/15.
 */
public class FooProviderImpl implements Provider {
  @Override
  public <T> T get(Class<T> type, String name) {
    System.out.println("Searching type: " + type.getName());
    System.out.println("with name: " + name);
    return null;
  }

  @Override
  public Boolean supports(Annotation annotation) {
    return FooProvider.class.isAssignableFrom(annotation.annotationType());
  }
}
