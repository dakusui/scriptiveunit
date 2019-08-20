package com.github.dakusui.scriptiveunit.examples.searchengine;

import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.Dictionary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.dakusui.scriptiveunit.examples.searchengine.SureSearchDoc.DOC_FACTORY;
import static java.util.stream.Collectors.toList;

public enum SureSearchDocSet {
  DEFAULT(
      DOC_FACTORY.apple(),
      DOC_FACTORY.orange(),
      DOC_FACTORY.pineapple(),
      DOC_FACTORY.citrus(),
      DOC_FACTORY.grapefruit(),
      DOC_FACTORY.forbiddenFruit(),
      DOC_FACTORY.appleDotCom()),
  ;

  final Map<String, SureSearchDoc> annotatedDocs;

  SureSearchDocSet(SureSearchDoc... annotatedDocs) {
    this.annotatedDocs = new HashMap<String, SureSearchDoc>() {{
      for (SureSearchDoc each : annotatedDocs)
        this.put(each.id(), each);
    }};
  }

  List<SureSearchDoc> find(Predicate<SureSearchDoc> cond) {
    return this.annotatedDocs.values().stream().filter(cond).collect(toList());
  }

  Optional<SureSearchDoc> lookUp(String id) {
    return annotatedDocs.containsKey(id) ?
        Optional.of(annotatedDocs.get(id)) :
        Optional.empty();
  }

  public List<Dictionary> docs() {
    return annotatedDocs.values().stream()
        .map(SureSearchDoc::content)
        .collect(toList());
  }
}
