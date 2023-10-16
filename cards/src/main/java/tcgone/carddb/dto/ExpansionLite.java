package tcgone.carddb.dto;

import java.util.Objects;

public class ExpansionLite {
  private final String id;
  private final String code;
  private final String name;

  public ExpansionLite(String id, String code, String name) {
    this.id = id;
    this.code = code;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExpansionLite that = (ExpansionLite) o;
    return Objects.equals(id, that.id) && Objects.equals(code, that.code) && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, code, name);
  }

  @Override
  public String toString() {
    return "ExpansionLite{" +
      "id='" + id + '\'' +
      ", code='" + code + '\'' +
      ", name='" + name + '\'' +
      '}';
  }
}
