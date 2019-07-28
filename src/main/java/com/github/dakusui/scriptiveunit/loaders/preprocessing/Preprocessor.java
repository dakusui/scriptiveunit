package com.github.dakusui.scriptiveunit.loaders.preprocessing;

import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.lang.ResourceStoreSpec;

import java.util.List;

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

    Builder applicationSpec(ApplicationSpec applicationSpec) {
      this.applicationSpec = requireNonNull(applicationSpec);
      return this;
    }

    public Preprocessor build() {
      requireNonNull(applicationSpec);
      requireNonNull(hostSpec);
      return new Preprocessor() {
        @Override
        public ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary rawScript, ResourceStoreSpec resourceStoreSpec) {
          ApplicationSpec.Dictionary ret = applicationSpec.createDefaultValues();
          ret = applicationSpec.deepMerge(
              preprocess(rawScript, applicationSpec.preprocessorUnits()),
              ret
          );
          for (String parent : parentsOf(rawScript, applicationSpec)) {
            ret = applicationSpec.deepMerge(
                ret,
                readApplicationDictionaryWithMerging(parent, applicationSpec, resourceStoreSpec)
            );
          }
          return applicationSpec.removeInheritanceDirective(ret);
        }

        ApplicationSpec.Dictionary readApplicationDictionaryWithMerging(
            String resourceName,
            ApplicationSpec applicationSpec,
            ResourceStoreSpec resourceStoreSpec) {
          ApplicationSpec.Dictionary inputDictionary = hostSpec.readRawScript(resourceName, resourceStoreSpec);
          ApplicationSpec.Dictionary work_ = inputDictionary;
          ApplicationSpec.Dictionary preprocessedInputDictionary = preprocess(
              work_,
              applicationSpec.preprocessorUnits());
          List<String> parents = parentsOf(inputDictionary, applicationSpec);
          for (String parentResourceName : parents)
            work_ = applicationSpec.deepMerge(
                work_,
                readApplicationDictionaryWithMerging(parentResourceName, applicationSpec, resourceStoreSpec)
            );
          return applicationSpec.deepMerge(preprocessedInputDictionary, work_);
        }

        ApplicationSpec.Dictionary preprocess(ApplicationSpec.Dictionary inputNode, List<PreprocessingUnit> preprocessingUnits) {
          for (PreprocessingUnit each : preprocessingUnits) {
            inputNode = ApplicationSpec.preprocess(inputNode, each);
          }
          return inputNode;
        }
      };
    }

    static List<String> parentsOf(ApplicationSpec.Dictionary rawScript, ApplicationSpec applicationSpec) {
      return applicationSpec.parentsOf(rawScript);
    }
  }
}
