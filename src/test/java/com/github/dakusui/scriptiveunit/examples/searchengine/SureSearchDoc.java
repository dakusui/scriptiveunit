package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;

class SureSearchDoc {
  static final SureSearchDoc.Factory DOC_FACTORY = new Factory();

  private final ApplicationSpec.Dictionary content;
  private final ApplicationSpec.Dictionary topics;

  private SureSearchDoc(ApplicationSpec.Dictionary topics, ApplicationSpec.Dictionary content) {
    this.content = content;
    this.topics = topics;
  }

  double relevancyWith(String userQuery) {
    if (topics.containsKey(userQuery))
      return ((ApplicationSpec.Atom) this.topics.valueOf(userQuery)).get();
    return 0;
  }

  String id() {
    return ((ApplicationSpec.Atom) this.content.valueOf("id")).get();
  }

  ApplicationSpec.Dictionary content() {
    return this.content;
  }

  static class Factory implements ApplicationSpec.Dictionary.Factory {
    SureSearchDoc apple() {
      return new SureSearchDoc(
          dict($("apple", 1.0)),
          dict(
              $("id", "0"),
              $("name", "Apple"),
              $("description", "This is a document about an apple")
          ));
    }

    SureSearchDoc orange() {
      return new SureSearchDoc(
          dict($("citrus", 0.5),
              $("orange", 1.0)),
          dict(
              $("id", "1"),
              $("name", "Orange"),
              $("description", "This is a document about an orange")
          ));
    }

    SureSearchDoc pineapple() {
      return new SureSearchDoc(
          dict($("pineapple", 1.0)),
          dict(
              $("id", "2"),
              $("name", "Pineapple"),
              $("description", "This is a document about a pineapple")
          ));
    }

    SureSearchDoc citrus() {
      return new SureSearchDoc(
          dict($("citrus", 1.0),
              $("orange", 0.5)),
          dict(
              $("id", "3"),
              $("name", "Citrus"),
              $("description", "This is a document about a citrus")
          ));
    }

    SureSearchDoc grapefruit() {
      return new SureSearchDoc(
          dict($("citrus", 0.5),
              $("grapefruit", 0.5)),
          dict(
              $("id", "4"),
              $("name", "Grapefruit"),
              $("description", "This is a document about a grapefruit")
          ));
    }

    SureSearchDoc forbiddenFruit() {
      return new SureSearchDoc(
          dict($("apple", 1.0)),
          dict(
              $("id", "5"),
              $("name", "Forbidden fruit"),
              $("description", "This is a document about a forbidden fruit")
          ));
    }

    SureSearchDoc appleDotCom() {
      return new SureSearchDoc(
          dict(),
          dict(
              $("id", "6"),
              $("name", "Apple.com"),
              $("description", "This is a document about the apple.com, not a fruit.")
          ));
    }
  }
}
