package com.github.dakusui.scriptiveunit;

public class ActionDescriptionComposer {
  private final String actionName;
  private final String actionNameForFixtureSetup;
  private final String actionDescriptionForFixtureSetUp;
  private final String actionNameForFixtureTearDown;
  private final String actionDescriptionForFixtureDescription;

  public ActionDescriptionComposer(String actionName, String actionNameForFixtureSetup, String actionDescriptionForFixtureSetUp, String actionNameForFixtureTearDown, String actionDescriptionForFixtureDescription) {
    this.actionName = actionName;
    this.actionNameForFixtureSetup = actionNameForFixtureSetup;
    this.actionDescriptionForFixtureSetUp = actionDescriptionForFixtureSetUp;
    this.actionNameForFixtureTearDown = actionNameForFixtureTearDown;
    this.actionDescriptionForFixtureDescription = actionDescriptionForFixtureDescription;
  }

  public String getActionName() {
    return actionName;
  }

  public String getActionNameForFixtureSetup() {
    return actionNameForFixtureSetup;
  }

  public String getActionDescriptionForFixtureSetUp() {
    return actionDescriptionForFixtureSetUp;
  }

  public String getActionNameForFixtureTearDown() {
    return actionNameForFixtureTearDown;
  }

  public String getActionDescriptionForFixtureDescription() {
    return actionDescriptionForFixtureDescription;
  }
}
