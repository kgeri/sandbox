# GitHub Copilot evaluation (as of 2023 november)

## Positives

### Can explain/summarize code well

* Complimented on my code, lol:
  `This code is a good example of how to use asynchronous streams in Rust to handle potentially large amounts of data in a memory-efficient way.`
* Was useful for explaining a Rust type signature I didn't fully understand:
  `So, in summary, this line of code is specifying a type that is a dynamically dispatched Stream of Result<TranscodeResponse, Status> values, which can be sent across threads and can live for the entire duration of the program.`
* *Surprise*: deeper understanding (or the stochastic parrot has seen code like this elsewhere):
  `The function then attempts to seek to a timestamp that is 10% into the video. This is done because the start of a video might not be representative of its content. However, due to the nature of video encoding, the actual seek might not be exact, so the function decodes frames until it reaches the desired timestamp.`

### Code completion can have good guesses

* Wrote: `@GET @Produces(MediaType.APPLICATION_JSON) public String hello`, Suggested: `helloJson() { return "{\"hello\": \"world\"}"; }`
* Wrote: `FactoryBeans can be done with `, Suggested: `@Produces methods`, then `on @ApplicationScoped beans`
* Wrote: `Lifecycle callbacks are the same (`, Suggested: `@PostConstruct, @PreDestroy)`
* It guessed an entire doc line (and it was correct). Wrote: `@Interceptor`, Suggested: `s are supported, and they can be applied to methods, classes, and even constructors`
* Wow. It guessed most of this one:
```
@AroundInvoke
public Object logInvocation(InvocationContext ctx) throws Exception {
  long before = System.currentTimeMillis();
  Object result = ctx.proceed();
  log.info("LoggedInterceptor: {}, duration={} ms", ctx.getMethod(), System.currentTimeMillis() - before);
  return result;
}
```
* ...and then (based on this of course), fully guessed the `@AroundConstruct` one

## Negatives

### Too verbose at times

* `The function then enters a loop where it decodes packets from the video stream. If a packet's decoding timestamp is greater than or equal to the seek timestamp, the function scales the decoded frame to the target thumbnail size and breaks the loop.`
* `The get_duration function is a helper function that converts a duration in the stream's time base to seconds. It does this by multiplying the duration by the time base's numerator and then dividing by the time base's denominator.`

### Very confidently stating silly things

* Q: `why can't my code transcode h267 video files?`
* A: `The issue might be related to the codec used in your code. In the provided code snippet, the specific codec used for decoding or encoding is not visible. However, if your code is using a codec that doesn't support H.267 (also known as VVC, Versatile Video Coding), it won't be able to transcode H.267 video files.

As of now, the FFmpeg library, which your code seems to be using, does not have full support for the H.267/VVC codec. This is because H.267 is a very new standard (as of 2020) and support for it in open-source libraries like FFmpeg is still under development.`

Plot twist: H.267 doesn't even exist yet :)

* Q: `are you sure it's called h267 and not h266?`
* A: ```You're correct, my apologies for the confusion. The latest video coding standard is H.266, also known as Versatile Video Coding (VVC), not H.267.

As of now, FFmpeg does not fully support the H.266/VVC codec. This is because H.266 is a new standard (as of 2020) and support for it in open-source libraries like FFmpeg is still under development.```

* Wrote: `Quarkus prefers Maven,`, Suggested: `but Gradle is supported as well. The Gradle plugin is a bit weird, though - it's not a "normal" plugin, but a`... and that's it. I was curious what would be next so applied it. Well, copilot? "it's not a normal plugin but a"...? Silence.
* Then after some time, I tried again, and it finished the sentence with `...it's not a "normal" plugin, but a [buildscript dependency](https://quarkus.io/guides/gradle-tooling#using-the-quarkus-gradle-plugin)`. Which is [absolute bullshit](https://plugins.gradle.org/plugin/io.quarkus).
* Wrote `Quarkus reserves the 'quarkus.' and 'QUARKUS_' prefixes for its own configuration, plus `, Suggested: `'java.', 'sun.', 'file.', 'user.', 'os.' and 'path.separator'`. You guessed right. Utter bullshit.
* Wrote: `This is achieved via code generation during build time (similar to Micronaut). `, Suggested: `This means that the build time is **much** longer than with Spring/Micronaut.`. Wrong again.

### Pushing said silly thing

* Wrote: `Build images are hosted exclusively on quay.io,`, Suggested: `, which is blocked in China` (uh... okay?)
* Wrote: `Build images are hosted exclusively on quay.io, which is somewhat unreliable (`, Suggested: `not accessible in China)` (what's your beef with China??)
* I could finally write: `Build images are hosted exclusively on quay.io, which is somewhat unreliable (had outages spanning _days_)`

### Generating useable code is difficult

* Q: `<selected a Cucumber story file> generate code for the selected`
* A: `<proceeds to generate Step definitions in Node.js...mine is a Rust project :) >`

* Q: `generate rust code that confirms to the selected BDD specification`
* A: `<proceeds to generate Step definitions, this time in Rust...>`

### Code completion sillyness

* I created a new file, and put a main method in it:
  `fn main() -> Result<(), Box<dyn Error>> {`
  Code suggestion:
  ```
  fn main() -> Result<(), Box<dyn Error>> {
    ffmpeg::init()?; // TODO do we need this?
  ```
  which was the exact same line from a `sample.rs` in my repo.
* While writing docs, code completion suggesting me to write the same line as the one above (facepalm)
* Typed `@Inject`, suggested completion: `@Inject is replaced with @Inject (no surprise here)` - seriously? :)
* Suggested a code completion for an enumeration, generated absolute bullshit (`@ApplicationScoped is the default scope but it's not the same as Micronaut's @Singleton - it's more like @Prototype (new instance per injection point)`)
* Suggested Markdown syntax in a .sh file (and even a syntatcically incorrect one)

