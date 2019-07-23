package com.github.dakusui.scriptiveunit.unittests.model;

import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Import.Alias;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.exceptions.ConfigurationException;
import com.github.dakusui.scriptiveunit.model.form.FormRegistry;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import org.junit.Test;

public class FormRegistryTest {
  @Test(expected = ConfigurationException.class)
  public void test() {
    try {
      FormRegistry formRegistry = FormRegistry.createFormRegistry(new DuplicatedForms());
      Object value = formRegistry.lookUp("func")
          .orElseThrow(RuntimeException::new)
          .resolveValue(new Value[0]);

      System.out.println(value);
    } catch (ConfigurationException e) {
      e.printStackTrace();
      throw e;
    }
  }

  public static class DuplicatedForms {
    @Import({@Alias (value = "func1", as = "func")})
    public final Lib1 lib1 = new Lib1();

    @Import({@Alias (value = "func2", as = "func")})
    public final Lib2 lib2 = new Lib2();
  }

  public static class Lib1 {
    @Scriptable
    public Value<String> func1(Value<String> s) {
      return Value.Const.createConst("constantValue");
    }
  }

  public static class Lib2 {
    @Scriptable
    public Value<String> func2(Value<String> s) {
      return Value.Const.createConst("constantValue");
    }
  }
}
