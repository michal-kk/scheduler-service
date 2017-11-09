package com.example.demo;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Schedule {

  @Id
  private String id;

  private String name;

  private String cron;

  private Instant lastRun = null;
  private Instant nextRun = null;

  private String description;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCron() {
    return cron;
  }

  public void setCron(String cron) {
    this.cron = cron;
  }

  public Instant getLastRun() {
    return lastRun;
  }

  public void setLastRun(Instant lastRun) {
    this.lastRun = lastRun;
  }

  public Instant getNextRun() {
    return nextRun;
  }

  public void setNextRun(Instant nextRun) {
    this.nextRun = nextRun;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
