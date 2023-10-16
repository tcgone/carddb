package tcgone.carddb.tools;

import tcgone.carddb.model.Card;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Locale;

/**
 * Downloads card images from pokemontcg.io
 *
 * @author axpendix@hotmail.com
 * @since 06.06.2019
 */
public class ScanDownloader {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScanDownloader.class);

  public void downloadAll(List<Card> cards) {
    for (Card card : cards) {
      try {
        String cardDir = card.getExpansion().getEnumId().toLowerCase(Locale.ENGLISH);
        new File(String.format("scans/%s", cardDir)).mkdirs();
        String urlString = String.format("https://images.pokemontcg.io/%s/%s_hires.png", card.getExpansion().getPioId(), card.getNumber());
        log.info("Downloading {}", urlString);
        String filename = String.format("scans/%s/%s.png", cardDir, card.getNumber());
        URL url = new URL(urlString);
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(filename);
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        fileOutputStream.close();
        readableByteChannel.close();
      } catch (java.io.FileNotFoundException e) {
        log.error(e.getMessage(), e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
