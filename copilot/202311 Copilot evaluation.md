# GitHub Copilot evaluation (as of 2023 november)

## Executive summary

At the moment, Copilot could be described as **code completion on steroids**.
It does have good suggestions, but also highly irrelevant and subtly wrong ones. As such, its **output needs to be double-checked** by an expert, somewhat negating the boost in productivity it gives, and making it dangerous in the hands of junior developers.

See below for details. I tried Copilot on pet projects and Advent of Code, in various languages and also attempted code generation and doc writing with it.
All you see here is my personal opinion, so treat it as such - tried to provide many examples to remain as objective as possible.

## Positives

### Can explain/summarize code well

* Plus points for complimenting my code, heh:
  `This code is a good example of how to use asynchronous streams in Rust to handle potentially large amounts of data in a memory-efficient way.`
* Was useful for explaining a Rust type signature I didn't fully understand:
  `So, in summary, this line of code is specifying a type that is a dynamically dispatched Stream of Result<TranscodeResponse, Status> values, which can be sent across threads and can live for the entire duration of the program.`
* *Wow*: deeper understanding (or the stochastic parrot has seen code like this elsewhere)?
  `The function then attempts to seek to a timestamp that is 10% into the video. This is done because the start of a video might not be representative of its content. However, due to the nature of video encoding, the actual seek might not be exact, so the function decodes frames until it reaches the desired timestamp.`

### Code completion can have good guesses

* Wrote: `@GET @Produces(MediaType.APPLICATION_JSON) public String hello`, Suggested: `helloJson() { return "{\"hello\": \"world\"}"; }`
* Wrote: `FactoryBeans can be done with `, Suggested: `@Produces methods`, then `on @ApplicationScoped beans`
* Wrote: `Lifecycle callbacks are the same (`, Suggested: `@PostConstruct, @PreDestroy)`
* It guessed an entire doc line (and it was correct). Wrote: `@Interceptor`, Suggested: `s are supported, and they can be applied to methods, classes, and even constructors`
* *Wow*. It guessed most of this one:
```
@AroundInvoke
public Object logInvocation(InvocationContext ctx) throws Exception {
  long before = System.currentTimeMillis();
  Object result = ctx.proceed();
  log.info("LoggedInterceptor: {}, duration={} ms", ctx.getMethod(), System.currentTimeMillis() - before);
  return result;
}
```
* ...and then (based on the previous of course), fully guessed the `@AroundConstruct` one
* Correctly guessed that I want an `elves.forEach(System.out::println);`
* Wrote: `grid`, Suggested: `grid = new int[rows.Length, rows[0].Length];` which was correct
* Guessed a correct interval intersection method just from the signature `internal Range? Intersect(Range range)`:
```
internal Range? Intersect(Range range)
{
    if (Start > range.End || End < range.Start) return null;
    var start = Math.Max(Start, range.Start);
    var end = Math.Min(End, range.End);
    return new Range(start, end);
}
```
* Wrote: `bool isFiveOfAKind = Cards.`, Suggested: `bool isFiveOfAKind = Cards.Distinct().Count() == 1;`
* Wrote: `bool isFourOfAKind =`, Suggested: `bool isFourOfAKind = Cards.GroupBy(c => c).Any(g => g.Count() == 4);`
* Wrote: `bool isTwoPair =`, Suggested: `bool isTwoPair = Cards.GroupBy(c => c).Count(g => g.Count() == 2) == 2;`
* In general, I found that it's very good at mechanical rewrites (eg. when having Coordinate neighbors like "north", "south", "east", "west", it generates correct code based on the previous lines _most of the time_)

## Negatives

### Too verbose at times

* `The function then enters a loop where it decodes packets from the video stream. If a packet's decoding timestamp is greater than or equal to the seek timestamp, the function scales the decoded frame to the target thumbnail size and breaks the loop.`
* `The get_duration function is a helper function that converts a duration in the stream's time base to seconds. It does this by multiplying the duration by the time base's numerator and then dividing by the time base's denominator.`

I can read code, too, thank you very much!

### Very confidently stating silly things

* Q: `why can't my code transcode h267 video files?`
* A: `The issue might be related to the codec used in your code. In the provided code snippet, the specific codec used for decoding or encoding is not visible. However, if your code is using a codec that doesn't support H.267 (also known as VVC, Versatile Video Coding), it won't be able to transcode H.267 video files.

As of now, the FFmpeg library, which your code seems to be using, does not have full support for the H.267/VVC codec. This is because H.267 is a very new standard (as of 2020) and support for it in open-source libraries like FFmpeg is still under development.`

Plot twist: H.267 doesn't even exist yet :)

* Q: `are you sure it's called h267 and not h266?`
* A: ```You're correct, my apologies for the confusion. The latest video coding standard is H.266, also known as Versatile Video Coding (VVC), not H.267.

As of now, FFmpeg does not fully support the H.266/VVC codec. This is because H.266 is a new standard (as of 2020) and support for it in open-source libraries like FFmpeg is still under development.```

