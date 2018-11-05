import java.util.ArrayList;
import java.util.List;


public class PCSynchronized 
{
	// buffer
	public static int buf = 0;
	public static Object lock = new Object();
	
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
			Thread t = new Thread( new producerSynchronized( i + 1 ) );
			t.start();
			producers.add( t );
		}
		
		for ( int i = 0; i < numConsumers; i++ )
		{
			Thread t = new Thread( new consumerSynchronized( i + 1 ) );
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

class producerSynchronized implements Runnable
{
	private int threadNum;
	
	public producerSynchronized( int threadNum )
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
		synchronized ( PCLocks.lock )
		{
			// Buffer is full
			while ( PCLocks.buf == 10 )
			{
				PCLocks.lock.wait();
			}
			
			// Produce
			PCLocks.buf++;
			System.out.println( "Producer " + threadNum + ": Produced object " + num );
			
			// Notify consumers
			PCLocks.lock.notifyAll();
		}
	}
}

class consumerSynchronized implements Runnable
{
	private int threadNum;
	
	public consumerSynchronized( int threadNum )
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
		synchronized ( PCLocks.lock )
		{
			// Buffer is empty
			while ( PCLocks.buf == 0 )
			{
				PCLocks.lock.wait();
			}
			
			// Consume
			PCLocks.buf--;
			System.out.println( "Consumer " + threadNum + ": Consumed Object" );
			
			// Notify producers
			PCLocks.lock.notifyAll();
		}
	}		
}