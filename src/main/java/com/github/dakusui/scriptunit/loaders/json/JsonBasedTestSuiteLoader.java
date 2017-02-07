package com.github.dakusui.scriptunit.loaders.json;

import com.github.dakusui.scriptunit.loaders.TestSuiteLoader;
import com.github.dakusui.scriptunit.loaders.json.JsonBeans.TestSuiteDescriptorBean;
import com.github.dakusui.scriptunit.model.TestSuiteDescriptor;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

import static com.github.dakusui.scriptunit.exceptions.ScriptUnitException.wrap;

public class JsonBasedTestSuiteLoader extends TestSuiteLoader.Base {
  @SuppressWarnings("WeakerAccess")
  protected JsonBasedTestSuiteLoader(InputStream inputStream, Class<?> driverClass) {
    super(inputStream, driverClass);
  }

  @Override
  protected TestSuiteDescriptor loadTestSuite(InputStream inputStream, Class<?> driverClass) {
    try {
      return new ObjectMapper()
          .readValue(inputStream, TestSuiteDescriptorBean.class)
          .create(driverClass);
    } catch (IOException e) {
      throw wrap(e);
    }
  }

  public static class Factory implements TestSuiteLoader.Factory {

    @Override
    public TestSuiteLoader create(InputStream inputStream, Class<?> driverClass) {
      return new JsonBasedTestSuiteLoader(inputStream, driverClass);
    }

  }
}
