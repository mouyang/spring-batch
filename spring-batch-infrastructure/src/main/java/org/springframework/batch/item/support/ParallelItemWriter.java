/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.item.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.Assert;


/**
 * 
 * This allows independent ItemWriters to be executed in parallel.  This may result in 
 * performance improvements over executing the ItemWriters serially with a CompositeItemWriter.
 * 
 * If any ItemWriters are dependent on each other (i.e. the proper operation of one ItemWriter 
 * depends on another), then they should not be included that instance.  Those dependencies 
 * should be satisfied externally.
 * 
 * The implementation is adapted from {@link http://incomplete-code.blogspot.ca/2013/04/spring-batch-parallel-item-writer.html}.
 * 
 * @author Matthew Ouyang
 * @since 3.0
 * @param <T>
 * @see org.springframework.batch.item.support.CompositeItemWriter
 */
public class ParallelItemWriter<T> implements ItemWriter<T>, InitializingBean {

	private List<ItemWriter<T>> itemWriters;

	private TaskExecutor taskExecutor;

	/**
	 * This is a simple container class that stores the values needed to execute a task 
	 * in parallel: the delegate ItemWriter and the items to be written.
	 * 
	 * @author Matthew Ouyang
	 */
	private class ItemWriterCallable implements Callable<Void> {

		private final ItemWriter<T> itemWriter;
		private final List<? extends T> items;

		public ItemWriterCallable(final ItemWriter<T> itemWriter, final List<? extends T> items) {
			this.itemWriter = itemWriter;
			this.items = items;
		}

		/**
		 * This implementation simply calls the write method of the ItemWriter
		 * 
		 * @throws Exception
		 * @see org.springframework.batch.item.ItemWriter#write(List)
		 */
		@Override
		public Void call() throws Exception {
			itemWriter.write(items);
			return null;
		}
	}

	public void setDelegates(List<ItemWriter<T>> delegates) {
		this.itemWriters = delegates;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * Check mandatory properties - there must ItemWriters and a TaskExecutor.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(itemWriters, "The 'delegates' may not be null");
		Assert.notEmpty(itemWriters, "The 'delegates' may not be empty");
		Assert.notNull(taskExecutor, "Task executor needs to be set");
	}

	/**
	 * This implementation wraps items into one FutureTask per ItemWriter delegate, and 
	 * passes them to the taskExecutor.  This method exits when all ItemWriters have 
	 * completed.
	 */
	@Override
	public void write(List<? extends T> items) throws Exception {
		// create FutureTask instances
		List<FutureTask<Void>> futureTasks = new ArrayList<FutureTask<Void>>(itemWriters.size());
		for (ItemWriter<T> itemWriter : itemWriters) {
			futureTasks.add(new FutureTask<Void>(new ItemWriterCallable(
				itemWriter, items
			)));
		}
		// submit futureTasks to taskExecutor
		for (FutureTask<Void> futureTask : futureTasks) {
			taskExecutor.execute(futureTask);
		}
		// wait for all futureTasks to be completed
		for (int i = 0; i < futureTasks.size(); i++) {
			if (!futureTasks.get(i).isDone()) {
				i = 0;
			}
		}
	}
}