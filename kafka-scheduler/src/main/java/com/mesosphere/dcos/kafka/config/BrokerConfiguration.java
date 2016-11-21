package com.mesosphere.dcos.kafka.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class BrokerConfiguration {
  @JsonProperty("cpus")
  private double cpus;
  @JsonProperty("mem")
  private double mem;
  @JsonProperty("heap")
  private HeapConfig heap;
  @JsonProperty("disks")
  private String disks;
  private List<DiskConfiguration> parsedDisks;
  @JsonProperty("kafkaUri")
  private String kafkaUri;
  @JsonProperty("javaUri")
  private String javaUri;
  @JsonProperty("overriderUri")
  private String overriderUri;
  @JsonProperty("port")
  private Long port;

  public BrokerConfiguration() {
    this.parsedDisks = new ArrayList<>();
  }

  @JsonCreator
  public BrokerConfiguration(
      @JsonProperty("cpus") double cpus,
      @JsonProperty("mem") double mem,
      @JsonProperty("heap") HeapConfig heap,
      @JsonProperty("disks") String disks,
      @JsonProperty("kafkaUri") String kafkaUri,
      @JsonProperty("javaUri") String javaUri,
      @JsonProperty("overriderUri") String overriderUri,
      @JsonProperty("port") Long port) {
    this.cpus = cpus;
    this.mem = mem;
    this.heap = heap;
    this.disks = disks;
    this.
    parseDisks();
    this.kafkaUri = kafkaUri;
    this.javaUri = javaUri;
    this.overriderUri = overriderUri;
    this.port = port;
  }

  public double getCpus() {
    return cpus;
  }

  @JsonProperty("cpus")
  public void setCpus(double cpus) {
    this.cpus = cpus;
  }

  public double getMem() {
    return mem;
  }

  @JsonProperty("mem")
  public void setMem(double mem) {
    this.mem = mem;
  }

  public HeapConfig getHeap() {
    return heap;
  }

  @JsonProperty("heap")
  public void setHeap(HeapConfig heap) {
    this.heap = heap;
  }

  public String getDisks() {
    return disks;
  }
  public List<DiskConfiguration> getActualDisks() {
    return parsedDisks;
  }

  @JsonProperty("disks")
  public void setDisks(String disks) {
    this.disks = disks;
    parseDisks();
  }

  private void parseDisks() {
    parsedDisks.clear();
    List<DiskConfiguration> _disks = new ArrayList<DiskConfiguration>();
    for (String tmpdisk : disks.split(",")) {
      String[] tmp = tmpdisk.split(":");
      String type = tmp[0];
      String[] tmp2 = tmp[1].split("=");
      String path = tmp2[0];
      String size = tmp2[1];
      parsedDisks.add(new DiskConfiguration(Double.parseDouble(size), path, type));
    }
  }

  public String getKafkaUri() {
    return kafkaUri;
  }

  @JsonProperty("kafkaUri")
  public void setKafkaUri(String kafkaUri) {
    this.kafkaUri = kafkaUri;
  }

  public String getJavaUri() {
    return javaUri;
  }

  @JsonProperty("javaUri")
  public void setJavaUri(String javaUri) {
    this.javaUri = javaUri;
  }

  public String getOverriderUri() {
    return overriderUri;
  }

  @JsonProperty("overriderUri")
  public void setOverriderUri(String overriderUri) {
    this.overriderUri = overriderUri;
  }

  public Long getPort() {
    return port;
  }

  @JsonProperty("port")
  public void setPort(Long port) {
    this.port = port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BrokerConfiguration that = (BrokerConfiguration) o;
    return Double.compare(that.cpus, cpus) == 0 &&
        Double.compare(that.mem, mem) == 0 &&
        Objects.equals(that.heap, heap) &&
        Objects.equals(that.disks, disks) &&
        Objects.equals(kafkaUri, that.kafkaUri) &&
        Objects.equals(javaUri, that.javaUri) &&
        Objects.equals(overriderUri, that.overriderUri) &&
        Objects.equals(port, that.port);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cpus, mem, heap, disks, kafkaUri, javaUri, overriderUri, port);
  }

  @Override
  public String toString() {
    return "BrokerConfiguration{" +
        "cpus=" + cpus +
        ", mem=" + mem +
        ", heap=" + heap +
        ", disks=" + disks +
        ", kafkaUri='" + kafkaUri + '\'' +
        ", javaUri='" + javaUri + '\'' +
        ", overriderUri='" + overriderUri + '\'' +
        ", port='" + port + '\'' +
        '}';
  }
}
