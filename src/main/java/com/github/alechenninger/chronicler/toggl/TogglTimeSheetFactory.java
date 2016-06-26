package com.github.alechenninger.chronicler.toggl;

import ch.simas.jtoggl.JToggl;
import ch.simas.jtoggl.Project;
import ch.simas.jtoggl.TimeEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alechenninger.chronicler.ChroniclerException;
import com.github.alechenninger.chronicler.TimeEntryCoordinates;
import com.github.alechenninger.chronicler.TimeSheet;
import com.github.alechenninger.chronicler.TimeSheetFactory;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TogglTimeSheetFactory implements TimeSheetFactory {
  private final TogglClientFactory togglFactory;

  private static final ObjectMapper jsonMapper = new ObjectMapper();
  private static final Logger log = Logger.getLogger(TogglTimeSheetFactory.class.getName());

  public TogglTimeSheetFactory(TogglClientFactory togglFactory) {
    this.togglFactory = togglFactory;
  }

  @Override
  public TimeSheet getTimeSheet(String[] args) {
    return getTimeSheet(args, Optional.empty());
  }

  @Override
  public TimeSheet getTimeSheet(String[] args, ZonedDateTime lastRecordedEntryTime) {
    return getTimeSheet(args, Optional.of(lastRecordedEntryTime));
  }

  private TimeSheet getTimeSheet(String[] args, Optional<ZonedDateTime> lastRecordedEntryTime) {
    try {
      TogglTimeSheetOptions options = new TogglTimeSheetOptions(args);
      
      Instant start = options.start()
          .map(d -> d.atStartOfDay(ZoneId.systemDefault()))
          .orElseGet(() -> lastRecordedEntryTime.orElseThrow(
              () -> new ChroniclerException("No start time or last recorded entry time provided.")))
          .toInstant();
      Instant end = options.end().atStartOfDay(ZoneId.systemDefault()).toInstant();
      Map<String, TimeEntryCoordinates> projectCoords = readProjectMap(options.projectMapPath());

      JToggl toggl = togglFactory.getClient(options);

      log.info("Querying toggl for time entries between " + start + " and " + end);
      List<TimeEntry> timeEntries = toggl.getTimeEntries(Date.from(start), Date.from(end));

      Set<Long> workspaceIds = timeEntries.stream()
          .map(TimeEntry::getWid)
          .collect(Collectors.toSet());

      log.info("Querying toggl for projects in workspaces: " + workspaceIds);

      Map<Long, Project> projects = workspaceIds.stream()
          .flatMap(wid -> toggl.getWorkspaceProjects(wid).stream())
          .collect(Collectors.toMap(Project::getId, Function.identity()));

      timeEntries.forEach(e -> {
        Long pid = e.getPid();

        if (!projects.containsKey(pid)) {
          throw new ChroniclerException("No project found for pid: " + pid);
        }

        e.setProject(projects.get(pid));
      });

      log.info("Retrieved time entries from toggl: " + timeEntries);

      return new TogglTimeSheet(timeEntries, projectCoords);
    } catch (ParseException | IOException e) {
      throw new ChroniclerException(e);
    }
  }

  private Map<String, TimeEntryCoordinates> readProjectMap(Path projectMapPath) throws IOException {
    return jsonMapper.readValue(
        projectMapPath.toFile(),
        new TypeReference<Map<String, TimeEntryCoordinates>>() {});
  }
}
