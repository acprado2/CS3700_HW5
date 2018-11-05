package hw5.producerconsumer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

public class PCActors 
{
	private static ActorRef buffer;
	
	private static ActorSystem system;
	
	private static AtomicInteger remainingProductions = new AtomicInteger( 0 );
	private static AtomicInteger bufSize = new AtomicInteger( 0 );
	
	// Buffer
	static class Buffer extends AbstractActor
	{
		private int buf = 0;
		
		public Receive createReceive() 
		{
		    return receiveBuilder().match( Integer.class, i -> 
		    { 
		    	// Check capacity
		    	if ( buf + i > 10 || buf + i < 0 )
		    	{
		    		getSender().tell( false, getSelf() );
		    	}
		    	else
		    	{
		    		getSender().tell( true, getSelf() );
		    		buf += i;
		    		bufSize.set( buf );
		    	}
		    } ).build();
		  }
		
		static Props props()
		{
			return Props.create( Buffer.class );
		}
		
		public int sizeOf() { return buf; }
	}
	
	static class Producer extends AbstractActor
	{
		private int productionCount = 0;
		private int threadNum;
		
		public Producer( int threadNum )
		{
			this.threadNum = threadNum;
		}
		
		public Receive createReceive()
		{
			return receiveBuilder().match( Boolean.class, b -> 
		    {
		    	// Check if production was successful
		    	if ( b )
		    	{
		    		System.out.println( "Producer " + threadNum + ": Produced Object " + productionCount );
		    		productionCount++;
		    		remainingProductions.decrementAndGet();
		    	}
		    	
		    	// Check if we need to produce
		    	if ( productionCount < 100 )
		    	{
		    		// Attempt to produce
		    		buffer.tell( 1, getSelf() );
		    	} 	
		    } ).build();
		}
	
		static Props props( int threadNum )
		{
			return Props.create( Producer.class, () -> new Producer( threadNum ) );
		}
	}
	
	static class Consumer extends AbstractActor
	{
		private int threadNum;
		
		public Consumer( int threadNum )
		{
			this.threadNum = threadNum;
		}
		
		public Receive createReceive()
		{
			return receiveBuilder().match( Boolean.class, b -> 
		    {
		    	// Check if production was successful
		    	if ( b )
		    	{
		    		// Success
		    		System.out.println( "Consumer " + threadNum + ": Consumed Object" );
		    		
		    		if ( bufSize.get() > 0 )
		    		{
		    			system.scheduler().scheduleOnce( Duration.create( 1000, TimeUnit.MILLISECONDS ), buffer, -1, system.dispatcher(), getSelf() );
		    		}
		    	}
		    	else
		    	{
		    		// Failure
		    		if ( bufSize.get() > 0 )
		    		{
		    			buffer.tell( -1, getSelf() );
		    		}
		    	}
		    } ).build();
		}
	
		static Props props( int threadNum )
		{
			return Props.create( Consumer.class, () -> new Consumer( threadNum ) );
		}
	}
	
	public static void main( String args[] )
	{
		system = ActorSystem.create( "system" );
		buffer = system.actorOf( Buffer.props(), "buffer" );
		
		long start, end;

		// 5 producers, 2 consumers
		/*start = System.nanoTime();
		
		remainingProductions.set( 500 );
		produceConsume( 5, 2 );		
		while ( remainingProductions.get() > 0 || bufSize.get() > 0  ) {}
		
		end = System.nanoTime();
		
		System.out.println( "5 Producers, 2 Consumers: " + + ( ( float ) end - ( float ) start ) / 1000000000 + " seconds" );*/
		
		// 2 producers, 5 consumers
		start = System.nanoTime();
		
		remainingProductions.set( 200 );
		produceConsume( 2, 5 );
		while ( remainingProductions.get() > 0 || bufSize.get() > 0 ) {}
		
		end = System.nanoTime();

		System.out.println( "2 Producers, 5 Consumers: " + + ( ( float ) end - ( float ) start ) / 1000000000 + " seconds" );

		System.exit( 0 );
	}
	
	private static void produceConsume( int numProducers, int numConsumers ) 
	{
		ActorRef ref;
		
		for ( int i = 0; i < numProducers; i++ )
		{
			ref = system.actorOf( Producer.props( i ), "producer" + i ); 
			ref.tell( false, buffer );			
		}
		
		for ( int i = 0; i < numConsumers; i++ )
		{
			ref = system.actorOf( Consumer.props( i ), "consumer" + i ); 
			ref.tell( false, buffer );
		}
	}
}
