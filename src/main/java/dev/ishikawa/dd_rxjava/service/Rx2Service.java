package dev.ishikawa.dd_rxjava.service;

import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class Rx2Service {

  public Flowable<User> getUsers() {
    return User.findAll()
        .map(user -> user.toBuilder().age(user.getAge() / 2).build())
        .filter(user -> (user.getAge() % 3) == 0);
  }

}
