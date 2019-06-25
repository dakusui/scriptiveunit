package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.drivers.Predicates;
import com.github.dakusui.scriptiveunit.model.form.handle.ObjectMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public enum DriverUtils {
  ;

  public static List<ObjectMethod> getObjectMethodsFromImportedFieldsInObject(Object object) {
    return ReflectionUtils.getAnnotatedFields(object, Import.class)
        .stream()
        .map(
            each -> ReflectionUtils.getAnnotatedMethods(
                each.get(),
                Scriptable.class,
                createAliasMap(each.getField().getAnnotation(Import.class).value())))
        .flatMap(List::stream)
        .filter(objectMethod -> objectMethod.getName() != null)
        .collect(toList());
  }

  private static Map<String, String> createAliasMap(Import.Alias[] aliases) {
    return Arrays.stream(
        aliases
    ).collect(toMap(alias -> requireNonNull(alias).value(), alias -> !"".equals(requireNonNull(alias).as()) ? alias.as() : ""
    ));
  }

  public static void main(String... args) {
    getObjectMethodsFromImportedFieldsInObject(new DriverExample())
        .forEach(System.out::println);
  }

  public static class DriverExample {
    @Import({
        @Import.Alias(value = "*"),
        @Import.Alias(value = "gt", as = ">"),
        @Import.Alias(value = "ge", as = ">="),
        @Import.Alias(value = "lt", as = "<"),
        @Import.Alias(value = "le", as = "<="),
        @Import.Alias(value = "eq", as = "=="),
        @Import.Alias(value = "ifthen", as = "if_then")
    })
    public final Object predicates = new Predicates();
  }
}
