# ScriptiveUnit

```ScriptiveUnit``` is a generic framework to build a JSON based DSTL (domain specific testing language)
for mainly input-output systems such as search engines.

# Installation
Include following xml fragment in your ```pom.xml```.

```xml

    <dependency>
      <groupId>com.github.dakusui</groupId>
      <artifactId>scriptiveunit</artifactId>
      <version>{ScriptiveUnit Version}</version>
    </dependency>
```

For the released **ScriptiveUnit version**s, you can refer to [this page](https://github.com/dakusui/scriptiveunit/releases).

# Usage
Following is a diagram that illustrates how engineers and assets are
interacting each other in an ecosystem where ScriptiveUnit is utilized.


<img src="doc/images/overview.jpg" alt="Overview" style="width: 640px;"/>

## Scripted test suite example

Full version of this example is found [here](src/test/resources/tests/regular/qapi.json).

```javascript

    {
        "description":"An example test suite to Query-Result model on ScriptiveUnit",
        "factorSpace": {
            "factors": {
                "terms": [["ヒーター"], ["iPhone"]],
                "sortBy": ["price", "lexical", "relevance"],
                "order" : ["asc", "desc"]
            },
            "constraints": [
                ["if_then", ["equals", ["attr", "sortBy"], "relevance"], ["equals", ["attr", "order"], "desc"]]
            ]
        },
        "testOracles": [
            {
                "description": "Searching 'iPhone' should not return too many accessories",
                "given": ["equals", ["attr", "terms"], ["quote", "iPhone"]],
                "when": ["issue", ["query"] ],
                "then": [">",
                    ["size", ["filter", ["result"], ["containedBy", ["issue", ["with", {"terms":["iPhone&&シルバー"]}, ["query"]]]]]],
                    3
                ]
            },
            {
                "description": "Searching 'ヒーター' should also return items that contain 'ヒータ' or 'ストーブ'",
                "given": ["equals", ["attr", "terms"], ["quote", "ヒーター"]],
                "when": ["issue", ["query"] ],
                "then": ["<",
                  ["-",
                    ["size", ["issue", ["with", { "terms": ["ヒータ", "ストーブ"], "hits":-1 }, ["query"]]] ],
                    ["size", ["result"] ]
                  ],
                  2
                ]
            },
            {
                "description": "Valid queries should result in 200",
                "given": ["always"],
                "when": ["issue", ["query"]],
                "then": ["==", ["value", "statusCode", ["result"]], 200]
            }
        ]
    }
```

## Script driver example
Full version of this example is found [here](src/test/java/com/github/dakusui/scriptiveunit/drivers/Qapi.java).

```java

    @Load(with = JsonBasedTestSuiteLoader.Factory.class)
    @RunWith(ScriptiveUnit.class)
    public class Qapi {
      ...

      @Import
      public Object collections = new Collections();

      @Import({
          @Alias(value = "*"),
          @Alias(value = "request", as = "query"),
          @Alias(value = "response", as = "result"),
          @Alias(value = "service", as = "issue")
      })
      public QueryApi<Request, Response, Entry> queryApi = new QueryApi<Request, Response, Entry>() {
        @Override
        protected Request buildRequest(Map<String, Object> fixture) {
          return new Request(fixture);
        }

        @Override
        protected Response service(Request request) {
          ...
          return new Response(matched);
        }

        @Override
        protected Request override(Map<String, Object> values, Request request) {
          ...
          return buildRequest(work);
        }
      };

      public static class Request {
        public static class Term {
          ...
        }
        ...
      }

      public static class Response extends LinkedList<Entry> implements Iterable<Entry> {
        ...
      }
    }
```
## Output example
The Script and driver mentioned above will generate test results like following.

<img src="doc/images/screenshot.png" alt="Screenshot" style="width: 800px;"/>

# Future works
* "Regex" factor support
* Support 'preprocessing'
* Evaluation inside a JSON object
* Dependency mechanism

# References
* [Site](https://dakusui.github.io/scriptiveunit/)
* [API Reference](https://dakusui.github.io/scriptiveunit/apidocs/index.html)
* [JCUnit](https://github.com/dakusui/jcunit)
* [ActionUnit](https://github.com/dakusui/actionunit)
