package com.github.dakusui.scriptiveunit.model.form;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.dakusui.scriptiveunit.exceptions.Exceptions.duplicatedFormsAreFound;
import static com.github.dakusui.scriptiveunit.utils.DriverUtils.getFormsFromImportedFieldsInObject;
import static java.util.stream.Collectors.toMap;

public interface FormRegistry {
  Optional<Form> lookUp(String name);

  static FormRegistry load(Object driverObject) {
    return createFormRegistry(driverObject);
  }

  static FormRegistry createFormRegistry(Object driverObject) {
    List<Form> forms = loadFormMapFromDriverObject(driverObject);
    Map<String, List<Form>> allFoundForms = new HashMap<>();
    forms.forEach((Form each) -> {
      allFoundForms.computeIfAbsent(each.getName(), name -> new LinkedList<>());
      allFoundForms.get(each.getName()).add(each);
    });
    Map<String, List<Form>> duplicatedForms = allFoundForms
        .entrySet()
        .stream()
        .filter((Map.Entry<String, List<Form>> each) -> each.getValue().size() > 1)
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    if (!duplicatedForms.isEmpty())
      throw duplicatedFormsAreFound(duplicatedForms);
    return name -> allFoundForms.containsKey(name) ?
        Optional.of(allFoundForms.get(name).get(0)) :
        Optional.empty();
  }

  static List<Form> loadFormMapFromDriverObject(Object driverObject) {
    return new LinkedList<>(getFormsFromImportedFieldsInObject(driverObject));
  }
}
