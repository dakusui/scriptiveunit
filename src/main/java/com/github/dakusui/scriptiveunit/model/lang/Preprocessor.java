package com.github.dakusui.scriptiveunit.model.lang;

import static java.util.Objects.requireNonNull;

public interface Preprocessor<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> {
  class Builder<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> {
    private       ApplicationSpec                         applicationSpec = new ApplicationSpec.Standard();

    private RawScriptReader<NODE, OBJECT, ARRAY, ATOM> rawScriptReader = (resourceName, hostSpec) -> hostSpec.toApplicationDictionary(hostSpec.readObjectNode(resourceName));

    private final HostSpec<NODE, OBJECT, ARRAY, ATOM>     hostSpec;

    public Builder(HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec) {
      this.hostSpec = requireNonNull(hostSpec);
    }

    public Builder applicationSpec(ApplicationSpec applicationSpec) {
      this.applicationSpec = requireNonNull(applicationSpec);
      return this;
    }

    public Builder scriptReader(RawScriptReader<NODE, OBJECT, ARRAY, ATOM> rawScriptReader) {
      this.rawScriptReader = requireNonNull(rawScriptReader);
      return this;
    }

    Preprocessor<NODE, OBJECT, ARRAY, ATOM> build() {
      return new Preprocessor<NODE, OBJECT, ARRAY, ATOM>() {
      };
    }
  }
}
