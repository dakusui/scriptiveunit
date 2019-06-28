package com.github.dakusui.scriptiveunit.model.form.handle;

import com.github.dakusui.scriptiveunit.exceptions.ConfigurationException;
import com.github.dakusui.scriptiveunit.utils.DriverUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

public interface ObjectMethodRegistry {
  Optional<ObjectMethod> lookUp(String name);

  static ObjectMethodRegistry load(Object driverObject) {
    Map<String, List<ObjectMethod>> allFoundObjectMedhods = new HashMap<>();
    Map<String, ObjectMethod> objectMethodMap = new HashMap<>();
    DriverUtils.getObjectMethodsFromImportedFieldsInObject(driverObject)
        .stream()
        .peek((ObjectMethod each) -> {
          allFoundObjectMedhods.computeIfAbsent(each.getName(), name -> new LinkedList<>());
          allFoundObjectMedhods.get(each.getName()).add(each);
        })
        .forEach((ObjectMethod each) -> {
          objectMethodMap.put(each.getName(), each);
        });
    Map<String, List<ObjectMethod>> duplicatedObjectMethods = allFoundObjectMedhods
        .entrySet()
        .stream()
        .filter((Map.Entry<String, List<ObjectMethod>> each) -> each.getValue().size() > 1)
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    if (!duplicatedObjectMethods.isEmpty())
      throw ConfigurationException.duplicatedFormsAreFound(duplicatedObjectMethods);
    return name -> Optional.ofNullable(objectMethodMap.get(name));
  }
}
