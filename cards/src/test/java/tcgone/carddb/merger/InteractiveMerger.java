package tcgone.carddb.merger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.util.NodeStyleResolver;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.ast.KeepType;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.Getter;
import org.apache.commons.beanutils.PropertyUtils;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.parser.Parser;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;
import tcgone.carddb.data.ConstraintViolation;
import tcgone.carddb.data.ImportException;
import tcgone.carddb.data.Importer;
import tcgone.carddb.data.Task;
import tcgone.carddb.model.Ability;
import tcgone.carddb.model.Card;
import tcgone.carddb.model.Move;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.*;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;

public class InteractiveMerger {

  private final List<Task> taskList = new ArrayList<>();
  private Task currentTask;
  private int currentTaskIndex;
  @Getter // the result
  private final List<Card> modifiedCards = new ArrayList<>();

private final YAMLMapper mapper = YAMLMapper.builder(YAMLFactory.builder()
    .nodeStyleResolver(s -> NodeStyleResolver.NodeStyle.FLOW)
    .build())
  .enable(ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
  .enable(MINIMIZE_QUOTES)
  .enable(USE_SINGLE_QUOTES)
  .disable(WRITE_DOC_START_MARKER)
  .build();

  public InteractiveMerger(Importer importer) {

    try {
      importer.process();
    } catch (ImportException e) {

      for (ConstraintViolation violation : e.getViolations()) {
        if (violation.getTask() != null) {
          Task task = violation.getTask();
          taskList.add(task);
        }
      }

    }

    mainLoop();

  }

  enum Command {
    USE_1('1'),
    USE_2('2'),
    EDIT_MANUAL('m'),
    SKIP('n'),
    GO_BACK('b'),
    GO_FORWARD('f'),
    CLEAR('c'),
    SAVE('s');
    final char letter;

    Command(char letter) {
      this.letter = letter;
    }
    static Optional<Command> fromLetter(char letter) {
      for (Command value : values()) {
        if (value.letter == letter) return Optional.of(value);
      }
      return Optional.empty();
    }
  }

  void mainLoop() {
    while (true) {
      if (taskList.isEmpty()) {
        break;
      }
      if (currentTask == null) {
        currentTask = taskList.get(0);
        currentTaskIndex = 0;
      }
      Command command = handleTask();
      if (command != null) {
        handleCommand(command);
      }
      if (currentTask == null) { // end
        if (askToSaveAll()) {
          handleSaveAll();
          break;
        } else {
          currentTask = taskList.get(0);
          currentTaskIndex = 0;
        }
      }
    }
  }

  Command handleTask(){
    System.out.printf("------TASK: %d / %d------\n", currentTaskIndex + 1, taskList.size());
    System.out.printf("Base: %s <%s>\nVariant: %s <%s>\n", currentTask.getBase().getEnumId(), currentTask.getBase().getScanUrl(), currentTask.getVariant().getEnumId(), currentTask.getVariant().getScanUrl());

    String unformattedMarkdown = "";
    DiffRowGenerator generator = DiffRowGenerator.create()
      .showInlineDiffs(true)
      .inlineDiffByWord(true)
      .oldTag(f -> "~")
      .newTag(f -> "**")
      .build();

    switch (currentTask.getField()) {

      case MOVES: {
//        System.out.printf("   MOVES   \n[1]: %s\n[2]: %s\n", currentTask.getBase().getMoves(), currentTask.getVariant().getMoves());

        List<DiffRow> rows = generator.generateDiffRows(
          currentTask.getBase().getMoves().stream().map(Move::toString).collect(Collectors.toList()),
          currentTask.getVariant().getMoves().stream().map(Move::toString).collect(Collectors.toList())
        );
        StringBuilder sb = new StringBuilder();
        sb.append("|base|variant|\n").append("|-|-|\n");
        for (DiffRow row : rows) {
          sb.append("|").append(row.getOldLine()).append("|").append(row.getNewLine()).append("|\n");
        }
        unformattedMarkdown = sb.toString();

        break;
      }
      case ABILITIES: {
        //        System.out.printf("   ABILITIES   \n[1]: %s\n[2]: %s\n", currentTask.getBase().getAbilities(), currentTask.getVariant().getAbilities());

        List<DiffRow> rows = generator.generateDiffRows(
          currentTask.getBase().getAbilities().stream().map(Ability::toString).collect(Collectors.toList()),
          currentTask.getVariant().getAbilities().stream().map(Ability::toString).collect(Collectors.toList())
        );
        StringBuilder sb = new StringBuilder();
        sb.append("|base|variant|\n").append("|-|-|\n");
        for (DiffRow row : rows) {
          sb.append("|").append(row.getOldLine()).append("|").append(row.getNewLine()).append("|\n");
        }
        unformattedMarkdown = sb.toString();
        break;
      }
      case TEXT: {
//        System.out.printf("   TEXT   \n[1]: %s\n[2]: %s\n", currentTask.getBase().getText(), currentTask.getVariant().getText());

        unformattedMarkdown = "|base|variant|\n" + "|-|-|\n" +
          "|" + currentTask.getBase().getText() + "|" + currentTask.getVariant().getText() + "|";

        break;
      }
    }

    // TODO THIS IS NOT WORKING
    DataHolder options = new MutableDataSet()
      .set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()))
      .set(HtmlRenderer.INDENT_SIZE, 2)
      // for full GFM table compatibility add the following table extension options:
      .set(TablesExtension.COLUMN_SPANS, false)
      .set(TablesExtension.APPEND_MISSING_COLUMNS, true)
      .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
      .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
      .toImmutable();
    Parser parser = Parser.builder(options).build();
    Node document = parser.parse(unformattedMarkdown);
    Formatter formatterWithExtension = Formatter.builder(options).build();
    String textRenderOfMarkdown = formatterWithExtension.render(document);

