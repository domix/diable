package com.domingosuarez.diable.provider;

import com.domingosuarez.diable.Provider;
import com.domingosuarez.diable.WithDiable;
import com.domingosuarez.diable.WithDiableAndConstructors;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

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
  public void wire(Object instance) {
    if (instance instanceof WithDiable) {
      ((WithDiable) instance).setMap(getTestMap());
    }else if(instance instanceof WithDiableAndConstructors){
      ((WithDiableAndConstructors) instance).setMap(getTestMap());
    }
  }

  @Override
  public Boolean supports(Annotation annotation) {
    return FooProvider.class.isAssignableFrom(annotation.annotationType());
  }
  public static Map<String,String > getTestMap(){
    return new HashMap<String, String>() {{
      put("one", "1");
      put("two", "2");
    }};
  }
}
