package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.model.session.Stage;

import java.io.IOException;
import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrap;
import static com.github.dakusui.scriptiveunit.utils.Checks.check;

@FunctionalInterface
public interface Form<O> extends Function<Stage, O>, Formattable {
  @Override
  O apply(Stage input);

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    try {
      formatter.out().append(this.name());
    } catch (IOException e) {
      throw wrap(e);
    }
  }

  default String name() {
    return "(noname)";
  }

  /**
   * An interface that represents a constant. The {@code apply} method of an
   * implementation of this class must return the same value always regardless
   * of its arguments value, even if a {@code null} is given.
   *
   * @param <O> Type of output constant.
   */
  interface Const<O> extends Form<O> {
  }
}
