package com.github.dakusui.scriptiveunit.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation that indicates methods in a target field can be used as
 * functions in test scripts.
 */
@Retention(RUNTIME)
public @interface Import {
  Alias[] value() default { @Alias(Alias.ALL) };

  @Retention(RUNTIME)
  @interface Alias {
    String ALL = "*";

    /**
     * If {@code "*"} is returned, ScriptiveUnit imports all the available {@code Func}s
     * in the class.
     */
    String value();

    /**
     * Returns an alias for the {@code Func} specified by {@code value()}.
     * If a string whose length is 0 is returned, alias is not used and the func will
     * be referred to by the {@code value}.
     */
    String as() default "";
  }
}
