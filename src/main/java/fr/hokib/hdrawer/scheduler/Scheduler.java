package fr.hokib.hdrawer.scheduler;

import java.time.Duration;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import fr.hokib.hdrawer.HDrawer;

public interface Scheduler {

	void run(Runnable task);

	void runInRegion(Location loc, Runnable task);

	void runAsync(Runnable task);
	
	default void runEntity(Entity entity, Runnable task) {
		run(task);
	}
	
	void runRepeating(Consumer<ScheduledTask> task, int delayTicks, int intervalTicks);
	
	/**
	 * Run the given task each given ticks, after waiting a delay
	 * 
	 * @param task task to run
	 * @param delayTicks delay before starting task
	 * @param intervalTicks ticks between each task runned
	 * @return the running task
	 */
	ScheduledTask runRepeating(Runnable task, int delayTicks, int intervalTicks);
	
	default ScheduledTask runRepeating(Runnable task, int intervalTicks) {
		return runRepeating(task, intervalTicks, null);
	}
	
	/**
	 * Run repeating task each given ticks
	 * 
	 * @param task the task to run
	 * @param intervalTicks ticks between each call
	 * @param name name of task (may be ignored)
	 * @return the running task
	 */
	ScheduledTask runRepeating(Runnable task, int intervalTicks, @Nullable String name);
	
	/**
	 * Run task after waiting given ticks
	 * 
	 * @param task task to run
	 * @param delayTicks ticks before running task
	 * @return the running task
	 */
	ScheduledTask runDelayed(Runnable task, int delayTicks);
	
	/**
	 * Run repeating task each given ticks according to given entity
	 * 
	 * @param entity The entity which this task is about
	 * @param task the task to run
	 * @param delayTicks delay before starting task
	 * @param intervalTicks ticks between each task runned
	 * @return the running task
	 */
	default ScheduledTask runEntityRepeating(Entity entity, Runnable task, int delayTicks, int intervalTicks) {
		return runRepeating(task, delayTicks, intervalTicks);
	}
	
	/**
	 * Run task after waiting given ticks according to given entity
	 * 
	 * @param entity The entity which this task is about
	 * @param task task to run
	 * @param delayTicks ticks before running task
	 * @return the running task
	 */
	default ScheduledTask runEntityDelayed(Entity entity, Runnable task, int delayTicks) {
		return runDelayed(task, delayTicks);
	}
	
	void teleportEntity(Entity entity, Location location);
	
	ScheduledTask runRepeatingAsync(Runnable task, Duration delay, Duration interval, @Nullable String name);
	
	static Scheduler getScheduler() {
		return HDrawer.get().getScheduler();
	}
}
