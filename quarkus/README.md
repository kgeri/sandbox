# Quarkus Hello World

| Property              | Value  |
|-----------------------|--------|
| Version               | 3.5.1  |
| Footprint             | 18 MB  |
| Startup time          | 463 ms |
| Footprint (native)    | 50 MB  |
| Startup time (native) | 22 ms  |
| Native build time     | 51.4 s |
| Native build RAM      | 4 GB   |

# Notes

## The expected stuff

* Opinionated (more than Micronaut), in a very good way
* `@Inject` supported, can inject lists of beans as well (`Instance<T>`)
* Lifecycle callbacks are the same (`@PostConstruct`, `@PreDestroy`)
* Scopes are supported, and mostly map to Spring's:

| Quarkus                        | Spring                                   |
|--------------------------------|------------------------------------------|
| `@ApplicationScoped` (default) | `@Scope("singleton")` + `@Lazy`          |
| `@Singleton`                   | `@Scope("singleton")`          (default) |
| `@RequestScoped`               | `@Scope("request")`                      |
| `@Dependent` (WTF?)            | `@Scope("prototype")`                    |
| `@SessionScoped`               | `@Scope("session")`                      |

* Events support (`Event<T>`) similar to Spring's `ApplicationEventPublisher`
* Administrative endpoints (`/q/health`, `/q/metrics`, `/q/openapi`, etc. - requires the corresponding extensions to be added!)

## The good stuff

* [Documentation is excellent](https://quarkus.io/guides)
* [Live Coding](https://quarkus.io/guides/getting-started#development-mode) (although by default it results in a full restart)
* Debug port automatically opened in dev mode (5005)
* Building on the Jakarta CDI spec was a wise choice (in contrast to Spring's homegrown DI)
* No classpath-scanning, bean discovery is done via `beans.xml`, `META-INF/jandex.xml` or `quarkus.index-dependency`
    * Specific types or groups of types can easily be excluded (and in profile-specific ways as well)
    * `@IfBeanProfile` can be used to [conditionally enable beans based on profiles](https://quarkus.io/guides/cdi-reference#enable_build_profile)
    * `@IfBuildProperty` can be used to [conditionally enable beans based on build properties](https://quarkus.io/guides/cdi-reference#enable_build_properties)
* Dependency injection failures (`UnsatisfiedResolutionException` / `AmbiguousResolutionException`) are detected **at build time**!
    * This is achieved via code generation during build time (similar to Micronaut), however the generated classes are packaged in a separate
      JAR (`quarkus-app/quarkus/generated-bytecode.jar`)
* Injection is lazy by default (similarly to Micronaut), can be overridden by `@Startup` (or `@Observes StartupEvent`)
    * So lazy in fact, that Quarkus will [even remove unused beans](https://quarkus.io/guides/cdi-reference#remove_unused_beans)
* `@Interceptor`s are supported, and they can be applied to methods, classes, and even constructors!
  * Static methods are [also possible to intercept](https://quarkus.io/guides/cdi-reference#interception-of-static-methods)
* `@Decorator`s are an interesting concept, can be used to add functionality to existing beans
* Quarkus reserves the `quarkus.` and `QUARKUS_` prefixes for its own configuration, plus distinguishes between "build-time" and "runtime" properties.
  Your custom props are always "runtime". This allows its Gradle plugin to do caching more efficiently (only rebuilding when its own properties
  change).
* Quarkus' build also [disables caching when it's running in CI](https://quarkus.io/guides/gradle-tooling#cached-build-artifacts)
* Different package types: `fast-jar` (default), `uber-jar`, `native`
* `@QuarkusIntegrationTest` is awesome. It allows you to re-run the same `@QuarkusTest`s you already wrote, but against a **real** application (
  packaged in any way, including native)!
* Adding Swagger UI is very convenient. Also, sensibly switched off for non-dev.

## The weird stuff

* They use CDI, but not really following [the spec](https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0.html#part_1)
* App scaffolding leans heavily on Maven and their own CLI. All changes can be done manually as well, though.
* Injection is **solely based on types**. This means `Qualifier`s are [typed](https://quarkus.io/guides/cdi#you-talked-about-some-qualifiers) - you
  have to create a new annotation type extending `Qualifier` for every single place you want to disambiguate! They seem to work like tags, though (get
  combined with the type of the class to be injected).
* There's
  an [important tradeoff between `@ApplicationScoped` and `@Singleton` documented here](https://quarkus.io/guides/cdi#applicationscoped-and-singleton-look-very-similar-which-one-should-i-choose-for-my-quarkus-application)
* `FactoryBean`s can be done with `@Produces` methods on `@ApplicationScoped` beans

## Additional notes

* Quarkus prefers Maven, but Gradle is supported as well. They have some hacks around `enforcedPlatform`, though, mimic the same behavior as Maven's
  BOMs.
* They have "extensions", but it's really just additional JARs (just like with Micronaut). Being able to add them with CLI commands is nice, though.
* The project config is a bit more verbose than Micronaut's
* Build images are hosted exclusively on quay.io, which is somewhat unreliable (had outages spanning _days_)
* It is [recommended to avoid private members with `@Inject`](https://quarkus.io/guides/cdi-reference#native-executables-and-private-members), because
  then the generated code does not have to generate reflexive calls (better for native, and in general)
