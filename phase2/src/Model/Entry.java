package Model;

import java.io.Serializable;

/** Entry is an abstract serializable class for all types of entry. */
public abstract class Entry implements Serializable {

  /** The name of this entry. */
  protected String name;

  /** The path of this entry. */
  protected String path;

  /**
   * Constructor with Entry's name.
   *
   * @param name String
   */
  public Entry(String name) {
    this.name = name;
  }

  /** A getter for the entry's name. @ return the entry's name. */
  public String getName() {
    return name.trim();
  }

  /**
   * A getter for the entry's path.
   *
   * @return The path of this entry.
   */
  public String getPath() {
    return path;
  }
}
