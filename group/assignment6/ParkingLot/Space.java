package assignment6.ParkingLot;

import java.util.Objects;

/**
 * Represents a space with an ID.
 */
public class Space {
  private final int id;

  public Space(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Space space = (Space) o;
    return id == space.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Space " + id;
  }
}