* Wrote: `Quarkus prefers Maven,`, Suggested: `but Gradle is supported as well. The Gradle plugin is a bit weird, though - it's not a "normal" plugin, but a`... and that's it. I was curious what would be next so applied it. Well, copilot? "it's not a normal plugin but a"...? Silence.
* Then after some time, I tried again, and it finished the sentence with `...it's not a "normal" plugin, but a [buildscript dependency](https://quarkus.io/guides/gradle-tooling#using-the-quarkus-gradle-plugin)`. Yeah, so it hallucinated a URL that doesn't exist, and the statement about the plugin [is crap](https://plugins.gradle.org/plugin/io.quarkus).
* Wrote `Quarkus reserves the 'quarkus.' and 'QUARKUS_' prefixes for its own configuration, plus `, Suggested: `'java.', 'sun.', 'file.', 'user.', 'os.' and 'path.separator'`. Yep... hallucinated crap.
* Wrote: `This is achieved via code generation during build time (similar to Micronaut). `, Suggested: `This means that the build time is **much** longer than with Spring/Micronaut.`. Far-fetched, to say the least.
* Guessing wrong URLs, eg.: * `@IfBuildProperty can be used to [conditionally enable beans based on build properties](https://quarkus.io/guides/cdi-reference#enable_build_property)`

* While programming with C#, it very frequently recommended methods that don't even exist (eg. `.map()` on an `IEnumerable`... didn't even get the naming convention right!)

* Started writing a 2D array iterator function and it generated this absolute nonsense:
```
public static <T> IEnumerable<Tuple<Coordinate, T>> Iterate2D(T[][] array) {
    return Iterate2D(array, (x, y, v) => new Tuple<>(new Coordinate(x, y), v));
}
```

### Pushing said silly thing

* Wrote: `Build images are hosted exclusively on quay.io,`, Suggested: `, which is blocked in China`. Uh... okay? Many things are, but how is this relevant?
* Wrote: `Build images are hosted exclusively on quay.io, which is somewhat unreliable (`, Suggested: `not accessible in China)`. Copilot, what's your beef with China??
* What I wanted to write: `Build images are hosted exclusively on quay.io, which is somewhat unreliable (had outages spanning _days_)`

* I was working on Advent of Code day 3 (it is about schematics), and generated `Console.WriteLine(CountTrees(s, 3, 1));` out of the blue. Nothing in my code suggests the use or presence of trees, and the method doesn't exist either.
* Same as above, I typed `internal IEnumerable<int>`, Suggested: `internal IEnumerable<int> TreesOnSlope(int right, int down)`... WTF is going on with trees??

### Generating useable code is difficult

* Q: `<selected a Cucumber story file> generate code for the selected`
* A: `<proceeds to generate Step definitions in Node.js...mine is a Rust project :) >`

* Q: `generate rust code that confirms to the selected BDD specification`
* A: `<proceeds to generate Step definitions, this time in Rust...>`... facepalm.

### Code completion sillyness

* I created a new file, and put a main method in it:
  `fn main() -> Result<(), Box<dyn Error>> {`
  Code suggestion:
  ```
  fn main() -> Result<(), Box<dyn Error>> {
    ffmpeg::init()?; // TODO do we need this?
  ```
  which was the exact same line from a `sample.rs` in my repo, it even left the TODO in there :)
