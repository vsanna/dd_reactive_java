package dev.ishikawa.dd_rxjava.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class ReactorService {

  public Flux<User> getUsers() {
    return User.findAllFlux()
        .map(user -> user.toBuilder().age(user.getAge() / 2).build())
        .filter(user -> (user.getAge() % 3) == 0);
  }


  public void play() throws InterruptedException {
    ReactorService app = new ReactorService();
//    app.ex1();
//    app.ex1_2();
    app.ex1_3();
    app.ex2();
  }

  private void ex1() {
    Mono.just("hello").subscribe(log::info);

    Flux.fromIterable(List.of(
        "brown",
        "quick",
        "fox",
        "jump",
        "over"
    ))
        .map(word -> word + "_suffix")
        .flatMap(word -> Flux.fromArray(word.split("")))
        .distinct()
        .sort()
        .zipWith(Flux.range(1, 10), (word, line) -> line + ":" + word)
//        .filter(s -> s.matches("^1:.*"))
        .subscribe(log::info);
  }

  private void ex1_2() {
    Flux fastClock = Flux.interval(Duration.ofSeconds(1))
        .map(tick -> "fast" + tick);
    Flux slowClock = Flux.interval(Duration.ofSeconds(3))
        .map(tick -> "slow" + tick);

    Flux clock = Flux.merge(fastClock, slowClock);
//    clock.subscribe(log::info);
  /*
13:29:10.594 [parallel-1] [tid: 45] INFO  :160 - fast0
13:29:11.593 [parallel-1] [tid: 45] INFO  :160 - fast1
13:29:12.593 [parallel-1] [tid: 45] INFO  :160 - fast2
13:29:12.594 [parallel-2] [tid: 46] INFO  :160 - slow0
13:29:13.589 [parallel-1] [tid: 45] INFO  :160 - fast3
13:29:14.592 [parallel-1] [tid: 45] INFO  :160 - fast4
13:29:15.593 [parallel-1] [tid: 45] INFO  :160 - fast5
13:29:15.593 [parallel-1] [tid: 45] INFO  :160 - slow1
  * */

    Flux feed = Flux.interval(Duration.ofSeconds(1))
        .map(tick -> LocalDateTime.now());

//    clock.zipWith(feed, (tick, time) -> tick + " " + time)
//        .subscribe(log::info);
    /*
fast0 2020-04-09T13:30:31.397632
fast1 2020-04-09T13:30:32.392857
fast2 2020-04-09T13:30:33.392862
slow0 2020-04-09T13:30:34.393329
fast3 2020-04-09T13:30:35.394072
fast4 2020-04-09T13:30:36.392825
fast5 2020-04-09T13:30:37.392720
slow1 2020-04-09T13:30:38.390834
    * */

    clock.withLatestFrom(feed, (tick, time) -> tick + " " + time)
        .subscribe(log::info);
    /*
fast1 2020-04-09T13:32:25.499661
fast2 2020-04-09T13:32:26.494348
slow0 2020-04-09T13:32:27.494485
fast3 2020-04-09T13:32:27.494485
fast4 2020-04-09T13:32:28.494399
fast5 2020-04-09T13:32:29.495581
slow1 2020-04-09T13:32:30.494777
    * */
  }

  private void ex1_3() throws InterruptedException {
    // Old style: feed.register(listener)
    // SomeFeed feed = new SomeFeed()
    Flux feedFlux = Flux.create(subscriber -> {
      subscriber.next(1);
      subscriber.next(2);
      subscriber.next(3);
      subscriber.next(4);
      subscriber.next(5);
      subscriber.complete();
    }, OverflowStrategy.BUFFER);
    ConnectableFlux publish = feedFlux.publish();
    publish.subscribe(log::info);
    Thread.sleep(2000);
    publish.subscribe(log::info);
    publish.connect();
    publish.subscribe(log::info);
  }

  private void ex2() {
    Flux.range(1, 100)
        .map(n -> n * 10)
        .filter(n -> n % 3 == 0)
        .parallel(10)
        .doOnNext(log::info)
        .sequential()
        .map(n -> {
          if (n > 30) {
            throw new RuntimeException();
          }
          return n;
        })
        .subscribe(log::info);
  }
}
