/* Clookup_table -- Michael Maguire -- June 94 */

/* SUPERCLASS = CMyRunArray */



#include "Clookup_table.h"

#include <math.h>	/* for sqrt -- note: in compiler settings, "Native Floating Point format" */
					/*  must be set to off or this code bombs for some reason */



/* macro for range checking	*/

#define ASSERT_INDEX( index)	ASSERT( (index > 0)&&(index <= largest_allowable_index))



#define	MIN(x,y)	(((x) > (y)) ? (y) : (x))

#define MAX(x,y)	(((x) < (y)) ? (y) : (x))


#define FIND_POSITION_IN_TABLE( num1, num2 ) 	( ( MAX(num1,num2) - 3) * MAX(num1,num2) / 2 + MIN(num1,num2)  + 1)
	// indices get assigned in the order 21, 31, 32, 41, 42, 43, 51, 52 etc
	// if n1 > n2 are indices then we assign them position (n1^2 - 3n1)/2 + n2 + 1

#define ARRAY_SIZE_NEEDED( number_of_vertices )	( (number_of_vertices)*((number_of_vertices) - 1)/2 )


#define INVERSE_LOOKUP_LARGEST( pos )			( (long) ( ( sqrt( 8.0*((double) (pos) ) - 7.0 ) + 3.0 )/2.0 ) )
#define INVERSE_LOOKUP_SMALLEST( pos, largest )	( (pos) - 1 - ((largest)*(largest) - 3*(largest))/2 )	


/* we defined LAST_POSITION to be some huge number so that vertex gets put at end of list */
#define LAST_POSITION	( (long) 100000 )



#if USE_RUN_ARRAY

int equal_proc( void *item1, void *item2 );


int equal_proc( void *item1, void *item2 )
{

	/* for this class, we will tell MyRunArray that two items are equal iff */
	/*  they are both empty */
	
	
	if( ( * (Boolean *) item1 == TRUE ) && ( * (Boolean *) item2 == TRUE ) )
	{
		return( TRUE );
	}
	else
	{
		return( FALSE );
	}

}

#else

void Clookup_table::InsertValue( void *itemPtr, long index, long count )
{

	long counter;
	
	for( counter = 1; counter <= count; counter++ )
	{
	
		InsertAtIndex( itemPtr, index );
	}

}

#define SetValue( x, y )	SetItem( x, y )
#define GetValue( x, y )	GetItem( x, y )


#endif


void	Clookup_table::initialize	( long element_size, long largest_index )
{


	Boolean		*empty_item;

	long array_size_needed;
	
	

	/* we make a run array for elements which are the required size plus an */
	/* some extra for a Boolean to keep track of whether an entry is empty */
	
#if USE_RUN_ARRAY
	CMyRunArray::IMyRunArray( element_size + sizeof( Boolean ), equal_proc );
#else
	CArray::IArray( element_size + sizeof( Boolean ) );
#endif

	SetBlockSize( 50 );

	largest_allowable_index = largest_index;
	
	
	/* be careful because nothing is needed for a lookup table of size zero or 1 */
	if( largest_allowable_index >= 2 )
	{
	
		array_size_needed = ARRAY_SIZE_NEEDED( largest_allowable_index );

	
		/* now allocate array_size_needed empty items in MyRunArray */
		/* note that we are passing InsertValue an item which is only meaningful */
		/*  for the size of 1 Boolean, but we don't care if it copies garbage for */
		/*  the rest -- the rest is undefined for empty items */

		empty_item = (Boolean *) NewPtr( elementSize );

		*empty_item = TRUE;
		
		InsertValue( (void *) empty_item, 1, array_size_needed );
		
		DisposePtr( (void *) empty_item );
	
	}
	
		
	non_empty_item_count = 0;
	

}



/* GetNumItems {OVERRIDE} */

long 	Clookup_table::GetNumItems( void)
{
	return non_empty_item_count;
	
}


