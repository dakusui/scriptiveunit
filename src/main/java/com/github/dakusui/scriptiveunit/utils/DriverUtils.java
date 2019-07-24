package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.model.form.Form;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public enum DriverUtils {
  ;

  public static List<Form> getFormsFromImportedFieldsInObject(Object object) {
    return ReflectionUtils.getAnnotatedFields(object, Import.class)
        .stream()
        .map(each -> ReflectionUtils.getAnnotatedMethods(
            each.get(),
            Scriptable.class,
            createAliasMap(each.getField().getAnnotation(Import.class).value())))
        .flatMap(List::stream)
        .filter((Form form) -> form.getName() != null)
        .collect(toList());
  }

  private static Map<String, String> createAliasMap(Import.Alias[] aliases) {
    return Arrays.stream(
        aliases
    ).collect(toMap(alias -> requireNonNull(alias).value(), alias -> !"".equals(requireNonNull(alias).as()) ? alias.as() : ""
    ));
  }

}
