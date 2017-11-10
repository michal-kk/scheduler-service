package com.example.demo;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableScheduling
public class SchedulerServiceApplication {
  private static final Logger log = LoggerFactory.getLogger(SchedulerServiceApplication.class);

  private AtomicBoolean active = new AtomicBoolean(false);

  @Autowired
  private Environment environment;

  @Autowired
  private ScheduleRepository repository;

  public static void main(String[] args) {
    SpringApplication.run(SchedulerServiceApplication.class, args);
  }

  @GetMapping
  public List<Schedule> getAll() {
    return repository.findAll();
  }

  @DeleteMapping
  public void deleteAll() {
    repository.deleteAll();
  }

  @PatchMapping
  public void purgeRunTimes() {
    List<Schedule> schedules = repository.findAll();

    for (Schedule schedule : schedules) {
      schedule.setLastRun(null);
      schedule.setRuns(new ArrayList<>());
    }

    repository.save(schedules);
  }

  @PostMapping
  public Schedule save(@RequestBody Schedule schedule) {
    CronSequenceGeneratorInstantAdapter generatorInstantAdapter =
        new CronSequenceGeneratorInstantAdapter(schedule.getCron());
    schedule.setNextRun(generatorInstantAdapter.next(Instant.now()));

    repository.save(schedule);

    Schedule exampleSchedule = new Schedule();
    exampleSchedule.setName(schedule.getName());
    return repository.findOne(Example.of(schedule));
  }

  @RequestMapping(value = "/start")
  public void start() {
    active.set(true);
  }

  @RequestMapping(value = "/stop")
  public void stop() {
    active.set(false);
  }

  @Scheduled(fixedRate = 1000)
  public void run() {
    if (!active.get()) {
      return;
    }

    log.info("Running tasks");

    List<Schedule> schedules = repository.findAll();

    for (Schedule schedule : schedules) {
      if (schedule.getLastRun() != null) {
        if (schedule.getNextRun().isBefore(Instant.now())) {
          log.info("Running {}", schedule.getName());

          Instant lastRun = Instant.now();

          CronSequenceGeneratorInstantAdapter adapter =
              new CronSequenceGeneratorInstantAdapter(schedule.getCron());
          Instant nextRun = adapter.next(lastRun);

          schedule.getRuns()
              .add(String.format("%s@%s", lastRun.atZone(ZoneId.systemDefault()).toString(),
                  environment.getProperty("local.server.port")));
          schedule.setLastRun(lastRun);
          schedule.setNextRun(nextRun);

          repository.save(schedule);
        }
      } else {
        log.info("Running {}", schedule.getName());
        schedule.setLastRun(Instant.now());

        repository.save(schedule);
      }
    }
  }
}