long	Clookup_table::get_largest_item( void )
{

	return( ARRAY_SIZE_NEEDED( largest_allowable_index)  );

}


void 	Clookup_table::make_bigger_by_one_index( void )
{

	Boolean		*empty_item;
	
	largest_allowable_index++;
	
	if( largest_allowable_index >= 2 )
	{

		empty_item = (Boolean *) NewPtr( elementSize );

		*empty_item = TRUE;
		
		InsertValue( (void *) empty_item, LAST_POSITION, ARRAY_SIZE_NEEDED( largest_allowable_index ) - ARRAY_SIZE_NEEDED( largest_allowable_index - 1) );
	
		DisposePtr( (void *) empty_item );
	
	}

}


void	Clookup_table::remove_all_items_for_index( long the_index )
{

	long step, index_counter, current_position;

	ASSERT_INDEX( the_index );


	non_empty_item_count -= total_items_associated_with_index( the_index );



	/* first of all remove any entries with the_index the largest */
	/* e.g. say largest_allowable_index = 7 and the_index = 4   */
	/* then we have 21 31 32 41 42 43 51 52 53 54 61 62 63 64 65 71 72 73 74 75 76 */
	/* we must remove:       ^^ ^^ ^^          ^^          ^^             ^^*/


	/* first we find the position of the 41 location */

	current_position = FIND_POSITION_IN_TABLE( the_index, 1);

	/*  we have 21 31 32 41 42 43 51 52 53 54 61 62 63 64 65 71 72 73 74 75 76 */
	/* first we remove:  ^^ ^^ ^^   */

	for( index_counter = 1; index_counter <= the_index -1; index_counter ++)
	{
		DeleteItem( current_position ); 
	}

	/* then we have 21 31 32 51 52 53 54 61 62 63 64 65 71 72 73 74 75 76 */
	/* now we remove:                 ^^          ^^             ^^  */


	/* note: current_position now points to the 51 position */

	step = the_index - 2;
	
	current_position++;
	
	/* now current_position points to the 52 position */
	
		
	for( index_counter = the_index + 1; index_counter <= largest_allowable_index; index_counter++ )
	{
		
		current_position += step;

		step++;
		
		DeleteItem( current_position );
		
	}

	largest_allowable_index--;

}


long Clookup_table::total_items_associated_with_index( long the_index )
{

	long search_index;
	long total=0;
	
	for( search_index = 1; search_index <= largest_allowable_index; search_index++ )
	{
	
		if( search_index != the_index )
		{
		
			if( is_there_item_between( the_index, search_index ) )
			{
			
				total++;
	
			}
	
		}

	}

	return( total );

}
	

void	Clookup_table::set_item_between( void *item_ptr, long a, long b)
{

	Boolean *boolean_temp_item_ptr;

	if( ! is_there_item_between(a , b) )
	{
		non_empty_item_count++;
	}
	
	boolean_temp_item_ptr = (Boolean *) NewPtr( elementSize );

	
	/* set this item so that it isn't empty */
	*boolean_temp_item_ptr = FALSE;
	
	/* now copy the item_ptr data into the temp_item with the appropriate offset */
	boolean_temp_item_ptr++;

	
	BlockMove( item_ptr, (void *) boolean_temp_item_ptr, elementSize - sizeof(Boolean) );
	boolean_temp_item_ptr--;
	
	
	/* now set the item in MyRunArray*/
	SetValue( (void *) boolean_temp_item_ptr, FIND_POSITION_IN_TABLE( a, b ) );
	
	DisposePtr( (void *) boolean_temp_item_ptr );

}
	
