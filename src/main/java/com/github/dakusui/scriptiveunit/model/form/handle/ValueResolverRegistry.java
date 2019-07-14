package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.scriptiveunit.exceptions.ConfigurationException;
import com.github.dakusui.scriptiveunit.utils.DriverUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

public interface ValueResolverRegistry {
  Optional<ValueResolver> lookUp(String name);

  static ValueResolverRegistry load(Object driverObject) {
    Map<String, List<ValueResolver>> allFoundObjectMedhods = new HashMap<>();
    Map<String, ValueResolver> objectMethodMap = new HashMap<>();
    DriverUtils.getObjectMethodsFromImportedFieldsInObject(driverObject)
        .stream()
        .peek((ValueResolver each) -> {
          allFoundObjectMedhods.computeIfAbsent(each.getName(), name -> new LinkedList<>());
          allFoundObjectMedhods.get(each.getName()).add(each);
        })
        .forEach((ValueResolver each) -> {
          objectMethodMap.put(each.getName(), each);
        });
    Map<String, List<ValueResolver>> duplicatedObjectMethods = allFoundObjectMedhods
        .entrySet()
        .stream()
        .filter((Map.Entry<String, List<ValueResolver>> each) -> each.getValue().size() > 1)
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    if (!duplicatedObjectMethods.isEmpty())
      throw ConfigurationException.duplicatedFormsAreFound(duplicatedObjectMethods);
    return name -> Optional.ofNullable(objectMethodMap.get(name));
  }
}
