package com.github.alechenninger.chronicler.toggl;

import ch.simas.jtoggl.JToggl;
import com.github.alechenninger.chronicler.ChroniclerException;

import java.util.Optional;

public interface TogglClientFactory {
  JToggl getClient(TogglTimeSheetOptions options);

  class Default implements TogglClientFactory {
    @Override
    public JToggl getClient(TogglTimeSheetOptions options) {
      Optional<String> apiKey = options.apiKey();

      if (apiKey.isPresent()) {
        return new JToggl(apiKey.get());
      }

      Optional<UserAndPassword> maybeUserAndPw = options.userAndPassword();

      if (!maybeUserAndPw.isPresent()) {
        throw new ChroniclerException("Expected a toggl apikey or user name and password but " +
            "found neither.");
      }

      UserAndPassword userAndPw = maybeUserAndPw.get();
      return new JToggl(userAndPw.user, userAndPw.password);
    }
  }
}
