package com.mesosphere.dcos.kafka.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class DiskConfiguration {

  @JsonProperty("size")
  private double size;
  @JsonProperty("path")
  private String path;
  @JsonProperty("type")
  private String type;

  public DiskConfiguration() {

  }

  @JsonCreator
  public DiskConfiguration(
      @JsonProperty("size")double size,
      @JsonProperty("path")String path,
      @JsonProperty("type")String type) {
    this.size = size;
    this.path = path;
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DiskConfiguration that = (DiskConfiguration) o;

    if (Double.compare(that.size, size) != 0) return false;
    if (that.path.equals(path) == false) return false;
    return type.equals(that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(size, path, type);
  }

  public double getSize() {
    return size;
  }

  @JsonProperty("size")
  public void setSize(double size) {
    this.size = size;
  }

  public String getPath() {
    return path;
  }

  @JsonProperty("path")
  public void setPath(String path) {
    this.path = path;
  }

  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }
}
