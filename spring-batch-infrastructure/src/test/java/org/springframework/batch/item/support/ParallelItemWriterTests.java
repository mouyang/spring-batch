package org.springframework.batch.item.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.springframework.batch.item.ItemWriter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.hamcrest.Matchers.*;

public class ParallelItemWriterTests {

	@Test
	public void testMultipleWriters() throws Exception {
		// Initialize test array; elements have values corresponding to their indices.
		final int testListSize = 100;
		List<Integer> integerList = new ArrayList<Integer>(testListSize);
		for (int i = 0; i < testListSize; i++) {
			integerList.add(i);
		}
		
		// Set up some ItemWriters and the ParallelItemWriter under test.
		ListItemWriter<Integer> listItemWriter1 = new ListItemWriter<Integer>();
		ListItemWriter<Integer> listItemWriter2 = new ListItemWriter<Integer>();
		ParallelItemWriter<Integer> parallelItemWriter = new ParallelItemWriter<Integer>();
		// Set up delegates and pass them to ParallelItemWriter.
		List<ItemWriter<Integer>> delegates = new ArrayList<ItemWriter<Integer>>();
		delegates.add(listItemWriter1);
		delegates.add(listItemWriter2);
		parallelItemWriter.setDelegates(delegates);
		// Add taskExecutor and add it to ParallelItemWriter.
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize(2);
		taskExecutor.setCorePoolSize(2);
		taskExecutor.setQueueCapacity(0);
		taskExecutor.initialize();
		parallelItemWriter.setTaskExecutor(taskExecutor);

		// Now that initialization is complete, execute InitializingBean.afterPropertiesSet() 
		// to make sure the required members have been set.
		parallelItemWriter.afterPropertiesSet();

		// Execute the write method.
		parallelItemWriter.write(integerList);

		// Make sure ItemWriters have the expected elements and nothing else.
		// These checks are dependent on the initialization.
		@SuppressWarnings("unchecked")
		List<Integer> writtenItems1 = (List<Integer>) listItemWriter1.getWrittenItems();
		@SuppressWarnings("unchecked")
		List<Integer> writtenItems2 = (List<Integer>) listItemWriter2.getWrittenItems();
		for (Integer i : integerList) {
			MatcherAssert.assertThat(i, isIn(writtenItems1));
			MatcherAssert.assertThat(i, isIn(writtenItems2));
		}
		MatcherAssert.assertThat(writtenItems1, hasSize(integerList.size()));
		MatcherAssert.assertThat(writtenItems2, hasSize(integerList.size()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoDelegates() throws Exception {
		// Set up ParallelItemWriter without delegates.
		ParallelItemWriter<Integer> parallelItemWriter = new ParallelItemWriter<Integer>();
		// Add taskExecutor and add it to ParallelItemWriter.
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize(2);
		taskExecutor.setCorePoolSize(2);
		taskExecutor.setQueueCapacity(0);
		parallelItemWriter.setTaskExecutor(taskExecutor);
		// Now that initialization is complete, execute InitializingBean.afterPropertiesSet() 
		// to make sure the required members have been set.
		parallelItemWriter.afterPropertiesSet();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyDelegates() throws Exception {
		// Set up ParallelItemWriter with an empty list of delegates.
		ParallelItemWriter<Integer> parallelItemWriter = new ParallelItemWriter<Integer>();
		parallelItemWriter.setDelegates(Collections.<ItemWriter<Integer>>emptyList());
		// Add taskExecutor and add it to ParallelItemWriter.
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize(2);
		taskExecutor.setCorePoolSize(2);
		taskExecutor.setQueueCapacity(0);
		parallelItemWriter.setTaskExecutor(taskExecutor);
		// Now that initialization is complete, execute InitializingBean.afterPropertiesSet() 
		// to make sure the required members have been set.
		parallelItemWriter.afterPropertiesSet();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoTaskExecutor() throws Exception {
		// Set up ParallelItemWriter with at least one delegate.
		ParallelItemWriter<Integer> parallelItemWriter = new ParallelItemWriter<Integer>();
		ListItemWriter<Integer> listItemWriter1 = new ListItemWriter<Integer>();
		List<ItemWriter<Integer>> delegates = new ArrayList<ItemWriter<Integer>>();
		delegates.add(listItemWriter1);
		parallelItemWriter.setDelegates(delegates);
		// Now that initialization is complete, execute InitializingBean.afterPropertiesSet() 
		// to make sure the required members have been set.
		parallelItemWriter.afterPropertiesSet();
	}
}