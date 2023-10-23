package tcgone.carddb.model;

import lombok.*;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class EnhancedCard extends Card {
  protected Expansion expansion;
  protected CardType superType;
  protected List<CardType> subTypes;
  protected CardType stage;
  protected String scanUrl;
  protected List<String> evolvesTo;
  protected String copyOf;
  protected String fullNameV1;
  protected String fullName;
  protected String seoName;
  protected String fullText;
  protected List<Variant> variants;
  protected List<String> legalInFormats;

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
