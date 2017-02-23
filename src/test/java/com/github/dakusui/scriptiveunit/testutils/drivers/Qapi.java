package com.github.dakusui.scriptiveunit.testutils.drivers;


import com.github.dakusui.scriptiveunit.ScriptiveUnit;
import com.github.dakusui.scriptiveunit.annotations.Import;
import com.github.dakusui.scriptiveunit.annotations.Import.Alias;
import com.github.dakusui.scriptiveunit.annotations.Load;
import com.github.dakusui.scriptiveunit.annotations.ReflectivelyReferenced;
import com.github.dakusui.scriptiveunit.doc.Help;
import com.github.dakusui.scriptiveunit.drivers.*;
import com.github.dakusui.scriptiveunit.drivers.actions.Basic;
import com.github.dakusui.scriptiveunit.loaders.json.JsonBasedTestSuiteLoader;
import com.google.common.collect.Maps;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

/**
 * A driver example.
 */
@Load(
    scriptPackagePrefix = "tests", scriptNamePattern = ".*\\.json", with = JsonBasedTestSuiteLoader.Factory.class)
@RunWith(ScriptiveUnit.class)
public class Qapi {
  @ReflectivelyReferenced
  @Rule
  public TestRule testRule = new TestWatcher() {
    @Override
    protected void starting(Description description) {
      System.out.println("Hello");
    }
  };
  @ReflectivelyReferenced
  @Import({
      @Alias(value = "*"),
      @Alias(value = "add", as = "+"),
      @Alias(value = "sub", as = "-"),
      @Alias(value = "mul", as = "*"),
      @Alias(value = "div", as = "/")
  })
  public Object   arith    = new Arith();

  @ReflectivelyReferenced
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

  @ReflectivelyReferenced
  @Import
  public Object strings = new Strings();

  @ReflectivelyReferenced
  @Import
  public Object collections = new Collections();

  @ReflectivelyReferenced
  @Import({
      @Alias(value = "*"),
      @Alias(value = "configAttr", as = "config_attr"),
      @Alias(value = "systemProperty", as = "system_property"),
  })
  public Object core = new Core();

  @ReflectivelyReferenced
  @Import
  public Object basicActions = new Basic();

  @ReflectivelyReferenced
  @Import({
      @Alias(value = "*"),
      @Alias(value = "request", as = "query"),
      @Alias(value = "response", as = "result"),
      @Alias(value = "service", as = "issue")
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

  public static class Request {
    public static class Term {
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
    private final Term[]              terms;

    Request(Map<String, Object> fixture) {
      this.fixture = unmodifiableMap(fixture);
      //noinspection unchecked
      this.terms = ((List<String>) this.fixture.get("terms")).stream()
          .map((Object input) -> new Term((String) input))
          .collect(Collectors.toList()).toArray(new Term[0]);
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

    @ReflectivelyReferenced
    public int statusCode() {
      return this.isEmpty() ? 404 : 200;
    }

  }

  @ReflectivelyReferenced
  public enum Entry {
    @ReflectivelyReferenced
    ITEM_01("ヒータ", 15_000),
    @ReflectivelyReferenced
    ITEM_02("ヒーター", 14_800),
    @ReflectivelyReferenced
    ITEM_03("ストーブ", 16_800),
    @ReflectivelyReferenced
    ITEM_03a("ストーブ用ポンプ", 200),
    @ReflectivelyReferenced
    ITEM_03b("ストーブ用替え扉", 480),
    @ReflectivelyReferenced
    ITEM_04("ヒーター", 9_800),
    @ReflectivelyReferenced
    ITEM_05("iPhone 7 ケース", 2_000),
    @ReflectivelyReferenced
    ITEM_06("iPhone 7 シルバー", 48_000),
    @ReflectivelyReferenced
    ITEM_07("iPhone 6 ケース", 1_980),
    @ReflectivelyReferenced
    ITEM_08("iPhone 6Plus シルバー", 68_000),;

    private final String content;
    private final int    price;

    Entry(String content, int price) {
      this.content = requireNonNull(content);
      this.price = price;
    }

    public boolean matches(String word) {
      return this.content.contains(requireNonNull(word));
    }

    @Override
    public String toString() {
      return format("%s:%s(%s)", this.name(), this.content, this.price);
    }
  }


  public static void main(String... args) {
    Help.help(Qapi.class, args);
  }
}
