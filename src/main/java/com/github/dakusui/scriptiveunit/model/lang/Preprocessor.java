package com.github.dakusui.scriptiveunit.model.lang;

import java.util.List;

import static com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.dict;
import static java.util.Objects.requireNonNull;

public interface Preprocessor {
  ApplicationSpec.Dictionary readScript(String scriptResourceName);

  class Builder<NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> {
    private ApplicationSpec applicationSpec;

    private RawScriptReader<NODE, OBJECT, ARRAY, ATOM> rawScriptReader;

    private final HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec;

    public Builder(HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec) {
      this.hostSpec = requireNonNull(hostSpec);
    }

    public Builder<NODE, OBJECT, ARRAY, ATOM> applicationSpec(ApplicationSpec applicationSpec) {
      this.applicationSpec = requireNonNull(applicationSpec);
      return this;
    }

    public Builder<NODE, OBJECT, ARRAY, ATOM> rawScriptReader(RawScriptReader<NODE, OBJECT, ARRAY, ATOM> rawScriptReader) {
      this.rawScriptReader = requireNonNull(rawScriptReader);
      return this;
    }

    public Preprocessor build() {
      requireNonNull(applicationSpec);
      requireNonNull(rawScriptReader);
      return new Preprocessor() {
        @Override
        public ApplicationSpec.Dictionary readScript(String scriptResourceName) {
          return readScript(
              scriptResourceName,
              applicationSpec.createDefaultValues(),
              applicationSpec,
              hostSpec);
        }

        ApplicationSpec.Dictionary readScript(
            String scriptResourceName,
            ApplicationSpec.Dictionary defaultValues,
            ApplicationSpec applicationSpec,
            HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec) {
          return applicationSpec.deepMerge(readScriptHandlingInheritance(scriptResourceName, applicationSpec, hostSpec), defaultValues);
        }

        ApplicationSpec.Dictionary readApplicationDictionaryWithMerging(
            String resourceName,
            ApplicationSpec applicationSpec,
            HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec) {
          ApplicationSpec.Dictionary child = preprocess(
              rawScriptReader.apply(resourceName, hostSpec),
              applicationSpec.preprocessors());

          ApplicationSpec.Dictionary work_ = dict();
          for (String s : applicationSpec.parentsOf(child))
            work_ = applicationSpec.deepMerge(readApplicationDictionaryWithMerging(s, applicationSpec, hostSpec), work_);
          return applicationSpec.deepMerge(child, work_);
        }


        ApplicationSpec.Dictionary readScriptHandlingInheritance(String scriptResourceName, ApplicationSpec applicationSpec, HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec) {
          return applicationSpec.removeInheritanceDirective(readApplicationDictionaryWithMerging(scriptResourceName, applicationSpec, hostSpec));
        }

        ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary inputNode, List<PreprocessingUnit> preprocessingUnits) {
          for (PreprocessingUnit each : preprocessingUnits) {
            inputNode = ApplicationSpec.preprocess(inputNode, each);
          }
          return inputNode;
        }

      };
    }
  }
}
