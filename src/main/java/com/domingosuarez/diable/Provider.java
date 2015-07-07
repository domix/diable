package com.domingosuarez.diable;

import java.lang.annotation.Annotation;

/**
 * Created by domix on 18/05/15.
 */
public interface Provider {
  <T> T get(Class<T> type, String name);
  void wire(Object instance);

  Boolean supports(Annotation annotation);
}
