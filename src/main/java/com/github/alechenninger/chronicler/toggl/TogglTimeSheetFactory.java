package com.github.alechenninger.chronicler.toggl;

import ch.simas.jtoggl.JToggl;
import ch.simas.jtoggl.TimeEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alechenninger.chronicler.ChroniclerException;
import com.github.alechenninger.chronicler.TimeEntryCoordinates;
import com.github.alechenninger.chronicler.TimeSheet;
import com.github.alechenninger.chronicler.TimeSheetFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Date;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class TogglTimeSheetFactory implements TimeSheetFactory {
  private final TogglClientFactory togglFactory;

  private static final ObjectMapper jsonMapper = new ObjectMapper();

  public TogglTimeSheetFactory(TogglClientFactory togglFactory) {
    this.togglFactory = togglFactory;
  }

  @Override
  public TimeSheet getTimeSheet(String[] args) {

    return null;
  }

  @Override
  public TimeSheet getTimeSheet(String[] args, ZonedDateTime lastRecordedEntryTime) {
    try {
      TogglTimeSheetOptions options = new TogglTimeSheetOptions(args);
      JToggl toggl = togglFactory.getClient(options);

      Instant start = lastRecordedEntryTime.toInstant();
      java.util.Date end = null;
      List<TimeEntry> timeEntries = toggl.getTimeEntries(Date.from(start), end);

      return new TogglTimeSheet(timeEntries, deserializeProjectMap(options.projectMapPath()));
    } catch (Exception e) {
      throw new ChroniclerException(e);
    }
  }

  private Map<String, TimeEntryCoordinates> deserializeProjectMap(Path projectMapPath)
      throws IOException {
    return jsonMapper.readValue(
        projectMapPath.toFile(),
        new TypeReference<Map<String, TimeEntryCoordinates>>() {});
  }
}