void	Clookup_table::get_item_between( void *item_ptr, long a, long b)
{
	Boolean *boolean_temp_item_ptr;

	


	boolean_temp_item_ptr = (Boolean *) NewPtr( elementSize );
	
	GetValue( (void *) boolean_temp_item_ptr, FIND_POSITION_IN_TABLE( a, b ) );


	boolean_temp_item_ptr++;

	BlockMove( (void *) boolean_temp_item_ptr, item_ptr, elementSize - sizeof(Boolean) );
	
	boolean_temp_item_ptr--;

	DisposePtr( (void *) boolean_temp_item_ptr );

}

	
Boolean	Clookup_table::is_there_item_between( long a, long b )
{

	void *temp_item;
	
	
	ASSERT_INDEX( a );
	ASSERT_INDEX( b );
	ASSERT( a!=b );
	
	
	temp_item = NewPtr( elementSize );
	
	GetValue( temp_item, FIND_POSITION_IN_TABLE( a, b ) );
	
	if( * (Boolean *) temp_item == FALSE )
	{
		/* the desired item was not marked empty */

		DisposePtr( temp_item );

		return( TRUE );
	}
	else
	{

		DisposePtr( temp_item );
	
		return( FALSE );
	
	}

}
	
void	Clookup_table::remove_item_between( long a, long b )
{

	Boolean		*temp_item;

	if( is_there_item_between( a, b ) )
	{
		temp_item = (Boolean *) NewPtr( elementSize );

		*temp_item = TRUE;
		
		SetValue( (void *) temp_item, FIND_POSITION_IN_TABLE( a, b ) );
		
	
		non_empty_item_count--;
		
		DisposePtr( (void *) temp_item );
	
	}

}


long	Clookup_table::next_associated_index( long the_index_being_examined, long the_last_index_we_found )
{

	void	*temp_item;

	long	current_index;
	
	
	current_index = the_last_index_we_found + 1;
	
	while( current_index <= largest_allowable_index )
	{
	
		if( current_index == the_index_being_examined )
		{
		
			current_index++;
		
			continue;	
		
		}
		
		if( is_there_item_between( the_index_being_examined, current_index ) )
		{
		
			return( current_index );
		
		}
	
		current_index++;
		
	}
	
	/* we didn't find any more items */
	
	return( 0 );

}


long	Clookup_table::next_item( long last_item, long *a, long *b )
{

	void	*temp_item;
	
	long	temp_largest_index;
	
	long	total_items;

	long	current_item;
	
	long	temp_index;


	total_items = CArray::GetNumItems();	/* we are looking throught the distinct runs now */


	temp_item = NewPtr( elementSize );
	

	for( current_item = last_item + 1; current_item <= total_items; current_item++)
	{

#if USE_RUN_ARRAY
		GetItemForRun( temp_item, current_item );		
#else
		GetItem( temp_item, current_item );
#endif
		if( * (Boolean *) temp_item == FALSE )
		{

#if USE_RUN_ARRAY
			temp_index = GetValueIndexForRun( current_item );
#else
			temp_index = current_item;
#endif


			*a = temp_largest_index = INVERSE_LOOKUP_LARGEST( temp_index );
			
			*b = INVERSE_LOOKUP_SMALLEST( temp_index, temp_largest_index );



			DisposePtr( temp_item );

			return( current_item );
		
		
		
		}		
		

	}

	DisposePtr( temp_item );

	return( 0 );

}



/******************************************************************************


 DeleteAll {OVERRIDE}
 
 	Delete all elements and runs
******************************************************************************/

void	Clookup_table::DeleteAll( void )
{

	while( ! IsEmpty() )
	{
	
		DeleteItem( (long) 1 );	
		
	}

	non_empty_item_count = 0;
	
	largest_allowable_index = 0;

}	/* Clookup_table::DeleteAll */



/******************************************************************************
 Copy {OVERRIDE}

	Duplicate a Clookup_table.
 ******************************************************************************/

CObject* Clookup_table::Copy( void)
{

	Clookup_table *theCopy;
	
	theCopy = (Clookup_table*) inherited::Copy();
	theCopy->non_empty_item_count = non_empty_item_count;
	theCopy->largest_allowable_index = largest_allowable_index;

	
	return theCopy;

}
