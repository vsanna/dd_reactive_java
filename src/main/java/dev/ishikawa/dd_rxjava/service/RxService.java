package dev.ishikawa.dd_rxjava.service;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class RxService {

  public void play() throws InterruptedException {
    RxService app = new RxService();
//    app.ex1();
//    app.ex1_2();
//    app.ex2();
//    app.ex2_2();
//    app.ex3();
//    app.ex3_2();
//    app.ex3_3();
    app.ex3_4();
  }

  /*
   * very basic
   * - Observable + subscribe
   * - only main thread
   18:00:58.217 [main] [tid: 1] INFO  :63 - 50
   18:00:58.219 [main] [tid: 1] INFO  :63 - 60
   18:00:58.219 [main] [tid: 1] INFO  :63 - 70
   18:00:58.219 [main] [tid: 1] INFO  :63 - 80
   18:00:58.219 [main] [tid: 1] INFO  :63 - 90
   * */
  private void ex1() {
    // source
    Observable<Integer> source = Observable.fromIterable(
        new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9))
    );
    // intermediate operations
    // they are non descruptive methods
    source = source
        .map(n -> n * 10).filter(n -> n > 40);
    // subscribe
    source.subscribe(log::info);
  }

  /*
   * very basic
   * - observeOnだけ使いparallel使わないと別threadを使いはするがその1threadだけを使うのでconcurrentだがpararrelではない
   18:00:58.253 [RxComputationThreadPool-1] [tid: 16] INFO  :63 - 50
   18:00:58.254 [RxComputationThreadPool-1] [tid: 16] INFO  :63 - 60
   18:00:58.255 [RxComputationThreadPool-1] [tid: 16] INFO  :63 - 70
   18:00:58.255 [RxComputationThreadPool-1] [tid: 16] INFO  :63 - 80
   18:00:58.255 [RxComputationThreadPool-1] [tid: 16] INFO  :63 - 90
   * */
  private void ex1_2() {
    // source
    Observable<Integer> source = Observable.fromIterable(
        new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9))
    );
    // intermediate operations
    // they are non descruptive methods
    source = source
        .observeOn(Schedulers.computation())
        .map(n -> n * 10).filter(n -> n > 40);
    // subscribe
    source.subscribe(log::info);
  }

  /*
   * Flowable
   * - backpressure
   * - pararrel vs concurrent
   *
   * ストリームの下流側（Observer、子Observable）の都合に合わせてemitする回数を制限するObservable（Cold Observable）と、
   *   - これはpull型だから。
   *   - Observable.from(Iterable)
   *   - Observable.range()
   * 下流の都合に関係なく自分のペースでemitするObservable（Hot Observable）が存在
   *   - これはpush型だから。
   *   - Observable.interval()
   * backpressure有効
   * :57 - light: 154
   * :61 - heavy: 154
   * */
  private void ex2() throws InterruptedException {
    // most basic
    Flowable<Integer> source = Flowable.range(0, 1000000000);
    source = source
        .parallel()
        .runOn(Schedulers.computation())
        .doOnNext(n -> {
          log.info("light: {}", n);
          Thread.sleep(1); // light operation
        })
        .doOnNext(n -> {
          log.info("heavy: {}", n);
          Thread.sleep(300); // heavy operation
        })
        .sequential();
    source.subscribe(log::info);
    Thread.sleep(3000);
  }

  /*
   * without Flowable. no backpressure
   * */
  private void ex2_2() throws InterruptedException {
//    Observable<Long> source = Observable.interval(1, TimeUnit.MILLISECONDS);
//    source = source
//        .observeOn(Schedulers.computation())
//        .doOnNext(n -> {
//          log.info("Observable light: {}", n);
//          Thread.sleep(1); // light operation
//        })
//        .doOnNext(n -> {
//          log.info("Observable light: {}", n);
//          Thread.sleep(1); // light operation
//        });
//    source.subscribe(n -> {
//      log.info("Observable last : {}", n);
//      Thread.sleep(300);
//    });

    PublishSubject<Integer> source = PublishSubject.<Integer>create();

    source.observeOn(Schedulers.computation())
        .subscribe((n) -> {
          log.info("Observable last: {}", n);
          Thread.sleep(300);
        }, Throwable::printStackTrace);

    IntStream.range(1, 1_000_000).forEach(source::onNext);

    Thread.sleep(5000);
  }


  /*
  * subscribeOn vs observeOn
  * なにもないとObservableの挙動(createの関数内)もObserverの挙動(subscribeの関数内)も
  * 同一スレッド
11:03:00.930 [http-nio-8080-exec-2] [tid: 27] INFO  :144 - ex3 before
11:03:00.930 [http-nio-8080-exec-2] [tid: 27] INFO  :146 - ex3 before 1
11:03:00.931 [http-nio-8080-exec-2] [tid: 27] INFO  :158 - ex3 n: 1
11:03:00.931 [http-nio-8080-exec-2] [tid: 27] INFO  :158 - ex3 n: 2
11:03:00.931 [http-nio-8080-exec-2] [tid: 27] INFO  :158 - ex3 n: 3
11:03:00.931 [http-nio-8080-exec-2] [tid: 27] INFO  :158 - ex3 n: 4
11:03:00.931 [http-nio-8080-exec-2] [tid: 27] INFO  :158 - ex3 n: 5
11:03:00.932 [http-nio-8080-exec-2] [tid: 27] INFO  :152 - ex3 after 5
11:03:00.932 [http-nio-8080-exec-2] [tid: 27] INFO  :154 - ex3 after onCompleted
11:03:00.932 [http-nio-8080-exec-2] [tid: 27] INFO  :160 - ex3 after
  * */
  private void ex3() throws InterruptedException {
    log.info("ex3 before");
    Flowable<Integer> source = Flowable.create(subscriber -> {
      log.info("ex3 before 1");
      subscriber.onNext(1);
      subscriber.onNext(2);
      subscriber.onNext(3);
      subscriber.onNext(4);
      subscriber.onNext(5);
      log.info("ex3 after 5");
      subscriber.onComplete();
      log.info("ex3 after onCompleted");
    }, BackpressureStrategy.BUFFER);

    source.subscribe(n -> {
      log.info("ex3 n: {}", n);
    });
    log.info("ex3 after");
  }

  /*
  * subscribeOn
  * ObservableもObserverもともにメインとは別スレッド
  *
11:05:10.941 [http-nio-8080-exec-1] [tid: 27] INFO  :191 - ex3_2 before
11:05:10.948 [http-nio-8080-exec-1] [tid: 27] INFO  :207 - ex3_2 after
11:05:10.948 [RxComputationThreadPool-1] [tid: 49] INFO  :193 - ex3_2 before 1
11:05:10.948 [RxComputationThreadPool-1] [tid: 49] INFO  :205 - ex3_2 n: 1
11:05:10.948 [RxComputationThreadPool-1] [tid: 49] INFO  :205 - ex3_2 n: 2
11:05:10.949 [RxComputationThreadPool-1] [tid: 49] INFO  :205 - ex3_2 n: 3
11:05:10.949 [RxComputationThreadPool-1] [tid: 49] INFO  :205 - ex3_2 n: 4
11:05:10.949 [RxComputationThreadPool-1] [tid: 49] INFO  :205 - ex3_2 n: 5
11:05:10.949 [RxComputationThreadPool-1] [tid: 49] INFO  :199 - ex3_2 after 5
11:05:10.950 [RxComputationThreadPool-1] [tid: 49] INFO  :201 - ex3_2 after onCompleted

11:11:54.449 [http-nio-8080-exec-1] [tid: 27] INFO  :194 - ex3_2 before
11:11:54.457 [http-nio-8080-exec-1] [tid: 27] INFO  :217 - ex3_2 after
11:11:54.457 [RxComputationThreadPool-1] [tid: 49] INFO  :196 - ex3_2 before 1
11:11:54.457 [RxComputationThreadPool-1] [tid: 49] INFO  :206 - ex3_2 in map
11:11:54.457 [RxComputationThreadPool-1] [tid: 49] INFO  :211 - ex3_2 in map2
11:11:54.457 [RxComputationThreadPool-1] [tid: 49] INFO  :215 - ex3_2 n: 100
11:11:54.457 [RxComputationThreadPool-1] [tid: 49] INFO  :206 - ex3_2 in map
11:11:54.458 [RxComputationThreadPool-1] [tid: 49] INFO  :211 - ex3_2 in map2
11:11:54.458 [RxComputationThreadPool-1] [tid: 49] INFO  :215 - ex3_2 n: 200
11:11:54.458 [RxComputationThreadPool-1] [tid: 49] INFO  :206 - ex3_2 in map
11:11:54.458 [RxComputationThreadPool-1] [tid: 49] INFO  :211 - ex3_2 in map2
11:11:54.458 [RxComputationThreadPool-1] [tid: 49] INFO  :215 - ex3_2 n: 300
11:11:54.458 [RxComputationThreadPool-1] [tid: 49] INFO  :200 - ex3_2 after 5
11:11:54.459 [RxComputationThreadPool-1] [tid: 49] INFO  :202 - ex3_2 after onCompleted

  * */
  private void ex3_2() throws InterruptedException {
    log.info("ex3_2 before");
    Flowable<Integer> source = Flowable.create(subscriber -> {
      log.info("ex3_2 before 1");
      subscriber.onNext(1);
      subscriber.onNext(2);
      subscriber.onNext(3);
      log.info("ex3_2 after 5");
      subscriber.onComplete();
      log.info("ex3_2 after onCompleted");
    }, BackpressureStrategy.BUFFER);
    source
        .map(n -> {
          log.info("ex3_2 in map");
          return n * 10;
        })
        .subscribeOn(Schedulers.computation())
        .map(n -> {
          log.info("ex3_2 in map2");
          return n * 10;
        })
        .subscribe(n -> {
          log.info("ex3_2 n: {}", n);
        });
    log.info("ex3_2 after");
  }

  /*
   * observeOn
   * observeOnを挟んだ移行が別スレッド
   *
11:05:10.948 [http-nio-8080-exec-1] [tid: 27] INFO  :228 - ex3_3 before
11:05:10.952 [http-nio-8080-exec-1] [tid: 27] INFO  :230 - ex3_3 before 1
11:05:10.953 [http-nio-8080-exec-1] [tid: 27] INFO  :236 - ex3_3 after 5
11:05:10.953 [RxComputationThreadPool-2] [tid: 50] INFO  :242 - ex3_3 n: 1
11:05:10.953 [http-nio-8080-exec-1] [tid: 27] INFO  :238 - ex3_3 after onCompleted
11:05:10.953 [RxComputationThreadPool-2] [tid: 50] INFO  :242 - ex3_3 n: 2
11:05:10.953 [RxComputationThreadPool-2] [tid: 50] INFO  :242 - ex3_3 n: 3
11:05:10.953 [http-nio-8080-exec-1] [tid: 27] INFO  :244 - ex3_3 after
11:05:10.953 [RxComputationThreadPool-2] [tid: 50] INFO  :242 - ex3_3 n: 4
11:05:10.953 [RxComputationThreadPool-2] [tid: 50] INFO  :242 - ex3_3 n: 5

11:10:20.413 [http-nio-8080-exec-1] [tid: 26] INFO  :247 - ex3_3 before
11:10:20.417 [http-nio-8080-exec-1] [tid: 26] INFO  :249 - ex3_3 before 1
11:10:20.417 [http-nio-8080-exec-1] [tid: 26] INFO  :261 - ex3_3 in map
11:10:20.418 [http-nio-8080-exec-1] [tid: 26] INFO  :261 - ex3_3 in map
11:10:20.418 [RxComputationThreadPool-2] [tid: 49] INFO  :266 - ex3_3 in map2
11:10:20.418 [RxComputationThreadPool-2] [tid: 49] INFO  :270 - ex3_3 n: 100
11:10:20.418 [http-nio-8080-exec-1] [tid: 26] INFO  :261 - ex3_3 in map
11:10:20.418 [RxComputationThreadPool-2] [tid: 49] INFO  :266 - ex3_3 in map2
11:10:20.418 [RxComputationThreadPool-2] [tid: 49] INFO  :270 - ex3_3 n: 200
11:10:20.418 [http-nio-8080-exec-1] [tid: 26] INFO  :261 - ex3_3 in map
11:10:20.418 [RxComputationThreadPool-2] [tid: 49] INFO  :266 - ex3_3 in map2
11:10:20.418 [RxComputationThreadPool-2] [tid: 49] INFO  :270 - ex3_3 n: 300
11:10:20.418 [http-nio-8080-exec-1] [tid: 26] INFO  :261 - ex3_3 in map
11:10:20.418 [RxComputationThreadPool-2] [tid: 49] INFO  :266 - ex3_3 in map2
11:10:20.418 [http-nio-8080-exec-1] [tid: 26] INFO  :255 - ex3_3 after 5
11:10:20.418 [RxComputationThreadPool-2] [tid: 49] INFO  :270 - ex3_3 n: 400
11:10:20.418 [RxComputationThreadPool-2] [tid: 49] INFO  :266 - ex3_3 in map2
11:10:20.418 [http-nio-8080-exec-1] [tid: 26] INFO  :257 - ex3_3 after onCompleted
11:10:20.419 [RxComputationThreadPool-2] [tid: 49] INFO  :270 - ex3_3 n: 500
11:10:20.419 [http-nio-8080-exec-1] [tid: 26] INFO  :272 - ex3_3 after

   *
   *
   * */
  private void ex3_3() throws InterruptedException {
    log.info("ex3_3 before");
    Flowable<Integer> source = Flowable.create(subscriber -> {
      log.info("ex3_3 before 1");
      subscriber.onNext(1);
      subscriber.onNext(2);
      subscriber.onNext(3);
      log.info("ex3_3 after 5");
      subscriber.onComplete();
      log.info("ex3_3 after onCompleted");
    }, BackpressureStrategy.BUFFER);
    source
        .map(n -> {
          log.info("ex3_3 in map");
          return n * 10;
        })
        .observeOn(Schedulers.computation())
        .map(n -> {
          log.info("ex3_3 in map2");
          return n * 10;
        })
        .subscribe(n -> {
          log.info("ex3_3 n: {}", n);
        });
    log.info("ex3_3 after");
  }


  /*
11:21:44.121 [http-nio-8080-exec-1] [tid: 26] INFO  :305 - ex3_4 before
11:21:44.153 [http-nio-8080-exec-1] [tid: 26] INFO  :307 - ex3_4 before 1
11:21:44.154 [http-nio-8080-exec-1] [tid: 26] INFO  :311 - ex3_4 after 5
11:21:44.153 [RxComputationThreadPool-2] [tid: 50] INFO  :319 - ex3_4 in map
11:21:44.153 [RxComputationThreadPool-1] [tid: 49] INFO  :319 - ex3_4 in map
11:21:44.153 [RxComputationThreadPool-3] [tid: 51] INFO  :319 - ex3_4 in map
11:21:44.154 [RxComputationThreadPool-1] [tid: 49] INFO  :323 - ex3_4 in map2
11:21:44.154 [RxComputationThreadPool-2] [tid: 50] INFO  :323 - ex3_4 in map2
11:21:44.154 [RxComputationThreadPool-3] [tid: 51] INFO  :323 - ex3_4 in map2
11:21:44.155 [http-nio-8080-exec-1] [tid: 26] INFO  :313 - ex3_4 after onCompleted
11:21:44.156 [http-nio-8080-exec-1] [tid: 26] INFO  :330 - ex3_4 after
11:21:44.157 [RxComputationThreadPool-1] [tid: 49] INFO  :328 - ex3_4 n: 100
11:21:44.157 [RxComputationThreadPool-1] [tid: 49] INFO  :328 - ex3_4 n: 200
11:21:44.157 [RxComputationThreadPool-1] [tid: 49] INFO  :328 - ex3_4 n: 300
  * */
  private void ex3_4() throws InterruptedException {
    log.info("ex3_4 before");
    Flowable<Integer> source = Flowable.create(subscriber -> {
      log.info("ex3_4 before 1");
      subscriber.onNext(1);
      subscriber.onNext(2);
      subscriber.onNext(3);
      log.info("ex3_4 after 5");
      subscriber.onComplete();
      log.info("ex3_4 after onCompleted");
    }, BackpressureStrategy.BUFFER);
    source
        .parallel()
        .runOn(Schedulers.computation())
        .map(n -> {
          log.info("ex3_4 in map");
          return n * 10;
        })
        .map(n -> {
          log.info("ex3_4 in map2");
          return n * 10;
        })
        .sequential()
        .subscribe(n -> {
          log.info("ex3_4 n: {}", n);
        });
    log.info("ex3_4 after");
  }
}
