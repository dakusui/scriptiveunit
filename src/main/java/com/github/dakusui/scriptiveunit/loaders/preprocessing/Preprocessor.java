package com.github.dakusui.scriptiveunit.loaders.preprocessing;

import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec;
import com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.Dictionary;
import com.github.dakusui.scriptiveunit.model.lang.HostSpec;
import com.github.dakusui.scriptiveunit.model.lang.ResourceStoreSpec;

import java.util.Comparator;
import java.util.List;

import static com.github.dakusui.scriptiveunit.model.lang.ApplicationSpec.Dictionary.Factory.emptyDictionary;
import static java.util.Objects.requireNonNull;

public interface Preprocessor {
  static <NODE, OBJECT extends NODE, ARRAY extends NODE, ATOM extends NODE> Preprocessor create(HostSpec<NODE, OBJECT, ARRAY, ATOM> hostSpec, ApplicationSpec applicationSpec) {
    return new Builder(hostSpec)
        .applicationSpec(applicationSpec)
        .build();
  }

  Dictionary preprocess(Dictionary rawScript, ResourceStoreSpec resourceStoreSpec);

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
      return new Impl(applicationSpec, hostSpec);
    }

    private static List<String> parentsOf(Dictionary rawScript, ApplicationSpec applicationSpec) {
      List<String> ret = applicationSpec.parentsOf(rawScript);
      ret.sort(Comparator.reverseOrder());
      return ret;
    }

    private static class Impl implements Preprocessor {
      private ApplicationSpec applicationSpec;
      private HostSpec        hostSpec;

      Impl(ApplicationSpec applicationSpec, HostSpec hostSpec) {
        this.applicationSpec = applicationSpec;
        this.hostSpec = hostSpec;
      }

      @Override
      public Dictionary preprocess(Dictionary rawScript, ResourceStoreSpec resourceStoreSpec) {
        Dictionary input = performPreprocessingUnits(rawScript, applicationSpec.preprocessorUnits());
        Dictionary defaultValues = applicationSpec.createDefaultValues();
        Dictionary work = emptyDictionary();
        {
          for (String parent : parentsOf(input, applicationSpec)) {
            work = applicationSpec.deepMerge(
                readApplicationDictionaryWithMerging(parent, applicationSpec, resourceStoreSpec),
                work
            );
          }
        }
        work = applicationSpec.deepMerge(work, defaultValues);
        work = applicationSpec.deepMerge(input, work);
        return applicationSpec.removeInheritanceDirective(work);
      }

      Dictionary readApplicationDictionaryWithMerging(
          String resourceName,
          ApplicationSpec applicationSpec,
          ResourceStoreSpec resourceStoreSpec) {
        Dictionary input = hostSpec.readRawScript(resourceName, resourceStoreSpec);

        Dictionary preprocessedInputDictionary = performPreprocessingUnits(
            input,
            applicationSpec.preprocessorUnits());
        List<String> parents = parentsOf(preprocessedInputDictionary, applicationSpec);

        Dictionary work_ = preprocessedInputDictionary;
        for (String eachParentResourceName : parents)
          work_ = applicationSpec.deepMerge(
              work_,
              readApplicationDictionaryWithMerging(eachParentResourceName, applicationSpec, resourceStoreSpec));
        return applicationSpec.deepMerge(preprocessedInputDictionary, work_);
      }

      Dictionary performPreprocessingUnits(
          Dictionary inputNode,
          List<PreprocessingUnit> preprocessingUnits) {
        for (PreprocessingUnit each : preprocessingUnits) {
          inputNode = ApplicationSpec.preprocess(inputNode, each);
        }
        return inputNode;
      }
    }
  }
}
