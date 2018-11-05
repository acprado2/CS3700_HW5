package hw5.sieveoferatosthenes;

public class SESingleThreaded 
{
	public static void main( String args[] )
	{
		int n = 1000001;
		long start, end;
		
		// arrays init to false so we'll assume as such:
		// false = prime
		// true = not prime
		
		boolean primes[] = new boolean[n];
		
		start = System.nanoTime();		
		for ( int i = 2; i * i < n; i++ )
		{
			if ( !primes[i] )
			{
				for ( int j = 2 * i; j < n; j += i )
				{
					primes[j] = true;
				}
			}
		}
		
		// Print
		System.out.println( "Printing prime numbers..." );
		for ( int i = 1; i < n; i++ )
		{
			if ( !primes[i] )
			{
				System.out.println( i );
			}
		}		
		end = System.nanoTime();
		
		System.out.println( "Computation Time: " + + ( ( float ) end - ( float ) start ) / 1000000000 + " seconds" );
	}
}
