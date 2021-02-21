package com.github.dakusui.scriptiveunit.unittests.model;

import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.libs.Arith;
import com.github.dakusui.scriptiveunit.model.form.Form;
import com.github.dakusui.scriptiveunit.model.form.FormRegistry;
import org.junit.Test;

public class FormTest {
  private final FormRegistry formRegistry = FormRegistry.createFormRegistry(new Driver());

  public static class Driver {
    @Import
    public final Arith arith = new Arith();
  }

  @Test
  public void test() {
    Form form = formRegistry.lookUp("add").orElseThrow(RuntimeException::new);

    System.out.println(form.getParameterCount());
  }

  @Test
  public void test2() {
    Form form = formRegistry.lookUp("add").orElseThrow(RuntimeException::new);

    System.out.println(form.getParameterTypes()[0]);
  }

  @Test
  public void test3() {
    Form form = formRegistry.lookUp("add").orElseThrow(RuntimeException::new);

    System.out.println(form.getParameterDoc(0).value().length);
    System.out.println(form.getParameterDoc(0).value()[0]);
  }

  @Test
  public void test4() {
    Form form = formRegistry.lookUp("add").orElseThrow(RuntimeException::new);

    System.out.println(form.toString());
  }

  @Test
  public void test5() {
    Form form = formRegistry.lookUp("add").orElseThrow(RuntimeException::new);

    System.out.println(Form.describe(form));
    System.out.println(Form.describe(form).content());
    System.out.println(Form.describe(form).toString());
    System.out.println(Form.describe(form).children());
  }
}