    System.out.println("---");
    System.out.print(textRenderOfMarkdown);
    System.out.println("---");

    Optional<Command> command = askForCommand(Arrays.asList(Command.USE_1, Command.USE_2, Command.EDIT_MANUAL, Command.SKIP, Command.GO_BACK, Command.GO_FORWARD, Command.CLEAR));
    if (command.isPresent()) {
      return command.get();
    } else {
      System.out.println("Bad command, try again.");
      return null;
    }
  }

  Optional<Command> askForCommand(List<Command> possibleCommands) {
    try (Terminal terminal = TerminalBuilder.builder().jna(true).system(true).build()) {
      System.out.print(possibleCommands.stream().map(c -> String.format("[%s]:%s", c.letter, c.name().toLowerCase())).collect(Collectors.joining(" ")) + " >");
      System.out.flush();
      terminal.enterRawMode();
      NonBlockingReader reader = terminal.reader();
      int read = reader.read();
      if (read > 0) {
        char character = (char) read;
        System.out.println();
        return Command.fromLetter(character);
      } else {
        throw new IOException("EOF");
      }
//      try (Scanner scanner = new Scanner(System.in)) {
//        Pattern oneCharPattern = Pattern.compile("[a-zA-Z0-9]");
//        System.out.println(possibleCommands.stream().map(c -> String.format("[%s]:%s", c.letter, c.name().toLowerCase())).collect(Collectors.joining(" ")));
//        String chat = scanner.next(oneCharPattern);
//        return Command.fromLetter(chat.charAt(0));
//      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  boolean askToSaveAll() {
    Optional<Command> command = askForCommand(Arrays.asList(Command.SAVE, Command.GO_BACK));
    return command.isPresent() && command.get() == Command.SAVE;
  }

  void handleSaveAll() {
    modifiedCards.clear();
    for (Task task : taskList) {
      if (task.isDone() && !task.isSkipped())
        modifiedCards.add(task.getResult());
    }
  }

  void handleCommand(Command command){
    Card result = new Card();
    try {
      PropertyUtils.copyProperties(result, currentTask.getBase());
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    switch (command) {

      case USE_1: {
        switch (currentTask.getField()) {

          case MOVES:
            result.setMoves(currentTask.getBase().getMoves());
            break;
          case ABILITIES:
            result.setAbilities(currentTask.getBase().getAbilities());
            break;
          case TEXT:
            result.setText(currentTask.getBase().getText());
            break;
        }
        currentTask.setResult(result);
        currentTask.setDone(true);
        nextTask();
        break;
      }
      case USE_2: {
        switch (currentTask.getField()) {

          case MOVES:
            result.setMoves(currentTask.getVariant().getMoves());
            break;
          case ABILITIES:
            result.setAbilities(currentTask.getVariant().getAbilities());
            break;
          case TEXT:
            result.setText(currentTask.getVariant().getText());
            break;
        }
        currentTask.setResult(result);
        currentTask.setDone(true);
        nextTask();
        break;
      }
      case EDIT_MANUAL:
        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
          LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
          String line;
          switch (currentTask.getField()) {
            case MOVES:
              line = mapper.writeValueAsString(currentTask.getBase().getMoves());
              line = lineReader.readLine("> ", null, line);
              List<Move> newMoves = mapper.readValue(line, new TypeReference<List<Move>>() {});
              result.setMoves(newMoves);
              break;
            case ABILITIES:
              line = mapper.writeValueAsString(currentTask.getBase().getAbilities());
              line = lineReader.readLine("> ", null, line);
              List<Ability> newAbilities = mapper.readValue(line, new TypeReference<List<Ability>>() {});
              result.setAbilities(newAbilities);
              break;
            case TEXT:
              line = currentTask.getBase().getText();
              line = lineReader.readLine("> ", null, line);
              line = line.replace("\\n", "\n");
              result.setText(line);
              break;
          }
          currentTask.setDone(true);
          nextTask();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        break;
      case SKIP:
        markSkippedTask(currentTask);
        nextTask();
        break;
      case GO_BACK:
        prevTask();
        break;
      case GO_FORWARD:
        nextTask();
        break;
      case CLEAR:
        currentTask.setDone(false);
        nextTask();
        break;
      case SAVE:
        break;
    }
  }

  void nextTask() {
    if (currentTask == null) {
      currentTask = taskList.get(0);
      currentTaskIndex = 0;
    }
    int i = taskList.indexOf(currentTask);
    if (i == taskList.size() - 1) {
      currentTask = null;// END
      currentTaskIndex = -1;
    } else {
      currentTask = taskList.get(i + 1);
      currentTaskIndex = i + 1;
    }
  }

  void prevTask() {
    if (currentTask == null) {
      currentTask = taskList.get(taskList.size() - 1);
      currentTaskIndex = taskList.size() - 1;
    }
    int i = taskList.indexOf(currentTask);
    if (i == 0) {
      currentTask = null;
      currentTaskIndex = -1;
    } else {
      currentTask = taskList.get(i - 1);
      currentTaskIndex = i - 1;
    }
  }

  boolean isSkipped(Task task) {
    // TODO check persistent storage
    return false;
  }

  void markSkippedTask(Task task) {
    task.setSkipped(true);
    // TODO store in persistent storage
  }

}
