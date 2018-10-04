package io.zink.utils

import java.util.concurrent._
import scala.util.DynamicVariable

object ThreadUtils {

  val forkJoinPool = new ForkJoinPool

  /**
    * Abstract class that defines how a task runs in parallel
    */
  abstract class TaskScheduler {
    def schedule[T](body: => T): ForkJoinTask[T]

    def parallel[A, B](taskA: => A, taskB: => B): (A, B) = {
      val right = task {
        taskB
      }
      val left = taskA
      (left, right.join())
    }
  }

  /**
    * Concrete implementation of TaskScheduler, defines how a task should be scheduled
    */
  class DefaultTaskScheduler extends TaskScheduler {

    def schedule[T](body: => T): ForkJoinTask[T] = {
      val newThread = new RecursiveTask[T] {
        def compute = body
      }
      Thread.currentThread match {
        case workerThread: ForkJoinWorkerThread =>
          newThread.fork()
        case _ =>
          forkJoinPool.execute(newThread)
      }
      newThread
    }
  }

  /**
    * DynamicVariable is an implementation of the loan and dynamic patterns. Similar to a java ThreadLocal.
    * When we want to perform a computation on an enclosed scope, where every thread has it's own copy of the variable's
    * value, we use DynamicVariable
    */
  val scheduler = new DynamicVariable[TaskScheduler](new DefaultTaskScheduler)

  /**
    * Method that performs a given task in a different thread if this thread is the worker thread.
    * This method does not automatically join this task to the main thread pool, you need to call .join().
    *
    * @param body - The task to be performed in a different thread
    * @tparam T - The return type of the task to be performed
    * @return A task ready to be joined to the main thread pool
    */
  def task[T](body: => T): ForkJoinTask[T] = {
    scheduler.value.schedule(body)
  }

  /**
    * Method that performs two given tasks in parallel
    * This method is just a simple use case of method task.
    *
    * @param taskA - Task A to be performed in parallel
    * @param taskB - Task B to be performed in parallel
    * @tparam A - Return type of task A
    * @tparam B - Return type of task B
    * @return - The result of executing task A and task B in parallel, after they've joined the main thread pool
    */
  def parallel[A, B](taskA: => A, taskB: => B): (A, B) = {
    scheduler.value.parallel(taskA, taskB)
  }

  /**
    * Method that performs four given tasks in parallel.
    * This method works exactly like parallel(taskA, taskB) except it supports more parallel tasks
    *
    * @param taskA - Task A to be performed in parallel
    * @param taskB - Task B to be performed in parallel
    * @param taskC - Task C to be performed in parallel
    * @param taskD - Task D to be performed in parallel
    * @tparam A - Return type of task A
    * @tparam B - Return type of task B
    * @tparam C - Return type of task C
    * @tparam D - Return type of task D
    * @return - The result of executing task A, B, C and D in parallel, after they've joined the main thread pool
    */
  def parallel[A, B, C, D](taskA: => A, taskB: => B, taskC: => C, taskD: => D): (A, B, C, D) = {
    val ta = task {
      taskA
    }
    val tb = task {
      taskB
    }
    val tc = task {
      taskC
    }
    val td = taskD
    (ta.join(), tb.join(), tc.join(), td)
  }
}