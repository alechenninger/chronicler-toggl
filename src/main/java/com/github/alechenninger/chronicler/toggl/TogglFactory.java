package com.github.alechenninger.chronicler.toggl;

import ch.simas.jtoggl.JToggl;

public interface TogglFactory {
  JToggl getClient(TogglTimeSheetOptions options);
}
