package tcgone.carddb.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public class EnhancedCard extends Card {
  private Expansion expansion;
  private CardType superType;
  private List<CardType> subTypes;
  private CardType stage;
  private String scanUrl;
  private List<String> evolvesTo;
  private String copyOf;
  private String fullNameV1;
  private String fullName;
  private String seoName;
  private String fullText;
  private List<Variant> variants;
  private List<String> legalInFormats;

  public static EnhancedCard fromCard(Card card) {
    try {
      EnhancedCard enhancedCard = new EnhancedCard();
      PropertyUtils.copyProperties(enhancedCard, card);
      return enhancedCard;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<EnhancedCard> findCardsOfExpansion(List<EnhancedCard> allCards) {
    return allCards
      .stream()
      .filter(card -> card.getExpansion().equals(this.getExpansion()))
      .collect(Collectors.toList());
  }
}
