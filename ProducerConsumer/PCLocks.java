import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PCLocks 
{
	// buffer
	public static int buf = 0;
	public static Lock lock = new ReentrantLock();
	public static Condition available = lock.newCondition();
	
	public static void main( String args[] ) throws InterruptedException
	{
		long startA, startB, endA, endB;

		// 5 producers, 2 consumers
		startA = System.nanoTime();
		produceConsume( 5, 2 );
		endA = System.nanoTime();
		
		// Let consumers finish
		Thread.sleep( 2000 );
		
		// 2 producers, 5 consumers
		startB = System.nanoTime();
		produceConsume( 2, 5 );
		endB = System.nanoTime();
		
		// Let consumers finish
		Thread.sleep( 2000 );
		
		System.out.println( "5 Producers, 2 Consumers: " + + ( ( float ) endA - ( float ) startA ) / 1000000000 + " seconds" );
		System.out.println( "2 Producers, 5 Consumers: " + + ( ( float ) endB - ( float ) startB ) / 1000000000 + " seconds" );
		
		System.exit( 0 );
	}
	
	private static void produceConsume( int numProducers, int numConsumers ) 
	{
		// Producers
		List<Thread> producers = new ArrayList<>();
		
		for ( int i = 0; i < numProducers; i++ )
		{
			Thread t = new Thread( new producerLocks( i + 1 ) );
			t.start();
			producers.add( t );
		}
		
		for ( int i = 0; i < numConsumers; i++ )
		{
			Thread t = new Thread( new consumerLocks( i + 1 ) );
			t.start();
		}
		
		// Wait until producers terminate
		while ( producers.size() != 0 )
		{
			Thread t = producers.get( 0 );			
			try 
			{
				t.join();
			} 
			catch ( InterruptedException e ) 
			{
				e.printStackTrace();
			}			
			producers.remove( t );
		}
	}
}

class producerLocks implements Runnable
{
	private int threadNum;
	
	public producerLocks( int threadNum )
	{
		this.threadNum = threadNum;
	}
	
	public void run()
	{
		// Produce 100 items
		for ( int i = 0; i < 100; i++ )
		{
			try 
			{
				produce( i );
			} 
			catch ( InterruptedException e ) 
			{
				e.printStackTrace();
			}
		}
	}
	
	private void produce( int num ) throws InterruptedException
	{		
		// Get the lock
		PCLocks.lock.lock();
		try
		{
			// Buffer is full
			while ( PCLocks.buf == 10 )
			{
				PCLocks.available.await();
			}
			
			// Produce
			PCLocks.buf++;
			System.out.println( "Producer " + threadNum + ": Produced object " + num );
			
			// Notify consumers
			PCLocks.available.signalAll();	
		}
		finally
		{
			// Release lock
			PCLocks.lock.unlock();
		}
	}
}

class consumerLocks implements Runnable
{
	private int threadNum;
	
	public consumerLocks( int threadNum )
	{
		this.threadNum = threadNum;
	}
	
	public void run()
	{
		// Keep going until termination
		while( true )
		{
			try
			{
				consume();
				
				// Sleep for 1 second
				Thread.sleep( 1000 );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}
	
	private void consume() throws InterruptedException
	{
		// Get the lock
		PCLocks.lock.lock();
		try
		{
			// Buffer is empty
			while ( PCLocks.buf == 0 )
			{
				PCLocks.available.await();
			}
			
			// Consume
			PCLocks.buf--;
			System.out.println( "Consumer " + threadNum + ": Consumed Object" );
			
			// Notify producers
			PCLocks.available.signalAll();
		}
		finally
		{
			// Release lock
			PCLocks.lock.unlock();
		}
	}	
}
