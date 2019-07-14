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
    Map<String, List<ValueResolver>> allFoundValueResolvers = new HashMap<>();
    Map<String, ValueResolver> valueResolverMap = new HashMap<>();
    DriverUtils.getValueResolversFromImportedFieldsInObject(driverObject)
        .stream()
        .peek((ValueResolver each) -> {
          allFoundValueResolvers.computeIfAbsent(each.getName(), name -> new LinkedList<>());
          allFoundValueResolvers.get(each.getName()).add(each);
        })
        .forEach((ValueResolver each) -> {
          valueResolverMap.put(each.getName(), each);
        });
    Map<String, List<ValueResolver>> duplicatedValueResolvers = allFoundValueResolvers
        .entrySet()
        .stream()
        .filter((Map.Entry<String, List<ValueResolver>> each) -> each.getValue().size() > 1)
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    if (!duplicatedValueResolvers.isEmpty())
      throw ConfigurationException.duplicatedValueResolversAreFound(duplicatedValueResolvers);
    return name -> Optional.ofNullable(valueResolverMap.get(name));
  }
}
