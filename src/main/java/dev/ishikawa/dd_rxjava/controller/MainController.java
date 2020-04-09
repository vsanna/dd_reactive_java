package dev.ishikawa.dd_rxjava.controller;

import dev.ishikawa.dd_rxjava.service.ReactorService;
import dev.ishikawa.dd_rxjava.service.Rx2Service;
import dev.ishikawa.dd_rxjava.service.RxService;
import dev.ishikawa.dd_rxjava.service.User;
import io.reactivex.rxjava3.core.Observable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@AllArgsConstructor
public class MainController {

  private final RxService rxService;
  private final Rx2Service rx2Service;
  private final ReactorService reactorService;

  @GetMapping("/rxjava")
  public DeferredResult<List<User>> rxjava() {
    // もしTがlistなら Observable<T> -> Single<U>に変換する
    Observable<List<User>> o = rx2Service.getUsers().reduce(
        (List<User>) new ArrayList<User>(),
        (list, user) -> {
          list.add(user);
          return list;
        }).toObservable();

    return toDeferredResult(o);
  }

  @GetMapping("/reactor")
  public DeferredResult<List<User>> reactor() {
    Flux<List<User>> source = Flux.from(reactorService.getUsers().reduce(
        (List<User>) new ArrayList<User>(),
        (list, user) -> {
          list.add(user);
          return list;
        }
    ));
    return toDeferredResult(source);
  }

  @GetMapping("/play")
  public String play() throws InterruptedException {
    reactorService.play();
    return "ok";
  }

  private <T> DeferredResult<T> toDeferredResult(Observable<T> observable) {
    DeferredResult<T> result = new DeferredResult<T>(2000L); // timeout
    // onNextでsetResultすると最初の一個目のデータでresponce返してしまう
    observable//.map(ResponseEntity::ok)
        .subscribe(
            result::setResult,
            result::setErrorResult);

    return result;
  }

  private <T> DeferredResult<T> toDeferredResult(Flux<T> flux) {
    DeferredResult<T> result = new DeferredResult<T>(2000L); // timeout
    // onNextでsetResultすると最初の一個目のデータでresponce返してしまう
    flux//.map(ResponseEntity::ok)
        .subscribe(
            result::setResult,
            result::setErrorResult);

    return result;
  }
}
