package tcgone.carddb.merger;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class TestMain {
  public static void main(String[] args) {
    try {
      Terminal terminal = TerminalBuilder.builder().system(true).build();
      LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
      String line = lineReader.readLine("", null, "here is what you can edit");
      System.out.println("You've entered: " + line);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }
}
