package com.github.dakusui.scriptiveunit.loaders.preprocessing;

import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.lang.ResourceStoreSpec;

import java.util.List;

import static com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.dict;
import static java.util.Objects.requireNonNull;

public interface Preprocessor {
  static <NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> Preprocessor create(HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec, ApplicationSpec applicationSpec) {
    return new Builder(hostSpec)
        .applicationSpec(applicationSpec)
        .build();
  }

  ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary rawScript, ResourceStoreSpec resourceStoreSpec);

  class Builder {
    private ApplicationSpec applicationSpec;


    private final HostSpec hostSpec;

    public Builder(HostSpec hostSpec) {
      this.hostSpec = requireNonNull(hostSpec);
    }

    public Builder applicationSpec(ApplicationSpec applicationSpec) {
      this.applicationSpec = requireNonNull(applicationSpec);
      return this;
    }

    public Preprocessor build() {
      requireNonNull(applicationSpec);
      requireNonNull(hostSpec);
      return new Preprocessor() {
        @Override
        public ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary rawScript, ResourceStoreSpec resourceStoreSpec) {
          ApplicationSpec.Dictionary ret = applicationSpec.deepMerge(
              preprocess(rawScript, applicationSpec.preprocessors()),
              applicationSpec.createDefaultValues());
          for (String parent : applicationSpec.parentsOf(rawScript)) {
            ret = applicationSpec.deepMerge(
                readApplicationDictionaryWithMerging(parent, applicationSpec, resourceStoreSpec),
                ret
            );
          }
          return applicationSpec.removeInheritanceDirective(ret);
        }

        ApplicationSpec.Dictionary readApplicationDictionaryWithMerging(
            String resourceName,
            ApplicationSpec applicationSpec, ResourceStoreSpec resourceStoreSpec) {
          ApplicationSpec.Dictionary resource = preprocess(
              hostSpec.readRawScript(resourceName, resourceStoreSpec),
              applicationSpec.preprocessors());

          ApplicationSpec.Dictionary work_ = dict();
          for (String s : applicationSpec.parentsOf(resource))
            work_ = applicationSpec.deepMerge(readApplicationDictionaryWithMerging(s, applicationSpec, resourceStoreSpec), work_);
          return applicationSpec.deepMerge(resource, work_);
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
