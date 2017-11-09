package com.example.demo;

import java.time.Instant;
import java.util.Date;

import org.springframework.scheduling.support.CronSequenceGenerator;

class CronSequenceGeneratorInstantAdapter {
  private CronSequenceGenerator cronSequenceGeneratorInstance;

  CronSequenceGeneratorInstantAdapter(String expression) {
    cronSequenceGeneratorInstance = new CronSequenceGenerator(expression);
  }

  Instant next(Instant instant) {
    return cronSequenceGeneratorInstance.next(Date.from(instant)).toInstant();
  }
}