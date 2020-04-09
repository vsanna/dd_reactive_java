package dev.ishikawa.dd_rxjava.service;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import lombok.Builder;
import lombok.Getter;
import reactor.core.publisher.Flux;

@Getter
@Builder(toBuilder = true)
public class User {

  private final int id;
  private final String name;
  private final int age;

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", age=" + age +
        '}';
  }

  public static Flowable<User> findAll() {
    return Flowable.create(subscriber -> {
      for (int i = 0; i < 100; i++) {
        User user = User.builder()
            .age(20 + i)
            .name("user " + Integer.valueOf(i).toString())
            .id(i).build();
        subscriber.onNext(user);
      }
      subscriber.onComplete();
    }, BackpressureStrategy.BUFFER);
  }

  public static Flux<User> findAllFlux() {
    return Flux.create(subscriber -> {
      for (int i = 0; i < 100; i++) {
        User user = User.builder()
            .age(20 + i)
            .name("user " + Integer.valueOf(i).toString())
            .id(i).build();
        subscriber.next(user);
      }
      subscriber.complete();
    });
  }
}
