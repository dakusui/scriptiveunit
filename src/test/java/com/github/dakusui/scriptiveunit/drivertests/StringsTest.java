package com.github.dakusui.scriptiveunit.drivertests;

import com.github.dakusui.scriptiveunit.libs.Strings;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.asObject;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.scriptiveunit.testutils.UtUtils.*;

public class StringsTest {
  private Strings lib = new Strings();

  private Stage stage = createOracleStage();

  @Test
  public void given_hello_StartsWith_h_$whenApplied$thenTrue() {
    Object value = lib.startsWith(value("hello"), value("h")).apply(stage);

    assertThat(
        value,
        asObject().isInstanceOf(Boolean.class).equalTo(Boolean.TRUE).$());
  }

  @Test
  public void given_hello_StartsWith_H_$whenApplied$thenFalse() {
    Object value = lib.startsWith(value("hello"), value("H")).apply(stage);

    assertThat(
        value,
        asObject().isInstanceOf(Boolean.class).equalTo(Boolean.FALSE).$());
  }

  @Test
  public void given_hello_endsWith_o_$whenApplied$thenTrue() {
    Object value = lib.endsWith(value("hello"), value("o")).apply(stage);

    assertThat(
        value,
        asObject().isInstanceOf(Boolean.class).equalTo(Boolean.TRUE).$());
  }

  @Test
  public void given_hello_endsWith_O_$whenApplied$thenFalse() {
    Object value = lib.endsWith(value("hello"), value("O")).apply(stage);

    assertThat(
        value,
        asObject().isInstanceOf(Boolean.class).equalTo(Boolean.FALSE).$());
  }

  @Test
  public void given_hello_length_$whenApplied$then6() {
    Object value = lib.length(value("hello")).apply(stage);

    assertThat(
        value,
        asObject().isInstanceOf(Integer.class).equalTo(5).$());
  }

  @Test
  public void given_empty_length_$whenApplied$then0() {
    Object value = lib.length(value("")).apply(stage);

    assertThat(
        value,
        asObject().isInstanceOf(Integer.class).equalTo(0).$());
  }

  @Test
  public void given_hello_substr_3$whenApplied$then_lo_() {
    Object value = lib.substr(value("hello"), value(3), value(5)).apply(stage);

    assertThat(
        value,
        asObject().isInstanceOf(String.class).equalTo("lo").$());
  }

  @Test
  public void given_matches_hello_h__$whenApplied$then_true_() {
    Object value = lib.matches(value("hello"), value("h.*")).apply(stage);

    assertThat(
        value,
        asObject().isInstanceOf(Boolean.class).equalTo(Boolean.TRUE).$());
  }

  @Test
  public void given_matches_hello_e__$whenApplied$then_true_() {
    Object value = lib.matches(value("hello"), value("e.*")).apply(stage);

    assertThat(
        value,
        asObject().isInstanceOf(Boolean.class).equalTo(Boolean.FALSE).$());
  }

  @Test
  public void given_format_hello_s_world$whenApplied$then_hello_world() {
    Object value = lib.format(value("hello:<%s>"), values("world")).apply(stage);

    assertThat(
        value,
        asObject().isInstanceOf(String.class).equalTo("hello:<world>").$());
  }
}
