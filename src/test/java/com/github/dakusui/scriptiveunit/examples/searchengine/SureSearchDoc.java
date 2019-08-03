package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;

public class SureSearchDoc {
  final ApplicationSpec.Dictionary content;
  final ApplicationSpec.Dictionary topics;

  public SureSearchDoc(ApplicationSpec.Dictionary topics, ApplicationSpec.Dictionary content) {
    this.content = content;
    this.topics = topics;
  }

  static class Factory implements ApplicationSpec.Dictionary.Factory {
    SureSearchDoc apple() {
      return new SureSearchDoc(
          dict(),
          dict(
              $("name", "Apple"),
              $("description", "This is a document about an apple")
          ));
    }

    SureSearchDoc orange() {
      return new SureSearchDoc(
          dict(),
          dict(
              $("name", "Orange"),
              $("description", "This is a document about an orange")
          ));
    }

    SureSearchDoc pineapple() {
      return new SureSearchDoc(
          dict(),
          dict(
              $("name", "Pineapple"),
              $("description", "This is a document about a pineapple")
          ));
    }

    SureSearchDoc citrus() {
      return new SureSearchDoc(
          dict(),
          dict(
              $("name", "Citrus"),
              $("description", "This is a document about a citrus")
          ));
    }

    SureSearchDoc grapefruit() {
      return new SureSearchDoc(
          dict(),
          dict(
              $("name", "Grapefruit"),
              $("description", "This is a document about a grapefruit")
          ));
    }

    SureSearchDoc forbiddenFruit() {
      return new SureSearchDoc(
          dict(),
          dict(
              $("name", "Forbidden fruit"),
              $("description", "This is a document about a forbidden fruit")
          ));
    }
  }
}