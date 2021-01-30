package tcgone.carddb.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardLite {
  private final String id;
  private final String name;
  private final String imageUrl;

  public CardLite(String id, String name, String imageUrl) {
    this.id = id;
    this.name = name;
    this.imageUrl = imageUrl;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CardLite cardLite = (CardLite) o;
    return Objects.equals(id, cardLite.id) &&
      Objects.equals(name, cardLite.name) &&
      Objects.equals(imageUrl, cardLite.imageUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, imageUrl);
  }

  @Override
  public String toString() {
    return "CardLite{" +
      "id='" + id + '\'' +
      ", name='" + name + '\'' +
      '}';
  }
}
