package hw5.sieveoferatosthenes;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class SEActors
{
	private static ActorSystem system;
	private static AtomicInteger count = new AtomicInteger( 0 );
	private static AtomicBoolean bLock;
	
	static class Worker extends AbstractActor
	{
		private int prime;
		private ActorRef next;
		
		public Worker( int prime )
		{
			this.prime = prime;
			count.getAndIncrement();
			//System.out.println( prime );
		}
		
		public Receive createReceive() 
		{
		    return receiveBuilder().match( Integer.class , i -> 
		    { 
		    	if ( i > 0 )
		    	{
		    		if ( i % prime != 0 )
		    		{
			    		if ( next != null )
			    		{
			    			// Potentially a prime number. Pass it down the pipeline
			    			next.tell( i,  ActorRef.noSender() );
			    		}
			    		else
			    		{
			    			// Prime number. Create a new neighbor to pass it to
			    			next = system.actorOf( Worker.props( i ), "worker.p" + i );
			    		}
		    		}
		    	}
		    	else if ( next != null )
		    	{
		    		// End of parsing. Notify neighbor
		    		next.tell( -1 , ActorRef.noSender() );
		    	}
		    	else
		    	{
		    		// No more neighbors. Terminate program.
		    		bLock.set( false );
		    	}
		    	
		    } ).build();
		}
		
		static Props props( int prime )
		{
			return Props.create( Worker.class, () -> new Worker( prime ) );
		}
	}
	
	public static void main( String args[] )
	{
		int n = 1000000;
		long start, end;
		
		system = ActorSystem.create( "system" );
		bLock = new AtomicBoolean( true );
		
		start = System.nanoTime();
		
		// Algorithm starts at 2 so print 1 first
		System.out.println( "1" );
		
		// Create root
		ActorRef root = system.actorOf( Worker.props( 2 ) );
		
		for ( int i = 3; i < n; i++ )
		{
			root.tell( i, ActorRef.noSender() );
		}
		
		// End of algorithm
		root.tell( -1, ActorRef.noSender() );	
		
		// Wait for actors to finish
		while ( bLock.get() ) {}
		
		end = System.nanoTime();
		
		System.out.println( "Computation Time: " + + ( ( float ) end - ( float ) start ) / 1000000000 + " seconds; " + count  );
		
		System.exit( 0 );
	}
}
