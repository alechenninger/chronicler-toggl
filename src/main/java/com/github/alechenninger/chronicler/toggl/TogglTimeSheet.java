package com.github.alechenninger.chronicler.toggl;

import ch.simas.jtoggl.TimeEntry;
import com.github.alechenninger.chronicler.TimeEntryCoordinates;
import com.github.alechenninger.chronicler.TimeSheet;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TogglTimeSheet implements TimeSheet {
  private final List<TimeEntry> entries;
  private final Map<String, TimeEntryCoordinates> projectToCoords;

  private static final Logger log = Logger.getLogger(TogglTimeSheet.class.getName());

  public TogglTimeSheet(List<TimeEntry> entries,
      Map<String, TimeEntryCoordinates> projectToCoords) {
    this.entries = Objects.requireNonNull(entries, "entries");
    this.projectToCoords = Objects.requireNonNull(projectToCoords, "projectToCoords");
  }

  @Override
  public List<com.github.alechenninger.chronicler.TimeEntry> getEntries() {
    return entries.stream()
        .flatMap(entry -> {
          String project = entry.getProject().getName();

          if (!projectToCoords.containsKey(project)) {
            log.warning("No time coordinate mapping found for project <" + project + ">. Ignoring " +
                "entry: " + entry.getDescription());
            return Stream.empty();
          }

          TimeEntryCoordinates coords = projectToCoords.get(project);
          ZonedDateTime start = ZonedDateTime.ofInstant(
              entry.getStart().toInstant(), ZoneId.systemDefault());
          ZonedDateTime stop = Optional.ofNullable(entry.getStop())
              .map(d -> ZonedDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()))
              .orElse(ZonedDateTime.now());
          Duration duration = Duration.between(start, stop);
          float hours = (float) duration.toHours() + ((float) duration.getSeconds() / 60f);

          return Stream.of(new com.github.alechenninger.chronicler.TimeEntry(coords, start, hours));
        })
        .collect(Collectors.toList());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TogglTimeSheet that = (TogglTimeSheet) o;

    if (!entries.equals(that.entries)) return false;
    return projectToCoords.equals(that.projectToCoords);
  }

  @Override
  public int hashCode() {
    int result = entries.hashCode();
    result = 31 * result + projectToCoords.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "TogglTimeSheet{" +
        "entries=" + entries +
        ", projectToCoords=" + projectToCoords +
        '}';
  }
}
