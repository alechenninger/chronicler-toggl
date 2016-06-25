package com.github.alechenninger.chronicler.toggl;

import com.github.alechenninger.chronicler.Plugin;
import com.github.alechenninger.chronicler.PluginInfo;
import com.github.alechenninger.chronicler.TimeSheetFactory;
import org.apache.commons.cli.Options;

public class TogglPlugin implements Plugin {
  @Override
  public TimeSheetFactory timeSheetFactory() {
    return null;
  }

  @Override
  public PluginInfo info() {
    return new PluginInfo() {
      @Override
      public String url() {
        return "https://github.com/alechenninger/chronicler-toggl";
      }

      @Override
      public String name() {
        return "chronicler-toggl";
      }

      @Override
      public Options cmdLineOptions() {
        return null;
      }

      @Override
      public String version() {
        return "0.1.0-SNAPSHOT";
      }

      @Override
      public String exampleUsage() {
        return "TODO";
      }

      @Override
      public String description() {
        return "TODO";
      }
    };
  }
}
