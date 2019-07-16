package com.github.dakusui.scriptiveunit.loaders.preprocessing;

import java.util.List;

import static com.github.dakusui.scriptiveunit.loaders.preprocessing.ApplicationSpec.dict;
import static java.util.Objects.requireNonNull;

public interface Preprocessor {
  default ApplicationSpec.Dictionary readScript(String scriptResourceName) {
    return preprocess(readRawScript(scriptResourceName));
  }

  ApplicationSpec.Dictionary readRawScript(String scriptResourceName);

  ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary rawScript);

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
      requireNonNull(hostSpec);
      requireNonNull(rawScriptReader);
      return new Preprocessor() {
        //@Override
        public ApplicationSpec.Dictionary _readScript(String scriptResourceName) {
          return readScript(
              scriptResourceName,
              applicationSpec.createDefaultValues(),
              applicationSpec);
        }

        /**
         * WIP
         * @param rawScript
         * @return
         */
        @Override
        public ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary rawScript) {
          ApplicationSpec.Dictionary ret = rawScript;
          for (String parent : applicationSpec.parentsOf(rawScript)) {
            ret = applicationSpec.deepMerge(
                readApplicationDictionaryWithMerging(parent, applicationSpec),
                ret
            );
          }
          return applicationSpec.removeInheritanceDirective(ret);
        }

        ApplicationSpec.Dictionary readScript(
            String scriptResourceName,
            ApplicationSpec.Dictionary defaultValues,
            ApplicationSpec applicationSpec) {
          return applicationSpec.deepMerge(
              readScriptHandlingInheritance(scriptResourceName, applicationSpec),
              defaultValues);
        }

        ApplicationSpec.Dictionary readApplicationDictionaryWithMerging(
            String resourceName,
            ApplicationSpec applicationSpec) {
          ApplicationSpec.Dictionary resource = preprocess(
              readRawScript(resourceName),
              applicationSpec.preprocessors());

          ApplicationSpec.Dictionary work_ = dict();
          for (String s : applicationSpec.parentsOf(resource))
            work_ = applicationSpec.deepMerge(readApplicationDictionaryWithMerging(s, applicationSpec), work_);
          return applicationSpec.deepMerge(resource, work_);
        }

        @Override
        public ApplicationSpec.Dictionary readRawScript(String resourceName) {
          return rawScriptReader.apply(resourceName, hostSpec);
        }


        ApplicationSpec.Dictionary readScriptHandlingInheritance(String scriptResourceName, ApplicationSpec applicationSpec) {
          return applicationSpec.removeInheritanceDirective(
              readApplicationDictionaryWithMerging(scriptResourceName, applicationSpec));
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
