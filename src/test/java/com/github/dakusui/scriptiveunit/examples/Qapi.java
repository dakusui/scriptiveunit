package com.github.dakusui.scriptiveunit.examples;


import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Import.Alias;
import com.github.dakusui.scriptiveunit.annotations.CompatLoad;
import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.core.JsonScript;
import com.github.dakusui.scriptiveunit.libs.Arith;
import com.github.dakusui.scriptiveunit.libs.Collections;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.libs.Strings;
import com.github.dakusui.scriptiveunit.libs.actions.Basic;
import com.github.dakusui.scriptiveunit.libs.extras.QueryApi;
import com.github.dakusui.scriptiveunit.loaders.ScriptCompiler;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.runners.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.unittests.cli.MemoizationExample;
import com.google.common.collect.Maps;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

/**
 * A driver example.
 */
@CompatLoad(with = Qapi.Loader.class)
@RunWith(ScriptiveUnit.class)
public class Qapi {
  public static class Loader extends ScriptCompiler.Impl {
    public Loader(JsonScript script) {
      super(script);
    }
  }

  public static class Misc {
    @SuppressWarnings("unused")
    @Scriptable
    public Value<String> content(Value<Entry> entry) {
      return input -> entry.apply(input).content();
    }

    @SuppressWarnings("unused")
    @Scriptable
    public Value<Boolean> evalintp(Value<Integer> value, Value<Value<Boolean>> predicate) {
      return input -> predicate.apply(input).apply(Collections.wrapValueAsArgumentInStage(input, value));
    }
  }

  @SuppressWarnings("unused")
  @Import({
      @Alias(value = "*"),
      @Alias(value = "add", as = "+"),
      @Alias(value = "sub", as = "-"),
      @Alias(value = "mul", as = "*"),
      @Alias(value = "div", as = "/")
  })
  public Object arith = new Arith();

  @SuppressWarnings("unused")
  @Import({
      @Alias(value = "*"),
      @Alias(value = "gt", as = ">"),
      @Alias(value = "ge", as = ">="),
      @Alias(value = "lt", as = "<"),
      @Alias(value = "le", as = "<="),
      @Alias(value = "eq", as = "=="),
      @Alias(value = "ifthen", as = "if_then")
  })
  public Object predicates = new Predicates();

  @SuppressWarnings("unused")
  @Import
  public Object strings = new Strings();

  @SuppressWarnings("unused")
  @Import
  public Object collections = new Collections();

  @SuppressWarnings("unused")
  @Import
  public Object basicActions = new Basic();

  @SuppressWarnings("unused")
  @Import
  public Object misc = new Misc();

  @SuppressWarnings("unused")
  @Import({
      @Alias(value = "*"),
      @Alias(value = "request", as = "query"),
      @Alias(value = "response", as = "result"),
      @Alias(value = "service", as = "issue"),
      @Alias(value = "configAttr", as = "config_attr"),
      @Alias(value = "systemProperty", as = "system_property"),
  })
  public QueryApi<Request, Response, Entry> queryApi = new QueryApi<Request, Response, Entry>() {
    @Override
    protected Request buildRequest(Map<String, Object> fixture) {
      return new Request(fixture);
    }

    @Override
    protected Response service(Request request) {
      List<Entry> matched = new LinkedList<>();
      L:
      for (Entry eachEntry : Entry.values()) {
        for (Request.Term eachTerm : request.getTerms()) {
          if (eachTerm.matches(eachEntry)) {
            matched.add(eachEntry);
            continue L;
          }
        }
      }
      return new Response(matched);
    }

    @Override
    protected Request override(Map<String, Object> values, Request request) {
      Map<String, Object> work = Maps.newTreeMap();
      work.putAll(request.toMap());
      work.putAll(values);
      return buildRequest(work);
    }
  };

  @SuppressWarnings("unused")
  @Import
  public Object memoizationExample = new MemoizationExample();

  public static class Request {
    static class Term {
      String[] words;

      Term(String term) {
        this.words = term.split("&&");
      }

      boolean matches(Entry entry) {
        for (String each : this.words) {
          if (!entry.matches(each))
            return false;
        }
        return true;
      }
    }

    private final Map<String, Object> fixture;
    private final Term[] terms;

    @SuppressWarnings("unchecked")
    Request(Map<String, Object> fixture) {
      this.fixture = unmodifiableMap(fixture);
      this.terms = ((List<String>) this.fixture.get("terms"))
          .stream()
          .map((Object input) -> new Term((String) input))
          .toArray(Term[]::new);
    }

    Term[] getTerms() {
      return this.terms;
    }

    Map<String, Object> toMap() {
      return this.fixture;
    }

    @Override
    public String toString() {
      return this.fixture.toString();
    }
  }

  public static class Response extends LinkedList<Entry> implements Iterable<Entry> {

    Response(Collection<? extends Entry> entries) {
      this.addAll(entries);
    }

    @SuppressWarnings("unused")
    public int statusCode() {
      return this.isEmpty() ? 404 : 200;
    }

  }

  @SuppressWarnings("unused")
  public enum Entry {
    @SuppressWarnings("unused")
    ITEM_01("ヒータ", 15_000),
    @SuppressWarnings("unused")
    ITEM_02("ヒーター", 14_800),
    @SuppressWarnings("unused")
    ITEM_03("ストーブ", 16_800),
    @SuppressWarnings("unused")
    ITEM_03a("ストーブ用ポンプ", 200),
    @SuppressWarnings("unused")
    ITEM_03b("ストーブ用替え扉", 480),
    @SuppressWarnings("unused")
    ITEM_04("ヒーター", 9_800),
    @SuppressWarnings("unused")
    ITEM_05("iPhone 7 ケース", 2_000),
    @SuppressWarnings("unused")
    ITEM_06("iPhone 7 シルバー", 48_000),
    @SuppressWarnings("unused")
    ITEM_07("iPhone 6 ケース", 1_980),
    @SuppressWarnings("unused")
    ITEM_08("iPhone 6Plus シルバー", 68_000),
    ;

    private final String content;
    private final int price;

    Entry(String content, int price) {
      this.content = requireNonNull(content);
      this.price = price;
    }

    public boolean matches(String word) {
      return this.content.contains(requireNonNull(word));
    }

    public String content() {
      return this.content;
    }

    @Override
    public String toString() {
      return format("%s:%s(%s)", this.name(), this.content, this.price);
    }
  }
}