* While writing docs, code completion suggesting me to write the same line as the one above (facepalm)
* Wrote: `@Inject` (in the context of mapping Quarkus annotations to Spring's), Suggested: `@Inject is replaced with @Inject (no surprise here)` - LOL :)
* Suggested a code completion for an enumeration, generated crap: `@ApplicationScoped is the default scope but it's not the same as Micronaut's @Singleton - it's more like @Prototype (new instance per injection point)`
* Suggested Markdown syntax in a .sh file (and it was even syntactically wrong)
* I was working on a `Stream<Elf>`, and at the point of `.collect(`, it suggested `.collect(ElfCollector.toElfList())`, a completely made up class and method name... WTF?
* Generated two methods of the below class out of the blue, with basically no context (and of course it was useless):
```
record Card(List<int> WinningNumbers, List<int> NumbersIHave) {

    public static Card Parse(string input) {
        var parts = input.Split(":");
        var winningNumbers = parts[0].Split(" ").Select(int.Parse).ToList();
        var numbersIHave = parts[1].Split(" ").Select(int.Parse).ToList();
        return new Card(winningNumbers, numbersIHave);
    }

    public int Score() {
        return WinningNumbers.Zip(NumbersIHave, (w, m) => w * m).Sum();
    }
}
```
* After successfully generating `isFiveOfAKind`, he thought I'm into poker, so generated: `bool isStraightFlush = isStraight && isFlush;`

### Confidently generates wrong code

* Was "aware" of the context and generated a C# switch expression that parsed 2023 Advent of Code day1 part2 _seemingly correctly_. Something like:
```
return regex.Match(line).Groups[1].Value switch
{
    "one" => 1,
    "two" => 2,
    "three" => 3,
    "four" => 4,
    "five" => 5,
    "six" => 6,
    "seven" => 7,
    "eight" => 8,
    "nine" => 9,
    _ => int.Parse(line)
};
```
This compiled, but was non-trivially wrong: in the fallback it ignores the match input and tries to parse _the original line_ into an `int`, which will obviously fail. This was also **a good example of how Copilot is extremely dangerous** for newbies: I just started learning C#, didn't know how to write switch expressions on my own, **trusted the code that was generated**, and spent way too much time debugging it afterwards.
* Generated: `bool isFullHouse = Cards.GroupBy(c => c).Any(g => g.Count() == 3) && Cards.GroupBy(c => c).Any(g => g.Count() == 2);`, which looks good at first, but doesn't conform to the spec ("...remaining two cards are each different from any other card")
* Based on previous good guesses, it generated: `bool isOnePair = Cards.GroupBy(c => c).Any(g => g.Count() == 2);`, which looks good at first glance, but is actually wrong (will return true for two pairs as well)
* I attempted to solve AoC 2023 exercises (to test its ability of generating code from previously unseen English text). The results:
  * day1/1: on this day I used copilot _after_ I implemented my solution. It just copy-pasted it as-is, so disqualified.
  * day1/2: with part2 I attempted to use copilot before implementing the solution. It did generate code that compiled, but it gave the wrong result.
  * day2/1: the code that was generated did compile, but then failed with a runtime exception. I can't be bothered to debug why, just threw it away and wrote it myself.
  * day2-3: didn't try these ones
  * day4/1: generated code that compiles, but failed to parse the input - gave it a chance and fixed the parser, then it returned the correct result. The core of the solution was:
  ```
  var matches = WinningNumbers.Intersect(NumbersIHave).Count();
  return matches > 0 ? 1 << (matches - 1) : 0;
  ```
  * day4/2: generated code that compiles and runs... but either horribly inefficient or loops infinitely. I shut it down after 5 minutes of burning CPU.
  * day5/1: "Sorry, but I can only assist with programming related questions." LOL.
  * day6/1: generated code that compiles and runs, but cannot parse the input. I fixed the parsing, it ran and it gave the wrong result
  * day6/2: it broke down spectacularly, generating code that declared some string arrays and then tried to parse them...
  * day7/1: "Sorry, but I can only assist with programming related questions.". Removing the story part from the beginning, it now came up with code that compiled, and returned the result mentioned in the task (6640). Feeding it the real input failed with an `System.ArgumentOutOfRangeException`
  * day8/1: generated code that compiles and runs and returns `6`, but lacks input parsing. When I implemented the missing parser part and it got the actual input, it failed with a `System.IndexOutOfRangeException`
  * day9/1: generated code that compiles and runs and... returns 0.
  * day10/1: generated code (and correctly guessed that the task required graph traversal), and then "Sorry, the response matched public code so it was blocked. Please rephrase your prompt. Learn more.". LOL.
    My next prompt was "solve the below with c# so that it doesn't match public code" - same code, same result.
    However, I could still copy-paste the code it generated - it compiled and failed with a runtime exception while parsing the input.
  * day11/1: generated a **skeleton** code only. I had to ask for the implementations of the methods one-by-one (6 methods, end result totaling 172 lines), which I did. I deliberately did not read any of the text that was generated for the methods - I think this is a great example to simulate those "we ain't need them coders anymoe!" use-cases, when someone just dumbly copy-pastes what copilot says. Let's see how that worked out:
    * First off, generating the code took ~5m, and it compiles. So far so good.
    * It was looking for the input in the wrong folder. I only did 10 of those from the exact same folder so far, but oh well. Fixed in ~1m.
    * The code was burning CPU for ~3 minutes, using ~27GB of RAM, after which it got killed by the OS. Fun stuff :) There's no way in hell I'm going to debug ~150 lines of array-manipulation code, so rewriting it from scratch instead.
  * day12/1: figured it'd need dynamic programming, and generated code (with 3 nested `for` loops) that compiled. It ran fairly quickly and returned... 0.

By AoC day 13 my Copilot eval period expired, and to be honest I don't see why I should pay $10/month for it...

### Plagiarism?
  * It came up with this one-liner for edge-to-point intersection in the context of a point-inside-polygon problem: `isInside ^= cj.Y > Y ^ ci.Y > Y && X < (ci.X - cj.X) * (Y - cj.Y) / (ci.Y - cj.Y) + cj.X;`, which worked flawlessly but is quite obscure. I'm reasonably certain that code like this is present in many repositories dealing with coordinate geometry.


