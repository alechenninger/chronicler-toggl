package com.github.alechenninger.chronicler.toggl;

import com.github.alechenninger.chronicler.ChroniclerException;
import org.apache.commons.cli.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;

public class TogglTimeSheetOptions {
  private static final String TIME_PATTERN = "yyyy-MM-dd";
  private static final DateTimeFormatter TIME_FORMATTER = ofPattern(TIME_PATTERN);

  private static final Option PROJECT_MAP = OptionBuilder.withLongOpt("projectMap")
      .hasArg()
      .isRequired(false)
      .withDescription("JSON file which maps Toggl entry projects to Rally projects, work " +
          "products, and, optionally, tasks, by project name.")
      .create("tpm");

  private static final Option START_DATE = OptionBuilder.withLongOpt("startDate")
      .hasArg()
      .isRequired(false)
      .withDescription("Date to start report generation (" + TIME_PATTERN + "). Defaults to date " +
          "of last entry uploaded if not provided.")
      .create("ts");

  private static final Option END_DATE = OptionBuilder.withLongOpt("endDate")
      .hasArg()
      .isRequired(false)
      .withDescription("Date to end report generation (" + TIME_PATTERN + "). Defaults to " +
          "today if not provided.")
      .create("te");

  private static final Option API_KEY = OptionBuilder.withLongOpt("apiKey")
      .hasArg()
      .isRequired(false)
      .withDescription("Toggl api key. If not provided, user and password options will be used.")
      .create("ta");

  private static final Option USER = OptionBuilder.withLongOpt("user")
      .hasArg()
      .isRequired(false)
      .withDescription("Toggl user name. Required if no api key provided.")
      .create("tu");

  private static final Option PASSWORD = OptionBuilder.withLongOpt("password")
      .hasArg()
      .isRequired(false)
      .withDescription("Toggl password. Required if no api key provided.")
      .create("tpw");

  private static final Options OPTIONS = new Options()
      .addOption(PROJECT_MAP)
      .addOption(START_DATE)
      .addOption(END_DATE)
      .addOption(API_KEY)
      .addOption(USER)
      .addOption(PASSWORD);

  private static final Path DEFAULT_MAP = Paths.get("projects.json");

  private final CommandLine cli;
  private final Clock clock;

  public static Options getOptions() {
    return OPTIONS;
  }

  public TogglTimeSheetOptions(String[] args) throws ParseException {
    this(args, Clock.systemDefaultZone());
  }

  public TogglTimeSheetOptions(String[] args, Clock clock) throws ParseException {
    this(args, new BasicParser(), clock);
  }

  public TogglTimeSheetOptions(String[] args, CommandLineParser parser, Clock clock)
      throws ParseException {
    this.clock = clock;

    cli = parser.parse(OPTIONS, args);
  }

  public Path projectMapPath() {
    if (cli.hasOption(PROJECT_MAP.getOpt())) {
      return Paths.get(cli.getOptionValue(PROJECT_MAP.getOpt()));
    }

    if (DEFAULT_MAP.toFile().exists()) {
      return DEFAULT_MAP;
    }

    throw new ChroniclerException("No project map specified, and default (" + DEFAULT_MAP + ") "
        + "not found. A project map is necessary to translate Toggl entry projects to Rally"
        + " time sheet entries. Specify one via " + PROJECT_MAP);
  }

  public Optional<LocalDate> start() {
    return Optional.ofNullable(cli.getOptionValue(START_DATE.getOpt()))
        .map(d -> TIME_FORMATTER.parse(d, LocalDate::from));
  }

  public LocalDate end() {
    return Optional.ofNullable(cli.getOptionValue(END_DATE.getOpt()))
        .map(d -> TIME_FORMATTER.parse(d, LocalDate::from))
        .orElse(LocalDate.now(clock));
  }

  public Optional<UserAndPassword> userAndPassword() {
    String user = cli.getOptionValue(USER.getOpt());
    String password = cli.getOptionValue(PASSWORD.getOpt());

    if (user == null && password == null) {
      return Optional.empty();
    }

    if (password == null) {
      throw new ChroniclerException("User provided without a password. Please provide a password.");
    }

    if (user == null) {
      throw new ChroniclerException("Password provided without a user. Please provide a user.");
    }

    return Optional.of(new UserAndPassword(user, password));
  }

  public Optional<String> apiKey() {
    return Optional.ofNullable(cli.getOptionValue(API_KEY.getOpt()));
  }
}
